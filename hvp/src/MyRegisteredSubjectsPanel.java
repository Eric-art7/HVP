import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MyRegisteredSubjectsPanel extends JPanel {

    private static final Color darkBlue = new Color(15, 40, 80);
    private static final Color lightBackground = new Color(243, 246, 251);
    private static final Color accentBlue = new Color(0, 123, 255);

    private final String username;

    private JTable subjectsTable;
    private DefaultTableModel tableModel;
    private JLabel studentInfoLabel;
    private JLabel totalSubjectsLabel;

    public MyRegisteredSubjectsPanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(20, 20));
        setBackground(lightBackground);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createSummaryPanel(), BorderLayout.SOUTH);

        loadRegisteredSubjects();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel heading = new JLabel("📚 My Registered Subjects");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(darkBlue);

        JLabel subtitle = new JLabel("Subjects you are currently enrolled in");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] columns = {
            "Subject", "Subject Code", "Teacher", "Class", "Enrollment Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        subjectsTable = new JTable(tableModel);
        subjectsTable.setRowHeight(32);
        subjectsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectsTable.getTableHeader().setBackground(darkBlue);
        subjectsTable.getTableHeader().setForeground(Color.WHITE);
        subjectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(subjectsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        studentInfoLabel = new JLabel("Student: " + username);
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        studentInfoLabel.setForeground(darkBlue);

        totalSubjectsLabel = new JLabel("Total Registered Subjects: 0");
        totalSubjectsLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalSubjectsLabel.setForeground(accentBlue);

        panel.add(studentInfoLabel, BorderLayout.WEST);
        panel.add(totalSubjectsLabel, BorderLayout.EAST);

        return panel;
    }

    private void loadRegisteredSubjects() {
        tableModel.setRowCount(0);

        String sql = """
            SELECT
                sub.subject_name,
                sub.subject_code,
                CONCAT(t.first_name, ' ', t.last_name) AS teacher_name,
                c.class_name,
                e.enrollment_date
            FROM users u
            JOIN students s ON u.student_id = s.student_id
            JOIN enrollments e ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            JOIN classes c ON e.class_id = c.class_id
            LEFT JOIN teachers t ON sub.teacher_id = t.teacher_id
            WHERE u.username = ?
            ORDER BY sub.subject_name
        """;

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                int count = 0;

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("subject_name"),
                        rs.getString("subject_code"),
                        rs.getString("teacher_name"),
                        rs.getString("class_name"),
                        rs.getDate("enrollment_date")
                    });

                    count++;
                }

                totalSubjectsLabel.setText("Total Registered Subjects: " + count);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load registered subjects: " + e.getMessage());
        }
    }
}