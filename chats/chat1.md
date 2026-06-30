# Table Tennis Robot UI Design

_Started 2026-06-20 18:46 UTC_

---

## User

<system-info comment="Only acknowledge these if relevant">
Project title is now "Untitled"
The project currently has no files except possibly dotfiles — a `list_files` on the project root would return nothing else.
Current date is now June 20, 2026
</system-info>

<default aesthetic_system_instructions>
The user has not attached a design system. If they have ALSO not attached references or art direction, and the project is empty, you must ASK the user what visual aesthetic they want. Use the questions_v2 tool to ask about preferred vibe, audience, colors, type, mood, etc. Do NOT just pick your own visual aesthetic without getting the user's aesthetic input -- this is how you get slop!

Once answered, use this guidance when creating designs:
- Choose a type pairing from web-safe set or Google Fonts. Helvetica is a good choice. Avoid hard-to-read or overly stylized fonts. Use 1-3 fonts only.
- Foreground and background: choose a color tone (warm, cool, neutral, something in-between). Use subtly-toned whites and blacks; avoid saturations above 0.02 for whites.
- Accents: choose 0-2 additional accent colors using oklch. All accents should share same chroma and lightness; vary hue.
- NEVER write out an SVG yourself that's more complicated than a square, circle, diamond, etc.
- For imagery, never hand-draw SVGs; use subtly-striped SVG placeholders instead with monospace explainers for what should be dropped there (e.g. “product shot”)

CRITICAL: ignore default aesthetic entirely if given other aesthetic instructions like reference images, design systems or guidance, or if there are files in the project already.
</default aesthetic_system_instructions>

<pasted_text name="Pasted text (21 lines)">
I'm building the control app for a robot called Vali — an AI-powered table-tennis ball-picker that drives around a TT hall collecting stray balls. This screen is the PILOT VIEW: you hold the phone sideways (landscape) and you're basically driving the robot while watching its onboard camera, like a drone controller or a racing-game cockpit.

Design this interface and give me the front-end for it: a single self-contained index.html with its CSS, and vanilla JS only — no React or any framework, because it's going to run inside an Android WebView. For the camera area, use a placeholder image or a muted looping &lt;video&gt; for now; I'll wire the real live feed in later.

Layout (landscape phone, roughly 900×430; controls MUST sit where thumbs reach — bottom-left and bottom-right):
- The live camera feed fills the entire screen as the background, edge to edge, like looking through the robot's eyes.
- A HUD floats on top:
  - top bar: connection state, a latency (ms) + FPS readout, a battery indicator, a signal indicator (all placeholders).
  - centre: a targeting/aim element that suits a BALL-COLLECTING robot — not a generic military crosshair; make it mean something for lining up on a ball.
  - bottom-left: the drive control for the left thumb — a joystick or d-pad (forward / back / left / right).
  - bottom-right: action controls for the right thumb — a big primary "Collect" trigger that starts the picker, plus smaller snapshot and record buttons.
  - a small, tucked-away settings affordance to type in the robot's address (IP).
- All controls can be visual/stubbed for now — I just need them to look and feel right and animate on press.

Look &amp; feel — this part matters to me: I do NOT want it to look like a default AI-generated app. Please avoid the obvious clichés — not the glowing neon-green sci-fi HUD, not glassmorphism everywhere, not the cream + serif + terracotta look, not "all black with one acid accent colour." Instead pull the personality from the actual subject — table tennis and a purposeful little robot. Think about the materials of that world (the matte blue/green of a tournament table, the bright white ball, the net, umpire/scoreboard typography, the line markings and geometry of a court) and the feel of real industrial robotics control panels. Make deliberate choices:
- a specific palette of 4–6 named hex colours
- a deliberate type pairing: a display face with personality used sparingly, a clean UI face, and a monospace face for the live numbers/telemetry
- one signature element the design is remembered by.
Keep everything else quiet and disciplined around that one bold element. High contrast so the HUD stays readable over moving video, generous tap targets, subtle press animations (respect reduced-motion), visible focus states.

Show me a couple of directions first, then build out the one I pick into the front-end code. Let's iterate.
</pasted_text>

