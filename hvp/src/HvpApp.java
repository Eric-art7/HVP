import javax.swing.SwingUtilities;

public final class HvpApp {

    private HvpApp() {
    }

    public static void main(String[] args) {
        AppSupport.initialize();

        SwingUtilities.invokeLater(() -> {
            try {
                AppSupport.installLookAndFeel();

                LoginApp login = new LoginApp();
                login.setLocationRelativeTo(null);
                login.setVisible(true);

                AppSupport.info(
                        "Application login window opened."
                );

            } catch (Exception | LinkageError error) {
                AppSupport.reportError(
                        null,
                        "The application could not start.",
                        error
                );
            }
        });
    }
}