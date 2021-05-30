import java.util.*;

enum PLAYER {
    BLANK,
    MINIMAX, //BOT
    MARKOV, //PLAYER
    FALSE
}

public class Othello implements Comparable {

    double probability = 0.8;
    double flatFactor = 0.3;
    int nextToCorner = -20;
    int corner = 50;
    int border = 20;
    int winReward = 100;


    static Scanner input = new Scanner(System.in);
    static HashMap<Tile, Othello> realBotMoves = new HashMap<>();
    ArrayList<Othello> moves = new ArrayList<>();
    Tile[][] board;
    int miniTiles = 0; //MINIMAX PLAYER +
    int markovTiles = 0; //REINFORCEMENT PLAYER -
    int possibleMoves;
    int score = 0;
    int turnCounter = 1;
    int depth;
    float reward = 0;
    Tile lastMove;

    int foresight = 3;
    float discount = (float) 0.7;

    private Othello(Tile[][] board, int depth) {
        this.board = board;
        this.depth = depth;
    }

    public static void main(String[] args) throws InterruptedException {
        Othello game = new Othello(new Tile[8][8], 0);
        System.out.println("Welcome to Othello. Enter the X and Y panel you wish to play.");
        game.run();
    }

    private void run() throws InterruptedException {
        constructBoard();
        while (!setOver()) {


            Thread.sleep(500);
            if (turnCounter == 1) {
                miniMoveStart();
                turnCounter++;
            } else if (turnCounter % 2 != 0) {
                System.out.println("Mini turn");
                botMove(PLAYER.MINIMAX);
                //makeRandomMove(PLAYER.MINIMAX);
                turnCounter++;
            } else if (turnCounter % 2 == 0) {
                System.out.println("Markov turn");
                setRewards(PLAYER.MARKOV);
                makeOptimalMove(PLAYER.MARKOV);
                turnCounter++;
            }
            printBoard();

            System.out.println("Mini score: " + miniTiles);
            System.out.println("Markov score: " + markovTiles);

        }
    }

