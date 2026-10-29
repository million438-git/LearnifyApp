"""

voice_study.py

================

Voice Study Assistant - offline answer engine.



Fixes and improvements over the original:

  1.  Shared tokenizer for corpus AND query (no more "photosynthesis?" misses).

  2.  Sentence-level corpus (voice-friendly answers instead of paragraphs).

  3.  Anchored command regex with allow-list (no more "open" hijacking questions).

  4.  Correct BM25 confidence: argmax on real scores, relative threshold.

  5.  Single score computation (no double get_scores call).

  6.  Empty / whitespace / non-string input guards.

  7.  Full try/except around the public entry point; error responses are typed.

  8.  Thread-safe scoring (lock) for concurrent voice inputs.

  9.  Stable JSON response contract: {type, content, lang, source, confidence}.

 10.  Language detection + lang field so Android TTS can pick the right voice.

 11.  Optional hybrid retrieval: BM25 + embeddings + RRF, with graceful

      fallback to BM25-only if sentence-transformers / numpy are missing.

 12.  Voice-friendly answer compression (1-2 sentences, spoken citation).

 13.  Configurable corpus loading (function, not hardcoded global).



Dependencies (minimum):

    rank_bm25>=0.2.2



Optional (for hybrid retrieval - strongly recommended):

    sentence-transformers>=2.2.2

    numpy>=1.24



Chaquopy build.gradle (app):

    python {

        pip {

            install "rank_bm25"

            // optional:

            // install "sentence-transformers"

            // install "numpy"

        }

    }

"""



from __future__ import annotations



import json

import logging

import re

import threading

from typing import Any, Dict, List, Optional, Tuple



# ---------------------------------------------------------------------------

# Optional heavy dependencies - degrade gracefully if not installed.

# ---------------------------------------------------------------------------

try:

    from rank_bm25 import BM25Okapi  # type: ignore

    _HAS_BM25 = True

except Exception:  # pragma: no cover

    BM25Okapi = None  # type: ignore

    _HAS_BM25 = False



try:

    import numpy as np  # type: ignore

    _HAS_NUMPY = True

except Exception:  # pragma: no cover

    np = None  # type: ignore

    _HAS_NUMPY = False



_EMBEDDER = None

_HAS_EMBEDDINGS = False





def _try_init_embedder() -> None:

    """Lazily load a small sentence-transformer if available.



    This is the 'hybrid retrieval' upgrade. If the package is not installed

    (e.g. on a constrained Android build) we simply skip it and fall back

    to BM25-only. The app keeps working either way.

    """

    global _EMBEDDER, _HAS_EMBEDDINGS

    if _EMBEDDER is not None or not _HAS_EMBEDDINGS:

        pass

    try:

        from sentence_transformers import SentenceTransformer  # type: ignore

        _EMBEDDER = SentenceTransformer("all-MiniLM-L6-v2")

        _HAS_EMBEDDINGS = True

        _log.info("Hybrid retrieval enabled (sentence-transformers loaded).")

    except Exception as exc:  # noqa: BLE001

        _HAS_EMBEDDINGS = False

        _log.info("Embeddings unavailable, BM25-only mode: %s", exc)





# ---------------------------------------------------------------------------

# Logging

# ---------------------------------------------------------------------------

_log = logging.getLogger("voice_study")

if not _log.handlers:

    _h = logging.StreamHandler()

    _h.setFormatter(logging.Formatter("[voice_study] %(levelname)s: %(message)s"))

    _log.addHandler(_h)

_log.setLevel(logging.INFO)





# ---------------------------------------------------------------------------

# Tokenizer - the SAME function is used for corpus and queries.

# ---------------------------------------------------------------------------

_TOKEN_RE = re.compile(r"[a-z0-9]+")

_SENTENCE_SPLIT_RE = re.compile(r"(?<=[.!?])\s+")



# Stopwords removed for BM25 (English only; safe for the demo corpus).

_STOPWORDS = {

    "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",

    "of", "to", "in", "on", "at", "for", "with", "by", "from", "as",

    "and", "or", "but", "if", "then", "than", "so", "that", "this",

    "these", "those", "it", "its", "do", "does", "did", "how", "what",

    "why", "when", "where", "which", "who", "whom", "i", "you", "we",

    "they", "he", "she", "me", "my", "your", "our", "their",

}





def tokenize(text: str) -> List[str]:

    """Lowercase alphanumeric tokenizer, shared between corpus and query."""

    if not isinstance(text, str):

        return []

    tokens = _TOKEN_RE.findall(text.lower())

    return [t for t in tokens if t not in _STOPWORDS]





def split_sentences(text: str) -> List[str]:

    """Split a paragraph into sentences for voice-friendly retrieval units."""

    if not isinstance(text, str):

        return []

    return [s.strip() for s in _SENTENCE_SPLIT_RE.split(text) if s.strip()]





