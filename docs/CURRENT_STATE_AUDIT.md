# Starting audit — 28 September 2026

Baseline: `61e6e1f839e72cb6fd508a174530d5afea1c88c1`, initially `main`; now `codex/saathi-pilot-foundations`. GitHub connector confirmed main exists. No fetch, commit, push or PR was performed. Existing edits to MainActivity, manifest, theme, launch activity, resources, icons and output were preserved; initial diff/status saved under `/tmp/saathi-preexisting-*`. No AGENTS.md was found under the parent workspace.

| Finding and evidence | Severity / starting status | Phase-one disposition |
|---|---|---|
| app/build.gradle.kts embeds GEMINI_API_KEY; GeminiClient directly calls provider | Critical, confirmed | Removed client and BuildConfig fields; cloud guidance disabled. Existing old APKs not remediated. |
| SaathiSession checks only active when presenting asynchronous results | Critical, confirmed | Session/revision/package/window tickets, main-thread state, stale overlay intent checks added; pure gate tests pass. Device races not verified. |
| Notification Stop only stops GuidanceForegroundService | Critical, confirmed | Calls SaathiSession.stop; device verification pending. |
| Capture lacks screen/timestamp alignment, projection callback and suitable lifecycle | Critical, confirmed | Capture implementation and UI entry disabled; no image path remains. Future implementation must start from current platform requirements. |
| NodeMasker ignores raw text and Hindi cues | High, confirmed | Added conservative text/Unicode/camel-case checks; tests pass; not complete redaction. |
| Five-second wrong-tap timer and twenty-second watchdog repeat unchanged work | High, confirmed | Removed; no inactivity-based inference. |
| Worker queue and shared mutable session state | High, confirmed | Session state serialized on main; one tree copy plus dirty flag; old copies rejected. Device burst behavior remains untested. |
| Quota acquired before remote eligibility | High, confirmed | Remote path removed, limiter unused. Server budgets still absent. |
| Demo fallback used on third-party screens; WebsiteGuide repeatedly selects first label | High, confirmed | Both removed from external session routing; only identified same-package practice nodes eligible. WebsiteGuide source remains unused. |
| Completed cache stores generic labels, not flow definitions | Medium, confirmed | Still generic categories; not a workflow registry. |
| Amazon shopping package labelled Amazon Pay restriction; other package claims unverified | Medium, confirmed | Policy remains legacy and unverified; current session no longer uses it as compatibility evidence. |
| ConversationStore saves 80 messages; GuidanceStateStore saves goals; allowBackup true | High, confirmed | Stores now memory-only, legacy values cleared on construction; backup disabled. OEM transfer and migration need device checks. |
| Broad substring guardrails e.g. bet in ordinary words | Medium, confirmed | Not fixed; release limitation. |
| App uses Views, no Compose; themes partly hard-coded | Scope gap, confirmed | User's existing UI preserved; Figma foundations created, migration pending. |
| minSdk26 / compileSdk35 / targetSdk33, guidance foreground service has no type | Release blocker, confirmed | No blind SDK bump; foreground service and notification migration remains pending. |
| Full-screen application overlay at default opacity | Release blocker, needs device reproduction | Cross-app guidance disabled; touch behavior still requires redesign/testing before re-enabling. |
| Voice service error retries and audio coordination | High, confirmed | Automatic session microphone path disabled; task-screen speech input remains opt-in and engine-dependent. |
| Baseline lint | Confirmed | 7 errors/59 warnings. Typed typeface constants and missing Hindi resource fixed. Final lint 0 errors/59 warnings. |

Baseline build and seven unit tests passed; baseline lint failed. Current verification is in [TEST_RESULTS](TEST_RESULTS.md).

Official sources checked 28 September 2026: [Android 12 touch restrictions](https://developer.android.com/about/versions/12/behavior-changes-all), [MediaProjection](https://developer.android.com/media/grow/media-projection), [Gemini deprecations](https://ai.google.dev/gemini-api/docs/deprecations). Current model choice is deliberately unset. The Play target-policy URL failed to load; current submission deadline is **not verified**. Recheck before SDK migration. No current pricing, free-tier or data-use approval is claimed.


## 29 September follow-up

The baseline findings above describe the original audit. A Compose app shell and core light/dark Figma frames now exist. Test evidence and remaining failures/unknowns are maintained in TEST_RESULTS.md and EXECUTION_PLAN.md. This does not close the audit's backend, lifecycle, accessibility, performance or real-app guidance requirements.
