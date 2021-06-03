import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

enum PLAYER {
    BLANK,
    MINIMAX, //BOT
    MARKOV, //PLAYER
    FALSE
}

public class Othello implements Comparable {
    static Scanner inFile;
    String fileName = "src/weights";

    // Used for adjusting weights
    double preAdjustedWeight;
    double postAdjustedWeight;

    double probability; //Base probability that opponent does a good move
    double flatFactor;
    float discount;
    int opposingMovesReward;


    boolean randomBot; //Boolean that determines if opponent is minimax or a randomizer.

    //FLAT REWARDS
    int nextToCorner = -20;
    int corner = 5;
    int border = 2;
    int winReward = 100;

    //Amount of steps ahead to check
    int foresight = 4;

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

    private Othello(Tile[][] board, int depth) throws FileNotFoundException {
        this.board = board;
        this.depth = depth;
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        Othello game = new Othello(new Tile[8][8], 0);
        System.out.println("Welcome");
        game.loadWeights();
        game.adjustRandomWeight();
        int loop = 100;
        while (loop > 0) {
            int noOfGames = 10;

            int wins = game.trainBot(game, noOfGames);
            System.out.println("\nTotal wins: " + wins);
            if (wins > noOfGames / 2) {
                try {
                    game.writeNewWeightsToFile(); // update weight in file
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            loop--;
        }
    }

    /**
     * Train bot through letting it play noOfGames games with its new weights
     * @param game
     * @param noOfGames
     * @return
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    private int trainBot(Othello game, int noOfGames) throws FileNotFoundException, InterruptedException {
        randomBot = Math.random() < 0.5 ? true : false;
        int won = 0;
        for (int i = 0; i < noOfGames; i++) {

            System.out.print("Running game no. " + i + " ... ");
            String winner = game.run();
            if (winner.equals("markov")) {
                won++;
                System.out.println("Won match");
            } else {
                System.out.println("Lost match");
            }
            turnCounter = 1;
        }
        return won;
    }

    /**
     * Randomize a weight to be adjusted
     */
    private void adjustRandomWeight() {
        Random random = new Random();
        int weight = random.nextInt(4);
        double sign = random.nextDouble();
        String modifier = sign < 0.5 ? String.valueOf(random.nextDouble()) : String.valueOf(random.nextDouble() * -1);
        switch(weight){
            case 0:
                adjustWeight("probability", modifier);
                break;
            case 1:
                adjustWeight("flatFactor", modifier);
                break;
            case 2:
                adjustWeight("discount", modifier);
                break;
            case 3:
                adjustWeight("opposingMovesReward", modifier);
                break;
        }
    }

    /**
     * Adjust a given weight with a given adjustment factor
     * @param weight
     * @param adjustment
     */
    private void adjustWeight(String weight, String adjustment) {
        System.out.println("Adjusting weights");

        switch (weight) {
            case "probability":
                preAdjustedWeight = probability;
                probability *= Double.parseDouble(adjustment);
                postAdjustedWeight = probability;
                break;
            case "flatFactor":
                preAdjustedWeight = flatFactor;
                flatFactor *= Double.parseDouble(adjustment);
                postAdjustedWeight = flatFactor;
                break;
            case "discount":
                preAdjustedWeight = discount;
                discount *= Double.parseDouble(adjustment);
                postAdjustedWeight = discount;
                break;
            case "opposingMovesReward":
                preAdjustedWeight = opposingMovesReward;
                opposingMovesReward *= Double.parseDouble(adjustment);
                postAdjustedWeight = opposingMovesReward;
                break;
            default:
                System.out.println("No weight exists with the given name.");
        }
    }

    /** Write new better weight to file
     *  code from https://stackoverflow.com/questions/8563294/modifying-existing-file-content-in-java
     **/
    private void writeNewWeightsToFile() throws IOException {
        System.out.println("Saving weights. Searching for " + preAdjustedWeight);

        List<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            if (line.contains(Double.toString(preAdjustedWeight))) {
                newLines.add(line.replace(Double.toString(preAdjustedWeight), "" + Double.toString(postAdjustedWeight)));
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get(fileName), newLines, StandardCharsets.UTF_8);
    }

    /**
     * Load the weights from the file
     */
    private void loadWeights() {
        try {
            inFile = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        probability = Double.parseDouble(inFile.nextLine());
        flatFactor = Double.parseDouble(inFile.nextLine());
        foresight = Integer.parseInt(inFile.nextLine());
        discount = Float.parseFloat(inFile.nextLine());
        opposingMovesReward = Integer.parseInt(inFile.nextLine());

    }

    /**
     * Runs a game.
     * @return
     * @throws InterruptedException
     * @throws FileNotFoundException
     */
    private String run() throws InterruptedException, FileNotFoundException {
        constructBoard();
        while (!setOver()) {


            //Thread.sleep(500);
            if (turnCounter == 1) {
                miniMoveStart();
                turnCounter++;
            } else if (turnCounter % 2 != 0) {
//                System.out.println("Mini turn");
                if (!randomBot)
                    botMove(PLAYER.MINIMAX);
                else
                    makeRandomMove(PLAYER.MINIMAX);

                turnCounter++;
            } else if (turnCounter % 2 == 0) {
//                System.out.println("Markov turn");
                setRewards(PLAYER.MARKOV);
                makeOptimalMove(PLAYER.MARKOV);
                turnCounter++;
            }
            printBoard();
//
            System.out.println("Mini score: " + miniTiles);
            System.out.println("Markov score: " + markovTiles);

        }

        return (miniTiles > markovTiles ? "mini" : (miniTiles < markovTiles ? "markov" : "tie"));
    }

    /**
     * Make a random move
     * @param player
     */
    private void makeRandomMove(PLAYER player) {
        ArrayList<Tile> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, player)) {
                    moves.add(new Tile(i, j, player));
                }
            }
        }
        if (moves.isEmpty())
            return;
        double random = Math.random() * moves.size();
        Tile move = moves.get((int) random);
        makeMove(board, move.getY(), move.getX(), player);
    }

    /**
     * Print out the board.
     */
    private void printBoard() {                          //PRINT THE INSTANCE BOARD
        System.out.println("Turn " + turnCounter);
        System.out.println("   0  1  2  3  4  5  6  7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                switch (board[i][j].getPlayer()) {
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

    /**
     * Check for tiles that are to be flipped.
     * @param y
     * @param x
     */
    private void flipTiles(int y, int x) {               //CHECK IF ANY OCCUPIED TILES ARE BETWEEN TWO TILES OF OTHER PLAYER
        PLAYER tile = board[y][x].getPlayer();
        Tile toChange;
        if (checkTile(y + 2, x) == tile && checkTile(y + 1, x) == otherPlayer(tile))
            board[y + 1][x].setPlayer(tile);
        if (checkTile(y - 2, x) == tile && checkTile(y - 1, x) == otherPlayer(tile))
            board[y - 1][x].setPlayer(tile);
        if (checkTile(y, x + 2) == tile && checkTile(y, x + 1) == otherPlayer(tile))
            board[y][x + 1].setPlayer(tile);
        if (checkTile(y, x - 2) == tile && checkTile(y, x - 1) == otherPlayer(tile))
            board[y][x - 1].setPlayer(tile);
        if (checkTile(y - 2, x - 2) == tile && checkTile(y - 1, x - 1) == otherPlayer(tile))
            board[y - 1][x - 1].setPlayer(tile);
        if (checkTile(y - 2, x + 2) == tile && checkTile(y - 1, x + 1) == otherPlayer(tile))
            board[y - 1][x + 1].setPlayer(tile);
        if (checkTile(y + 2, x + 2) == tile && checkTile(y + 1, x + 1) == otherPlayer(tile))
            board[y + 1][x + 1].setPlayer(tile);
        if (checkTile(y + 2, x - 2) == tile && checkTile(y + 1, x - 1) == otherPlayer(tile))
            board[y + 1][x - 1].setPlayer(tile);

    }

    /**
     * Perform starting move for miniMax bot.
     */
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

    /**
     * Make a manual move
     */
    private void manualMove() {
        PLAYER myPlayer = PLAYER.MARKOV;
        if (hasValidMove(myPlayer)) {
            char[] move;
            do {
                move = input.next().toCharArray();
            } while (move.length != 2 && !isValidMove(move[1], move[0], myPlayer));
            int x = Character.getNumericValue(move[0]);
            int y = Character.getNumericValue(move[1]);
            System.out.println("Tile " + x + "" + y);
            makeMove(board, y, x, myPlayer);
        }
    }

    /**
     * Check if a move is valid.
     * @param player
     * @return
     */
    private boolean hasValidMove(PLAYER player) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if any player has a valid move and can continue the game
     * @return boolean that says the state of the game
     */
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


    /**
     * Make automated move with MiniMax.
     * @param player
     * @throws FileNotFoundException
     */
    private void botMove(PLAYER player) throws FileNotFoundException {                        //EXECUTE BOT MOVE WITH A CALCULATED LIST OF OPTIMAL MOVES
        miniMax(player);
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
            makeMove(board, move.getY(), move.getX(), player);
            realBotMoves.clear();
        } else {
        }
    }

    /**
     * MiniMax algorithm
     * @param player
     * @return Othello, a simulated board
     * @throws FileNotFoundException
     */
    private Othello miniMax(PLAYER player) throws FileNotFoundException {

        HashMap<Tile, Othello> possibleMoves = new HashMap<>();             //STORE INSTANCES POSSIBLE MOVES
        ArrayList<Othello> optimalMoves = new ArrayList<>();                //STORE OPTIMAL MOVES

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidMove(i, j, player)) {
                    Othello theory = new Othello(new Tile[8][8], depth + 1);//IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {

                            theory.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getPlayer());

                        }//IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    }
                    theory.makeMove(theory.board, i, j, player);
                    possibleMoves.put(theory.lastMove, theory);


                }
            }
        }
        this.possibleMoves = possibleMoves.size();
        if (possibleMoves.isEmpty() || depth > 5) { //IF THERE ARE NO MORE MOVES OR IF WE'VE GONE TOO DEEP, RETURN SCORE
            return this;
        }
        for (Othello t : possibleMoves.values()) {
            if (!t.setOver()) //                     IF WE'RE NOT AT THE END OF THE GAME, CALCULATE THE BEST MOVE FOR THE NEXT TURN, BUT FOR THE OTHER PLAYER
                t.score += t.miniMax(otherPlayer(player)).getScore();
            if (optimalMoves.isEmpty())
                optimalMoves.add(t);
            else if ((t.getScore() > optimalMoves.get(0).getScore() && player == PLAYER.MINIMAX) || (t.getScore() < optimalMoves.get(0).getScore() && player == PLAYER.MARKOV)) {
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

    private void setRewards(PLAYER player) throws FileNotFoundException {
        possibleMoves = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                /**Proceed if the tile in the current iteration is a viable move **/
                if (isValidMove(i, j, player)) {
                    possibleMoves++;
                    Othello future = simulateBoard();

                    /**Make the move in a simulated board**/
                    future.makeMove(future.board, i, j, player);
                    future.setScore();

                    /**Add the added score to the reward of the simulated game **/
                    future.reward += future.getScore() - getScore();

                    /**Add the flat reward for the tile taken**/
                    future.reward += player.equals(PLAYER.MINIMAX) ? (board[i][j].getFlatReward() * flatFactor * -1) : (future.reward += board[i][j].getFlatReward() * flatFactor); /**Add the flat reward for the tile taken**/

                    /**Set the reward depending on which player we are simulating the move on**/
                    if (future.setOver()) {
                        if (score < 0)
                            future.reward += player.equals(PLAYER.MINIMAX) ? (winReward * -1) : winReward;
                        else if (score > 0)
                            future.reward -= player.equals(PLAYER.MINIMAX) ? (winReward * -1) : winReward;
                    }

                    /**If we want to go deeper**/
                    if (future.hasValidMove(otherPlayer(player)) && depth < foresight) {
                        Tile temp = future.lastMove;
                        future.setRewards(otherPlayer(player));

                        /**Add reward for limiting the other player's options.**/
                        future.reward += player.equals(PLAYER.MINIMAX) ? possibleMoves * opposingMovesReward : possibleMoves * opposingMovesReward * -1;
                        if (Math.random() * 10 < probability || future.moves.size() < 2)
                            future.makeOptimalMove(otherPlayer(player)); /**Assume that MOST of the time the other player will do an optimal move **/
                        else
                            future.makeSecondOptimalMove(otherPlayer(player));/**Assume that sometimes the other player will do the second best move **/


                        future.setRewards(player);
                        if (future.hasValidMove(player))

                        /**The move is worth it's own accumulated value plus future optimal values times a discount value, according to the Markov Decision Process**/
                            future.reward += player.equals(PLAYER.MINIMAX) ? future.moves.get(0).reward * discount * -1 : future.moves.get(0).reward * discount;
                        future.lastMove = temp;
                    }
                    moves.add(future);


                }

            }


        }
    }

    /**
     * Simulate performing the move by creating a new board and playing it out
     * @return
     * @throws FileNotFoundException
     */
    private Othello simulateBoard() throws FileNotFoundException {
        Othello future = new Othello(new Tile[8][8], depth + 1);
        for (int k = 0; k < 8; k++) {
            for (int l = 0; l < 8; l++) {
                future.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getPlayer());
            }
        } //Create a board that simulates a move
        return future;
    }

    /**
     * Choose the next to best move for the player.
     * @param player
     */
    private void makeSecondOptimalMove(PLAYER player) {
        Collections.sort(moves);
        Tile move;
        if (player.equals(PLAYER.MARKOV)) {
            move = moves.get(moves.size() - 2).lastMove;
        } else {
            move = moves.get(1).lastMove;
        }
        makeMove(board, move.getY(), move.getX(), player);
    }

    /**
     * Choose the best move for the player.
     * @param player
     */
    private void makeOptimalMove(PLAYER player) {
        Collections.sort(moves);
        if (moves.size() == 0)
            return;
        Tile move;
        if (player.equals(PLAYER.MARKOV)) {
            move = moves.get(0).lastMove;
        } else {
            move = moves.get(moves.size() - 1).lastMove;
        }
        makeMove(board, move.getY(), move.getX(), player);
        moves.clear();
    }

    /**
     * Create the board.
     */
    private void constructBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Tile(i, j, PLAYER.BLANK);
                if ((i == 3 && j == 3) || (i == 4 && j == 4))
                    board[i][j].setPlayer(PLAYER.MARKOV);
                else if ((i == 3 && j == 4) || (i == 4 && j == 3))
                    board[i][j].setPlayer(PLAYER.MINIMAX);
            }
        }
    }

    /**
     * Execute a move
     * @param board
     * @param y
     * @param x
     * @param player
     */

    private void makeMove(Tile[][] board, int y, int x, PLAYER player) {
        if (isValidMove(y, x, player)) {
            board[y][x].setPlayer(player);
            setScore();
            lastMove = board[y][x];
            flipTiles(y, x);
            setScore();
            setOver();
        }
    }

    /**
     * Check if a move is valid.
     * @param y
     * @param x
     * @param player
     * @return
     */
    private boolean isValidMove(int y, int x, PLAYER player) {
        if (board[y][x].getPlayer() == PLAYER.BLANK) {
            if (checkTile(y - 2, x) == player && checkTile(y - 1, x) == otherPlayer(player))
                return true;
            if (checkTile(y + 2, x) == player && checkTile(y + 1, x) == otherPlayer(player))
                return true;
            if (checkTile(y, x - 2) == player && checkTile(y, x - 1) == otherPlayer(player))
                return true;
            if (checkTile(y, x + 2) == player && checkTile(y, x + 1) == otherPlayer(player))
                return true;
            if (checkTile(y + 2, x + 2) == player && checkTile(y + 1, x + 1) == otherPlayer(player))
                return true;
            if (checkTile(y - 2, x + 2) == player && checkTile(y - 1, x + 1) == otherPlayer(player))
                return true;
            if (checkTile(y + 2, x - 2) == player && checkTile(y + 1, x - 1) == otherPlayer(player))
                return true;
            if (checkTile(y - 2, x - 2) == player && checkTile(y - 1, x - 1) == otherPlayer(player))
                return true;

        }
        return false;
    }

    /**
     * Return the opposite player.
     * @param player
     * @return
     */
    private PLAYER otherPlayer(PLAYER player) {
        if (player == PLAYER.MARKOV)
            return PLAYER.MINIMAX;
        else if (player == PLAYER.MINIMAX)
            return PLAYER.MARKOV;
        return PLAYER.FALSE;
    }

    /**
     * Check state of a tile.
     * @param y
     * @param x
     * @return
     */
    private PLAYER checkTile(int y, int x) {
        if (y < 0 || y > 7 || x < 0 || x > 7)
            return PLAYER.FALSE;
        else
            return board[y][x].getPlayer();
    }

    /**
     * Set the score.
     */
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

    /**
     * Return the score.
     * @return
     */
    private int getScore() {
        return score;
    }

    /**
     * Overridden compareTo that enables ordering simulated boards by their reward.
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        return (int) (((Othello) o).reward - this.reward);
    }

    /**
     * Inner Tile class.
     */

    class Tile {
        int x;
        int y;
        int flatReward;
        PLAYER player;

        private Tile(int y, int x, PLAYER player) {
            this.player = player;
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

        private void setPlayer(PLAYER player) {
            this.player = player;
        }

        private PLAYER getPlayer() {
            return player;
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
