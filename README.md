# 🧩 Block Blast Solver — Overlay APK

A real-time overlay that analyses your Block Blast screen and highlights the best placement for each piece using **brightness/luminance detection** (not color matching, so skin/theme changes don't break it).

---

## How to Build & Install (No PC needed)

### Step 1 — Fork or Upload to GitHub
1. Go to [github.com](https://github.com) and create a free account if you don't have one.
2. Create a **new repository** (name it anything, e.g. `block-blast-solver`).
3. Upload ALL files from this ZIP maintaining the same folder structure.
   - Easiest way: click **"uploading an existing file"** on the repo page, then drag the entire folder contents in.

### Step 2 — Trigger the Build
1. Once files are uploaded, click the **Actions** tab in your repo.
2. You'll see **"Build APK"** workflow. Click it.
3. Click **"Run workflow"** → **"Run workflow"** (green button).
4. Wait ~3–5 minutes for it to finish (green checkmark ✓).

### Step 3 — Download the APK
1. Click on the completed workflow run.
2. Scroll down to **Artifacts**.
3. Download **`BlockBlastSolver-debug`** — this is your APK zip.
4. Unzip it → you'll get `app-debug.apk`.

### Step 4 — Install on Your Phone
1. Transfer `app-debug.apk` to your Android phone (via Google Drive, USB, Telegram, etc.).
2. Open the file → Android will ask you to **"Allow from this source"** → allow it.
3. Install.

---

## How to Use

1. Open **Block Blast Solver** app.
2. Tap **▶ Start Solver**.
3. Grant **"Display over other apps"** permission when prompted.
4. Grant **Screen recording** permission when prompted.
5. Switch to **Block Blast** — the overlay activates automatically.
6. You'll see coloured highlights:
   - **Cyan (P1)** = best spot for the 1st piece
   - **Yellow (P2)** = best spot for the 2nd piece
   - **Pink (P3)** = best spot for the 3rd piece
7. When done, open the app and tap **⏹ Stop Solver**.

---

## Detection Method

Uses **luminance thresholding** — not color matching:
- Filled cells are bright/raised tiles → high brightness
- Empty cells are dark background → low brightness

This means it works regardless of Block Blast's skin, color theme, or block texture changes.

---

## Calibration (if highlights are off-position)

The board detection percentages are in `BoardDetector.java`:
```java
private static final float BOARD_TOP_PCT    = 0.235f;
private static final float BOARD_LEFT_PCT   = 0.045f;
private static final float BOARD_RIGHT_PCT  = 0.955f;
private static final float BOARD_BOTTOM_PCT = 0.720f;
private static final float TRAY_TOP_PCT     = 0.760f;
private static final float TRAY_BOTTOM_PCT  = 0.900f;
```
These are tuned for typical 9:19.5 phones. If your phone has a different aspect ratio, adjust them, re-upload, and rebuild.

---

## Permissions Required
- **Display over other apps** — to draw the overlay
- **Screen recording** — to read the game board (via MediaProjection API)

No root. No accessibility service abuse. No interaction with Block Blast's process.
