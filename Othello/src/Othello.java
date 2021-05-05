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
    int score = 0;
    int turnCounter = 1;
    int depth;
    boolean isOver = false;
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
        while(!isOver) {
            printBoard();
            if (turnCounter == 1) {
                botMoveStart();
                turnCounter++;
            } else if (turnCounter % 2 == 0) {
                playerMove();
                turnCounter++;
            }
            else if(turnCounter % 2 != 0){
                botMove();
                turnCounter++;
            }
            setScore();
            setOver();

        }
    }

    private void printBoard(){
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

    private void flipTiles(int y, int x){
        Color tile = board[y][x].getColor();
        if(checkTile(y+2, x) == tile)
            board[y+1][x].setColor(tile);
        if(checkTile(y-2, x) == tile)
            board[y-1][x].setColor(tile);
        if(checkTile(y, x+2) == tile)
            board[y][x+1].setColor(tile);
        if(checkTile(y, x-2) == tile)
            board[y][x-1].setColor(tile);
        if(checkTile(y-2, x-2) == tile)
            board[y-1][x-1].setColor(tile);
        if(checkTile(y-2, x+2) == tile)
            board[y-1][x+1].setColor(tile);
        if(checkTile(y+2, x+2) == tile)
            board[y+1][x+1].setColor(tile);
        if(checkTile(y+2, x-2) == tile)
            board[y+1][x-1].setColor(tile);

    }

    private void botMoveStart(){
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

    private void playerMove() {
        Color myColor = Color.BLACK;
        char[] move;
        do{
            move = input.next().toCharArray();
        }while(move.length != 2);
        int x = Character.getNumericValue(move[0]);
        int y = Character.getNumericValue(move[1]);
        System.out.println(y);
        System.out.println(x);
        makeMove(board, y, x, myColor);
    }

    private void setOver(){
        isOver = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; i < 8; i++){
                if (isValidMove(i, j, Color.WHITE) && turnCounter % 2 != 0);
                    isOver = false;
                if (isValidMove(i, j, Color.BLACK) && turnCounter % 2 == 0);
                }
            }
        }

    private void botMove() {
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
        realBotMoves.clear();
        makeMove(board, move.getY(), move.getX(), myColor);
    }

    private int miniMax(Color color){

        ArrayList<Othello> possibleMoves = new ArrayList<>();
        ArrayList<Othello> optimalMoves = new ArrayList<>();

        if(isOver)
            return getScore();

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(isValidMove(i, j, color)){
                    Othello theory = new Othello(new Tile[8][8], depth+1);
                    for(int k = 0; k < 8; k++){
                        for(int l = 0; l < 8; l++){
                            theory.board[k][l] = new Tile(board[k][l].getY(), board[k][l].getX(), board[k][l].getColor());
                        }
                    }
                    theory.makeMove(theory.board, i, j, color);
                    if(!theory.isOver && depth < 15){
                        theory.score += theory.miniMax(otherColor(color));
                    }
                    if(depth == 0){
                        realBotMoves.put(theory.lastMove, theory);
                    }

                }
            }
        }
        if(depth == 0){
            for(Othello t : realBotMoves.values()){
                t.miniMax(otherColor(color));
            }
            return -1;
        }

        for(Othello t : possibleMoves){
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
        Othello bestMove = optimalMoves.get(0);
        for(Othello o : optimalMoves){
            if(bestMove == o){
                continue;
            }else{
                if(bestMove.turnCounter > o.turnCounter)
                    bestMove = o;
            }
        }
        return bestMove.getScore();
    }

    private Tile getLastMove(){
        return lastMove;
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

    private boolean makeMove(Tile[][] board, int y, int x, Color color){
        if(isValidMove(y, x, color)){
            board[y][x].setColor(color);
            setScore();
            lastMove = board[y][x];
            flipTiles(y, x);
            return true;
        }
        return false;
    }

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
        boolean hasWhite = false;
        boolean hasBlack = false;
        for(int i = 0; i < 7; i++){
            for(int j = 0; i < 7; i++){
                switch(board[i][j].getColor()){
                    case BLACK:
                        score++;
                        hasBlack = true;
                        break;
                    case WHITE:
                        score--;
                        hasWhite = true;
                        break;
                    case BLANK:
                        break;
                }
            }
        }
        if(!hasBlack || !hasWhite)
            isOver = true;
    }

    private int getScore(){
        return score;
    }

    class Tile{
        int x;
        int y;
        Color color;

        public Tile(int y, int x, Color color){
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
