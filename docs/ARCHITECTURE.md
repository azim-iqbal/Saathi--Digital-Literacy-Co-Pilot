# Current architecture — phase one

Android Views remain the UI. MainActivity starts an explicit session. Accessibility callbacks invalidate the previous presentation immediately; one off-main tree copy is allowed at a time with a dirty flag for the next observation. Results return to main with a session/revision/package/window ticket. The ticket must still match before evaluation. Only identified same-package practice nodes enter DemoGuide. Targets resolve to one enabled nonsensitive current node; overlays check the presentation identity again on service delivery.

No cloud provider or screenshot path runs. WebsiteGuide and GeminiRateLimiter are legacy unused helpers. ObservationGate is not the complete future task state machine. There is no protected backend or dual-model path. Notification Stop invalidates session state and releases session resources; device lifecycle coverage is still pending.

Conversation/goal state is memory-only. Preferences and generic completion categories persist. See PRIVACY.md, CURRENT_STATE_AUDIT.md and EXECUTION_PLAN.md for unresolved boundaries and migration steps.


## Compose shell — 29 September

LaunchActivity redirects to MainActivity → SaathiApp. Preferences stores onboarding/language/theme/speech/motion/haptics; Copy provides English/Hindi/Hinglish shell text. PracticeTask accepts supported synthetic task categories. The UI observes SaathiSession status and Android permission readiness. Voice transcription is user-triggered and editable before continuing. DemoBillPayActivity remains the deterministic local fixture; LegacyTaskActivity is retained and non-exported. The shell's Active/Paused/Stopped display is provisional, not the complete specification state machine. No backend was introduced.
