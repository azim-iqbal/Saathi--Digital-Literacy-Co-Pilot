# Saathi Privacy and Safety

Saathi is a guide, not an operator. It helps you understand the current screen while a guidance session is active. It can read the ordinary visible layout, labels, and controls exposed by Android Accessibility. It does not watch continuously when guidance is off.

## What Saathi never sees

Before anything is analysed or sent to a model, Saathi excludes fields flagged by Android as passwords and fields whose hint, identifier, or description indicates a PIN, OTP, password, CVV, MPIN, passcode, or security code. This includes `TYPE_TEXT_VARIATION_PASSWORD`, `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`, `TYPE_TEXT_VARIATION_WEB_PASSWORD`, `TYPE_NUMBER_VARIATION_PASSWORD`, and the password flag reported by Accessibility.

The value and content description of those fields are replaced with nothing. If screen capture is enabled, Saathi paints a solid black rectangle over every excluded field *before* it encodes the image. Screenshots are kept in memory only: they are not written to disk. A screenshot is used only for the single guidance request that needs it.

## Online and offline guidance

When online Gemini guidance is enabled, the app sends the stated goal, redacted non-sensitive screen structure, and (only when enabled) the already-masked screenshot to Google’s Gemini API. This leaves the device only for that request. The built-in deterministic demo path sends nothing over the network.

Saathi keeps no persistent conversation history, screen content, screenshots, voice recordings, PINs, or model responses. Its local rate limiter stores timestamps only. Its completed-flow cache stores up to three generic flow categories only, never a goal or screen data.

## You remain in control

Saathi never taps, types, presses Pay, submits a form, or performs gestures. It only observes, highlights, and speaks. This preserves trust, protects financial and account actions, and helps the person learn the flow rather than silently acting for them.

You can immediately stop access by disabling **Saathi guidance** in Android Accessibility settings and turning off **Display over other apps** for Saathi. Accessibility events stop reaching Saathi, the overlay cannot appear, and no new guidance requests are made. Stopping a session also removes the overlay.

## Scope guardrail

Saathi is not a general screen reader, surveillance tool, or chatbot. Its local guardrail allows only concrete on-screen help such as bill payments, forms, payment navigation, ticket booking, government portals, and essential related settings. It declines general knowledge, entertainment, writing, and unrelated advice before any network call. The same boundary is included in the Gemini instruction, and risky screens such as gambling, adult content, or unlicensed lending are refused. Guardrail audit records are memory-only labels for testing; they never contain goals or screen content.
