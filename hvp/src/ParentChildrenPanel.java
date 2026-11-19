import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ParentChildrenPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color BACKGROUND = new Color(243, 246, 251);

    private static final int PAGE_SIZE = 25;

    private static final String ACCESS_CHECK = """
        EXISTS (
            SELECT 1
            FROM users account
            JOIN roles role
                ON role.role_id = account.role_id
            JOIN parent_students link
                ON link.parent_user_id = account.user_id
            WHERE account.username = ?
              AND account.is_active = 1
              AND role.role_name = 'Parent'
              AND link.student_id = ?
        )
        """;

    private final String username;

    private JComboBox<ChildItem> childSelector;
    private JComboBox<YearItem> yearSelector;
    private JButton refreshButton;
    private JButton previousButton;
    private JButton nextButton;
    private JLabel pageLabel;
    private JTextArea profileArea;
    private JLabel statusLabel;

    private boolean updatingSelectors;

    private final Map<String, DefaultTableModel> models =
            new LinkedHashMap<>();

    private int currentPage;
    private int lastPage;

    public ParentChildrenPanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadChildren();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 6));
        titles.setOpaque(false);

        JLabel title = new JLabel("My Children");
        title.setFont(AppSupport.font(Font.BOLD, 26));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Read-only information for your approved linked children"
        );
        subtitle.setFont(AppSupport.font(Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        childSelector = new JComboBox<>();
        childSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        childSelector.setPreferredSize(new Dimension(320, 32));
        childSelector.addActionListener(event -> {
            if (!updatingSelectors) {
                currentPage = 0;
                loadYears();
            }
        });

        yearSelector = new JComboBox<>();
        yearSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        yearSelector.setPreferredSize(new Dimension(210, 32));
        yearSelector.addActionListener(event -> {
            if (!updatingSelectors) {
                currentPage = 0;
                loadSelectedChild();
            }
        });

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(AppSupport.font(Font.BOLD, 13));
        refreshButton.addActionListener(event -> loadChildren());

        controls.add(new JLabel("Child:"));
        controls.add(childSelector);
        controls.add(new JLabel("Academic year:"));
        controls.add(yearSelector);
        controls.add(refreshButton);

        header.add(titles, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppSupport.font(Font.BOLD, 14));

        profileArea = new JTextArea();
        profileArea.setEditable(false);
        profileArea.setLineWrap(true);
        profileArea.setWrapStyleWord(true);
        profileArea.setFont(AppSupport.font(Font.PLAIN, 16));
        profileArea.setForeground(DARK_BLUE);
        profileArea.setBackground(Color.WHITE);
        profileArea.setMargin(new java.awt.Insets(20, 20, 20, 20));

        tabs.addTab("Child Profile", new JScrollPane(profileArea));

        addTableTab(tabs, "Registered Subjects",
                new String[]{
                    "Subject", "Code", "Class", "Academic Year",
                    "First Enrollment", "Enrollment Records"
                },
                new Class<?>[]{
                    String.class, String.class, String.class,
                    String.class, java.sql.Date.class, Integer.class
                });

        addTableTab(tabs, "Grades",
                new String[]{
                    "Date", "Subject", "Class", "Academic Year",
                    "Term", "Assessment", "Score", "Maximum",
                    "Percentage", "Result Status", "Remarks"
                },
                new Class<?>[]{
                    java.sql.Date.class, String.class, String.class,
                    String.class, String.class, String.class,
                    BigDecimal.class, BigDecimal.class,
                    BigDecimal.class, String.class, String.class
                });

        addTableTab(tabs, "Official Results",
                new String[]{
                    "Subject", "Class", "Academic Year",
                    "Policy", "Weighted Percentage",
                    "Official Grade"
                },
                new Class<?>[]{
                    String.class, String.class, String.class,
                    String.class, BigDecimal.class, String.class
                });

        addTableTab(tabs, "Attendance",
                new String[]{
                    "Date", "Class", "Academic Year",
                    "Status", "Remarks"
                },
                new Class<?>[]{
                    java.sql.Date.class, String.class,
                    String.class, String.class, String.class
                });

        addTableTab(tabs, "Class Timetable",
                new String[]{
                    "Day", "Time", "Subject", "Teacher",
                    "Class", "Academic Year", "Classroom"
                },
                new Class<?>[]{
                    String.class, String.class, String.class,
                    String.class, String.class, String.class,
                    String.class
                });

        addTableTab(tabs, "Payments",
                new String[]{
                    "Date", "Amount", "Method", "Receipt",
                    "Term", "Remarks"
                },
                new Class<?>[]{
                    java.sql.Date.class, BigDecimal.class,
                    String.class, String.class, String.class,
                    String.class
                });

        addTableTab(tabs, "Fee Summary",
                new String[]{
                    "Currency", "Term", "Charges",
                    "Adjustments", "Allocated Payments",
                    "Outstanding Balance"
                },
                new Class<?>[]{
                    String.class, String.class, BigDecimal.class,
                    BigDecimal.class, BigDecimal.class, BigDecimal.class
                });

        return tabs;
    }

    private void addTableTab(
            JTabbedPane tabs,
            String name,
            String[] columns,
            Class<?>[] columnTypes
    ) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return columnTypes[column];
            }
        };

        models.put(name, model);

        JTable table = new JTable(model);
        table.setFont(AppSupport.font(Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (!"Class Timetable".equals(name)) {
            table.setAutoCreateRowSorter(true);
        }

        table.getTableHeader().setFont(AppSupport.font(Font.BOLD, 14));
        table.getTableHeader().setBackground(DARK_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        if ("Grades".equals(name)) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            for (int i = 0; i < columns.length; i++) {
                table.getColumnModel().getColumn(i)
                        .setPreferredWidth(
                                "Remarks".equals(columns[i]) ? 220 : 125
                        );
            }
        }

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        String note;

        switch (name) {
            case "Registered Subjects" -> note =
                    "One row per subject, class and academic year. "
                    + "Multiple enrollment records are counted, not repeated.";
            case "Grades" -> note =
                    "Individual recorded assessments. Percentage is shown "
                    + "only when both score and maximum are valid.";
            case "Official Results" -> note =
                    "Weighted results use the active grading policy. "
                    + "Invalid assessments are excluded.";
            case "Class Timetable" -> note =
                    "Current class schedule for the selected academic year.";
            case "Payments" -> note =
                    "Recorded payments only. See Fee Summary for "
                    + "charges, allocations and balances.";
            case "Fee Summary" -> note =
                    "Balances are based on charges, adjustments and "
                    + "payment allocations, grouped by currency and term.";
            default -> note =
                    "Records for the selected academic year, ordered newest first.";
        }

        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(AppSupport.font(Font.PLAIN, 12));
        noteLabel.setBorder(new EmptyBorder(8, 8, 0, 8));

        panel.add(noteLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        tabs.addTab(name, panel);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel account = new JLabel("Parent account: " + username);
        account.setFont(AppSupport.font(Font.BOLD, 14));
        account.setForeground(DARK_BLUE);

        JPanel navigation = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        navigation.setOpaque(false);

        previousButton = new JButton("Previous");
        nextButton = new JButton("Next");
        pageLabel = new JLabel("Page 1");

        for (JButton button : new JButton[]{
                previousButton, nextButton}) {
            button.setFont(AppSupport.font(Font.BOLD, 13));
            button.setFocusPainted(false);
        }

        pageLabel.setFont(AppSupport.font(Font.BOLD, 13));

        previousButton.addActionListener(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadSelectedChild();
            }
        });

        nextButton.addActionListener(event -> {
            if (currentPage < lastPage) {
                currentPage++;
                loadSelectedChild();
            }
        });

        navigation.add(previousButton);
        navigation.add(pageLabel);
        navigation.add(nextButton);

        statusLabel = new JLabel("Loading linked children...");
        statusLabel.setFont(AppSupport.font(Font.PLAIN, 13));

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(navigation, BorderLayout.NORTH);
        bottom.add(statusLabel, BorderLayout.CENTER);

        footer.add(account, BorderLayout.NORTH);
        footer.add(bottom, BorderLayout.CENTER);

        return footer;
    }

    private void setBusy(boolean busy) {
        childSelector.setEnabled(!busy);
        yearSelector.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        previousButton.setEnabled(!busy && currentPage > 0);
        nextButton.setEnabled(!busy && currentPage < lastPage);
    }

    private void clearDetails() {
        profileArea.setText("");

        for (DefaultTableModel model : models.values()) {
            model.setRowCount(0);
        }
    }

    private void updatePageControls(int totalRows) {
        lastPage = totalRows <= PAGE_SIZE
                ? 0
                : (totalRows - 1) / PAGE_SIZE;

        pageLabel.setText("Page " + (currentPage + 1)
                + " of " + (lastPage + 1));

        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < lastPage);
    }

    private void loadChildren() {
        ChildItem previous =
                (ChildItem) childSelector.getSelectedItem();

        Integer previousId = previous == null ? null : previous.id;

        setBusy(true);
        clearDetails();
        statusLabel.setText("Loading linked children...");

        SwingWorker<List<ChildItem>, Void> worker =
                new SwingWorker<List<ChildItem>, Void>() {

            @Override
            protected List<ChildItem> doInBackground()
                    throws Exception {

                List<ChildItem> children = new ArrayList<>();

                String sql = """
                    SELECT
                        s.student_id,
                        s.admission_number,
                        s.first_name,
                        s.last_name,
                        ps.relationship
                    FROM users u
                    JOIN roles r ON r.role_id = u.role_id
                    JOIN parent_students ps
                        ON ps.parent_user_id = u.user_id
                    JOIN students s ON s.student_id = ps.student_id
                    WHERE u.username = ?
                      AND u.is_active = 1
                      AND r.role_name = 'Parent'
                    ORDER BY s.first_name, s.last_name, s.student_id
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setQueryTimeout(30);
                    ps.setString(1, username);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            children.add(new ChildItem(
                                    rs.getInt("student_id"),
                                    rs.getString("admission_number"),
                                    rs.getString("first_name") + " "
                                            + rs.getString("last_name"),
                                    rs.getString("relationship")
                            ));
                        }
                    }
                }

                return children;
            }

            @Override
            protected void done() {
                boolean loadDetails = false;

                try {
                    List<ChildItem> children = get();
                    replaceChildren(children, previousId);

                    if (children.isEmpty()) {
                        profileArea.setText(
                                "No linked children are available.\n\n"
                                + "Ask an authorized school administrator "
                                + "to check your Parent account and child links."
                        );
                        statusLabel.setText("No linked children available.");
                    } else {
                        loadDetails = true;
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");

                } catch (ExecutionException error) {
                    replaceChildren(new ArrayList<>(), null);
                    showFailure(
                            "Your linked children could not be loaded.",
                            error
                    );

                } finally {
                    setBusy(false);
                }

                if (loadDetails) {
                    loadYears();
                }
            }
        };

        worker.execute();
    }

    private void replaceChildren(
            List<ChildItem> children,
            Integer previousId
    ) {
        updatingSelectors = true;

        try {
            childSelector.removeAllItems();
            ChildItem restored = null;

            for (ChildItem child : children) {
                childSelector.addItem(child);

                if (previousId != null
                        && child.id == previousId.intValue()) {
                    restored = child;
                }
            }

            if (restored != null) {
                childSelector.setSelectedItem(restored);
            }

        } finally {
            updatingSelectors = false;
        }
    }

    private void loadYears() {
        ChildItem child =
                (ChildItem) childSelector.getSelectedItem();

        yearSelector.removeAllItems();

        if (child == null) {
            statusLabel.setText("Select a linked child.");
            return;
        }

        setBusy(true);
        statusLabel.setText("Loading academic years...");

        SwingWorker<List<YearItem>, Void> worker =
                new SwingWorker<List<YearItem>, Void>() {

            @Override
            protected List<YearItem> doInBackground()
                    throws Exception {

                List<YearItem> years = new ArrayList<>();

                String sql = """
                    SELECT DISTINCT
                        ay.academic_year_id,
                        ay.academic_year
                    FROM academic_years ay
                    LEFT JOIN classes c
                        ON c.academic_year_id = ay.academic_year_id
                    LEFT JOIN enrollments e
                        ON e.class_id = c.class_id
                       AND e.student_id = ?
                    LEFT JOIN attendance a
                        ON a.class_id = c.class_id
                       AND a.student_id = ?
                    LEFT JOIN payments p
                        ON p.student_id = ?
                       AND p.payment_date BETWEEN ay.start_date
                           AND ay.end_date
                    LEFT JOIN fee_charges fc
                        ON fc.student_id = ?
                       AND fc.academic_year_id = ay.academic_year_id
                    WHERE (
                            e.enrollment_id IS NOT NULL
                         OR a.attendance_id IS NOT NULL
                         OR p.payment_id IS NOT NULL
                         OR fc.charge_id IS NOT NULL
                          )
                      AND EXISTS (
                          SELECT 1
                          FROM users account
                          JOIN roles role
                              ON role.role_id = account.role_id
                          JOIN parent_students link
                              ON link.parent_user_id = account.user_id
                          WHERE account.username = ?
                            AND account.is_active = 1
                            AND role.role_name = 'Parent'
                            AND link.student_id = ?
                      )
                    ORDER BY ay.academic_year DESC
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setQueryTimeout(30);
                    ps.setInt(1, child.id);
                    ps.setInt(2, child.id);
                    ps.setInt(3, child.id);
                    ps.setInt(4, child.id);
                    ps.setString(5, username);
                    ps.setInt(6, child.id);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            years.add(new YearItem(
                                    rs.getInt("academic_year_id"),
                                    rs.getString("academic_year")
                            ));
                        }
                    }
                }

                return years;
            }

            @Override
            protected void done() {
                try {
                    List<YearItem> years = get();

                    updatingSelectors = true;

                    try {
                        for (YearItem year : years) {
                            yearSelector.addItem(year);
                        }

                        if (!years.isEmpty()) {
                            yearSelector.setSelectedIndex(0);
                        }

                    } finally {
                        updatingSelectors = false;
                    }

                    if (years.isEmpty()) {
                        profileArea.setText(
                                "No enrollment, attendance, payment, "
                                + "or fee records are available "
                                + "for this child."
                        );
                        statusLabel.setText(
                                "No academic years available."
                        );
                    } else {
                        loadSelectedChild();
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");

                } catch (ExecutionException error) {
                    showFailure(
                            "Academic years could not be loaded.",
                            error
                    );

                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void loadSelectedChild() {
        ChildItem child =
                (ChildItem) childSelector.getSelectedItem();
        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        clearDetails();

        if (child == null || year == null) {
            statusLabel.setText("Select a child and academic year.");
            return;
        }

        setBusy(true);
        statusLabel.setText("Loading child information...");

        SwingWorker<ChildData, Void> worker =
                new SwingWorker<ChildData, Void>() {

            @Override
            protected ChildData doInBackground()
                    throws Exception {

                ChildData data = new ChildData();
                int offset = currentPage * PAGE_SIZE;

                try (Connection con = Conn.getConnection()) {
                    data.profile = readProfile(con, child.id);

                    if (data.profile == null) {
                        data.accessDenied = true;
                        return data;
                    }

                    data.subjectCount = readSubjects(
                            con, child.id, year.id, offset,
                            data.subjectRows
                    );

                    data.gradeCount = readGrades(
                            con, child.id, year.id, offset,
                            data.gradeRows
                    );

                    data.officialCount = readOfficialResults(
                            con, child.id, year.id, offset,
                            data.officialRows
                    );

                    data.attendanceCount = readAttendance(
                            con, child.id, year.id, offset,
                            data.attendanceRows
                    );

                    data.timetableCount = readTimetable(
                            con, child.id, year.id, offset,
                            data.timetableRows
                    );

                    data.paymentCount = readPayments(
                            con, child.id, year.id, offset,
                            data.paymentRows
                    );

                    data.feeCount = readFeeSummary(
                            con, child.id, year.id, offset,
                            data.feeRows
                    );
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    ChildData data = get();

                    if (data.accessDenied) {
                        profileArea.setText(
                                "This child's information is no longer "
                                + "available to this account.\n\n"
                                + "Click Refresh or contact the administrator."
                        );
                        statusLabel.setText("Child access unavailable.");
                        return;
                    }

                    profileArea.setText(data.profile);
                    profileArea.setCaretPosition(0);

                    fill("Registered Subjects", data.subjectRows);
                    fill("Grades", data.gradeRows);
                    fill("Official Results", data.officialRows);
                    fill("Attendance", data.attendanceRows);
                    fill("Class Timetable", data.timetableRows);
                    fill("Payments", data.paymentRows);
                    fill("Fee Summary", data.feeRows);

                    int largest = Math.max(
                            data.subjectCount,
                            Math.max(data.gradeCount,
                                    Math.max(data.officialCount,
                                            Math.max(data.attendanceCount,
                                                    Math.max(
                                                            data.timetableCount,
                                                            Math.max(
                                                                    data.paymentCount,
                                                                    data.feeCount)))))
                    );

                    updatePageControls(largest);

                    statusLabel.setText(
                            "Subjects: " + data.subjectCount
                                    + " | Assessments: " + data.gradeCount
                                    + " | Official results: "
                                    + data.officialCount
                                    + " | Attendance: "
                                    + data.attendanceCount
                                    + " | Lessons: "
                                    + data.timetableCount
                                    + " | Payments: "
                                    + data.paymentCount
                                    + " | Fee summaries: "
                                    + data.feeCount
                    );

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");

                } catch (ExecutionException error) {
                    clearDetails();
                    showFailure(
                            "The selected child's information "
                                    + "could not be loaded.",
                            error
                    );

                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void fill(String tab, List<Object[]> rows) {
        DefaultTableModel model = models.get(tab);

        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private PreparedStatement detailStatement(
            Connection con,
            String sql,
            int studentId,
            int yearId,
            int pageSize,
            int offset
    ) throws SQLException {

        PreparedStatement ps = con.prepareStatement(sql);

        try {
            ps.setQueryTimeout(30);
            ps.setInt(1, studentId);
            ps.setString(2, username);
            ps.setInt(3, studentId);
            ps.setInt(4, yearId);
            ps.setInt(5, pageSize);
            ps.setInt(6, offset);
            return ps;

        } catch (SQLException error) {
            ps.close();
            throw error;
        }
    }

    private String readProfile(
            Connection con,
            int studentId
    ) throws SQLException {

        String sql = """
            SELECT
                s.admission_number,
                s.first_name,
                s.last_name,
                s.gender,
                s.date_of_birth,
                s.phone,
                s.address,
                s.enrollment_date,
                s.is_active,
                c.class_name,
                ay.academic_year,
                ps.relationship
            FROM students s
            LEFT JOIN classes c ON c.class_id = s.class_id
            LEFT JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            JOIN parent_students ps
                ON ps.student_id = s.student_id
            JOIN users u
                ON u.user_id = ps.parent_user_id
            JOIN roles r
                ON r.role_id = u.role_id
            WHERE s.student_id = ?
              AND u.username = ?
              AND u.is_active = 1
              AND r.role_name = 'Parent'
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setQueryTimeout(30);
            ps.setInt(1, studentId);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                StringBuilder text =
                        new StringBuilder("STUDENT PROFILE\n\n");

                field(text, "Name", rs.getString("first_name")
                        + " " + rs.getString("last_name"));
                field(text, "Admission number",
                        rs.getString("admission_number"));
                field(text, "Your relationship",
                        rs.getString("relationship"));
                field(text, "Current class",
                        rs.getString("class_name"));
                field(text, "Class academic year",
                        rs.getString("academic_year"));
                field(text, "Gender", rs.getString("gender"));
                field(text, "Date of birth",
                        rs.getString("date_of_birth"));
                field(text, "Enrollment date",
                        rs.getString("enrollment_date"));
                field(text, "Student phone", rs.getString("phone"));
                field(text, "Address", rs.getString("address"));
                field(text, "Student status",
                        rs.getBoolean("is_active")
                                ? "Active" : "Inactive");

                return text.toString();
            }
        }
    }

    private int readSubjects(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                sub.subject_name,
                sub.subject_code,
                c.class_name,
                ay.academic_year,
                MIN(e.enrollment_date) AS first_enrollment_date,
                COUNT(*) AS enrollment_records
            FROM enrollments e
            JOIN subjects sub ON sub.subject_id = e.subject_id
            JOIN classes c ON c.class_id = e.class_id
            JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            WHERE e.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND c.academic_year_id = ?
            GROUP BY
                sub.subject_id,
                sub.subject_name,
                sub.subject_code,
                c.class_id,
                c.class_name,
                ay.academic_year
            ORDER BY sub.subject_name, c.class_id
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getString("subject_name"),
                        rs.getString("subject_code"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getDate("first_enrollment_date"),
                        rs.getInt("enrollment_records")
                    }
                )
        );
    }

    private int readGrades(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                g.grade_date,
                sub.subject_name,
                c.class_name,
                ay.academic_year,
                g.term,
                g.assessment_type,
                g.score,
                g.max_score,
                g.remarks
            FROM grades g
            JOIN enrollments e ON e.enrollment_id = g.enrollment_id
            JOIN subjects sub ON sub.subject_id = e.subject_id
            JOIN classes c ON c.class_id = e.class_id
            JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            WHERE e.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND c.academic_year_id = ?
            ORDER BY g.grade_date DESC, g.grade_id DESC
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> {
                    BigDecimal score = rs.getBigDecimal("score");
                    BigDecimal maximum = rs.getBigDecimal("max_score");
                    BigDecimal percentage = null;
                    String status;

                    if (maximum == null || maximum.signum() <= 0) {
                        status = "Invalid maximum score";
                    } else if (score == null || score.signum() < 0) {
                        status = "Invalid score";
                    } else if (score.compareTo(maximum) > 0) {
                        status = "Review: score exceeds maximum";
                    } else {
                        percentage = score
                                .multiply(BigDecimal.valueOf(100))
                                .divide(maximum, 2, RoundingMode.HALF_UP);
                        status = "Valid";
                    }

                    rows.add(new Object[]{
                        rs.getDate("grade_date"),
                        rs.getString("subject_name"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getString("term"),
                        rs.getString("assessment_type"),
                        score,
                        maximum,
                        percentage,
                        status,
                        rs.getString("remarks")
                    });
                }
        );
    }

    private int readOfficialResults(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                sub.subject_name,
                c.class_name,
                ay.academic_year,
                gp.policy_name,
                ROUND(
                    SUM(
                        (g.score / g.max_score) * 100
                        * aw.weight_percent
                    ) / SUM(aw.weight_percent),
                    2
                ) AS official_percentage,
                gb.grade_label AS official_grade
            FROM grades g
            JOIN enrollments e
                ON e.enrollment_id = g.enrollment_id
            JOIN subjects sub
                ON sub.subject_id = e.subject_id
            JOIN classes c
                ON c.class_id = e.class_id
            JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            JOIN grading_policies gp
                ON gp.academic_year_id = ay.academic_year_id
               AND gp.is_active = 1
            JOIN assessment_weights aw
                ON aw.policy_id = gp.policy_id
               AND aw.assessment_type = g.assessment_type
            LEFT JOIN grade_bands gb
                ON gb.policy_id = gp.policy_id
               AND (g.score / g.max_score) * 100
                   BETWEEN gb.minimum_percent
                   AND gb.maximum_percent
            WHERE e.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND c.academic_year_id = ?
              AND g.max_score > 0
              AND g.score >= 0
              AND g.score <= g.max_score
            GROUP BY
                sub.subject_id,
                sub.subject_name,
                c.class_name,
                ay.academic_year,
                gp.policy_name,
                gb.grade_label
            ORDER BY sub.subject_name
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getString("subject_name"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getString("policy_name"),
                        rs.getBigDecimal("official_percentage"),
                        rs.getString("official_grade")
                    }
                )
        );
    }

    private int readAttendance(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                a.attendance_date,
                c.class_name,
                ay.academic_year,
                a.status,
                a.remarks
            FROM attendance a
            JOIN classes c ON c.class_id = a.class_id
            JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            WHERE a.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND c.academic_year_id = ?
            ORDER BY a.attendance_date DESC, a.attendance_id DESC
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getDate("attendance_date"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getString("status"),
                        rs.getString("remarks")
                    }
                )
        );
    }

    private int readTimetable(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                tt.day_of_week,
                TIME_FORMAT(tt.start_time, '%H:%i') AS lesson_start,
                TIME_FORMAT(tt.end_time, '%H:%i') AS lesson_end,
                sub.subject_name,
                COALESCE(
                    NULLIF(
                        TRIM(CONCAT_WS(' ', te.first_name, te.last_name)),
                        ''
                    ),
                    'Not assigned'
                ) AS teacher_name,
                c.class_name,
                ay.academic_year,
                COALESCE(
                    NULLIF(TRIM(tt.classroom), ''),
                    'Not assigned'
                ) AS classroom
            FROM students s
            JOIN timetable tt ON tt.class_id = s.class_id
            JOIN classes c ON c.class_id = tt.class_id
            JOIN academic_years ay
                ON ay.academic_year_id = c.academic_year_id
            JOIN subjects sub ON sub.subject_id = tt.subject_id
            LEFT JOIN teachers te ON te.teacher_id = tt.teacher_id
            WHERE s.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND c.academic_year_id = ?
            ORDER BY
                FIELD(
                    tt.day_of_week,
                    'Monday', 'Tuesday', 'Wednesday',
                    'Thursday', 'Friday'
                ),
                tt.start_time,
                tt.timetable_id
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getString("day_of_week"),
                        rs.getString("lesson_start") + " - "
                                + rs.getString("lesson_end"),
                        rs.getString("subject_name"),
                        rs.getString("teacher_name"),
                        rs.getString("class_name"),
                        rs.getString("academic_year"),
                        rs.getString("classroom")
                    }
                )
        );
    }

    private int readPayments(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                p.payment_date,
                p.amount,
                p.payment_method,
                p.receipt_number,
                p.term,
                p.remarks
            FROM payments p
            JOIN academic_years ay
                ON p.payment_date BETWEEN ay.start_date AND ay.end_date
            WHERE p.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND ay.academic_year_id = ?
            ORDER BY p.payment_date DESC, p.payment_id DESC
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getDate("payment_date"),
                        rs.getBigDecimal("amount"),
                        rs.getString("payment_method"),
                        rs.getString("receipt_number"),
                        rs.getString("term"),
                        rs.getString("remarks")
                    }
                )
        );
    }

    private int readFeeSummary(
            Connection con,
            int studentId,
            int yearId,
            int offset,
            List<Object[]> rows
    ) throws SQLException {

        String sql = """
            SELECT SQL_CALC_FOUND_ROWS
                fc.currency_code,
                fc.term,
                SUM(fc.amount) AS total_charges,
                COALESCE(SUM(fa.amount), 0) AS total_adjustments,
                COALESCE(SUM(pa.amount_allocated), 0) AS total_paid,
                ROUND(
                    SUM(fc.amount)
                    - COALESCE(SUM(fa.amount), 0)
                    - COALESCE(SUM(pa.amount_allocated), 0),
                    2
                ) AS outstanding_balance
            FROM fee_charges fc
            LEFT JOIN fee_adjustments fa
                ON fa.student_id = fc.student_id
               AND fa.academic_year_id = fc.academic_year_id
               AND fa.term = fc.term
               AND fa.currency_code = fc.currency_code
            LEFT JOIN payment_allocations pa
                ON pa.charge_id = fc.charge_id
            WHERE fc.student_id = ?
              AND
            """ + ACCESS_CHECK + """
              AND fc.academic_year_id = ?
            GROUP BY
                fc.currency_code,
                fc.term
            ORDER BY
                fc.currency_code,
                FIELD(fc.term, 'Term 1', 'Term 2', 'Term 3')
            LIMIT ? OFFSET ?
            """;

        return readPagedRows(
                con, sql, studentId, yearId, offset, rs -> rows.add(
                    new Object[]{
                        rs.getString("currency_code"),
                        rs.getString("term"),
                        rs.getBigDecimal("total_charges"),
                        rs.getBigDecimal("total_adjustments"),
                        rs.getBigDecimal("total_paid"),
                        rs.getBigDecimal("outstanding_balance")
                    }
                )
        );
    }

    private int readPagedRows(
            Connection con,
            String sql,
            int studentId,
            int yearId,
            int offset,
            RowReader reader
    ) throws SQLException {

        int total = 0;

        try (PreparedStatement ps = detailStatement(
                con, sql, studentId, yearId, PAGE_SIZE, offset);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reader.read(rs);
            }
        }

        try (PreparedStatement countStatement = con.prepareStatement(
                "SELECT FOUND_ROWS()")) {

            countStatement.setQueryTimeout(30);

            try (ResultSet countResult =
                    countStatement.executeQuery()) {

                if (countResult.next()) {
                    total = countResult.getInt(1);
                }
            }
        }

        return total;
    }

    private void field(
            StringBuilder text,
            String label,
            String value
    ) {
        text.append(label)
                .append(": ")
                .append(value == null || value.trim().isEmpty()
                        ? "Not provided" : value)
                .append("\n\n");
    }

    private void showFailure(String message, Exception error) {
        statusLabel.setText(message);
        AppSupport.reportError(
                ParentChildrenPanel.this,
                message,
                error
        );
    }

    @FunctionalInterface
    private interface RowReader {
        void read(ResultSet rs) throws SQLException;
    }

    private static class ChildItem {
        final int id;
        final String admission;
        final String name;
        final String relationship;

        ChildItem(
                int id,
                String admission,
                String name,
                String relationship
        ) {
            this.id = id;
            this.admission = admission;
            this.name = name;
            this.relationship = relationship;
        }

        @Override
        public String toString() {
            return name + " (" + admission + ")";
        }
    }

    private static class YearItem {
        final int id;
        final String year;

        YearItem(int id, String year) {
            this.id = id;
            this.year = year;
        }

        @Override
        public String toString() {
            return year;
        }
    }

    private static class ChildData {
        String profile;
        boolean accessDenied;

        int subjectCount;
        int gradeCount;
        int officialCount;
        int attendanceCount;
        int timetableCount;
        int paymentCount;
        int feeCount;

        final List<Object[]> subjectRows = new ArrayList<>();
        final List<Object[]> gradeRows = new ArrayList<>();
        final List<Object[]> officialRows = new ArrayList<>();
        final List<Object[]> attendanceRows = new ArrayList<>();
        final List<Object[]> timetableRows = new ArrayList<>();
        final List<Object[]> paymentRows = new ArrayList<>();
        final List<Object[]> feeRows = new ArrayList<>();
    }
}