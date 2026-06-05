package com.blockblast.solver.detector;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BoardDetector {

    public static final int GRID = 8;
    public static final int PIECES = 3;

    // Your perfect Redmi Note 12 dimensions
    public static final float BOARD_TOP_PCT    = 0.227f;
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

        // 1. Sample a known EMPTY space on the board to lock in the background color
        // Row 0, Col 7 (top right) is almost always empty at the start of a turn
        int left   = (int)(BOARD_LEFT_PCT   * W);
        int right  = (int)(BOARD_RIGHT_PCT  * W);
        int top    = (int)(BOARD_TOP_PCT    * H);
        int bottom = (int)(BOARD_BOTTOM_PCT * H);
        int cellW  = (right  - left) / GRID;
        int cellH  = (bottom - top ) / GRID;
        
        int bgX = left + 7 * cellW + cellW / 2;
        int bgY = top + 0 * cellH + cellH / 2;
        int bgColor = bmp.getPixel(bgX, bgY);

        // 2. Detect Board using color difference
        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int px = left + col * cellW + cellW / 2;
                int py = top  + row * cellH + cellH / 2;
                if (px < W && py < H) {
                    board[row][col] = isDifferentColor(bmp.getPixel(px, py), bgColor);
                }
            }
        }

        // 3. Detect Pieces using the tray background color
        int trayTop    = (int)(TRAY_TOP_PCT    * H);
        int trayBottom = (int)(TRAY_BOTTOM_PCT * H);
        int trayH      = trayBottom - trayTop;
        int cellPx     = (int)((cellW) * 0.60f);

        // Sample background of the tray (far left edge where no piece sits)
        int trayBgColor = bmp.getPixel((int)(0.02f * W), trayTop + trayH / 2);

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
                        pieces[p][r][c] = isDifferentColor(bmp.getPixel(px, py), trayBgColor);
                    }
                }
            }
        }
    }

    /** Compares two colors. Returns true if they are distinct (meaning it's a block). */
    private boolean isDifferentColor(int colorA, int colorB) {
        int rA = Color.red(colorA);
        int gA = Color.green(colorA);
        int bA = Color.blue(colorA);

        int rB = Color.red(colorB);
        int gB = Color.green(colorB);
        int bB = Color.blue(colorB);

        // Euclidean distance formula for RGB color space
        double distance = Math.sqrt(Math.pow(rA - rB, 2) + Math.pow(gA - gB, 2) + Math.pow(bA - bB, 2));
        
        // If color distance is greater than 35, it's definitely a tile, not background
        return distance > 35.0;
    }
}
