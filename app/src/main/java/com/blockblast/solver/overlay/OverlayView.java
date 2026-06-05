package com.blockblast.solver.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.media.projection.MediaProjection;

import com.blockblast.solver.detector.BoardDetector;
import com.blockblast.solver.solver.BlockSolver;

/**
 * Transparent overlay View drawn on top of all apps via WindowManager.
 * Draws coloured highlight rectangles showing the best placement positions.
 * Enhanced with physical piece-tray bounds visualization grids.
 */
public class OverlayView extends View {

    // Highlight colours per piece slot (semi-transparent)
    private static final int[] PIECE_COLORS = {
            0xAA00E5FF,   // cyan
            0xAAFFD600,   // yellow
            0xAAFF4081,   // pink
    };

    private static final int BOARD_COLOR   = 0x330000FF; // faint blue tint on all filled cells
    private static final int STROKE_COLOR  = 0xFFFFFFFF;

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Board layout (set from OverlayService after we know screen size)
    private float boardLeft, boardTop, boardRight, boardBottom;
    private float trayTop, trayBottom;

    // Current solver output
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

    /** Called by OverlayService with updated solver results. */
    public void update(boolean[][] board, BlockSolver.Placement[] placements,
                       int screenW, int screenH) {
        this.board      = board;
        this.placements = placements;

        boardLeft   = BoardDetector.BOARD_LEFT_PCT   * screenW;
        boardTop    = BoardDetector.BOARD_TOP_PCT    * screenH;
        boardRight  = BoardDetector.BOARD_RIGHT_PCT  * screenW;
        boardBottom = BoardDetector.BOARD_BOTTOM_PCT * screenH;
        trayTop     = BoardDetector.TRAY_TOP_PCT     * screenH;
        trayBottom  = BoardDetector.TRAY_BOTTOM_PCT  * screenH;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the diagnostic tray frames even if placements solver logic hasn't completed yet
        float screenW = canvas.getWidth();
        float screenH = canvas.getHeight();
        
        // Dynamic horizontal layout markers matching BoardDetector centers (15%, 50%, 83%)
        float[] pieceCenterPct = { 0.15f, 0.50f, 0.83f };
        float currentCellW = (boardRight - boardLeft) / BoardDetector.GRID;
        float debugCellSize = currentCellW * 0.60f; // matches shrunken piece grid size

        // --- VISUAL CALIBRATION DEBUG GRIDS ---
        if (boardLeft > 0 && trayTop > 0) {
            Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setStyle(Paint.Style.STROKE);
            debugPaint.setStrokeWidth(4f);
            
            float cy = trayTop + (trayBottom - trayTop) / 2;

            for (int p = 0; p < 3; p++) {
                float cx = pieceCenterPct[p] * screenW;

                // 1. Draw outer green box container showing the 5x5 piece detection grid bounds
                debugPaint.setColor(Color.GREEN);
                debugPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(cx - (debugCellSize * 2.5f), cy - (debugCellSize * 2.5f), 
                                cx + (debugCellSize * 2.5f), cy + (debugCellSize * 2.5f), debugPaint);

                // 2. Draw individual small red verification dots at the 25 matrix scan points
                debugPaint.setColor(Color.RED);
                debugPaint.setStyle(Paint.Style.FILL);
                for (int dr = -2; dr <= 2; dr++) {
                    for (int dc = -2; dc <= 2; dc++) {
                        float px = cx + dc * debugCellSize;
                        float py = cy + dr * debugCellSize;
                        canvas.drawCircle(px, py, 6f, debugPaint);
                    }
                }
            }
        }

        // Keep standard placement solver execution running
        if (placements == null) return;

        float cellW = (boardRight - boardLeft) / BoardDetector.GRID;
        float cellH = (boardBottom - boardTop) / BoardDetector.GRID;

        // Draw faint grid over board
        for (int r = 0; r <= BoardDetector.GRID; r++)
            canvas.drawLine(boardLeft, boardTop + r * cellH,
                            boardRight, boardTop + r * cellH, gridPaint);
        for (int c = 0; c <= BoardDetector.GRID; c++)
            canvas.drawLine(boardLeft + c * cellW, boardTop,
                            boardLeft + c * cellW, boardBottom, gridPaint);

        // Draw each piece's best placement
        for (int p = 0; p < placements.length; p++) {
            BlockSolver.Placement pl = placements[p];
            if (pl == null) continue;

            fillPaint.setColor(PIECE_COLORS[p]);
            strokePaint.setColor(PIECE_COLORS[p] | 0xFF000000); // fully opaque stroke

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

            // Label: P1, P2, P3
            fillPaint.setColor(Color.WHITE);
            fillPaint.setTextSize(36f);
            fillPaint.setStyle(Paint.Style.FILL);
            BlockSolver.Placement first = pl;
            canvas.drawText("P" + (p + 1),
                    boardLeft + first.col * cellW + 4,
                    boardTop  + first.row * cellH + 40,
                    fillPaint);
            fillPaint.setStyle(Paint.Style.FILL); // reset
        }
    }
}