<!-- The user explicitly selected the following skills for this project, as attachments to their message. These are not optional context — they define how you work. Use them. -->
<attached-skill name="Design Components">
This project uses Design Components: every design is a single streaming `Name.dc.html` file. The full authoring spec is in your system prompt under "Writing code — Design Components" — follow it. Author and edit `.dc.html` content with the `dc_write`, `dc_html_str_replace`, `dc_js_str_replace`, and `dc_set_props` tools (not `write_file`; `str_replace_edit` works but won't stream); template edits stream into the live preview as you type.
</attached-skill>

<attached-skill name="Hi-fi design">
Create a high-fidelity, polished design.

Follow this general design process (use the todo list to remember):
(1) ask questions, (2) find existing UI kits and collect design context — copy ALL relevant components and read ALL relevant examples; ask the user if you can't find them, (3) start your file with assumptions + context + design reasoning (as if you are a junior designer and the user is your manager), with placeholders for the designs, and show it to the user early, (4) build out the designs and show the user again ASAP; append some next steps, (5) use your tools to check, verify and iterate on the design.

Good hi-fi designs do not start from scratch — they are rooted in existing design context. Ask the user to Import their codebase, or find a suitable UI kit / design resources, or ask for screenshots of existing UI. You MUST spend time trying to acquire design context, including components. If you cannot find them, ask the user for them. In the Import menu, they can link a local codebase, provide screenshots or Figma links; they can also link another project. Mocking a full product from scratch is a LAST RESORT and will lead to poor design. If stuck, try listing design assets and ls'ing design system files — be proactive! Some designs may need multiple design systems — get them all. Use the starter components (device frames and the like) to get high-quality scaffolding for free.

When presenting several options or explorations side-by-side, lay them out as labeled frames directly in the template — plain markup, not a canvas/artboard component, so every frame stays directly editable. The page is plain HTML — let the body itself scroll; never set `overflow:auto`/`scroll` on an inner wrapper, and don't build pan/zoom into the file. The outermost wrapper carries both the gray background and `width:max-content` (so the gray extends with the scroll) plus `min-width:100%; min-height:100vh; box-sizing:border-box; padding:48px; background:#e7e5df`. Inside it, each section is a start-aligned flex row — `display:flex; gap:48px; align-items:flex-start` — never centered on the horizontal axis: `justify-content:center`, `place-items:center`, or `margin:auto` on an overflowing row pushes frames off the left edge where scroll can't reach. Each frame gets `flex:none` and a fixed pixel width; a frame is a small label above a white card (`background:#fff; border-radius:2px; box-shadow:0 1px 3px rgba(0,0,0,.08)`).

When designing, asking many good questions is ESSENTIAL.

Give options: try to give 3+ variations across several dimensions. Mix by-the-book designs that match existing patterns with new and novel interactions, including interesting layouts, metaphors, and visual styles. Have some options that use color or advanced CSS; some with iconography and some without. Start your variations basic and get more advanced and creative as you go! Try remixing the brand assets and visual DNA in interesting ways — play with scale, fills, texture, visual rhythm, layering, novel layouts, type treatments. The goal is not the perfect option; it's exploring atomic variations the user can mix and match.

CSS, HTML, JS and SVG are amazing. Users often don't know what they can do. Surprise the user.

If you do not have an icon, asset or component, draw a placeholder: in hi-fi design, a placeholder is better than a bad attempt at the real thing.
</attached-skill>

<attached-skill name="Interactive prototype">
Create a fully interactive prototype with realistic state management and transitions. Use React useState/useEffect for dynamic behavior. Include hover states, click interactions, form validation, animated transitions, and multi-step navigation flows. It should feel like a real working app, not a static mockup.
</attached-skill>


<system-reminder>Auto-injected reminder (ignore if not relevant): do not recreate copyrighted or branded UI unless the user's email domain matches that company. Create original designs instead.</system-reminder>

## Assistant

Got it — a landscape pilot/cockpit view for Vali. Strong brief, and I want to nail the look before writing code. A few quick questions, then I'll show you a couple of visual directions to pick from.

One note up front: you asked for a standalone `index.html` with vanilla JS for an Android WebView — I'll honor that exactly for the final deliverable (no framework). The direction explorations I show first will be in the same plain-HTML spirit so what you pick is what you ship.

_[tool: questions_v2]_

## User

Questions answered:
- directions_count: Two contrasting directions
- palette_lean: Tournament table blue (ITTF blue, white ball, net), Industrial robotics panel (warning-yellow, slate, machined metal)
- drive_control: Analog joystick (free 360° + magnitude)
- signature: The ball-targeting reticle (centre)
- camera_placeholder: Looping muted video of a TT hall
- reticle_behavior: Lock-on ring that snaps when a ball is centred, Intake alignment guide (am I lined up with the picker mouth?)
- handedness: Make it mirrorable in settings
- extra: Keep it consistent with the existing Vali brand — reuse the accent colour(s)
from our landing page (vali-real.vercel.app) so the app feels like the same
product, not a separate thing. [paste your brand hex here if you have them]

Vali's character: a small, precise, purposeful little machine — a helpful
ball-fetcher, not a menacing military drone. Tone should feel sharp and a
touch playful, never aggressive.

For the live numbers/telemetry, lean on umpire/scoreboard-style type — it
nods to table tennis without being a gimmick.

Put a small "Vali" wordmark somewhere unobtrusive (a corner), not dominating
the screen.

Must-haves: stays readable under bright gym lighting and over moving video,
and big tap targets — this gets used on a court, sometimes with sweaty hands.

## Assistant

Let me pull the Vali brand from your landing page so the app matches.I couldn't reach the live landing page (the fetcher only opens links that show up in search, and it didn't surface). Since you left the hex as a placeholder, I'll pick a deliberate palette from the two worlds you chose and you can drop in your exact brand hex anytime — I've isolated the accent as one variable so swapping is trivial.

