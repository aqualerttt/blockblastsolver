package com.blockblast.solver.detector;

import android.content.Context;
import android.graphics.Bitmap;

public class BoardDetector {

    public static final int GRID = 8;
    public static final int PIECES = 3;

    // Stable main board percentage metrics
    public static final float BOARD_TOP_PCT    = 0.225f;
    public static final float BOARD_LEFT_PCT   = 0.055f;
    public static final float BOARD_RIGHT_PCT  = 0.944f;
    public static final float BOARD_BOTTOM_PCT = 0.665f;
    
    // Hard baseline definition for the item tray zone
    public static final float TRAY_TOP_PCT     = 0.745f; 
    public static final float TRAY_BOTTOM_PCT  = 0.835f; 

    // Isolated horizontal screen placement weights for Slots 1, 2, and 3
    public static final float[] PIECE_CENTER_X = { 0.19f, 0.50f, 0.81f };

    public boolean[][] board = new boolean[GRID][GRID];
    public boolean[][][] pieces = new boolean[PIECES][5][5];

    public int debugTrayTop = 0;
    public int debugTrayBottom = 0;
    public int[] debugPieceX = new int[PIECES];
    public int debugCellSize = 0;

    public void detect(Bitmap bmp, final Context context) {
        int W = bmp.getWidth();
        int H = bmp.getHeight();

        int left = (int)(BOARD_LEFT_PCT * W);
        int right = (int)(BOARD_RIGHT_PCT * W);
        int top = (int)(BOARD_TOP_PCT * H);
        int bottom = (int)(BOARD_BOTTOM_PCT * H);
        int cellW = (right - left) / GRID;
        int cellH = (bottom - top) / GRID;

        this.debugTrayTop = (int)(TRAY_TOP_PCT * H);
        this.debugTrayBottom = (int)(TRAY_BOTTOM_PCT * H);
        
        // Re-stabilize cell scale factors cleanly based on screen scaling
        this.debugCellSize = (int)(cellW * 0.38f); 

        for (int p = 0; p < PIECES; p++) {
            this.debugPieceX[p] = (int)(PIECE_CENTER_X[p] * W);
        }

        // 1. Scan Main Board Matrix
        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int px = left + col * cellW + cellW / 2;
                int py = top + row * cellH + cellH / 2;
                if (px < W && py < H) {
                    int color = bmp.getPixel(px, py);
                    int r = (color >> 16) & 0xFF; int g = (color >> 8) & 0xFF; int b = color & 0xFF;
                    board[row][col] = (0.299 * r + 0.587 * g + 0.114 * b) > 65;
                }
            }
        }

        // 2. Scan Pieces via Standard Baseline Matrix Loops
        int trayH = debugTrayBottom - debugTrayTop;
        int cy = debugTrayTop + trayH / 2;

        for (int p = 0; p < PIECES; p++) {
            int cx = debugPieceX[p];

            for (int dr = -2; dr <= 2; dr++) {
                for (int dc = -2; dc <= 2; dc++) {
                    int px = cx + dc * debugCellSize;
                    int py = cy + dr * debugCellSize;
                    int r  = dr + 2;
                    int c  = dc + 2;

                    if (px >= 0 && px < W && py >= 0 && py < H) {
                        int color = bmp.getPixel(px, py);
                        int redVal = (color >> 16) & 0xFF; 
                        int greenVal = (color >> 8) & 0xFF; 
                        int blueVal = color & 0xFF;
                        int luma = (int)(0.299 * redVal + 0.587 * greenVal + 0.114 * blueVal);
                        
                        pieces[p][r][c] = (luma > 75);
                    }
                }
            }
        }
    }
}
