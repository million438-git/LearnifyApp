#!/usr/bin/env python3
"""
AceGrade Academic Partner - complete assistant backend.

Includes:
  1. Multilingual command engine (en, am, om, ar, fr) with word-boundary matching,
     fuzzy typo matching, confidence scoring, confirmation, multi-intent and session memory
  2. Locale system (all reply text is data; JSON files in ./locales override built-ins)
  3. Lesson script writer (humanize_explanation) with levels: simple / normal / advanced
  4. Speech preparation (science/math pronunciation, SSML, chunking for TTS limits)
  5. TTS provider interface, audio cache, text-only fallback for unsupported languages
  6. Optional LLM tutor (Claude API) with academic-only guardrails and offline fallback
  7. Spaced repetition (SM-2), adaptive difficulty, progress tracking, streaks, weak topics
  8. Logging of unrecognized commands (to improve the app) and built-in self-tests

Backward compatible: understand_command(text) and humanize_explanation(topic, raw_content)
still work. Optional dependencies: rapidfuzz, anthropic, google-cloud-texttospeech.
Run:  python acegrade_assistant.py --test    |    python acegrade_assistant.py
"""
from __future__ import annotations

import difflib
import hashlib
import json
import logging
import os
import re
import sys
import time
import unicodedata
from dataclasses import asdict, dataclass, field
from datetime import date, timedelta
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple
from xml.sax.saxutils import escape as xml_escape

try:  # optional, faster and better fuzzy matching
    from rapidfuzz import fuzz as _rf

    def _sim(a: str, b: str) -> float:
        return _rf.ratio(a, b) / 100.0
except ImportError:
    def _sim(a: str, b: str) -> float:
        return difflib.SequenceMatcher(None, a, b).ratio()

log = logging.getLogger("acegrade")
DATA_DIR = Path(os.environ.get("ACEGRADE_DATA", "acegrade_data"))
LANGS = ["en", "am", "om", "ar", "fr"]
LANG_NAMES_DISPLAY = {"en": "English", "am": "Amharic", "om": "Afan Oromo", "ar": "Arabic", "fr": "French"}
LEVELS = ["simple", "normal", "advanced"]


# ============================================================================
# 1. TEXT NORMALIZATION
# ============================================================================
def normalize(text: Any) -> str:
    """Lowercase, strip accents/diacritics, unify Arabic alef, drop punctuation."""
    t = unicodedata.normalize("NFKD", str(text or "").lower())
    t = "".join(c for c in t if unicodedata.category(c) != "Mn")
    t = re.sub("[\u0623\u0625\u0622]", "\u0627", t)
    t = re.sub(r"[^\w\s]", " ", t)
    return re.sub(r"\s+", " ", t).strip()


# ============================================================================
# 2. LOCALES (reply text is data, never hardcoded in logic)
#    NOTE: have native speakers review am / om / ar / fr before release.
# ============================================================================
BUILTIN_LOCALES: Dict[str, Dict[str, str]] = {
    "en": {
        "greet": "Hello! I am your AceGrade Academic Partner. I can read your lessons, open subjects, or quiz you. What shall we study today?",
        "help": "You can say: 'Open Biology', 'Read this lesson', 'Quiz me on Physics', 'Slower', 'Explain simpler', 'Go to dashboard', or 'Speak Amharic'.",
        "open_subject": "Opening {subject}. Let's begin!",
        "open_library": "Opening the Academic Library.",
        "open_settings": "Opening your settings so you can personalize your experience.",
        "open_profile": "Loading your student profile and progress.",
        "open_dashboard": "Returning to your study dashboard.",
        "open_chat": "Opening EduAI, your personal academic assistant.",
        "open_challenge": "Starting your Daily Exam Challenge. You've got this!",
        "start_quiz": "Starting a quiz{for_subject}. Take your time.",
        "summarize": "I'll prepare a short summary of this lesson for you.",
        "read_aloud": "Starting the read-aloud session for this topic.",
        "stop_voice": "Stopping the voice session.",
        "pause": "Paused. Say 'continue' when you are ready.",
        "resume": "Continuing where we stopped.",
        "repeat": "Sure, let me say that again.",
        "next": "Moving to the next topic.",
        "previous": "Going back to the previous topic.",
        "slower": "Okay, slower. Speed is now {speed}x.",
        "faster": "Okay, faster. Speed is now {speed}x.",
        "simpler": "No problem. I'll explain it in simpler words.",
        "detailed": "Sure. I'll go into more detail.",
        "set_language": "Switching to {language}.",
        "confirm": "Did you mean: {intent_label}? Say yes or no.",
        "fallback": "Sorry, I did not understand that. Try 'Open Biology', 'Read this lesson', or say 'help'.",
        "fb_correct": "Correct! Well done.",
        "fb_wrong": "Not quite. Let's look at it again.",
        "fb_streak": "That's {n} correct answers in a row. Nice work.",
    },
    "am": {
        "greet": "ሰላም! እኔ የAceGrade የትምህርት አጋርዎ ነኝ። ዛሬ ምን እናጥና?",
        "open_subject": "{subject}ን በመክፈት ላይ ነው። እንጀምር!",
        "open_library": "ቤተ መጽሐፍቱን በመክፈት ላይ ነው።",
        "open_settings": "ቅንብሮችን በመክፈት ላይ ነው።",
        "open_profile": "የእርስዎን መገለጫ በመክፈት ላይ ነው።",
        "open_dashboard": "ወደ ዋና ገጽ በመመለስ ላይ ነው።",
        "open_challenge": "የዕለቱን ፈተና እየጀመርኩ ነው። ይሳካልዎታል!",
        "summarize": "የትምህርቱን ማጠቃለያ አዘጋጃለሁ።",
        "read_aloud": "ትምህርቱን ማንበብ እጀምራለሁ።",
        "stop_voice": "ድምጹን አቆማለሁ።",
        "fallback": "አልገባኝም። 'ባዮሎጂ ክፈት' ወይም 'ትምህርቱን አንብብ' ይበሉ።",
        "fb_correct": "ትክክል ነው! በጣም ጥሩ።",
        "fb_wrong": "ልክ አይደለም። እንደገና እንመልከተው።",
    },
    "om": {
        "greet": "Akkam! Ani gargaaraa barnootaa AceGrade keessani dha. Har'a maal haa barannu?",
        "open_subject": "{subject} banaa jira. Haa jalqabnu!",
        "stop_voice": "Sagaleen dhaabbate.",
        "fallback": "Hin hubanne. 'Biyoloojii bani' ykn 'Barnoota dubbisi' jedhaa.",
        "fb_correct": "Sirrii! Baayyee gaarii.",
        "fb_wrong": "Hin sirrooftu. Irra deebi'ii haa ilaallu.",
    },
    "ar": {
        "greet": "مرحبًا! أنا شريكك الأكاديمي في AceGrade. يمكنني قراءة دروسك وفتح المواد واختبارك. ماذا ندرس اليوم؟",
        "open_subject": "جارٍ فتح {subject}. لنبدأ!",
        "open_library": "جارٍ فتح المكتبة الأكاديمية.",
        "open_settings": "جارٍ فتح الإعدادات.",
        "open_profile": "جارٍ تحميل ملفك الشخصي وتقدمك.",
        "open_dashboard": "العودة إلى لوحة الدراسة.",
        "open_challenge": "بدء تحدي الامتحان اليومي. أنت قادر على ذلك!",
        "summarize": "سأجهّز ملخصًا قصيرًا لهذا الدرس.",
        "read_aloud": "بدء قراءة الدرس بصوت عالٍ.",
        "stop_voice": "تم إيقاف الصوت.",
        "fallback": "عذرًا، لم أفهم. جرّب «افتح الأحياء» أو «اقرأ الدرس».",
        "fb_correct": "إجابة صحيحة! أحسنت.",
        "fb_wrong": "ليست صحيحة تمامًا. لنراجعها مرة أخرى.",
    },
    "fr": {
        "greet": "Bonjour ! Je suis votre partenaire d'étude AceGrade. Que voulez-vous étudier aujourd'hui ?",
        "open_subject": "Ouverture de {subject}. Commençons !",
        "open_library": "Ouverture de la bibliothèque académique.",
        "open_settings": "Ouverture des paramètres.",
        "open_profile": "Chargement de votre profil et de vos progrès.",
        "open_dashboard": "Retour au tableau de bord.",
        "open_challenge": "Début du défi quotidien. Vous pouvez le faire !",
        "summarize": "Je prépare un court résumé de cette leçon.",
        "read_aloud": "Début de la lecture à voix haute.",
        "stop_voice": "Arrêt de la voix.",
        "fallback": "Désolé, je n'ai pas compris. Essayez « Ouvre biologie » ou « Lis la leçon ».",
        "fb_correct": "Correct ! Bien joué.",
        "fb_wrong": "Pas tout à fait. Revoyons cela.",
    },
}


