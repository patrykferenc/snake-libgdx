import javafx.scene.paint.Color;

public interface GameSettings {
    double HEIGHT = Screen.getPrimary().getBounds().getHeight();
    double WIDTH = Screen.getPrimary().getBounds().getWidth();
    Color snakeColor = Color.DARKSALMON;
    Color foodColor = Color.DARKRED;
    Color background_one = Color.GREEN;
    Color getBackground_two = Color.DARKGREEN;
    double segmentSize = 20;
}