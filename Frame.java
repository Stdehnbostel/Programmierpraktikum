import javax.swing.*;
import java.awt.*;

public class Frame {
public static void main(String[] args) {
    JFrame frame = new JFrame("Example");
    frame.setSize(480, 640);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setBackground(Color.RED);
    frame.add(new JLabel("Fenster"));
    frame.setVisible(true);
    }
}