class _Safe(dict):
    def __missing__(self, key: str) -> str:
        return ""


class Locales:
    """Built-in strings, overridable by locales/<lang>.json files. Missing keys fall back to English."""

    def __init__(self, directory: Optional[str] = "locales"):
        self.data = {k: dict(v) for k, v in BUILTIN_LOCALES.items()}
        if directory and Path(directory).is_dir():
            for f in Path(directory).glob("*.json"):
                try:
                    self.data.setdefault(f.stem, {}).update(json.loads(f.read_text(encoding="utf-8")))
                except (OSError, ValueError) as e:
                    log.warning("Bad locale file %s: %s", f, e)

    def t(self, lang: str, key: str, **params: Any) -> str:
        tpl = self.data.get(lang, {}).get(key) or self.data["en"].get(key, key)
        return tpl.format_map(_Safe(params))


# ============================================================================
# 3. INTENT AND SUBJECT DATA (language support is just data, add more freely)
# ============================================================================
def K(en=(), am=(), om=(), ar=(), fr=()) -> Dict[str, List[str]]:
    return {"en": list(en), "am": list(am), "om": list(om), "ar": list(ar), "fr": list(fr)}


SUBJECTS: Dict[str, Dict[str, List[str]]] = {
    "Biology": K(["biology", "bio"], ["ባዮሎጂ"], ["biyoloojii", "biology"], ["أحياء", "الأحياء", "علم الأحياء"], ["biologie", "svt"]),
    "Physics": K(["physics"], ["ፊዚክስ"], ["fiiziksii"], ["فيزياء", "الفيزياء"], ["physique"]),
    "Mathematics": K(["math", "maths", "mathematics"], ["ሒሳብ", "ሂሳብ"], ["herrega", "herregaa"], ["رياضيات", "الرياضيات"], ["mathématiques", "maths"]),
    "Chemistry": K(["chemistry", "chem"], ["ኬሚስትሪ"], ["keemistrii"], ["كيمياء", "الكيمياء"], ["chimie"]),
    "English": K(["english", "english language"], ["እንግሊዝኛ"], ["afaan ingiliizii", "ingiliizii"], ["الإنجليزية", "انجليزي"], ["anglais"]),
    "Scholastic Aptitude": K(["aptitude", "scholastic", "scholastic aptitude"], [], [], ["القدرات"], ["aptitude"]),
}

NAV_VERBS = K(
    ["open", "go to", "show me", "take me to"],
    ["ክፈት", "ክፈትልኝ", "ውሰደኝ", "አሳየኝ"],
    ["bani", "natti bani", "agarsiisi"],
    ["افتح", "اذهب الى", "اعرض", "خذني الى"],
    ["ouvre", "ouvrir", "aller à", "montre moi", "va à"],
)

