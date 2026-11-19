import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MyClassesPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE =
            new Color(15, 40, 80);

    private static final Color BACKGROUND =
            new Color(243, 246, 251);

    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);

    private final String username;

    private DefaultTableModel tableModel;
    private JComboBox<String> yearSelector;
    private JButton refreshButton;
    private JLabel statusLabel;

    private boolean updatingYearSelector;

    public MyClassesPanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadClasses(true);
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 12));
        header.setOpaque(false);

        JPanel titleArea = new JPanel(new BorderLayout(0, 6));
        titleArea.setOpaque(false);

        JLabel heading = new JLabel("My Classes");
        heading.setFont(AppSupport.font(Font.BOLD, 26));
        heading.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Your assigned classes, subjects, and current student totals"
        );
        subtitle.setFont(AppSupport.font(Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        titleArea.add(heading, BorderLayout.NORTH);
        titleArea.add(subtitle, BorderLayout.SOUTH);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        JLabel yearLabel = new JLabel("Academic year:");
        yearLabel.setFont(AppSupport.font(Font.BOLD, 14));

        yearSelector = new JComboBox<>();
        yearSelector.addItem("All Years");
        yearSelector.setFont(AppSupport.font(Font.PLAIN, 14));

        yearSelector.addActionListener(event -> {
            if (!updatingYearSelector) {
                loadClasses(false);
            }
        });

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(AppSupport.font(Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(
                event -> loadClasses(true)
        );

        controls.add(yearLabel);
        controls.add(yearSelector);
        controls.add(refreshButton);

        header.add(titleArea, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    // ================= TABLE =================

    private JScrollPane createTable() {
        String[] columns = {
            "Class",
            "Subject",
            "Academic Year",
            "Current Class Students"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3
                        ? Integer.class
                        : String.class;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(AppSupport.font(Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
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
                .setPreferredWidth(150);
        table.getColumnModel().getColumn(1)
                .setPreferredWidth(220);
        table.getColumnModel().getColumn(2)
                .setPreferredWidth(140);
        table.getColumnModel().getColumn(3)
                .setPreferredWidth(180);

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
        JPanel footer = new JPanel(new BorderLayout(15, 8));
        footer.setBackground(Color.WHITE);
        footer.setBorder(
                new EmptyBorder(15, 20, 15, 20)
        );

        JLabel teacherLabel = new JLabel(
                "Teacher: " + username
        );
        teacherLabel.setFont(
                AppSupport.font(Font.BOLD, 14)
        );
        teacherLabel.setForeground(DARK_BLUE);

        statusLabel = new JLabel("Loading assignments...");
        statusLabel.setFont(
                AppSupport.font(Font.BOLD, 14)
        );
        statusLabel.setForeground(ACCENT_BLUE);

        footer.add(teacherLabel, BorderLayout.NORTH);
        footer.add(statusLabel, BorderLayout.CENTER);

        return footer;
    }

    // ================= DATA LOADING =================

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        yearSelector.setEnabled(!busy);
    }

    private void loadClasses(boolean refreshYears) {
        final String selectedYear =
                (String) yearSelector.getSelectedItem();

        setBusy(true);
        tableModel.setRowCount(0);
        statusLabel.setText("Loading class assignments...");

        SwingWorker<ClassResult, Void> worker =
                new SwingWorker<ClassResult, Void>() {

            @Override
            protected ClassResult doInBackground()
                    throws Exception {

                ClassResult result = new ClassResult();

                try (Connection con = Conn.getConnection()) {
                    if (refreshYears) {
                        result.years = readAcademicYears(con);
                    }

                    String effectiveYear = selectedYear;

                    if (effectiveYear == null
                            || (refreshYears
                            && !"All Years".equals(effectiveYear)
                            && !result.years.contains(effectiveYear))) {

                        effectiveYear = "All Years";
                    }

                    result.selectedYear = effectiveYear;

                    readAssignments(
                            con,
                            effectiveYear,
                            result
                    );
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    ClassResult result = get();

                    if (refreshYears) {
                        updateYearSelector(
                                result.years,
                                result.selectedYear
                        );
                    }

                    for (Object[] row : result.rows) {
                        tableModel.addRow(row);
                    }

                    if (result.rows.isEmpty()) {
                        statusLabel.setText(
                                "No assignments found for this account "
                                + "and academic year."
                        );
                    } else {
                        statusLabel.setText(
                                "Classes: "
                                        + result.classIds.size()
                                        + " | Subjects: "
                                        + result.subjectIds.size()
                                        + " | Assignments: "
                                        + result.rows.size()
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");

                } catch (ExecutionException error) {
                    statusLabel.setText(
                            "Could not load class assignments."
                    );

                    AppSupport.reportError(
                            MyClassesPanel.this,
                            "Your class assignments could not be loaded.",
                            error
                    );

                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private List<String> readAcademicYears(
            Connection con
    ) throws Exception {

        List<String> years = new ArrayList<>();

        String sql = """
            SELECT DISTINCT ta.academic_year
            FROM users u
            JOIN roles r
                ON r.role_id = u.role_id
            JOIN teacher_assignments ta
                ON ta.teacher_id = u.teacher_id
            WHERE u.username = ?
              AND u.is_active = 1
              AND r.role_name = 'Teacher'
            ORDER BY ta.academic_year DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    years.add(rs.getString("academic_year"));
                }
            }
        }

        return years;
    }

    private void readAssignments(
            Connection con,
            String selectedYear,
            ClassResult result
    ) throws Exception {

        String sql = """
            SELECT
                c.class_id,
                c.class_name,
                sub.subject_id,
                sub.subject_name,
                ta.academic_year,
                (
                    SELECT COUNT(*)
                    FROM students s
                    WHERE s.class_id = c.class_id
                ) AS total_students
            FROM users u
            JOIN roles r
                ON r.role_id = u.role_id
            JOIN teacher_assignments ta
                ON ta.teacher_id = u.teacher_id
            JOIN classes c
                ON c.class_id = ta.class_id
            JOIN subjects sub
                ON sub.subject_id = ta.subject_id
            WHERE u.username = ?
              AND u.is_active = 1
              AND r.role_name = 'Teacher'
            """;

        boolean filterYear =
                !"All Years".equals(selectedYear);

        if (filterYear) {
            sql += " AND ta.academic_year = ? ";
        }

        sql += """
            ORDER BY
                ta.academic_year DESC,
                c.class_name,
                sub.subject_name
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setString(1, username);

            if (filterYear) {
                ps.setString(2, selectedYear);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.classIds.add(
                            rs.getInt("class_id")
                    );

                    result.subjectIds.add(
                            rs.getInt("subject_id")
                    );

                    result.rows.add(new Object[]{
                        rs.getString("class_name"),
                        rs.getString("subject_name"),
                        rs.getString("academic_year"),
                        rs.getInt("total_students")
                    });
                }
            }
        }
    }

    private void updateYearSelector(
            List<String> years,
            String selectedYear
    ) {
        updatingYearSelector = true;

        try {
            yearSelector.removeAllItems();
            yearSelector.addItem("All Years");

            for (String year : years) {
                yearSelector.addItem(year);
            }

            yearSelector.setSelectedItem(selectedYear);

        } finally {
            updatingYearSelector = false;
        }
    }

    // ================= RESULT MODEL =================

    private static class ClassResult {

        final List<Object[]> rows = new ArrayList<>();
        final Set<Integer> classIds = new HashSet<>();
        final Set<Integer> subjectIds = new HashSet<>();

        List<String> years = new ArrayList<>();
        String selectedYear = "All Years";
    }
}