    private void makeRandomMove(PLAYER player) {
        ArrayList<Tile> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, player)) {
                    moves.add(new Tile(i, j, player));
                }
            }
        }
        if(moves.isEmpty())
            return;
        double random = Math.random() * moves.size();
        Tile move = moves.get((int)random);
        makeMove(board, move.getY(), move.getX(), player);
    }

    private void printBoard() {                          //PRINT THE INSTANCE BOARD
        System.out.println("Turn " + turnCounter);
        System.out.println("   0  1  2  3  4  5  6  7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                switch (board[i][j].getColor()) {
                    case MARKOV:
                        System.out.print("[-]");
                        break;
                    case MINIMAX:
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
        PLAYER tile = board[y][x].getColor();
        Tile toChange;
        if (checkTile(y + 2, x) == tile && checkTile(y + 1, x) == otherPlayer(tile))
            board[y + 1][x].setColor(tile);
        if (checkTile(y - 2, x) == tile && checkTile(y - 1, x) == otherPlayer(tile))
            board[y - 1][x].setColor(tile);
        if (checkTile(y, x + 2) == tile && checkTile(y, x + 1) == otherPlayer(tile))
            board[y][x + 1].setColor(tile);
        if (checkTile(y, x - 2) == tile && checkTile(y, x - 1) == otherPlayer(tile))
            board[y][x - 1].setColor(tile);
        if (checkTile(y - 2, x - 2) == tile && checkTile(y - 1, x - 1) == otherPlayer(tile))
            board[y - 1][x - 1].setColor(tile);
        if (checkTile(y - 2, x + 2) == tile && checkTile(y - 1, x + 1) == otherPlayer(tile))
            board[y - 1][x + 1].setColor(tile);
        if (checkTile(y + 2, x + 2) == tile && checkTile(y + 1, x + 1) == otherPlayer(tile))
            board[y + 1][x + 1].setColor(tile);
        if (checkTile(y + 2, x - 2) == tile && checkTile(y + 1, x - 1) == otherPlayer(tile))
            board[y + 1][x - 1].setColor(tile);

    }

    private void miniMoveStart() {                        //SINCE START OF GAME IS PERFECTLY SYMMETRICAL, WE CAN RANDOMIZE THE BOT'S FIRST MOVE IF HE BEGINS
        double move = Math.floor(Math.random() * 3);
        switch ((int) move) {
            case 0:
                makeMove(board, 2, 3, PLAYER.MINIMAX);
                break;
            case 1:
                makeMove(board, 3, 2, PLAYER.MINIMAX);
                break;
            case 2:
                makeMove(board, 4, 5, PLAYER.MINIMAX);
                break;
            case 3:
                makeMove(board, 5, 4, PLAYER.MINIMAX);
                break;
        }
    }

    private void manualMove() {
        PLAYER myColor = PLAYER.MARKOV;
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

    private boolean hasValidMove(PLAYER color) {
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
                if (isValidMove(i, j, PLAYER.MINIMAX))
                    return false;
                if (isValidMove(i, j, PLAYER.MARKOV))
                    return false;
            }
        }
        return true;
    }


    private void botMove(PLAYER color) {                        //EXECUTE BOT MOVE WITH A CALCULATED LIST OF OPTIMAL MOVES
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

    private Othello miniMax(PLAYER color) {

        HashMap<Tile, Othello> possibleMoves = new HashMap<>();             //STORE INSTANCES POSSIBLE MOVES
        ArrayList<Othello> optimalMoves = new ArrayList<>();                //STORE OPTIMAL MOVES

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, color)) {
                    Othello theory = new Othello(new Tile[8][8], depth + 1);//IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {

                            theory.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getColor());

                        }//IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    }
                    theory.makeMove(theory.board, i, j, color);
                    possibleMoves.put(theory.lastMove, theory);


                }
            }
        }
        this.possibleMoves = possibleMoves.size();
        if (possibleMoves.isEmpty() || depth > 5) { //IF THERE ARE NO MORE MOVES OR IF WE'VE GONE TOO DEEP, RETURN SCORE
            return this;
        }
        for (Othello t : possibleMoves.values()) {
            if (!t.setOver()) //                     IF WE'RE NOT AT THE END OF THE GAME, CALCULATE THE BEST MOVE FOR THE NEXT TURN, BUT FOR THE OTHER COLOR
                t.score += t.miniMax(otherPlayer(color)).getScore();
            if (optimalMoves.isEmpty())
                optimalMoves.add(t);
            else if ((t.getScore() > optimalMoves.get(0).getScore() && color == PLAYER.MINIMAX) || (t.getScore() < optimalMoves.get(0).getScore() && color == PLAYER.MARKOV)) {
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

    private void setRewards(PLAYER color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, color)) {
                    Othello future = new Othello(new Tile[8][8], depth + 1);      //IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {
                            future.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getColor());
                        }
                    } //Create a board that simulates a move
                    future.makeMove(future.board, i, j, color);
                    future.setScore();


                    future.reward += future.getScore();//Add the added score of making the move to the reward

                    if(future.setOver() && score > 0)
                        future.reward += winReward;
                    else if(future.setOver() && score < 0)
                        future.reward -= winReward;


                    double flatReward = flatFactor;
                    if(color.equals(PLAYER.MINIMAX))
                        flatReward = flatReward * -1;
                    future.reward += board[i][j].getFlatReward() * flatFactor;
                    moves.add(future);
                    if (depth < foresight && future.hasValidMove(otherPlayer(color))) { //Check if we've looked as far into the future as we want
                        future.setRewards(otherPlayer(color)); //Assume the other player does a similar calculation
                        double randomizer = Math.random() * 10;
                        Tile temp = future.lastMove;
                        if (randomizer < probability || future.moves.size() < 2)
                            future.makeOptimalMove(otherPlayer(color));
                        else
                            future.makeSecondOptimalMove(otherPlayer(color));
                        future.setRewards(color);
                        future.lastMove = temp;
                        if (future.hasValidMove(color))
                            future.reward += future.moves.get(0).reward * discount; //The future move is worth it's own accumulated value plus future optimal values times a discount value
                    }

                }
            }
        }
    }

    private void makeSecondOptimalMove(PLAYER color) {
        Collections.sort(moves);
        Tile move;
        if (color.equals(PLAYER.MARKOV)) {
            move = moves.get(moves.size() - 2).lastMove;
        } else {
            move = moves.get(1).lastMove;
        }
        makeMove(board, move.getY(), move.getX(), color);
    }

    private void makeOptimalMove(PLAYER color) {
        Collections.sort(moves);
        Othello bestMove;
        if(depth == 0) {
            for (Othello move : moves) {
                System.out.println("Tile: " + move.lastMove.getX() + "" + move.lastMove.getY() + " Reward: " + move.reward + " Score: " + move.score);
            }
        }
        if (moves.size() == 0)
            return;
        Tile move;
        if (color.equals(PLAYER.MARKOV)) {
            move = moves.get(0).lastMove;
        } else {
            move = moves.get(moves.size() - 1).lastMove;
        }
        if (depth == 0)
            System.out.println(move.getY() + " " + move.getX());
        makeMove(board, move.getY(), move.getX(), color);
        moves.clear();
    }

    private void constructBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Tile(i, j, PLAYER.BLANK);
                if ((i == 3 && j == 3) || (i == 4 && j == 4))
                    board[i][j].setColor(PLAYER.MARKOV);
                else if ((i == 3 && j == 4) || (i == 4 && j == 3))
                    board[i][j].setColor(PLAYER.MINIMAX);
            }
        }
    }

    private void makeMove(Tile[][] board, int y, int x, PLAYER color) {
        if (isValidMove(y, x, color)) {
            board[y][x].setColor(color);
            setScore();
            lastMove = board[y][x];
            flipTiles(y, x);
            setScore();
            setOver();
        }
    }

    private boolean isValidMove(int y, int x, PLAYER color) {
        if (board[y][x].getColor() == PLAYER.BLANK) {
            if (checkTile(y - 2, x) == color && checkTile(y - 1, x) == otherPlayer(color))
                return true;
            if (checkTile(y + 2, x) == color && checkTile(y + 1, x) == otherPlayer(color))
                return true;
            if (checkTile(y, x - 2) == color && checkTile(y, x - 1) == otherPlayer(color))
                return true;
            if (checkTile(y, x + 2) == color && checkTile(y, x + 1) == otherPlayer(color))
                return true;
            if (checkTile(y + 2, x + 2) == color && checkTile(y + 1, x + 1) == otherPlayer(color))
                return true;
            if (checkTile(y - 2, x + 2) == color && checkTile(y - 1, x + 1) == otherPlayer(color))
                return true;
            if (checkTile(y + 2, x - 2) == color && checkTile(y + 1, x - 1) == otherPlayer(color))
                return true;
            if (checkTile(y - 2, x - 2) == color && checkTile(y - 1, x - 1) == otherPlayer(color))
                return true;

        }
        return false;
    }

    private PLAYER otherPlayer(PLAYER color) {
        if (color == PLAYER.MARKOV)
            return PLAYER.MINIMAX;
        else if (color == PLAYER.MINIMAX)
            return PLAYER.MARKOV;
        return PLAYER.FALSE;
    }

    private PLAYER checkTile(int y, int x) {
        if (y < 0 || y > 7 || x < 0 || x > 7)
            return PLAYER.FALSE;
        else
            return board[y][x].getColor();
    }

    private void setScore() {
        miniTiles = 0;
        markovTiles = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                switch (checkTile(j, i)) {
                    case MINIMAX:
                        miniTiles++;
                        break;
                    case MARKOV:
                        markovTiles++;
                        break;
                    case BLANK:
                        break;
                }
                score = markovTiles - miniTiles;

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
        PLAYER color;

        private Tile(int y, int x, PLAYER color) {
            this.color = color;
            this.y = y;
            this.x = x;
            setReward();
        }

        private void setReward() {
            if (x == 0 || x == 7) {
                if (y == 1 || y == 6)
                    flatReward = nextToCorner;
                else if (y == 0 || y == 7)
                    flatReward = corner;
                else
                    flatReward = border;
            } else if (y == 0 || y == 7) {
                if (x == 1 || x == 6)
                    flatReward = nextToCorner;
                else
                    flatReward = border;
            }
        }

        int getFlatReward() {
            return flatReward;
        }

        private void setColor(PLAYER color) {
            this.color = color;
        }

        private PLAYER getColor() {
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