# priority breaks ties; needs_subject / needs_language require an extracted entity
INTENTS: Dict[str, Dict[str, Any]] = {
    "open_subject": dict(type="nav", page="SubjectActivity", priority=3, needs_subject=True, kw=NAV_VERBS),
    "open_library": dict(type="nav", page="SubjectActivity", priority=2, kw=K(
        ["library", "courses", "course", "all subjects"], ["ቤተ መጽሐፍት", "ኮርሶች"], [], ["المكتبة", "المقررات"], ["bibliothèque", "cours"])),
    "open_settings": dict(type="nav", page="SettingsActivity", priority=3, kw=K(
        ["settings", "setting", "preferences"], ["ቅንብር", "ቅንብሮች"], ["seetingii"], ["الإعدادات", "اعدادات"], ["paramètres", "réglages"])),
    "open_profile": dict(type="nav", page="ProfileActivity", priority=3, kw=K(
        ["profile", "my progress"], ["መገለጫ"], ["profaayilii"], ["الملف الشخصي", "ملفي"], ["profil"])),
    "open_dashboard": dict(type="nav", page="DashboardActivity", priority=3, kw=K(
        ["dashboard", "go home", "home page", "main page", "back to home"], ["ዋና ገጽ", "ዳሽቦርድ"], ["fuula duraa"], ["الرئيسية", "لوحة التحكم"], ["tableau de bord", "accueil"])),
    "open_chat": dict(type="nav", page="AIChatActivity", priority=3, kw=K(
        ["chat", "ai", "assistant", "tutor", "eduai"], ["ውይይት"], [], ["المساعد", "محادثة"], ["assistant", "discussion"])),
    "open_challenge": dict(type="nav", page="DailyChallengeActivity", priority=4, kw=K(
        ["daily challenge", "challenge"], ["የዕለት ፈተና", "ፈተና"], [], ["التحدي اليومي", "تحدي"], ["défi quotidien", "défi"])),
    "start_quiz": dict(type="nav", page="DailyChallengeActivity", priority=4, uses_subject=True, kw=K(
        ["quiz", "quiz me", "test me", "exam"], ["ጥያቄ", "ፈትነኝ"], ["qormaata", "na qoradhu"], ["اختبار", "اختبرني"], ["quiz", "teste moi", "interroge moi"])),
    "summarize": dict(type="action", action="summarize", priority=3, uses_subject=True, kw=K(
        ["summarize", "summary", "sum up"], ["አጠቃልል", "ማጠቃለያ"], ["cuunfaa", "gabaabsi"], ["لخص", "تلخيص", "ملخص"], ["résumé", "résumer"])),
    "read_aloud": dict(type="action", action="read_aloud", priority=3, uses_subject=True, kw=K(
        ["read", "read aloud", "read to me", "read this", "read the lesson", "read lesson"], ["አንብብ", "አንብብልኝ"], ["dubbisi", "naaf dubbisi"], ["اقرأ", "اقرأ لي"], ["lis", "lis moi", "lire", "lis la leçon"])),
    "stop_voice": dict(type="action", action="stop_voice", priority=5, kw=K(
        ["stop", "be quiet", "stop reading", "stop speaking"], ["አቁም", "ቁም"], ["dhaabi"], ["توقف", "قف"], ["arrête", "stop", "arrêter"])),
    "pause": dict(type="action", action="pause", priority=4, kw=K(
        ["pause", "wait", "hold on"], ["ለአፍታ አቁም"], ["boqodhu"], ["ايقاف مؤقت"], ["pause", "attends"])),
    "resume": dict(type="action", action="resume", priority=4, kw=K(
        ["resume", "continue", "go on"], ["ቀጥል"], ["itti fufi"], ["استمر", "تابع"], ["continue", "reprends"])),
    "repeat": dict(type="action", action="repeat", priority=4, uses_subject=True, kw=K(
        ["repeat", "again", "say that again", "read it again", "one more time"], ["ድገም", "እንደገና"], ["irra deebi'i"], ["كرر", "مرة اخرى"], ["répète", "encore", "répéter"])),
    "next": dict(type="action", action="next", priority=3, kw=K(
        ["next", "next topic", "next lesson", "skip"], ["ቀጣይ"], ["itti aanu"], ["التالي"], ["suivant", "prochain"])),
    "previous": dict(type="action", action="previous", priority=3, kw=K(
        ["previous", "go back", "back", "last topic"], ["ቀዳሚ", "ተመለስ"], ["duubatti"], ["السابق"], ["précédent", "retour"])),
    "slower": dict(type="action", action="slower", priority=4, kw=K(
        ["slower", "slow down", "too fast"], ["ቀስ በል"], [], ["ابطأ"], ["plus lentement", "ralentis"])),
    "faster": dict(type="action", action="faster", priority=4, kw=K(
        ["faster", "speed up", "too slow"], ["ፍጠን"], [], ["اسرع"], ["plus vite", "accélère"])),
    "simpler": dict(type="action", action="simpler", priority=4, kw=K(
        ["simpler", "explain simply", "explain simpler", "i don't understand", "too hard", "make it easier"],
        ["አልገባኝም", "ቀላል አድርገህ አስረዳ"], ["hin hubanne"], ["لم افهم", "بسط", "ابسط"], ["plus simple", "je ne comprends pas"])),
    "detailed": dict(type="action", action="detailed", priority=4, kw=K(
        ["in detail", "more detail", "detailed", "advanced", "explain more"], ["በዝርዝር"], [], ["بالتفصيل"], ["en détail", "plus de détails"])),
    "set_language": dict(type="action", action="set_language", priority=4, needs_language=True, kw=K(
        ["speak", "switch language", "change language", "speak in"], [], [], ["تكلم"], ["parle"])),
    "help": dict(type="chat", priority=2, kw=K(
        ["help", "what can you do", "commands"], ["እርዳታ", "እገዛ"], ["gargaarsa"], ["مساعدة", "ساعدني"], ["aide", "aidez moi"])),
    "greet": dict(type="chat", priority=1, kw=K(
        ["hello", "hi", "hey", "good morning", "who are you"], ["ሰላም", "ታዲያስ"], ["akkam", "nagaa"], ["مرحبا", "اهلا", "السلام عليكم"], ["bonjour", "salut", "coucou"])),
}

LANG_WORDS = {normalize(k): v for k, v in {
    "amharic": "am", "አማርኛ": "am", "english": "en", "afaan oromo": "om", "oromo": "om", "oromiffa": "om",
    "arabic": "ar", "العربية": "ar", "french": "fr", "français": "fr", "francais": "fr"}.items()}

_ETHIOPIC = re.compile("[\u1200-\u137F]")
_ARABIC = re.compile("[\u0600-\u06FF]")
_SPLIT = re.compile(r"(?<!\w)(?:and then|and|then|after that|እና|ከዚያም|ከዚያ|fi|ergasii|ثم|et|puis)(?!\w)")


def _phrase_regex(norm_phrase: str) -> "re.Pattern[str]":
    return re.compile(r"(?<!\w)" + r"\s+".join(map(re.escape, norm_phrase.split())) + r"(?!\w)")


# ============================================================================
# 4. SESSION, RESULT, COMMAND ENGINE
# ============================================================================
@dataclass
class Session:
    lang: str = "en"
    level: str = "normal"
    speed: float = 1.0
    last_subject: Optional[str] = None
    last_intent: Optional[str] = None


@dataclass
class Result:
    type: str                      # nav | action | chat | confirm | unknown
    intent: str
    confidence: float = 0.0
    page: Optional[str] = None
    action: Optional[str] = None
    params: Dict[str, Any] = field(default_factory=dict)
    response_key: str = "fallback"
    pending: Optional[Dict[str, Any]] = None   # what to run if user confirms

    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)


@dataclass
class Reply:
    lang: str
    results: List[Result]
    replies: List[str]
    needs_llm: bool = False
    raw: str = ""


