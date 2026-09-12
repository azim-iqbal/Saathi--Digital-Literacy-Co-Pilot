# Saathi - Digital Literacy Co-Pilot

Saathi is an Android accessibility co-pilot for people who are new to digital services. It observes the active screen, highlights the next control, and gives short spoken guidance - but the person using the phone performs every action themselves.

> **Guide, do not act.** Saathi never taps, types, submits a form, or performs a gesture on the user's behalf.

## Why Saathi

Complex payment, form, and government-service apps can be difficult for first-time smartphone users. Saathi acts like a patient person sitting beside them: it explains one next step at a time in English, Hindi, or Hinglish, while keeping sensitive credentials out of its AI input.

## Features

- Accessibility-event capture with a 600 ms screen-settle debounce.
- A non-touchable, pulsing overlay that points to the next control without blocking the user's tap.
- Android Text-to-Speech and optional voice input for the user's goal.
- English, Hindi, and Hinglish modes, chosen on first launch and changeable later in Language settings.
- A reliable offline Bill Pay demo flow, including deliberate wrong-tap recovery.
- Optional Gemini structured-JSON guidance for in-scope online flows.
- Local scope guardrails that decline unrelated questions before any network call.
- Flash-only model enforcement plus local request limits to protect the free-tier budget.

## Privacy and safety by design

Saathi applies two privacy layers before optional online guidance:

1. Accessibility nodes marked as password, PIN, OTP, CVV, MPIN, or passcode fields have their text and descriptions removed.
2. If screen capture is enabled, those same field regions are painted black before an image is encoded.

The demo shows this boundary with a visible `LOCKED - EXCLUDED FROM AI` label. Read the complete policy in [PRIVACY.md](PRIVACY.md).

Saathi is intentionally limited to concrete on-screen tasks such as bill payment, forms, ticket booking, government portals, payment-app navigation, and related phone settings. It is not a general chatbot or surveillance tool.

## Requirements

- Android Studio with JDK 17
- Android SDK 35 or newer
- Android 8.0 (API 26) or newer device/emulator
- Optional: a Gemini API key for online guidance

### Supported Android build toolchain

This project is pinned to **Android Gradle Plugin 8.6.1**, **Gradle 8.7**, and **JDK 17**. In Android Studio, set **Gradle JDK** to the bundled JDK 17 (`Settings/Preferences → Build, Execution, Deployment → Build Tools → Gradle`) and sync the **repository root** - the folder that contains `settings.gradle.kts` - rather than the `app` folder alone.

If Android Studio shows manifest errors such as “attribute is not allowed here” together with unresolved `MainActivity` or service classes, resolve the Gradle sync first. Those errors are typically the IDE falling back to incomplete Android model information; they do not indicate that the manifest attributes should be removed.

## Getting started

1. Clone the repository and open it in Android Studio.
2. Allow Gradle to sync, then select a device or emulator running Android 8.0+.
3. Build and install the `app` module.
4. In Saathi, grant **Display over other apps** and enable **Saathi guidance** in Android Accessibility settings.
5. Optionally approve privacy-filtered screen capture.
6. Start guidance and open **Demo Bill Pay**. Follow the highlighted control; every tap remains yours.

To build from a terminal:

```bash
./gradlew assembleDebug
```

The generated debug APK is intentionally not committed. Android places it under `app/build/outputs/`.

## Optional Gemini configuration

The application works without a key by using the deterministic offline demo guide. To enable online Gemini guidance, create a local file named `local.properties` in the repository root:

```properties
sdk.dir=/absolute/path/to/Android/sdk
GEMINI_API_KEY=your_key_here
GEMINI_MODEL=gemini-2.0-flash
```

`local.properties` is ignored by Git. Do not add an API key to Kotlin code, XML resources, screenshots, issue comments, or commits. Saathi accepts Flash-class models only and locally limits calls to 8 per minute and 120 per day.

For Hinglish and Hindi, Android may ask for the Hindi Text-to-Speech voice. Install it through the Android TTS settings when prompted; Saathi falls back to English speech instead of becoming silent.

## Repository layout

