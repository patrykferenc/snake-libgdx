import java.io.*;
import java.util.ListIterator;
import java.util.Scanner;

public final class GameBoard {
    public static int score, highscore;
    private final double boardHeight, boardWidth;
    private final Snake mySnake;
    private final Food myFood;
    private final SpecialFood mySpecialFood;
    private final GameSoundPlayer mySoundPlayer;
    private final Saw verticalSaw, horizontalSaw, diagonalUpSaw, diagonalDownSaw;

    public GameBoard() throws FileNotFoundException {
        score = 0;
        ReaderOfHighscore r = new ReaderOfHighscore();
        r.readHighScore();
        highscore = r.getHighscore();
        boardHeight = GameSettings.BOARD_HEIGHT;
        boardWidth = GameSettings.BOARD_WIDTH;

        myFood = new Food();
        mySpecialFood = new SpecialFood();
        mySnake = new Snake();
        mySoundPlayer = new GameSoundPlayer();
        verticalSaw = new Saw(4, GameSettings.SMALL_SAW_SIZE);
        horizontalSaw = new Saw(1, GameSettings.BIG_SAW_SIZE);
        diagonalUpSaw = new Saw(3, GameSettings.SMALL_SAW_SIZE);
        diagonalDownSaw = new Saw(2, GameSettings.BIG_SAW_SIZE);
    }
    public GameBoard(boolean test){
        score = 0;
        ReaderOfHighscore r = new ReaderOfHighscore();
        r.readHighScore();
        highscore = r.getHighscore();
        boardHeight = GameSettings.BOARD_HEIGHT;
        boardWidth = GameSettings.BOARD_WIDTH;

        myFood = new Food();
        mySpecialFood = new SpecialFood();
        mySnake = new Snake();
        mySoundPlayer = null;
        verticalSaw = new Saw(4, GameSettings.SMALL_SAW_SIZE);
        horizontalSaw = new Saw(1, GameSettings.BIG_SAW_SIZE);
        diagonalUpSaw = new Saw(3, GameSettings.SMALL_SAW_SIZE);
        diagonalDownSaw = new Saw(2, GameSettings.BIG_SAW_SIZE);
    }

    public Snake getMySnake() {
        return mySnake;
    }

    public Food getMyFood() {
        return myFood;
    }

    public SpecialFood getMySpecialFood() {
        return mySpecialFood;
    }

    public Saw getMyVerticalSaw() {
        return verticalSaw;
    }

    public Saw getMyHorizontalSaw() {
        return horizontalSaw;
    }

    public Saw getMyDiagonalUpSaw() {
        return diagonalUpSaw;
    }

    public Saw getMyDiagonalDownSaw() {
        return diagonalDownSaw;
    }

    public void updateGame(PointVector mousePosition) throws IOException {
        mySnake.move(mousePosition);
        mySpecialFood.move();
        verticalSaw.verticalMove();
        horizontalSaw.horizontalMove();
        diagonalUpSaw.diagonalMoveUp();
        diagonalDownSaw.diagonalMoveDown();
        Controller c = new Controller();
        checkBorders();
        if (checkTailCollision(getMySnake()) || checkSawCollision()) {
            mySoundPlayer.playSnakeCrashedSound();
            c.setLosePane();
            Controller.disablePauseButton();
            Controller.layerPane.getChildren().add(Controller.losePane);
            Controller.gameLoop.stop();
            Controller.soundtrackPlayer.stop();
            if (score > highscore) {
                ReaderOfHighscore r = new ReaderOfHighscore();
                r.setHighscore(score);
                r.writeHighScore();
            }
        }
        if (checkFood(new PointVector(mySnake.getHead().getX(), mySnake.getHead().getY()), getMyFood().getPosition())
                || checkSpecialFood(new PointVector(mySnake.getHead().getX(), mySnake.getHead().getY()), getMySpecialFood().getPosition())) {
            mySoundPlayer.playFoodEatenSound();
            score = mySnake.getSizeForUser();
            System.out.println("Score:" + score);
        }
    }

    private boolean checkFood(PointVector headPosition, PointVector foodPosition) {
        headPosition.subtract(foodPosition);
        if (headPosition.length() < GameSettings.SEGMENT_SIZE) {
            getMyFood().respawn();
            for (int i = 0; i < GameSettings.FOOD_MULTIPLIER; i++) mySnake.addBodySegments();
            return true;
        }
        return false;
    }

    private boolean checkSpecialFood(PointVector headPosition, PointVector foodPosition) {
        if (mySpecialFood.isAlive()) {
            var distance = new PointVector(headPosition);
            distance.subtract(foodPosition);
            if (distance.length() < GameSettings.SEGMENT_SIZE) {
                for (int i = 0; i < GameSettings.SPECIAL_FOOD_MULTIPLIER; i++) mySnake.addBodySegments();
                mySpecialFood.setLongevity(0);
                return true;
            } else
                return false;
        } else {
            getMySpecialFood().respawn();
            return false;
        }
    }