class UnrecognizedLog:
    """Stores unknown phrases (text only, never audio) so you can learn what students really say."""

    def __init__(self, path: Path = DATA_DIR / "unrecognized.jsonl", enabled: bool = True):
        self.path, self.enabled = path, enabled

    def add(self, text: str, lang: str) -> None:
        if not self.enabled:
            return
        try:
            self.path.parent.mkdir(parents=True, exist_ok=True)
            with self.path.open("a", encoding="utf-8") as f:
                f.write(json.dumps({"t": int(time.time()), "lang": lang, "text": text}, ensure_ascii=False) + "\n")
        except OSError as e:
            log.warning("Could not log unrecognized text: %s", e)


class CommandEngine:
    EXEC, CONFIRM = 0.80, 0.55

    def __init__(self, locales: Optional[Locales] = None, unknown_log: Optional[UnrecognizedLog] = None):
        self.locales = locales or Locales()
        self.unknown_log = unknown_log or UnrecognizedLog(enabled=False)
        self._kw: Dict[str, List[Tuple[str, "re.Pattern[str]", int]]] = {}
        for name, spec in INTENTS.items():
            items = []
            for kws in spec["kw"].values():
                for k in kws:
                    kn = normalize(k)
                    if kn:
                        items.append((kn, _phrase_regex(kn), len(kn.split())))
            self._kw[name] = items
        self._subj = [(canon, normalize(k), _phrase_regex(normalize(k)))
                      for canon, d in SUBJECTS.items() for ks in d.values() for k in ks if normalize(k)]
        self._lex: Dict[str, set] = {l: set() for l in LANGS}
        for spec in INTENTS.values():
            for l, ks in spec["kw"].items():
                for k in ks:
                    self._lex[l].update(normalize(k).split())
        for d in SUBJECTS.values():
            for l, ks in d.items():
                for k in ks:
                    self._lex[l].update(normalize(k).split())

    # ---- language detection -------------------------------------------------
    def detect_language(self, raw: str, norm: str, default: str) -> Tuple[str, bool]:
        if _ETHIOPIC.search(raw):
            return "am", True
        if _ARABIC.search(raw):
            return "ar", True
        toks = set(norm.split())
        scores = {l: len(toks & self._lex[l]) for l in ("en", "om", "fr")}
        best = max(scores, key=scores.get)
        if scores[best] == 0:
            return default, False
        if list(scores.values()).count(scores[best]) > 1 and default in scores and scores[default] == scores[best]:
            return default, True
        return best, True

    # ---- entities -----------------------------------------------------------
    def extract_subject(self, norm: str, tokens: List[str]) -> Tuple[Optional[str], float]:
        for canon, kn, pat in self._subj:
            if pat.search(norm):
                return canon, 1.0
        best: Tuple[Optional[str], float] = (None, 0.0)
        for canon, kn, _ in self._subj:
            if " " in kn or len(kn) < 6:
                continue
            for t in tokens:
                if abs(len(t) - len(kn)) <= 2:
                    s = _sim(kn, t)
                    if s >= 0.84 and s * 0.9 > best[1]:
                        best = (canon, s * 0.9)
        return best

    @staticmethod
    def extract_language(norm: str) -> Optional[str]:
        for word in sorted(LANG_WORDS, key=len, reverse=True):
            if _phrase_regex(word).search(norm):
                return LANG_WORDS[word]
        return None

    # ---- scoring ------------------------------------------------------------
    def _score(self, name: str, norm: str, tokens: List[str]) -> Tuple[float, int]:
        best = (0.0, 0)
        for kw, pat, n in self._kw[name]:
            if pat.search(norm):
                s = 1.0
            elif n == 1 and len(kw) >= 6:
                sims = [_sim(kw, t) for t in tokens if abs(len(t) - len(kw)) <= 2]
                top = max(sims, default=0.0)
                s = top * 0.9 if top >= 0.85 else 0.0
            else:
                continue
            best = max(best, (s, n))
        return best

    def match_clause(self, norm: str, carry_subject: Optional[str]) -> Result:
        tokens = norm.split()
        subj, sconf = self.extract_subject(norm, tokens)
        cands: List[Tuple[float, float, str]] = []
        for name, spec in INTENTS.items():
            conf, n = self._score(name, norm, tokens)
            if conf <= 0:
                continue
            if spec.get("needs_subject"):
                if not subj:
                    continue
                conf = min(conf, sconf)
            if spec.get("needs_language") and not self.extract_language(norm):
                continue
            cands.append((conf + 0.05 * spec["priority"] + 0.02 * n, conf, name))
        if not cands and subj:                         # bare subject name, e.g. just "biology"
            cands.append((0.85 * sconf, 0.85 * sconf, "open_subject"))
        if not cands:
            return Result("unknown", "unknown", 0.0, response_key="fallback")
        _, conf, name = max(cands)
        spec = INTENTS[name]
        params: Dict[str, Any] = {}
        subject = subj or (carry_subject if spec.get("uses_subject") else None)
        if subject:
            params["subject"] = subject
        if name == "set_language":
            params["language_code"] = self.extract_language(norm)
        return Result(spec["type"], name, round(conf, 3), spec.get("page"), spec.get("action"), params, name)

    # ---- main entry ---------------------------------------------------------
    def process(self, text: str, session: Optional[Session] = None) -> Reply:
        session = session or Session()
        raw = str(text or "")
        norm = normalize(raw)
        lang, confident = self.detect_language(raw, norm, session.lang)
        reply_lang = lang if confident else session.lang

        results: List[Result] = []
        carry = session.last_subject
        for clause in [c.strip() for c in _SPLIT.split(norm) if c.strip()] or [norm]:
            r = self.match_clause(clause, carry)
            if r.intent == "unknown":
                continue
            if r.confidence < self.CONFIRM:
                continue
            if r.confidence < self.EXEC:                # unsure: ask the learner to confirm
                r = Result("confirm", "confirm", r.confidence, params={"intent_label": r.intent.replace("_", " ")},
                           response_key="confirm", pending=r.to_dict())
            else:
                carry = r.params.get("subject", carry)
                self._apply_to_session(r, session)
            results.append(r)

        if not results:
            self.unknown_log.add(raw, reply_lang)
            results = [Result("unknown", "unknown", 0.0, response_key="fallback", params={"text": raw})]
            return Reply(reply_lang, results, [self.locales.t(reply_lang, "fallback")], needs_llm=True, raw=raw)

        replies = [self._render(r, reply_lang) for r in results]
        return Reply(reply_lang, results, replies, False, raw)

    def _apply_to_session(self, r: Result, s: Session) -> None:
        s.last_intent = r.intent
        if r.params.get("subject"):
            s.last_subject = r.params["subject"]
        if r.intent == "slower":
            s.speed = max(0.5, round(s.speed - 0.25, 2)); r.params["speed"] = s.speed
        elif r.intent == "faster":
            s.speed = min(2.0, round(s.speed + 0.25, 2)); r.params["speed"] = s.speed
        elif r.intent == "simpler":
            s.level = LEVELS[max(0, LEVELS.index(s.level) - 1)]; r.params["level"] = s.level
        elif r.intent == "detailed":
            s.level = LEVELS[min(2, LEVELS.index(s.level) + 1)]; r.params["level"] = s.level
        elif r.intent == "set_language" and r.params.get("language_code"):
            s.lang = r.params["language_code"]
            r.params["language"] = LANG_NAMES_DISPLAY[s.lang]

    def _render(self, r: Result, lang: str) -> str:
        p = dict(r.params)
        subject = p.get("subject")
        if subject:
            local = SUBJECTS[subject].get(lang) or []
            p["subject"] = local[0].capitalize() if local else subject
            p["for_subject"] = f" on {p['subject']}" if lang == "en" else f" ({p['subject']})"
        return self.locales.t(lang, r.response_key, **p)


