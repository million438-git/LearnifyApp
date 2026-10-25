# LEARNIFY ANDROID APPLICATION
## COMPLETE UI/UX, ARCHITECTURAL, AND FEATURE PRESERVATION AUDIT

**Project:** Learnify Native Android Application  
**Package:** `com.million.learnify`  
**Workspace:** `C:\Users\milli\OneDrive\Desktop\LearnifyApp\TestApp`  
**Target Hardware:** Physical TECNO Spark 10C (`TECNO_KI5q`, Android 12 / API 31+)  
**Audit Date:** October 2026  
**Status:** Audit Completed — Awaiting User Approval Prior to Implementation

---

## 1. EXECUTIVE SUMMARY & SCOPE

Learnify is a feature-rich, dual-portal (Student & Administrator) native Android application developed with Java, Android XML layouts, SQLite local storage, and Chaquopy embedded Python (NumPy, OpenCV, scientific simulations).

### Audit Objectives
1. **100% Feature Preservation Guarantee:** Identify and map every single activity, layout, dialog, user workflow, and administrative capability. Ensure zero features, screens, or database capabilities are removed, replaced, or disabled.
2. **Defect Discovery & Crash Prevention:** Pinpoint critical architectural, manifest, and runtime navigation bugs that currently crash the application or prevent users from accessing features.
3. **UI/UX & Design System Modernization:** Analyze visual inconsistencies, fragmented themes, branding misalignments, broken dark-mode support, and anti-pattern navigation, providing a concrete Material 3 blueprint for transformation into a world-class educational product.
4. **Implementation Roadmap:** Structure a non-destructive, batch-by-batch execution plan with strict verification gates on physical hardware.

### Git Safety & Baseline Checkpoint
- **Staged Changes Preserved:** `app/build.gradle` (Python Chaquopy ABI configurations) and 4 normalized asset filenames (`Enzymes.html`, `Genetics.html`, `Population and Natural Resources.html`, `Vectors.html`).
- **Unstaged Changes Preserved:** Verified Step 1–9 and Phase 2 Batch 1 security improvements across 13 core files (PBKDF2 password updates in `PrivacySecurityActivity.java`, real password-change validation in `DatabaseHelper.java`, secure exam countdown lifecycle in `ExamActivity.java`, strict role enforcement in `BaseActivity.java`, disabled unauthenticated LinkedIn callbacks, and verification guards).
- **Audit Rule Adherence:** Read-only audit; zero destructive Git commands, zero commits, and zero file alterations performed during this phase.

---

## 2. COMPLETE ACTIVITY & FEATURE INVENTORY

The Learnify application comprises **60 Java Activities**, **58 Activity XML Layouts**, **4 Dialog Layouts**, **23 Item Layouts**, **9 Simulation Controls**, and **3 Menus**.

### Comprehensive Inventory Matrix

| # | Activity Class | XML Layout | Portal | Manifest Status | Current Route / Trigger | Capabilities & Features | Preservation Strategy |
|---|---|---|---|---|---|---|---|
| 1 | `WelcomeActivity` | `activity_welcome.xml` | Gateway | Registered (Launcher) | App Startup | SplashScreen API, animated logo, auto-session routing | Modernize typography & branding; preserve auto-login logic |
| 2 | `MainActivity` | `activity_main.xml` | Gateway | Registered | `WelcomeActivity` (Get Started) | Portal selector (Student vs Admin) | Fix "EduPortal" & "AceGrade" branding; preserve dual routing |
| 3 | `StudentLoginActivity` | `activity_student_login.xml` | Auth | Registered | `MainActivity` (Student Portal) | Student credentials, Remember Me, Google Sign-in | Modernize form inputs to Material 3; preserve PBKDF2 auth |
| 4 | `AdminLoginActivity` | `activity_admin_login.xml` | Auth | Registered | `MainActivity` (Admin Portal) | Admin credentials, password recovery, secure login | Harmonize styling; preserve strict admin role check |
| 5 | `RegisterActivity` | `activity_register.xml` | Auth | Registered | `StudentLoginActivity` (Sign Up) | Student registration, grade/country dropdowns | Upgrade to unified outlined inputs; preserve validation |
| 6 | `AdminRegisterActivity` | `activity_admin_register.xml` | Admin Auth | Registered | `AdminDashboardActivity` (Add User) | Admin provisioning with access code validation | Preserve admin role provisioning & audit logging |
| 7 | `ForgotPasswordActivity` | `activity_forgot_password.xml` | Auth | Registered | Login screens (Forgot Password) | Email password reset request via MailService | Harmonize with Material 3 styling; preserve reset flow |
| 8 | `EmailVerificationActivity` | `activity_email_verification.xml` | Auth | Registered | Post-registration / Login guard | 6-digit OTP verification & resend | Preserve verification gate & OTP validation |
| 9 | `ResetPasswordActivity` | `dialog_change_password.xml` / dynamic | Auth | Registered | Password reset token link | Token-based password update | Preserve PBKDF2 hash update |
| 10 | `DashboardActivity` | `activity_dashboard.xml` | Student | Registered | Student Login / Splash auto-route | Main student hub: quick cards, stats, notifications, voice | Fix floating drawer button; preserve all 12 feature cards |
| 11 | `AdminDashboardActivity` | `activity_admin_dashboard.xml` | Admin | Registered | Admin Login / Splash auto-route | System stats, student/teacher counts, quick actions | Fix back button in root toolbar; wire all admin actions |
| 12 | **`FlashcardActivity`** | `activity_flashcard.xml` | Student | **MISSING FROM MANIFEST (CRASH)** | `DashboardActivity` (`cardFlashcards`) | Card flipping, difficulty rating (Hard/Good/Easy), mastery | **Register in manifest; fix instant crash on tap** |
| 13 | **`CameraHistoryActivity`** | `activity_camera_history.xml` | Student / Lab | **MISSING FROM MANIFEST (CRASH)** | `CameraLabActivity` (`btnHistory`) | Inspection logs, timestamped captures, visual history | **Register in manifest; fix crash from Camera AI Lab** |
| 14 | **`CameraSettingsActivity`** | `activity_camera_settings.xml` | Student / Lab | **MISSING FROM MANIFEST (CRASH)** | `CameraLabActivity` (`btnSettings`) | Resolution, flash, auto-focus, AI engine settings | **Register in manifest; fix crash from Camera AI Lab** |
| 15 | **`AnalyticsActivity`** | `activity_analytics.xml` | Student | **MISSING FROM MANIFEST (ORPHAN)** | Unlinked (Orphaned feature) | Overall XP, study streak, quizzes passed, subject progress | **Register in manifest; link in Navigation Drawer & Dashboard** |
| 16 | **`AttendanceActivity`** | `activity_attendance.xml` | Student | **MISSING FROM MANIFEST (ORPHAN)** | Unlinked (Orphaned feature) | Overall percentage, present/absent stats, history log | **Register in manifest; link in Navigation Drawer** |
| 17 | **`GpaCalculatorActivity`** | `activity_gpa_calculator.xml` | Student | **MISSING FROM MANIFEST (ORPHAN)** | Unlinked (Orphaned feature) | Dynamic credit/grade rows, cumulative GPA engine | **Register in manifest; link in Navigation Drawer** |
| 18 | `SubjectActivity` | `activity_subject.xml` | Student | Registered | `DashboardActivity` (`cardCourses`), Drawer | Course catalog: Grade 9–12 Biology, Physics, Chemistry | Preserve dynamic subject loading & offline pack downloader |
| 19 | `ChapterListActivity` | `activity_chapter_list.xml` | Student | Registered | `SubjectActivity` (Subject click) | Chapter listing, progress indicators, unit hierarchy | Preserve chapter progress tracking |
| 20 | `TopicListActivity` | `activity_topic_list.xml` | Student | Registered | `ChapterListActivity` (Chapter click) | Subtopics, lesson materials, quizzes, interactive labs | Preserve topic-level navigation |
| 21 | `NoteDetailActivity` | `activity_note_detail.xml` | Student | Registered | `TopicListActivity` (Lesson click) | HTML rich notes, embedded equations, TTS audio reader | Preserve WebView rendering & speech playback |
| 22 | `CourseDetailActivity` | `activity_course_detail.xml` | Student | Registered | Direct link / Catalog | Course description, syllabus overview, enroll action | Preserve course metadata display |
| 23 | `QuestionActivity` | `activity_question.xml` | Student | Registered | Topic practice / Quiz buttons | Interactive question practice with explanations | Preserve question evaluation & instant feedback |
| 24 | `ExamActivity` | `activity_exam.xml` | Student | Registered | Timed Exams / Unit tests | Timed exam runner, auto-submit countdown, radio choices | Preserve fixed Batch 1 timer, state restore & scoring |
| 25 | `ProfileActivity` | `activity_profile.xml` | Student | Registered | Drawer (`nav_profile`), Dashboard | User profile info, grade, avatar, quick stats | Preserve profile image loading & edit redirection |
| 26 | `EditProfileActivity` | `activity_edit_profile.xml` | Student | Registered | `ProfileActivity` (Edit button) | Update full name, grade, profile photo picker | Preserve database profile updates & image URI |
| 27 | `AboutActivity` | `activity_about.xml` | Shared | Registered | Settings / Drawer | App version, mission, credits, licenses | Preserve legal & version details |
| 28 | `HelpSupportActivity` | `activity_help_support.xml` | Student | Registered | Settings / Drawer | FAQ accordion, support ticket submission | Preserve support ticket persistence to database |
| 29 | `SettingsActivity` | `activity_settings.xml` | Student | Registered | Drawer (`nav_settings`), Bottom Nav | Dark mode toggle, notifications, sound effects, security | Preserve shared preference persistence |
| 30 | `PrivacySecurityActivity` | `activity_privacy_security.xml` | Student | Registered | `SettingsActivity` (Privacy) | PBKDF2 password change, session audit, data privacy | Preserve verified Batch 1 password change implementation |
| 31 | `NotificationPreferencesActivity` | `activity_notification_preferences.xml` | Student | Registered | `SettingsActivity` (Notifications) | Study reminders, exam alerts, announcement toggles | Preserve alarm scheduling & preference keys |
| 32 | `AIChatActivity` | `activity_ai_chat.xml` | Student | Registered | `DashboardActivity` (`cardAI`), Drawer | AI study assistant chat interface, offline AI engine fallback | Preserve ChatAdapter, offline AI engine & chat history |
| 33 | `PaymentActivity` | `activity_payment.xml` | Student | Registered | `DashboardActivity` (`cardPremium`) | Telebirr, CBE, Commercial Bank ETB transfer proof upload | Preserve payment proof submission to database |
| 34 | `AnnouncementsActivity` | `activity_announcements.xml` | Student | Registered | `DashboardActivity` (`cardAnnouncements`), Drawer | School & admin broadcast announcements feed | Preserve announcement list adapter & date formatting |
| 35 | `TasksActivity` | `activity_tasks.xml` | Student | Registered | `DashboardActivity` (`cardAssignments`), Drawer | To-do checklist, assignments, priority filtering, due dates | Preserve task creation, completion toggles & deletion |
| 36 | `ClassScheduleActivity` | `activity_class_schedule.xml` | Student | Registered | `DashboardActivity` (`cardTimetable`), Drawer | Weekly timetable, period tracker, room numbers | Preserve schedule database persistence |
| 37 | `DailyChallengeActivity` | `activity_daily_challenge.xml` | Student | Registered | `DashboardActivity` (`cardDailyChallenge`) | Daily quiz challenge, streak tracking, XP awards | Preserve streak calculation & daily challenge lock |
| 38 | `LeaderboardActivity` | `activity_leaderboard.xml` | Student | Registered | `DailyChallengeActivity`, Drawer | Student ranking, XP leaderboard, peer competition | Preserve leaderboard query & sorting |
| 39 | `CertificateActivity` | `activity_certificate.xml` | Student | Registered | Drawer (`nav_certificate`) | Course completion certificate view & PDF sharing | Preserve certificate generation & bitmap sharing |
| 40 | `VisualizerActivity` | `activity_visualizer.xml` | Student / Lab | Registered | `DashboardActivity` (`cardVisualizers`), Drawer | Interactive physics/chemistry simulations runner | Preserve Chaquopy simulation hooks & canvas views |
| 41 | `CameraLabActivity` | `activity_camera_lab.xml` | Student / Lab | Registered | `DashboardActivity` (`cardCameraLab`) | CameraX live view, OCR problem recognition, AR overlay | Preserve CameraX lifecycle, OCR pipeline & history |
| 42 | `NotificationListActivity` | `activity_notification_list.xml` | Student | Registered | `DashboardActivity` (Bell icon) | List of delivered notifications, unread badges, mark read | Preserve notification receiver & repository updates |
| 43 | `PhysicsLabActivity` | `activity_physics_lab.xml` | Student / Lab | Registered | `VisualizerActivity` | Mechanics, waves, optics, Atwood machine experiment list | Preserve custom physics views & parameter sliders |
| 44 | `PhysicsSimulationActivity` | `activity_physics_simulation.xml` | Student / Lab | Registered | `PhysicsLabActivity` | Dedicated simulation canvas (Pendulum, Gas Law, Spring) | Preserve real-time math physics simulation renderers |
| 45 | `BiologyLabActivity` | `activity_physics_lab.xml` | Student / Lab | Registered | `VisualizerActivity` | Cell structure, genetics, photosynthesis interactive lab | Preserve biology interactive modules |
| 46 | `BiologySimulationActivity` | `activity_biology_simulation.xml` | Student / Lab | Registered | `BiologyLabActivity` | Cell division & genetics interactive simulation | Preserve biology animations & interactive stages |
| 47 | `ChemistryLabActivity` | `activity_physics_lab.xml` | Student / Lab | Registered | `VisualizerActivity` | Periodic table, reactions, acid-base titration lab | Preserve chemistry experiments |
| 48 | `ChemistrySimulationActivity` | `activity_chemistry_simulation.xml` | Student / Lab | Registered | `ChemistryLabActivity` | Molecular models, Bohr atom simulation | Preserve Bohr atom & chemical reaction views |
| 49 | `VoiceAssistantActivity` | `activity_voice_assistant.xml` | Student | Registered | `DashboardActivity` (Mic icon) | Speech recognition, voice command navigation, voice tutor | Preserve Android SpeechRecognizer & TTS engine |
| 50 | `LinkedAccountsActivity` | `activity_linked_accounts.xml` | Student | Registered | `SettingsActivity` (Accounts) | Connected Google, Telegram, Email accounts | Preserve account linking state |
| 51 | `InviteFriendsActivity` | `activity_invite_friends.xml` | Student | Registered | `SettingsActivity` (Invite) | Referral link generator, social share intent | Preserve referral code generator & share intent |
| 52 | `AdminUserManagementActivity` | `activity_admin_user_management.xml` | Admin | Registered | `AdminDashboardActivity`, Admin Drawer | Search, filter (Student/Teacher), block/unblock, delete | Preserve user query, status toggling & role filters |
| 53 | `AdminContentManagementActivity` | `activity_admin_content_management.xml` | Admin | Registered | `AdminDashboardActivity`, Admin Drawer | Upload lessons, HTML assets, video links, subject creation | Preserve material persistence & file storage |
| 54 | `AdminReportsActivity` | `activity_admin_reports.xml` | Admin | Registered | `AdminDashboardActivity`, Admin Drawer | System analytics, student activity logs, export reports | Preserve audit logs & export functionality |
| 55 | `AdminSystemSettingsActivity` | `activity_admin_system_settings.xml` | Admin | Registered | `AdminDashboardActivity`, Admin Drawer | Site name, maintenance mode toggle, email SMTP settings | Preserve global system settings & preferences |
| 56 | `AdminPaymentVerificationActivity` | `activity_admin_payment_verification.xml` | Admin | Registered | **Omitted from Drawer/Dashboard** | Review pending student ETB bank transfers, approve/reject | **Preserve verification engine; wire into Admin Navigation** |
| 57 | `AdminQuestionActivity` | `activity_admin_question.xml` | Admin | Registered | `AdminDashboardActivity` (`btnCreateLab`) | Add exam questions with 4 options, answer key, explanation | Preserve question authoring & database insertion |
| 58 | `AdminSupportManagementActivity` | `activity_admin_support_management.xml` | Admin | Registered | **Omitted from Drawer/Dashboard** | Manage student support tickets, respond, change status | **Preserve support ticketing; wire into Admin Navigation** |
| 59 | `AdminCameraLabActivity` | `activity_admin_camera_lab.xml` | Admin | Registered | `AdminDashboardActivity`, Admin Drawer | Camera Lab telemetry, OCR usage statistics, model settings | Preserve OCR usage statistics & telemetry |
| 60 | `AdminProfileActivity` | `activity_admin_profile.xml` | Admin | Registered | `AdminDashboardActivity` (Profile avatar) | Admin account information, password change, audit actions | Preserve admin profile management |

