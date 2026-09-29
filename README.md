# Saathi — Digital Literacy Co-Pilot

Saathi helps people learn screen navigation while performing every tap themselves. **This branch currently supports local synthetic Bill Pay practice only.** Real-app guidance, cloud AI and screen capture are disabled while safety and architecture work proceeds.

The September 2026 redesign is **in progress**, not finished. [Progress and resume plan](docs/EXECUTION_PLAN.md) · [Audit](docs/CURRENT_STATE_AUDIT.md) · [Documentation index](docs/README.md) · [Figma designs](https://www.figma.com/design/vx2M28p625yZQTAzcTLlQj)

## Build and try

Use JDK 17, Android SDK 35, Gradle wrapper 8.7 and AGP 8.6.1. Set only `sdk.dir=/absolute/path/to/Android/sdk` in ignored `local.properties`. Provider keys are neither needed nor read by the Android build.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Install on an Android API26+ test device, enable Saathi Accessibility and overlay access, start a bill practice task and open Demo Bill Pay. Use made-up values only. No money moves. Notification Stop ends guidance; lifecycle/device validation is still pending.

English, Hindi and Hinglish guidance strings exist; the new Compose shell also has these three languages. Native-language, voice quality and accessibility review remain pending. Task-screen voice uses the device recognition service and is not guaranteed offline. See [privacy](PRIVACY.md) and [test results](docs/TEST_RESULTS.md).

No universal compatibility, production readiness, perfect redaction, unlimited free AI or successful real payment is claimed. The new app shell uses Jetpack Compose: welcome, home, practice, task intake, setup, session controls, settings and privacy. The synthetic practice activity remains Android Views. Editable light/dark Figma screens exist; full design parity, remaining variants and a recorded demo are pending. See docs/screenshots/2026-09-29 for emulator captures.

The navigation now uses a floating capsule with spring-driven selection and swipable synthetic Practice categories. [Implementation and measured limits](docs/MOTION_AND_HAPTICS.md) · [Dark Practice screenshot](docs/screenshots/2026-09-29-navigation/practice-dark.png). Emulator performance is below target; physical-device profiling remains required.

Latest UI phase: shared Liquid Glass controls and branded header are implemented in the Android shell. See [scope and remaining parity](docs/GLASS_UI.md), [current verification](docs/TEST_RESULTS.md), and [dark Home screenshot](docs/screenshots/2026-09-29-glass/home-dark.png). Native/legacy screen parity, Figma synchronization and physical-device performance remain open.

The latest launch update adds shared vector brand assets, adaptive/themed launcher support and a centered animated opening. [Launch details and limits](docs/LAUNCH_EXPERIENCE.md).
