import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminParentManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color BACKGROUND = new Color(243, 246, 251);

    private final String adminUsername;

    private JTable parentTable;
    private DefaultTableModel parentModel;

    private JTable linkTable;
    private DefaultTableModel linkModel;

    private JComboBox<ParentItem> parentSelector;
    private JComboBox<StudentItem> studentSelector;
    private JComboBox<String> relationshipSelector;

    private JButton refreshButton;
    private JButton createParentButton;
    private JButton toggleStatusButton;
    private JButton resetPasswordButton;
    private JButton linkButton;
    private JButton unlinkButton;

    private JLabel statusLabel;

    public AdminParentManagementPanel(String adminUsername) {
        this.adminUsername = adminUsername;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadParents();
        loadStudents();
        loadLinks();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JLabel title = new JLabel("Parent Management");
        title.setFont(AppSupport.font(Font.BOLD, 26));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Manage Parent accounts and approved child access"
        );
        subtitle.setFont(AppSupport.font(Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(AppSupport.font(Font.BOLD, 13));
        refreshButton.addActionListener(event -> {
            loadParents();
            loadStudents();
            loadLinks();
        });

        controls.add(refreshButton);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppSupport.font(Font.BOLD, 14));

        tabs.addTab("Parent Accounts", createParentTab());
        tabs.addTab("Parent-Child Links", createLinkTab());
        tabs.addTab("Access Preview", createAccessTab());

        return tabs;
    }

    private JPanel createParentTab() {
        parentModel = new DefaultTableModel(
                new String[]{
                    "User ID", "Username", "Full Name", "Email",
                    "Status", "Linked Children", "Last Login"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        parentTable = createTable(parentModel);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        actions.setOpaque(false);

        createParentButton = new JButton("Create Parent");
        toggleStatusButton = new JButton("Activate / Deactivate");
        resetPasswordButton = new JButton("Reset Password");

        for (JButton button : new JButton[]{
                createParentButton,
                toggleStatusButton,
                resetPasswordButton}) {

            button.setFont(AppSupport.font(Font.BOLD, 13));
            button.setFocusPainted(false);
            actions.add(button);
        }

        createParentButton.addActionListener(
                event -> showCreateParentDialog()
        );

        toggleStatusButton.addActionListener(
                event -> toggleSelectedParentStatus()
        );

        resetPasswordButton.addActionListener(
                event -> showResetPasswordDialog()
        );

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(parentTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLinkTab() {
        linkModel = new DefaultTableModel(
                new String[]{
                    "Parent", "Student", "Admission",
                    "Relationship", "Linked At"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        linkTable = createTable(linkModel);

        JPanel form = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        form.setOpaque(false);

        parentSelector = new JComboBox<>();
        studentSelector = new JComboBox<>();
        relationshipSelector = new JComboBox<>(new String[]{
            "Guardian", "Father", "Mother",
            "Brother", "Sister", "Other"
        });

        for (JComboBox<?> box : new JComboBox<?>[]{
                parentSelector, studentSelector,
                relationshipSelector}) {

            box.setFont(AppSupport.font(Font.PLAIN, 14));
            box.setPreferredSize(new Dimension(240, 32));
        }

        linkButton = new JButton("Link Child");
        unlinkButton = new JButton("Unlink Selected");

        linkButton.setFont(AppSupport.font(Font.BOLD, 13));
        unlinkButton.setFont(AppSupport.font(Font.BOLD, 13));

        linkButton.addActionListener(event -> linkSelectedChild());
        unlinkButton.addActionListener(event -> unlinkSelectedLink());

        form.add(new JLabel("Parent:"));
        form.add(parentSelector);
        form.add(new JLabel("Student:"));
        form.add(studentSelector);
        form.add(new JLabel("Relationship:"));
        form.add(relationshipSelector);
        form.add(linkButton);
        form.add(unlinkButton);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(linkTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAccessTab() {
        DefaultTableModel accessModel = new DefaultTableModel(
                new String[]{
                    "Parent", "Student", "Admission",
                    "Relationship", "Account Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable accessTable = createTable(accessModel);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel note = new JLabel(
                "This preview shows the students each active Parent "
                + "account is authorized to view."
        );
        note.setFont(AppSupport.font(Font.PLAIN, 12));
        note.setBorder(new EmptyBorder(8, 8, 0, 8));

        panel.add(note, BorderLayout.NORTH);
        panel.add(new JScrollPane(accessTable), BorderLayout.CENTER);

        loadAccessPreview(accessModel);

        return panel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(AppSupport.font(Font.PLAIN, 14));
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(AppSupport.font(Font.BOLD, 14));
        table.getTableHeader().setBackground(DARK_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel admin = new JLabel("Administrator: " + adminUsername);
        admin.setFont(AppSupport.font(Font.BOLD, 14));
        admin.setForeground(DARK_BLUE);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(AppSupport.font(Font.PLAIN, 13));

        footer.add(admin, BorderLayout.NORTH);
        footer.add(statusLabel, BorderLayout.CENTER);

        return footer;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void loadParents() {
        parentModel.setRowCount(0);
        parentSelector.removeAllItems();

        SwingWorker<List<ParentItem>, Void> worker =
                new SwingWorker<List<ParentItem>, Void>() {

            @Override
            protected List<ParentItem> doInBackground()
                    throws Exception {

                List<ParentItem> parents = new ArrayList<>();

                String sql = """
                    SELECT
                        u.user_id,
                        u.username,
                        u.full_name,
                        u.email,
                        u.is_active,
                        u.last_login,
                        COUNT(ps.student_id) AS linked_children
                    FROM users u
                    JOIN roles r ON r.role_id = u.role_id
                    LEFT JOIN parent_students ps
                        ON ps.parent_user_id = u.user_id
                    WHERE r.role_name = 'Parent'
                    GROUP BY
                        u.user_id,
                        u.username,
                        u.full_name,
                        u.email,
                        u.is_active,
                        u.last_login
                    ORDER BY u.username
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        parents.add(new ParentItem(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getBoolean("is_active"),
                                rs.getInt("linked_children"),
                                rs.getObject("last_login")
                        ));
                    }
                }

                return parents;
            }

            @Override
            protected void done() {
                try {
                    List<ParentItem> parents = get();

                    for (ParentItem parent : parents) {
                        parentModel.addRow(new Object[]{
                            parent.id,
                            parent.username,
                            parent.fullName,
                            parent.email,
                            parent.active ? "Active" : "Inactive",
                            parent.linkedChildren,
                            parent.lastLogin
                        });

                        parentSelector.addItem(parent);
                    }

                    setStatus(parents.size()
                            + " Parent account(s) loaded.");

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    setStatus("Loading interrupted.");

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "Parent accounts could not be loaded.",
                            error
                    );
                    setStatus("Failed to load Parent accounts.");
                }
            }
        };

        worker.execute();
    }

    private void loadStudents() {
        studentSelector.removeAllItems();

        SwingWorker<List<StudentItem>, Void> worker =
                new SwingWorker<List<StudentItem>, Void>() {

            @Override
            protected List<StudentItem> doInBackground()
                    throws Exception {

                List<StudentItem> students = new ArrayList<>();

                String sql = """
                    SELECT
                        student_id,
                        admission_number,
                        first_name,
                        last_name
                    FROM students
                    WHERE is_active = 1
                    ORDER BY first_name, last_name, student_id
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        students.add(new StudentItem(
                                rs.getInt("student_id"),
                                rs.getString("admission_number"),
                                rs.getString("first_name") + " "
                                        + rs.getString("last_name")
                        ));
                    }
                }

                return students;
            }

            @Override
            protected void done() {
                try {
                    for (StudentItem student : get()) {
                        studentSelector.addItem(student);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "Students could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadLinks() {
        linkModel.setRowCount(0);

        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        p.username AS parent_username,
                        s.first_name,
                        s.last_name,
                        s.admission_number,
                        ps.relationship,
                        ps.linked_at
                    FROM parent_students ps
                    JOIN users p
                        ON p.user_id = ps.parent_user_id
                    JOIN students s
                        ON s.student_id = ps.student_id
                    ORDER BY p.username, s.first_name, s.last_name
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getString("parent_username"),
                            rs.getString("first_name") + " "
                                    + rs.getString("last_name"),
                            rs.getString("admission_number"),
                            rs.getString("relationship"),
                            rs.getTimestamp("linked_at")
                        });
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        linkModel.addRow(row);
                    }

                    setStatus(linkModel.getRowCount()
                            + " parent-child link(s) loaded.");

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "Parent-child links could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadAccessPreview(DefaultTableModel model) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        p.username,
                        s.first_name,
                        s.last_name,
                        s.admission_number,
                        ps.relationship,
                        p.is_active
                    FROM parent_students ps
                    JOIN users p
                        ON p.user_id = ps.parent_user_id
                    JOIN students s
                        ON s.student_id = ps.student_id
                    WHERE p.is_active = 1
                    ORDER BY p.username, s.first_name, s.last_name
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getString("username"),
                            rs.getString("first_name") + " "
                                    + rs.getString("last_name"),
                            rs.getString("admission_number"),
                            rs.getString("relationship"),
                            "Active"
                        });
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        model.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "Access preview could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void showCreateParentDialog() {
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField fullNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] fields = {
            "Username:", usernameField,
            "Email:", emailField,
            "Full name:", fullNameField,
            "Temporary password:", passwordField
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Create Parent Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || email.isEmpty()
                || fullName.isEmpty() || password.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }

        createParent(username, email, fullName, password);
    }

    private void createParent(
            String username,
            String email,
            String fullName,
            String password
    ) {
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                String sql = """
                    INSERT INTO users (
                        username,
                        email,
                        password_hash,
                        password_salt,
                        full_name,
                        role_id,
                        is_active
                    )
                    SELECT
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        r.role_id,
                        1
                    FROM roles r
                    WHERE r.role_name = 'Parent'
                    """;

                String[] hashed = PasswordCodec.hash(password);

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, username);
                    ps.setString(2, email);
                    ps.setString(3, hashed[0]);
                    ps.setString(4, hashed[1]);
                    ps.setString(5, fullName);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Parent account created: "
                                + username);
                        loadParents();
                        loadLinks();

                    } else {
                        showWarning(
                                "The Parent role was not found."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "The Parent account could not be created.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void toggleSelectedParentStatus() {
        int row = parentTable.getSelectedRow();

        if (row < 0) {
            showWarning("Select a Parent account first.");
            return;
        }

        int modelRow = parentTable
                .convertRowIndexToModel(row);

        int userId = (int) parentModel
                .getValueAt(modelRow, 0);

        String username = String.valueOf(
                parentModel.getValueAt(modelRow, 1)
        );

        boolean currentlyActive =
                "Active".equals(parentModel.getValueAt(modelRow, 4));

        boolean newStatus = !currentlyActive;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Set " + username + " to "
                        + (newStatus ? "Active" : "Inactive") + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                String sql = """
                    UPDATE users
                    SET is_active = ?
                    WHERE user_id = ?
                      AND role_id = (
                          SELECT role_id
                          FROM roles
                          WHERE role_name = 'Parent'
                      )
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setBoolean(1, newStatus);
                    ps.setInt(2, userId);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus(username + " is now "
                                + (newStatus ? "Active" : "Inactive"));
                        loadParents();
                        loadLinks();

                    } else {
                        showWarning(
                                "The selected account could not "
                                        + "be updated."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "The account status could not be changed.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void showResetPasswordDialog() {
        int row = parentTable.getSelectedRow();

        if (row < 0) {
            showWarning("Select a Parent account first.");
            return;
        }

        int modelRow = parentTable
                .convertRowIndexToModel(row);

        int userId = (int) parentModel
                .getValueAt(modelRow, 0);

        String username = String.valueOf(
                parentModel.getValueAt(modelRow, 1)
        );

        JPasswordField passwordField = new JPasswordField();

        int result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                    "Set a new password for " + username + ":",
                    passwordField
                },
                "Reset Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String password = new String(passwordField.getPassword());

        if (password.isEmpty()) {
            showWarning("The new password cannot be empty.");
            return;
        }

        resetPassword(userId, username, password);
    }

    private void resetPassword(
            int userId,
            String username,
            String password
    ) {
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                String sql = """
                    UPDATE users
                    SET password_hash = ?,
                        password_salt = ?
                    WHERE user_id = ?
                    """;

                String[] hashed = PasswordCodec.hash(password);

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, hashed[0]);
                    ps.setString(2, hashed[1]);
                    ps.setInt(3, userId);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Password reset for " + username);

                    } else {
                        showWarning(
                                "The password could not be reset."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "The password could not be reset.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void linkSelectedChild() {
        ParentItem parent =
                (ParentItem) parentSelector.getSelectedItem();

        StudentItem student =
                (StudentItem) studentSelector.getSelectedItem();

        String relationship =
                String.valueOf(relationshipSelector.getSelectedItem());

        if (parent == null || student == null) {
            showWarning("Select both a Parent and a Student.");
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                String sql = """
                    INSERT INTO parent_students (
                        parent_user_id,
                        student_id,
                        relationship
                    )
                    VALUES (?, ?, ?)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, parent.id);
                    ps.setInt(2, student.id);
                    ps.setString(3, relationship);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus(parent.username
                                + " linked to "
                                + student.name
                                + " as "
                                + relationship);

                        loadLinks();
                        loadParents();

                    } else {
                        showWarning(
                                "The link could not be created."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "The parent-child link could not "
                                    + "be created.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void unlinkSelectedLink() {
        int row = linkTable.getSelectedRow();

        if (row < 0) {
            showWarning("Select a parent-child link first.");
            return;
        }

        int modelRow = linkTable
                .convertRowIndexToModel(row);

        String parentUsername = String.valueOf(
                linkModel.getValueAt(modelRow, 0)
        );

        String admission = String.valueOf(
                linkModel.getValueAt(modelRow, 2)
        );

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Remove access for " + parentUsername
                        + " to student " + admission + "?",
                "Confirm Unlink",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                String sql = """
                    DELETE ps
                    FROM parent_students ps
                    JOIN users p
                        ON p.user_id = ps.parent_user_id
                    JOIN students s
                        ON s.student_id = ps.student_id
                    WHERE p.username = ?
                      AND s.admission_number = ?
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, parentUsername);
                    ps.setString(2, admission);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Access removed for "
                                + parentUsername + " to "
                                + admission);

                        loadLinks();
                        loadParents();

                    } else {
                        showWarning(
                                "The selected link could not "
                                        + "be removed."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminParentManagementPanel.this,
                            "The parent-child link could not "
                                    + "be removed.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private static class ParentItem {
        final int id;
        final String username;
        final String fullName;
        final String email;
        final boolean active;
        final int linkedChildren;
        final Object lastLogin;

        ParentItem(
                int id,
                String username,
                String fullName,
                String email,
                boolean active,
                int linkedChildren,
                Object lastLogin
        ) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.active = active;
            this.linkedChildren = linkedChildren;
            this.lastLogin = lastLogin;
        }

        @Override
        public String toString() {
            return username + " - " + fullName
                    + (active ? "" : " (Inactive)");
        }
    }

    private static class StudentItem {
        final int id;
        final String admission;
        final String name;

        StudentItem(
                int id,
                String admission,
                String name
        ) {
            this.id = id;
            this.admission = admission;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (" + admission + ")";
        }
    }
}