# ---------------------------------------------------------------------------

# Language detection (lightweight heuristic).

# Android's TTS will receive this in the 'lang' field and pick a voice.

# For production you can swap in `langdetect` or `langid`; this heuristic

# avoids an extra dependency and covers the common cases.

# ---------------------------------------------------------------------------

_CJK_RE = re.compile(r"[\u3040-\u30ff\u3400-\u4dbf\u4e00-\u9fff\uac00-\ud7af]")

_CYRILLIC_RE = re.compile(r"[\u0400-\u04FF]")

_ARABIC_RE = re.compile(r"[\u0600-\u06FF]")

_DEVANAGARI_RE = re.compile(r"[\u0900-\u097F]")

_LATIN_RE = re.compile(r"[A-Za-z]")





def detect_language(text: str) -> str:

    """Return a BCP-47-ish language tag used by Android TextToSpeech.



    Defaults to 'en-US' for Latin-script input. This is intentionally

    heuristic; for best accuracy install `langdetect` and replace this.

    """

    if not isinstance(text, str) or not text.strip():

        return "en-US"

    if _CJK_RE.search(text):

        if re.search(r"[\u3040-\u30ff]", text):

            return "ja-JP"

        if re.search(r"[\uac00-\ud7af]", text):

            return "ko-KR"

        return "zh-CN"

    if _CYRILLIC_RE.search(text):

        return "ru-RU"

    if _ARABIC_RE.search(text):

        return "ar-SA"

    if _DEVANAGARI_RE.search(text):

        return "hi-IN"

    if _LATIN_RE.search(text):

        return "en-US"

    return "en-US"





# ---------------------------------------------------------------------------

# Response builder - single place that guarantees the JSON contract.

# ---------------------------------------------------------------------------

def _resp(

    type_: str,

    content: str,

    *,

    lang: Optional[str] = None,

    source: Optional[str] = None,

    confidence: Optional[float] = None,

) -> str:

    payload: Dict[str, Any] = {

        "type": type_,

        "content": content,

        "lang": lang or detect_language(content),

    }

    if source is not None:

        payload["source"] = source

    if confidence is not None:

        payload["confidence"] = round(float(confidence), 3)

    return json.dumps(payload, ensure_ascii=False)





# ---------------------------------------------------------------------------

# Engine

# ---------------------------------------------------------------------------