Quick call on format: you need a standalone vanilla `index.html` for a WebView, so I'm doing the direction explorations in that same plain-HTML medium (not the usual component format) — what you pick is literally what ships. Building the two directions now.

_[tool: update_todos]_

_[tool: web_fetch]_

_[tool: web_search]_

_[tool: write_file]_

I forgot to define the per-direction CSS variables. Adding them now.

_[tool: str_replace_edit]_

_[tool: show_html]_

_[tool: show_html]_

Top looks clean. Let me see the full device frames.

_[tool: save_screenshot]_

Direction A reads beautifully. Now Direction B.

_[tool: save_screenshot]_

Both directions render cleanly and distinctly. Marking the mockup done and surfacing for your pick.

_[tool: update_todos]_

_[tool: ready_for_verification]_

Two directions are live in your preview — both are real, interactive, and built in plain HTML/CSS/JS so the one you pick ships as-is. The reticle auto-cycles searching↔lock; drag the stick, tap Collect/Snap/Rec, open the gear (⋯) for the IP + left-handed mirror.

**A — Centre Court** (tournament blue + Vali orange): sporty and friendly. Rounded **ball-lock** reticle that snaps orange and pulls its corner ticks inward, soft intake mouth goes green on alignment. Signature = the warm, confident ball-lock.

**B — Service Robot** (slate + safety-yellow): an industrial teleop instrument. Angular **machined bracket** reticle with a live range readout and hazard-striped intake rails. Reads hardest in bright gym light; the loud yellow does all the talking.

Both share the same anatomy, telemetry, and ergonomics; they differ in material world and reticle personality.

A few things worth deciding as you look:
- **Which direction** (or a hybrid — e.g. B's high-contrast legibility with A's rounded ball-lock).
- **Accent hex** — I used `#FF6A2B` / `#FFC400` as placeholders; paste your real Vali brand hex and I'll swap the single accent variable.
- **Camera placeholder** — right now it's a CSS-built TT-hall scene to stress-test HUD contrast; you wanted a looping muted video. Drop me a clip (or I'll keep the CSS scene with a `<video>` slot wired so you just point it at a file).

Tell me your pick and I'll build it out into the final `index.html`.

## User

continue doing it

## Assistant

Building out **Direction A — Centre Court** as the final `index.html` (best fit for Vali's "sharp, a touch playful, never menacing" character, with B's legibility discipline folded in). Standalone vanilla file for the WebView, with a real video slot, working joystick magnitude, functional left-handed mirror, and persisted IP.

_[tool: update_todos]_

_[tool: write_file]_

_[tool: show_html]_

_[tool: save_screenshot]_

_[tool: show_html]_

_[tool: save_screenshot]_

Clean and on-spec. Marking the build tasks done and handing to verification.

_[tool: update_todos]_

_[tool: ready_for_verification]_

Built out **Direction A — Centre Court** as the final `index.html` — single self-contained file, vanilla JS, WebView-ready.

**What's wired:**
- **Ball-lock reticle** (the signature): dashed scan ring → snaps orange, corner ticks pull inward, ball-dot glows on lock; pulses while picking. Intake guide below goes green when aligned.
- **Drive joystick** — real analog drag with magnitude + clamp; outputs a normalized `{nx, ny}` vector (forward = up) at the `// hook: send to robot` line. Keyboard-nudgeable for focus testing.
- **Collect** — latching trigger (COLLECT ↔ PICKING) with active ring pulse; **Snap** flashes, **Rec** counts up `0:00`.
- **Settings (⋯)** — IP field + **left-handed mirror** that actually swaps the thumb clusters; both persist to localStorage. SET triggers a reconnect blink.
- Top bar: connection LED states, live latency/FPS, draining battery, signal.
- Reduced-motion honored, visible focus rings, 62–104px tap targets, safe-area insets, portrait→rotate hint, haptics on supported devices.

**To wire your real feed:** set `#feed`'s `src` (or swap it for your MJPEG/WebRTC element) — it auto-reveals and hides the CSS fallback scene on `loadeddata`. The single brand accent is `--accent` at the top of the CSS — paste your real Vali orange there and everything follows.

Two open items for you: drop me your **actual brand hex** and a **looping clip** if you want it baked in, and tell me whether you'd like B's machined-bracket reticle offered as a toggle. Want me to adjust anything?