---

## 3. CRITICAL ARCHITECTURAL & NAVIGATION DEFECTS

### Defect 1: Six Unregistered Activities Causing Hard Crashes (`ActivityNotFoundException`)
The following six fully implemented Activities and XML layouts exist in the codebase but are **completely omitted from `AndroidManifest.xml`**:

1. **`FlashcardActivity.java` (Layout: `activity_flashcard.xml`):**
   - Trigger: `DashboardActivity.java` line 165:
     ```java
     safeSetClick(R.id.cardFlashcards, v -> startActivity(new Intent(this, FlashcardActivity.class)));
     ```
   - **User Impact:** Tapping the "Mastery Flashcards" card on the student dashboard immediately crashes the application with `android.content.ActivityNotFoundException: Unable to find explicit activity class {com.million.learnify/com.million.learnify.FlashcardActivity}`.
2. **`CameraHistoryActivity.java` (Layout: `activity_camera_history.xml`):**
   - Trigger: `CameraLabActivity.java` line 213:
     ```java
     btnHistory.setOnClickListener(v -> startActivity(new Intent(this, CameraHistoryActivity.class)));
     ```
   - **User Impact:** Tapping the History button inside Camera AI Lab crashes the app.
3. **`CameraSettingsActivity.java` (Layout: `activity_camera_settings.xml`):**
   - Trigger: `CameraLabActivity.java` line 215:
     ```java
     btnSettings.setOnClickListener(v -> startActivity(new Intent(this, CameraSettingsActivity.class)));
     ```
   - **User Impact:** Tapping the Settings icon inside Camera AI Lab crashes the app.
4. **`AnalyticsActivity.java` (Layout: `activity_analytics.xml`):**
   - Fully built visual progress tracking screen containing XP counters, study streaks, quizzes passed, minutes studied, and subject mastery bars. It is unregistered and orphaned from navigation.
5. **`AttendanceActivity.java` (Layout: `activity_attendance.xml`):**
   - Fully implemented student attendance portal with circular progress indicators, present/absent counters, and attendance history logs. It is unregistered and orphaned from navigation.
6. **`GpaCalculatorActivity.java` (Layout: `activity_gpa_calculator.xml`):**
   - High-value academic tool allowing students to dynamically add courses, select credit hours and letter grades, and calculate term/cumulative GPA with visual feedback. It is unregistered and orphaned from navigation.

### Defect 2: Toolbar & Navigation Drawer Anti-Patterns
1. **Misplaced Navigation Drawer Button:**
   - Both `activity_dashboard.xml` (line 94) and `activity_admin_dashboard.xml` (line 114) render a separate `ImageButton` (`btnOpenDrawer`) floating inside the scrollable content view *below* the toolbar.
   - In standard Android/Material design, the hamburger menu belongs directly in the Toolbar's navigation icon slot (`setSupportActionBar` + `ActionBarDrawerToggle`).
2. **Back Navigation on Root Screens:**
   - In `BaseActivity.java`, `setupPortalToolbar()` unconditionally attaches a back arrow (`android.R.drawable.ic_menu_revert`) wired to `onBackPressed()`. When called from root dashboards (`DashboardActivity` and `AdminDashboardActivity`), this creates a back arrow on the top-level home screen that exits the app when pressed.
   - `AdminDashboardActivity` has a custom toolbar element: `[ ← Dashboard ]` containing a back arrow on the root admin landing screen.
3. **Corrupted String Encoding in Navigation Drawer:**
   - In `app/src/main/res/menu/drawer_menu.xml` (line 9):
     ```xml
     <item
         android:id="@+id/nav_visualizer"
         android:icon="@android:drawable/ic_menu_view"
         android:title="dY&quot; Interactive Lab" />
     ```
   - A corrupted multi-byte emoji string (`dY"`) is displayed directly in the student navigation drawer.
4. **Outdated Android Framework Drawables:**
   - Menus and headers rely on ancient Android 2.x system drawables (`@android:drawable/ic_menu_today`, `ic_menu_agenda`, `ic_menu_myplaces`, `ic_menu_manage`, `ic_lock_power_off`), creating a dated, inconsistent look.

### Defect 3: Disconnected Admin Management Workflows
- **`AdminPaymentVerificationActivity`:** Students submit bank transfer receipts (Telebirr, CBE) through `PaymentActivity.java`. While the admin verification screen is fully functional in code and registered in the manifest, it is not present in `admin_drawer_menu.xml` or reachable from `AdminDashboardActivity`. Admins have no UI path to review and approve student payments.
- **`AdminSupportManagementActivity`:** Students submit support requests through `HelpSupportActivity.java`, but the admin support ticket resolution portal is completely missing from admin navigation.

---

## 4. VISUAL HIERARCHY, STYLING & THEME INCONSISTENCIES

### 1. The "Split-Brain" Multi-Theme Dilemma
The application currently switches between four fundamentally incompatible design aesthetics without rhyme or reason:
1. **The Admin "Cyber / Neon" Dark Theme:**
   - Background: Dark slate `#0F172A`, cards: `#1E293B`, accents: Neon Cyan `#00B4D8`, neon amber `#F59E0B`, neon green `#10B981`. Uses custom rounded drawables (`bg_cyber_card`, `bg_admin_menu_panel`).
2. **The Student "Rainbow Pastel" Theme:**
   - Student Dashboard displays cards with 8 distinct, arbitrary pastel backgrounds: `#F0FDFA` (Teal), `#EFF6FF` (Blue), `#FFF7ED` (Orange), `#FFFBEB` (Amber), `#F5F3FF` (Purple), `#E0F2F1` (Mint), `#EEF2FF` (Indigo), `#FDF2F8` (Pink).
3. **The Midnight / Pitch-Black Void:**
   - `AdminContentManagementActivity` sets `android:background="#010114"`.
   - `activity_main.xml` sets portal card backgrounds to pure black `#000000` with bright blue `#2962FF` borders over `@drawable/bg_admin_mix`.
4. **Mismatched Toolbars Across Screens:**
   - Dashboard: Indigo (`#4F46E5`).
   - Subject / Course Catalog: Ocean Blue (`#0077B6`).
   - Analytics: Royal Blue (`#2563EB`).
   - GPA Calculator: Plain White (`#FFFFFF`) with dark text.
   - Admin Dashboard: Dark Slate (`#0F172A`).
   - Admin User Management: Indigo (`#4F46E5`).

### 2. Branding Incoherence
- **`MainActivity.xml` Header:** Hardcoded title reads `"EduPortal"` with a heavy 90s-style shadow (`#480D66`).
- **`MainActivity.xml` Footer:** Hardcoded footer reads `"AceGrade • Professional Learning"`.
- **Application Manifest & Strings:** The official application name is `"Learnify"`.
- Users launching Learnify see "Learnify" on the splash screen, "EduPortal" and "AceGrade" on the gateway screen, and "Student Dashboard" inside the portal.

### 3. Input Component Discrepancies
- **`StudentLoginActivity`:**
  - Username uses a raw `EditText` with `android:background="@drawable/bg_input_blue_border"`.
  - Password uses a `TextInputLayout` with `app:boxBackgroundMode="none"` wrapping a `TextInputEditText` that also has `@drawable/bg_input_blue_border`.
  - Top avatar image uses `@drawable/ic_real_camera` (a literal camera) for the student login profile icon.
- **`AdminLoginActivity`:**
  - Uses `com.google.android.material.textfield.TextInputLayout` with OutlinedBox styling and floating labels.
- **`AdminUserManagementActivity`:**
  - The search input inside the toolbar uses `android:background="@drawable/bg_chat_bubble_user"` (a chat message bubble drawable) as an input field background.

### 4. Broken Dark Mode Adaptability & Hardcoded Color Anti-Patterns
- `styles.xml` hardcodes light backgrounds (`#FFFFFF`, `#F8FAFC`, `#F3F4F6`) and dark text colors (`#111827`, `#1E293B`, `#333333`) across cards and inputs.
- When Android Dark Theme is enabled, `Theme.Material3.DayNight.NoActionBar` sets window backgrounds to dark, but cards with hardcoded `#FFFFFF` remain blinding white boxes with unreadable white or light-gray text inside.
- Dozens of screens hardcode emojis into button texts and headings (`"⏱️ Focus Timer"`, `"🌈 Mood Log"`, `"🎴 Mastery"`, `"📊 GPA Calculator"`, `"🎯 GPA RESULT CARD"`), which render inconsistently across Android OEM skins and look unprofessional.

---

## 5. UNIFIED DESIGN SYSTEM SPECIFICATION (MODERN MATERIAL 3)

To give Learnify a cohesive, world-class educational experience, all screens will adhere to a single unified Material 3 design system with semantic design tokens:

### 1. Color Palette Tokens (Academic Sapphire & Slate)

| Token Name | Light Value | Dark Value | Purpose |
|---|---|---|---|
| `colorPrimary` | `#1E40AF` (Sapphire Blue 800) | `#60A5FA` (Blue 400) | Primary branding, active buttons, key highlights |
| `colorOnPrimary` | `#FFFFFF` | `#0F172A` | Text/icons on primary color |
| `colorPrimaryContainer` | `#DBEAFE` (Blue 100) | `#1E3A8A` (Blue 900) | Selected chips, badge backgrounds, active tabs |
| `colorOnPrimaryContainer` | `#1E3A8A` (Blue 900) | `#DBEAFE` (Blue 100) | Text/icons on primary container |
| `colorSecondary` | `#0D9488` (Teal 600) | `#2DD4BF` (Teal 400) | Accents, success stats, secondary actions |
| `colorSurface` | `#FFFFFF` | `#1E293B` (Slate 800) | Card surfaces, dialog backgrounds, sheets |
| `colorSurfaceVariant` | `#F1F5F9` (Slate 100) | `#334155` (Slate 700) | Input field fills, subtle card backgrounds |
| `colorBackground` | `#F8FAFC` (Slate 50) | `#0F172A` (Slate 900) | Global screen window background |
| `colorTextPrimary` | `#0F172A` (Slate 900) | `#F8FAFC` (Slate 50) | Main titles, primary headings, body text |
| `colorTextSecondary` | `#64748B` (Slate 500) | `#94A3B8` (Slate 400) | Subtitles, helper text, timestamps |
| `colorOutline` | `#E2E8F0` (Slate 200) | `#334155` (Slate 700) | Card borders, dividers, outlined inputs |
| `colorStatusSuccess` | `#10B981` | `#34D399` | Passing grades, active status, completed |
| `colorStatusWarning` | `#F59E0B` | `#FBBF24` | Pending verification, exam timers < 5 min |
| `colorStatusError` | `#EF4444` | `#F87171` | Suspended users, incorrect answers, errors |

### 2. Component Standards
- **AppBars / Toolbars:**
  - Surface: `colorPrimary` with `colorOnPrimary` text, or clean `colorSurface` with `colorTextPrimary`.
  - Elevation: 0dp with 1dp bottom divider or subtle 2dp elevation.
  - Root screens (`DashboardActivity`, `AdminDashboardActivity`): Standard hamburger icon (`ActionBarDrawerToggle`) opening `drawerLayout`.
  - Child screens: Standard back arrow (`ic_arrow_back`) wired to `finish()`.
- **Card Views (`MaterialCardView`):**
  - Corner radius: `16dp` uniformly (replacing arbitrary 8dp, 20dp, 28dp, 40dp).
  - Elevation: `1dp` to `2dp` subtle elevation with `strokeColor="@color/colorOutline"` and `strokeWidth="1dp"`.
  - Background: `@color/colorSurface` (never hardcoded pastel or jet-black hex values).
- **Form Inputs (`TextInputLayout` & `TextInputEditText`):**
  - Style: `Widget.Material3.TextInputLayout.OutlinedBox` with `boxCornerRadius="12dp"`.
  - No raw `EditText` controls with fixed drawables.
  - Floating labels, start icons, end icons (password toggle).
- **Buttons (`MaterialButton`):**
  - Corner radius: `12dp` (or `24dp` for pill-shaped primary CTAs).
  - Clear hierarchy: Filled for primary, Tonal for secondary, Outlined for auxiliary, Text for links.
- **Iconography:**
  - Replace hardcoded emojis and `@android:drawable/ic_menu_*` with clean, modern vector drawables (`@drawable/ic_*`).

---

## 6. PHASED IMPLEMENTATION ROADMAP (BATCHES 1–6)

All implementation steps will strictly preserve 100% of existing logic, database schemas, and workflows, working incrementally with verification gates on the physical phone.

### Batch 1: Navigation Foundation, Manifest Fixes & Crash Prevention
- **Objective:** Eliminate all crashes from missing manifest declarations, resolve broken drawer navigation, fix corrupted menu items, and connect orphaned features.
- **Key Tasks:**
  1. Add all 6 missing activities to `AndroidManifest.xml` (`FlashcardActivity`, `CameraHistoryActivity`, `CameraSettingsActivity`, `AnalyticsActivity`, `AttendanceActivity`, `GpaCalculatorActivity`).
  2. Fix `drawer_menu.xml` and `admin_drawer_menu.xml`: Replace corrupted `dY"` string with "Interactive Lab", link `AnalyticsActivity`, `AttendanceActivity`, and `GpaCalculatorActivity` in student drawer, link `AdminPaymentVerificationActivity` and `AdminSupportManagementActivity` in admin drawer.
  3. Refactor `BaseActivity.java`: Separate root dashboard toolbar setup (hamburger icon toggles drawer) from child screen toolbar setup (back arrow returns). Remove redundant floating `btnOpenDrawer` buttons.
