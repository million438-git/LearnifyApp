package com.million.learnify;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * WelcomeActivity - Full Onboarding Experience for Learnify.
 * Features full-bleed dynamic gradient hero, static 32dp rounded white bottom sheet,
 * 4-slide ViewPager2 with parallax PageTransformer, gently floating illustrations,
 * drifting background orbs, animated stretch dots, robust cutout/status-bar insets,
 * and strict session routing preservation.
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";

    // Static slide specification
    private static final SlideItem[] SLIDES = new SlideItem[]{
            new SlideItem(
                    "Learn anything, your way",
                    "All your subjects, notes, lessons and exams together in one simple place.",
                    R.drawable.ic_onb_learn,
                    0xFF4F46E5, // Indigo
                    0xFF7C3AED  // Violet
            ),
            new SlideItem(
                    "Your personal AI tutor",
                    "Ask any question in any subject and get clear, step-by-step help anytime.",
                    R.drawable.ic_onb_ai,
                    0xFF2563EB, // Blue
                    0xFF06B6D4  // Cyan
            ),
            new SlideItem(
                    "Practice. Test. Improve.",
                    "Quizzes, exams, flashcards and daily challenges to lock in what you learn.",
                    R.drawable.ic_onb_practice,
                    0xFF7C3AED, // Violet
                    0xFFEC4899  // Pink
            ),
            new SlideItem(
                    "Learn anywhere, even offline",
                    "Download materials and keep studying without internet. Track your progress as you grow.",
                    R.drawable.ic_onb_offline,
                    0xFF059669, // Emerald
                    0xFF14B8A6  // Teal
            )
    };

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private final List<ObjectAnimator> runningAnimators = new ArrayList<>();

    // Views
    private View vBackgroundGradient;
    private ImageView ivOrb1;
    private ImageView ivOrb2;
    private View headerContainer;
    private View vWhiteSheet;
    private ViewPager2 viewPager;
    private LinearLayout llControlsContainer;
    private View[] dotViews;
    private TextView tvSkip;
    private MaterialButton btnGetStarted;
    private TextView btnSignIn;
    private MaterialButton btnPortalHubTop;

    private int currentSlideIndex = 0;
    private int topHeaderHeight = 0;
    private int navBarBottomInset = 0;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Install AndroidX SplashScreen API before super.onCreate
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 2. Set up Window Edge-to-Edge & System Bars Appearance
        configureEdgeToEdgeWindow();

        // 3. Inflate Layout with Safe Fallback
        try {
            setContentView(R.layout.activity_welcome);
            initViews();
            setupViewPager();
            setupControls();
            startAmbientOrbDrift();
            playEntranceChoreography();
        } catch (Exception e) {
            Log.e(TAG, "Layout inflation failed in WelcomeActivity", e);
            navigateToMain();
            return;
        }

        // 4. Strictly preserve auto-login session routing after 1200ms
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndProceed, 1200);
    }

    /**
     * Edge-to-edge configuration: transparent status bar, white icons on gradient hero,
     * and dark navigation icons on bottom sheet.
     */
    private void configureEdgeToEdgeWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false); // White status-bar icons on top gradient
            controller.setAppearanceLightNavigationBars(true); // Dark navigation-bar icons on white sheet
        }
    }

    private void initViews() {
        vBackgroundGradient = findViewById(R.id.vBackgroundGradient);
        ivOrb1 = findViewById(R.id.ivOrb1);
        ivOrb2 = findViewById(R.id.ivOrb2);
        headerContainer = findViewById(R.id.headerContainer);
        vWhiteSheet = findViewById(R.id.vWhiteSheet);
        viewPager = findViewById(R.id.viewPager);
        llControlsContainer = findViewById(R.id.llControlsContainer);

        dotViews = new View[]{
                findViewById(R.id.dot0),
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        tvSkip = findViewById(R.id.tvSkip);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnPortalHubTop = findViewById(R.id.btnPortalHubTop);

        // Calculate initial fallback header height
        int initialStatusInset = getStatusBarHeightFallback();
        int initialHeaderPadding = initialStatusInset + dpToPx(10);
        topHeaderHeight = initialHeaderPadding + dpToPx(44);

        if (headerContainer != null) {
            headerContainer.setPadding(
                    headerContainer.getPaddingLeft(),
                    initialHeaderPadding,
                    headerContainer.getPaddingRight(),
                    dpToPx(10)
            );
        }

        // Apply Window Insets cleanly (respecting status bar, display cutout notch, and navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootWelcomeLayout), (v, windowInsets) -> {
            Insets statusInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()
            );
            Insets navInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
            );

            int statusBarInset = Math.max(statusInsets.top, getStatusBarHeightFallback());
            navBarBottomInset = navInsets.bottom;

            int headerTopPadding = statusBarInset + dpToPx(10);
            if (headerContainer != null) {
                headerContainer.setPadding(
                        headerContainer.getPaddingLeft(),
                        headerTopPadding,
                        headerContainer.getPaddingRight(),
                        dpToPx(10)
                );
            }

            topHeaderHeight = headerTopPadding + dpToPx(44);

            if (onboardingAdapter != null) {
                onboardingAdapter.setHeaderAndSpacerInsets(topHeaderHeight, dpToPx(148) + navBarBottomInset);
            }

            if (llControlsContainer != null) {
                llControlsContainer.setPadding(
                        llControlsContainer.getPaddingLeft(),
                        llControlsContainer.getPaddingTop(),
                        llControlsContainer.getPaddingRight(),
                        dpToPx(24) + navBarBottomInset
                );
            }

            return windowInsets;
        });

        // Initialize background and button gradients with slide 0 colors
        applyGradients(SLIDES[0].startColor, SLIDES[0].endColor);
    }

    private int getStatusBarHeightFallback() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return dpToPx(28);
    }

    private void setupViewPager() {
        onboardingAdapter = new OnboardingAdapter(SLIDES, topHeaderHeight, dpToPx(148) + navBarBottomInset);
        viewPager.setAdapter(onboardingAdapter);
        viewPager.setOffscreenPageLimit(3);

        // Parallax PageTransformer
        viewPager.setPageTransformer((page, position) -> {
            View illustration = page.findViewById(R.id.flIllustrationContainer);
            View textGroup = page.findViewById(R.id.llTextContainer);

            if (illustration != null) {
                if (position >= -1f && position <= 1f) {
                    float absPos = Math.abs(position);
                    // Illustration moves slower than the page (parallax)
                    illustration.setTranslationX(-position * (page.getWidth() * 0.40f));
                    // Shrinks to about 75%
                    float scale = 0.75f + (1f - absPos) * 0.25f;
                    illustration.setScaleX(scale);
                    illustration.setScaleY(scale);
                    // Fades
                    illustration.setAlpha(1f - absPos);
                } else {
                    illustration.setAlpha(0f);
                }
            }

            if (textGroup != null) {
                if (position >= -1f && position <= 1f) {
                    float absPos = Math.abs(position);
                    // Text fades faster than the illustration
                    float textAlpha = Math.max(0f, 1f - (absPos * 1.8f));
                    textGroup.setAlpha(textAlpha);
                    textGroup.setTranslationX(position * (page.getWidth() * 0.15f));
                } else {
                    textGroup.setAlpha(0f);
                }
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                blendGradients(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentSlideIndex = position;
                updateDotIndicators(position);
                updateControlsForPage(position);
            }
        });
    }

    /**
     * Blends the background hero gradient and button gradient dynamically between slides.
     */
    private void blendGradients(int position, float positionOffset) {
        if (position >= SLIDES.length - 1) {
            applyGradients(SLIDES[SLIDES.length - 1].startColor, SLIDES[SLIDES.length - 1].endColor);
            return;
        }

        SlideItem current = SLIDES[position];
        SlideItem next = SLIDES[position + 1];

        int blendedStart = (Integer) argbEvaluator.evaluate(positionOffset, current.startColor, next.startColor);
        int blendedEnd = (Integer) argbEvaluator.evaluate(positionOffset, current.endColor, next.endColor);

        applyGradients(blendedStart, blendedEnd);
    }

    private void applyGradients(int startColor, int endColor) {
        if (vBackgroundGradient != null) {
            GradientDrawable bgGradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{startColor, endColor}
            );
            vBackgroundGradient.setBackground(bgGradient);
        }

        if (btnGetStarted != null) {
            GradientDrawable btnGradient = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{startColor, endColor}
            );
            btnGradient.setCornerRadius(dpToPx(28));
            btnGetStarted.setBackground(btnGradient);
        }
    }

    private void setupControls() {
        // Dot 0 initially selected (24dp pill)
        updateDotIndicators(0);

        // Skip Button -> Jumps to last slide
        if (tvSkip != null) {
            tvSkip.setOnClickListener(v -> animateButtonAndExecute(v, () -> {
                if (viewPager != null) {
                    viewPager.setCurrentItem(SLIDES.length - 1, true);
                }
            }));
        }

        // Main Action Button ("Next" on slides 1-3, "Get Started" on slide 4)
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> animateButtonAndExecute(v, () -> {
                if (currentSlideIndex < SLIDES.length - 1) {
                    viewPager.setCurrentItem(currentSlideIndex + 1, true);
                } else {
                    navigateToRegister();
                }
            }));
        }

        // Format "Already have an account?  Sign In" with bold Indigo "Sign In"
        if (btnSignIn != null) {
            String fullText = "Already have an account?  Sign In";
            SpannableString spannable = new SpannableString(fullText);
            int start = fullText.indexOf("Sign In");
            if (start != -1) {
                int end = start + "Sign In".length();
                spannable.setSpan(new ForegroundColorSpan(0xFF4F46E5), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            btnSignIn.setText(spannable);
            btnSignIn.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToLogin));
        }

        // Portals Button in Header
        if (btnPortalHubTop != null) {
            btnPortalHubTop.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToMain));
        }
    }

    /**
     * Stretches the selected dot from 8dp to 24dp pill and colors it with slide accent.
     * Contracts other dots back to 8dp light gray circles.
     */
    private void updateDotIndicators(int selectedPosition) {
        if (dotViews == null) return;

        int activeColor = SLIDES[selectedPosition].startColor;
        int inactiveColor = 0xFFCBD5E1;

        for (int i = 0; i < dotViews.length; i++) {
            final View dot = dotViews[i];
            boolean isSelected = (i == selectedPosition);
            int targetWidth = dpToPx(isSelected ? 24 : 8);
            int targetColor = isSelected ? activeColor : inactiveColor;

            // Animate width
            int currentWidth = dot.getLayoutParams().width;
            if (currentWidth != targetWidth) {
                ValueAnimator widthAnimator = ValueAnimator.ofInt(currentWidth, targetWidth);
                widthAnimator.setDuration(250);
                widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                widthAnimator.addUpdateListener(animation -> {
                    ViewGroup.LayoutParams lp = dot.getLayoutParams();
                    lp.width = (Integer) animation.getAnimatedValue();
                    dot.setLayoutParams(lp);
                });
                widthAnimator.start();
            }

            // Set shape & color
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(dpToPx(4));
            shape.setColor(targetColor);
            dot.setBackground(shape);
        }
    }

    /**
     * Updates button labels and skip visibility based on the active page.
     */
    private void updateControlsForPage(int position) {
        boolean isLastSlide = (position == SLIDES.length - 1);

        // Skip Button: Fades out and becomes unclickable on the last slide
        if (tvSkip != null) {
            tvSkip.animate()
                    .alpha(isLastSlide ? 0f : 1f)
                    .setDuration(200)
                    .withEndAction(() -> tvSkip.setEnabled(!isLastSlide))
                    .start();
        }

        // Main Button: "Next" on slides 1-3, "Get Started" on slide 4 with bounce animation
        if (btnGetStarted != null) {
            if (isLastSlide) {
                btnGetStarted.setText("Get Started");
                btnGetStarted.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(160)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .withEndAction(() -> btnGetStarted.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                        .start();
            } else {
                btnGetStarted.setText("Next");
                btnGetStarted.setScaleX(1.0f);
                btnGetStarted.setScaleY(1.0f);
            }
        }
    }

    /**
     * Ambient floating animation for background orbs.
     */
    private void startAmbientOrbDrift() {
        if (ivOrb1 != null) {
            ObjectAnimator orb1Anim = ObjectAnimator.ofFloat(ivOrb1, "translationY", 0f, dpToPx(24), 0f);
            orb1Anim.setDuration(5000);
            orb1Anim.setRepeatCount(ValueAnimator.INFINITE);
            orb1Anim.setInterpolator(new AccelerateDecelerateInterpolator());
            orb1Anim.start();
            runningAnimators.add(orb1Anim);
        }

        if (ivOrb2 != null) {
            ObjectAnimator orb2Anim = ObjectAnimator.ofFloat(ivOrb2, "translationY", 0f, -dpToPx(30), 0f);
            orb2Anim.setDuration(6500);
            orb2Anim.setRepeatCount(ValueAnimator.INFINITE);
            orb2Anim.setInterpolator(new AccelerateDecelerateInterpolator());
            orb2Anim.start();
            runningAnimators.add(orb2Anim);
        }
    }

    /**
     * Tasteful staggered entrance choreography on first launch.
     */
    private void playEntranceChoreography() {
        if (headerContainer != null) {
            headerContainer.setAlpha(0f);
            headerContainer.setTranslationY(-dpToPx(10));
            headerContainer.animate().alpha(1f).translationY(0f).setDuration(500).start();
        }

        if (viewPager != null) {
            viewPager.setAlpha(0f);
            viewPager.animate().alpha(1f).setDuration(600).setStartDelay(100).start();
        }

        if (llControlsContainer != null) {
            llControlsContainer.setAlpha(0f);
            llControlsContainer.setTranslationY(dpToPx(30));
            llControlsContainer.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start();
        }
    }

    /**
     * Tactile touch feedback animation: 0.96 scale down, then execute action with fade transition.
     */
    private void animateButtonAndExecute(View v, Runnable action) {
        if (v == null) return;
        v.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(90)
                .withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(90).start();
                    if (action != null) {
                        action.run();
                    }
                })
                .start();
    }

    /**
     * Strictly preserves existing session verification and routing:
     * - Authenticated & verified student -> Student Dashboard
     * - Authenticated unverified student -> Email Verification Portal
     * - Authenticated administrator -> Admin Dashboard
     * - Unauthenticated user -> Remains on Welcome onboarding screen
     */
    private void checkSessionAndProceed() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        try {
            SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
            if (session.getBoolean("isLoggedIn", false)) {
                String type = session.getString("user_type", "STUDENT");
                String username = session.getString("username", "");

                if ("STUDENT".equals(type) && !username.isEmpty()) {
                    if (repository != null && !repository.isUserVerified(username)) {
                        navigateToVerification(username);
                        return;
                    }
                    navigateToDashboard(false);
                } else if ("ADMIN".equals(type)) {
                    navigateToDashboard(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Session verification check failed", e);
        }
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, StudentLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToVerification(String username) {
        Intent intent = new Intent(this, EmailVerificationActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("EMAIL", username);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard(boolean isAdmin) {
        Intent intent = new Intent(this, isAdmin ? AdminDashboardActivity.class : DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // Cancel all running ambient loop animators to prevent memory leaks
        for (ObjectAnimator anim : runningAnimators) {
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        runningAnimators.clear();
        super.onDestroy();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // =========================================================================
    // SLIDE DATA MODEL & ADAPTER
    // =========================================================================

    private static final class SlideItem {
        final String title;
        final String description;
        final int illustrationRes;
        final int startColor;
        final int endColor;

        SlideItem(String title, String description, int illustrationRes, int startColor, int endColor) {
            this.title = title;
            this.description = description;
            this.illustrationRes = illustrationRes;
            this.startColor = startColor;
            this.endColor = endColor;
        }
    }

    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {

        private final SlideItem[] items;
        private int topHeaderHeight;
        private int bottomSpacerHeight;

        OnboardingAdapter(SlideItem[] items, int topHeaderHeight, int bottomSpacerHeight) {
            this.items = items;
            this.topHeaderHeight = topHeaderHeight;
            this.bottomSpacerHeight = bottomSpacerHeight;
        }

        void setHeaderAndSpacerInsets(int headerHeight, int spacerHeight) {
            this.topHeaderHeight = headerHeight;
            this.bottomSpacerHeight = spacerHeight;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new SlideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            SlideItem item = items[position];
            holder.bind(item, topHeaderHeight, bottomSpacerHeight);
        }

        @Override
        public void onViewRecycled(@NonNull SlideViewHolder holder) {
            super.onViewRecycled(holder);
            holder.stopFloatAnimation();
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull SlideViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.stopFloatAnimation();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull SlideViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            holder.startFloatAnimation();
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        static class SlideViewHolder extends RecyclerView.ViewHolder {
            final Guideline guidelineTopHeader;
            final ImageView ivIllustration;
            final TextView tvTitle;
            final TextView tvDescription;
            final View vPageSpacerBottom;
            private ObjectAnimator floatAnimator;

            SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                guidelineTopHeader = itemView.findViewById(R.id.guidelineTopHeader);
                ivIllustration = itemView.findViewById(R.id.ivIllustration);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                vPageSpacerBottom = itemView.findViewById(R.id.vPageSpacerBottom);
            }

            void bind(SlideItem item, int headerHeight, int spacerHeight) {
                if (tvTitle != null) {
                    tvTitle.setText(item.title);
                }
                if (tvDescription != null) {
                    tvDescription.setText(item.description);
                }
                if (ivIllustration != null) {
                    ivIllustration.setImageResource(item.illustrationRes);
                    ivIllustration.setContentDescription(item.title);
                }

                // Adjust top guideline to guarantee NO overlap with Header Bar
                if (guidelineTopHeader != null && headerHeight > 0) {
                    guidelineTopHeader.setGuidelineBegin(headerHeight);
                }

                // Adjust bottom spacer to guarantee NO overlap with Controls Container
                if (vPageSpacerBottom != null && spacerHeight > 0) {
                    ViewGroup.LayoutParams lp = vPageSpacerBottom.getLayoutParams();
                    if (lp.height != spacerHeight) {
                        lp.height = spacerHeight;
                        vPageSpacerBottom.setLayoutParams(lp);
                    }
                }
                startFloatAnimation();
            }

            void startFloatAnimation() {
                stopFloatAnimation();
                if (ivIllustration != null) {
                    float offset = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8,
                            ivIllustration.getResources().getDisplayMetrics()
                    );
                    floatAnimator = ObjectAnimator.ofFloat(ivIllustration, "translationY", -offset, offset);
                    floatAnimator.setDuration(3000);
                    floatAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    floatAnimator.start();
                }
            }

            void stopFloatAnimation() {
                if (floatAnimator != null) {
                    floatAnimator.cancel();
                    floatAnimator = null;
                }
                if (ivIllustration != null) {
                    ivIllustration.setTranslationY(0f);
                }
            }
        }
    }
}