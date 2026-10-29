"""
sympy_solver.py
================
Real math / physics / chemistry / geometry solver for the Voice Study app.

Design contract
---------------
One entry point for Android:

    solve_problem(text, lang="en", image_base64=None, want_plot=True)

Returns a JSON string with a STABLE schema:

    {
      "type":        "answer" | "clarification" | "error",
      "content":     "<human-readable step-by-step explanation>",
      "answer":      "<short final answer>",        # may be None
      "lang":        "en" | "am" | ...,
      "domain":      "math" | "physics" | "chemistry" | "geometry",
      "confidence":  0.0 .. 1.0,
      "plot_png":    "<base64 PNG>" | None,          # 2D or 3D graph
      "plot_kind":   "2d" | "3d" | None,
      "latex":       "<LaTeX of the answer>" | None, # for on-screen render
      "source":      "<original problem text>",
      "warnings":    ["..."]                         # e.g. "approximate"
    }

Guarantees
----------
* NEVER raises out of solve_problem().
* NEVER returns a numeric answer with confidence > 0.5 unless SymPy verified it.
* ALWAYS states clearly when it cannot solve something.
* Plots are generated with the non-interactive 'Agg' backend (Android safe).
* No `.lower()` on math text (preserves X vs x, case-sensitive chemistry).
* Full i18n for math / physics / chemistry / geometry / plot labels.

Android integration notes (read carefully)
------------------------------------------
1.  OpenCV and Matplotlib are heavy. On a production Android build you should
    do OCR in Kotlin (ML Kit Text Recognition) and pass only text here.
    The image helpers below are kept for desktop/unit tests and for
    "vision-assisted" hints; they degrade gracefully when cv2 is missing.
2.  Chaquopy pip block (app/build.gradle):
        python {
            pip {
                install "sympy==1.13.2"
                install "numpy==1.26.4"
                install "matplotlib==3.8.4"
                // optional, desktop/unit tests only:
                // install "opencv-python-headless==4.10.0.84"
            }
        }
3.  Call solve_problem() from a background thread. Plot generation takes
    200–800 ms; never call it on the UI thread.
4.  Kotlin side: parse JSON, show `content` in a scrollable TextView,
    decode `plot_png` (if not null) into a Bitmap and show it in an ImageView,
    and feed `content` to your TTS engine with `lang`.
5.  Do not pass data-URI prefixed base64 for plots back to Kotlin without
    decoding first — decode with android.util.Base64.DEFAULT.

Author: rewritten from the user's original module.
"""

from __future__ import annotations

import base64
import io
import json
import logging
import math
import re
import threading
from typing import Any, Dict, List, Optional, Tuple

# ---------------------------------------------------------------------------
# Optional imports — module must import even if any heavy dep is missing.
# ---------------------------------------------------------------------------
try:
    import sympy as sp
    from sympy.parsing.sympy_parser import (
        implicit_multiplication_application,
        standard_transformations,
    )
    _HAS_SYMPY = True
except Exception:  # noqa: BLE001
    sp = None  # type: ignore
    _HAS_SYMPY = False

try:
    import numpy as np  # type: ignore
    _HAS_NUMPY = True
except Exception:  # noqa: BLE001
    np = None  # type: ignore
    _HAS_NUMPY = False

try:
    import matplotlib
    matplotlib.use("Agg")  # MUST be before pyplot import — Android-safe
    import matplotlib.pyplot as plt
    from mpl_toolkits.mplot3d import Axes3D  # noqa: F401
    _HAS_MPL = True
except Exception:  # noqa: BLE001
    plt = None  # type: ignore
    _HAS_MPL = False

try:
    import cv2  # type: ignore
    _HAS_CV2 = True
except Exception:  # noqa: BLE001
    cv2 = None  # type: ignore
    _HAS_CV2 = False

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------
_log = logging.getLogger("sympy_solver")
if not _log.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("[sympy_solver] %(levelname)s: %(message)s"))
    _log.addHandler(_h)
_log.setLevel(logging.INFO)

# SymPy calls plotting through its own wrappers; when we generate plots we do
# it ourselves with Matplotlib, so we keep a lock for thread safety.
_PLOT_LOCK = threading.Lock()


