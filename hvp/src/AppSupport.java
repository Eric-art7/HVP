import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public final class AppSupport {


    private static final Logger LOGGER =
            Logger.getLogger("HVP.Gatagara");


    private static final AtomicBoolean INITIALIZED =
            new AtomicBoolean(false);


    private static final AtomicBoolean ERROR_DIALOG_OPEN =
            new AtomicBoolean(false);


    private AppSupport() {
    }


    // ================= INITIALIZATION =================


    public static void initialize() {

        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }


        configureLogging();


        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> reportError(
                        null,
                        "An unexpected application error occurred.",
                        error
                )
        );
    }


    // ================= LOGGING =================


    private static void configureLogging() {

        LOGGER.setLevel(Level.INFO);
        LOGGER.setUseParentHandlers(false);


        try {

            String userHome =
                    System.getProperty("user.home");


            if (userHome == null
                    || userHome.trim().isEmpty()) {

                throw new IOException(
                        "A user home directory is unavailable."
                );
            }


            Path logDirectory = Paths.get(
                    userHome,
                    ".hvp-gatagara",
                    "logs"
            );


            Files.createDirectories(logDirectory);


            String pattern = logDirectory.resolve(
                    "application-%g.log"
            ).toString();


            FileHandler fileHandler = new FileHandler(
                    pattern,
                    1_000_000,
                    5,
                    true
            );


            fileHandler.setEncoding("UTF-8");
            fileHandler.setFormatter(new SimpleFormatter());


            LOGGER.addHandler(fileHandler);


            LOGGER.info(
                    "Application logging initialized."
            );


        } catch (IOException | SecurityException error) {

            System.err.println(
                    "HVP Gatagara: file logging is unavailable."
            );

            error.printStackTrace();
        }
    }


    public static void info(String event) {
        LOGGER.info(event);
    }


    public static void warning(String event) {
        LOGGER.warning(event);
    }


    // ================= LOOK AND FEEL =================


    public static void installLookAndFeel() {

        if (!SwingUtilities.isEventDispatchThread()) {

            throw new IllegalStateException(
                    "Install the look and feel on the Swing event thread."
            );
        }


        boolean flatLafLoaded = false;


        try {

            Class<?> flatLafClass = Class.forName(
                    "com.formdev.flatlaf.FlatLightLaf"
            );


            Object result = flatLafClass
                    .getMethod("setup")
                    .invoke(null);


            flatLafLoaded = Boolean.TRUE.equals(result);


        } catch (ReflectiveOperationException
                | LinkageError error) {

            LOGGER.warning(
                    "FlatLaf unavailable; using the Swing fallback."
            );


            LOGGER.warning(
                    "FlatLaf details: "
                    + error.getClass().getName()
                    + ": "
                    + error.getMessage()
            );
        }


        if (!flatLafLoaded) {
            installFallbackLookAndFeel();
        }


        applySharedDefaults();
    }


    private static void installFallbackLookAndFeel() {

        try {

            UIManager.setLookAndFeel(
                    UIManager
                            .getCrossPlatformLookAndFeelClassName()
            );


        } catch (Exception error) {

            LOGGER.warning(
                    "Could not install the fallback look and feel."
            );


            LOGGER.warning(
                    "Look and feel details: "
                    + error.getClass().getName()
                    + ": "
                    + error.getMessage()
            );
        }
    }


    private static void applySharedDefaults() {

        Font normalFont = new Font(
                Font.SANS_SERIF,
                Font.PLAIN,
                14
        );


        Font boldFont = normalFont.deriveFont(Font.BOLD);


        UIManager.put("defaultFont", normalFont);


        String[] normalFontKeys = {
            "Label.font",
            "Button.font",
            "ToggleButton.font",
            "TextField.font",
            "FormattedTextField.font",
            "PasswordField.font",
            "TextArea.font",
            "TextPane.font",
            "EditorPane.font",
            "ComboBox.font",
            "List.font",
            "Table.font",
            "Tree.font",
            "CheckBox.font",
            "RadioButton.font",
            "Menu.font",
            "MenuItem.font",
            "CheckBoxMenuItem.font",
            "RadioButtonMenuItem.font",
            "PopupMenu.font",
            "TabbedPane.font",
            "ToolTip.font",
            "OptionPane.messageFont",
            "OptionPane.buttonFont"
        };


        for (String key : normalFontKeys) {
            UIManager.put(key, normalFont);
        }


        UIManager.put("TableHeader.font", boldFont);


        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.rowHeight", 32);
    }


    // ================= SHARED FONTS =================


    public static Font font(int style, int size) {

        Font baseFont = UIManager.getFont("Label.font");


        if (baseFont == null) {

            baseFont = new Font(
                    Font.SANS_SERIF,
                    Font.PLAIN,
                    14
            );
        }


        return baseFont.deriveFont(
                style,
                (float) size
        );
    }


    // ================= ERROR REPORTING =================


    public static void reportError(
            Component owner,
            String userMessage,
            Throwable error
    ) {

        String reference = UUID.randomUUID()
                .toString()
                .substring(0, 8);


        LOGGER.severe(
                "Error reference: " + reference
        );


        logErrorStructure(error);


        String safeMessage =
                userMessage == null
                || userMessage.trim().isEmpty()
                        ? "The requested operation could not be completed."
                        : userMessage;


        if (GraphicsEnvironment.isHeadless()) {

            System.err.println(
                    "HVP Gatagara error reference: "
                    + reference
            );


            return;
        }


        Runnable showDialog = () -> {

            if (!ERROR_DIALOG_OPEN.compareAndSet(false, true)) {
                return;
            }


            try {

                JOptionPane.showMessageDialog(
                        owner,
                        safeMessage
                                + "\n\nReference: "
                                + reference
                                + "\nPlease contact the school administrator "
                                + "if the problem continues.",
                        "HVP Gatagara",
                        JOptionPane.ERROR_MESSAGE
                );


            } finally {
                ERROR_DIALOG_OPEN.set(false);
            }
        };


        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            SwingUtilities.invokeLater(showDialog);
        }
    }


    private static void logErrorStructure(Throwable error) {

        if (error == null) {

            LOGGER.severe(
                    "No exception information supplied."
            );


            return;
        }


        Throwable current = error;
        int causeDepth = 0;


        while (current != null && causeDepth < 10) {

            LOGGER.severe(
                    "Exception type: "
                    + current.getClass().getName()
            );


            String message = current.getMessage();


            if (message != null
                    && !message.trim().isEmpty()) {

                LOGGER.severe(
                        "Exception message: "
                        + message
                );
            }


            StackTraceElement[] trace =
                    current.getStackTrace();


            for (StackTraceElement element : trace) {

                LOGGER.severe(
                        "    at " + element
                );
            }


            current = current.getCause();
            causeDepth++;
        }
    }
}