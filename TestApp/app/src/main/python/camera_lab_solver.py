import sympy as sp
import re
import cv2
import numpy as np
import base64
from sympy.parsing.sympy_parser import standard_transformations, implicit_multiplication_application

# Internationalization Dictionary (Section 10.1)
I18N = {
    "en": {
        "ext_prob": "EXTRACTED PROBLEM",
        "sol_steps": "SOLUTION STEPS",
        "final_ans": "FINAL ANSWER",
        "math_engine": "Mathematical Engine Error",
        "id_eq": "1. Identify Equation",
        "bring_terms": "2. Bring terms to one side",
        "apply_alg": "3. Apply algebraic solver algorithms to find roots.",
        "id_var": "1. Identify the variable of differentiation.",
        "apply_diff": "2. Apply derivative rules (Power rule, Chain rule, etc.).",
        "id_int": "1. Identify the integration variable.",
        "apply_int": "2. Apply integration techniques.",
        "parse_expr": "1. Parse the mathematical expression.",
        "apply_simp": "2. Apply simplification and expansion rules."
    },
    "am": { # Amharic
        "ext_prob": "የወጣ ችግር",
        "sol_steps": "የመፍትሄ እርምጃዎች",
        "final_ans": "የመጨረሻ መልስ",
        "math_engine": "የሂሳብ ሞተር ስህተት",
        "id_eq": "1. እኩልታውን መለየት",
        "bring_terms": "2. ውሎችን ወደ አንድ ጎን አምጣ",
        "apply_alg": "3. ስር ለማግኘት የአልጀብራ ፈቺ ስልተ ቀመሮችን ተግብር።",
        "id_var": "1. የልዩነት ተለዋዋጭን መለየት።",
        "apply_diff": "2. ተዋጽኦ ደንቦችን ተግብር።",
        "id_int": "1. የመዋሃድ ተለዋዋጭን መለየት።",
        "apply_int": "2. የመዋሃድ ዘዴዎችን ተግብር።",
        "parse_expr": "1. የሂሳብ አገላለጹን መተንተን።",
        "apply_simp": "2. የማቅለል እና የማስፋፊያ ደንቦችን ተግብር።"
    }
}

PERIODIC_TABLE = {
    "H": {"mass": 1.008, "name": "Hydrogen"}, "He": {"mass": 4.0026, "name": "Helium"},
    "Li": {"mass": 6.94, "name": "Lithium"}, "Be": {"mass": 9.0122, "name": "Beryllium"},
    "B": {"mass": 10.81, "name": "Boron"}, "C": {"mass": 12.011, "name": "Carbon"},
    "N": {"mass": 14.007, "name": "Nitrogen"}, "O": {"mass": 15.999, "name": "Oxygen"},
    "F": {"mass": 18.998, "name": "Fluorine"}, "Ne": {"mass": 20.180, "name": "Neon"},
    "Na": {"mass": 22.990, "name": "Sodium"}, "Mg": {"mass": 24.305, "name": "Magnesium"},
    "Al": {"mass": 26.982, "name": "Aluminum"}, "Si": {"mass": 28.085, "name": "Silicon"},
    "P": {"mass": 30.974, "name": "Phosphorus"}, "S": {"mass": 32.06, "name": "Sulfur"},
    "Cl": {"mass": 35.45, "name": "Chlorine"}, "Ar": {"mass": 39.948, "name": "Argon"},
    "K": {"mass": 39.098, "name": "Potassium"}, "Ca": {"mass": 40.078, "name": "Calcium"},
    "Fe": {"mass": 55.845, "name": "Iron"}, "Cu": {"mass": 63.546, "name": "Copper"},
    "Zn": {"mass": 65.38, "name": "Zinc"}, "Ag": {"mass": 107.87, "name": "Silver"},
    "Au": {"mass": 196.97, "name": "Gold"}, "Pb": {"mass": 207.2, "name": "Lead"}
}

