package com.blockblast.solver.detector;

import android.graphics.Bitmap;

/**
 * Detects the 8×8 Block Blast board and the 3 piece slots from a screen bitmap.
 *
 * Strategy: luminance threshold.
 *   - Filled cell  → bright, raised tile  → high luminance
 *   - Empty cell   → dark background      → low luminance
 * This is completely colour-agnostic, so skin/theme changes don't break detection.
 */
public class BoardDetector {

    public static final int GRID = 8;   // 8×8 board
    public static final int PIECES = 3; // 3 piece slots

    // ── Tuneable constants (percentages of screen dimensions) ──────────────
    // These approximate Block Blast's layout on a typical 9:19.5 phone.
    // Users can tweak BOARD_TOP / BOARD_LEFT etc. via a calibration screen later.

public static final float BOARD_TOP_PCT    = 0.240f; // Stretched up from 0.238 to perfectly hit your 580y mark
public static final float BOARD_LEFT_PCT   = 0.055f; // KEPT (Perfect!)
public static final float BOARD_RIGHT_PCT  = 0.944f; // KEPT (Perfect!)
public static final float BOARD_BOTTOM_PCT = 0.660f; // Stretched down from 0.648 to perfectly hit your 1540y mark
public static final float TRAY_TOP_PCT     = 0.720f;
public static final float TRAY_BOTTOM_PCT  = 0.850f;

    // Piece tray columns (left-centre of each of the 3 slots)
    private static final float[] PIECE_CENTER_X = { 0.15f, 0.50f, 0.83f };
    // Max piece size we check (5×5 bounding box around centre)
    private static final int PIECE_SCAN_HALF    = 2; // ±2 cells

    /** Luminance cutoff: 0-255. Pixels above this = filled. */
    private static final float LUMA_THRESHOLD = 80f;

    // ── Outputs ────────────────────────────────────────────────────────────

    /** board[row][col] = true if that cell is filled */
    public boolean[][] board = new boolean[GRID][GRID];

    /**
     * pieces[p][r][c] = true for piece p at relative row r, col c.
     * Each piece fits in a 5×5 bounding box.
     */
    public boolean[][][] pieces = new boolean[PIECES][5][5];

    // ── Public API ─────────────────────────────────────────────────────────

    public void detect(Bitmap bmp) {
        int W = bmp.getWidth();
        int H = bmp.getHeight();

        detectBoard(bmp, W, H);
        detectPieces(bmp, W, H);
    }

    // ── Board detection ────────────────────────────────────────────────────

    private void detectBoard(Bitmap bmp, int W, int H) {
        int left   = (int)(BOARD_LEFT_PCT   * W);
        int right  = (int)(BOARD_RIGHT_PCT  * W);
        int top    = (int)(BOARD_TOP_PCT    * H);
        int bottom = (int)(BOARD_BOTTOM_PCT * H);

        int cellW = (right  - left) / GRID;
        int cellH = (bottom - top ) / GRID;

        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                // Sample centre of each cell
                int px = left + col * cellW + cellW / 2;
                int py = top  + row * cellH + cellH / 2;
                board[row][col] = (px < W && py < H) && luma(bmp.getPixel(px, py)) > LUMA_THRESHOLD;
            }
        }
    }

    // ── Piece detection ────────────────────────────────────────────────────

    private void detectPieces(Bitmap bmp, int W, int H) {
        int trayTop    = (int)(TRAY_TOP_PCT    * H);
        int trayBottom = (int)(TRAY_BOTTOM_PCT * H);
        int trayH      = trayBottom - trayTop;

        // Approximate cell size within tray (same as board cell width)
        int boardLeft  = (int)(BOARD_LEFT_PCT  * W);
        int boardRight = (int)(BOARD_RIGHT_PCT * W);
        int cellPx     = (boardRight - boardLeft) / GRID;

        for (int p = 0; p < PIECES; p++) {
            int cx = (int)(PIECE_CENTER_X[p] * W);
            int cy = trayTop + trayH / 2;

            for (int dr = -PIECE_SCAN_HALF; dr <= PIECE_SCAN_HALF; dr++) {
                for (int dc = -PIECE_SCAN_HALF; dc <= PIECE_SCAN_HALF; dc++) {
                    int px = cx + dc * cellPx;
                    int py = cy + dr * cellPx;
                    int r  = dr + PIECE_SCAN_HALF;
                    int c  = dc + PIECE_SCAN_HALF;
                    pieces[p][r][c] = (px >= 0 && px < W && py >= 0 && py < H)
                            && luma(bmp.getPixel(px, py)) > LUMA_THRESHOLD;
                }
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Standard luminance from ARGB pixel (0-255). */
    private static float luma(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        return 0.299f * r + 0.587f * g + 0.114f * b;
    }
}
