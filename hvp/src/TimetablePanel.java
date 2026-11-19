import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class TimetablePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE =
            new Color(15, 40, 80);

    private static final Color BACKGROUND =
            new Color(243, 246, 251);

    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);

    private final String username;

    private DefaultTableModel tableModel;
    private JLabel headingLabel;
    private JLabel subtitleLabel;
    private JLabel accountLabel;
    private JLabel statusLabel;
    private JComboBox<String> daySelector;
    private JButton refreshButton;

    public TimetablePanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadTimetable();
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 6));
        titles.setOpaque(false);

        JPanel titleRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        titleRow.setOpaque(false);

        JLabel clock = new JLabel(
                new ClockIcon(28, DARK_BLUE)
        );

        headingLabel = new JLabel("Timetable");
        headingLabel.setFont(
                AppSupport.font(Font.BOLD, 26)
        );
        headingLabel.setForeground(DARK_BLUE);

        titleRow.add(clock);
        titleRow.add(headingLabel);

        subtitleLabel = new JLabel(
                "Weekly lessons ordered by day and time"
        );
        subtitleLabel.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        subtitleLabel.setForeground(
                new Color(100, 115, 135)
        );

        titles.add(titleRow, BorderLayout.NORTH);
        titles.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        JLabel dayLabel = new JLabel("Day:");
        dayLabel.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        daySelector = new JComboBox<>(new String[]{
            "All Days",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday"
        });
        daySelector.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        daySelector.addActionListener(
                event -> loadTimetable()
        );

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(
                event -> loadTimetable()
        );

        controls.add(dayLabel);
        controls.add(daySelector);
        controls.add(refreshButton);

        header.add(titles, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    // ================= TABLE =================

    private JScrollPane createTable() {
        String[] columns = {
            "Day",
            "Time",
            "Subject",
            "Teacher",
            "Class",
            "Classroom"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(AppSupport.font(Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.getTableHeader().setFont(
                AppSupport.font(Font.BOLD, 14)
        );
        table.getTableHeader().setBackground(DARK_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0)
                .setPreferredWidth(100);
        table.getColumnModel().getColumn(1)
                .setPreferredWidth(130);
        table.getColumnModel().getColumn(2)
                .setPreferredWidth(180);
        table.getColumnModel().getColumn(3)
                .setPreferredWidth(190);
        table.getColumnModel().getColumn(4)
                .setPreferredWidth(110);
        table.getColumnModel().getColumn(5)
                .setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 215, 235)
                )
        );

        return scrollPane;
    }

    // ================= FOOTER =================

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBackground(Color.WHITE);
        footer.setBorder(
                new EmptyBorder(15, 20, 15, 20)
        );

        accountLabel = new JLabel("Account: " + username);
        accountLabel.setFont(
                AppSupport.font(Font.BOLD, 14)
        );
        accountLabel.setForeground(DARK_BLUE);

        statusLabel = new JLabel("Loading timetable...");
        statusLabel.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        statusLabel.setForeground(ACCENT_BLUE);

        footer.add(accountLabel, BorderLayout.NORTH);
        footer.add(statusLabel, BorderLayout.CENTER);

        return footer;
    }

    // ================= DATA LOADING =================

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        daySelector.setEnabled(!busy);
    }

    private void loadTimetable() {
        final String selectedDay =
                (String) daySelector.getSelectedItem();

        setBusy(true);
        tableModel.setRowCount(0);
        statusLabel.setText("Loading timetable...");

        SwingWorker<ScheduleResult, Void> worker =
                new SwingWorker<ScheduleResult, Void>() {

            @Override
            protected ScheduleResult doInBackground()
                    throws Exception {

                try (Connection con = Conn.getConnection()) {
                    ScheduleResult result = readAccount(con);

                    if (result.notice != null) {
                        return result;
                    }

                    switch (result.role) {
                        case "Teacher":
                            result.title = "My Teaching Timetable";
                            result.subtitle =
                                    "Lessons assigned to your teacher account";
                            break;

                        case "Student":
                            result.title = "My Class Timetable";
                            result.subtitle =
                                    "Lessons for your currently assigned class";
                            break;

                        case "Admin":
                        case "Secretary":
                        case "Registrar":
                            result.title = "School Timetable";
                            result.subtitle =
                                    "Lessons across school classes and teachers";
                            break;

                        case "Parent":
                            result.notice =
                                    "Open My Children to view a child's timetable.";
                            return result;

                        default:
                            result.notice =
                                    "Timetable access is not configured "
                                    + "for this role.";
                            return result;
                    }

                    if ("Teacher".equals(result.role)
                            && result.teacherId == null) {

                        result.notice =
                                "This account is not linked to a teacher.";
                        return result;
                    }

                    if ("Student".equals(result.role)
                            && result.studentId == null) {

                        result.notice =
                                "This account is not linked to a student.";
                        return result;
                    }

                    if ("Student".equals(result.role)) {
                        result.classId = readStudentClass(
                                con,
                                result.studentId
                        );

                        if (result.classId == null) {
                            result.notice =
                                    "No class is assigned to this student.";
                            return result;
                        }
                    }

                    readLessons(con, selectedDay, result);

                    return result;
                }
            }

            @Override
            protected void done() {
                try {
                    ScheduleResult result = get();

                    headingLabel.setText(result.title);
                    subtitleLabel.setText(result.subtitle);

                    accountLabel.setText(
                            (result.role == null
                                    ? "Account"
                                    : result.role)
                                    + ": "
                                    + username
                    );

                    if (result.notice != null) {
                        statusLabel.setText(result.notice);
                        return;
                    }

                    for (Object[] row : result.rows) {
                        tableModel.addRow(row);
                    }

                    if (result.rows.isEmpty()) {
                        statusLabel.setText(
                                "No lessons found for the selected day(s)."
                        );
                    } else {
                        statusLabel.setText(
                                ("All Days".equals(selectedDay)
                                        ? "Weekly lessons: "
                                        : selectedDay + " lessons: ")
                                        + result.rows.size()
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");

                } catch (ExecutionException error) {
                    statusLabel.setText(
                            "Could not load the timetable."
                    );

                    AppSupport.reportError(
                            TimetablePanel.this,
                            "The timetable could not be loaded.",
                            error
                    );

                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private ScheduleResult readAccount(
            Connection con
    ) throws SQLException {

        ScheduleResult result = new ScheduleResult();

        String sql = """
            SELECT
                u.is_active,
                u.teacher_id,
                u.student_id,
                r.role_name
            FROM users u
            JOIN roles r
                ON r.role_id = u.role_id
            WHERE u.username = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    result.notice = "This account is unavailable.";
                    return result;
                }

                if (!rs.getBoolean("is_active")) {
                    result.notice =
                            "This account is inactive. "
                            + "Please contact the school administrator.";
                    return result;
                }

                String role = rs.getString("role_name");
                result.role = role == null ? "" : role.trim();

                int teacherId = rs.getInt("teacher_id");
                result.teacherId = rs.wasNull()
                        ? null : teacherId;

                int studentId = rs.getInt("student_id");
                result.studentId = rs.wasNull()
                        ? null : studentId;
            }
        }

        return result;
    }

    private Integer readStudentClass(
            Connection con,
            int studentId
    ) throws SQLException {

        String sql = """
            SELECT class_id
            FROM students
            WHERE student_id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int classId = rs.getInt("class_id");
                return rs.wasNull() ? null : classId;
            }
        }
    }

    private void readLessons(
            Connection con,
            String selectedDay,
            ScheduleResult result
    ) throws SQLException {

        /*
         * Recheck the active account and current role in the
         * lesson query, not only in the account lookup above.
         */
        String sql = """
            SELECT
                tt.day_of_week,
                TIME_FORMAT(tt.start_time, '%H:%i') AS lesson_start,
                TIME_FORMAT(tt.end_time, '%H:%i') AS lesson_end,
                sub.subject_name,
                COALESCE(
                    NULLIF(
                        TRIM(CONCAT_WS(
                            ' ',
                            te.first_name,
                            te.last_name
                        )),
                        ''
                    ),
                    'Not assigned'
                ) AS teacher_name,
                c.class_name,
                COALESCE(
                    NULLIF(TRIM(tt.classroom), ''),
                    'Not assigned'
                ) AS classroom
            FROM timetable tt
            JOIN classes c
                ON c.class_id = tt.class_id
            JOIN subjects sub
                ON sub.subject_id = tt.subject_id
            LEFT JOIN teachers te
                ON te.teacher_id = tt.teacher_id
            WHERE EXISTS (
                SELECT 1
                FROM users u
                JOIN roles r
                    ON r.role_id = u.role_id
                WHERE u.username = ?
                  AND u.is_active = 1
                  AND r.role_name = ?
                  AND (
                      (
                          r.role_name = 'Teacher'
                          AND u.teacher_id = tt.teacher_id
                      )
                      OR
                      (
                          r.role_name = 'Student'
                          AND EXISTS (
                              SELECT 1
                              FROM students s
                              WHERE s.student_id = u.student_id
                                AND s.class_id = tt.class_id
                          )
                      )
                      OR r.role_name IN (
                          'Admin',
                          'Secretary',
                          'Registrar'
                      )
                  )
            )
            """;

        boolean filterDay =
                selectedDay != null
                && !"All Days".equals(selectedDay);

        if (filterDay) {
            sql += " AND tt.day_of_week = ? ";
        }

        sql += """
            ORDER BY
                FIELD(
                    tt.day_of_week,
                    'Monday',
                    'Tuesday',
                    'Wednesday',
                    'Thursday',
                    'Friday'
                ),
                tt.start_time,
                c.class_name,
                sub.subject_name,
                tt.timetable_id
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setString(1, username);
            ps.setString(2, result.role);

            if (filterDay) {
                ps.setString(3, selectedDay);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.rows.add(new Object[]{
                        rs.getString("day_of_week"),
                        rs.getString("lesson_start")
                                + " - "
                                + rs.getString("lesson_end"),
                        rs.getString("subject_name"),
                        rs.getString("teacher_name"),
                        rs.getString("class_name"),
                        rs.getString("classroom")
                    });
                }
            }
        }
    }

    // ================= RESULT MODEL =================

    private static class ScheduleResult {

        String role;
        String title = "Timetable";
        String subtitle = "Weekly school schedule";
        String notice;

        Integer teacherId;
        Integer studentId;
        Integer classId;

        final List<Object[]> rows = new ArrayList<>();
    }

    // ================= DRAWN CLOCK ICON =================

    private static class ClockIcon implements Icon {

        private final int size;
        private final Color color;

        ClockIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {
            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor(color);
                g.setStroke(new BasicStroke(
                        2.0f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                int centerX = x + size / 2;
                int centerY = y + size / 2;

                g.drawOval(
                        x + 3,
                        y + 3,
                        size - 7,
                        size - 7
                );

                g.drawLine(
                        centerX,
                        centerY,
                        centerX,
                        y + size / 4
                );

                g.drawLine(
                        centerX,
                        centerY,
                        x + size * 3 / 4,
                        centerY
                );

            } finally {
                g.dispose();
            }
        }
    }
}