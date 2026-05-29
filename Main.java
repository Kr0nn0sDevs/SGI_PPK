import ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        new java.io.File("data").mkdirs();
        new java.io.File("reports").mkdirs();
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}