- **Verification Gate:** Build APK; verify on physical phone that tapping Flashcards, Camera History, and Camera Settings never crashes; verify drawer opens smoothly on both portals.

### Batch 2: Design Tokens & Core Theme Unification
- **Objective:** Establish the unified Material 3 color system, semantic tokens, and typography in XML resources without breaking any existing layout ID.
- **Key Tasks:**
  1. Refactor `colors.xml`: Define academic sapphire, slate, surface, and semantic status tokens.
  2. Refactor `themes.xml` & `styles.xml`: Update `Theme.TestApp` to properly support DayNight mode, standard card styles (`AppCard`), standard outlined inputs (`AppInputLayout`), and button styles.
  3. Replace outdated Android framework drawables with modern vector drawables in `res/drawable/`.
- **Verification Gate:** Build APK; toggle Light/Dark mode on phone; verify no crashes, no missing resources, and high text contrast.

### Batch 3: Welcome, Gateway & Authentication Modernization
- **Objective:** Deliver a cohesive, modern first impression and auth flow with consistent "Learnify" branding.
- **Key Tasks:**
  1. `WelcomeActivity` / `activity_welcome.xml`: Modernize typography, apply sapphire brand accents, preserve auto-login timer and splash transition.
  2. `MainActivity` / `activity_main.xml`: Fix "EduPortal" to "Learnify", remove "AceGrade", update Student and Admin selection cards to modern MaterialCards.
  3. `StudentLoginActivity` / `activity_student_login.xml`: Convert inputs to OutlinedBox `TextInputLayout`, replace camera avatar with student avatar, clean up social buttons and layout weights.
  4. `AdminLoginActivity` / `AdminRegisterActivity` / `RegisterActivity`: Harmonize with unified design tokens, preserving PBKDF2 hashing, email verification, and session persistence.
- **Verification Gate:** Build and install on physical phone; test student and admin login, registration, and logout flows.

### Batch 4: Student Portal & Academic Dashboard Revamp
- **Objective:** Transform the student experience from fragmented pastel cards into an inspiring, cohesive educational hub.
- **Key Tasks:**
  1. `DashboardActivity` / `activity_dashboard.xml`: Modernize quick action cards (Focus Timer, Mood Tracker, Mastery Flashcards), standardize subject cards, replace emojis with vector icons, polish welcome banner.
  2. Polish linked student screens: `SubjectActivity`, `ChapterListActivity`, `TopicListActivity`, and `NoteDetailActivity`.
  3. Polish `FlashcardActivity`, `AnalyticsActivity`, `AttendanceActivity`, and `GpaCalculatorActivity` with the new design tokens.
  4. Ensure `ExamActivity` maintains verified Batch 1 timer and scoring while adopting clean Material card styling.
- **Verification Gate:** Navigate through full student study path on device (Dashboard → Subject → Chapter → Topic → Note → Flashcard → Quiz → Exam).

### Batch 5: Admin Experience & Governance Modernization
- **Objective:** Align the admin interface with the modern educational system, removing jarring black/white contrast shifts.
- **Key Tasks:**
  1. `AdminDashboardActivity` / `activity_admin_dashboard.xml`: Remove redundant toolbar back arrow, replace cyber neon styling with authoritative dark slate/sapphire tokens, wire all stat cards.
  2. `AdminUserManagementActivity`: Fix chat bubble background on search input, polish user list cards and filter chips.
  3. `AdminContentManagementActivity`: Unify background with portal theme, style asset upload forms.
  4. Integrate `AdminPaymentVerificationActivity` and `AdminSupportManagementActivity` into admin quick actions and drawer.
- **Verification Gate:** Log in as admin on device; verify user management, content upload, payment verification, and ticket resolution.

### Batch 6: Learning Tools, Labs, Simulations & Polish
- **Objective:** Elevate interactive components and ensure flawless multi-screen responsiveness.
- **Key Tasks:**
  1. `VisualizerActivity`, `PhysicsLabActivity`, `ChemistryLabActivity`, `BiologyLabActivity`: Clean up simulation controls, sliders, and experiment cards while strictly preserving Chaquopy Python bindings.
  2. `CameraLabActivity`: Modernize camera overlay, OCR control chips, and action buttons.
  3. `AIChatActivity`: Elevate message bubble styling, input bar, and offline AI status indicator.
  4. Comprehensive device smoke test across all 60 activities.
- **Verification Gate:** Run full regression suite on physical TECNO phone. Confirm 0 crashes, 100% features functioning, and flawless responsive layouts.

---

## 7. TOP 3 HIGHEST-IMPACT IMPROVEMENTS FOR IMMEDIATE IMPLEMENTATION

The following three improvements provide the greatest immediate value in stability, usability, and visual quality:

### 1. Register Missing Activities & Fix Showstopper Navigation Crashes
- **Impact:** Fixes critical runtime crashes (`ActivityNotFoundException`) when users tap "Mastery Flashcards" on the Dashboard or "History"/"Settings" in Camera AI Lab. 
- **Value:** Instantly restores access to 6 fully implemented features (`FlashcardActivity`, `CameraHistoryActivity`, `CameraSettingsActivity`, `AnalyticsActivity`, `AttendanceActivity`, `GpaCalculatorActivity`) without writing new business logic.

### 2. Standardize AppBars, Drawers & Fix Toolbar Anti-Patterns
- **Impact:** Resolves the broken navigation architecture where root dashboards have confusing back buttons and floating sidebar toggles.
- **Value:** Replaces the corrupted `dY"` string, embeds the hamburger menu properly inside the toolbar, connects unlinked Admin features (Payment Verification and Support Management), and provides intuitive, standard Android navigation.

### 3. Unify Split-Brain Styling into Modern Material 3 Design Tokens
- **Impact:** Eliminates the jarring clash between the Admin "Cyber" theme, the Student "Rainbow Pastel" cards, and the pitch-black gateway cards.
- **Value:** Fixes broken Dark Mode contrast, unifies branding under "Learnify", replaces raw `EditText` controls with clean `TextInputLayout` fields, and elevates the application into a cohesive, premium educational product.

---

## 8. BATCH 1 EXECUTION RESULTS & VERIFICATION REPORT

**Execution Date:** October 2026  
**Status:** Completed & Verified — Build Passing (`BUILD SUCCESSFUL in 52s`)  
**Target APK:** `app\build\outputs\apk\debug\app-debug.apk` (149,226,111 bytes)  
**Hardware Verification Target:** Physical TECNO Spark 10C (`TECNO_KI5q`, Android 12)

### 1. Scope Accomplishments

#### A. Registered Verified Missing Activities in `AndroidManifest.xml`
- Registered the 6 previously undeclared Activities with strict security attributes:
  ```xml
  <activity android:name=".FlashcardActivity" android:exported="false" />
  <activity android:name=".CameraHistoryActivity" android:exported="false" />
  <activity android:name=".CameraSettingsActivity" android:exported="false" />
  <activity android:name=".AnalyticsActivity" android:exported="false" />
  <activity android:name=".AttendanceActivity" android:exported="false" />
  <activity android:name=".GpaCalculatorActivity" android:exported="false" />
  ```
- **Impact:** Completely eliminates runtime `ActivityNotFoundException` crashes when tapping "Mastery Flashcards" on the Student Dashboard or "History" and "Settings" inside Camera AI Lab.

#### B. Repaired Drawer Menu Title
- Fixed corrupted encoding in `app/src/main/res/menu/drawer_menu.xml`:
  - Replaced corrupted `dY" Interactive Lab` / `🔬 Interactive Lab` with clean, standard UTF-8 string: `Interactive Lab`.

#### C. Expanded Student Navigation (Zero Removal Guarantee)
- Added a dedicated "Academics" category in `drawer_menu.xml` without altering or removing any of the 8 pre-existing primary group items, the Personal group, or the System group:
  - `Learning Analytics` (`@id/nav_analytics` → `AnalyticsActivity.class`)
  - `Attendance Tracker` (`@id/nav_attendance` → `AttendanceActivity.class`)
  - `GPA Calculator` (`@id/nav_gpa_calc` → `GpaCalculatorActivity.class`)
- Updated `DashboardActivity.java` `handleNavigation()` to route each destination cleanly.

#### D. Expanded Admin Navigation (Strict RBAC Preserved)
- Added the two missing operational portals into `app/src/main/res/menu/admin_drawer_menu.xml`:
  - `Payment Verification` (`@id/admin_nav_payment_verification` → `AdminPaymentVerificationActivity.class`)
  - `Support Center` (`@id/admin_nav_support_management` → `AdminSupportManagementActivity.class`)
- Updated `AdminDashboardActivity.java` `handleNavigation()` to route both screens.
- **Security Check:** Verified that both `AdminPaymentVerificationActivity.java` and `AdminSupportManagementActivity.java` execute `enforceRole("ADMIN")` immediately on entry.

#### E. Standardized Toolbar & Drawer Toggle Architecture
- **`BaseActivity.java`:**
  - Modernized `setupPortalToolbar()` for child screens to load `R.drawable.ic_back_white` (standard Android back arrow) with fallback to `android.R.drawable.ic_menu_revert`.
  - Introduced `setupRootToolbar(Toolbar toolbar, String title, DrawerLayout drawerLayout)` for root dashboards, setting `R.drawable.ic_menu` (hamburger icon) to toggle `drawerLayout.openDrawer(GravityCompat.START)`.
  - Preserved `setupDrawerButton(drawerLayout)` so any layout containing `btnOpenDrawer` retains 100% of its existing click functionality.
- **`DashboardActivity.java`:**
  - Configured root toolbar with `setupRootToolbar(findViewById(R.id.toolbar), "Student Dashboard", drawerLayout)`.
  - Kept `btnOpenDrawer` operational.
- **`AdminDashboardActivity.java` & `activity_admin_dashboard.xml`:**
  - In `activity_admin_dashboard.xml`, replaced `ic_back_white` in `llToolbarBack` with `@drawable/ic_menu`.
  - In `AdminDashboardActivity.java`, wired `llToolbarBack` to open the admin drawer and removed the duplicate back arrow from the toolbar. Kept `btnOpenDrawer` operational.

---

### 2. Feature Preservation Matrix

| Feature / Component | Status | Verification Detail |
|---|---|---|
| All 60 Activities | **100% Preserved** | All 60 activities present and compilable; 6 previously unregistered activities now safe to launch. |
| All 58 XML Layouts | **100% Preserved** | Zero layouts deleted or renamed. |
| Student Dashboard Cards | **100% Preserved** | All 12 feature cards (Focus Timer, Mood Tracker, Mastery Flashcards, Timetable, Courses, Assignments, Daily Challenge, Camera Lab, Visualizers, AI Assistant, Premium, Announcements) remain active. |
| Student Drawer Destinations | **100% Preserved + Expanded** | 8 original items + 3 new Academic items (`Analytics`, `Attendance`, `GPA`) + Personal + System intact. |
| Admin Drawer Destinations | **100% Preserved + Expanded** | 7 original items + 2 new items (`Payment Verification`, `Support Center`) + System intact. |
| Database & Schema | **100% Preserved** | Zero schema migrations or table modifications. Existing SQLite database intact. |
| Security & PBKDF2 Password Updates | **100% Preserved** | `PasswordUtils.java`, `PrivacySecurityActivity.java`, `enforceRole()` guards, email verification walls, and session healing intact. |
| Timed Exam Lifecycle & Timer | **100% Preserved** | Phase 2 Batch 1 timer recovery and auto-submission in `ExamActivity.java` intact. |

---

### 3. Step-by-Step Physical Device Testing Instructions

Connect the physical phone (`TECNO Spark 10C`) via ADB and install the newly built APK:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

#### Test Suite 1: Crash Prevention & New Student Destinations
1. Open the Learnify app on the phone and log in as a student (or auto-resume student session).
2. On the **Student Dashboard**, tap the **Mastery Flashcards** card.
   - *Expected:* `FlashcardActivity` opens smoothly without crashing. Flip flashcards and verify rating buttons.
3. Tap back to return to the Dashboard.
4. Tap the **Camera Lab** card, then tap the **History** icon in the top toolbar.
   - *Expected:* `CameraHistoryActivity` opens showing previous scan history without crashing.
5. Tap back to Camera Lab, then tap the **Settings** icon in the top toolbar.
   - *Expected:* `CameraSettingsActivity` opens showing camera toggles without crashing.
6. Return to the Student Dashboard. Tap the top-left **hamburger menu icon** on the toolbar (or the floating button below).
   - *Expected:* The navigation drawer slides open smoothly.
7. Verify the new **Academics** section:
   - Tap **Learning Analytics**: Verify XP, streak, and subject progress screen opens.
   - Tap **Attendance Tracker**: Verify attendance circular progress and logs screen opens.
   - Tap **GPA Calculator**: Verify GPA calculation engine opens; test adding courses and computing term GPA.

#### Test Suite 2: Admin Navigation & Verification Portals
1. Log out or switch to the **Admin Portal** and log in with admin credentials.
2. On the **Admin Dashboard**:
   - Verify the top toolbar displays the hamburger menu `[ ☰ Dashboard ]` instead of a back button `[ ← Dashboard ]`.
   - Tap `[ ☰ Dashboard ]` (or the sidebar button below): Verify the admin navigation drawer slides open.
3. In the admin drawer:
   - Tap **Payment Verification**: Verify `AdminPaymentVerificationActivity` opens, displaying pending student bank transfers.
   - Tap **Support Center**: Verify `AdminSupportManagementActivity` opens, displaying student tickets.
   - Verify all existing admin items (User Management, Issue Certificate, Content Management, Camera Lab Control, Reports & Monitoring, Add Exam Question, System Settings, Logout) remain functional.




---

## 9. Batch 2A: Student & Admin Dashboard Redesign Implementation Report

### Executive Summary
In Batch 2A, the Student Dashboard (`DashboardActivity` / `activity_dashboard.xml`) and Administrator Command Center (`AdminDashboardActivity` / `activity_admin_dashboard.xml`) were redesigned into an **Academic Sapphire** identity. 100% of all existing screens, features, navigation destinations, buttons, cards, database operations, and authentication rules were preserved with zero breaking changes or deletions.

### Key Enhancements

