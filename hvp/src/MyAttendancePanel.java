import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MyAttendancePanel extends JPanel {

    private final String username;

    public MyAttendancePanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(243, 246, 251));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("My Attendance");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(new Color(15, 40, 80));

        add(heading, BorderLayout.NORTH);
        add(createAttendanceTable(), BorderLayout.CENTER);
    }

    private JScrollPane createAttendanceTable() {
        String[] columns = {
            "Date", "Class", "Status", "Remarks"
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
            SELECT a.attendance_date, c.class_name, a.status, a.remarks
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN classes c ON a.class_id = c.class_id
            JOIN users u ON u.student_id = s.student_id
            WHERE u.username = ?
            ORDER BY a.attendance_date DESC
            """;

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getDate("attendance_date"),
                        rs.getString("class_name"),
                        rs.getString("status"),
                        rs.getString("remarks")
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load attendance: " + e.getMessage());
        }

        return new JScrollPane(table);
    }
}