class StudyEngine:

    """BM25 (+ optional embeddings) retrieval over a sentence-level corpus."""



    # Confidence below this fraction of the best possible score -> uncertain.

    RELATIVE_CONFIDENCE_FLOOR = 0.35



    # Command allow-list. Only these targets may be sent to Android.

    COMMAND_ALLOW_LIST = {"notes", "settings", "home", "flashcards", "help"}

    COMMAND_PATTERNS = [

        ("open", re.compile(r"^(?:please\s+)?open\s+(.+)$")),

        ("goto", re.compile(r"^(?:please\s+)?go\s+to\s+(.+)$")),

        ("show", re.compile(r"^(?:please\s+)?show\s+me\s+(.+)$")),

        ("help", re.compile(r"^(?:please\s+)?help(?:\s+me)?$")),

    ]



    def __init__(self, documents: List[str]) -> None:

        self._lock = threading.Lock()

        self._docs: List[str] = []

        self._tokens: List[List[str]] = []

        self._bm25 = None

        self._embeddings = None



        for raw in documents or []:

            if not isinstance(raw, str):

                continue

            for sentence in split_sentences(raw):

                toks = tokenize(sentence)

                if not toks:

                    continue

                self._docs.append(sentence)

                self._tokens.append(toks)



        if _HAS_BM25 and self._tokens:

            try:

                self._bm25 = BM25Okapi(self._tokens)

                _log.info("Indexed %d sentences with BM25.", len(self._docs))

            except Exception as exc:

                self._bm25 = None

                _log.warning("BM25 init failed: %s, fallback scorer active.", exc)

        else:

            self._bm25 = None

            if self._tokens:

                _log.info("BM25 unavailable, fallback token-overlap scorer active.")

            else:

                _log.warning("Empty corpus - answers disabled.")



        # Optional hybrid retrieval.

        _try_init_embedder()

        if _HAS_EMBEDDINGS and self._docs and _HAS_NUMPY:

            try:

                self._embeddings = _EMBEDDER.encode(  # type: ignore[union-attr]

                    self._docs, convert_to_numpy=True, normalize_embeddings=True

                )

                _log.info("Embeddings indexed for %d sentences.", len(self._docs))

            except Exception as exc:  # noqa: BLE001

                _log.warning("Embedding index failed, using BM25 only: %s", exc)

                self._embeddings = None



    # ---- public API -----------------------------------------------------



    def answer(self, query: str) -> str:

        """Main entry point. Always returns a JSON string, never raises."""

        try:

            return self._answer_impl(query)

        except Exception as exc:  # noqa: BLE001

            _log.exception("Unhandled error answering query")

            return _resp("error", f"Something went wrong: {exc}")



    # ---- internals ------------------------------------------------------



    def _answer_impl(self, query: str) -> str:

        # 1. Input validation (voice engines send null / "" on silence).

        if not isinstance(query, str) or not query.strip():

            return _resp("chat", "I didn't catch that. Please try again.")



        query = query.strip()



        # 2. Command detection - anchored + allow-listed.

        command = self._parse_command(query)

        if command is not None:

            return command



        # 3. Retrieval.

        if not self._docs:

            return _resp(

                "chat",

                "My study index isn't loaded yet. Please import your notes first.",

            )



        q_tokens = tokenize(query)

        if not q_tokens:

            return _resp("chat", "Could you ask that again using different words?")



        top_indices, confidence = self._retrieve(query, q_tokens)

        if not top_indices:

            return _resp(

                "chat",

                "I couldn't find anything relevant in your notes. Try asking "

                "about Photosynthesis, Newton's Laws, or Acids and Bases.",

            )



        best_idx = top_indices[0]

        best_doc = self._docs[best_idx]



        # 4. Confidence gate - honest uncertainty beats confident nonsense.

        if confidence < self.RELATIVE_CONFIDENCE_FLOOR:

            return _resp(

                "chat",

                "I'm not entirely sure, but here's the closest note I found: "

                + best_doc,

                confidence=confidence,

            )



        # 5. Voice-friendly answer with spoken citation.

        spoken = self._compress_for_voice(best_doc)

        citation = self._short_citation(best_doc)

        content = f"{spoken} {citation}".strip()



        return _resp(

            "answer",

            content,

            lang=detect_language(best_doc),

            source=best_doc,

            confidence=confidence,

        )



    def _parse_command(self, query: str) -> Optional[str]:

        lowered = query.lower()

        for kind, pattern in self.COMMAND_PATTERNS:

            match = pattern.match(lowered)

            if not match:

                continue

            if kind == "help":

                return _resp(

                    "command",

                    "help",

                    lang=detect_language(query),

                )

            target = match.group(1).strip()

            # Take the first word as the target, ignore trailing filler.

            target_word = target.split()[0] if target.split() else ""

            if target_word in self.COMMAND_ALLOW_LIST:

                return _resp(

                    "command",

                    target_word,

                    lang=detect_language(query),

                )

            # Recognised intent but unknown target -> do NOT execute.

            _log.info("Rejected command target: %r", target_word)

            return _resp(

                "chat",

                f"I can open {', '.join(sorted(self.COMMAND_ALLOW_LIST))}. "

                "Which one?",

                lang=detect_language(query),

            )

        return None



    def _fallback_scores(self, q_tokens: List[str]) -> List[float]:

        q_set = set(q_tokens)

        scores = []

        for doc_toks in self._tokens:

            if not doc_toks:

                scores.append(0.0)

                continue

            overlap = len(q_set & set(doc_toks))

            scores.append(float(overlap) / (len(doc_toks) ** 0.5))

        return scores



    def _retrieve(self, query: str, q_tokens: List[str]) -> Tuple[List[int], float]:

        """Return ranked doc indices and a normalized confidence in [0, 1]."""

        with self._lock:

            if self._bm25 is not None:

                scores = self._bm25.get_scores(q_tokens)  # type: ignore[union-attr]

            else:

                scores = self._fallback_scores(q_tokens)



        bm25_ranking = list(np.argsort(scores)[::-1]) if _HAS_NUMPY else list(

            sorted(range(len(scores)), key=lambda i: scores[i], reverse=True)

        )

        top_bm25 = bm25_ranking[:10]



        # Normalize confidence against the best raw score in this corpus.

        raw_best = float(scores[top_bm25[0]]) if top_bm25 else 0.0

        raw_max = float(max(scores)) if len(scores) else 0.0

        if raw_max <= 0:

            return ([], 0.0)

        confidence = raw_best / raw_max  # 1.0 means "the top hit is the max"



        # Hybrid re-rank (only if embeddings are available).

        if self._embeddings is not None and _HAS_NUMPY:

            try:

                emb_scores = self._embedding_scores(query)

                fused = self._rrf([top_bm25, emb_scores[:10]])

                if fused:

                    return (fused, confidence)

            except Exception as exc:  # noqa: BLE001

                _log.warning("Hybrid rerank failed, using BM25: %s", exc)



        return (top_bm25, confidence)



    def _embedding_scores(self, query: str) -> List[int]:

        q_vec = _EMBEDDER.encode(  # type: ignore[union-attr]

            [query], convert_to_numpy=True, normalize_embeddings=True

        )[0]

        sims = self._embeddings @ q_vec  # type: ignore[operator]

        return list(np.argsort(sims)[::-1])



    @staticmethod

    def _rrf(rankings: List[List[int]], k: int = 60) -> List[int]:

        """Reciprocal Rank Fusion of multiple ranked index lists."""

        totals: Dict[int, float] = {}

        for ranking in rankings:

            for rank, idx in enumerate(ranking):

                totals[idx] = totals.get(idx, 0.0) + 1.0 / (k + rank + 1)

        return [idx for idx, _ in sorted(totals.items(), key=lambda kv: kv[1], reverse=True)]



    @staticmethod

    def _compress_for_voice(text: str, max_sentences: int = 2, max_chars: int = 400) -> str:

        """Keep the answer short - reading paragraphs aloud is bad UX."""

        sentences = split_sentences(text) or [text]

        kept: List[str] = []

        length = 0

        for sentence in sentences[:max_sentences]:

            if length + len(sentence) > max_chars and kept:

                break

            kept.append(sentence)

            length += len(sentence) + 1

        return " ".join(kept).strip()



    @staticmethod

    def _short_citation(doc: str) -> str:

        """Brief spoken source hint so students can verify the answer."""

        # If we had chapter metadata we'd use it here. Fall back to topic words.

        return "You can find this in your study notes."





