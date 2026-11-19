import java.awt.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginApp extends JFrame {

    private static final Color darkBlue = new Color(15, 40, 80);
    private static final Color lightBlue = new Color(235, 242, 255);
    private static final Color accentBlue = new Color(0, 123, 255);
    private static final Color white = Color.WHITE;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginApp() {
        setTitle("HVP Gatagara High School - Login");
        setSize(900, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createDatabaseTables();

        add(createLeftPanel(), BorderLayout.WEST);
        add(createRightPanel(), BorderLayout.CENTER);
    }

    // ================= DATABASE SETUP =================

    private void createDatabaseTables() {
        String rolesTable = """
            CREATE TABLE IF NOT EXISTS roles (
                role_id INT AUTO_INCREMENT PRIMARY KEY,
                role_name VARCHAR(50) NOT NULL UNIQUE,
                role_description VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String usersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                password_salt VARCHAR(100) NOT NULL,
                full_name VARCHAR(100) NOT NULL,
                role_id INT NOT NULL,
                teacher_id INT NULL,
                officer_id INT NULL,
                student_id INT NULL,
                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                last_login DATETIME NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (role_id) REFERENCES roles(role_id),
                FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
                FOREIGN KEY (officer_id) REFERENCES officers(officer_id),
                FOREIGN KEY (student_id) REFERENCES students(student_id)
            )
        """;

        try (Connection con = Conn.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(rolesTable);
            st.executeUpdate(usersTable);
            insertDefaultRoles(con);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database setup error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertDefaultRoles(Connection con) {
        String check = "SELECT COUNT(*) FROM roles";
        String insert = "INSERT INTO roles (role_name, role_description) VALUES (?, ?)";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(check)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String[][] roles = {
                    {"Admin", "Full access to manage users, academic records, staff, and school settings."},
                    {"Teacher", "Manages assigned subjects, student grades, attendance, and class records."},
                    {"Secretary", "Maintains student records, correspondence, schedules, and general school documents."},
                    {"Bursar", "Manages fees, payments, financial records, and school budgets."},
                    {"Registrar", "Manages admissions, enrollment, academic transcripts, and official student records."},
                    {"Student", "Limited access to personal academic information, timetable, and results."}
                };

                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    for (String[] role : roles) {
                        ps.setString(1, role[0]);
                        ps.setString(2, role[1]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

        } catch (Exception e) {
            System.out.println("Role setup error: " + e.getMessage());
        }
    }

    // ================= LOGIN UI =================

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(darkBlue);
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel icon = new JLabel("🏫");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("HVP GATAGARA");
        title.setForeground(white);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("High School Management System");
        subtitle.setForeground(new Color(200, 220, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setForeground(white);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(20));
        panel.add(title);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(40));
        panel.add(welcome);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(lightBlue);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel heading = new JLabel("Sign in to your account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(darkBlue);

        usernameField = createTextField();
        passwordField = createPasswordField();

        JButton loginButton = createButton("Login", accentBlue);
        JButton signupButton = createButton("Create New Account", darkBlue);

        gbc.gridy = 0;
        panel.add(heading, gbc);

        gbc.gridy = 1;
        panel.add(new JLabel("Username or Email"), gbc);

        gbc.gridy = 2;
        panel.add(usernameField, gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("Password"), gbc);

        gbc.gridy = 4;
        panel.add(passwordField, gbc);

        gbc.gridy = 5;
        panel.add(loginButton, gbc);

        gbc.gridy = 6;
        panel.add(signupButton, gbc);

        loginButton.addActionListener(e -> loginUser());
        signupButton.addActionListener(e -> new SignupDialog(this).setVisible(true));

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private static JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(color);
        button.setForeground(white);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(0, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ================= LOGIN LOGIC =================

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username/email and password.");
            return;
        }

        String sql = """
            SELECT u.user_id, u.username, u.full_name,
                   u.password_hash, u.password_salt, u.is_active,
                   r.role_name
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.username = ? OR u.email = ?
        """;

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean active = rs.getBoolean("is_active");
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");
                String loggedInUsername = rs.getString("username");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role_name");

                if (!active) {
                    showMessage("This account is inactive. Contact the administrator.");
                    return;
                }

                if (PasswordUtil.verifyPassword(password, storedHash, storedSalt)) {
                    updateLastLogin(loggedInUsername);

                    JOptionPane.showMessageDialog(this,
                            "Login successful!\nWelcome, " + fullName,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    dispose();

                    new Dashboard(loggedInUsername, fullName, role).setVisible(true);

                } else {
                    showMessage("Invalid username or password.");
                }

            } else {
                showMessage("Invalid username or password.");
            }

        } catch (Exception e) {
            showMessage("Login error: " + e.getMessage());
        }
    }

    private void updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = NOW() WHERE username = ? OR email = ?";

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, username);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Could not update last login: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "HVP Gatagara", JOptionPane.WARNING_MESSAGE);
    }

    // ================= PASSWORD SECURITY =================

    static class PasswordUtil {

        private static final int SALT_LENGTH = 16;
        private static final int ITERATIONS = 65536;
        private static final int KEY_LENGTH = 256;

        static String generateSalt() {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        }

        static String hashPassword(String password, String salt) {
            try {
                byte[] saltBytes = Base64.getDecoder().decode(salt);

                KeySpec spec = new PBEKeySpec(
                        password.toCharArray(),
                        saltBytes,
                        ITERATIONS,
                        KEY_LENGTH
                );

                SecretKeyFactory factory =
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                byte[] hash = factory.generateSecret(spec).getEncoded();

                return Base64.getEncoder().encodeToString(hash);

            } catch (Exception e) {
                throw new RuntimeException("Password hashing failed: " + e.getMessage());
            }
        }

        static boolean verifyPassword(String enteredPassword, String storedHash, String storedSalt) {
            String newHash = hashPassword(enteredPassword, storedSalt);
            return newHash.equals(storedHash);
        }
    }

    // ================= SIGNUP DIALOG =================

    static class SignupDialog extends JDialog {

        private JTextField fullNameField;
        private JTextField usernameField;
        private JTextField emailField;
        private JPasswordField passwordField;
        private JPasswordField confirmPasswordField;
        private JComboBox<String> roleBox;

        public SignupDialog(JFrame parent) {
            super(parent, "Create New Account", true);
            setSize(520, 620);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            add(createHeader(), BorderLayout.NORTH);
            add(createFormPanel(), BorderLayout.CENTER);
        }

        private JPanel createHeader() {
            JPanel panel = new JPanel();
            panel.setBackground(darkBlue);
            panel.setBorder(new EmptyBorder(25, 30, 25, 30));
            panel.setLayout(new BorderLayout());

            JLabel title = new JLabel("Create HVP Gatagara Account");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));

            JLabel subtitle = new JLabel("Select your user role and create login details");
            subtitle.setForeground(new Color(200, 220, 255));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            panel.add(title, BorderLayout.NORTH);
            panel.add(subtitle, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel();
            panel.setBackground(lightBlue);
            panel.setLayout(new GridBagLayout());
            panel.setBorder(new EmptyBorder(25, 35, 25, 35));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.insets = new Insets(7, 0, 7, 0);

            fullNameField = new JTextField();
            usernameField = new JTextField();
            emailField = new JTextField();
            passwordField = new JPasswordField();
            confirmPasswordField = new JPasswordField();
            roleBox = new JComboBox<>();

            loadRoles();

            JButton signupButton = createButton("Create Account", accentBlue);
            JButton cancelButton = createButton("Cancel", darkBlue);

            addField(panel, gbc, 0, "Full Name", fullNameField);
            addField(panel, gbc, 2, "Username", usernameField);
            addField(panel, gbc, 4, "Email", emailField);
            addField(panel, gbc, 6, "User Role", roleBox);
            addField(panel, gbc, 8, "Password", passwordField);
            addField(panel, gbc, 10, "Confirm Password", confirmPasswordField);

            gbc.gridy = 12;
            panel.add(signupButton, gbc);

            gbc.gridy = 13;
            panel.add(cancelButton, gbc);

            signupButton.addActionListener(e -> registerUser());
            cancelButton.addActionListener(e -> dispose());

            return panel;
        }

        private void addField(JPanel panel, GridBagConstraints gbc,
                              int y, String label, JComponent field) {

            gbc.gridy = y;
            panel.add(new JLabel(label), gbc);

            gbc.gridy = y + 1;
            panel.add(field, gbc);
        }

        private void loadRoles() {
            ArrayList<String> roles = new ArrayList<>();

            String sql = "SELECT role_name FROM roles ORDER BY role_id";

            try (Connection con = Conn.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    roles.add(rs.getString("role_name"));
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Could not load roles: " + e.getMessage());
            }

            if (roles.isEmpty()) {
                roles.add("No roles available");
            }

            roleBox.setModel(new DefaultComboBoxModel<>(roles.toArray(new String[0])));
        }

        private void registerUser() {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String selectedRole = (String) roleBox.getSelectedItem();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                showMessage("Please fill in all fields.");
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                showMessage("Please enter a valid email address.");
                return;
            }

            if (password.length() < 8) {
                showMessage("Password must be at least 8 characters long.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showMessage("Passwords do not match.");
                return;
            }

            if (selectedRole == null || selectedRole.equals("No roles available")) {
                showMessage("Please select a valid user role.");
                return;
            }

            String checkSql = """
                SELECT COUNT(*) FROM users
                WHERE username = ? OR email = ?
            """;

            String roleSql = "SELECT role_id FROM roles WHERE role_name = ?";

            String insertSql = """
                INSERT INTO users
                (username, email, password_hash, password_salt, full_name, role_id)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (Connection con = Conn.getConnection()) {

                try (PreparedStatement check = con.prepareStatement(checkSql)) {
                    check.setString(1, username);
                    check.setString(2, email);

                    ResultSet rs = check.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        showMessage("Username or email already exists.");
                        return;
                    }
                }

                int roleId;

                try (PreparedStatement rolePs = con.prepareStatement(roleSql)) {
                    rolePs.setString(1, selectedRole);
                    ResultSet rs = rolePs.executeQuery();

                    if (rs.next()) {
                        roleId = rs.getInt("role_id");
                    } else {
                        showMessage("Selected role was not found.");
                        return;
                    }
                }

                String salt = PasswordUtil.generateSalt();
                String hashedPassword = PasswordUtil.hashPassword(password, salt);

                try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                    insert.setString(1, username);
                    insert.setString(2, email);
                    insert.setString(3, hashedPassword);
                    insert.setString(4, salt);
                    insert.setString(5, fullName);
                    insert.setInt(6, roleId);

                    insert.executeUpdate();
                }

                JOptionPane.showMessageDialog(this,
                        "Account created successfully!\nYou can now login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();

            } catch (Exception e) {
                showMessage("Signup error: " + e.getMessage());
            }
        }

        private void showMessage(String message) {
            JOptionPane.showMessageDialog(this, message, "Signup", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginApp().setVisible(true));
    }
}