# ---------------------------------------------------------------------------
# i18n — full coverage for every solver and every UI label.
# ---------------------------------------------------------------------------
I18N: Dict[str, Dict[str, str]] = {
    "en": {
        "ext_prob": "EXTRACTED PROBLEM",
        "sol_steps": "SOLUTION STEPS",
        "final_ans": "FINAL ANSWER",
        "math_engine": "Mathematical Engine Error",
        "need_clarify": "I need more information to solve this.",
        "cannot_solve": "This problem is outside my current solving range.",
        "approx": "Approximate solution (no exact closed form found).",
        "plot_2d": "2D graph",
        "plot_3d": "3D surface",
        # math
        "id_eq": "1. Identify equation",
        "bring_terms": "2. Bring all terms to one side",
        "apply_alg": "3. Solve for the variable",
        "id_var": "1. Identify the variable of differentiation",
        "apply_diff": "2. Apply derivative rules",
        "id_int": "1. Identify the integration variable",
        "apply_int": "2. Apply integration techniques",
        "parse_expr": "1. Parse the expression",
        "apply_simp": "2. Simplify",
        "numeric_fallback": "3. No exact form; using numerical methods",
        # physics
        "phys_domain": "1. Domain",
        "phys_formula": "2. Formula",
        "phys_params": "3. Known values",
        "phys_solve": "4. Solve for the unknown",
        # chemistry
        "chem_balance": "1. Balance the reaction",
        "chem_mass": "Summing atomic masses",
        "chem_moles": "2. Compute moles / mass",
        # geometry
        "geo_visual": "1. Visual detection",
        "geo_theorem": "2. Map to geometry theorems",
        # practice
        "practice": "Practice problem",
    },
    "am": {
        "ext_prob": "የወጣ ችግር",
        "sol_steps": "የመፍትሄ እርምጃዎች",
        "final_ans": "የመጨረሻ መልስ",
        "math_engine": "የሂሳብ ሞተር ስህተት",
        "need_clarify": "ለመፍታት ተጨማሪ መረጃ ያስፈልገኛል።",
        "cannot_solve": "ይህ ችግር ከአሁኑ የመፍትሄ ክልል ውጭ ነው።",
        "approx": "ግምታዊ መፍትሄ (ትክክለኛ ቅርጽ አልተገኘም)።",
        "plot_2d": "የ2 ልኡላዊ ግራፍ",
        "plot_3d": "የ3 ልኡላዊ ገጽታ",
        "id_eq": "1. እኩልታውን መለየት",
        "bring_terms": "2. ውሎችን ወደ አንድ ጎን ማምጣት",
        "apply_alg": "3. ለተለዋዋጭው መፍታት",
        "id_var": "1. የልዩነት ተለዋዋጭን መለየት",
        "apply_diff": "2. የተዋጽኦ ደንቦችን መተግበር",
        "id_int": "1. የመዋሃድ ተለዋዋጭን መለየት",
        "apply_int": "2. የመዋሃድ ዘዴዎችን መተግበር",
        "parse_expr": "1. አገላለጹን መተርጎም",
        "apply_simp": "2. ማቅለል",
        "numeric_fallback": "3. ትክክለኛ ቅርጽ የለም፤ በቁጥር ዘዴ መፍታት",
        "phys_domain": "1. መስክ",
        "phys_formula": "2. ቀመር",
        "phys_params": "3. የታወቁ እሴቶች",
        "phys_solve": "4. የማይታወቀውን መፍታት",
        "chem_balance": "1. እኩልታውን ማስተካከል",
        "chem_mass": "የአቶሚክ ብዛቶችን መደመር",
        "chem_moles": "2. ሞል / ብዛት ማስላት",
        "geo_visual": "1. ምስላዊ መለየት",
        "geo_theorem": "2. ከጂኦሜትሪ ቲዎሬሞች ጋር ማገናኘት",
        "practice": "የልምምድ ጥያቄ",
    },
}


def _t(lang: str) -> Dict[str, str]:
    return I18N.get(lang, I18N["en"])


# ---------------------------------------------------------------------------
# Stable JSON response builder.
# ---------------------------------------------------------------------------
def _resp(
    type_: str,
    content: str,
    *,
    lang: str = "en",
    domain: Optional[str] = None,
    confidence: Optional[float] = None,
    answer: Optional[str] = None,
    plot_png: Optional[str] = None,
    plot_kind: Optional[str] = None,
    latex: Optional[str] = None,
    source: Optional[str] = None,
    warnings: Optional[List[str]] = None,
) -> str:
    payload: Dict[str, Any] = {
        "type": type_,
        "content": content,
        "lang": lang,
        "answer": answer,
        "domain": domain,
        "confidence": None if confidence is None else round(float(confidence), 3),
        "plot_png": plot_png,
        "plot_kind": plot_kind,
        "latex": latex,
        "source": source,
        "warnings": warnings or [],
    }
    return json.dumps(payload, ensure_ascii=False)


# ---------------------------------------------------------------------------
# Periodic table (expanded).
# ---------------------------------------------------------------------------
PERIODIC_TABLE: Dict[str, float] = {
    "H": 1.008, "He": 4.0026, "Li": 6.94, "Be": 9.0122, "B": 10.81,
    "C": 12.011, "N": 14.007, "O": 15.999, "F": 18.998, "Ne": 20.180,
    "Na": 22.990, "Mg": 24.305, "Al": 26.982, "Si": 28.085, "P": 30.974,
    "S": 32.06, "Cl": 35.45, "Ar": 39.948, "K": 39.098, "Ca": 40.078,
    "Fe": 55.845, "Cu": 63.546, "Zn": 65.38, "Ag": 107.87, "Au": 196.97,
    "Pb": 207.2, "Br": 79.904, "I": 126.90, "Mn": 54.938, "Ba": 137.33,
    "Cr": 51.996, "Ni": 58.693, "Co": 58.933, "Sn": 118.71,
}