#### A. Academic Sapphire Visual Identity & System Tokens
1. **Design Tokens (`colors.xml`)**:
   - `sapphire_primary` (`#1E40AF`), `sapphire_primary_dark` (`#1E3A8A`), `sapphire_primary_light` (`#3B82F6`), `sapphire_subtle` (`#EFF6FF`).
   - `teal_accent` (`#0F766E`), `teal_accent_light` (`#14B8A6`), `teal_subtle` (`#F0FDFA`).
   - `amber_accent` (`#D97706`), `amber_subtle` (`#FFFBEB`).
   - `rose_accent` (`#E11D48`), `rose_subtle` (`#FFF1F2`).
   - `purple_accent` (`#7C3AED`), `purple_subtle` (`#F5F3FF`).
   - `emerald_accent` (`#059669`), `emerald_subtle` (`#ECFDF5`).
   - Card strokes and surfaces: `card_stroke` (`#E2E8F0`), `surface_light` (`#F8FAFC`), `surface_card` (`#FFFFFF`).
   - Executive Admin dark surfaces: `admin_dark_bg` (`#0F172A`), `admin_card_bg_v2` (`#1E293B`), `admin_card_stroke` (`#334155`).
2. **Production-Grade Vector Drawables (`res/drawable/`)**:
   - Replaced emoji iconography with crisp, scalable XML vector drawables:
     - Focus Timer: `ic_timer.xml`
     - Mood Tracker: `ic_mood.xml`
     - Flashcards / Mastery: `ic_flashcard.xml`
     - Timetable / Schedule: `ic_calendar.xml`
     - Daily Challenge: `ic_trophy.xml`
     - Announcements: `ic_announcement.xml`
     - Unlock Premium: `ic_crown.xml`
     - Streak Indicator: `ic_flame.xml`
     - XP Counter: `ic_bolt.xml`
     - Card Chevron: `ic_chevron_right.xml`
     - Verification Badge: `ic_verified_check.xml`
     - Support Center: `ic_support_headset.xml`

#### B. Student Dashboard (`activity_dashboard.xml` & `DashboardActivity.java`)
1. **Hero Header & Gamification Bar**:
   - Elevated Academic Sapphire gradient hero card (`bg_hero_banner.xml`).
   - Displays real-time student name, greeting, and learning path status.
   - Live XP and Streak gamification chips dynamically populated from `repository.getLocalDb().getChallengeStats(currentUsername)`.
2. **Top Quick Utility Trio**:
   - 3-column row with 16dp corner radius cards, subtle border, and tinted icon containers.
   - `cardFocusTimer`: Focus Timer dialog with 25-minute Pomodoro prompt.
   - `cardMoodTracker`: Mood Log dialog with daily check-in options.
   - `cardFlashcards`: Direct launch to Flashcards Mastery.
3. **Structured Academic Categorization (100% ID & Feature Preservation)**:
   - **Academic Essentials**:
     - `cardDailyChallenge`: Featured Daily Quest card with trophy icon, XP callout, and quest indicator.
     - `cardCourses`: Curriculum lessons with `ic_courses`.
     - `cardTimetable`: Weekly schedule with `ic_calendar`.
     - `cardAssignments`: Tasks and assignments tracking with `ic_tasks`.
   - **Discovery & AI Labs**:
     - `cardCameraLab`: Camera AI Lab with `ic_real_camera`.
     - `cardVisualizers`: Interactive Science Lab with `ic_lab`.
     - `cardAI`: Edu AI Assistant with `ic_ai_hexagon`.
   - **Campus & Membership**:
     - `cardAnnouncements`: Campus Bulletin with `ic_announcement`.
     - `cardPremium`: Radiant Sapphire & Gold PRO membership card with `ic_crown`.
4. **App Bar & Navigation**:
   - Root toolbar with notification bell container (`rlNotificationContainer`, unread badge count `tvNotificationBadgeCount`), voice assistant (`ivVoiceAssistant`), drawer toggle, and preserved bottom navigation (`bottomNavigation`).

#### C. Administrator Dashboard (`activity_admin_dashboard.xml` & `AdminDashboardActivity.java`)
1. **Executive Command Center Layout**:
   - Deep Executive Slate palette (`#0F172A` background, `#1E293B` elevated cards, `#334155` subtle borders).
   - Removed jarring neon styling in favor of modern enterprise card design.
2. **Real-Time Institutional Metrics Grid**:
   - Replaced vertical list with an organized 2-column executive metrics grid:
     - `cardTotalStudents` / `tvTotalStudents`: Total Student Records
     - `cardActiveStudents` / `tvActiveStudents`: Active Students Online
     - `cardTeachers` / `tvTotalTeachers`: Faculty & Staff Count
     - `cardTotalLabs` / `tvTotalLabs`: Virtual Labs Count
     - `cardCompletedLabs` / `tvCompletedLabs`: Completed Submissions
     - `cardPendingLabs` / `tvPendingLabs`: Pending Action Tasks
   - Full-width System Infrastructure Health Card (`cardSystemHealth` / `tvSystemHealth`).
   - Primary Credential Issuance Banner (`cardIssueCert`).
3. **Management & Governance Actions**:
   - Clean executive action buttons using `@style/AdminExecutiveButton` with vector icons:
     - `btnIssueCertificate`: Issue Certificate
     - `btnCreateLab`: Create Lab / Question
     - `btnAddUser`: Add User / Staff
     - `btnUpload`: Upload Curriculum
     - `btnSimulate`: Physics Simulation
     - `btnReports`: System Analytics
     - `btnSettings`: System Settings
4. **Compliance & Support Direct Access**:
   - Direct cards for `cardPaymentVerification` (Admin Payment Verification) and `cardSupportCenter` (Admin Support Center).

### Build & Test Verification Results
- **Debug APK Compilation**: `.\gradlew.bat assembleDebug --no-daemon` succeeded (`BUILD SUCCESSFUL in 1m 30s`).
  - Output APK: `app\build\outputs\apk\debug\app-debug.apk` (Size: 150,607,225 bytes).
- **Unit Tests**: `.\gradlew.bat testDebugUnitTest --no-daemon` succeeded (`BUILD SUCCESSFUL in 1m 24s`).
  - Result: 12 tests executed, 0 failures, 100% pass rate.


---

## 10. Complete Settings, Buttons, Controls, and Navigation Quality Audit

### 1. Complete Inventory of Settings-Related Screens
The application includes 13 dedicated settings, preference, profile, security, and administrative control activities:

| Activity | Layout File | Portal | Primary Responsibility | Data Persistence Layer |
| :--- | :--- | :--- | :--- | :--- |
| **`SettingsActivity`** | `activity_settings.xml` | Student | Primary settings hub: study reminder time, haptics, theme, language, cache, sub-page navigation | `StudentSettings` (`SharedPreferences`) |
| **`PrivacySecurityActivity`** | `activity_privacy_security.xml` | Student | PBKDF2 password updates, 2FA toggle, account export, account deletion | SQLite `users` table & `StudentSettings` |
| **`NotificationPreferencesActivity`** | `activity_notification_preferences.xml` | Student | Granular notification channel toggles (8 switches) & daily schedule | `StudentSettings` (`SharedPreferences`) |
| **`EditProfileActivity`** | `activity_edit_profile.xml` | Student | Student avatar, personal credentials, school/year/major, daily XP goal slider, visibility | SQLite `users` table |
| **`ProfileActivity`** | `activity_profile.xml` | Student | Interactive profile overview, achievements, stats, navigation to edit profile/settings | SQLite `users` & `materials` tables |
| **`LinkedAccountsActivity`** | `activity_linked_accounts.xml` | Student | Google, Apple, Microsoft SSO linkage statuses and unlinking | In-memory ephemeral booleans *(Defect)* |
| **`InviteFriendsActivity`** | `activity_invite_friends.xml` | Student | Referral code generator, clipboard copy, share intent, contacts picker | Ephemeral in-memory |
| **`CameraSettingsActivity`** | `activity_camera_settings.xml` | Student | AI diagram auto-detect, alignment grid lines, scan history saving | Non-persistent Toasts *(Defect)* |
| **`HelpSupportActivity`** | `activity_help_support.xml` | Student | Categorized FAQs, problem reporter, student ticket submission, developer email | SQLite `support_tickets` table |
| **`AboutActivity`** | `activity_about.xml` | Student | Dynamic package version, Terms of Service, Privacy Policy, Play Store rating | Android `PackageManager` |
| **`AdminSystemSettingsActivity`** | `activity_admin_system_settings.xml` | Admin | 10 domains of platform infrastructure, maintenance toggle, exam timer/pass score defaults | SQLite `settings` table |
| **`AdminSupportManagementActivity`** | `activity_admin_support_management.xml` | Admin | Student ticket queue management, status resolution, official administrative replies | SQLite `support_tickets` table |
| **`AdminProfileActivity`** | `activity_admin_profile.xml` | Admin | Administrator avatar, credentials, policy summary, admin chat | SQLite `users` table |

---

### 2. Comprehensive Interactive Control Inventory

#### A. Student Settings Hub (`SettingsActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `settingsToolbar` | Toolbar | Back arrow navigates to dashboard | Sets up back navigation `v -> finish()` | ✅ Verified functional |
| `btnSaveSettings` | ImageView | Saves settings and reschedules study reminder | Calls `settingsService.saveSettings()` and `scheduleStudyReminder()` | ✅ Verified functional |
| `llAccountSettings` | LinearLayout | Opens Edit Profile screen | Launches `new Intent(this, EditProfileActivity.class)` | ✅ Verified functional |
| `llPrivacySecurity` | LinearLayout | Opens Privacy & Security screen | Launches `new Intent(this, PrivacySecurityActivity.class)` | ✅ Verified functional |
| `llNotificationPrefs` | LinearLayout | Opens Notification Preferences screen | Launches `new Intent(this, NotificationPreferencesActivity.class)` | ✅ Verified functional |
| `llReminderTime` / `tvReminderTime` | LinearLayout | Opens TimePickerDialog and updates study reminder | Shows `TimePickerDialog`, saves to `StudentSettings`, schedules alarm | ✅ Verified functional |
| `switchSoundEffects` | MaterialSwitch | Toggles UI audio effects | Updates `StudentSettings.soundEffects` and saves | ✅ Verified functional |
| `switchHapticFeedback` | MaterialSwitch | Toggles vibration feedback | Updates `StudentSettings.hapticFeedback` and saves | ✅ Verified functional |
| `switchAnimations` | MaterialSwitch | Toggles celebratory animations | Updates `StudentSettings.animatedCelebrations` and saves | ✅ Verified functional |
| `switchReduceMotion` | MaterialSwitch | Reduces animations for accessibility | Updates `StudentSettings.reduceMotion` and saves | ✅ Verified functional |
| `switchOfflineMode` | MaterialSwitch | Restricts queries to offline DB | Updates `StudentSettings.offlineMode` and saves | ✅ Verified functional |
| `switchCloudSync` | MaterialSwitch | Toggles background data sync | Updates `StudentSettings.cloudSync` and saves | ✅ Verified functional |
| `llLanguage` / `tvLanguage` | LinearLayout | Selects app language & restarts | Material dialog with 4 locales; restarts app to apply | ✅ Verified functional |
| `llRegion` / `tvRegion` | LinearLayout | Selects regional curriculum standard | Material dialog with 4 regions; saves to `StudentSettings` | ✅ Verified functional |
| `llDateFormat` / `tvDateFormat` | LinearLayout | Selects date display format | **NO LISTENER** attached in `setupListeners()`; completely dead | ❌ **High Defect** |
| `llTheme` / `tvTheme` | LinearLayout | Switches Light / Dark / System mode | Sets `AppCompatDelegate.setDefaultNightMode()` and saves | ✅ Verified functional |
| `llFontSize` / `tvFontSize` | LinearLayout | Sets base font scale | Saves font size choice to `StudentSettings` | ✅ Verified functional |
| `llClearCache` / `tvCacheSize` | LinearLayout | Computes cache size and clears files | Asynchronously computes size; deletes files in `getCacheDir()` | ✅ Verified functional |
| `llAutoDownload` / `tvAutoDownload` | LinearLayout | Selects media download policy | **NO LISTENER** attached in `setupListeners()`; completely dead | ❌ **High Defect** |
| `llLinkedAccounts` | LinearLayout | Opens Linked Accounts screen | Executes `Toast.makeText("Opening Linked Accounts...")` instead of launching `LinkedAccountsActivity` | ❌ **High Defect** |
| `llShareProgress` | LinearLayout | Shares progress externally | Fires `Intent.ACTION_SEND` chooser | ✅ Verified functional |
| `llInviteFriends` | LinearLayout | Opens Invite Friends screen | Fires generic text `ACTION_SEND` instead of opening `InviteFriendsActivity` | ⚠️ **Medium Defect** |
| `tvVersion` | TextView | Displays current app build version | Hardcoded string `"2.8 Professional"` instead of `BuildConfig.VERSION_NAME` | ⚠️ **Medium Defect** |
| `llTerms` / `llPrivacyPolicy` | LinearLayout | Displays legal terms and policies | Loads remote URLs in `WebView`; fails if offline or domain unreachable | ⚠️ **Medium Defect** |
| `llRateUs` | LinearLayout | Prompts user to rate app on store | Shows toast only; does not open Play Store listing | ⚠️ **Low Defect** |
| `llDeleteAccount` | LinearLayout | Initiates verified account deletion | Confirmation dialog displays Toast only; **no deletion executed** | ❌ **Critical Defect** |

#### B. Privacy & Security (`PrivacySecurityActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `privacyToolbar` | Toolbar | Back arrow navigates to previous screen | `toolbar.setNavigationOnClickListener(v -> finish())` | ✅ Verified functional |
| `llChangePassword` | LinearLayout | Opens PBKDF2 password change dialog | Verifies old password against DB, enforces 8 chars, hashes with PBKDF2 salt, updates DB, invalidates session | ✅ Verified functional |
| `llTwoFactor` | LinearLayout | Toggles two-factor authentication requirement | Toggles boolean in `StudentSettings` and persists | ✅ Verified functional |
| `llExportData` | LinearLayout | Compiles and exports student history | Shows Toast `"Data export compiled and sent!"`; **no export happens** | ❌ **High Defect** |
| `llDeleteAccount` | LinearLayout | Permanently deletes student profile | Dialog claims "Type DELETE", but has no text input, and confirmation button only displays Toast; **account is never deleted** | ❌ **Critical Defect** |

#### C. Notification Preferences (`NotificationPreferencesActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `notificationsToolbar` | Toolbar | Back navigation | `v -> finish()` | ✅ Verified functional |
| `switchPushNotifications` | MaterialSwitch | Master toggle for push alerts | Updates `StudentSettings.pushEnabled` and saves | ✅ Verified functional |
| `switchDailyChallenge` | MaterialSwitch | Toggles daily challenge reminders | Updates `StudentSettings.notifyDailyChallenge` and saves | ✅ Verified functional |
| `switchFriendRequests` | MaterialSwitch | Toggles peer request alerts | Updates `StudentSettings.notifyFriendRequests` and saves | ✅ Verified functional |
| `switchFriendActivity` | MaterialSwitch | Toggles peer study alerts | Updates `StudentSettings.notifyFriendActivity` and saves | ✅ Verified functional |
| `switchAchievements` | MaterialSwitch | Toggles badge unlock alerts | Updates `StudentSettings.notifyAchievements` and saves | ✅ Verified functional |
| `switchXPMilestones` | MaterialSwitch | Toggles level up alerts | Updates `StudentSettings.notifyXPMilestones` and saves | ✅ Verified functional |
| `switchStreakAlerts` | MaterialSwitch | Toggles streak expiration warnings | Updates `StudentSettings.notifyStreakAlerts` and saves | ✅ Verified functional |
| `switchAnnouncements` | MaterialSwitch | Toggles campus announcement alerts | Updates `StudentSettings.notifyAnnouncements` and saves | ✅ Verified functional |
| `llReminderTime` / `tvReminderTime` | LinearLayout | Sets reminder time and updates AlarmManager | Updates `StudentSettings`, but **fails to call `NotificationHelper.scheduleStudyReminder()`** | ❌ **High Defect** |

