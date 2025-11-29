package tetris.view;

import tetris.domain.GameModel;
import tetris.domain.Board;
import tetris.domain.BlockKind;

/**
 * Improved PvP renderer for 80x25 terminal.
 * - Removed reflection usage for better performance and safety
 * - Added proper constants and error handling
 * - Improved layout calculations for tetris boards
 */
public final class PvPGameRenderer {
    // Screen dimensions
    public static final int WIDTH = 80;
    public static final int HEIGHT = 25;
    
    // Board cell types
    private static final int EMPTY_CELL = 0;
    private static final int ATTACK_LINE = 8;
    
    // Layout constants
    private static final int TETRIS_BOARD_WIDTH = 10;
    private static final int TETRIS_BOARD_HEIGHT = 20;
    private static final int BOARD_START_ROW = 4;
    private static final int LEFT_PANEL_WIDTH = WIDTH / 2;
    private static final int LEFT_BOARD_COL = (LEFT_PANEL_WIDTH - TETRIS_BOARD_WIDTH) / 2;
    private static final int RIGHT_BOARD_COL = LEFT_PANEL_WIDTH + LEFT_BOARD_COL;

    private PvPGameRenderer() {}

    public static String render(GameModel left, GameModel right, 
                               boolean leftConnected, boolean rightConnected, 
                               String statusMessage) {
        char[][] screen = initializeScreen();
        
        // Draw headers
        drawHeaders(screen, leftConnected, rightConnected);
        
        // Draw game info (scores, next blocks)
        drawGameInfo(screen, left, right);
        
        // Draw boards
        drawBoard(screen, left, LEFT_BOARD_COL, "P1");
        drawBoard(screen, right, RIGHT_BOARD_COL, "P2");
        
        // Draw status message
        drawStatusMessage(screen, statusMessage);
        
        return screenToString(screen);
    }

    private static char[][] initializeScreen() {
        char[][] screen = new char[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                screen[y][x] = ' ';
            }
        }
        return screen;
    }

    private static void drawHeaders(char[][] screen, boolean leftConnected, boolean rightConnected) {
        String leftHeader = "P1: " + (leftConnected ? "CONNECTED" : "DISCONNECTED");
        String rightHeader = "P2: " + (rightConnected ? "CONNECTED" : "DISCONNECTED");
        
        writeLine(screen, 0, 2, leftHeader);
        writeLine(screen, 0, LEFT_PANEL_WIDTH + 2, rightHeader);
    }

    private static void drawGameInfo(char[][] screen, GameModel left, GameModel right) {
        // Scores
        String leftScore = "Score: " + getScore(left);
        String rightScore = "Score: " + getScore(right);
        writeLine(screen, 1, 2, leftScore);
        writeLine(screen, 1, LEFT_PANEL_WIDTH + 2, rightScore);
        
        // Next blocks
        String leftNext = "Next: " + getNextBlock(left);
        String rightNext = "Next: " + getNextBlock(right);
        writeLine(screen, 2, 2, leftNext);
        writeLine(screen, 2, LEFT_PANEL_WIDTH + 2, rightNext);
    }

    private static void drawBoard(char[][] screen, GameModel model, int startCol, String playerLabel) {
        if (model == null) {
            writeLine(screen, BOARD_START_ROW, startCol, "(no board)");
            return;
        }

        Board board;
        try {
            board = model.getBoard();
        } catch (Exception e) {
            writeLine(screen, BOARD_START_ROW, startCol, "(board error)");
            return;
        }

        int[][] grid;
        try {
            grid = board.gridView();
        } catch (Exception e) {
            writeLine(screen, BOARD_START_ROW, startCol, "(grid error)");
            return;
        }

        if (grid == null || grid.length == 0) {
            writeLine(screen, BOARD_START_ROW, startCol, "(empty grid)");
            return;
        }

        // Draw board border
        drawBoardBorder(screen, startCol);
        
        // Draw grid content
        int maxRows = Math.min(grid.length, TETRIS_BOARD_HEIGHT);
        int maxCols = Math.min(grid[0].length, TETRIS_BOARD_WIDTH);
        
        for (int row = 0; row < maxRows; row++) {
            int screenRow = BOARD_START_ROW + 1 + row;
            if (screenRow >= HEIGHT - 1) break;
            
            for (int col = 0; col < maxCols; col++) {
                int screenCol = startCol + 1 + col;
                if (screenCol >= WIDTH) break;
                
                char cellChar = getCellChar(grid[row][col]);
                screen[screenRow][screenCol] = cellChar;
            }
        }
    }

    private static void drawBoardBorder(char[][] screen, int startCol) {
        // Top border
        for (int i = 0; i < TETRIS_BOARD_WIDTH + 2; i++) {
            if (startCol + i < WIDTH) {
                screen[BOARD_START_ROW][startCol + i] = '-';
            }
        }
        
        // Side borders
        for (int row = 1; row <= TETRIS_BOARD_HEIGHT; row++) {
            int screenRow = BOARD_START_ROW + row;
            if (screenRow >= HEIGHT - 1) break;
            
            if (startCol < WIDTH) {
                screen[screenRow][startCol] = '|';
            }
            if (startCol + TETRIS_BOARD_WIDTH + 1 < WIDTH) {
                screen[screenRow][startCol + TETRIS_BOARD_WIDTH + 1] = '|';
            }
        }
        
        // Bottom border
        int bottomRow = BOARD_START_ROW + TETRIS_BOARD_HEIGHT + 1;
        if (bottomRow < HEIGHT) {
            for (int i = 0; i < TETRIS_BOARD_WIDTH + 2; i++) {
                if (startCol + i < WIDTH) {
                    screen[bottomRow][startCol + i] = '-';
                }
            }
        }
    }

    private static void drawStatusMessage(char[][] screen, String statusMessage) {
        if (statusMessage != null && !statusMessage.isEmpty()) {
            int row = HEIGHT - 2;
            int col = Math.max(0, (WIDTH - statusMessage.length()) / 2);
            writeLine(screen, row, col, statusMessage);
        }
    }

    private static String screenToString(char[][] screen) {
        StringBuilder sb = new StringBuilder((WIDTH + 1) * HEIGHT);
        for (int y = 0; y < HEIGHT; y++) {
            sb.append(screen[y]);
            if (y < HEIGHT - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static void writeLine(char[][] screen, int row, int col, String text) {
        if (text == null || row < 0 || row >= screen.length) {
            return;
        }
        
        for (int i = 0; i < text.length(); i++) {
            int targetCol = col + i;
            if (targetCol >= 0 && targetCol < screen[row].length) {
                screen[row][targetCol] = text.charAt(i);
            }
        }
    }

    private static char getCellChar(int cellValue) {
        switch (cellValue) {
            case EMPTY_CELL:
                return '.';
            case ATTACK_LINE:
                return '=';  // Attack lines from opponent
            default:
                return '#';  // Regular blocks
        }
    }

    private static String getScore(GameModel model) {
        if (model == null) {
            return "-";
        }
        
        try {
            // Direct method call instead of reflection
            return String.valueOf(model.getScore());
        } catch (Exception e) {
            // Log error but don't crash
            System.err.println("Failed to get score: " + e.getMessage());
            return "ERR";
        }
    }

    private static String getNextBlock(GameModel model) {
        if (model == null) {
            return "-";
        }
        
        try {
            // Direct method call instead of reflection
            BlockKind nextBlock = model.getNextBlockKind();
            return nextBlock != null ? nextBlock.name() : "-";
        } catch (Exception e) {
            // Log error but don't crash
            System.err.println("Failed to get next block: " + e.getMessage());
            return "ERR";
        }
    }
}