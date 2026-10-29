import os

for root, dirs, files in os.walk('app/src/main'):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as fh:
                t = fh.read()
                if 'VoiceAssistantActivity.class' in t or 'VoiceAssistantActivity' in t:
                    print(f, 'references VoiceAssistantActivity')

