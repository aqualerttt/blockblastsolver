package com.blockblast.solver.detector;

import android.graphics.Bitmap;

public class BoardDetector {

    public static final int GRID = 8;
    public static final int PIECES = 3;

    // Your verified Redmi Note 12 layout percentages
    public static final float BOARD_TOP_PCT    = 0.225f;
    public static final float BOARD_LEFT_PCT   = 0.055f;
    public static final float BOARD_RIGHT_PCT  = 0.944f;
    public static final float BOARD_BOTTOM_PCT = 0.665f;
    public static final float TRAY_TOP_PCT     = 0.735f; 
    public static final float TRAY_BOTTOM_PCT  = 0.815f; 

    private static final float[] PIECE_CENTER_X = { 0.15f, 0.50f, 0.83f };
    private static final int PIECE_SCAN_HALF    = 2;

    public boolean[][] board = new boolean[GRID][GRID];
    public boolean[][][] pieces = new boolean[PIECES][5][5];

    public void detect(Bitmap bmp) {
        int W = bmp.getWidth();
        int H = bmp.getHeight();

        int left   = (int)(BOARD_LEFT_PCT   * W);
        int right  = (int)(BOARD_RIGHT_PCT  * W);
        int top    = (int)(BOARD_TOP_PCT    * H);
        int bottom = (int)(BOARD_BOTTOM_PCT * H);
        int cellW  = (right  - left) / GRID;
        int cellH  = (bottom - top ) / GRID;

        // 1. Scan Main Board using texture contrast variance
        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int px = left + col * cellW + cellW / 2;
                int py = top  + row * cellH + cellH / 2;
                if (px < W && py < H) {
                    board[row][col] = hasTextureContrast(bmp, px, py);
                }
            }
        }

        // 2. Scan Piece Tray using tight shrunken steps
        int trayTop    = (int)(TRAY_TOP_PCT    * H);
        int trayBottom = (int)(TRAY_BOTTOM_PCT * H);
        int trayH      = trayBottom - trayTop;
        int cellPx     = (int)((cellW) * 0.60f); // Match the shrunken piece size

        for (int p = 0; p < PIECES; p++) {
            int cx = (int)(PIECE_CENTER_X[p] * W);
            int cy = trayTop + trayH / 2;

            for (int dr = -PIECE_SCAN_HALF; dr <= PIECE_SCAN_HALF; dr++) {
                for (int dc = -PIECE_SCAN_HALF; dc <= PIECE_SCAN_HALF; dc++) {
                    int px = cx + dc * cellPx;
                    int py = cy + dr * cellPx;
                    int r  = dr + PIECE_SCAN_HALF;
                    int c  = dc + PIECE_SCAN_HALF;

                    if (px >= 0 && px < W && py >= 0 && py < H) {
                        pieces[p][r][c] = hasTextureContrast(bmp, px, py);
                    }
                }
            }
        }
    }

    /**
     * Checks a 3x3 pixel neighborhood around a coordinate.
     * If the brightness jumps around significantly, it's a textured block element.
     */
    private boolean hasTextureContrast(Bitmap bmp, int centerX, int centerY) {
        int minLuma = 255;
        int maxLuma = 0;

        // Sample a tiny 3x3 matrix around the target pixel center
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                if (x >= 0 && x < bmp.getWidth() && y >= 0 && y < bmp.getHeight()) {
                    int color = bmp.getPixel(x, y);
                    
                    // Convert to standard grayscale luminance
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    int luma = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                    if (luma < minLuma) minLuma = luma;
                    if (luma > maxLuma) maxLuma = luma;
                }
            }
        }

        // A high contrast delta means borders, bevels, or block shines are present
        int delta = maxLuma - minLuma;
        return delta > 18; 
    }
}
