import javax.swing.SwingUtilities;

/** Entry point for the Smart Pole Lighting System. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppWindow().setVisible(true));
    }
}
