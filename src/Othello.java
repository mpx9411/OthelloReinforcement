import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

enum Color{
    BLANK,
    WHITE, //BOT
    BLACK, //PLAYER
    FALSE
}

public class Othello implements Comparable {

    static Scanner input = new Scanner(System.in);
    static HashMap<Tile, Othello> realBotMoves = new HashMap<>();
    ArrayList<Othello> moves = new ArrayList<>();
    Tile[][] board;
    int whiteTiles = 0; //REINFORCEMENT PLAYER
    int blackTiles = 0; //MINIMAX PLAYER
    int possibleMoves;
    int score = 0;
    int turnCounter = 1;
    int depth;
    float reward = 0;
    Tile lastMove;

    int foresight = 5;
    float discount = (float) 0.5;

    private Othello(Tile[][] board, int depth) {
        this.board = board;
        this.depth = depth;
    }

    public static void main(String[] args) throws InterruptedException {
        Othello game = new Othello(new Tile[8][8], 0);
        System.out.println("Welcome to Othello. Enter the X and Y panel you wish to play.");
        game.setFlatRewards();
        game.run();
    }

    private void run() throws InterruptedException {
        constructBoard();
        while (!setOver()) {
            Thread.sleep(500);
            printBoard();
            if (turnCounter == 1) {
                blackMoveStart();
                turnCounter++;
            } else if (turnCounter % 2 == 0) {
                System.out.println("Bot 1 turn");
                blackMove(Color.BLACK);
                turnCounter++;
            } else if (turnCounter % 2 != 0) {
                System.out.println("Bot 2 turn");
                blackMove(Color.WHITE);
                turnCounter++;
            }

            System.out.println("Bot 1 score: " + whiteTiles);
            System.out.println("Bot 2 score: " + blackTiles);

        }
    }

