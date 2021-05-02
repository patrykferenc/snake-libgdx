package snakegame;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

public final class Snake {
    private final double safeHeight, safeWidth, segmentSize;
    private final int sizeMultiplier;
    private final PointVector velocity;
    private final List<PointVector> bodySegments;

    public Snake() {
        this.sizeMultiplier = GameSettings.SIZE_MULTIPLIER;
        this.segmentSize = GameSettings.SEGMENT_SIZE;
        this.safeWidth = GameSettings.WIDTH - this.segmentSize;
        this.safeHeight = GameSettings.HEIGHT - this.segmentSize;
        bodySegments = new ArrayList<>();
        bodySegments.add(new PointVector(ThreadLocalRandom.current().nextDouble(this.segmentSize, this.safeWidth),
                ThreadLocalRandom.current().nextDouble(this.segmentSize, this.safeHeight)));
        this.velocity = new PointVector(0, 0);
    }

    public List<PointVector> getBodySegments() {
        return bodySegments;
    }

    public void move(PointVector mouse) {
        ListIterator<PointVector> mySnakeIterator = bodySegments.listIterator(); //creates the iterator

        var oldLocation = mySnakeIterator.next();   //save head location to temp
        var temporarySegment = oldLocation;

        PointVector dir = PointVector.subtract(mouse, temporarySegment);
        dir.normalize();
        dir.multiply(0.5);
        velocity.add(dir);
        velocity.setConstantSpeed(GameSettings.MAX_SPEED);
        temporarySegment.add(velocity);
        mySnakeIterator.set(temporarySegment);  //save new head location

        while (mySnakeIterator.hasNext()) {
            temporarySegment = oldLocation;
            oldLocation = mySnakeIterator.next();
            mySnakeIterator.set(new PointVector(temporarySegment.getX(), temporarySegment.getY()));
        }

    }

    /*
     * returns size of bodySegments list divided by multiplier (user score)
     */
    public int getSizeForUser() {
        return bodySegments.size() / sizeMultiplier;
    }

    /*
     * returns actual size of bodySegments list
     */
    public int getActualSize() {
        return bodySegments.size();
    }

    /*
     * returns snake's head (first body segment)
     */
    public PointVector getHead() {
        return bodySegments.get(0);
    }

    /*
     * returns snake's tail (last body segment)
     */
    public PointVector getTail() {
        return bodySegments.get(getActualSize() - 1); //actually quite robust when using ArrayList
    }

    /*
     * adds n (n=GameSettings.SIZE_MULTIPLIER) segments to the end of the snake
     */
    public void addBodySegment() {
        for (int i = 0; i < GameSettings.SIZE_MULTIPLIER; i++) {
            var newBodySegment = new PointVector(getTail().getX(), getTail().getY());
            bodySegments.add(newBodySegment);
        }
    }
}