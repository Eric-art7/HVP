import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MyGradesPanel extends JPanel {

    private final String username;

    public MyGradesPanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(243, 246, 251));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("My Grades");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(new Color(15, 40, 80));

        add(heading, BorderLayout.NORTH);
        add(createGradesTable(), BorderLayout.CENTER);
    }

    private JScrollPane createGradesTable() {
        String[] columns = {
            "Subject", "Term", "Assessment", "Score", "Max Score", "Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        String sql = """
            SELECT sub.subject_name, g.term, g.assessment_type,
                   g.score, g.max_score, g.grade_date
            FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            JOIN students s ON e.student_id = s.student_id
            JOIN users u ON u.student_id = s.student_id
            WHERE u.username = ?
            """;

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("subject_name"),
                        rs.getString("term"),
                        rs.getString("assessment_type"),
                        rs.getBigDecimal("score"),
                        rs.getBigDecimal("max_score"),
                        rs.getDate("grade_date")
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load grades: " + e.getMessage());
        }

        return new JScrollPane(table);
    }
}