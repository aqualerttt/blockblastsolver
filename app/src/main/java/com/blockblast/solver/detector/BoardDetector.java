package com.blockblast.solver.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class BoardDetector {

    public static final int GRID = 8;
    public static final int PIECES = 3;

    public static final float BOARD_TOP_PCT    = 0.225f;
    public static final float BOARD_LEFT_PCT   = 0.055f;
    public static final float BOARD_RIGHT_PCT  = 0.944f;
    public static final float BOARD_BOTTOM_PCT = 0.665f;
    
    public static final float TRAY_TOP_PCT     = 0.735f; 
    public static final float TRAY_BOTTOM_PCT  = 0.815f; 

    private static final float[] PIECE_CENTER_X = { 0.15f, 0.50f, 0.83f };

    public boolean[][] board = new boolean[GRID][GRID];
    public boolean[][][] pieces = new boolean[PIECES][5][5];

    public void detect(Bitmap bmp, final Context context) {
        int W = bmp.getWidth();
        int H = bmp.getHeight();

        // Standard board check remains active
        int left = (int)(BOARD_LEFT_PCT * W);
        int right = (int)(BOARD_RIGHT_PCT * W);
        int top = (int)(BOARD_TOP_PCT * H);
        int bottom = (int)(BOARD_BOTTOM_PCT * H);
        int cellW = (right - left) / GRID;
        int cellH = (bottom - top) / GRID;

        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int px = left + col * cellW + cellW / 2;
                int py = top + row * cellH + cellH / 2;
                if (px < W && py < H) {
                    // Quick fallback calculation for board grid
                    int color = bmp.getPixel(px, py);
                    int r = (color >> 16) & 0xFF; int g = (color >> 8) & 0xFF; int b = color & 0xFF;
                    board[row][col] = (0.299 * r + 0.587 * g + 0.114 * b) > 65;
                }
            }
        }

        // --- NEW SIMPLIFIED BRIGHTNESS LASER RULER ---
        int firstActiveY = -1;
        int lastActiveY = -1;
        int detectedSlot = -1;

        for (int p = 0; p < PIECES; p++) {
            int targetX = (int)(PIECE_CENTER_X[p] * W);
            
            for (int y = 1600; y < 2050; y++) {
                if (targetX >= 0 && targetX < W && y >= 0 && y < H) {
                    int color = bmp.getPixel(targetX, y);
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    int luma = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                    // Smooth yellow or bright red blocks easily register luma values > 80.
                    // The dark background stays way below 50.
                    if (luma > 75) {
                        if (firstActiveY == -1) firstActiveY = y;
                        lastActiveY = y;
                    }
                }
            }
            
            if (firstActiveY != -1 && (lastActiveY - firstActiveY) > 40) {
                detectedSlot = p + 1;
                break;
            } else {
                firstActiveY = -1;
                lastActiveY = -1;
            }
        }

        if (context != null) {
            final String message;
            if (firstActiveY != -1 && lastActiveY != -1) {
                float topPct = (float) firstActiveY / H;
                float bottomPct = (float) lastActiveY / H;
                message = "Slot " + detectedSlot + " Found!\nTOP: " + String.format("%.3f", topPct) + "f\nBOTTOM: " + String.format("%.3f", bottomPct) + "f";
            } else {
                message = "Ruler Active!\nNo bright blocks found in Y 1600-2050.";
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