# ============================================================================
# 5. SPEECH PREPARATION (pronunciation, SSML, chunking)
# ============================================================================
_SUBSCRIPTS = str.maketrans("₀₁₂₃₄₅₆₇₈₉", "0123456789")
_EN_TERMS = [
    (r"\bH2O\b", "H two O"), (r"\bCO2\b", "C O two"), (r"\bO2\b", "O two"), (r"\bNaCl\b", "N a C L"),
    (r"\bDNA\b", "D N A"), (r"\bRNA\b", "R N A"), (r"\bATP\b", "A T P"), (r"\bpH\b", "p H"),
    (r"\be\.g\.", "for example"), (r"\bi\.e\.", "that is"), (r"\betc\.", "and so on"), (r"\bvs\.?(?=\s)", "versus"),
    (r"x\^2|x²", "x squared"), (r"²", " squared"), (r"³", " cubed"), (r"\^(\d+)", r" to the power of \1"),
    (r"√", "square root of "), (r"(\d)\s*°C", r"\1 degrees Celsius"), (r"%", " percent"),
    (r"→", " leads to "), (r"=", " equals "), (r"\+", " plus "), (r"×", " times "), (r"÷", " divided by "),
    (r"\u2212", " minus "), (r"<", " less than "), (r">", " greater than "),
]
_SYMBOLS = {
    "fr": [(r"%", " pour cent"), (r"=", " égale "), (r"\+", " plus "), (r"×", " fois "), (r"→", " donne ")],
    "ar": [(r"%", " بالمئة"), (r"=", " يساوي "), (r"\+", " زائد "), (r"→", " يؤدي إلى ")],
}
PRONUNCIATIONS: Dict[str, Dict[str, str]] = {"en": {}, "am": {}, "om": {}, "ar": {}, "fr": {}}  # extend from user feedback


def prepare_for_speech(text: str, lang: str = "en", pron: Optional[Dict[str, str]] = None) -> str:
    t = text.translate(_SUBSCRIPTS)
    for word, spoken in {**PRONUNCIATIONS.get(lang, {}), **(pron or {})}.items():
        t = re.sub(rf"(?<!\w){re.escape(word)}(?!\w)", spoken, t)
    for pat, rep in (_EN_TERMS if lang == "en" else _SYMBOLS.get(lang, [])):
        t = re.sub(pat, rep, t)
    return re.sub(r"\s+", " ", t).strip()


_ABBR = ["Dr.", "Mr.", "Mrs.", "Ms.", "Prof.", "e.g.", "i.e.", "etc.", "vs.", "Fig.", "No.", "approx."]


def split_sentences(text: str) -> List[str]:
    """Sentence splitter that survives abbreviations and decimals (Latin, Ethiopic, Arabic)."""
    t = text
    for a in _ABBR:
        t = re.sub(re.escape(a), a.replace(".", "\u0001"), t, flags=re.I)
    t = re.sub(r"(?<=\d)\.(?=\d)", "\u0001", t)
    parts = re.split(r"(?<=[.!?።؟])\s+", t)
    return [p.replace("\u0001", ".").strip() for p in parts if p.strip()]


def speech_units(segments: List[str]) -> List[Tuple[str, bool]]:
    """(sentence, is_end_of_segment) pairs so longer pauses fall between ideas."""
    units: List[Tuple[str, bool]] = []
    for seg in segments:
        sents = split_sentences(seg) or [seg]
        units.extend((s, i == len(sents) - 1) for i, s in enumerate(sents))
    return units


def build_ssml(units: List[Tuple[str, bool]], lang: str = "en", speed: float = 1.0,
               pron: Optional[Dict[str, str]] = None) -> str:
    rate = int(max(0.5, min(2.0, speed)) * 100)
    parts = ["<speak>", f'<prosody rate="{rate}%">']
    for sent, end in units:
        parts.append(xml_escape(prepare_for_speech(sent, lang, pron)))
        parts.append(f'<break time="{750 if end else 350}ms"/>')
    parts += ["</prosody>", "</speak>"]
    return "".join(parts)


def ssml_chunks(units: List[Tuple[str, bool]], lang: str = "en", speed: float = 1.0,
                pron: Optional[Dict[str, str]] = None, max_chars: int = 3000) -> List[str]:
    """TTS engines reject long input, so split into several SSML documents."""
    chunks, cur, size = [], [], 0
    for u in units:
        if cur and size + len(u[0]) > max_chars:
            chunks.append(build_ssml(cur, lang, speed, pron)); cur, size = [], 0
        cur.append(u); size += len(u[0])
    if cur:
        chunks.append(build_ssml(cur, lang, speed, pron))
    return chunks