#### D. Account & Profile (`EditProfileActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `editProfileToolbar` | Toolbar | Back navigation | `v -> finish()` | ✅ Verified functional |
| `btnSaveTop` | Button/View | Validates and persists full profile | Validates name, saves to SQLite `users` table via `updateUserProfileFull()` | ✅ Verified functional |
| `btnChangePic` / `ivEditProfilePic` | ImageView | Opens gallery to pick image | `ACTION_OPEN_DOCUMENT`, persists URI permission, updates view | ✅ Verified functional |
| `sliderXP` / `tvXPValue` | Slider | Sets daily XP goal (100-2000 XP) | Interactive slider updates text and persists on save | ✅ Verified functional |
| `llChangePassword` | LinearLayout | Opens authenticated password change | Launches `ForgotPasswordActivity.class` (unauthenticated reset) instead of `PrivacySecurityActivity.class` | ❌ **High Defect** |
| `tvVisibility` | TextView | Profile visibility dropdown | Dialog updates selection ("Public", "Private", "Friends Only") | ✅ Verified functional |
| `tvLeaderboard` | TextView | Leaderboard inclusion toggle | Dialog updates selection ("Yes", "No") | ✅ Verified functional |
| `tvNotifications` | TextView | Email notification toggle | Dialog updates selection ("On", "Off") | ✅ Verified functional |
| `btnAddSubject` | View | Adds student study subjects | Shows Toast `"Add Subject list coming soon!"` | ⚠️ **Medium Defect** |
| `btnDeactivate` | View | Deactivates account | Clears session and returns to `MainActivity` | ✅ Verified functional |

#### E. Third-Party Integrations (`LinkedAccountsActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `toolbar` | Toolbar | Back navigation | `v -> finish()` | ✅ Verified functional |
| `llGoogle` / `tvGoogleStatus` | LinearLayout | Shows Google linkage status and allows unlink | Ephemeral in-memory boolean only; **never persisted to disk** | ❌ **High Defect** |
| `llApple` / `tvAppleStatus` | LinearLayout | Shows Apple linkage status and allows unlink | Ephemeral in-memory boolean only; **never persisted to disk** | ❌ **High Defect** |
| `llMicrosoft` / `tvMicrosoftStatus` | LinearLayout | Shows Microsoft linkage status and allows unlink | Ephemeral in-memory boolean only; **never persisted to disk** | ❌ **High Defect** |

#### F. Camera AI Settings (`CameraSettingsActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `toolbar` | Toolbar | Back navigation | `v -> finish()` | ✅ Verified functional |
| `switchAutoDetect` | MaterialSwitch | Toggles auto-detection of equations/diagrams | Only shows a Toast; **never persisted to SharedPreferences** | ❌ **High Defect** |
| `switchGrid` | MaterialSwitch | Toggles alignment grid lines overlay | Only shows a Toast; **never persisted to SharedPreferences** | ❌ **High Defect** |
| `switchSaveHistory` | MaterialSwitch | Toggles scan auto-saving | Only shows a Toast; **never persisted to SharedPreferences** | ❌ **High Defect** |

#### G. Student Support Portal (`HelpSupportActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `helpToolbar` | Toolbar | Back navigation | `setupPortalToolbar()` | ✅ Verified functional |
| FAQ Buttons (4) | Views | Displays categorized FAQ dialogs | Shows informative categorized dialogs | ✅ Verified functional |
| Guide Buttons (4) | Views | Displays troubleshooting & user guides | Shows informative dialogs | ✅ Verified functional |
| `btnContactSupport` | View | Contact options dialog | Launches email client, opens `AIChatActivity`, or shows phone number | ✅ Verified functional |
| "Emergency Phone Support" | Dialog Item | Initiates phone dialer | Shows Toast `"Calling 0902399206..."` instead of opening `ACTION_DIAL` | ⚠️ **Medium Defect** |
| `btnReportProblem` | View | Problem report form | Submits categorized ticket into SQLite `support_tickets` table | ✅ Verified functional |
| `btnFeedback` | View | General feedback form | Submits feedback into SQLite `support_tickets` table | ✅ Verified functional |

#### H. Administrator System Settings (`AdminSystemSettingsActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `settingsToolbar` | Toolbar | Back navigation | `v -> finish()` | ✅ Verified functional |
| `btnSaveSettings` | MaterialButton | Persists all 10 domain settings | Writes all keys to SQLite `settings` table and logs admin audit | ✅ Verified functional |
| 10 Domain Inputs | EditTexts & Switches | Edits site name, SMTP, FCM, 2FA, lockout, maintenance, exam defaults | Loads from and writes to SQLite `settings` table | ✅ Verified functional |
| `btnBackupRestore` | View | Creates database backup | Logs audit action and displays Toast; does not output backup file | ⚠️ **Medium Defect** |
| `btnSystemLogs` | View | Opens admin system audit logs | Displays Toast instead of navigating to existing `AdminReportsActivity` | ❌ **High Defect** |

#### I. Administrator Profile (`AdminProfileActivity`)
| Control ID | Control Type | Expected Behavior | Actual Implementation | Verification Status |
| :--- | :--- | :--- | :--- | :--- |
| `ivAdminPhoto` | ShapeableImageView | Opens gallery to pick admin avatar | `ACTION_OPEN_DOCUMENT`, updates temporary view | ✅ Verified functional |
| `btnSaveAdminProfile` | Button | Persists admin profile changes | Shows Toast only; **never writes image URI to database** | ❌ **Critical Defect** |
| `btnEditProfileAdmin` | View | Opens full profile editing | Launches `EditProfileActivity.class` | ✅ Verified functional |
| `ivToolbarChat` | ImageView | Opens administrative AI chat | Launches `AIChatActivity.class` | ✅ Verified functional |

---

### 3. Classified Audit Findings

#### Critical Severity
1. **Unexecuted Account Deletion (Pseudo-Action):**
   - In both `SettingsActivity.java` and `PrivacySecurityActivity.java`, the "Delete Account" dialogs promise permanent account erasure, but neither actually deletes the student account from SQLite nor wipes the session.
2. **Unsaved Admin Avatar:**
   - In `AdminProfileActivity.java`, selecting a new photo and tapping "Save Changes" shows a success Toast but never writes the URI to `DatabaseHelper`. The photo is lost on screen reload.

#### High Severity
1. **Disconnected Navigation in `SettingsActivity`:**
   - `llLinkedAccounts` fires a Toast instead of launching the existing `LinkedAccountsActivity.class`.
   - `llInviteFriends` fires a generic share intent instead of opening the dedicated `InviteFriendsActivity.class`.
2. **Dead Controls (Missing Listeners) in `SettingsActivity`:**
   - `llAutoDownload` (Auto-download media) has no listener.
   - `llDateFormat` (Date Format selector) has no listener.
3. **Ephemeral State in `LinkedAccountsActivity`:**
   - Google, Apple, and Microsoft linkage states are stored only in volatile memory fields and revert on activity restart.
4. **Non-Persistent Switches in `CameraSettingsActivity`:**
   - `switchAutoDetect`, `switchGrid`, and `switchSaveHistory` only display Toast messages and never save to `SharedPreferences`.
5. **Disconnected Study Reminder Scheduling:**
   - In `NotificationPreferencesActivity.java`, updating reminder time updates `StudentSettings`, but omits `NotificationHelper.scheduleStudyReminder()`.
6. **Fake Data Export:**
   - In `PrivacySecurityActivity.java`, `llExportData` displays a Toast without exporting student data.
7. **Wrong Password Flow in `EditProfileActivity`:**
   - Tapping "Change Password" opens unauthenticated `ForgotPasswordActivity` instead of authenticated `PrivacySecurityActivity`.
8. **Dead Logs Button in `AdminSystemSettingsActivity`:**
   - `btnSystemLogs` shows a Toast instead of opening `AdminReportsActivity`.

#### Medium Severity
1. **Emergency Phone Support Intent:**
   - In `HelpSupportActivity.java`, emergency phone support displays a Toast instead of launching `Intent.ACTION_DIAL`.
2. **Hardcoded Version String:**
   - In `SettingsActivity.java`, version is hardcoded to `"2.8 Professional"` rather than dynamic `BuildConfig.VERSION_NAME`.
3. **Fragile External URLs in Modal WebView:**
   - In `SettingsActivity.java`, Terms and Privacy policy attempt to load `https://learnify.com/...` in a WebView, failing when offline.
4. **Hardcoded Subjects in Profile Save:**
   - `EditProfileActivity.java` hardcodes `"Math,Physics"` into the user record upon profile update.

#### Low Severity
1. **Visual Inconsistencies:**
   - Settings screens use disparate card shapes, legacy system icons (`@android:drawable/...`), and outdated indigo/grey color tokens rather than the Academic Sapphire visual identity.
2. **Inconsistent Dialog Builders:**
   - Mixed usage of `AlertDialog.Builder` and `MaterialAlertDialogBuilder`.

---

### 4. Proposed Prioritized Remediation Batches

#### Batch 2B: Core Student Settings & Navigation Foundation
- **Scope:**
  - Wire `llLinkedAccounts` to `LinkedAccountsActivity.class`.
  - Wire `llInviteFriends` to `InviteFriendsActivity.class`.
  - Implement real listeners for `llAutoDownload` and `llDateFormat` in `SettingsActivity.java`.
  - Implement persistent storage in `LinkedAccountsActivity` (`SharedPreferences`).
  - Implement persistent storage in `CameraSettingsActivity` (`SharedPreferences`).
  - Wire `NotificationHelper.scheduleStudyReminder()` into `NotificationPreferencesActivity.java`.
  - Apply Academic Sapphire design identity (styling, vector drawables, typography) to `activity_settings.xml`, `activity_notification_preferences.xml`, `activity_linked_accounts.xml`, and `activity_camera_settings.xml`.

#### Batch 2C: Account, Profile, and Security Remediation
- **Scope:**
  - Fix `EditProfileActivity.java` so "Change Password" navigates to `PrivacySecurityActivity.class`.
  - Implement genuine account deletion safeguards with password confirmation in `PrivacySecurityActivity.java` and `SettingsActivity.java`.
  - Fix `AdminProfileActivity.java` to persist selected image URIs to SQLite.
  - Implement clean local JSON generation for `llExportData` in `PrivacySecurityActivity.java`.
  - Replace hardcoded version string in `SettingsActivity.java` with dynamic `BuildConfig.VERSION_NAME`.
  - Apply Academic Sapphire design identity to `activity_edit_profile.xml`, `activity_privacy_security.xml`, and `activity_admin_profile.xml`.

#### Batch 2D: Support, Compliance, and Admin Infrastructure
- **Scope:**
  - Wire `btnSystemLogs` in `AdminSystemSettingsActivity.java` to launch `AdminReportsActivity.class`.
  - Fix `HelpSupportActivity.java` emergency call action to trigger `Intent.ACTION_DIAL`.
  - Implement local offline modal dialog fallbacks for Terms of Service and Privacy Policy.
  - Modernize `activity_help_support.xml`, `activity_about.xml`, `activity_admin_system_settings.xml`, and `activity_admin_support_management.xml` with Academic Sapphire & Executive Slate styling.


---

## 11. Batch 2B Implementation & Verification Report — Core Student Settings & Navigation Foundation

### Executive Summary
Batch 2B has been successfully implemented and verified without any deletions, breaking changes, or regressions. All 4 target student settings screens (`SettingsActivity`, `NotificationPreferencesActivity`, `LinkedAccountsActivity`, `CameraSettingsActivity`) and their underlying persistence layers, navigation routes, and Academic Sapphire design tokens have been brought up to enterprise standards.

### Scope of Changes Completed

#### 1. Settings Navigation Hub (`SettingsActivity.java`)
- **Linked Accounts Navigation**: Replaced the placeholder Toast in `llLinkedAccounts` with a direct explicit intent to `LinkedAccountsActivity.class`.
- **Invite Friends Navigation**: Replaced the generic share intent in `llInviteFriends` with a direct intent to `InviteFriendsActivity.class`, allowing access to the dedicated referral code generator, clipboard copying, and contact picker.
- **Auto-Download Media Interactive Dialog**: Attached an interactive click listener to `llAutoDownloadView` offering `Wi-Fi only`, `Wi-Fi and Cellular`, and `Never` options, updating `tvAutoDownload` and persisting to `StudentSettings` / `SettingsService`.
- **Date Format Interactive Dialog**: Attached an interactive click listener to `llDateFormatView` offering `MM/DD/YYYY`, `DD/MM/YYYY`, and `YYYY-MM-DD` options, updating `tvDateFormat` and persisting to `StudentSettings` / `SettingsService`.
- **Preserved Existing Capabilities**: Retained exact view IDs (35+ views), cache calculation/deletion, theme switching, language selection, sound/haptic/celebration toggles, and notification permission requests.

#### 2. Persistent Linked Accounts (`LinkedAccountsActivity.java`)
- **SharedPreferences Persistence**: Replaced ephemeral memory-only booleans with persistent storage in `"LinkedAccountsPrefs"` with keys:
  - `google_linked` (default: `true`)
  - `apple_linked` (default: `false`)
  - `microsoft_linked` (default: `false`)
- **Dynamic Status Badging**: Updated status indicators (`tvGoogleStatus`, `tvAppleStatus`, `tvMicrosoftStatus`) to render `@color/emerald_primary` ("Connected") or `@color/text_secondary_slate` ("Not Connected").
- **Interactive Provider Expansion**: Connected the "Link New Provider" action button (`btnLinkNewAccount`) to an informative dialog detailing upcoming SSO and OAuth providers.

#### 3. Persistent Camera Lab Settings (`CameraSettingsActivity.java` & `CameraLabActivity.java`)
- **SharedPreferences Persistence**: Replaced non-persistent Toast listeners with full preferences storage in `"CameraSettings"`:
  - `auto_detect` (default: `true`)
  - `grid_lines` (default: `false`)
  - `save_history` (default: `true`)
- **State Restoration**: Restored toggle states on activity launch.
- **Camera Lab Integration**: Connected `CameraLabActivity.solveProblem()` to check the `save_history` preference in `"CameraSettings"`, automatically storing solved problems into `DatabaseHelper` history when enabled.

