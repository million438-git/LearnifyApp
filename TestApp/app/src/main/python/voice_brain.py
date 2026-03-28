# noinspection PyInterpreter
import re

def understand_command(text):
    text = text.lower().strip()
    
    # Navigation Commands
    if any(cmd in text for cmd in ["open", "go to", "show me", "take me to"]):
        if "biology" in text: 
            return {"type": "nav", "response": "Certainly! Opening the Biology modules. Let's explore the science of life.", "page": "SubjectActivity", "extra": "Biology"}
        if "physics" in text: 
            return {"type": "nav", "response": "Opening Physics. Time to master the laws of nature.", "page": "SubjectActivity", "extra": "Physics"}
        if "math" in text: 
            return {"type": "nav", "response": "Mathematics is the key to logic. Opening your math catalog.", "page": "SubjectActivity", "extra": "Mathematics"}
        if "chemistry" in text: 
            return {"type": "nav", "response": "Opening Chemistry. Let's look at the elements.", "page": "SubjectActivity", "extra": "Chemistry"}
        if "english" in text:
            return {"type": "nav", "response": "Opening English Language and Literature. Let's dive into the world of words.", "page": "SubjectActivity", "extra": "English"}
        if "aptitude" in text or "scholastic" in text:
            return {"type": "nav", "response": "Opening Scholastic Aptitude. Let's sharpen those analytical skills.", "page": "SubjectActivity", "extra": "Scholastic Aptitude"}
        
        if "setting" in text: 
            return {"type": "nav", "response": "Opening your settings so you can personalize your experience.", "page": "SettingsActivity"}
        if "profile" in text: 
            return {"type": "nav", "response": "Loading your student profile and progress.", "page": "ProfileActivity"}
        if "challenge" in text or "quiz" in text: 
            return {"type": "nav", "response": "Starting your Daily Exam Challenge. You've got this!", "page": "DailyChallengeActivity"}
        if "dashboard" in text or "home" in text:
            return {"type": "nav", "response": "Returning to your study dashboard.", "page": "DashboardActivity"}
        if "library" in text or "course" in text:
            return {"type": "nav", "response": "Opening the Academic Library.", "page": "SubjectActivity"}
        if "chat" in text or "ai" in text or "assistant" in text:
            return {"type": "nav", "response": "Opening EduAI, your personal academic assistant.", "page": "AIChatActivity"}

    # Action Commands
    if "summarize" in text or "summary" in text:
        return {"type": "action", "response": "I'll prepare a concise summary of this lesson for you.", "action": "summarize"}
    
    if "read" in text and ("lesson" in text or "this" in text or "aloud" in text):
        return {"type": "action", "response": "Starting the read-aloud session for this topic.", "action": "read_aloud"}

    if "stop" in text and ("voice" in text or "reading" in text or "speaking" in text):
        return {"type": "action", "response": "Stopping the voice session.", "action": "stop_voice"}

    # Conversational Greeting
    if any(greet in text for greet in ["hello", "hi", "hey", "who are you"]):
        return {"type": "chat", "response": "Hello! I am your AceGrade Academic Partner. I can read your lessons, navigate the app, or quiz you. What shall we study today?"}

    return {"type": "chat", "response": "I understood: " + text + ". You can say things like 'Open Biology', 'Read this lesson', or 'Go to Dashboard'."}

def humanize_explanation(topic, raw_content):
    """
    Transforms raw textbook text into a high-quality teaching script.
    Cleans up noise and adds natural teacher-like transitions.
    """
    if not raw_content or len(raw_content) < 10:
        return f"Welcome to our study session on {topic}. Let's dive into the core principles."

    # Remove repetitive technical artifacts
    text = raw_content.replace("Correct answer:", ". The correct answer is: ")
    text = text.replace("Why?", ". The insight here is: ")
    text = text.replace("✔️", "")

    # Simplify Option markers for speech
    text = re.sub(r'([A-D])\)', r'Option \1. ', text)
    text = re.sub(r'Q\s*(\d+)', r'Question \1', text)

    # Humanized Teacher Intro
    human_script = f"Hello student. Let's study {topic} together. ... First, let's look at the foundational concepts. ... "

    # Add natural pauses and scholarly transitions
    # Split by common textbook markers
    blocks = re.split(r'Question:|Point:', text)
    if len(blocks) > 1:
        for i, block in enumerate(blocks):
            if not block.strip(): continue
            if i > 0:
                human_script += "... ... Moving on to the next point. ... "
            human_script += block.strip() + ". "
    else:
        # Fallback for non-Q&A content
        sentences = text.split(". ")
        for i, sent in enumerate(sentences):
            if i % 3 == 0 and i > 0: 
                human_script += "... Now, this is an important detail. ... "
            human_script += sent.strip() + ". "

    human_script += " ... ... That concludes our session for this topic. Great job staying focused!"
    return human_script