# ============================================================================
# 6. LESSON SCRIPT WRITER (humanize_explanation)
# ============================================================================
SCRIPT: Dict[str, Dict[str, List[str]]] = {
    "en": {
        "intro": ["Hello! Today we are going to learn about {topic}.", "Welcome. Let's explore {topic} together.",
                  "Hi there. Let's take a clear look at {topic}."],
        "transitions": ["Next point.", "Now let's look at another idea.", "Here is the next piece.", "Let's build on that."],
        "recap": ["Let's recap. The key idea: {key}", "To sum up, remember this: {key}"],
        "check": ["Quick check: can you explain {topic} in your own words?"],
    },
    "am": {"intro": ["ሰላም! ዛሬ ስለ {topic} እንማራለን።"], "transitions": ["በመቀጠል፣", "አሁን ወደሚቀጥለው እንሂድ።"],
           "recap": ["በአጭሩ እንድገመው። ዋናው ሐሳብ፦ {key}"], "check": ["ፈጣን ጥያቄ፦ ስለ {topic} በራስዎ አባባል ማብራራት ይችላሉ?"]},
    "om": {"intro": ["Akkam! Har'a waa'ee {topic} baranna."], "transitions": ["Itti aanee,", "Amma gara itti aanutti haa deemnu."],
           "recap": ["Haa walitti qabnu. Yaada ijoo: {key}"], "check": ["Gaaffii gabaabaa: waa'ee {topic} jecha keetiin ibsuu dandeessaa?"]},
    "ar": {"intro": ["مرحبًا! اليوم سنتعلم عن {topic}."], "transitions": ["والآن،", "لننتقل إلى النقطة التالية."],
           "recap": ["لنراجع باختصار. الفكرة الرئيسية: {key}"], "check": ["سؤال سريع: هل تستطيع شرح {topic} بكلماتك؟"]},
    "fr": {"intro": ["Bonjour ! Aujourd'hui, nous allons étudier {topic}."], "transitions": ["Ensuite,", "Passons au point suivant."],
           "recap": ["Récapitulons. L'idée clé : {key}"], "check": ["Question rapide : pouvez-vous expliquer {topic} avec vos mots ?"]},
}


def clean_textbook(raw: str) -> str:
    """Remove textbook artifacts without touching ordinary words (no blind replaces)."""
    t = raw.replace("✔️", "").replace("✔", "")
    t = re.sub(r"\bCorrect answer\s*:", " The correct answer is: ", t, flags=re.I)
    t = re.sub(r"\bWhy\s*\?", " The reason is: ", t)
    if re.search(r"\bA\)\s.*\bB\)\s", t, re.S):              # real multiple-choice block
        t = re.sub(r"\b([A-D])\)\s*", r"Option \1. ", t)
    else:
        t = re.sub(r"(?m)^\s*([A-D])\)\s*", r"Option \1. ", t)
    t = re.sub(r"(?<![A-Za-z0-9])Q\s*(\d+)\b", r"Question \1", t)  # not FAQ 2 or IQ 120
    return re.sub(r"[ \t]+", " ", t).strip()


def _shorten(sentence: str, max_words: int = 18) -> List[str]:
    if len(sentence.split()) <= max_words:
        return [sentence]
    parts = re.split(r"(?<=[,;:])\s+|\s+(?=(?:which|because|while|whereas|but)\b)", sentence)
    out, cur = [], ""
    for p in parts:
        p = p.strip(" ,;:")
        if not p:
            continue
        if cur and len((cur + " " + p).split()) > max_words:
            out.append(cur); cur = p
        else:
            cur = f"{cur}, {p}" if cur else p
    if cur:
        out.append(cur)
    return [o[0].upper() + o[1:].rstrip(".!?") + "." for o in out]


def _pick(pool: List[str], seed: int) -> str:
    return pool[seed % len(pool)]


def build_lesson_script(topic: str, raw: Any, level: str = "normal", lang: str = "en") -> List[str]:
    """Return spoken-style segments: intro, ideas with varied transitions, recap, check question."""
    S = SCRIPT.get(lang, SCRIPT["en"])
    seed = int(hashlib.md5(str(topic).encode("utf-8")).hexdigest(), 16)   # stable, so audio caching works
    if not isinstance(raw, str) or len(raw.strip()) < 10:
        return [_pick(S["intro"], seed).format(topic=topic)]
    text = clean_textbook(raw)
    blocks = [b.strip() for b in re.split(r"\b(?:Question|Point)\s*:", text) if b.strip()]
    if len(blocks) <= 1:
        sents = split_sentences(text)
        blocks = [" ".join(sents[i:i + 3]) for i in range(0, len(sents), 3)] or [text]
    segs = [_pick(S["intro"], seed).format(topic=topic)]
    first_sentence = ""
    for i, block in enumerate(blocks):
        sents = split_sentences(block)
        if level == "simple":
            sents = [x for s in sents for x in _shorten(s)]
        if i > 0:
            segs.append(_pick(S["transitions"], seed + i))
        if sents and not first_sentence:
            first_sentence = " ".join(sents[0].split()[:25])
        segs.extend(sents)
    segs.append(_pick(S["recap"], seed).format(key=first_sentence, topic=topic))
    segs.append(_pick(S["check"], seed).format(topic=topic))
    return segs


def humanize_explanation(topic: str, raw_content: Any, level: str = "normal", lang: str = "en",
                         ssml: bool = False, speed: float = 1.0, tutor: Optional["LLMTutor"] = None) -> str:
    """Turn textbook text into a teaching script (plain text, or SSML when ssml=True)."""
    segs: Optional[List[str]] = None
    if tutor and tutor.available() and isinstance(raw_content, str) and len(raw_content.strip()) >= 10:
        rewritten = tutor.rewrite_lesson(topic, raw_content, level, lang)
        if rewritten:
            segs = split_sentences(rewritten)
    segs = segs or build_lesson_script(topic, raw_content, level, lang)
    if ssml:
        return build_ssml(speech_units(segs), lang, speed)
    return " ".join(segs)


# ============================================================================
# 7. TTS PROVIDERS + CACHE + VOICE SERVICE
# ============================================================================
class TTSProvider:
    name = "base"

    def supports(self, lang: str) -> bool:
        return False

    def synthesize(self, ssml: str, lang: str) -> bytes:
        raise NotImplementedError


class GoogleCloudTTS(TTSProvider):
    """Needs: pip install google-cloud-texttospeech and Google credentials.
    Check Google's current language list; unsupported languages (e.g. Oromo) fall back to text."""
    name = "google"
    CODES = {"en": "en-US", "am": "am-ET", "ar": "ar-XA", "fr": "fr-FR"}

    def supports(self, lang: str) -> bool:
        return lang in self.CODES

    def synthesize(self, ssml: str, lang: str) -> bytes:
        from google.cloud import texttospeech
        client = texttospeech.TextToSpeechClient()
        resp = client.synthesize_speech(
            input=texttospeech.SynthesisInput(ssml=ssml),
            voice=texttospeech.VoiceSelectionParams(language_code=self.CODES[lang]),
            audio_config=texttospeech.AudioConfig(audio_encoding=texttospeech.AudioEncoding.MP3))
        return resp.audio_content


