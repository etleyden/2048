
import java.util.ArrayList;
import java.util.Scanner;

public class _2048 {
    //note-to-self: boards will always need to be nxn moving forward. Which is totally fine with me lol
    private int[][] board;
    private int[][] prev_board;
    private boolean debugWanted = false;
    public _2048() {

        Scanner in = new Scanner(System.in); //is this still relevant? Should investigate later.
        //initialize random starting board
        resetBoard();
    }

    /**
     * Resetting the board means </br>
     * 1. Filling the array with 0's; </br>
     * 2. Populating the board with 2 random values (either a 2, or a 4)
     */
    public void resetBoard() {
        board = new int[4][4];

        addValues(2);

    }
    public void addValues(int qty) {
        for(int i = 0; i < qty; i++) {
            int y, x;
            do {
                y = (int) Math.floor(Math.random() * 4);
                x = (int) Math.floor(Math.random() * 4);
            } while(board[y][x] != 0);
            board[y][x] = (int) Math.ceil(Math.random() * 2);
        }
    }
    //a wrapper method to handle an ongoing game in the console
    public void begin() {
//        board = new int[][]{{0, 0, 0, 0},
//                            {2, 0, 0, 0},
//                            {1, 0, 0, 0},
//                            {1, 0, 0, 0}};
        while(doesNotExist2048()) {
            System.out.println(this.getBoardString());
            String move = getNextMove().trim();
            if(move.equals("n")) {
                debugWanted = !debugWanted;
                continue;
            }
            makeMove(move);
            //if the board hasn't changed, then we don't want to add new values. You have to contribute something before we give you something.
            if(prev_board != null && !prev_board.equals(board)) {
                addValues(1);
            }

//            System.out.println(this.getBoardString());
//            break;
        }
    }