```text
app/
  src/main/java/com/saathi/
    accessibility/    Accessibility event capture and sensitive-node masking
    capture/          Consent-based screenshot capture and pixel redaction
    guardrails/       Local topic/safety boundary and audit labels
    language/         English, Hindi, and Hinglish language support
    orchestrator/     Session state, offline guide, Gemini client, limits, cache
    overlay/          Non-touchable visual highlight overlay
    speech/           Text-to-Speech and speech recognition
  src/test/           Guardrail tests
docs/                 Architecture and live-demo notes
PRIVACY.md            Data handling and revocation details
```

## Git hygiene and `.gitignore`

The `.gitignore` is deliberately strict about generated files and secrets, while preserving source code, Gradle wrapper files, documentation, and safe templates such as `.env.example`. This keeps pull requests reviewable and prevents accidental credential disclosure.

| Category | Ignored patterns | Why they are ignored | What to commit instead |
| --- | --- | --- | --- |
| Android and Gradle outputs | `.gradle/`, `build/`, `**/build/`, `**/out/`, `*.apk`, `*.aab`, `*.ap_`, `*.dex`, `*.class` | These are reproducible build products, Gradle caches, or machine-specific bytecode. They create noisy diffs and can be regenerated with Gradle. | Kotlin/Java source, resources, Gradle build scripts, and the Gradle wrapper. Use release assets or CI artifacts to distribute APK/AAB files. |
| Local Android configuration | `local.properties`, `local.properties.*`, `.externalNativeBuild/`, `.cxx/`, `externalNativeBuild/`, `captures/` | SDK paths differ across computers; native build directories and captured demo media are local artifacts. `local.properties` may also contain the Gemini key. | A documented setup section or a safe `local.properties.example` with placeholder values only. |
| Secrets and signing assets | `.env`, `.env.*`, `*.jks`, `*.keystore`, `*.p12`, `*.pem`, `*.key`, `key.properties`, `secrets.properties`, `google-services.json` | API keys, signing certificates, Firebase configuration, and private keys must never enter repository history. A leaked signing key or API key cannot be made safe by deleting a later commit. | `.env.example`, `key.properties.example`, and redacted configuration examples that contain no usable values. Store real secrets in a password manager or CI secret store. |
| IDE metadata | `.idea/`, `*.iml`, `*.ipr`, `*.iws` | Android Studio and IntelliJ generate personal workspace, device, window, and indexing state. It changes frequently and causes unnecessary merge conflicts. | Shared formatting/editor configuration only when intentionally curated and reviewed by the team. |
| Kotlin and Java runtime artifacts | `.kotlin/`, `hs_err_pid*`, `replay_pid*` | Kotlin tooling caches and JVM crash/replay reports are local diagnostic output, not source code. | A short issue report with a sanitized stack trace when a reproducible defect needs investigation. |
| Operating-system and editor files | `.DS_Store`, `Thumbs.db`, `Desktop.ini`, `*.swp`, `*.swo`, `*~` | Finder, Windows Explorer, and editors create these files automatically. They have no project value. | Nothing - delete them locally if they appear outside ignored paths. |
| Test, profiling, and temporary artifacts | `test-results/`, `coverage/`, `*.ec`, `*.hprof`, `*.trace`, `tmp/` | These can be large, machine-specific, or contain diagnostic snapshots. They should be published through CI or attached to a private issue only when necessary. | Test source, a coverage summary in CI, and anonymized diagnostic details where appropriate. |

### Before committing

Run the following quick checks before opening a pull request:

```bash
git status
git diff --check
./gradlew test
./gradlew assembleDebug
```

Confirm that no key, certificate, APK, screen capture, local SDK path, or Android Studio workspace file appears in `git status`. If a secret is ever committed, revoke or rotate it immediately; removing the file in a later commit is not enough.

## Testing

The guardrail suite covers English, Hindi, and Hinglish allow/deny examples, along with unsafe-screen detection. Run it with:

```bash
./gradlew test
```

## Limitations

- Android only; iOS does not offer an equivalent general-purpose Accessibility Service and overlay model.
- Saathi guides a user but does not automate their interaction.
- Optional online guidance depends on connectivity and the configured free-tier model quota.
- The included Bill Pay experience is a controlled, safe mock flow. No real payment is made.
