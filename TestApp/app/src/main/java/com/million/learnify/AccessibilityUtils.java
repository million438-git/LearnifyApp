package com.million.learnify;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AccessibilityUtils - Reusable accessibility helpers for TalkBack semantics,
 * programmatic announcements, focus management, minimum touch targets, and
 * phonetic math/scientific translation for spoken feedback.
 */
public final class AccessibilityUtils {

    private static final int DEFAULT_MIN_TOUCH_TARGET_DP = 48;

    private AccessibilityUtils() {
        // Utility class
    }

    /**
     * Announces text politely using Android's native announcement event.
     */
    public static void announceForAccessibility(@Nullable View view, @Nullable CharSequence text) {
        if (view == null || text == null || text.length() == 0) return;
        view.announceForAccessibility(text);
    }

    /**
     * Announces critical information assertively (e.g. time warnings, exam errors).
     */
    public static void announceAssertive(@Nullable View view, @Nullable CharSequence text) {
        if (view == null || text == null || text.length() == 0) return;
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
        event.getText().add(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            event.setContentChangeTypes(AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT);
        }
        AccessibilityManager am = (AccessibilityManager) view.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null && am.isEnabled()) {
            am.sendAccessibilityEvent(event);
        } else {
            view.announceForAccessibility(text);
        }
    }

    /**
     * Sets a semantic heading on a View so screen readers can navigate by section.
     */
    public static void setAsHeading(@Nullable View view) {
        setAsHeading(view, true);
    }

    public static void setAsHeading(@Nullable View view, boolean isHeading) {
        if (view == null) return;
        ViewCompat.setAccessibilityHeading(view, isHeading);
    }

    /**
     * Sets an accessible label and an optional role description (e.g. "Tab", "Button", "Card").
     */
    public static void setAccessibilityLabel(@Nullable View view, @Nullable CharSequence label, @Nullable final CharSequence roleDescription) {
        if (view == null) return;
        if (label != null) {
            view.setContentDescription(label);
        }
        if (roleDescription != null && roleDescription.length() > 0) {
            ViewCompat.setAccessibilityDelegate(view, new AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setRoleDescription(roleDescription);
                }
            });
        }
    }

    /**
     * Configures a view with selection state and state description (e.g. "Selected" vs "Not selected").
     */
    public static void setSelectedState(@Nullable View view, final boolean isSelected, @Nullable final CharSequence stateDescription) {
        if (view == null) return;
        view.setSelected(isSelected);
        ViewCompat.setAccessibilityDelegate(view, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setSelected(isSelected);
                if (stateDescription != null) {
                    info.setStateDescription(stateDescription);
                }
            }
        });
    }

    /**
     * Marks a view as an expandable element (accordion, dropdown) and reports its expanded state.
     */
    public static void setExpandedState(@Nullable View view, final boolean isExpanded) {
        if (view == null) return;
        ViewCompat.setAccessibilityDelegate(view, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.addAction(isExpanded ? AccessibilityNodeInfoCompat.ACTION_COLLAPSE : AccessibilityNodeInfoCompat.ACTION_EXPAND);
                info.setStateDescription(isExpanded ? "Expanded" : "Collapsed");
            }
        });
    }

    /**
     * Requests focus for accessibility so TalkBack reads the target element immediately.
     */
    public static void focusForAccessibility(@Nullable final View view) {
        if (view == null) return;
        view.post(() -> {
            view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            view.requestFocus();
        });
    }

    /**
     * Checks if TalkBack or another spoken accessibility service is currently active.
     */
    public static boolean isTalkBackActive(@Nullable Context context) {
        if (context == null) return false;
        return InclusiveModeManager.getInstance(context).isSystemScreenReaderActive();
    }

    /**
     * Checks whether reduced motion is requested either via app preference or system setting.
     */
    public static boolean isReducedMotion(@Nullable Context context) {
        if (context == null) return false;
        if (InclusiveModeManager.getInstance(context).isReducedMotionEnabled()) {
            return true;
        }
        try {
            float animScale = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    1.0f
            );
            return animScale == 0.0f;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ensures an interactive view meets the minimum recommended touch target (48dp).
     */
    public static void ensureMinTouchTarget(@Nullable View view) {
        ensureMinTouchTarget(view, DEFAULT_MIN_TOUCH_TARGET_DP);
    }

    public static void ensureMinTouchTarget(@Nullable View view, int minDp) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        int minPx = (int) (minDp * density + 0.5f);
        view.setMinimumWidth(Math.max(view.getMinimumWidth(), minPx));
        view.setMinimumHeight(Math.max(view.getMinimumHeight(), minPx));
    }

    /**
     * Converts mathematical equations, chemical formulas, and scientific notations
     * into clear, phonetically readable speech for students using screen readers or TTS.
     */
    public static String cleanMathForSpeech(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String speech = text;

        // Remove LaTeX math delimiter tokens $...$
        speech = speech.replace("$$", " ").replace("$", " ");

        // Scientific constants and Greek letters
        speech = speech.replaceAll("\\\\pi\\b", "pi")
                       .replaceAll("\\\\theta\\b", "theta")
                       .replaceAll("\\\\alpha\\b", "alpha")
                       .replaceAll("\\\\beta\\b", "beta")
                       .replaceAll("\\\\lambda\\b", "lambda")
                       .replaceAll("\\\\Delta\\b", "Delta")
                       .replaceAll("\\\\omega\\b", "omega")
                       .replaceAll("\\\\mu\\b", "micro");

        // LaTeX fractions: \frac{a}{b} -> "fraction a over b"
        Pattern fracPattern = Pattern.compile("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}");
        Matcher fracMatcher = fracPattern.matcher(speech);
        StringBuffer fracSb = new StringBuffer();
        while (fracMatcher.find()) {
            fracMatcher.appendReplacement(fracSb, "fraction " + fracMatcher.group(1) + " over " + fracMatcher.group(2));
        }
        fracMatcher.appendTail(fracSb);
        speech = fracSb.toString();

        // LaTeX square root: \sqrt{x} -> "square root of x"
        Pattern sqrtPattern = Pattern.compile("\\\\sqrt\\{([^}]+)\\}");
        Matcher sqrtMatcher = sqrtPattern.matcher(speech);
        StringBuffer sqrtSb = new StringBuffer();
        while (sqrtMatcher.find()) {
            sqrtMatcher.appendReplacement(sqrtSb, "square root of " + sqrtMatcher.group(1));
        }
        sqrtMatcher.appendTail(sqrtSb);
        speech = sqrtSb.toString();

        // Common chemical formulas
        speech = speech.replaceAll("\\bH2O\\b|\\bH₂O\\b", "H 2 O, water")
                       .replaceAll("\\bCO2\\b|\\bCO₂\\b", "C O 2, carbon dioxide")
                       .replaceAll("\\bO2\\b|\\bO₂\\b", "O 2, oxygen gas")
                       .replaceAll("\\bN2\\b|\\bN₂\\b", "N 2, nitrogen gas")
                       .replaceAll("\\bNaCl\\b", "N A C L, sodium chloride")
                       .replaceAll("\\bHCl\\b", "H C L, hydrochloric acid")
                       .replaceAll("\\bH2SO4\\b|\\bH₂SO₄\\b", "H 2 S O 4, sulfuric acid")
                       .replaceAll("\\bCH4\\b|\\bCH₄\\b", "C H 4, methane")
                       .replaceAll("\\bC6H12O6\\b|\\bC₆H₁₂O₆\\b", "C 6 H 12 O 6, glucose");

        // Scientific units
        speech = speech.replaceAll("(?i)\\bm/s\\^2\\b|\\bm/s²\\b", " meters per second squared")
                       .replaceAll("(?i)\\bm/s\\b", " meters per second")
                       .replaceAll("(?i)\\bkm/h\\b", " kilometers per hour")
                       .replaceAll("(?i)\\bkg\\b", " kilograms")
                       .replaceAll("(?i)\\bcm\\b", " centimeters")
                       .replaceAll("(?i)\\bmm\\b", " millimeters")
                       .replaceAll("(?i)\\bmol/L\\b", " moles per liter")
                       .replaceAll("(?i)\\bkJ/mol\\b", " kilojoules per mole")
                       .replaceAll("(?i)\\bHz\\b", " hertz")
                       .replaceAll("(?i)\\bkHz\\b", " kilohertz")
                       .replaceAll("(?i)\\bMHz\\b", " megahertz");

        // Mathematical operators & superscripts
        speech = speech.replaceAll("\\\\times\\b|×", " times ")
                       .replaceAll("\\\\div\\b|÷", " divided by ")
                       .replaceAll("\\\\pm\\b|±", " plus or minus ")
                       .replaceAll("\\\\approx\\b|≈", " approximately equals ")
                       .replaceAll("\\\\neq\\b|≠", " does not equal ")
                       .replaceAll("\\\\leq\\b|≤", " is less than or equal to ")
                       .replaceAll("\\\\geq\\b|≥", " is greater than or equal to ")
                       .replaceAll("\\\\rightarrow\\b|→", " yields ")
                       .replaceAll("\\^2\\b|²", " squared")
                       .replaceAll("\\^3\\b|³", " cubed")
                       .replaceAll("\\^([0-9]+)", " to the power of $1");

        // Strip remaining LaTeX backslashes
        speech = speech.replaceAll("\\\\[a-zA-Z]+", " ");

        // Clean extra whitespace
        speech = speech.replaceAll("\\s{2,}", " ").trim();

        return speech;
    }

    /**
     * Cleans HTML markup into plain spoken text, stripping script, style, and tag brackets.
     */
    public static String cleanHtmlForSpeech(@Nullable String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        // Remove style and script blocks
        String text = html.replaceAll("(?s)<style.*?>.*?</style>", "")
                          .replaceAll("(?s)<script.*?>.*?</script>", "")
                          .replaceAll("<br\\s*/?>", "\n")
                          .replaceAll("</p>", "\n\n")
                          .replaceAll("</li>", "\n")
                          .replaceAll("<.*?>", "")
                          .replace("&nbsp;", " ")
                          .replace("&amp;", "&")
                          .replace("&lt;", "<")
                          .replace("&gt;", ">")
                          .replace("&quot;", "\"");

        return cleanMathForSpeech(text);
    }
}
