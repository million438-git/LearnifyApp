import json
from rank_bm25 import BM25Okapi
import re

# Mock textbook data for demonstration
# In a real app, this would be loaded from assets or a local database
TEXTBOOK_DATA = [
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
    "The pH scale measures how acidic or basic a substance is. It ranges from 0 to 14. A pH of 7 is neutral."
]

tokenized_corpus = [doc.lower().split() for doc in TEXTBOOK_DATA]
bm25 = BM25Okapi(tokenized_corpus)

def get_offline_answer(query):
    query = query.lower().strip()
    
    # 1. Check for specific system commands first
    if any(cmd in query for cmd in ["open", "go to", "show me"]):
        return json.dumps({"type": "command", "content": query})

    # 2. Perform Retrieval (BM25)
    tokenized_query = query.split()
    top_n = bm25.get_top_n(tokenized_query, TEXTBOOK_DATA, n=1)
    
    if not top_n or len(query) < 3:
        return json.dumps({
            "type": "chat", 
            "content": "I couldn't find a specific answer in your textbook for that. Could you try rephrasing or asking about Photosynthesis, Newton's Laws, or Acids and Bases?"
        })

    # 3. Formulate Response
    answer = top_n[0]
    
    # Simple heuristic to see if the match is good enough
    score = bm25.get_scores(tokenized_query)
    if max(score) < 1.0:
        return json.dumps({
            "type": "chat", 
            "content": "I'm not entirely sure, but here is what I found in your notes: " + answer
        })

    return json.dumps({
        "type": "answer",
        "content": answer
    })

def process_voice_input(text):
    """
    Main entry point for the Android VoiceStudyManager.
    Returns a JSON string containing the response type and content.
    """
    return get_offline_answer(text)
