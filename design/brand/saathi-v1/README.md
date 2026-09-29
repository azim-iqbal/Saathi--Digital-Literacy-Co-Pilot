# Saathi logo — Together, forward

Created 29 September 2026 using the built-in image-generation tool. A soft S monogram with two interlocking curves suggests companionship and progress. It uses Saathi’s existing UI palette rather than outside visual references.

## Deliverables

- saathi-symbol-green.png — transparent green symbol for light surfaces.
- saathi-icon-dark.png — mint symbol on a dark square, suitable as an app-icon concept.
- saathi-wordmark-light.png — horizontal symbol and Saathi name on white.
- preview.html — light/dark presentation and 24/48/96px size comparison.

Palette targets: green #087900, mint #A4EE99, ink #171A17, dark #131513, white #FFFFFF. These are generated raster assets; a production vector rebuild should enforce exact flat colors and shared geometry. The generated wordmark lettering is artwork, not an identified or bundled font. Preserve aspect ratio and surrounding whitespace. Use the symbol on compact surfaces and the full wordmark where the name must be introduced.

Visual review: green master, dark icon and wordmark inspected. A transparent mint export with visible artifacts was rejected and is not included. The dark icon resolves those artifacts with an opaque background. Existing launcher icons and user artwork have not been overwritten. Android adaptive-icon mask testing, vector reconstruction, in-app integration and small-screen recognition testing remain future implementation work.

## Generation brief

Generate an extremely minimal S-shaped monogram for Saathi, a patient digital-literacy companion, formed from equal-weight softly rounded interlocking paths. Prioritize a unified S silhouette, balanced open negative space and warm geometric proportions. Green #087900 for the transparent light-surface master. Avoid leaves, hands, faces, robots, sparkles, speech bubbles, chain links and infinity shapes. No text in the standalone symbol.

Derived dark-icon prompt: preserve the silhouette, recolor mint #A4EE99 over an opaque #131513 square, centered with generous mask-safe margins. Remove noise, speckles, texture and decoration.

Derived wordmark prompt: preserve the symbol at left, exact text “Saathi” at right in warm geometric medium-to-semibold lettering, dark #171A17 on white; optical spacing, generous whitespace, no tagline or decoration.

29 September integration follow-up: the existing green symbol is copied as saathi_symbol_green.png and used in the Compose header with theme tint alongside live Saathi text. Screenshot evidence is in docs/screenshots/2026-09-29-glass. Vector reconstruction, launcher integration and recognition testing remain pending.