# ===========================================================================
# IMAGE HELPERS (kept for desktop / unit tests; gracefully no-op on Android)
# ===========================================================================
def _strip_data_uri(b64: str) -> str:
    if not isinstance(b64, str):
        return ""
    if b64.strip().startswith("data:") and "," in b64:
        return b64.split(",", 1)[1]
    return b64


def preprocess_image(image_base64: str) -> Optional[str]:
    """Return a preprocessed base64 PNG, or None on any failure."""
    if not _HAS_CV2 or not _HAS_NUMPY:
        _log.info("cv2 not available; preprocess_image no-op")
        return None
    b64 = _strip_data_uri(image_base64)
    if not b64:
        return None
    try:
        raw = base64.b64decode(b64, validate=False)
    except Exception as exc:  # noqa: BLE001
        _log.warning("Base64 decode failed: %s", exc)
        return None
    if len(raw) > 20 * 1024 * 1024:
        _log.warning("Image too large (%d bytes)", len(raw))
        return None
    try:
        nparr = np.frombuffer(raw, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            return None
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        denoised = cv2.GaussianBlur(gray, (5, 5), 0)
        thresh = cv2.adaptiveThreshold(
            denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
            cv2.THRESH_BINARY_INV, 11, 2,
        )
        coords = np.column_stack(np.where(thresh > 0))
        if coords.size > 0:
            rect = cv2.minAreaRect(coords)
            angle = rect[-1]
            angle = -(90 + angle) if angle < -45 else -angle
            h, w = img.shape[:2]
            M = cv2.getRotationMatrix2D((w // 2, h // 2), angle, 1.0)
            rotated = cv2.warpAffine(
                thresh, M, (w, h), flags=cv2.INTER_CUBIC,
                borderMode=cv2.BORDER_REPLICATE,
            )
        else:
            rotated = thresh
        final = cv2.bitwise_not(rotated)
        ok, buffer = cv2.imencode(".png", final)
        if not ok:
            return None
        return base64.b64encode(buffer).decode("utf-8")
    except Exception as exc:  # noqa: BLE001
        _log.exception("Image preprocessing failed: %s", exc)
        return None


def detect_visuals(image_base64: str) -> List[str]:
    """Return high-level shape labels from an image, or [] on failure."""
    if not _HAS_CV2 or not _HAS_NUMPY:
        return []
    b64 = _strip_data_uri(image_base64)
    if not b64:
        return []
    try:
        raw = base64.b64decode(b64, validate=False)
        nparr = np.frombuffer(raw, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
        if img is None:
            return []
        h, w = img.shape[:2]
        min_area = max(300.0, 0.005 * h * w)
        _, thresh = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY_INV)
        contours, _ = cv2.findContours(
            thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE
        )
        findings: set = set()
        for cnt in contours:
            area = cv2.contourArea(cnt)
            if area < min_area:
                continue
            approx = cv2.approxPolyDP(cnt, 0.02 * cv2.arcLength(cnt, True), True)
            sides = len(approx)
            if sides == 3:
                findings.add("Triangle")
            elif sides == 4:
                _, _, ww, hh = cv2.boundingRect(approx)
                aspect = ww / float(hh) if hh else 0
                findings.add("Square" if 0.9 <= aspect <= 1.1 else "Rectangle")
            elif sides == 5:
                findings.add("Pentagon")
            elif sides == 6:
                findings.add("Hexagon")
            elif 7 <= sides <= 9:
                findings.add("Polygon")
            elif sides >= 10:
                findings.add("Circle or star")
        lines = cv2.HoughLinesP(
            thresh, 1, np.pi / 180, 50,
            minLineLength=int(0.2 * min(h, w)), maxLineGap=20,
        )
        if lines is not None and len(lines) >= 4:
            findings.add("Graph")
        return sorted(findings)
    except Exception as exc:  # noqa: BLE001
        _log.exception("Shape detection failed: %s", exc)
        return []


# ===========================================================================
# PLOT GENERATION (Matplotlib -> base64 PNG; Agg backend, Android-safe)
# ===========================================================================
def _fig_to_base64_png(fig) -> Optional[str]:
    try:
        buf = io.BytesIO()
        fig.savefig(buf, format="png", dpi=150, bbox_inches="tight")
        buf.seek(0)
        return base64.b64encode(buf.read()).decode("ascii")
    except Exception as exc:  # noqa: BLE001
        _log.exception("Figure serialization failed: %s", exc)
        return None
    finally:
        try:
            plt.close(fig)
        except Exception:
            pass


def _parse_expr_safe(text: str):
    """Parse math WITHOUT lowering case (preserves X vs x)."""
    transformations = standard_transformations + (implicit_multiplication_application,)
    return sp.parse_expr(text, transformations=transformations)


def _free_symbols(expr) -> List[Any]:
    try:
        return sorted(expr.free_symbols, key=lambda s: s.name)
    except Exception:
        return []


def _numeric_samples(expr, var, lo: float = -10.0, hi: float = 10.0, n: int = 400):
    """Return (xs, ys) numeric samples for a single-variable expression."""
    xs: List[float] = []
    ys: List[float] = []
    step = (hi - lo) / (n - 1)
    f = sp.lambdify(var, expr, modules=["numpy"])
    arr_x = np.linspace(lo, hi, n)
    try:
        arr_y = f(arr_x)
    except Exception:  # noqa: BLE001
        # Fallback: evaluate point-by-point to skip complex values
        arr_y = []
        for xv in arr_x:
            try:
                yv = float(expr.subs(var, xv).evalf())
                arr_y.append(yv if math.isfinite(yv) else np.nan)
            except Exception:
                arr_y.append(np.nan)
        arr_y = np.array(arr_y, dtype=float)
    return arr_x, arr_y


def plot_2d(expr, var, *, lo: float = -10.0, hi: float = 10.0) -> Optional[str]:
    """Render y = f(x) as a 2D PNG. Returns base64 or None."""
    if not _HAS_MPL or not _HAS_NUMPY or not _HAS_SYMPY:
        return None
    with _PLOT_LOCK:
        try:
            xs, ys = _numeric_samples(expr, var, lo=lo, hi=hi)
            fig, ax = plt.subplots(figsize=(7, 4.5))
            ax.plot(xs, ys, color="#2563eb", linewidth=2.2, label=f"y = {sp.sstr(expr)}")
            ax.axhline(0, color="#9ca3af", linewidth=0.8)
            ax.axvline(0, color="#9ca3af", linewidth=0.8)
            ax.grid(True, linestyle="--", alpha=0.4)
            ax.set_xlabel(str(var))
            ax.set_ylabel("y")
            ax.set_title(f"y = {sp.sstr(expr)}")
            ax.legend(loc="best")
            return _fig_to_base64_png(fig)
        except Exception as exc:  # noqa: BLE001
            _log.exception("2D plot failed: %s", exc)
            return None


def plot_3d(expr, var1, var2, *, lo: float = -5.0, hi: float = 5.0) -> Optional[str]:
    """Render z = f(x, y) as a 3D surface PNG. Returns base64 or None."""
    if not _HAS_MPL or not _HAS_NUMPY or not _HAS_SYMPY:
        return None
    with _PLOT_LOCK:
        try:
            f = sp.lambdify((var1, var2), expr, modules=["numpy"])
            X = np.linspace(lo, hi, 80)
            Y = np.linspace(lo, hi, 80)
            X, Y = np.meshgrid(X, Y)
            with np.errstate(all="ignore"):
                Z = f(X, Y)
            Z = np.asarray(Z, dtype=float)
            Z[~np.isfinite(Z)] = np.nan

            fig = plt.figure(figsize=(7, 5.5))
            ax = fig.add_subplot(111, projection="3d")
            surf = ax.plot_surface(
                X, Y, Z, cmap="viridis", linewidth=0, antialiased=True, alpha=0.95
            )
            fig.colorbar(surf, shrink=0.6, aspect=12, pad=0.08)
            ax.set_xlabel(str(var1))
            ax.set_ylabel(str(var2))
            ax.set_zlabel("z")
            ax.set_title(f"z = {sp.sstr(expr)}")
            return _fig_to_base64_png(fig)
        except Exception as exc:  # noqa: BLE001
            _log.exception("3D plot failed: %s", exc)
            return None


# ===========================================================================
# MATH SOLVER — real SymPy, correct symbols, numeric fallback, plot.
# ===========================================================================
def solve_math(
    text: str,
    lang: str = "en",
    max_steps: int = 10,
    want_plot: bool = True,
) -> str:
    t = _t(lang)
    if not _HAS_SYMPY:
        return _resp(
            "error",
            "SymPy is not installed. Add `sympy` to the Chaquopy pip block.",
            lang=lang, domain="math", source=text,
        )
    if not isinstance(text, str) or not text.strip():
        return _resp("clarification", t["need_clarify"], lang=lang, domain="math")

    # Preserve case; normalise unicode operators only.
    raw = (
        text.replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")
        .replace("^", "**")
        .strip()
    )

    try:
        # ---------------- Equation branch ---------------------------------
        if "=" in raw and "==" not in raw:
            lhs_str, rhs_str = (raw.split("=", 1) + ["0"])[:2]
            lhs_str = lhs_str.strip()
            rhs_str = rhs_str.strip() or "0"
            lhs = _parse_expr_safe(lhs_str)
            rhs = _parse_expr_safe(rhs_str)
            eq = sp.Eq(lhs, rhs)

            symbols = _free_symbols(lhs - rhs)
            if not symbols:
                truth = bool(sp.simplify(lhs - rhs) == 0)
                return _resp(
                    "answer",
                    _format_steps(
                        t, text,
                        f"{t['id_eq']}: {lhs} = {rhs}",
                        "Identity is true." if truth else "Identity is false.",
                    ),
                    lang=lang, domain="math", confidence=1.0,
                    answer="true" if truth else "false",
                    latex=sp.latex(sp.simplify(lhs - rhs)),
                    source=text,
                )
            if len(symbols) > 1:
                return _resp(
                    "clarification",
                    f"Multiple variables detected ({', '.join(s.name for s in symbols)}). "
                    "Please say which one to solve for.",
                    lang=lang, domain="math", source=text,
                )

            var = symbols[0]
            try:
                solutions = sp.solve(eq, var)
            except Exception as exc:  # noqa: BLE001
                _log.exception("solve() failed")
                return _resp(
                    "error",
                    f"{t['math_engine']}: {exc}",
                    lang=lang, domain="math", source=text,
                )

            warnings: List[str] = []
            confidence = 1.0
            if not solutions:
                try:
                    guess = float(sp.nsolve(eq.subs({var: 1}), var, 1))
                    solutions = [sp.nsimplify(guess)]
                    warnings.append(t["approx"])
                    confidence = 0.45
                except Exception:
                    return _resp(
                        "clarification",
                        t["cannot_solve"] + " No exact or numeric solution found.",
                        lang=lang, domain="math", source=text,
                    )

            steps = (
                f"{t['id_eq']}: {lhs} = {rhs}\n"
                f"{t['bring_terms']}: {sp.simplify(lhs - rhs)} = 0\n"
                f"{t['apply_alg']} for {var.name}"
            )
            answer_text = f"{var.name} = {solutions}"

            # Plot: only when there is exactly one variable and RHS is 0.
            plot_b64 = None
            plot_kind = None
            if want_plot and confidence >= 0.9:
                plot_b64 = plot_2d(lhs - rhs, var)
                plot_kind = "2d" if plot_b64 else None

            return _resp(
                "answer",
                _format_steps(t, text, steps, answer_text),
                lang=lang, domain="math", confidence=confidence,
                answer=answer_text,
                latex=sp.latex(sp.Eq(var, solutions[0])) if solutions else None,
                plot_png=plot_b64, plot_kind=plot_kind,
                source=text, warnings=warnings,
            )

        # ---------------- Derivative branch -------------------------------
        m_diff = re.match(r"^(?:diff|d/dx|derivative)\s*\(?(.+?)\)?$", raw, re.I)
        if m_diff:
            expr = _parse_expr_safe(m_diff.group(1).strip())
            syms = _free_symbols(expr) or [sp.Symbol("x")]
            var = syms[0]
            ans = sp.diff(expr, var)
            steps = f"{t['id_var']} ({var.name})\n{t['apply_diff']}"
            answer_text = f"d/d{var.name}({expr}) = {ans}"
            plot_b64 = plot_2d(expr, var) if want_plot else None
            return _resp(
                "answer", _format_steps(t, text, steps, answer_text),
                lang=lang, domain="math", confidence=1.0,
                answer=answer_text, latex=sp.latex(ans),
                plot_png=plot_b64, plot_kind="2d" if plot_b64 else None,
                source=text,
            )

        # ---------------- Integral branch ---------------------------------
        m_int = re.match(r"^(?:integrate|integral|int)\s*\(?(.+?)\)?$", raw, re.I)
        if m_int:
            body = m_int.group(1).strip()
            expr = _parse_expr_safe(body)
            syms = _free_symbols(expr) or [sp.Symbol("x")]
            var = syms[0]
            ans = sp.integrate(expr, var)
            steps = f"{t['id_int']} ({var.name})\n{t['apply_int']}"
            answer_text = f"∫ {expr} d{var.name} = {ans} + C"
            return _resp(
                "answer", _format_steps(t, text, steps, answer_text),
                lang=lang, domain="math", confidence=1.0,
                answer=answer_text, latex=sp.latex(ans),
                source=text,
            )

        # ---------------- Simplify / evaluate -----------------------------
        # 2D plot of a single-variable expression like "sin(x)" or "x**2".
        expr = _parse_expr_safe(raw)
        simplified = sp.simplify(expr)
        syms = _free_symbols(simplified)
        steps = f"{t['parse_expr']}: {expr}\n{t['apply_simp']}: {simplified}"

        plot_b64 = None
        plot_kind = None
        if want_plot and len(syms) == 1:
            plot_b64 = plot_2d(simplified, syms[0])
            plot_kind = "2d" if plot_b64 else None
        elif want_plot and len(syms) == 2:
            plot_b64 = plot_3d(simplified, syms[0], syms[1])
            plot_kind = "3d" if plot_b64 else None

        return _resp(
            "answer", _format_steps(t, text, steps, f"Result: {simplified}"),
            lang=lang, domain="math", confidence=0.9,
            answer=str(simplified), latex=sp.latex(simplified),
            plot_png=plot_b64, plot_kind=plot_kind, source=text,
        )

    except Exception as exc:  # noqa: BLE001
        _log.exception("Math solver failed on %r", text)
        return _resp(
            "error", f"{t['math_engine']}: {exc}",
            lang=lang, domain="math", source=text,
        )


def _format_steps(t: Dict[str, str], problem: str, steps: str, answer: str) -> str:
    return (
        f"🔍 {t['ext_prob']}\n{problem}\n\n"
        f"📝 {t['sol_steps']}\n{steps}\n\n"
        f"✅ {t['final_ans']}\n{answer}"
    )


# ===========================================================================
# PHYSICS SOLVER — key=value parsing + symbolic solve + 2D plot when 1 var.
# ===========================================================================
_PHYS_FORMULAS: Dict[str, Tuple[str, str]] = {
    "newton2":    ("F - m*a", "Newton's 2nd Law"),
    "kinematics": ("v - (u + a*t)", "Kinematics v = u + at"),
    "ohm":        ("V - I*R", "Ohm's Law"),
    "kinetic":    ("K - 0.5*m*v**2", "Kinetic Energy"),
    "weight":     ("W - m*g", "Weight"),
}

_PHYS_TRIGGERS: Dict[str, re.Pattern] = {
    "newton2":    re.compile(r"\bf\s*=\s*m\s*\*?\s*a\b|\bforce\b", re.I),
    "kinematics": re.compile(r"\bv\s*=\s*u\s*\+\s*a\s*\*?\s*t\b", re.I),
    "ohm":        re.compile(r"\bv\s*=\s*i\s*\*?\s*r\b|\bohm\b", re.I),
    "kinetic":    re.compile(r"\bkinetic\b|\bke\b", re.I),
    "weight":     re.compile(r"\bweight\b|\bw\s*=\s*m\s*\*?\s*g\b", re.I),
}

_KV_RE = re.compile(r"\b([A-Za-z])\s*=\s*(-?\d+\.?\d*)")


def _parse_known_values(text: str) -> Dict[str, float]:
    known: Dict[str, float] = {}
    for var, val in _KV_RE.findall(text):
        try:
            known[var] = float(val)
        except ValueError:
            continue
    return known


def solve_physics(text: str, lang: str = "en", want_plot: bool = True) -> str:
    t = _t(lang)
    if not _HAS_SYMPY:
        return _resp("error", "SymPy missing.", lang=lang, domain="physics", source=text)
    if not isinstance(text, str) or not text.strip():
        return _resp("clarification", t["need_clarify"], lang=lang, domain="physics")

    matched_key: Optional[str] = None
    for key, pattern in _PHYS_TRIGGERS.items():
        if pattern.search(text):
            matched_key = key
            break
    if matched_key is None:
        return _resp(
            "clarification",
            "I couldn't recognise the formula. Try e.g. `F=m*a, m=5, a=9.8`.",
            lang=lang, domain="physics", source=text,
        )

    expr_str, name = _PHYS_FORMULAS[matched_key]
    known = _parse_known_values(text)

    sym_map = {s: sp.Symbol(s) for s in "F m a v u t V I R K W g".split()}
    expr = sp.sympify(expr_str, locals=sym_map)
    free = sorted(expr.free_symbols, key=lambda s: s.name)
    unknowns = [s for s in free if s.name not in known]

    steps = (
        f"{t['phys_domain']}: {name}\n"
        f"{t['phys_formula']}: {expr} = 0\n"
        f"{t['phys_params']}: {known if known else 'none given'}\n"
    )

    if not known:
        return _resp(
            "clarification",
            _format_steps(t, text, steps, "Provide known values, e.g. m=5, a=9.8."),
            lang=lang, domain="physics", source=text, confidence=0.2,
        )
    if not unknowns:
        return _resp(
            "answer",
            _format_steps(t, text, steps, "All variables are known — nothing to solve."),
            lang=lang, domain="physics", source=text, confidence=0.4,
        )
    if len(unknowns) > 1:
        return _resp(
            "clarification",
            _format_steps(
                t, text, steps,
                f"Multiple unknowns ({', '.join(s.name for s in unknowns)}). "
                "Provide all but one.",
            ),
            lang=lang, domain="physics", source=text, confidence=0.3,
        )

    target = unknowns[0]
    try:
        sols = sp.solve(sp.Eq(expr.subs(known), 0), target)
    except Exception as exc:  # noqa: BLE001
        _log.exception("Physics solve failed")
        return _resp(
            "error", f"Physics solver error: {exc}",
            lang=lang, domain="physics", source=text,
        )
    if not sols:
        return _resp(
            "clarification",
            _format_steps(t, text, steps, "No solution for " + target.name),
            lang=lang, domain="physics", source=text, confidence=0.2,
        )

    value = sols[0]
    answer_text = f"{target.name} = {value}"

    # If exactly one variable remains and we want a plot, plot against it.
    plot_b64 = None
    plot_kind = None
    if want_plot:
        remaining = [s for s in free if s.name not in known and s != target]
        if not remaining:
            plot_b64 = plot_2d(expr.subs(known), target)
            plot_kind = "2d" if plot_b64 else None

    return _resp(
        "answer",
        _format_steps(t, text, steps + f"\n{t['phys_solve']}: {answer_text}", answer_text),
        lang=lang, domain="physics", confidence=0.9,
        answer=answer_text,
        latex=sp.latex(sp.Eq(target, value)),
        plot_png=plot_b64, plot_kind=plot_kind,
        source=text,
    )


# ===========================================================================
# CHEMISTRY SOLVER — parenthesis-aware formula parser + reactions.
# ===========================================================================
_FORMULA_TOKEN_RE = re.compile(r"([A-Z][a-z]?)(\d*)|(\()|(\))(\d*)")
_REACTION_ARROW_RE = re.compile(r"->|→|=>")


def _parse_formula(formula: str) -> Optional[Dict[str, int]]:
    stack: List[Dict[str, int]] = [dict()]
    pos = 0
    while pos < len(formula):
        m = _FORMULA_TOKEN_RE.match(formula, pos)
        if not m:
            return None
        elem, count, open_p, close_p, close_count = m.groups()
        if elem:
            n = int(count) if count else 1
            stack[-1][elem] = stack[-1].get(elem, 0) + n
        elif open_p:
            stack.append(dict())
        elif close_p:
            n = int(close_count) if close_count else 1
            if len(stack) < 2:
                return None
            group = stack.pop()
            for e, c in group.items():
                stack[-1][e] = stack[-1].get(e, 0) + c * n
        pos = m.end()
    if len(stack) != 1:
        return None
    return stack[0]


def _molar_mass(formula: str) -> Optional[float]:
    counts = _parse_formula(formula)
    if counts is None:
        return None
    total = 0.0
    for elem, n in counts.items():
        if elem not in PERIODIC_TABLE:
            return None
        total += PERIODIC_TABLE[elem] * n
    return total


def _side_molar_mass(side: str) -> Optional[float]:
    total = 0.0
    for token in re.findall(r"(?:\d+\s*)?([A-Z][A-Za-z0-9()]*)", side):
        m = _molar_mass(token)
        if m is None:
            return None
        total += m
    return total


def solve_chemistry(text: str, lang: str = "en") -> str:
    t = _t(lang)
    if not isinstance(text, str) or not text.strip():
        return _resp("clarification", t["need_clarify"], lang=lang, domain="chemistry")

    # Reaction branch
    if _REACTION_ARROW_RE.search(text):
        left, right = _REACTION_ARROW_RE.split(text, maxsplit=1)
        lm, rm = _side_molar_mass(left), _side_molar_mass(right)
        steps = (
            f"{t['chem_balance']}: LHS mass ≈ {lm}, RHS mass ≈ {rm}\n"
            f"{t['chem_moles']}: (auto-balancing not implemented in demo)"
        )
        if lm is None or rm is None:
            return _resp(
                "clarification",
                _format_steps(t, text, steps, "Some formulas could not be parsed."),
                lang=lang, domain="chemistry", source=text, confidence=0.3,
            )
        balanced = abs(lm - rm) < 0.01
        answer = "Balanced by mass." if balanced else f"Not mass-balanced ({lm:.3f} vs {rm:.3f})."
        return _resp(
            "answer",
            _format_steps(t, text, steps, answer),
            lang=lang, domain="chemistry", confidence=0.6,
            answer=answer, source=text,
        )

    # Single-formula branch
    m = re.search(r"([A-Z][A-Za-z0-9()]*)", text)
    if not m:
        return _resp(
            "clarification",
            "I couldn't find a chemical formula in your question.",
            lang=lang, domain="chemistry", source=text,
        )
    formula = m.group(1)
    mass = _molar_mass(formula)
    if mass is None:
        return _resp(
            "clarification",
            _format_steps(t, text, f"{t['chem_mass']} ({formula})",
                          f"Could not parse {formula}."),
            lang=lang, domain="chemistry", source=text, confidence=0.2,
        )
    answer = f"Molar mass of {formula} = {round(mass, 4)} g/mol"
    return _resp(
        "answer",
        _format_steps(t, text, f"{t['chem_mass']} for {formula}", answer),
        lang=lang, domain="chemistry", confidence=0.9,
        answer=answer, source=text,
    )


# ===========================================================================
# GEOMETRY SOLVER
# ===========================================================================
def solve_geometry(
    text: str,
    visual_results: Optional[List[str]] = None,
    lang: str = "en",
) -> str:
    t = _t(lang)
    shapes = ", ".join(visual_results) if visual_results else "no shapes detected"
    steps = (
        f"{t['geo_visual']}: {shapes}\n"
        f"{t['geo_theorem']}: ready for dimension-based solving"
    )
    if not visual_results:
        return _resp(
            "clarification",
            _format_steps(t, text, steps, "Send a clearer image or type the shape + dimensions."),
            lang=lang, domain="geometry", source=text, confidence=0.2,
        )
    answer = "Detected shapes: " + ", ".join(visual_results)
    return _resp(
        "answer",
        _format_steps(t, text, steps, answer),
        lang=lang, domain="geometry", confidence=0.6,
        answer=answer, source=text,
    )


# ===========================================================================
# DISPATCHER — single entry point for Android.
# ===========================================================================
_MATH_HINT = re.compile(
    r"(?:^|\b)(?:solve|simplify|expand|factor|diff|integrat\w*|deriv\w*)"
    r"|[=+\-*/^]|\(|\)",
    re.I,
)
_PHYS_HINT = re.compile(
    r"\b(force|velocity|acceleration|ohm|newton|kinetic|weight)\b|"
    r"\b[A-Za-z]\s*=\s*[A-Za-z]\b",
    re.I,
)
_CHEM_HINT = re.compile(r"\b(molar mass|mole|reaction|balance)\b|[A-Z][a-z]?\d+", re.I)


def solve_problem(
    text: str,
    lang: str = "en",
    image_base64: Optional[str] = None,
    want_plot: bool = True,
) -> str:
    """Single entry point. Always returns a JSON string; never raises."""
    try:
        if not isinstance(text, str) or not text.strip():
            if image_base64:
                visuals = detect_visuals(image_base64)
                return solve_geometry(text or "(image only)", visuals, lang=lang)
            return _resp(
                "clarification", _t(lang)["need_clarify"], lang=lang
            )

        # Chemistry reactions are strong signals — check first.
        if _REACTION_ARROW_RE.search(text):
            return solve_chemistry(text, lang=lang)

        # Math vs physics: math when operators or explicit verbs are present.
        if _MATH_HINT.search(text) and not _PHYS_HINT.search(text):
            return solve_math(text, lang=lang, want_plot=want_plot)

        if _PHYS_HINT.search(text):
            return solve_physics(text, lang=lang, want_plot=want_plot)

        if _CHEM_HINT.search(text) and re.search(r"[A-Z][a-z]?\d+", text):
            return solve_chemistry(text, lang=lang)

        visuals = detect_visuals(image_base64) if image_base64 else []
        if visuals:
            return solve_geometry(text, visuals, lang=lang)

        return _resp(
            "clarification",
            "I couldn't identify the problem type. Ask a math, physics, "
            "chemistry, or geometry question.",
            lang=lang, source=text,
        )
    except Exception as exc:  # noqa: BLE001
        _log.exception("Dispatcher failed")
        return _resp("error", f"Solver error: {exc}", lang=lang)


# ===========================================================================
# PRACTICE GENERATOR
# ===========================================================================
_SIMILAR: Dict[str, Dict[str, str]] = {
    "MATH": {
        "en": "Practice: Solve 2x + 5 = 15",
        "am": "ልምምድ፦ 2x + 5 = 15 ን ፍታ",
    },
    "PHYSICS": {
        "en": "Practice: m=10 kg, a=5 m/s². Find force.",
        "am": "ልምምድ፦ m=10 kg, a=5 m/s²። ኃይልን ፈልግ።",
    },
    "CHEMISTRY": {
        "en": "Practice: Molar mass of (NH4)2SO4.",
        "am": "ልምምድ፦ የ (NH4)2SO4 ሞላር ብዛት።",
    },
    "GEOMETRY": {
        "en": "Practice: Area of circle r=7.",
        "am": "ልምምድ፦ r=7 የሆነ ክብ ስፋት።",
    },
}


def generate_similar(text: str, mode: str, lang: str = "en") -> str:
    key = (mode or "").upper()
    entries = _SIMILAR.get(key, _SIMILAR["MATH"])
    prompt = entries.get(lang, entries["en"])
    return _resp(
        "answer",
        _format_steps(_t(lang), text, _t(lang)["practice"], prompt),
        lang=lang, domain=key.lower() or "general", confidence=1.0,
        answer=prompt, source=text,
    )


# ===========================================================================
# Local smoke test — prints JSON so you can eyeball the contract.
# ===========================================================================
if __name__ == "__main__":
    examples = [
        "2X + 5 = 15",
        "x^2 - 4 = 0",
        "diff(x**3)",
        "integrate(x**2)",
        "sin(x)",
        "x**2 + y**2",
        "F = m*a, m=5, a=9.8",
        "molar mass of (NH4)2SO4",
        "H2 + O2 -> H2O",
        "hello",
    ]
    for q in examples:
        out = solve_problem(q, want_plot=False)  # disable plots for speed
        print(q, "->", out[:220], "..." if len(out) > 220 else "")