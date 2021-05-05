import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

enum Color{
    BLANK,
    WHITE, //BOT
    BLACK, //PLAYER
    FALSE
}

public class Othello {

    static Scanner input = new Scanner(System.in);
    static HashMap<Tile, Othello> realBotMoves = new HashMap<>();
    Tile[][] board;
    int playerScore = 0;
    int botScore = 0;
    int score = 0;
    int turnCounter = 1;
    int depth;
    Tile lastMove;

    private Othello(Tile[][] board, int depth){
        this.board = board;
        this.depth = depth;
    }

    public static void main(String[] args){
        Othello game = new Othello(new Tile[8][8], 0);
        System.out.println("Welcome to Othello. Enter the X and Y panel you wish to play.");
        game.run();
    }

    private void run(){
        constructBoard();
        while(!setOver()) {
            printBoard();
            if (turnCounter == 1) {
                botMoveStart();
                turnCounter++;
            } else if (turnCounter % 2 == 0) {
                System.out.println("Player turn");
                playerMove();
                turnCounter++;
            }
            else if(turnCounter % 2 != 0){
                System.out.println("Bot turn");
                botMove();
                turnCounter++;
            }

            System.out.println("Score: " + score);

        }
    }

    private void printBoard(){                          //PRINT THE INSTANCE BOARD
        System.out.println("Turn " + turnCounter);
        System.out.println("   0  1  2  3  4  5  6  7");
        for(int i = 0; i < 8; i++){
            System.out.print(i + " ");
            for(int j = 0; j < 8; j++){
                switch(board[i][j].getColor()) {
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

    private void flipTiles(int y, int x){               //CHECK IF ANY OCCUPIED TILES ARE BETWEEN TWO TILES OF OTHER COLOR
        Color tile = board[y][x].getColor();
        Tile toChange;
        if(checkTile(y+2, x) == tile && checkTile(y+1, x) == otherColor(tile))
            board[y+1][x].setColor(tile);
        if(checkTile(y-2, x) == tile && checkTile(y-1, x) == otherColor(tile))
            board[y-1][x].setColor(tile);
        if(checkTile(y, x+2) == tile && checkTile(y, x+1) == otherColor(tile))
            board[y][x+1].setColor(tile);
        if(checkTile(y, x-2) == tile && checkTile(y, x-1) == otherColor(tile))
            board[y][x-1].setColor(tile);
        if(checkTile(y-2, x-2) == tile && checkTile(y-1, x-1) == otherColor(tile))
            board[y-1][x-1].setColor(tile);
        if(checkTile(y-2, x+2) == tile && checkTile(y-1, x+1) == otherColor(tile))
            board[y-1][x+1].setColor(tile);
        if(checkTile(y+2, x+2) == tile && checkTile(y+1, x+1) == otherColor(tile))
            board[y+1][x+1].setColor(tile);
        if(checkTile(y+2, x-2) == tile && checkTile(y+1, x-1) == otherColor(tile))
            board[y+1][x-1].setColor(tile);

    }

    private void botMoveStart(){                        //SINCE START OF GAME IS PERFECTLY SYMMETRICAL, WE CAN RANDOMIZE THE BOT'S FIRST MOVE IF HE BEGINS
        double move = Math.floor(Math.random() * 3);
        switch((int) move){
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

    private void playerMove() {                     //REQUEST PLAYER MOVE
        Color myColor = Color.BLACK;
        char[] move;
        do{
            move = input.next().toCharArray();
        }while(move.length != 2);
        int x = Character.getNumericValue(move[0]);
        int y = Character.getNumericValue(move[1]);
        System.out.println("Tile " + x + "" + y);
        makeMove(board, y, x, myColor);
    }

    private boolean setOver(){                      //CHECK IF GAME IS OVER
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if (isValidMove(i, j, Color.WHITE))
                    return false;
                if (isValidMove(i, j, Color.BLACK))
                    return false;
                }
            }
        return true;
        }

    private void botMove() {                        //EXECUTE BOT MOVE WITH A CALCULATED LIST OF OPTIMAL MOVES
        Color myColor = Color.WHITE;
        miniMax(myColor);
        Tile move = null;
        for(Tile t : realBotMoves.keySet()){
            if(move == null)
                move = t;
            else{
                if(realBotMoves.get(t).getScore() > realBotMoves.get(move).getScore())
                    move = t;
            }
        }
        System.out.println("Tile " + move.getX() + "" + move.getY());
        makeMove(board, move.getY(), move.getX(), myColor);
        realBotMoves.clear();
    }

    private int miniMax(Color color){

        HashMap<Tile, Othello> possibleMoves = new HashMap<>();             //STORE INSTANCES POSSIBLE MOVES
        ArrayList<Othello> optimalMoves = new ArrayList<>();                //STORE OPTIMAL MOVES

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(isValidMove(i, j, color)){
                    Othello theory = new Othello(new Tile[8][8], depth+1);      //IF A VALID MOVE, CREATE SCENARIO AND INSERT INTO POSSIBLE MOVES
                    for(int k = 0; k < 8; k++){
                        for(int l = 0; l < 8; l++){
                            theory.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getColor());
                        }
                    }
                    theory.makeMove(theory.board, i, j, color);
                    possibleMoves.put(theory.lastMove, theory);

                }
            }
        }

        if(possibleMoves.isEmpty() || depth > 5){ //IF THERE ARE NO MORE MOVES OR IF WE'VE GONE TOO DEEP, RETURN SCORE
            return getScore();
        }
        for(Othello t : possibleMoves.values()){
            if(!t.setOver()) //                     IF WE'RE NOT AT THE END OF THE GAME, CALCULATE THE BEST MOVE FOR THE NEXT TURN, BUT FOR THE OTHER COLOR
                t.score += t.miniMax(otherColor(color));
            if(optimalMoves.isEmpty())
                optimalMoves.add(t);
            else
            if((t.getScore() > optimalMoves.get(0).getScore() && color == Color.WHITE) || (t.getScore() < optimalMoves.get(0).getScore() && color == Color.BLACK)){
                optimalMoves.clear();
                optimalMoves.add(t);
            }else if(t.getScore() == optimalMoves.get(0).getScore()){
                optimalMoves.add(t);
            }
        }

        Othello bestMove = null;
        for(Othello o : optimalMoves){//CHOOSE WINNING MOVE WITH FEWEST TURNS
            if(bestMove == null)
                bestMove = o;
            if(bestMove == o){
                continue;
            }else{
                if(bestMove.turnCounter > o.turnCounter)
                    bestMove = o;
            }
        }

        if(depth == 0){                                                     //IF WE'RE IN THE REAL GAME, INSERT POSSIBILITIES INTO A REAL LIST
            System.out.println("I'M BACK");
            realBotMoves.put(bestMove.lastMove, bestMove);
        }
        return bestMove.getScore();
    }

    private void constructBoard() {
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                board[i][j] = new Tile(i, j, Color.BLANK);
                if((i == 3 && j == 3) ||(i == 4 && j == 4))
                    board[i][j].setColor(Color.BLACK);
                else if((i == 3 && j == 4) || (i == 4 && j == 3))
                    board[i][j].setColor(Color.WHITE);
            }
        }
    }

    private void makeMove(Tile[][] board, int y, int x, Color color){
        if(isValidMove(y, x, color)){
            board[y][x].setColor(color);
            setScore();
            lastMove = board[y][x];
            flipTiles(y, x);
            setScore();
            setOver();
        }
    }

    /**
     * This method takes the coordinates of a tile on the game board, and determines whether performing a move on
     * it is viable for the requesting player.
     * First off, it checks if a player has already put something on this tile by confirming if the tile is of the color
     * type BLANK. If it isn't, we already know the move isn't possible and can return false. Otherwise, we check for
     * every direction (vertical, horizontal and diagonally) if one tile away there is a tile of the opposite color
     * and if two tiles away there is a tile of the same color. If this is true for any of the directions we check,
     * the move is possible according to the rules and we can return true. If none of these are true, we will end up
     * at the end of the method and return false.
     *
     * A possible error is if our int parameters are not valid tiles on the board. To circumvent this error, we use the
     * method checkTile() which determines if the tile exists on the board and returns an enum telling us if the
     * values represent a tile, and in that case, what state it is in.
     *
     *
     * @param  int  Takes two integers representing the coordinates of the tile we want to change
     * @param  Color The enum representing the state we wish to change the tile to.
     * @return      a boolean value representing if the move is executable or not
     */

    private boolean isValidMove(int y, int x, Color color) {
        if(board[y][x].getColor() == Color.BLANK){
            if(checkTile(y-2, x) == color && checkTile(y-1, x) == otherColor(color))
                return true;
            if(checkTile(y+2, x) == color && checkTile(y+1, x) == otherColor(color))
                return true;
            if(checkTile(y, x-2) == color && checkTile(y, x-1) == otherColor(color))
                return true;
            if(checkTile(y, x+2) == color && checkTile(y, x+1) == otherColor(color))
                return true;
            if(checkTile(y+2, x+2) == color && checkTile(y+1, x+1) == otherColor(color))
                return true;
            if(checkTile(y-2, x+2) == color && checkTile(y-1, x+1) == otherColor(color))
                return true;
            if(checkTile(y+2, x-2) == color && checkTile(y+1, x-1) == otherColor(color))
                return true;
            if(checkTile(y-2, x-2) == color && checkTile(y-1, x-1) == otherColor(color))
                return true;

        }
        return false;
    }

    private Color otherColor(Color color){
        if(color == Color.BLACK)
            return Color.WHITE;
        else if(color == Color.WHITE)
            return Color.BLACK;
        return Color.FALSE;
    }

    private Color checkTile(int y, int x){
        if(y < 0 || y > 7 || x < 0 || x > 7)
            return Color.FALSE;
        else
            return board[y][x].getColor();
    }

    private void setScore(){
        botScore = 0;
        playerScore = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                switch(checkTile(j, i)){
                    case BLACK:
                        playerScore++;
                        break;
                    case WHITE:
                        botScore++;
                        break;
                    case BLANK:
                        break;
                }
                score = botScore - playerScore;
            }
        }
    }

    private int getScore(){
        return score;
    }

    class Tile{
        int x;
        int y;
        Color color;

        private Tile(int y, int x, Color color){
            this.color = color;
            this.y = y;
            this.x = x;
        }

        private void setColor(Color color){
            this.color = color;
        }

        private Color getColor(){
            return color;
        }

        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
    }
}
