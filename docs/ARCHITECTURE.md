# Architecture and privacy boundary

`SaathiAccessibilityService` receives window events and turns the current accessibility hierarchy into `UiNode` values. `NodeMasker` identifies password, PIN, OTP, CVV, and MPIN fields **before** any AI request is constructed; their values and descriptions are replaced with null. `ScreenshotCapture` paints matching rectangles black before JPEG encoding.

`SaathiSession` debounces a changing screen for 600 ms, records a fingerprint, requests a `GuideStep`, and sends its bounds to the overlay and its text to TTS. It never calls `performAction(ACTION_CLICK)` or dispatches gestures. If the fingerprint stays unchanged for five seconds after a prompt, the next response is requested with `previousStepFailed=true` and is worded as a calm redirect.

For a repeatable demo, `DemoGuide` provides the same structured response shape as Gemini. If `GEMINI_API_KEY` is provided, `GeminiClient` sends the masked node tree and optional masked screenshot to Gemini with JSON-only output. The offline guide is retained if the network/API call fails.