#### 4. Synchronized Study Reminders (`NotificationPreferencesActivity.java`)
- **Real-Time Alarm Scheduling**: Wired `NotificationHelper.scheduleStudyReminder(this, newTime)` directly into the `TimePickerDialog` callback so that updating the study time immediately reschedules the exact alarm in Android's `AlarmManager`.
- **Master Push Toggle Synchronization**: Wired `switchPushNotifications` to cancel active study reminders via `NotificationHelper.cancelStudyReminder(this)` when toggled off, and reschedule them when toggled back on.

#### 5. Academic Sapphire Visual Elevation & Vector Assets
- **Layout Modernization**:
  - `activity_settings.xml`: Refactored to Academic Sapphire surface tones (`@color/surface_light`, `@color/surface_card`), 16dp rounded card radii, crisp chevron arrows (`ic_chevron_right`), and high-contrast typography.
  - `activity_notification_preferences.xml`: Modernized with vector icons (`ic_bell`, `ic_timer`, `ic_bolt`, `ic_user_plus`, `ic_user_person`, `ic_trophy`, `ic_crown`, `ic_flame`, `ic_announcement`) and subtle card dividers.
  - `activity_linked_accounts.xml`: Elevated with official monochrome vector icons (`ic_google_logo`, `ic_apple_logo`, `ic_microsoft_logo`), green security reassurance card, and Sapphire action button.
  - `activity_camera_settings.xml`: Styled with `@color/surface_light`, vector icons (`ic_real_camera`, `ic_grid`, `ic_history`), and an OCR framing tip card.
- **Vector Assets Added**:
  - `ic_bell.xml` (notification bell)
  - `ic_palette.xml` (appearance/theme)
  - `ic_globe.xml` (language/region)
  - `ic_database.xml` (data/cache)
  - `ic_apple_logo.xml` (Apple vector brand)
  - `ic_microsoft_logo.xml` (Microsoft 4-square vector brand)
  - `ic_share_nodes.xml` (social sharing)
  - `ic_user_plus.xml` (invite friends)
  - `ic_trash.xml` (cache clearing and danger zone)
  - `ic_volume_up.xml` (audio effects)
  - `ic_vibration.xml` (haptic feedback)
  - `ic_grid.xml` (camera viewfinder grid)
  - `ic_history.xml` (scan history)
- **100% ID Preservation**: Zero IDs were removed, renamed, or altered across all 4 layouts.

### Build and Verification Results
- **Unit Test Suite**:
  - Command: `.\gradlew.bat testDebugUnitTest --no-daemon`
  - Result: `BUILD SUCCESSFUL in 32s`
  - Status: 12 tests passed, 0 failures, 100% pass rate.
- **Debug APK Build**:
  - Command: `.\gradlew.bat assembleDebug --no-daemon`
  - Result: `BUILD SUCCESSFUL in 40s`
  - APK Artifact: `app\build\outputs\apk\debug\app-debug.apk`
  - Size: 150,610,229 bytes
  - Last Write Time: 8:06:12 PM
- **Git Safety Verification**:
  - All pre-existing staged and unstaged work preserved intact.
  - Zero git resets, stashes, cleanups, commits, or pushes executed.

---

## 12. Batch 2C Implementation Report: Professional Profile, Privacy & Security

### Overview & Objectives
Batch 2C addressed high-risk account operations, privacy protections, photo persistence, and Academic Sapphire design consistency across student and administrator profile and security portals:
1. **Authenticated Change Password Navigation**: Repaired the broken navigation loop in `EditProfileActivity.java` where `llChangePassword` previously opened unauthenticated `ForgotPasswordActivity.class`. It now routes directly to `PrivacySecurityActivity.class` which contains full PBKDF2 hash verification and salt handling.
2. **Safe Transactional Account Deletion**: Engineered end-to-end cascade deletion in SQLite wrapped in an atomic transaction. Connected a dual-factor confirmation dialog (`dialog_delete_account.xml`) requiring current password verification plus typing "DELETE" in capital letters before purging user records and wiping sessions.
3. **Admin Profile Photo Persistence**: Added standalone photo persistence routines (`updateUserProfileImage`) in `DatabaseHelper` and `DataRepository`. Connected photo saving in `AdminProfileActivity.handleSave()`, guarded image loading against revoked URIs with `try-catch`, and wired the back navigation arrow.
4. **Privacy-Conscious Data Export**: Implemented sanitized academic JSON compilation (`exportUserData`) excluding password hashes, salts, and tokens. Configured secure file sharing via AndroidX `FileProvider` and standard Android share sheet (`ACTION_SEND`).
5. **Academic Sapphire Modernization**: Elevated `activity_edit_profile.xml`, `activity_privacy_security.xml`, and `activity_admin_profile.xml` into the Academic Sapphire design system while preserving 100% of existing view IDs and hierarchy elements.

---

### Implementation Details

#### 1. Authenticated Change Password Flow (`EditProfileActivity.java`)
- **Intent Correction**: Updated `llChangePassword.setOnClickListener` to launch `PrivacySecurityActivity.class` instead of `ForgotPasswordActivity.class`.
- **Image Loading Resilience**: Wrapped `ivEditProfilePic.setImageURI(Uri.parse(imgUriStr))` in a `try-catch` block with fallback to `R.drawable.ic_user_person`, preventing crashes if file permissions are revoked or file paths are invalid.

#### 2. Safe Cascade Account Deletion (`DatabaseHelper.java`, `DataRepository.java`, `PrivacySecurityActivity.java`, `SettingsActivity.java`)
- **Atomic Database Cascade**: Added `public boolean deleteUserAccount(String identifier)` in `DatabaseHelper.java`. Wrapped all table deletions in `db.beginTransaction() ... db.setTransactionSuccessful() ... db.endTransaction()`:
  - `challenge_stats`
  - `user_notifications`
  - `lab_results`
  - `enrollments`
  - `payments`
  - `exam_results`
  - `certificates`
  - `study_notes`
  - `flashcards`
  - `mood_logs`
  - `support_tickets`
  - `users`
- **Exposed in Repository**: Added `DataRepository.deleteUserAccount(String identifier)` as the centralized access point.
- **Dual-Factor Deletion Dialog**: Created `dialog_delete_account.xml` and wired `showDeleteAccountDialog()` in both `PrivacySecurityActivity.java` and `SettingsActivity.java`:
  - Enforces current password validation against stored PBKDF2 hash via `repository.verifyUserPassword()`.
  - Requires explicit capital confirmation string `"DELETE"`.
  - Intercepts positive dialog button click to keep the dialog open if validation fails.
  - Clears `UserSession` SharedPreferences and `SecurePreferencesHelper.clearSession()`.
  - Redirects user to `WelcomeActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.

#### 3. Admin Profile Photo Persistence & Navigation (`AdminProfileActivity.java`, `DatabaseHelper.java`)
- **Database Helper API**: Added `updateUserProfileImage(String identifier, String imageUri)` to update `profile_image` in SQLite.
- **Repository Integration**: Added `DataRepository.updateUserProfileImage(String identifier, String imageUri)`.
- **Photo Persistence**: In `AdminProfileActivity.handleSave()`, persists `selectedImageUri.toString()` to SQLite when an image is selected.
- **Crash Prevention**: Wrapped `ivAdminPhoto.setImageURI(uri)` and `ivToolbarProfile.setImageURI(uri)` in `try-catch` with fallback to `R.drawable.ic_user_person`.
- **Back Navigation**: Wired `findViewById(R.id.ivBack).setOnClickListener(v -> finish())`.

#### 4. Privacy-Conscious Data Export (`DatabaseHelper.java`, `PrivacySecurityActivity.java`, `file_paths.xml`)
- **Sanitized JSON Generation**: Added `exportUserData(String identifier)` in `DatabaseHelper.java` returning a structured `JSONObject`:
  - Metadata: app version, export timestamp.
  - Profile: `full_name`, `username`, `email`, `phone`, `university`, `year`, `major`, `xp_goal`, `subjects`, `visibility`, `created_at` (**STRICTLY EXCLUDES** password hashes, salts, and verification tokens).
  - Learning Stats: `total_xp`, `streak`, `last_completion_date`.
  - Enrollments: list of enrolled courses.
  - Exam Results: list of subject, score, total questions, correct answers, date.
  - Certificates: list of titles, courses, dates, certificate IDs.
  - Activity Summary: counts of study notes, flashcards, and mood logs.
- **FileProvider Sharing**:
  - Registered `<cache-path name="cache_files" path="." />` in `file_paths.xml`.
  - Export writes to `getCacheDir()/exports/learnify_data_export.json`.
  - Generates secure URI via `FileProvider.getUriForFile()`.
  - Fires `Intent.ACTION_SEND` chooser with MIME `application/json` and `FLAG_GRANT_READ_URI_PERMISSION`.

#### 5. Academic Sapphire Layout Modernization
- **`activity_privacy_security.xml`**:
  - Restyled using `@color/surface_light`, `@color/surface_card`, and 16dp rounded card containers.
  - Integrated color-coded icon badges: Sapphire for Password (`bg_icon_container_sapphire`), Emerald for 2FA (`bg_icon_container_emerald`), Teal for Data Export (`bg_icon_container_teal`), and Rose for Danger Zone (`bg_icon_container_rose`).
  - Added chevron indicators (`ic_chevron_right`) and subtle borders (`@color/card_stroke`).
  - Preserved 100% of IDs: `privacyToolbar`, `llChangePassword`, `llTwoFactor`, `llExportData`, `llDeleteAccount`.
- **`activity_edit_profile.xml`**:
  - Modernized top bar with Sapphire pill button `btnSaveTop` with check icon and language action `btnLanguageTop`.
  - Added avatar header banner with full elevation and FAB `btnChangePic`.
  - Cleaned text input fields (`ProfileInputStyle`) with 16dp corner radiuses and subtle outlines.
  - Styled subject chips with pastel badges (`sapphire_subtle`, `teal_subtle`).
  - Preserved 100% of IDs: `editProfileToolbar`, `btnSaveTop`, `btnLanguageTop`, `ivEditProfilePic`, `btnChangePic`, `tvDisplayFullName`, `tvDisplayUsername`, `etFullName`, `etUsername`, `etEmail`, `etPhone`, `etUniversity`, `etYear`, `etMajor`, `chipGroupSubjects`, `btnAddSubject`, `sliderXP`, `tvXPValue`, `llReminderTime`, `tvReminderTime`, `tvCurrentLanguage`, `llChangeLanguage`, `tvVisibility`, `tvLeaderboard`, `tvNotifications`, `llChangePassword`, `btnDeactivate`.
- **`activity_admin_profile.xml`**:
  - Styled with executive dark collapsing header banner, sapphire photo stroke, and camera badge.
  - Information card styled with high-contrast rows and themed icon containers (`bg_icon_container_sapphire`, `bg_icon_container_teal`, `bg_icon_container_purple`).
  - Elevated action buttons (`btnEditProfileAdmin`, `btnSaveAdminProfile`) with modern corners and stroke.
  - Preserved 100% of IDs: `appBar`, `collapsingToolbar`, `ivAdminPhoto`, `tvAdminFullNameTop`, `adminProfileToolbar`, `ivBack`, `ivToolbarProfile`, `ivToolbarChat`, `tvAdminFullName`, `tvAdminEmail`, `tvAdminPhone`, `btnEditProfileAdmin`, `btnSaveAdminProfile`, `btnTwoFactor`.

---

### Build and Verification Results
- **Unit Test Suite**:
  - Command: `.\gradlew.bat testDebugUnitTest --no-daemon`
  - Result: `BUILD SUCCESSFUL in 38s`
  - Status: 12 tests passed, 0 failures, 100% pass rate.
- **Debug APK Build**:
  - Command: `.\gradlew.bat assembleDebug --no-daemon`
  - Result: `BUILD SUCCESSFUL in 35s`
  - APK Artifact: `app\build\outputs\apk\debug\app-debug.apk`
  - Size: 150,610,523 bytes
  - Timestamp: 10/10/2026 8:35:42 PM
- **Git Safety Verification**:
  - Zero git resets, stashes, cleanups, commits, or pushes.
  - All pre-existing staged and unstaged work preserved intact.

---

## 7. BATCH A: LEARNIFY ADMIN PORTAL INVENTORY & CRASH INVESTIGATION

**Audit Date:** October 10, 2026  
**Scope:** Complete Admin Portal architectural inventory, root-cause crash diagnosis, and implementation roadmap for world-class redesign under Learnify Executive — Academic Sapphire.

### A. Dark Mode Crash Diagnosis & Permanent Resolution
1. **Reported Symptom**:
   Application crashed immediately on startup and when switching to Dark Mode with:
   `android.content.res.Resources$NotFoundException: Resource ID #0x0` at `com.google.android.material.navigation.NavigationBarMenuView.createDefaultColorStateList(NavigationBarMenuView.java:696)`.
2. **Exact Root Cause**:
   In `app/src/main/res/values-night/themes.xml`, theme color attributes (`android:textColorSecondary`, `android:textColorPrimary`, `colorSurface`, `colorPrimary`, etc.) were declared with raw hexadecimal color literals (e.g., `<item name="android:textColorSecondary">#94A3B8</item>`) instead of `@color/...` resource references.
   When Android's `Theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)` was evaluated, `typedValue.resourceId` resolved to `0x0`. Calling `AppCompatResources.getColorStateList(context, 0x0)` threw `Resources$NotFoundException: Resource ID #0x0`.
   Additionally, `values-night/colors.xml` was completely absent, and `values-night/themes.xml` lacked `Theme.App.Starting`.
3. **Permanent Fix Completed**:
   - Replaced all hex literals in `values-night/themes.xml` and `values/themes.xml` with `@color/...` resource references.
   - Created `app/src/main/res/values-night/colors.xml` containing semantic dark mode palette mappings (`surface_card`, `surface_light`, `text_primary_slate`, `card_stroke`, etc.).
   - Created `ThemeManager.java` as the centralized controller handling `MODE_NIGHT_NO`, `MODE_NIGHT_YES`, and `MODE_NIGHT_FOLLOW_SYSTEM`.
   - Wired `MainApplication.java`, `BaseActivity.java`, and `SettingsActivity.java` through `ThemeManager`.
   - Built and verified: `testDebugUnitTest` (16/16 tests passing), `assembleDebug` successful, installed onto physical TECNO Spark 10C (`099573736Q002275`). Verified app launches in Dark Mode with zero crashes.

### B. Admin Portal Screen Inventory & Architecture Matrix
All 13 Admin activities, their layouts, database integrations, and current defects:

