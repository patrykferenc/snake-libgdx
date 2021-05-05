package snakegame;

import java.util.ListIterator;

public final class GameBoard {
    private final double boardHeight, boardWidth;
    private final Snake mySnake;
    private final Food myFood;
    private final SpecialFood mySpecialFood;
    private final GameSoundPlayer mySoundPlayer;
    private final Saw verticalSaw, horizontalSaw, diagonalUpSaw, diagonalDownSaw;

    public GameBoard() {
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

    public Snake getMySnake() {
        return mySnake;
    }

    public Food getMyFood() {
        return myFood;
    }

    public SpecialFood getMySpecialFood() {
        return mySpecialFood;
    }

    public Saw getMyVerticalSaw() { return verticalSaw; }

    public Saw getMyHorizontalSaw() { return horizontalSaw; }

    public Saw getMyDiagonalUpSaw() { return diagonalUpSaw; }

    public Saw getMyDiagonalDownSaw() { return diagonalDownSaw; }

    public void updateGame(PointVector mousePosition) {
        mySnake.move(mousePosition);
        mySpecialFood.move();
        verticalSaw.verticalMove();
        horizontalSaw.horizontalMove();
        diagonalUpSaw.diagonalMoveUp();
        diagonalDownSaw.diagonalMoveDown();
        checkBorders();
        if (checkTailCollision() || checkSawCollision()) {
            mySoundPlayer.playSnakeCrashedSound();
        }
        if (checkFood()) {
            mySoundPlayer.playFoodEatenSound();
        }
        if (checkSpecialFood()) {
            mySoundPlayer.playFoodEatenSound();
        }
    }

    private boolean checkFood() {
        var distance = new PointVector(mySnake.getHead().getX(), mySnake.getHead().getY());
        distance.subtract(getMyFood().getPosition());
        if (distance.length() < GameSettings.SEGMENT_SIZE) {
            getMyFood().respawn();
            for(int i = 0; i < GameSettings.FOOD_MULTIPLIER; i++) mySnake.addBodySegment();
            return true;
        }
        return false;
    }

    private boolean checkSpecialFood() {
        if (mySpecialFood.isAlive()) {
            var distance = new PointVector(mySnake.getHead().getX(), mySnake.getHead().getY());
            distance.subtract(getMySpecialFood().getPosition());
            if (distance.length() < GameSettings.SEGMENT_SIZE) {
                for(int i = 0; i < GameSettings.SPECIAL_FOOD_MULTIPLIER; i++) mySnake.addBodySegment();
                mySpecialFood.setLongevity(0);
                return true;
            } else
                return false;
        } else {
            getMySpecialFood().respawn();
            return false;
        }

    }

    private boolean checkTailCollision() {
        final int biteLimit = (int)GameSettings.SEGMENT_SIZE; // nie da się zbytnio zderzyć poniżej
        var mySnake = getMySnake();

        if ((mySnake.getActualSize()) > biteLimit) {
            var myBodySegments = mySnake.getBodySegments();
            ListIterator<PointVector> mySnakeIterator = myBodySegments.listIterator(biteLimit);

            var headPosition = mySnake.getHead();

            while (mySnakeIterator.hasNext()) {
                var distance = new PointVector(headPosition);
                distance.subtract(mySnakeIterator.next());
                if (distance.length() < (GameSettings.SEGMENT_SIZE / 2)) {
                    System.exit(1); //to będzie trzeba usunąć oczywiście
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSawCollision() {
        var mySnake = getMySnake();
        for(PointVector bodySegment : mySnake.getBodySegments()) {
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
                System.exit(1); //do usunięcia later
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
}