    /**
     * Adjusts the board to respond to move input
     * @param move must be a valid wasd input
     * @return the updated board
     */
    public int[][] makeMove(String move) {
        //a simpler reference to the board
        prev_board = board.clone();

        int[][] b = board;

        int innerDirection = (move.equals("s") || move.equals("d")) ? -1 : 1;
        int innerStartIndex = (move.equals("s") || move.equals("d")) ? b.length - 1: 0;


        int o = 0, i = 0, j = 0;

        try {
            //o = outer index. o is our "other index". This should be static during each move.
            for (o = 0; o < b.length; o++) {
                ArrayList<Integer> unmergableIndices = new ArrayList<>();

                //i = inner index. i is our "from" index
                for (i = innerStartIndex; 0 <= i && i < b.length; i += innerDirection) {
                    //if our current cell has something, then we need to move it

                    if (!compareBoardValues(b, i, o, 0, move, true)) {
                        //move back. j is our target index
                        j = i - innerDirection;
                        for (; 0 <= j && j < b.length; j -= innerDirection) {
                            //if we are at the beginning of the array and the value here is not 0, then we can just move it up.
                            if ((j == 0 || j == b.length - 1) && compareBoardValues(b, j, o, 0, move, true)) {
                                moveInDirection(b, i, j, o, move);
                                break;
                            }
                            //until we find a thing
                            if (!compareBoardValues(b, j, o, 0, move, true)) {
                                //if they're the same, then we need to merge the things
                                if (compareBoardValues(b, i, o, j, move, false) && !unmergableIndices.contains(j)) {
                                    moveInDirection(b, i, j, o, move);
                                    unmergableIndices.add(j);
                                } else if (i != j + innerDirection
                                        && compareBoardValues(b, j + innerDirection, o, 0, move, true)
                                        && !unmergableIndices.contains(j + 1)) { //if not, then we just move i up to the next empty spot.
                                    //if we're just moving it to the same location then we don't need to do anything
                                    moveInDirection(b, i, j + innerDirection, o, move);
                                    unmergableIndices.add(j + innerDirection);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException err) {
            System.out.printf("o: %d, i: %d, j: %d, direction: %d", o, i, j, innerDirection);
            for(StackTraceElement element : err.getStackTrace()) {
                System.out.println(element.toString());
            }
        }

        board = b;

        return b;
    }

    /**
     * Handles moving items using the relavent index (either b[thisIndex][otherIndex] or b[otherIndex][thisIndex]) based on move input.
     * Prevents clutter of the makeMove function, and is helpful whenever the indices are known.
     * @param b an int[][] which represents a board
     * @param from the item being moved
     * @param target the index destination
     * @param otherIndex the row or column that the move is happening along
     * @param move the move direction. This directly affects if the changing index is the first or second value
     * @return an updated board
     */
    public int[][] moveInDirection(int[][] b, int from, int target, int otherIndex, String move) {
        if(move.equals("w") || move.equals("s")) {
            if(b[target][otherIndex] == 0) {
                b[target][otherIndex] = b[from][otherIndex];
            } else {
                //if it is not empty, AND we're moving, that means makeMove determined these values were the same.
                //therefore this value needs to increase by 1 instead.
                b[target][otherIndex]++;
            }
            b[from][otherIndex] = 0;
        } else {
            if(b[otherIndex][target] == 0) {
                b[otherIndex][target] = b[otherIndex][from];
            } else {

                //if it is not empty, AND we're moving, that means makeMove determined these values were the same.
                //therefore this value needs to increase by 1 instead.
                b[otherIndex][target]++;
            }

            b[otherIndex][from] = 0;
        }

        return b;
    }

    /**
     * Compares two given board values. Needs to use the relevant index slot based on move.
     * @param b board array
     * @param relevantIndex The relevant index that is changing based on the move. (for example, if the user is making a "w" move, then the relevant index is the first index)
     * @param otherIndex The index that's not the relevant index
     * @param target Depending on the value of targetIsDirectValue, this target is either the target index to compare to the value at relevantIndex, or this is a direct value to compare to the given board value
     * @param move The String that represents our current move ("w", "a", "s", "d")
     * @param targetIsDirectValue true means that the target is a value to compare directly to the given board location (usually the target in this case would be 0). false means that the target is actually an index location
     * @return true: they are equal. false: not equal
     */
    public boolean compareBoardValues(int[][] b, int relevantIndex, int otherIndex, int target, String move, boolean targetIsDirectValue) {
        //check if any index is less than 0
        if(relevantIndex < 0) return false;

        if(targetIsDirectValue) {
            return (move.equals("w") || move.equals("s")) ? (b[relevantIndex][otherIndex] == target) : (b[otherIndex][relevantIndex] == target);
        } else {
            return (move.equals("w") || move.equals("s")) ? (b[relevantIndex][otherIndex] == b[target][otherIndex]) : (b[otherIndex][relevantIndex] == b[otherIndex][target]);
        }

    }

    /**
     * Retrieves and validates the next move as command line input from the user
     * @return the next valid wasd input from System.in
     */
    public String getNextMove() {
        Scanner in = new Scanner(System.in);
        boolean inputValid = false;
        String input = "";
        String validInputs = "wasdn";
        while(!inputValid) {
            System.out.print("Next move: ");
            input = in.nextLine().trim().toLowerCase();
            if(input.length() == 1 && validInputs.indexOf(input) >= 0) inputValid = true;
        }
        return input;
    }

    /**
     * Checks the board to see if there is a 2048 value in it
     * @return
     */
    public boolean doesNotExist2048() {
        for(int[] row : board) for(int cell : row) if(Math.pow(2, cell) >= 2048) return false;
        return true;
    }

    /**
     * Returns the board as a console-printable string
     * @return
     */
    public String getBoardString() {
        String boardString = "";
        for(int[] row : board) {
            boardString += "|";
            for(int cell : row) {
//                String cellData = ;
                boardString += (cell > 0) ? String.format("%-5d|", (int) Math.pow(2, cell)) : "     |";
            }
            boardString += "\n";
        }
        return boardString;
    }
    public static void main(String[] args) {

        _2048 m = new _2048();

        m.begin();
    }
}