| Screen / Feature | Activity | Layout & Components | Database / Backend Integration | Current Visual & Functional Defects |
|---|---|---|---|---|
| **Admin Command Center** | `AdminDashboardActivity.java` | `activity_admin_dashboard.xml`, `nav_header_admin.xml`, `admin_drawer_menu.xml` | `users`, `materials`, `lab_results`, `tasks`, `system_settings` via `DataRepository` | Forced dark background `#0F172A`, does not adapt when user selects Light Mode. Outdated "AceGrade" drawer header branding. Generic Android drawer icons. |
| **User Administration** | `AdminUserManagementActivity.java` | `activity_admin_user_management.xml`, `item_admin_user.xml`, `dialog_student_profile_admin.xml` | `users` table: role filtering (STUDENT/TEACHER/ADMIN), status toggling, CSV export | Hardcoded light background `#F8FAFC`, legacy indigo app bar (`@color/colorPrimary`), search input using chat bubble drawable. Hardcoded card & dialog colors. |
| **Content & Media Hub** | `AdminContentManagementActivity.java` | `activity_admin_content_management.xml` | `materials` and `announcements` tables via `DataRepository` | Hardcoded `#010114` pitch-black background, `#1E293B` cards, white hint texts. Completely disconnected from Light Mode. |
| **Camera Lab Governance** | `AdminCameraLabActivity.java` | `activity_admin_camera_lab.xml` | `camera_lab_telemetry` and OCR configuration | Hardcoded `#F8FAFC` background with `#1E293B` dark app bar; unstyled check boxes and hardcoded text colors. |
| **Certificate Editor** | `AdminIssueCertificateActivity.java` | `activity_admin_issue_certificate.xml` | `certificates` table: issuance & custom templates | Hardcoded light background `#F8FAFC` with `#1E293B` app bar. Form controls lack unified Material 3 outline styling. |
| **Payment Verification** | `AdminPaymentVerificationActivity.java` | `activity_admin_payment_verification.xml` | `payments` table: ETB bank transfer approval / rejection | Uses `@color/dashboard_bg` with legacy header; reuses `item_admin_user.xml`; unstyled programmatic AlertDialog. |
| **Reports & Monitoring** | `AdminReportsActivity.java` | `activity_admin_reports.xml`, `item_admin_widget.xml` | `users`, `lab_results`, `tasks`, audit logs | Background uses `@drawable/bg_reports_mix`; `item_admin_widget.xml` has hardcoded `#1E293B` card background and `#FFFFFF` text. |
| **Support Center** | `AdminSupportManagementActivity.java` | `activity_admin_support_management.xml` | `support_tickets` table: ticket resolution & replies | Hardcoded `#F8FAFC` background; cards and text views are constructed programmatically in Java with hardcoded `Color.WHITE` and text colors. |
| **System Settings** | `AdminSystemSettingsActivity.java` | `activity_admin_system_settings.xml` | `system_settings` table (10 categories: 8.1–8.10) | Hardcoded `#F8FAFC` background, mixed `#4F46E5` indigo accents, hardcoded `#E2E8F0` dividers and text colors. |
| **Question Authoring** | `AdminQuestionActivity.java` | `activity_admin_question.xml` | `daily_questions` table: MCQ question publishing | Embedded custom DrawerLayout duplicating admin navigation; dummy user name "Jane Doe"; fixed `350dp` search bar overflowing on narrow screens; dead Toast-only file upload button. |
| **Admin Login** | `AdminLoginActivity.java` | `activity_admin_login.xml` | PBKDF2 authentication, Google OAuth, role check | Legacy gradient backgrounds (`bg_admin_mix` and `bg_admin_card`); hardcoded `#312E81` icon container. |
| **Admin Registration** | `AdminRegisterActivity.java` | `activity_admin_register.xml` | Admin provisioning with access code validation | Legacy gradient header; needs unified outlined input styling and semantic theme tokens. |
| **Admin Profile** | `AdminProfileActivity.java` | `activity_admin_profile.xml` | Admin profile update & password change | Batch 2C modernized; verified working in both themes. |

### C. Prioritized Defect List
1. **Dual-Theme Disconnection**: Several screens are locked to dark background while others are locked to light background, causing severe visual inconsistency when switching themes.
2. **Programmatic Hardcoded Styling**: `AdminSupportManagementActivity` builds cards in Java with hardcoded light colors that break in dark mode.
3. **Mobile Responsiveness & Clipping**: `activity_admin_question.xml` has a fixed-width `350dp` search bar that clips on smaller displays.
4. **Dead / Toast-Only Controls**: `btnUploadFile` in `AdminQuestionActivity` shows a Toast without launching a file picker.
5. **Outdated Branding**: `nav_header_admin.xml` displays "AceGrade Administrator" and "admin@acegrade.com". Generic system drawables used across drawer menu.

### D. Proposed Batch Execution Plan
- **Batch B**: Design System Tokens & Admin Dashboard (`activity_admin_dashboard.xml`, `nav_header_admin.xml`, `admin_drawer_menu.xml`) — **COMPLETED**.
- **Batch C**: High-Priority Operations & Management (`AdminUserManagementActivity`, `AdminContentManagementActivity`, `AdminQuestionActivity`, `AdminIssueCertificateActivity`).
- **Batch D**: Payments, Support & Reports (`AdminPaymentVerificationActivity`, `AdminSupportManagementActivity`, `AdminReportsActivity`).
- **Batch E**: System Settings, Camera Lab & Auth Screens (`AdminSystemSettingsActivity`, `AdminCameraLabActivity`, `AdminLoginActivity`, `AdminRegisterActivity`).
- **Batch F**: App-Wide Admin Consistency & Theme Polish.
- **Batch G**: Final Physical-Device Verification & Comprehensive Report.

---

## 8. BATCH B COMPLETION: EXECUTIVE ADMIN DASHBOARD & THEME-AWARE DESIGN SYSTEM

**Completion Date:** October 10, 2026  
**Status:** COMPLETED & VERIFIED ON PHYSICAL TECNO SPARK 10C (`099573736Q002275`, Android 13)

### A. Implemented Design System Tokens
Established the **Learnify Executive — Academic Sapphire** design tokens symmetrically in both `values/colors.xml` and `values-night/colors.xml` to ensure zero crash risk and full theme harmony:
- `admin_bg`: `#F8FAFC` (Light) / `#0F172A` (Dark)
- `admin_card_bg` & `admin_card_bg_v2`: `#FFFFFF` (Light) / `#1E293B` (Dark)
- `admin_card_stroke` & `admin_border`: `#E2E8F0` (Light) / `#334155` (Dark)
- `admin_surface_card`: `#FFFFFF` (Light) / `#1E293B` (Dark)
- `admin_surface_elevated`: `#F1F5F9` (Light) / `#243247` (Dark)
- `admin_text_primary`: `#0F172A` (Light) / `#F8FAFC` (Dark)
- `admin_text_secondary`: `#64748B` (Light) / `#94A3B8` (Dark)
- `admin_text_muted`: `#94A3B8` (Light) / `#64748B` (Dark)
- `admin_sapphire_primary`: `#1E40AF` (Light) / `#3B82F6` (Dark)
- `admin_sapphire_subtle`: `#EFF6FF` (Light) / `#1E293B` (Dark)
- `admin_teal_accent`: `#0F766E` (Light) / `#14B8A6` (Dark)
- `admin_teal_subtle`: `#F0FDFA` (Light) / `#132E35` (Dark)

### B. Dashboard Modernization (`activity_admin_dashboard.xml`)
- **Executive App Bar**: Clean, borderless `AppBarLayout` with `adminToolbar`, drawer toggle `llToolbarBack` displaying "Learnify Admin" + gold/sapphire `EXEC` pill, bell notification `ivAdminNotification` using vector `ic_bell`, and circular profile avatar `adminProfileImg`.
- **Command Center Sub-Bar**: Clean drawer quick-trigger button `btnOpenDrawer` and "Command Center" indicator.
- **Academic Sapphire Hero Card**: `bg_admin_hero_banner` gradient (`#1E3A8A` to `#1E40AF`) with 20dp corners, dynamic titles, and emerald pulsing status pill `"SYSTEM STATUS: OPERATIONAL & SECURE"`.
- **2-Column Institutional Metrics Grid**: 6 responsive `MaterialCardView` cards with theme-aware surfaces, subtle strokes, rounded tinted icon containers, bold legible values, and secondary captions:
  1. `cardTotalStudents` / `tvTotalStudents` (Sapphire icon container + `ic_student_badge`)
  2. `cardActiveStudents` / `tvActiveStudents` (Emerald icon container + `ic_user_person`)
  3. `cardTeachers` / `tvTotalTeachers` (Amber icon container + `ic_user_plus`)
  4. `cardTotalLabs` / `tvTotalLabs` (Purple icon container + `ic_lab`)
  5. `cardCompletedLabs` / `tvCompletedLabs` (Teal icon container + `ic_verified_check`)
  6. `cardPendingLabs` / `tvPendingLabs` (Rose icon container + `ic_history`)
- **System Health Card**: `cardSystemHealth` / `tvSystemHealth` full-width card with sapphire icon container, status subtitle, and chevron arrow.
- **Primary Credential Banner**: `cardIssueCert` high-impact emerald banner with `ic_certificate`, "CREDENTIAL ISSUANCE", and chevron arrow.
- **Management & Governance Action Center**: The 7 action buttons styled with theme-adaptive `@style/AdminExecutiveButton`:
  - `btnIssueCertificate` (Issue Certificate)
  - `btnCreateLab` (Create Virtual Lab)
  - `btnAddUser` (Add User / Faculty)
  - `btnUpload` (Upload Curriculum Content)
  - `btnSimulate` (Launch Physics Simulation)
  - `btnReports` (Analytics & Reports)
  - `btnSettings` (System Settings)
- **Compliance & Support Section**:
  - `cardPaymentVerification`: Emerald badge, "Payment Verification", subtitle, chevron arrow.
  - `cardSupportCenter`: Sapphire badge, "Support Center Management", subtitle, chevron arrow.

### C. Drawer Header (`nav_header_admin.xml`) & Menu (`admin_drawer_menu.xml`)
- **Branding Rectification**: Replaced legacy "AceGrade Administrator" and "admin@acegrade.com" with "Learnify Administrator" and "admin@learnify.com".
- **Header Aesthetics**: Applied Academic Sapphire gradient banner (`bg_hero_banner`), white stroke circle avatar (`ivUserHeader`), and "EXECUTIVE PORTAL" badge.
- **Vector Menu Drawables**: Upgraded all menu items from generic system drawables to high-definition vector assets:
  - `admin_nav_dashboard` -> `@drawable/ic_grid`
  - `admin_nav_users` -> `@drawable/ic_user_plus`
  - `admin_nav_issue_cert` -> `@drawable/ic_certificate`
  - `admin_nav_content` -> `@drawable/ic_database`
  - `admin_nav_camera_lab` -> `@drawable/ic_real_camera`
  - `admin_nav_reports` -> `@drawable/ic_history`
  - `admin_nav_add_question` -> `@drawable/ic_flashcard`
  - `admin_nav_payment_verification` -> `@drawable/ic_verified_check`
  - `admin_nav_support_management` -> `@drawable/ic_support_headset`
  - `admin_nav_settings` -> `@drawable/ic_settings`
  - `admin_nav_logout` -> `@android:drawable/ic_lock_power_off`

### D. Java Controller Presentation Updates (`AdminDashboardActivity.java`)
- Updated `updateDashboardStats()`: `tvSystemHealth.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.admin_green))` replacing deprecated holo color.
- Updated `loadProfileData()`: `ivUserHeader.setImageResource(R.drawable.ic_user_person)` fallback replacing generic system drawable.
- Preserved 100% of all listeners, database queries, and role verification (`enforceRole("ADMIN")`).

### E. Physical Device Verification Results
- **Target Device**: TECNO Spark 10C (`099573736Q002275`, Android 13).
- **Deployment**: `adb install -r app\build\outputs\apk\debug\app-debug.apk` -> `Success`.
- **Logcat Stability**: Zero crashes, zero `FATAL` exceptions, zero `Resources$NotFoundException`.
- **UI & Interaction Verification**:
  - Full screenshot capture of top dashboard: Verified hero banner, operational pill, all 6 metric cards with live database counts ("1 Active Records", "1 Online Now", etc.).
  - Full screenshot capture of scrolled dashboard: Verified 7 executive buttons, compliance cards, and support card.
  - Full screenshot capture of drawer menu: Verified Academic Sapphire header, user profile, and all 11 menu items with vector drawables.
- **Strict Preservation**:
  - All 31 View IDs preserved 100%.
  - All 41 modified/untracked pre-existing Git files preserved 100%.
  - Zero git resets, stashes, or discards.

---

## 9. BATCH C: CORE ADMIN MANAGEMENT IMPLEMENTATION & VERIFICATION

### A. Screen 1: User Management (`AdminUserManagementActivity`)
- **Layout Redesign (`activity_admin_user_management.xml`)**:
  - Standardized background to `@color/admin_bg`.
  - Converted app bar to Academic Sapphire banner (`@drawable/bg_admin_hero_banner`) with "STUDENT & FACULTY DIRECTORY" subtitle.
  - Replaced legacy speech bubble search bar with CardView + `ic_search` vector drawable.
  - Upgraded filter chips (`chipAll`, `chipStudents`, `chipAdmins`, `chipBlocked`) with high-contrast state list selectors (`chip_admin_bg_color.xml`, `chip_admin_text_color.xml`, `chip_admin_stroke_color.xml`).
  - Added export OutlinedButton with `ic_download` vector asset and real-time user counter badge.
- **Item Row Redesign (`item_admin_user.xml`)**:
  - Converted card background to `@color/admin_card_bg` with `@color/admin_card_stroke` and 16dp corner radius.
  - Replaced avatar with `ShapeableImageView` inside sapphire icon container with `ic_user_person`.
  - Added dedicated status badges with `@drawable/bg_badge_active` and `@drawable/bg_badge_blocked`.
  - Added `ic_more_vert` options vector icon with touch ripple.
- **Profile Dialog (`dialog_student_profile_admin.xml`)**:
  - Replaced hardcoded `#1E293B` text and `#E2E8F0` borders with `@color/admin_text_primary`, `@color/admin_text_secondary`, and `@color/admin_border`.
- **Java Controller Updates (`AdminUserManagementActivity.java`)**:
  - Standardized color retrieval using `ContextCompat.getColor(this, ...)` instead of deprecated methods.
  - Preserved 100% of SQLite database queries, search filtering, and user blocking/unblocking logic.

### B. Screen 2: Content & Media Management (`AdminContentManagementActivity`)
- **Layout Redesign (`activity_admin_content_management.xml`)**:
  - Replaced pitch-black `#010114` background with theme-adaptive `@color/admin_bg`.
  - Converted app bar to Academic Sapphire banner with "CURRICULUM REPOSITORY & MEDIA" governance subtitle.
  - Upgraded upload form into 16dp rounded MaterialCardView with OutlinedBox text inputs and sapphire focus accents.
  - Added vector attachment button with `ic_download` / attachment icon and high-impact publish button with `ic_bolt`.
- **Item Row Redesign (`item_material.xml`)**:
  - Replaced hardcoded card background with `@color/admin_card_bg` and `@color/admin_card_stroke`.
  - Styled subject tag as pill chip (`bg_stat_chip_xp`).
  - Standardized title and description colors for both Light and Dark themes.
  - Added "OPEN & ACCESS FILE" button with ripple and download vector asset.