class VoiceService:
    """SSML in, cached audio files out. Falls back to on-screen text if voice is unavailable."""

    def __init__(self, provider: Optional[TTSProvider] = None, cache_dir: Path = DATA_DIR / "audio"):
        self.provider, self.cache = provider, Path(cache_dir)

    def speak(self, segments: List[str], lang: str = "en", speed: float = 1.0,
              pron: Optional[Dict[str, str]] = None) -> Dict[str, Any]:
        chunks = ssml_chunks(speech_units(segments), lang, speed, pron)
        text = " ".join(segments)
        if not self.provider or not self.provider.supports(lang):
            return {"mode": "text_only", "text": text, "ssml": chunks}
        try:
            self.cache.mkdir(parents=True, exist_ok=True)
            files = []
            for ssml in chunks:
                key = hashlib.sha256(f"{self.provider.name}|{lang}|{ssml}".encode("utf-8")).hexdigest()
                f = self.cache / f"{key}.mp3"
                if not f.exists():
                    f.write_bytes(self.provider.synthesize(ssml, lang))
                files.append(str(f))
            return {"mode": "audio", "files": files, "text": text}
        except Exception as e:  # network, quota, credentials
            log.warning("TTS failed, using text only: %s", e)
            return {"mode": "text_only", "text": text, "ssml": chunks}


# ============================================================================
# 8. OPTIONAL LLM TUTOR (academic-only guardrails, offline-safe)
# ============================================================================
class LLMTutor:
    SYSTEM = ("You are AceGrade, a patient academic tutor for students. Answer only study-related questions; "
              "politely redirect anything else. Use simple words and short sentences suitable for being read aloud. "
              "No markdown. If you are not sure, say so instead of guessing. Reply in {language}.")

    def __init__(self, model: Optional[str] = None):
        self.model = model or os.environ.get("ACEGRADE_MODEL", "claude-sonnet-5-5")
        self._client = None

    def available(self) -> bool:
        if not os.environ.get("ANTHROPIC_API_KEY"):
            return False
        try:
            import anthropic  # noqa: F401
            return True
        except ImportError:
            return False

    def _ask(self, system: str, prompt: str, max_tokens: int = 700) -> Optional[str]:
        try:
            import anthropic
            self._client = self._client or anthropic.Anthropic()
            r = self._client.messages.create(model=self.model, max_tokens=max_tokens, system=system,
                                             messages=[{"role": "user", "content": prompt}])
            return "".join(b.text for b in r.content if getattr(b, "type", "") == "text").strip() or None
        except Exception as e:
            log.warning("LLM call failed: %s", e)
            return None

    def ask(self, question: str, lang: str = "en", level: str = "normal", context: str = "") -> Optional[str]:
        system = self.SYSTEM.format(language=LANG_NAMES_DISPLAY.get(lang, "English")) + f" Explanation level: {level}."
        return self._ask(system, (f"Lesson context:\n{context}\n\n" if context else "") + f"Student question: {question}")

    def rewrite_lesson(self, topic: str, raw: str, level: str, lang: str) -> Optional[str]:
        system = self.SYSTEM.format(language=LANG_NAMES_DISPLAY.get(lang, "English"))
        prompt = (f"Rewrite this textbook content about '{topic}' as a friendly spoken lesson of about 60 seconds at a "
                  f"{level} level. Use one idea per sentence, one everyday local example, and end with one check "
                  f"question. Only use facts present in the text.\n\n{raw}")
        return self._ask(system, prompt, 900)


# ============================================================================
# 9. LEARNING: spaced repetition, adaptive difficulty, progress
# ============================================================================
@dataclass
class Card:
    id: str
    subject: str
    question: str
    answer: str
    difficulty: int = 2          # 1 easy .. 5 hard
    ease: float = 2.5
    interval: int = 0
    reps: int = 0
    due: float = field(default_factory=time.time)