    private void printBoard() {                          //PRINT THE INSTANCE BOARD
        System.out.println("Turn " + turnCounter);
        System.out.println("   0  1  2  3  4  5  6  7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                switch (board[i][j].getColor()) {
                    case BLACK:
                        System.out.print("[-]");
                        break;
                    case WHITE:
                        System.out.print("[+]");
                        break;
                    case BLANK:
                        System.out.print("[ ]");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private void flipTiles(int y, int x) {               //CHECK IF ANY OCCUPIED TILES ARE BETWEEN TWO TILES OF OTHER COLOR
        Color tile = board[y][x].getColor();
        Tile toChange;
        if (checkTile(y + 2, x) == tile && checkTile(y + 1, x) == otherColor(tile))
            board[y + 1][x].setColor(tile);
        if (checkTile(y - 2, x) == tile && checkTile(y - 1, x) == otherColor(tile))
            board[y - 1][x].setColor(tile);
        if (checkTile(y, x + 2) == tile && checkTile(y, x + 1) == otherColor(tile))
            board[y][x + 1].setColor(tile);
        if (checkTile(y, x - 2) == tile && checkTile(y, x - 1) == otherColor(tile))
            board[y][x - 1].setColor(tile);
        if (checkTile(y - 2, x - 2) == tile && checkTile(y - 1, x - 1) == otherColor(tile))
            board[y - 1][x - 1].setColor(tile);
        if (checkTile(y - 2, x + 2) == tile && checkTile(y - 1, x + 1) == otherColor(tile))
            board[y - 1][x + 1].setColor(tile);
        if (checkTile(y + 2, x + 2) == tile && checkTile(y + 1, x + 1) == otherColor(tile))
            board[y + 1][x + 1].setColor(tile);
        if (checkTile(y + 2, x - 2) == tile && checkTile(y + 1, x - 1) == otherColor(tile))
            board[y + 1][x - 1].setColor(tile);

    }

    private void blackMoveStart() {                        //SINCE START OF GAME IS PERFECTLY SYMMETRICAL, WE CAN RANDOMIZE THE BOT'S FIRST MOVE IF HE BEGINS
        double move = Math.floor(Math.random() * 3);
        switch ((int) move) {
            case 0:
                makeMove(board, 2, 3, Color.WHITE);
                break;
            case 1:
                makeMove(board, 3, 2, Color.WHITE);
                break;
            case 2:
                makeMove(board, 4, 5, Color.WHITE);
                break;
            case 3:
                makeMove(board, 5, 4, Color.WHITE);
                break;
        }
    }

    private void manualMove() {
        Color myColor = Color.BLACK;
        if (hasValidMove(myColor)) {
            char[] move;
            do {
                move = input.next().toCharArray();
            } while (move.length != 2 && !isValidMove(move[1], move[0], myColor));
            int x = Character.getNumericValue(move[0]);
            int y = Character.getNumericValue(move[1]);
            System.out.println("Tile " + x + "" + y);
            makeMove(board, y, x, myColor);
        }
    }

    private boolean hasValidMove(Color color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean setOver() {                      //CHECK IF GAME IS OVER
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, Color.WHITE))
                    return false;
                if (isValidMove(i, j, Color.BLACK))
                    return false;
            }
        }
        return true;
    }


    private void blackMove(Color color) {                        //EXECUTE BOT MOVE WITH A CALCULATED LIST OF OPTIMAL MOVES

        miniMax(color);
        Tile move = null;
        for (Tile t : realBotMoves.keySet()) {
            if (move == null)
                move = t;
            else {
                if (realBotMoves.get(t).getScore() > realBotMoves.get(move).getScore())
                    move = t;
            }
        }
        if (move != null) {
            System.out.println("Tile " + move.getX() + "" + move.getY());
            makeMove(board, move.getY(), move.getX(), color);
            realBotMoves.clear();
        } else {
            System.out.println("No valid moves");
        }
    }

    private Othello miniMax(Color color) {

        HashMap<Tile, Othello> possibleMoves = new HashMap<>();             //STORE INSTANCES POSSIBLE MOVES
        ArrayList<Othello> optimalMoves = new ArrayList<>();                //STORE OPTIMAL MOVES

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, color)) {
                    Othello theory = new Othello(board, depth + 1);      //IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    theory.makeMove(theory.board, i, j, color);
                    possibleMoves.put(theory.lastMove, theory);

                }
            }
        }
        this.possibleMoves = possibleMoves.size();
        if (possibleMoves.isEmpty() || depth > 3) { //IF THERE ARE NO MORE MOVES OR IF WE'VE GONE TOO DEEP, RETURN SCORE
            return this;
        }
        for (Othello t : possibleMoves.values()) {
            if (!t.setOver()) //                     IF WE'RE NOT AT THE END OF THE GAME, CALCULATE THE BEST MOVE FOR THE NEXT TURN, BUT FOR THE OTHER COLOR
                t.score += t.miniMax(otherColor(color)).getScore();
            if (optimalMoves.isEmpty())
                optimalMoves.add(t);
            else if ((t.getScore() > optimalMoves.get(0).getScore() && color == Color.WHITE) || (t.getScore() < optimalMoves.get(0).getScore() && color == Color.BLACK)) {
                optimalMoves.clear();
                optimalMoves.add(t);
            } else if (t.getScore() == optimalMoves.get(0).getScore()) {
                optimalMoves.add(t);
            }
        }

        Othello bestMove = null;
        for (Othello o : optimalMoves) {//CHOOSE WINNING MOVE WITH FEWEST TURNS
            if (bestMove == null)
                bestMove = o;
            if (bestMove == o) {
                continue;
            } else {
                if (bestMove.turnCounter > o.turnCounter)
                    bestMove = o;
                else if (bestMove.turnCounter == o.turnCounter)
                    bestMove = Math.random() > 0.5 ? bestMove : o;
            }
        }

        if (depth == 0) {                                                     //IF WE'RE IN THE REAL GAME, INSERT POSSIBILITIES INTO A REAL LIST
            realBotMoves.put(bestMove.lastMove, bestMove);
        }

        return this;
    }

    private void setRewards(Color color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, color)) {
                    Othello future = new Othello(board, depth + 1);  //Create a board that simulates a move
                    future.makeMove(future.board, i, j, color);
                    future.setScore();
                    future.reward += future.getScore(); //Add the added score of making the move to the reward
                    future.reward += board[i][j].getFlatReward();
                    if (depth < foresight) { //Check if we've looked as far into the future as we want
                        future.setRewards(otherColor(color)); //Assume the other player does a similar calculation
                        future.makeOptimalMove(otherColor(color));
                        future.setRewards(color);
                        future.reward += moves.get(0).reward * discount; //The future move is worth it's own accumulated value plus future optimal values times a discount value
                    }
                    moves.add(future);
                }
            }
        }
    }

    private void makeOptimalMove(Color color) {
        Tile move;
        if (color.equals(Color.BLACK)) {
            move = moves.get(moves.size() - 1).lastMove;
        } else {
            move = moves.get(0).lastMove;
        }
        makeMove(board, move.getX(), move.getY(), color);
    }

    private void constructBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Tile(i, j, Color.BLANK);
                if ((i == 3 && j == 3) || (i == 4 && j == 4))
                    board[i][j].setColor(Color.BLACK);
                else if ((i == 3 && j == 4) || (i == 4 && j == 3))
                    board[i][j].setColor(Color.WHITE);
            }
        }
    }

    private void makeMove(Tile[][] board, int y, int x, Color color) {
        if (isValidMove(y, x, color)) {
            board[y][x].setColor(color);
            setScore();
            lastMove = board[y][x];
            flipTiles(y, x);
            setScore();
            setOver();
        }
    }

    private boolean isValidMove(int y, int x, Color color) {
        if (board[y][x].getColor() == Color.BLANK) {
            if (checkTile(y - 2, x) == color && checkTile(y - 1, x) == otherColor(color))
                return true;
            if (checkTile(y + 2, x) == color && checkTile(y + 1, x) == otherColor(color))
                return true;
            if (checkTile(y, x - 2) == color && checkTile(y, x - 1) == otherColor(color))
                return true;
            if (checkTile(y, x + 2) == color && checkTile(y, x + 1) == otherColor(color))
                return true;
            if (checkTile(y + 2, x + 2) == color && checkTile(y + 1, x + 1) == otherColor(color))
                return true;
            if (checkTile(y - 2, x + 2) == color && checkTile(y - 1, x + 1) == otherColor(color))
                return true;
            if (checkTile(y + 2, x - 2) == color && checkTile(y + 1, x - 1) == otherColor(color))
                return true;
            if (checkTile(y - 2, x - 2) == color && checkTile(y - 1, x - 1) == otherColor(color))
                return true;

        }
        return false;
    }

    private Color otherColor(Color color) {
        if (color == Color.BLACK)
            return Color.WHITE;
        else if (color == Color.WHITE)
            return Color.BLACK;
        return Color.FALSE;
    }

    private Color checkTile(int y, int x) {
        if (y < 0 || y > 7 || x < 0 || x > 7)
            return Color.FALSE;
        else
            return board[y][x].getColor();
    }

    private void setScore() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                switch (checkTile(j, i)) {
                    case WHITE:
                        whiteTiles++;
                        break;
                    case BLACK:
                        blackTiles++;
                        break;
                    case BLANK:
                        break;
                }
                score = blackTiles - whiteTiles;

            }
        }
    }

    private int getScore() {
        return score;
    }

    @Override
    public int compareTo(Object o) {
        return (int) (((Othello) o).reward - this.reward);
    }

    class Tile {
        int x;
        int y;
        int flatReward;
        Color color;

        private Tile(int y, int x, Color color) {
            this.color = color;
            this.y = y;
            this.x = x;
            setReward();
        }

        private void setReward() {
            if(x == 0 || x == 7){
                if(y == 1 || y == 6)
                    flatReward = nextToCorner;
                else if(y == 0 || y == 7)
                    flatReward = corner;
                else
                    flatReward = border;
            }
            else if(y == 0 || y == 7){
                if(x == 1 || x == 6)
                    flatReward = nextToCorner;
                else
                    flatReward = border;
            }
        }

        private int getFlatReward(){
            return flatReward;
        }

        private void setColor(Color color) {
            this.color = color;
        }

        private Color getColor() {
            return color;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public int hashCode() {
            return (x + 1) * 10 + y + 1;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof Tile)) {
                return false;
            }
            Tile t = (Tile) o;
            return x == t.x && y == t.y;
        }
    }
}
