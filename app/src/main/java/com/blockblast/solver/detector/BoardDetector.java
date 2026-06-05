package com.blockblast.solver.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class BoardDetector {

    public static final int GRID = 8;
    public static final int PIECES = 3;

    // Your perfect main board parameters
    public static final float BOARD_TOP_PCT    = 0.225f;
    public static final float BOARD_LEFT_PCT   = 0.055f;
    public static final float BOARD_RIGHT_PCT  = 0.944f;
    public static final float BOARD_BOTTOM_PCT = 0.665f;
    
    // Fallbacks
    public static final float TRAY_TOP_PCT     = 0.735f; 
    public static final float TRAY_BOTTOM_PCT  = 0.815f; 

    public boolean[][] board = new boolean[GRID][GRID];
    public boolean[][][] pieces = new boolean[PIECES][5][5];

    // Added context parameter here so we can show on-screen text
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

        // --- ON-SCREEN CALIBRATION RULER ---
        int middleX = W / 2;
        int firstActiveY = -1;
        int lastActiveY = -1;

        // Scan down the center column to find the 5x5 piece
        for (int y = 1600; y < 2100; y++) {
            if (hasTextureContrast(bmp, middleX, y)) {
                if (firstActiveY == -1) firstActiveY = y;
                lastActiveY = y;
            }
        }

        if (firstActiveY != -1 && lastActiveY != -1 && context != null) {
            final float topPct = (float) firstActiveY / H;
            final float bottomPct = (float) lastActiveY / H;
            
            // Format the exact text we need to copy-paste
            final String message = "TOP: " + String.format("%.3f", topPct) + "f\nBOTTOM: " + String.format("%.3f", bottomPct) + "f";

            // Pop it up on your phone screen immediately
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean hasTextureContrast(Bitmap bmp, int centerX, int centerY) {
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
        return (maxLuma - minLuma) > 18;
    }
}