def sm2_review(card: Card, quality: int) -> Card:
    """SM-2 spaced repetition. quality: 0 (blackout) .. 5 (perfect)."""
    quality = max(0, min(5, quality))
    if quality < 3:
        card.reps, card.interval = 0, 1
    else:
        card.reps += 1
        card.interval = 1 if card.reps == 1 else 6 if card.reps == 2 else max(1, round(card.interval * card.ease))
    card.ease = max(1.3, card.ease + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    card.due = time.time() + card.interval * 86400
    return card


class Progress:
    """Study time, accuracy, streaks, weak topics. Stores no audio and no personal data."""

    def __init__(self, path: Path = DATA_DIR / "progress.json"):
        self.path = Path(path)
        self.data: Dict[str, Any] = {"subjects": {}, "days": []}
        if self.path.exists():
            try:
                self.data = json.loads(self.path.read_text(encoding="utf-8"))
            except (OSError, ValueError):
                pass

    def _subject(self, s: str) -> Dict[str, int]:
        return self.data["subjects"].setdefault(s, {"seconds": 0, "correct": 0, "wrong": 0})

    def _touch(self) -> None:
        today = date.today().isoformat()
        if today not in self.data["days"]:
            self.data["days"].append(today)

    def record_study(self, subject: str, seconds: int) -> None:
        self._subject(subject)["seconds"] += int(seconds); self._touch()

    def record_answer(self, subject: str, correct: bool) -> None:
        self._subject(subject)["correct" if correct else "wrong"] += 1; self._touch()

    def streak(self) -> int:
        days, d, n = set(self.data["days"]), date.today(), 0
        if d.isoformat() not in days:
            d -= timedelta(days=1)
        while d.isoformat() in days:
            n += 1; d -= timedelta(days=1)
        return n

    def weak_topics(self, min_answers: int = 3, threshold: float = 0.6) -> List[str]:
        weak = []
        for s, v in self.data["subjects"].items():
            total = v["correct"] + v["wrong"]
            if total >= min_answers and v["correct"] / total < threshold:
                weak.append((v["correct"] / total, s))
        return [s for _, s in sorted(weak)]

    def save(self) -> None:
        try:
            self.path.parent.mkdir(parents=True, exist_ok=True)
            self.path.write_text(json.dumps(self.data, ensure_ascii=False, indent=1), encoding="utf-8")
        except OSError as e:
            log.warning("Could not save progress: %s", e)


class QuizEngine:
    def __init__(self, progress: Optional[Progress] = None, locales: Optional[Locales] = None):
        self.cards: Dict[str, Card] = {}
        self.progress = progress or Progress()
        self.locales = locales or Locales()
        self.level: Dict[str, int] = {}
        self.streak_correct = 0

    def add(self, card: Card) -> None:
        self.cards[card.id] = card

    def next_card(self, subject: Optional[str] = None) -> Optional[Card]:
        pool = [c for c in self.cards.values() if not subject or c.subject == subject]
        due = [c for c in pool if c.due <= time.time()]
        if not due:
            return None
        target = self.level.get(subject or "", 2)
        return min(due, key=lambda c: (abs(c.difficulty - target), c.due))

    def answer(self, card_id: str, correct: bool, lang: str = "en", quality: Optional[int] = None) -> str:
        card = self.cards[card_id]
        sm2_review(card, quality if quality is not None else (4 if correct else 1))
        self.progress.record_answer(card.subject, correct)
        cur = self.level.get(card.subject, 2)
        self.streak_correct = self.streak_correct + 1 if correct else 0
        if correct and self.streak_correct % 2 == 0:
            cur = min(5, cur + 1)            # adapt: raise difficulty after 2 correct in a row
        elif not correct:
            cur = max(1, cur - 1)
        self.level[card.subject] = cur
        self.progress.save()
        if correct and self.streak_correct >= 3:      # praise only when earned
            return self.locales.t(lang, "fb_streak", n=self.streak_correct)
        return self.locales.t(lang, "fb_correct" if correct else "fb_wrong")


# ============================================================================
# 10. FACADE: one object for your app
# ============================================================================
class AceGradeAssistant:
    def __init__(self, tts: Optional[TTSProvider] = None, tutor: Optional[LLMTutor] = None,
                 lang: str = "en", log_unknown: bool = True):
        self.locales = Locales()
        self.engine = CommandEngine(self.locales, UnrecognizedLog(enabled=log_unknown))
        self.session = Session(lang=lang)
        self.tutor = tutor or LLMTutor()
        self.voice = VoiceService(tts)
        self.progress = Progress()
        self.quiz = QuizEngine(self.progress, self.locales)

    def handle(self, text: str, lesson_context: str = "") -> Dict[str, Any]:
        reply = self.engine.process(text, self.session)
        replies = reply.replies
        if reply.needs_llm and self.tutor.available():       # layer 3: LLM for anything rules cannot parse
            answer = self.tutor.ask(text, reply.lang, self.session.level, lesson_context)
            if answer:
                replies = [answer]
                reply.results = [Result("chat", "llm_answer", 0.5, response_key="llm")]
        return {"lang": reply.lang, "results": [r.to_dict() for r in reply.results],
                "replies": replies, "session": asdict(self.session)}

    def teach(self, topic: str, raw_content: str, speak: bool = True) -> Dict[str, Any]:
        s = self.session
        segs = split_sentences(humanize_explanation(topic, raw_content, s.level, s.lang, tutor=self.tutor))
        out: Dict[str, Any] = {"segments": segs}
        if speak:
            out["voice"] = self.voice.speak(segs, s.lang, s.speed)
        return out


# ============================================================================
# 11. BACKWARD-COMPATIBLE FUNCTIONS
# ============================================================================
_DEFAULT_ENGINE: Optional[CommandEngine] = None


def understand_command(text: str) -> Dict[str, Any]:
    """Stateless legacy API: returns {'type','response', optional 'page','extra','action'}."""
    global _DEFAULT_ENGINE
    _DEFAULT_ENGINE = _DEFAULT_ENGINE or CommandEngine()
    reply = _DEFAULT_ENGINE.process(text, Session())
    r = reply.results[0]
    out: Dict[str, Any] = {"type": "chat" if r.type in ("unknown", "confirm") else r.type, "response": reply.replies[0]}
    if r.page:
        out["page"] = r.page
    if r.type == "nav" and r.params.get("subject"):
        out["extra"] = r.params["subject"]
    if r.action:
        out["action"] = r.action
    return out


# ============================================================================
# 12. SELF-TESTS AND DEMO
# ============================================================================
def run_self_tests() -> None:
    eng = CommandEngine()

    def top(text: str, session: Optional[Session] = None) -> Result:
        return eng.process(text, session or Session()).results[0]

    r = top("Open Biology"); assert r.intent == "open_subject" and r.params["subject"] == "Biology"
    r = top("I think this is hard"); assert r.intent != "greet"
    r = top("Hello there"); assert r.intent == "greet"
    r = top("open math quiz"); assert r.intent == "start_quiz" and r.params["subject"] == "Mathematics"
    r = top("biology"); assert r.intent == "open_subject"
    r = top("Show me how photosynthesis works at home"); assert r.intent != "open_dashboard"
    r = top("stop reading"); assert r.intent == "stop_voice"
    rep = eng.process("ክፈት ባዮሎጂ"); assert rep.lang == "am" and rep.results[0].params["subject"] == "Biology"
    rep = eng.process("افتح الفيزياء"); assert rep.lang == "ar" and rep.results[0].params["subject"] == "Physics"
    rep = eng.process("Open biology and read the lesson")
    assert [x.intent for x in rep.results] == ["open_subject", "read_aloud"]
    assert rep.results[1].params["subject"] == "Biology"          # context carried over
    s = Session(); eng.process("slower", s); assert s.speed == 0.75
    s = Session(); eng.process("explain simpler", s); assert s.level == "simple"
    assert top("speak amharic").params["language_code"] == "am"
    assert understand_command("open chemistry")["extra"] == "Chemistry"

    assert "H two O" in prepare_for_speech("H₂O + CO2 → sugar")
    assert "squared" in prepare_for_speech("x^2")
    assert len(split_sentences("Dr. Smith used 3.5 g. Then he left.")) == 2
    assert "IQ 120" in clean_textbook("The IQ 120 score. See FAQ 2.")
    assert "Question 3" in clean_textbook("Q3 What is DNA?")
    assert "<break" in humanize_explanation("Cells", "Cells are the basic unit of life. They divide to grow.", ssml=True)
    assert "&amp;" in build_ssml([("salt & water", True)])
    assert all(len(c) < 5000 for c in ssml_chunks([("word " * 50, True)] * 200, max_chars=3000))
    plain = humanize_explanation("Cells", "Cells are the basic unit of life. They divide to grow.")
    assert "..." not in plain and "Cells" in plain

    c = Card("1", "Biology", "q", "a"); sm2_review(c, 5); sm2_review(c, 5)
    assert c.interval == 6
    print("All self-tests passed.")


def _demo() -> None:
    bot = AceGradeAssistant(log_unknown=False)
    for line in ["hello", "open biology and read the lesson", "slower", "explain simpler",
                 "ክፈት ባዮሎጂ", "افتح الفيزياء", "quiz me on chemistry", "what is the weather"]:
        out = bot.handle(line)
        print(f"> {line}\n  [{out['lang']}] {out['replies']}")
    lesson = bot.teach("Photosynthesis", "Plants make food using sunlight, water, and CO2. "
                       "This process happens in the chloroplasts, which contain chlorophyll.", speak=False)
    print("\nLesson script:\n ", " ".join(lesson["segments"]))


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    run_self_tests()
    if "--test" not in sys.argv:
        _demo()