def preprocess_image(image_base64):
    try:
        img_data = base64.b64decode(image_base64)
        nparr = np.frombuffer(img_data, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        denoised = cv2.GaussianBlur(gray, (5, 5), 0)
        thresh = cv2.adaptiveThreshold(denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)
        coords = np.column_stack(np.where(thresh > 0))
        if coords.size > 0:
            angle = cv2.minAreaRect(coords)[-1]
            if angle < -45: angle = -(90 + angle)
            else: angle = -angle
            (h, w) = img.shape[:2]
            M = cv2.getRotationMatrix2D((w // 2, h // 2), angle, 1.0)
            rotated = cv2.warpAffine(thresh, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
        else:
            rotated = thresh
        final = cv2.bitwise_not(rotated)
        _, buffer = cv2.imencode('.jpg', final)
        return base64.b64encode(buffer).decode('utf-8')
    except:
        return image_base64

def detect_visuals(image_base64):
    try:
        img_data = base64.b64decode(image_base64)
        nparr = np.frombuffer(img_data, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
        _, thresh = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY_INV)
        contours, _ = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        findings = []
        lines = cv2.HoughLinesP(thresh, 1, np.pi/180, 50, minLineLength=100, maxLineGap=20)
        if lines is not None and len(lines) >= 2: findings.append("Graph")
        for cnt in contours:
            if cv2.contourArea(cnt) < 300: continue
            approx = cv2.approxPolyDP(cnt, 0.02*cv2.arcLength(cnt, True), True)
            if len(approx) == 3: findings.append("Triangle")
            elif len(approx) == 4: findings.append("Quadrilateral")
            elif len(approx) > 8: findings.append("Circle")
        return list(set(findings))
    except:
        return []

def solve_math(text, lang="en", max_steps=10):
    t = I18N.get(lang, I18N["en"])
    try:
        clean = text.lower().replace('^', '**').replace('×', '*').replace('÷', '/').replace(' ', '')
        transformations = standard_transformations + (implicit_multiplication_application,)
        
        if "=" in clean:
            parts = clean.split('=')
            lhs = sp.parse_expr(parts[0], transformations=transformations)
            rhs = sp.parse_expr(parts[1], transformations=transformations) if len(parts)>1 and parts[1] else sp.Integer(0)
            eq = sp.Eq(lhs, rhs)
            solutions = sp.solve(eq)
            steps = f"{t['id_eq']}: {lhs} = {rhs}\n{t['bring_terms']}: {sp.simplify(lhs - rhs)} = 0\n{t['apply_alg']}"
            return format_result(text, steps, f"Solutions: {solutions}", lang)
        
        expr = sp.parse_expr(clean, transformations=transformations)
        if "diff" in clean or "'" in clean:
            ans = sp.diff(expr)
            steps = f"{t['id_var']}\n{t['apply_diff']}"
            return format_result(text, steps, f"Derivative: {ans}", lang)
        else:
            simplified = sp.simplify(expr)
            steps = f"{t['parse_expr']}\n{t['apply_simp']}\n   Simplified: {simplified}"
            return format_result(text, steps, f"Result: {simplified}", lang)
    except Exception as e:
        return f"{t['math_engine']}: {str(e)}"

def solve_physics(text, lang="en"):
    # Physics is mostly formula based, but step descriptions can be translated if needed
    t = I18N.get(lang, I18N["en"])
    formulas = {
        r"f\s*=\s*ma": ("Newton's 2nd Law", "F = m * a"),
        r"v\s*=\s*u\s*\+\s*at": ("Kinematics", "v = u + at"),
        r"v\s*=\s*ir": ("Ohm's Law", "V = I * R")
    }
    clean = text.lower().replace(" ", "").replace("*", "")
    numbers = re.findall(r"(\d+\.?\d*)", text)
    
    for pattern, (name, formula) in formulas.items():
        if re.search(pattern, clean):
            steps = f"1. Domain: {name}\n2. Formula: {formula}\n3. Identify parameters from context."
            if len(numbers) >= 2:
                res = float(numbers[0]) * float(numbers[1])
                return format_result(text, steps, f"Calculation: {res} (Verify units)", lang)
            return format_result(text, steps, f"Formula detected. Provide variables.", lang)
    return format_result(text, "Analyzing physics context...", "Provide values or explicit formula.", lang)

def solve_chemistry(text, lang="en"):
    if "->" in text or "→" in text:
        return format_result(text, "1. Balance atoms.\n2. Calculate moles.", "Reaction recognized.", lang)
    pattern = r"([A-Z][a-z]?)(\d*)"
    matches = re.findall(pattern, text)
    if matches:
        total_mass = 0
        res_list = []
        for sym, count in matches:
            if sym in PERIODIC_TABLE:
                c = int(count) if count else 1
                mass = PERIODIC_TABLE[sym]["mass"]
                total_mass += mass * c
                res_list.append(f"{sym}x{c}")
        if res_list:
            return format_result(text, "Summing atomic masses...", f"Molar Mass: {round(total_mass, 4)} g/mol", lang)
    return format_result(text, "Scanning Periodic Table...", "Ready for Chemistry analysis.", lang)

def solve_geometry(text, visual_results, lang="en"):
    shapes = ", ".join(visual_results) if visual_results else "Geometry context"
    steps = f"1. Visual: {shapes} detected.\n2. Mapping theorems."
    return format_result(text, steps, "Ready for dimensions.", lang)

def format_result(problem, steps, answer, lang="en"):
    t = I18N.get(lang, I18N["en"])
    res = f"🔍 {t['ext_prob']}\n{problem}\n\n"
    res += f"📝 {t['sol_steps']}\n{steps}\n\n"
    res += f"✅ {t['final_ans']}\n{answer}"
    return res

def generate_similar(text, mode):
    samples = {
        "MATH": "Practice: Solve 2x + 5 = 15",
        "PHYSICS": "Practice: m=10kg, a=5m/s2. Find Force.",
        "CHEMISTRY": "Practice: Find mass of NaCl.",
        "GEOMETRY": "Practice: Area of circle r=7."
    }
    return samples.get(mode, "Explore related modules.")
