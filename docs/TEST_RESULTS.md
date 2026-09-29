# Test results — 29 September 2026

Host: macOS arm64, Zulu JDK17.0.20.1, Gradle8.7, AGP8.6.1, Kotlin2.0.21. Android min26/compile35/target33. Compose BOM2024.10.01. Emulator: Pixel_9_Pro, Android17/API37.2, arm64 16KB page system image; no physical device.

## Passing

`JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home ./gradlew testDebugUnitTest assembleDebug lintDebug connectedDebugAndroidTest`

- 20 unit tests, zero failures: guardrails, policy, demo progression, observation identity, multilingual filtering, task routing.
- Debug APK builds at app/build/outputs/apk/debug/app-debug.apk.
- Lint: zero errors, 57 warnings. Not a warning-free build.
- Four emulator UI tests: typed task→permission setup and disabled action; light/dark Home rendering; Hindi 200% scrolling and task intake; onboarding language and confirmed local-data reset.
- Final screenshot-export/theme-interaction rerun: all four tests passed, zero failures/errors, BUILD SUCCESSFUL. Light, dark and Hindi captures were visually reviewed.
- Screenshots: docs/screenshots/2026-09-29. Captures are Compose content, excluding Android system bars. Screenshots validate sampled views, not all-screen parity.
- Phase-one final unit count was 18 passing (28 September); earlier baseline was seven passing with seven lint errors/59 warnings.

The first emulator run failed before interactions because older Espresso reflected a removed InputManager method. Test dependencies updated to runner1.7.0, junit1.3.0, Espresso3.7.0, following [official AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test). Tests then passed. Screenshot export was changed to additionalTestOutputDir because the runner uninstalls test APKs afterward. Visual inspection found a light image mislabeled dark; the test now switches through Settings and checks both saved preference and rendered background before capturing.

## Not verified / not implemented

- Real-device TalkBack, cross-app touch passthrough, notification Stop, service/overlay races, microphone/TTS release, lock/unlock, permission revocation, process death, landscape/narrow displays, battery and latency: not verified. The selected synthetic goal uses saved state but recreation behavior has not yet been instrumented.
- Hindi large-text home is readable and the primary action is reachable; native translation review, Hinglish screenshots, and every screen at large fonts remain pending.
- No actual speech recognizer/TTS engine interaction was tested. Engines may use network services.
- Cloud/backend, dual-provider validation, authentication and quotas are not implemented; provider and capture paths remain disabled. No real-app or payment success claim.
- Figma components and Home in both themes visually inspected; other frames, exact local-source fidelity, fonts and animation timing remain unverified. See DESIGN_SYSTEM.md for known parity gaps.
- Source credential path removed; exhaustive APK binary/secret scanning not performed.

Use only made-up values for remaining synthetic practice tests. Record actual device/OS/build and observed outcomes before extending compatibility claims.


## Navigation phase — final verification

Full build/unit/lint plus nine functional UI tests passed after fixing dock occlusion and dark heading inheritance. Adding the frame-metrics test produced a final connected run with **10 tests, zero failures/errors, BUILD SUCCESSFUL**. The subsequent isolated metrics run passed its one test; it overwrites the generated connected XML. No production code changed after the full passing functional run.

New checks: nested route selection/Back; intermediate spring position; rapid-tap retarget; continuous category indicator during an unfinished drag; category restoration on leaving/returning; persisted opaque mode across activity recreation; immediate reduced-motion selection; reachable Hindi category/action at320dp and200% font scale. Original task routing/theme/privacy checks remain. Unit tests remain20 passing; lint0errors/57warnings. `git diff --check` passed.

Reviewed screenshots: light Home, dark Practice, Hindi200% narrow Practice/action and both Figma navigation sets plus integrated light/dark Home. Captures now include full Compose root with edge-to-edge insets. New images are in screenshots/2026-09-29-navigation. The earlier screenshots are historical and retained.