    private boolean checkTailCollision(Snake mySnake) {
        final int biteLimit = (int) GameSettings.SEGMENT_SIZE; // nie da się zbytnio zderzyć poniżej
        //var mySnake = getMySnake();

        if ((mySnake.getActualSize()) > biteLimit) {
            var myBodySegments = mySnake.getBodySegments();
            ListIterator<PointVector> mySnakeIterator = myBodySegments.listIterator(biteLimit);

            var headPosition = mySnake.getHead();

            while (mySnakeIterator.hasNext()) {
                var distance = new PointVector(headPosition);
                distance.subtract(mySnakeIterator.next());
                if (distance.length() < (GameSettings.SEGMENT_SIZE / 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSawCollision() {
        var mySnake = getMySnake();
        for (PointVector bodySegment : mySnake.getBodySegments()) {
            var distanceToHorizontalSaw = new PointVector(bodySegment);
            var distanceToVerticalSaw = new PointVector(bodySegment);
            var distanceToDiagonalUpSaw = new PointVector(bodySegment);
            var distanceToDiagonalDownSaw = new PointVector(bodySegment);
            distanceToHorizontalSaw.subtract(getMyHorizontalSaw().getLocation());
            distanceToVerticalSaw.subtract(getMyVerticalSaw().getLocation());
            distanceToDiagonalUpSaw.subtract(getMyDiagonalUpSaw().getLocation());
            distanceToDiagonalDownSaw.subtract(getMyDiagonalDownSaw().getLocation());
            if (distanceToHorizontalSaw.length() < (GameSettings.SEGMENT_SIZE + getMyHorizontalSaw().getSize()) / 2.3 ||
                    distanceToVerticalSaw.length() < (GameSettings.SEGMENT_SIZE + getMyVerticalSaw().getSize()) / 2.3 ||
                    distanceToDiagonalUpSaw.length() < ((GameSettings.SEGMENT_SIZE + getMyDiagonalUpSaw().getSize()) / 2.3) ||
                    distanceToDiagonalDownSaw.length() < (GameSettings.SEGMENT_SIZE + getMyDiagonalDownSaw().getSize()) / 2.3) {
                return true;
            }
        }
        return false;
    }
    public boolean checkSawCollision_testVersion(Snake mySnake) {
        for (PointVector bodySegment : mySnake.getBodySegments()) {
            var distanceToHorizontalSaw = new PointVector(bodySegment);
            var distanceToVerticalSaw = new PointVector(bodySegment);
            var distanceToDiagonalUpSaw = new PointVector(bodySegment);
            var distanceToDiagonalDownSaw = new PointVector(bodySegment);
            distanceToHorizontalSaw.subtract(getMyHorizontalSaw().getLocation());
            distanceToVerticalSaw.subtract(getMyVerticalSaw().getLocation());
            distanceToDiagonalUpSaw.subtract(getMyDiagonalUpSaw().getLocation());
            distanceToDiagonalDownSaw.subtract(getMyDiagonalDownSaw().getLocation());
            if (distanceToHorizontalSaw.length() < (GameSettings.SEGMENT_SIZE + getMyHorizontalSaw().getSize()) / 2 ||
                distanceToVerticalSaw.length() < (GameSettings.SEGMENT_SIZE + getMyVerticalSaw().getSize()) / 2 ||
                distanceToDiagonalUpSaw.length() < ((GameSettings.SEGMENT_SIZE + getMyDiagonalUpSaw().getSize()) / 2) ||
                distanceToDiagonalDownSaw.length() < (GameSettings.SEGMENT_SIZE + getMyDiagonalDownSaw().getSize()) / 2) {
                return true;
            }
        }
        return false;
    }

    private void checkBorders() {
        ListIterator<PointVector> mySnakeIterator = mySnake.getBodySegments().listIterator();
        final var head = mySnakeIterator.next();
        var newHead = new PointVector(head.getX(), head.getY());

        if (head.getX() > boardWidth) {
            newHead.setX(0);
            mySnakeIterator.set(newHead);
        } else if (head.getX() < 0) {
            newHead.setX(boardWidth);
            mySnakeIterator.set(newHead);
        }

        if (head.getY() > boardHeight) {
            newHead.setY(0);
            mySnakeIterator.set(newHead);
        } else if (head.getY() < 0) {
            newHead.setY(boardHeight);
            mySnakeIterator.set(newHead);
        }
    }
    public boolean checkBorders_testVersion(PointVector head) {
        return head.getX() > boardWidth || head.getX() < 0 || head.getY() > boardHeight || head.getY() < 0;
    }
    public boolean getCheckFood(PointVector headPosition, PointVector snakePosition){
        return checkFood(headPosition, snakePosition);
    }
    public boolean getCheckSpecialFood(PointVector headPosition, PointVector snakePosition){
        return checkSpecialFood(headPosition, snakePosition);
    }
    public boolean getCheckTailCollision(Snake s){
        return checkTailCollision(s);
    }
}