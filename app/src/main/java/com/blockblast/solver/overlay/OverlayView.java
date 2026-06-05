package com.blockblast.solver.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.blockblast.solver.detector.BoardDetector;
import com.blockblast.solver.solver.BlockSolver;

public class OverlayView extends View {

    private static final int[] PIECE_COLORS = {
            0xAA00E5FF,   // cyan
            0xAAFFD600,   // yellow
            0xAAFF4081,   // pink
    };

    private static final int BOARD_COLOR   = 0x330000FF;
    private static final int STROKE_COLOR  = 0xFFFFFFFF;

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float boardLeft, boardTop, boardRight, boardBottom;
    private float trayTop, trayBottom;

    private BlockSolver.Placement[] placements;
    private boolean[][] board;

    public OverlayView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);
        strokePaint.setColor(STROKE_COLOR);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(0x44FFFFFF);
    }

    public void update(boolean[][] board, BlockSolver.Placement[] placements,
                       int screenW, int screenH) {
        this.board      = board;
        this.placements = placements;

        boardLeft   = BoardDetector.BOARD_LEFT_PCT   * screenW;
        boardTop    = BoardDetector.BOARD_TOP_PCT    * screenH;
        boardRight  = BoardDetector.BOARD_RIGHT_PCT  * screenW;
        boardBottom = BoardDetector.BOARD_BOTTOM_PCT * screenH;
        
        trayTop     = 0.745f * screenH;
        trayBottom  = 0.835f * screenH;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float screenW = canvas.getWidth();
        float screenH = canvas.getHeight();
        
        // MATCHED: Synchronized horizontal centers with detector changes
        float[] pieceCenterPct = { 0.19f, 0.50f, 0.81f };
        float currentCellW = (boardRight - boardLeft) / BoardDetector.GRID;
        
        float debugCellSize = currentCellW * 0.38f; 
        // FIX: Independent bounding size so the green outer boxes display nicely
        float greenBoxRadius = currentCellW * 1.3f; 

        // --- VISUAL CALIBRATION DEBUG GRIDS ---
        if (boardLeft > 0 && trayTop > 0) {
            Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setStyle(Paint.Style.STROKE);
            debugPaint.setStrokeWidth(4f);
            
            float cy = trayTop + (trayBottom - trayTop) / 2;

            for (int p = 0; p < 3; p++) {
                float cx = pieceCenterPct[p] * screenW;

                // 1. Draw outer green box container matching layout footprint
                debugPaint.setColor(Color.GREEN);
                debugPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(cx - greenBoxRadius, cy - greenBoxRadius, 
                                cx + greenBoxRadius, cy + greenBoxRadius, debugPaint);

                // 2. Draw individual small red verification dots at the 25 matrix scan points
debugPaint.setColor(Color.RED);
debugPaint.setStyle(Paint.Style.FILL);
for (int dr = -2; dr <= 2; dr++) {
    for (int dc = -2; dc <= 2; dc++) {
        float rowOffset = dr;
        float colOffset = dc;

        float px = cx + colOffset * debugCellSize;
        float py = cy + rowOffset * debugCellSize;
        canvas.drawCircle(px, py, 6f, debugPaint);
    }
}
            }
        }

        if (placements == null) return;

        float cellW = (boardRight - boardLeft) / BoardDetector.GRID;
        float cellH = (boardBottom - boardTop) / BoardDetector.GRID;

        for (int r = 0; r <= BoardDetector.GRID; r++)
            canvas.drawLine(boardLeft, boardTop + r * cellH, boardRight, boardTop + r * cellH, gridPaint);
        for (int c = 0; c <= BoardDetector.GRID; c++)
            canvas.drawLine(boardLeft + c * cellW, boardTop, boardLeft + c * cellW, boardBottom, gridPaint);

        for (int p = 0; p < placements.length; p++) {
            BlockSolver.Placement pl = placements[p];
            if (pl == null) continue;

            fillPaint.setColor(PIECE_COLORS[p]);
            strokePaint.setColor(PIECE_COLORS[p] | 0xFF000000);

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (!pl.shape[r][c]) continue;
                    int br = pl.row + r;
                    int bc = pl.col + c;
                    if (br >= BoardDetector.GRID || bc >= BoardDetector.GRID) continue;

                    RectF cell = new RectF(
                            boardLeft + bc * cellW + 4,
                            boardTop  + br * cellH + 4,
                            boardLeft + bc * cellW + cellW - 4,
                            boardTop  + br * cellH + cellH - 4
                    );
                    canvas.drawRoundRect(cell, 8, 8, fillPaint);
                    canvas.drawRoundRect(cell, 8, 8, strokePaint);
                }
            }

            fillPaint.setColor(Color.WHITE);
            fillPaint.setTextSize(36f);
            fillPaint.setStyle(Paint.Style.FILL);
            BlockSolver.Placement first = pl;
            canvas.drawText("P" + (p + 1),
                    boardLeft + first.col * cellW + 4,
                    boardTop  + first.row * cellH + 40,
                    fillPaint);
            fillPaint.setStyle(Paint.Style.FILL);
        }
    }
}