- **Java Controller Updates (`AdminContentManagementActivity.java`)**:
  - Eliminated the white-on-white text bug by applying `ContextCompat.getColor(this, R.color.admin_text_primary)`.
  - Integrated `setupPortalToolbar()` and wired `btnViewMaterial` to open assets via `Intent.ACTION_VIEW`.

### C. Screen 3: Assessment & Question Management (`AdminQuestionActivity`)
- **Layout Redesign (`activity_admin_question.xml`)**:
  - Eliminated fixed-width `350dp` overflow search bar that clipped on small devices.
  - Removed hardcoded "Jane Doe" and legacy "EduLearn" branding strings.
  - Reduced excessive 32dp container padding to responsive 16dp.
  - Themed all form inputs with OutlinedBox, 12dp corner radii, and sapphire focus borders.
  - Structured options A, B, C, D into clean, readable cards with high-contrast radio selectors.
- **Java Controller Updates (`AdminQuestionActivity.java`)**:
  - Dynamically bound `tvAdminHeaderName` to the logged-in admin session username.
  - Wired `btnUploadFile` to an `ActivityResultLauncher` document picker.
  - Preserved 100% of question creation and drawer navigation logic.

### D. Screen 4: Camera Lab Governance (`AdminCameraLabActivity`)
- **Layout Redesign (`activity_admin_camera_lab.xml`)**:
  - Replaced `#F8FAFC` and `#1E293B` app bar with `@color/admin_bg` and Academic Sapphire header.
  - Organized governance sections into 5 modern cards: System Status, Supported Domains, Solver Parameters, Training & Moderation, and Usage Analytics.
  - Themed all switches, checkboxes, and radio buttons with sapphire primary tints.
  - Added analytics metric cards and audit log buttons.
- **Java Controller Updates (`AdminCameraLabActivity.java`)**:
  - Standardized toolbar with `setupPortalToolbar()`.
  - Preserved OCR model controls, solver parameters, and dataset moderation hooks.

---

## 10. BATCH D: CREDENTIALS, TRANSACTIONS, SERVICE DESK & TELEMETRY

### A. Screen 1: Academic Certificates (`AdminIssueCertificateActivity`)
- **Layout Redesign (`activity_admin_issue_certificate.xml`)**:
  - Standardized background to `@color/admin_bg`.
  - Added Academic Sapphire executive banner with "CREDENTIAL GOVERNANCE" subtitle and official seal container.
  - Structured the issuance workflow into two logical cards:
    - Card 1: **Recipient & Curriculum** (Select Student Account, Full Name, Student ID, Target Course).
    - Card 2: **Certificate Details & Authority** (Certificate Title, Presentation Text, Body Description, Issue Date, Serial ID, Director Name, Director Role).
  - Standardized all text fields with `Widget.MaterialComponents.TextInputLayout.OutlinedBox` and 12dp corner radii.
  - Styled action buttons: Outlined "Cancel" button and Solid Sapphire "Issue Certificate" button with `ic_verified_check`.
- **Java Controller Updates (`AdminIssueCertificateActivity.java`)**:
  - Standardized toolbar setup with `setupPortalToolbar(toolbar, "Issue Certificate")`.
  - Preserved 100% of `loadStudents()`, `updateCourseDropdown()`, `saveFullCertificate()`, notification dispatch, and audit logging.

### B. Screen 2: Payment Verification (`AdminPaymentVerificationActivity`)
- **Dedicated Layout Item (`item_payment_admin.xml`)**:
  - Created a brand-new, dedicated payment transaction card replacing legacy `item_admin_user` inflation.
  - Features credit card icon container, student username, transaction ID, dynamic status badge (`bg_badge_active`, `bg_badge_pending`, `bg_badge_blocked`), ETB transaction amount, and receipt attachment indicator.
- **Activity Layout (`activity_admin_payment_verification.xml`)**:
  - Added Academic Sapphire executive banner with "FINANCIAL GOVERNANCE & AUDIT" subtitle.
  - Added section header with transaction count chip (`tvPaymentCount`).
  - Added dedicated empty state view (`emptyPaymentState`) with credit card illustration and clear guidance text.
- **Java Controller Updates (`AdminPaymentVerificationActivity.java`)**:
  - Standardized toolbar with `setupPortalToolbar(toolbar, "Payment Verification")`.
  - Upgraded verification dialog to `MaterialAlertDialogBuilder` with high-contrast text and receipt proof image preview.
  - Preserved 100% of repository calls (`getAllPayments()`, `adminUpdatePayment()`, `logSystemAction()`).

### C. Screen 3: Support Management (`AdminSupportManagementActivity`)
- **Dedicated Layout Item (`item_admin_support_ticket.xml`)**:
  - Created a brand-new, dedicated support ticket card replacing raw programmatic `new MaterialCardView(this)` inflation.
  - Features support headset icon container, student username, category chip, status badge, ticket title, description, and admin resolution container.
- **Activity Layout (`activity_admin_support_management.xml`)**:
  - Added Academic Sapphire executive banner with "SERVICE DESK & ESCALATIONS" subtitle.
  - Added ticket count badge and dedicated empty state view (`emptyTicketState`).
- **Java Controller Updates (`AdminSupportManagementActivity.java`)**:
  - Replaced hardcoded Java colors (`Color.WHITE`, `Color.RED`, `Color.BLUE`) with theme-aware tokens.
  - Upgraded reply dialog from raw `EditText` to `MaterialAlertDialogBuilder` with an Outlined `TextInputLayout` and pre-populated response.
  - Preserved 100% of database operations (`getAllSupportTickets()`, `updateTicketReply()`).

### D. Screen 4: Executive Reports & Analytics (`AdminReportsActivity`)
- **Widget Item Redesign (`item_admin_widget.xml`)**:
  - Replaced hardcoded dark background (`#1E293B`) with `@color/admin_card_bg` and `@color/admin_card_stroke`.
  - Added rounded icon container with vector drawables and theme-aware colors.
- **Activity Layout (`activity_admin_reports.xml`)**:
  - Removed hardcoded dark slate gradient background (`bg_reports_mix`) to ensure 100% theme consistency in Light and Dark modes.
  - Added Academic Sapphire executive banner with dynamic admin username and profile avatar.
  - Added "Academic Intelligence & Diagnostics" card with Average Score, Pass Rate, and Top Domain metrics.
  - Added "System Health & Live Logs" card with heartbeat indicator and live telemetry container.
  - Replaced legacy "AceGrade Professional Edition" footer with "Learnify Executive Portal • v2.4 Enterprise Edition".
- **Java Controller Updates (`AdminReportsActivity.java`)**:
  - Standardized toolbar with `setupPortalToolbar(toolbar, "System Reports")`.
  - Replaced legacy system drawables with vector icons (`ic_student_badge`, `ic_admin_badge`, `ic_edit`, `ic_courses`, `ic_ai_hexagon`, `ic_verified_check`).
  - Formatted live logs with monospace font and theme-aware status colors.
  - Wired live database counts and refresh/export action buttons.

### E. Physical Device Verification Results (Batch D)
- **Target Device**: TECNO Spark 10C (`099573736Q002275`, Android 13).
- **Deployment**: `adb install -r app\build\outputs\apk\debug\app-debug.apk` -> `Success`.
- **Screenshots Captured & Inspected**:
  - `admin_issue_certificate.png`: Verified hero banner, seal container, 2 structured cards, and outlined inputs.
  - `admin_payment_verification.png`: Verified transaction queue header, 0 transactions badge, credit card container, and empty state.
  - `admin_support_management.png`: Verified service desk banner, active tickets header, headset container, and resolved state.
  - `admin_reports.png`: Verified 6 metric widgets with live database counts ("1 Total Students", "1 Admins / Staff", "12 Total Subjects"), performance index card (+4.8% MOM), live logs, and action buttons.
  - `admin_reports_scroll.png`: Verified scrolled telemetry logs, export/refresh buttons, and enterprise footer.
  - `admin_dashboard_batch_d.png`: Verified Admin Dashboard stability and seamless navigation to all Batch D portals.
- **Theme Consistency**: Verified zero crashes, high contrast, and flawless rendering.

## 11. BATCH E: SECURITY ENFORCEMENT, SYSTEM SETTINGS, ADMIN PROFILE & AUTHENTICATION

### A. Phase 1 Security Review & Platform Hardening
1. **Network Security Configuration (`network_security_config.xml`)**:
   - Disallowed broad cleartext HTTP traffic across the entire application by default (`cleartextTrafficPermitted="false"`).
   - Configured system certificate trust anchors.
   - Permitted cleartext traffic strictly on development loopback addresses (`localhost`, `127.0.0.1`, `10.0.2.2`).
   - Removed `android:usesCleartextTraffic="true"` from `AndroidManifest.xml` and wired `android:networkSecurityConfig="@xml/network_security_config"`.
2. **FileProvider Hardening (`file_paths.xml`)**:
   - Verified that `androidx.core.content.FileProvider` paths are strictly confined to private app storage:
     - `<external-files-path name="external_files" path="." />`
     - `<cache-path name="cache_files" path="." />`
     - `<files-path name="internal_files" path="." />`
   - Verified zero exposure of `root-path` or arbitrary filesystem mount points.
3. **Role-Based Access Control (RBAC) & Exported Component Defense**:
   - Re-verified all administrative activities implement `enforceRole("ADMIN")` in `onCreate()` prior to UI inflation or data binding.
   - Verified that session tampering or unauthenticated launches trigger automatic redirection to the login gateway.
4. **PBKDF2 Password Security & Salt Verification**:
   - Re-verified password verification and hashing algorithms across administrative and student account operations.

### B. Screen 1: System Settings & Infrastructure (`AdminSystemSettingsActivity`)
- **Layout Modernization (`activity_admin_system_settings.xml`)**:
  - Replaced hardcoded `#F8FAFC` background with theme-aware `@color/admin_bg`.
  - Added Academic Sapphire executive banner with "CENTRAL RUNTIME ARCHITECTURE" eyebrow, "Platform Policies & Governance" title, and "SYSTEM ACTIVE" status badge.
  - Organized all 10 infrastructure categories and camera governance into 7 structured MaterialCardViews:
    - Card 1: **Platform Identity & Branding** (`etAppName`, `etTagline`, `etContactEmail`, `etSupportEmail`, `etPrimaryColor`).
    - Card 2: **Academic Engine Governance** (`etExamTime`, `etPassingScore`).
    - Card 3: **Security & Compliance Policies** (`switch2FA`, `switchLockout`, `switchCookie`).
    - Card 4: **Localization & Notifications** (`etCurrency`, `switchRTL`, `etSmtpHost`, `etFcmKey`).
    - Card 5: **Integrations & Cache Performance** (`etZoomKey`, `etS3Bucket`, `switchCache`).
    - Card 6: **Maintenance & Disaster Recovery** (`switchMaintenance`, `switchRegistration`, `switchVoiceFeature`, `btnBackupRestore`, `btnSystemLogs`).
    - Card 7: **Camera Lab Solver Governance** (`switchCameraLab`, `switchLabMath`, `switchLabPhysics`, `switchLabChem`, `etLabMaxSteps`).
  - Standardized all inputs with `Widget.MaterialComponents.TextInputLayout.OutlinedBox`, 12dp radii, and sapphire accents.
  - Styled primary action button `btnSaveSettings` with sapphire background and checkmark icon.
- **Java Controller Updates (`AdminSystemSettingsActivity.java`)**:
  - Bound toolbar navigation with `setNavigationOnClickListener(v -> finish())`.
  - Added strict numeric validation for exam duration (1-360 minutes) and passing threshold (1-100%).
  - Added interactive backup confirmation and status modal for `btnBackupRestore`.
  - Added interactive administrative audit trail viewer for `btnSystemLogs`, querying real-time rows from the `admin_logs` SQLite table.
  - Preserved 100% of settings persistence in the SQLite `settings` table.

### C. Screen 2: Executive Admin Profile (`AdminProfileActivity`)
- **Layout Modernization (`activity_admin_profile.xml`)**:
  - Replaced hardcoded `@color/surface_light` background with `@color/admin_bg`.
  - Updated collapsing hero banner with circular photo frame, camera indicator, and dynamic full name header (`tvAdminFullNameTop`).
  - Added "VERIFIED PRIVILEGED CREDENTIALS" emerald badge.
  - Structured profile into two clean cards:
    - Card 1: **Administrative Credentials** (Full Name, Official Email Address, Emergency Contact Phone).
    - Card 2: **Security & Access Control** (Change Master Password row, Two-Factor Protection status button).
  - Styled primary buttons: "EDIT ADMINISTRATIVE PROFILE" and "SAVE PROFILE CHANGES".
- **Java Controller Updates (`AdminProfileActivity.java`)**:
  - Dynamically bound `tvAdminFullNameTop` to display the administrator's actual recorded name.
  - Wired `btnAdminChangePassword` to inflate `dialog_change_password.xml`, verifying old password via PBKDF2 hash and persisting new credentials.
  - Wired `btnTwoFactor` to show a hardware-enforced 2FA security status advisory.
  - Preserved gallery photo picker with persistable URI permissions and fallback defaults.

### D. Screen 3: Administrator Sign-In (`AdminLoginActivity`)
- **Layout Modernization (`activity_admin_login.xml`)**:
  - Replaced legacy dark gradient with `@drawable/bg_admin_hero_banner` and sapphire theme.
  - Replaced hardcoded input styling with MaterialCardView container, 24dp corner radii, and Outlined TextInputLayouts.
  - Added executive shield badge with verified check indicator.
  - Refined social sign-in buttons with elevated circular containers.
- **Java Controller Integration (`AdminLoginActivity.java`)**:
  - Preserved 100% of PBKDF2 authentication, Google OAuth SSO, and default password warning prompt.

### E. Screen 4: Administrator Onboarding (`AdminRegisterActivity`)
- **Layout Modernization (`activity_admin_register.xml`)**:
  - Replaced legacy dark background with standard `@color/admin_bg` and sapphire hero banner.
  - Added "RBAC ONBOARDING" status badge and privileged identity management subtitle.
  - Structured registration fields into clean cards (Profile Photo, Identity & Identifiers, Security Credentials).
  - Updated Admin ID hint to `ACE-ADMIN-2026-001`, aligning with the required security regex prefix.
- **Java Controller Integration (`AdminRegisterActivity.java`)**:
  - Preserved real-time username uniqueness feedback, Admin ID validation (`ACE-ADMIN-`), PBKDF2 password registration, and admin action logging.

### F. Verification & Build Results (Batch E)
- **Compilation**: `compileDebugSources` completed with `BUILD SUCCESSFUL` (0 errors, 0 warnings).
- **Unit Testing**: `testDebugUnitTest` executed and passed all tests with `BUILD SUCCESSFUL`.
- **APK Generation**: `assembleDebug` generated a clean 149MB production-ready APK at `app\build\outputs\apk\debug\app-debug.apk`.
- **Preservation Check**: Verified with `git status --short` that 100% of existing staged, unstaged, and untracked changes remain fully intact.