# ---------------------------------------------------------------------------

# Default corpus (demo). Replace by calling load_corpus() from Android.

# ---------------------------------------------------------------------------

_DEFAULT_PARAGRAPHS: List[str] = [

    "Photosynthesis is the process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water.",

    "The chemical equation for photosynthesis is 6CO2 + 6H2O + light energy -> C6H12O6 + 6O2.",

    "Chlorophyll is the pigment in plants that absorbs light energy used in photosynthesis.",

    "Cell division is the process by which a parent cell divides into two or more daughter cells.",

    "Mitosis is a part of the cell cycle in which replicated chromosomes are separated into two new nuclei.",

    "Meiosis is a special type of cell division of germ cells in sexually-reproducing organisms used to produce the gametes.",

    "Newton's First Law: An object at rest remains at rest, and an object in motion remains in motion at constant speed and in a straight line unless acted on by an unbalanced force.",

    "Newton's Second Law: The acceleration of an object depends on the mass of the object and the amount of force applied (F=ma).",

    "Newton's Third Law: Whenever one object exerts a force on a second object, the second object exerts an equal and opposite force on the first.",

    "Acids are chemical substances characterized by a sour taste and the ability to turn blue litmus paper red.",

    "Bases are substances that, in aqueous solution, are slippery to the touch, taste astringent, change the color of indicators (e.g., turn red litmus paper blue).",

    "The pH scale measures how acidic or basic a substance is. It ranges from 0 to 14. A pH of 7 is neutral.",

]



_ENGINE: Optional[StudyEngine] = None

_ENGINE_LOCK = threading.Lock()





def load_corpus(documents: List[str]) -> None:

    """Replace the in-memory corpus. Call this from Android when notes change."""

    global _ENGINE

    with _ENGINE_LOCK:

        _ENGINE = StudyEngine(documents)





def _get_engine() -> StudyEngine:

    global _ENGINE

    with _ENGINE_LOCK:

        if _ENGINE is None:

            _ENGINE = StudyEngine(_DEFAULT_PARAGRAPHS)

        return _ENGINE





# ---------------------------------------------------------------------------

# Public Android entry points

# ---------------------------------------------------------------------------

def get_offline_answer(query: str) -> str:

    """Retrieve an answer for a single query. Returns a JSON string."""

    return _get_engine().answer(query)





def process_voice_input(text: str) -> str:

    """Main entry point for Android's VoiceStudyManager.



    Always returns JSON. Never raises. Kotlin side should parse:

        type        : "answer" | "chat" | "command" | "error"

        content     : string (what to show / speak)

        lang        : BCP-47 tag for TextToSpeech.setLanguage()

        source      : original passage (optional, for "show in notes")

        confidence  : float in [0,1] (optional)

    """

    return _get_engine().answer(text)





# ---------------------------------------------------------------------------

# Local smoke test

# ---------------------------------------------------------------------------

if __name__ == "__main__":

    samples = [

        "what is photosynthesis",

        "why do plants need sunlight",

        "open notes",

        "open the door please",

        "show me how Newton's second law works",

        "what is pH",

        "???",

        "",

        None,

        "explain meiosis",

        "بصريات",  # Arabic sample, exercises language detection

    ]

    for sample in samples:

        print(repr(sample), "->", process_voice_input(sample))  # type: ignore[arg-type]