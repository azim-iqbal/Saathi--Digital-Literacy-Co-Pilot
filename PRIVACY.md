# Privacy — current local-practice build

Updated 29 September 2026. Saathi observes accessibility structure during a user-started session and guides only its synthetic practice screen. It never taps, types or submits for the user.

Cloud reasoning and screenshot capture are disabled. There is no provider API key field in newly built APKs. Task-screen Android speech recognition and installed TTS engines may use network services; their offline operation and retention have not been verified. Use typing when voice is not desired. Automatic session listening is disabled.

Sensitive-field filtering is best effort, including password flags, English/Hindi cues and conservative numeric checks. It is not perfect. Do not enter real secrets into the practice flow. No screenshot or audio recording is written by Saathi. Android speech engines have their own behavior.

Free-form task text and conversation text are held in process memory, discarded at process death. A selected synthetic practice category may be saved by Android to restore the activity. Older persisted transcript/goal preferences are cleared when the corresponding stores are constructed. Language/theme preferences and generic practice completion categories persist. Android backup is disabled for this build; old backups, earlier APKs and OEM transfer behavior are not remediated or verified. Settings → Privacy → Clear local data stops the session and clears Saathi preferences and in-memory stores; this reset was tested on the emulator. Android Clear storage removes local app data; uninstalling also removes local app data according to platform behavior.

Stop invalidates pending observation results, removes overlays and stops session services and speech. Screen-off also requests Stop. End-to-end lifecycle verification on a device remains outstanding. Disable Saathi Accessibility in Android settings to revoke observation. A future cloud path requires separate consent, a protected backend and verified data terms.
