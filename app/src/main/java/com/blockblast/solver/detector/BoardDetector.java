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
                if (px < W && py < H) board[row][col] = hasTextureContrast(bmp, px, py);
            }
        }

        // --- CALIBRATION RULER (Y 1600 - 2050) ---
        int firstActiveY = -1;
        int lastActiveY = -1;
        int detectedSlot = -1;

        for (int p = 0; p < PIECES; p++) {
            int targetX = (int)(PIECE_CENTER_X[p] * W);
            
            // ADJUSTED: Tightened vertical search window down to 2050 max
            for (int y = 1600; y < 2050; y++) {
                if (hasTextureContrast(bmp, targetX, y)) {
                    if (firstActiveY == -1) firstActiveY = y;
                    lastActiveY = y;
                }
            }
            
            if (firstActiveY != -1 && (lastActiveY - firstActiveY) > 50) {
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
                message = "Scanning tray...\nNo block detected between Y 1600-2050 (H=" + H + ")";
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean hasTextureContrast(Bitmap bmp, int centerX, int centerY) {
        if (centerX < 0 || centerX >= bmp.getWidth() || centerY < 0 || centerY >= bmp.getHeight()) return false;
        int minLuma = 255; int maxLuma = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = centerX + dx; int y = centerY + dy;
                if (x >= 0 && x < bmp.getWidth() && y >= 0 && y < bmp.getHeight()) {
                    int color = bmp.getPixel(x, y);
                    int r = (color >> 16) & 0xFF; int g = (color >> 8) & 0xFF; int b = color & 0xFF;
                    int luma = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                    if (luma < minLuma) minLuma = luma;
                    if (luma > maxLuma) maxLuma = luma;
                }
            }
        }
        return (maxLuma - minLuma) > 15;
    }
}