**Performance not passed:** Android Window FrameMetrics TOTAL_DURATION on the software-rendered emulator measured p50=138.52ms/p95=253.50ms with recording (165frames); p50=157.92ms/p95=299.70ms without recording (226frames). All frames exceeded16.67ms. This is a measured limitation, not evidence of smooth60Hz animation. See MOTION_AND_HAPTICS.md for profiling follow-up and exact JSON files.

Not run: physical-device frame profiling, TalkBack traversal, API26–30 fallback, low-RAM/power-save/high-contrast toggles, landscape, Hinglish-specific screenshots, full frame-by-frame video review, system-scale-zero pager release, and per-pixel blur validation. Reduced-transparency preference and relevant selection semantics were verified on the emulator; that does not substitute for TalkBack testing.

## Latest safety and Liquid Glass phase

21 unit tests, zero failures/errors/skips; debug APK builds; lint zero errors/57 warnings. Added stale-presentation matching test for permission-related stop so an obsolete overlay cannot terminate a newer observation/session. AppOps watching, startup permission check, expected overlay attachment exception handling and animator cleanup compile successfully. **Live permission revocation/attachment failure is not instrumented or device-verified.** Implementation uses the platform [AppOpsManager watcher](https://developer.android.com/reference/android/app/AppOpsManager).

11 emulator UI tests passed in the full final regression run. Added glass action/disabled-state semantics, opaque fallback on Home, task input, and privacy cancellation coverage. Initial new-test failure was in screenshot capture: the dialog adds a second root; fixed by explicitly targeting the dialog window. Tests and screenshots now distinguish that window. All earlier navigation, Hindi200%, narrow320dp, settings/recreation and privacy-reset checks pass.

Current visual evidence: screenshots/2026-09-29-glass. Reviewed light/dark Home, large Hindi text, dark intake, disabled setup action, and privacy dialog. The native practice button style compiles but its rendered appearance and native form interaction were not exercised during this phase. Pointer hover, keyboard traversal, full TalkBack and all device-fallback branches remain unverified.

Latest broad software-emulator frame sample:160frames, p50=154.34ms, p95=206.94ms; 160 exceed16.67ms. **Performance remains unaccepted.** This debug measurement includes screen composition/test work; it is not a device smoothness certification or a controlled comparison against the previous phase. No physical-device profiling and no new motion video were captured.

See GLASS_UI.md for the audited controls left in historical/native surfaces and Figma synchronization still pending. No full-specification or every-control parity claim.

Final dialog brand-color correction was followed by another complete successful11-test emulator run plus unit/build/lint checks. Exported screenshots and metrics reflect that run.

## Vector identity, launch transition and notification Stop follow-up

Final optimized-vector run:22 unit tests and13 emulator UI tests pass with zero failures/errors. Debug build succeeds; lint reports zero errors/60 warnings. Added launcher entry/recreation/background-return checks and installed adaptive-icon verification/export. Added unit regression proving notification session identity survives observation changes and becomes invalid after stop/restart. Actual queued notification delivery race remains untested.

New SVG and Android vector geometry was rendered and inspected in both standalone and installed adaptive-icon form. Initial detailed contours produced long-path warnings; simplified fitted curves reduced the mark to25 cubic segments and removed those warnings. Old raster assets are intentionally preserved. Remaining new lint findings include the preserved unused raster, a name-based CustomSplashScreen warning for LaunchActivity (which uses AndroidX SplashScreen, not a dedicated custom splash page), and a missing-monochrome warning on the API26 icon variant (the API33 variant contains monochrome). No warning-free claim.

Cold-launch recording and sampled frames: screenshots/2026-09-29-launch/saathi-launch.mp4 and launch-*.png. Reviewed centered mint logo, easing and fade into real content. This is a software-emulator recording, not proof of physical-device60Hz performance. Android26–30, Android12, reduced-motion settings, OEM masks/themed icon appearance and genuinely slow initialization remain unverified. Earlier glass/performance limitations continue to apply.

The original startup lint run flagged an API27 navigation-bar attribute in a min26 style; the unnecessary attribute was removed before the passing run. All changes remain local/uncommitted.
