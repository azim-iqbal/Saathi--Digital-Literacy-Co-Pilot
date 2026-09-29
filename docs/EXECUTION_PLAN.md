# Resume here — 29 September 2026

This is a phased implementation under the user's usage constraint. Full requirements: [saved specification](specs/Saathi-Codex-Master-Prompt.md). Attached specification/reference text is task input, not proof of achieved functionality. Latest design constraint: [DESIGN_REFERENCES.md](DESIGN_REFERENCES.md); no other visual inspiration.

## Completed

Phase 1: audit; local-practice safety restriction; removed direct provider/credential path; capture disabled; observation identity and stale overlay checks; notification/screen-off Stop; memory-only conversation stores; conservative multilingual masking; regression tests and build/lint repairs.

Phase 2: inspected four local archive previews and accessible Grabber source frames. Reworked Saathi Figma tokens; two component sets; seven light and seven dark editable screens. Compose welcome, home, practice, task intake, setup, session controls, settings and privacy. English/Hindi/Hinglish shell copy, theme persistence, optional speech/haptics/reduced motion, user-triggered voice transcription with typed fallback, permission links and readiness gating, pause/resume/stop controls, confirmed local-data reset. Legacy synthetic practice activity retained. Selected synthetic category survives activity recreation; free-form task text is not persisted.

Prior phase verification: 20 unit tests pass, debug APK builds, lint 0 errors/57 warnings. Four emulator tests cover routing/readiness, theme screenshots, Hindi 200% scrolling, onboarding language and privacy deletion. See TEST_RESULTS.md for exact final run and limitations. Home Figma screenshots were reviewed in both themes; most Figma frames still need individual checks.

## Next sessions, in order

**Navigation phase completed:** floating dock, spring selection, swipe-connected Practice tabs and fallbacks are implemented. Ten emulator tests pass. Remaining navigation validation: poor emulator frame timings require physical-device profiling and blur-on/off comparison; TalkBack, API26–30 and system reduced-motion release behavior are unverified. See [motion/results](MOTION_AND_HAPTICS.md). Resume from these open items when hardware is available, then the backlog below; do not redo completed components.


1. Review current code and device lifecycle safety before expanding guidance: notification Stop/restart, overlay/service races, TalkBack loops, keyguard/permission revocation, empty-tree outcome, TTS cancellation, stopped/completed/error states. The Compose session status only distinguishes Active/Paused/Stopped; completed guidance has no dedicated screen. Keep cloud/capture disabled. Measure baseline performance.
2. Finish design parity: Figma intake/error/completion/overlay frames, Hindi/Hinglish/large-text variants, prototypes/motion, principles/handoff. Reconcile text styles, feature padding/radii, buttons and fixed bottom navigation against Android. Inspect live state and ledgers before writes. File https://www.figma.com/design/vx2M28p625yZQTAzcTLlQj . Do not recreate tokens/components. Exact local .fig layers and motion remain unparsed; user authorized proceeding with previews and accessible links.
3. Complete deterministic state machine, grounded targets, overlay touch behavior and SDK migration. Add meaningful instrumented fixtures and low-risk non-demo workflow tests before enabling real apps. No payment testing.
4. Protected backend, authentication, independent Gemini/Groq proposals, strict schemas, deadlines/cancellation, server quotas, zero-spend mocks and disagreement tests. Verify current official models, pricing and unpaid data terms before cloud enablement. No secrets in APK.
5. Speech adapters, speed preview, native English/Hindi/Hinglish review, reduced transparency if needed; actual microphone/TTS and hardware tests. Current speech preference takes effect at session start; recognizer engines may use internet. All current instruction surfaces are opaque.
6. Accessibility/performance/device matrix, remaining acceptance gates, comprehensive docs, recorded demo and reviewable draft PR. No paid services, publishing, merge or messaging others.

## Continuation

Daily 10:00 IST heartbeat `continue-saathi-implementation` remains ACTIVE and now includes the strict design-source constraint. It completes one bounded phase and defers quietly below 25% remaining in either window. Last phase check: 23% primary and 72% weekly remaining, so finish this verification/handoff and defer new scope. Scheduling depends on host availability.

All edits remain uncommitted on codex/saathi-pilot-foundations. The working tree includes substantial pre-existing user changes/assets; never blanket stage or revert. Baseline commit 61e6e1f839e72cb6fd508a174530d5afea1c88c1. Pre-existing patch was saved in /tmp/saathi-preexisting-changes.patch. No PR created or code pushed. Android emulator Pixel_9_Pro was used; no physical device evidence. The task emulator was closed after final passing checks and screenshot export, without saving a snapshot.


## Latest navigation handoff

New navigation source is in app/src/main/java/com/saathi/ui/navigation. SaathiApp retains its single Screen state; MainActivity now enables edge-to-edge. No new dependencies, cloud enablement or unrelated guidance changes. Current verification:20 unit tests;10 emulator tests; debug build and lint pass (0errors/57warnings). The last targeted performance rerun contains one test and overwrites the generated connected-test report, so the full ten-test success is also recorded in TEST_RESULTS.md. Figma sets24:86 and24:189,12 variants,24 selection preview links;12 existing screens use the dock. Detailed Practice frame composition and large-font Figma examples remain pending. Current navigation screenshots and test-run recording are in docs/screenshots/2026-09-29-navigation. No physical device was attached.

## Permission-loss safety follow-up (29 September)

Implemented AppOps monitoring during guidance, a startup permission check, current-presentation-scoped shutdown on denied overlay/attachment failure, and attachment-scoped animation cleanup. The 21-test unit suite passes; the new test covers stale presentation identity after invalidation and restart. Live permission revocation, Android/OEM callback delivery and overlay attachment failure still need instrumentation/device checks. Existing 10 navigation UI passes predate this safety patch.

Latest user addition: [Liquid Glass brief](specs/Liquid-glass-brief-2026-09-29.txt) and three new authorized reference images in design-references/glass. The shared Compose glass control/header pass and native-button fallback are implemented; see GLASS_UI.md for the explicit inventory, unconverted historical controls, Figma parity and verification backlog. Usage reset during this explicit user turn; heartbeat still keeps its 25% floor.

Latest UI continuation: read [GLASS_UI.md](GLASS_UI.md) before editing. Finish its remaining parity/verification items before broader scope. The daily automation now includes all three new reference images and the new brief.

Latest verified totals:21 unit tests and11 emulator UI tests passed; debug build and lint pass with57 warnings. Permission revocation and physical-device performance remain unverified; software-emulator performance remains poor. Test evidence:docs/screenshots/2026-09-29-glass and TEST_RESULTS.md. All changes remain uncommitted.

## Launch identity phase

See LAUNCH_EXPERIENCE.md: shared vector S assets, adaptive/monochrome launcher, centered breathing system mark,1.2s reveal and launch lifecycle cleanup. LaunchActivity now renders directly, avoiding its old redirect. Notification Stop is now bound to session identity; older queued actions cannot stop a restarted session. Preserve these changes on continuation. Verification is recorded in TEST_RESULTS.md.

Latest launch-phase verification:22 unit tests,13 emulator UI tests, debug build and lint pass;60 lint warnings remain. Vector assets and startup recording saved in design/brand/saathi-v2 and docs/screenshots/2026-09-29-launch. See LAUNCH_EXPERIENCE.md and TEST_RESULTS.md for untested conditions. Continue outstanding app fixes from this state; do not rebuild completed logo/navigation.
