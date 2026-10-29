package com.million.learnify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * VoiceStudyManager - World-Class Academic Voice Engine.
 * Optimized for Blind Accessibility and hands-free subject navigation.
 * Integrated with Python Brain for natural command understanding.
 */
public class VoiceStudyManager implements TextToSpeech.OnInitListener {

    private static final String TAG = "VoiceStudyManager";
    private final Context context;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private boolean isTtsReady = false;
    private VoiceCallback callback;

    public interface VoiceCallback {
        void onCommandRecognized(String text);
        void onActionRequired(String action, String page, String extra);
        default void onSpeechFinished() {}
    }

    public VoiceStudyManager(Context context, VoiceCallback callback) {
        this.context = context;
        this.callback = callback;
        initSpeech();
    }

    private void initSpeech() {
        try {
            tts = new TextToSpeech(context, this);
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
                setupRecognitionListener();
            } else {
                Log.e(TAG, "Speech Recognition not available on this device");
            }
        } catch (Exception e) {
            Log.e(TAG, "Voice Init Failed: " + e.getMessage());
        }
    }

    private void setupRecognitionListener() {
        if (speechRecognizer == null) return;
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { Log.d(TAG, "Listening..."); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                Log.e(TAG, "Speech Error: " + error);
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    processCommand(matches.get(0));
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void processCommand(String command) {
        if (callback != null) callback.onCommandRecognized(command);

        try {
            if (Python.isStarted()) {
                Python py = Python.getInstance();
                PyObject pyBrain = py.getModule("voice_brain");
                PyObject result = pyBrain.callAttr("understand_command", command);
                Map<PyObject, PyObject> resultMap = result.asMap();

                PyObject typeObj = resultMap.get(py.getBuiltins().get("str").call("type"));
                String type = (typeObj != null) ? typeObj.toString() : "chat";
                PyObject respObj = resultMap.get(py.getBuiltins().get("str").call("response"));
                String response = (respObj != null) ? respObj.toString() : "I am ready to help.";

                if ("nav".equals(type)) {
                    speak(response);
                    PyObject pageObj = resultMap.get(py.getBuiltins().get("str").call("page"));
                    String page = (pageObj != null) ? pageObj.toString() : "DashboardActivity";
                    PyObject extraObj = resultMap.get(py.getBuiltins().get("str").call("extra"));
                    String extra = (extraObj != null) ? extraObj.toString() : null;
                    callback.onActionRequired("navigate", page, extra);
                } else if ("action".equals(type)) {
                    speak(response);
                    PyObject actionObj = resultMap.get(py.getBuiltins().get("str").call("action"));
                    String action = (actionObj != null) ? actionObj.toString() : "stop_voice";
                    callback.onActionRequired(action, null, null);
                } else {
                    // Educational question or Chat: Query voice_assistant_engine for curriculum answers
                    boolean answered = false;
                    try {
                        PyObject engine = py.getModule("voice_assistant_engine");
                        String jsonResult = engine.callAttr("process_voice_input", command).toString();
                        org.json.JSONObject jsonAnswer = new org.json.JSONObject(jsonResult);
                        String answerType = jsonAnswer.optString("type", "chat");
                        String answerContent = jsonAnswer.optString("content", "");

                        if ("answer".equals(answerType) && !answerContent.isEmpty()) {
                            speak(answerContent);
                            callback.onActionRequired("answer", null, answerContent);
                            answered = true;
                        } else if ("command".equals(answerType) && !answerContent.isEmpty()) {
                            callback.onActionRequired("command", answerContent, null);
                            answered = true;
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Offline study engine lookup error", ex);
                    }

                    if (!answered) {
                        speak(response);
                        callback.onActionRequired("chat", null, response);
                    }
                }
            } else {
                fallbackCommandProcessing(command.toLowerCase());
            }
        } catch (Exception e) {
            Log.e(TAG, "Python command processing failed", e);
            fallbackCommandProcessing(command.toLowerCase());
        }
    }

    private void fallbackCommandProcessing(String command) {
        if (command.contains("open physics")) {
            speak("Navigating to Physics Library.");
            callback.onActionRequired("navigate", "SubjectActivity", "Physics");
        } else if (command.contains("open biology")) {
            speak("Navigating to Biology Library.");
            callback.onActionRequired("navigate", "SubjectActivity", "Biology");
        } else if (command.contains("dashboard") || command.contains("home")) {
            speak("Going to Dashboard.");
            callback.onActionRequired("navigate", "DashboardActivity", null);
        } else {
            speak("I heard " + command + ". Please try again.");
        }
    }

    public void speak(String text) {
        if (isTtsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AceGradeVoice");
        }
    }

    public void stop() {
        if (tts != null) tts.stop();
    }

    public void startListening() {
        if (speechRecognizer == null) {
            Toast.makeText(context, "Voice recognition not supported on this device.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(intent);
    }

    public boolean isTtsReady() {
        return isTtsReady;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setPitch(1.0f);
            tts.setSpeechRate(0.9f);
            isTtsReady = true;
            
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override public void onDone(String utteranceId) { if (callback != null) callback.onSpeechFinished(); }
                @Override public void onError(String utteranceId) {}
            });
        }
    }

    public void shutdown() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) speechRecognizer.destroy();
    }
}
