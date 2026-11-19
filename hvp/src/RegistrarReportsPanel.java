import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;


public class RegistrarReportsPanel extends JPanel {


    private static final long serialVersionUID = 1L;


    private static final Color DARK_BLUE =
            new Color(15, 40, 80);

    private static final Color BACKGROUND =
            new Color(243, 246, 251);

    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);

    private static final Color SUCCESS_GREEN =
            new Color(25, 135, 84);

    private static final Color DANGER_RED =
            new Color(220, 53, 69);

    private static final Color WHITE =
            Color.WHITE;


    private final String registrarUsername;


    private JComboBox<AcademicYearItem> yearSelector;
    private JComboBox<ClassItem> classSelector;
    private JComboBox<String> termSelector;
    private JComboBox<StudentItem> studentSelector;


    private JTable classResultsTable;
    private DefaultTableModel classResultsModel;


    private JTable transcriptTable;
    private DefaultTableModel transcriptModel;


    private JLabel studentNameLabel;
    private JLabel overallAverageLabel;
    private JLabel overallGradeLabel;
    private JLabel subjectsCountLabel;


    private JButton refreshButton;
    private JButton loadClassResultsButton;
    private JButton loadTranscriptButton;
    private JButton exportClassResultsButton;
    private JButton exportTranscriptButton;


    private JLabel statusLabel;


    public RegistrarReportsPanel(
            String registrarUsername
    ) {

        this.registrarUsername = registrarUsername;


        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );


        add(
                createHeader(),
                BorderLayout.NORTH
        );

        add(
                createMainContent(),
                BorderLayout.CENTER
        );

        add(
                createFooter(),
                BorderLayout.SOUTH
        );


        loadAcademicYears();
    }


    // ================= HEADER =================


    private JPanel createHeader() {

        JPanel header = new JPanel(
                new BorderLayout(15, 0)
        );

        header.setOpaque(false);


        JPanel titlePanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        titlePanel.setOpaque(false);


        JLabel icon = new JLabel("📄");

        icon.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        28
                )
        );


        JLabel title = new JLabel(
                "Academic Reports and Transcripts"
        );

        title.setFont(
                AppSupport.font(Font.BOLD, 24)
        );

        title.setForeground(DARK_BLUE);


        titlePanel.add(icon);
        titlePanel.add(title);


        JLabel subtitle = new JLabel(
                "Generate weighted class results and student transcripts."
        );

        subtitle.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        subtitle.setForeground(
                new Color(100, 115, 135)
        );


        JPanel textPanel = new JPanel(
                new BorderLayout(0, 5)
        );

        textPanel.setOpaque(false);

        textPanel.add(
                titlePanel,
                BorderLayout.NORTH
        );

        textPanel.add(
                subtitle,
                BorderLayout.SOUTH
        );


        refreshButton = new JButton("Refresh");

        refreshButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        refreshButton.setBackground(ACCENT_BLUE);
        refreshButton.setForeground(WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(
                new Dimension(120, 38)
        );

        refreshButton.addActionListener(event -> {

            loadAcademicYears();
            statusLabel.setText("Ready.");
        });


        header.add(
                textPanel,
                BorderLayout.CENTER
        );

        header.add(
                refreshButton,
                BorderLayout.EAST
        );


        return header;
    }


    // ================= MAIN CONTENT =================


    private JPanel createMainContent() {

        JPanel mainContent = new JPanel(
                new BorderLayout(15, 15)
        );

        mainContent.setOpaque(false);


        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(
                AppSupport.font(Font.BOLD, 14)
        );


        tabs.addTab(
                "Class Results",
                createClassResultsTab()
        );

        tabs.addTab(
                "Student Transcript",
                createTranscriptTab()
        );


        mainContent.add(
                tabs,
                BorderLayout.CENTER
        );


        return mainContent;
    }


    // ================= CLASS RESULTS =================


    private JPanel createClassResultsTab() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setOpaque(false);


        JPanel controls = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        controls.setBackground(WHITE);
        controls.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        yearSelector = new JComboBox<>();
        classSelector = new JComboBox<>();
        termSelector = new JComboBox<>(
                new String[]{
                    "Term 1",
                    "Term 2",
                    "Term 3"
                }
        );


        yearSelector.setPreferredSize(
                new Dimension(150, 38)
        );

        classSelector.setPreferredSize(
                new Dimension(200, 38)
        );

        termSelector.setPreferredSize(
                new Dimension(130, 38)
        );


        yearSelector.addActionListener(
                event -> loadClasses()
        );


        loadClassResultsButton = new JButton(
                "Load Results"
        );

        loadClassResultsButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        loadClassResultsButton.setBackground(ACCENT_BLUE);
        loadClassResultsButton.setForeground(WHITE);
        loadClassResultsButton.setFocusPainted(false);
        loadClassResultsButton.setPreferredSize(
                new Dimension(140, 38)
        );

        loadClassResultsButton.addActionListener(
                event -> loadClassResults()
        );


        exportClassResultsButton = new JButton(
                "Export CSV"
        );

        exportClassResultsButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        exportClassResultsButton.setBackground(DARK_BLUE);
        exportClassResultsButton.setForeground(WHITE);
        exportClassResultsButton.setFocusPainted(false);
        exportClassResultsButton.setPreferredSize(
                new Dimension(130, 38)
        );

        exportClassResultsButton.addActionListener(
                event -> exportClassResults()
        );


        controls.add(
                labeledField(
                        "Academic Year",
                        yearSelector
                )
        );

        controls.add(
                labeledField(
                        "Class",
                        classSelector
                )
        );

        controls.add(
                labeledField(
                        "Term",
                        termSelector
                )
        );

        controls.add(loadClassResultsButton);
        controls.add(exportClassResultsButton);


        classResultsModel = new DefaultTableModel(
                new String[]{
                    "Student",
                    "Admission No.",
                    "Subject",
                    "Subject Code",
                    "Assessments",
                    "Average (%)",
                    "Grade"
                },
                0
        ) {


            private static final long serialVersionUID = 1L;


            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };


        classResultsTable = new JTable(
                classResultsModel
        );

        classResultsTable.setRowHeight(32);
        classResultsTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        classResultsTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane tableScroll =
                new JScrollPane(classResultsTable);


        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                tableScroll,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= TRANSCRIPT =================


    private JPanel createTranscriptTab() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setOpaque(false);


        JPanel controls = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        controls.setBackground(WHITE);
        controls.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        studentSelector = new JComboBox<>();

        studentSelector.setPreferredSize(
                new Dimension(280, 38)
        );


        loadTranscriptButton = new JButton(
                "Load Transcript"
        );

        loadTranscriptButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        loadTranscriptButton.setBackground(ACCENT_BLUE);
        loadTranscriptButton.setForeground(WHITE);
        loadTranscriptButton.setFocusPainted(false);
        loadTranscriptButton.setPreferredSize(
                new Dimension(160, 38)
        );

        loadTranscriptButton.addActionListener(
                event -> loadTranscript()
        );


        exportTranscriptButton = new JButton(
                "Export Transcript"
        );

        exportTranscriptButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        exportTranscriptButton.setBackground(DARK_BLUE);
        exportTranscriptButton.setForeground(WHITE);
        exportTranscriptButton.setFocusPainted(false);
        exportTranscriptButton.setPreferredSize(
                new Dimension(170, 38)
        );

        exportTranscriptButton.addActionListener(
                event -> exportTranscript()
        );


        controls.add(
                labeledField(
                        "Student",
                        studentSelector
                )
        );

        controls.add(loadTranscriptButton);
        controls.add(exportTranscriptButton);


        JPanel summary = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        25,
                        10
                )
        );

        summary.setBackground(WHITE);
        summary.setBorder(
                new EmptyBorder(
                        12,
                        15,
                        12,
                        15
                )
        );


        studentNameLabel = new JLabel(
                "Student: Not selected"
        );

        studentNameLabel.setFont(
                AppSupport.font(Font.BOLD, 18)
        );

        studentNameLabel.setForeground(DARK_BLUE);


        overallAverageLabel = new JLabel(
                "Overall Average: --"
        );

        overallAverageLabel.setFont(
                AppSupport.font(Font.BOLD, 18)
        );

        overallAverageLabel.setForeground(ACCENT_BLUE);


        overallGradeLabel = new JLabel(
                "Overall Grade: --"
        );

        overallGradeLabel.setFont(
                AppSupport.font(Font.BOLD, 18)
        );

        overallGradeLabel.setForeground(SUCCESS_GREEN);


        subjectsCountLabel = new JLabel(
                "Subjects: 0"
        );

        subjectsCountLabel.setFont(
                AppSupport.font(Font.PLAIN, 15)
        );

        subjectsCountLabel.setForeground(
                new Color(100, 115, 135)
        );


        summary.add(studentNameLabel);
        summary.add(overallAverageLabel);
        summary.add(overallGradeLabel);
        summary.add(subjectsCountLabel);


        transcriptModel = new DefaultTableModel(
                new String[]{
                    "Academic Year",
                    "Term",
                    "Subject",
                    "Subject Code",
                    "Assessments",
                    "Average (%)",
                    "Grade"
                },
                0
        ) {


            private static final long serialVersionUID = 1L;


            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };


        transcriptTable = new JTable(
                transcriptModel
        );

        transcriptTable.setRowHeight(32);
        transcriptTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        transcriptTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane tableScroll =
                new JScrollPane(transcriptTable);


        JPanel topPanel = new JPanel(
                new BorderLayout(0, 15)
        );

        topPanel.setOpaque(false);

        topPanel.add(
                controls,
                BorderLayout.NORTH
        );

        topPanel.add(
                summary,
                BorderLayout.CENTER
        );


        panel.add(
                topPanel,
                BorderLayout.NORTH
        );

        panel.add(
                tableScroll,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= FOOTER =================


    private JPanel createFooter() {

        JPanel footer = new JPanel(
                new BorderLayout()
        );

        footer.setBackground(WHITE);
        footer.setBorder(
                new EmptyBorder(
                        12,
                        15,
                        12,
                        15
                )
        );


        JLabel registrar = new JLabel(
                "Registrar: " + registrarUsername
        );

        registrar.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        registrar.setForeground(DARK_BLUE);


        statusLabel = new JLabel("Ready.");

        statusLabel.setFont(
                AppSupport.font(Font.PLAIN, 13)
        );


        footer.add(
                registrar,
                BorderLayout.NORTH
        );

        footer.add(
                statusLabel,
                BorderLayout.CENTER
        );


        return footer;
    }


    private void setStatus(String message) {
        statusLabel.setText(message);
    }


    // ================= FORM HELPERS =================


    private JPanel labeledField(
            String labelText,
            JComponent field
    ) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 4)
        );

        panel.setOpaque(false);


        JLabel label = new JLabel(labelText);

        label.setFont(
                AppSupport.font(Font.BOLD, 12)
        );

        label.setForeground(DARK_BLUE);


        panel.add(
                label,
                BorderLayout.NORTH
        );

        panel.add(
                field,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= SELECTOR DATA =================


    private void loadAcademicYears() {

        yearSelector.removeAllItems();
        classSelector.removeAllItems();
        studentSelector.removeAllItems();


        SwingWorker<List<AcademicYearItem>, Void> worker =
                new SwingWorker<List<AcademicYearItem>, Void>() {


            @Override
            protected List<AcademicYearItem> doInBackground()
                    throws Exception {


                List<AcademicYearItem> years =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        academic_year_id,
                        academic_year
                    FROM academic_years
                    ORDER BY academic_year DESC
                    """;


                try (
                        Connection con =
                                Conn.getConnection();

                        PreparedStatement ps =
                                con.prepareStatement(sql);

                        ResultSet rs =
                                ps.executeQuery()
                ) {


                    while (rs.next()) {

                        years.add(new AcademicYearItem(
                                rs.getInt(
                                        "academic_year_id"
                                ),
                                rs.getString(
                                        "academic_year"
                                )
                        ));
                    }
                }


                return years;
            }


            @Override
            protected void done() {

                try {

                    for (AcademicYearItem year
                            : get()) {

                        yearSelector.addItem(year);
                    }


                    loadClasses();
                    loadAllStudents();


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Academic years could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private void loadClasses() {

        classSelector.removeAllItems();


        AcademicYearItem year =
                (AcademicYearItem) yearSelector
                        .getSelectedItem();


        if (year == null) {
            return;
        }


        SwingWorker<List<ClassItem>, Void> worker =
                new SwingWorker<List<ClassItem>, Void>() {


            @Override
            protected List<ClassItem> doInBackground()
                    throws Exception {


                List<ClassItem> classes =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        class_id,
                        class_name
                    FROM classes
                    WHERE academic_year_id = ?
                    ORDER BY class_name
                    """;


                try (
                        Connection con =
                                Conn.getConnection();

                        PreparedStatement ps =
                                con.prepareStatement(sql)
                ) {

                    ps.setInt(1, year.id);


                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {


                        while (rs.next()) {

                            classes.add(new ClassItem(
                                    rs.getInt("class_id"),
                                    rs.getString("class_name")
                            ));
                        }
                    }
                }


                return classes;
            }


            @Override
            protected void done() {

                try {

                    for (ClassItem classItem
                            : get()) {

                        classSelector.addItem(classItem);
                    }


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Classes could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private void loadAllStudents() {

        studentSelector.removeAllItems();


        SwingWorker<List<StudentItem>, Void> worker =
                new SwingWorker<List<StudentItem>, Void>() {


            @Override
            protected List<StudentItem> doInBackground()
                    throws Exception {


                List<StudentItem> students =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        student_id,
                        admission_number,
                        first_name,
                        last_name
                    FROM students
                    WHERE is_active = 1
                    ORDER BY first_name, last_name
                    """;


                try (
                        Connection con =
                                Conn.getConnection();

                        PreparedStatement ps =
                                con.prepareStatement(sql);

                        ResultSet rs =
                                ps.executeQuery()
                ) {


                    while (rs.next()) {

                        students.add(new StudentItem(
                                rs.getInt("student_id"),
                                rs.getString(
                                        "admission_number"
                                ),
                                rs.getString("first_name")
                                        + " "
                                        + rs.getString(
                                                "last_name"
                                        )
                        ));
                    }
                }


                return students;
            }


            @Override
            protected void done() {

                try {

                    for (StudentItem student
                            : get()) {

                        studentSelector.addItem(student);
                    }


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Students could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    // ================= GRADING DATA =================


    private GradingPolicy loadActivePolicy(
            Connection con,
            int academicYearId
    ) throws Exception {

        String policySql = """
            SELECT
                policy_id,
                policy_name,
                missing_assessment_rule
            FROM grading_policies
            WHERE academic_year_id = ?
              AND is_active = 1
            ORDER BY policy_id DESC
            LIMIT 1
            """;


        int policyId = -1;
        String missingRule = "EXCLUDE";


        try (
                PreparedStatement ps =
                        con.prepareStatement(policySql)
        ) {

            ps.setInt(1, academicYearId);


            try (
                    ResultSet rs = ps.executeQuery()
            ) {

                if (rs.next()) {

                    policyId =
                            rs.getInt("policy_id");

                    missingRule = rs.getString(
                            "missing_assessment_rule"
                    );
                }
            }
        }


        GradingPolicy policy = new GradingPolicy();

        policy.policyId = policyId;
        policy.missingRule = missingRule;


        if (policyId == -1) {
            return policy;
        }


        String weightsSql = """
            SELECT
                assessment_type,
                weight_percent
            FROM assessment_weights
            WHERE policy_id = ?
            """;


        try (
                PreparedStatement ps =
                        con.prepareStatement(weightsSql)
        ) {

            ps.setInt(1, policyId);


            try (
                    ResultSet rs = ps.executeQuery()
            ) {


                while (rs.next()) {

                    policy.weights.put(
                            rs.getString(
                                    "assessment_type"
                            ),
                            rs.getBigDecimal(
                                    "weight_percent"
                            )
                    );
                }
            }
        }


        String bandsSql = """
            SELECT
                grade_label,
                minimum_percent,
                maximum_percent
            FROM grade_bands
            WHERE policy_id = ?
            ORDER BY minimum_percent DESC
            """;


        try (
                PreparedStatement ps =
                        con.prepareStatement(bandsSql)
        ) {

            ps.setInt(1, policyId);


            try (
                    ResultSet rs = ps.executeQuery()
            ) {


                while (rs.next()) {

                    policy.bands.add(new GradeBand(
                            rs.getString("grade_label"),
                            rs.getBigDecimal(
                                    "minimum_percent"
                            ),
                            rs.getBigDecimal(
                                    "maximum_percent"
                            )
                    ));
                }
            }
        }


        return policy;
    }


    private String getGradeLabel(
            GradingPolicy policy,
            BigDecimal percentage
    ) {

        if (policy == null
                || percentage == null) {
            return "--";
        }


        for (GradeBand band : policy.bands) {

            if (percentage.compareTo(
                    band.minimum) >= 0
                    && percentage.compareTo(
                            band.maximum) <= 0) {

                return band.label;
            }
        }


        return "--";
    }


    // ================= CLASS RESULTS =================


    private void loadClassResults() {

        AcademicYearItem year =
                (AcademicYearItem) yearSelector
                        .getSelectedItem();

        ClassItem classItem =
                (ClassItem) classSelector
                        .getSelectedItem();

        String term = String.valueOf(
                termSelector.getSelectedItem()
        );


        if (year == null || classItem == null) {

            showWarning(
                    "Select an academic year and class."
            );

            return;
        }


        classResultsModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                try (
                        Connection con =
                                Conn.getConnection()
                ) {

                    GradingPolicy policy =
                            loadActivePolicy(
                                    con,
                                    year.id
                            );


                    String sql = """
                        SELECT
                            CONCAT(
                                s.first_name,
                                ' ',
                                s.last_name
                            ) AS student_name,
                            s.admission_number,
                            sub.subject_name,
                            sub.subject_code,
                            g.assessment_type,
                            g.score,
                            g.max_score
                        FROM grades g
                        JOIN enrollments e
                            ON e.enrollment_id =
                                g.enrollment_id
                        JOIN students s
                            ON s.student_id =
                                e.student_id
                        JOIN subjects sub
                            ON sub.subject_id =
                                e.subject_id
                        WHERE e.class_id = ?
                          AND g.term = ?
                        ORDER BY
                            s.first_name,
                            s.last_name,
                            sub.subject_name
                        """;


                    Map<String, StudentSubjectRecord>
                            records = new HashMap<>();


                    try (
                            PreparedStatement ps =
                                    con.prepareStatement(sql)
                    ) {

                        ps.setInt(1, classItem.id);
                        ps.setString(2, term);


                        try (
                                ResultSet rs =
                                        ps.executeQuery()
                        ) {


                            while (rs.next()) {

                                String studentName =
                                        rs.getString(
                                                "student_name"
                                        );

                                String admission =
                                        rs.getString(
                                                "admission_number"
                                        );

                                String subjectName =
                                        rs.getString(
                                                "subject_name"
                                        );

                                String subjectCode =
                                        rs.getString(
                                                "subject_code"
                                        );

                                String assessmentType =
                                        rs.getString(
                                                "assessment_type"
                                        );

                                BigDecimal score =
                                        rs.getBigDecimal("score");

                                BigDecimal maxScore =
                                        rs.getBigDecimal("max_score");


                                String key =
                                        admission + "|"
                                        + subjectCode;


                                StudentSubjectRecord record =
                                        records.computeIfAbsent(
                                                key,
                                                k -> new StudentSubjectRecord(
                                                        studentName,
                                                        admission,
                                                        subjectName,
                                                        subjectCode
                                                )
                                        );


                                record.addScore(
                                        assessmentType,
                                        score,
                                        maxScore
                                );
                            }
                        }
                    }


                    for (StudentSubjectRecord record
                            : records.values()) {

                        BigDecimal average =
                                record.calculateAverage(policy);


                        rows.add(new Object[]{
                            record.studentName,
                            record.admission,
                            record.subjectName,
                            record.subjectCode,
                            record.assessmentCount,
                            average == null
                                    ? "Incomplete"
                                    : average.toString(),
                            getGradeLabel(policy, average)
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        classResultsModel.addRow(row);
                    }


                    setStatus(
                            classResultsModel.getRowCount()
                            + " subject result(s) loaded."
                    );


                    if (classResultsModel
                            .getRowCount() == 0) {

                        classResultsModel.addRow(
                                new Object[]{
                                    "No results found",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    ""
                                }
                        );
                    }


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Class results could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    // ================= TRANSCRIPT =================


    private void loadTranscript() {

        StudentItem student =
                (StudentItem) studentSelector
                        .getSelectedItem();


        AcademicYearItem year =
                (AcademicYearItem) yearSelector
                        .getSelectedItem();


        if (student == null || year == null) {

            showWarning(
                    "Select a student and academic year."
            );

            return;
        }


        transcriptModel.setRowCount(0);


        studentNameLabel.setText(
                "Student: " + student.name
        );

        overallAverageLabel.setText(
                "Overall Average: --"
        );

        overallGradeLabel.setText(
                "Overall Grade: --"
        );

        subjectsCountLabel.setText(
                "Subjects: 0"
        );


        SwingWorker<TranscriptResult, Void> worker =
                new SwingWorker<TranscriptResult, Void>() {


            @Override
            protected TranscriptResult doInBackground()
                    throws Exception {


                TranscriptResult result =
                        new TranscriptResult();


                try (
                        Connection con =
                                Conn.getConnection()
                ) {

                    GradingPolicy policy =
                            loadActivePolicy(
                                    con,
                                    year.id
                            );


                    String sql = """
                        SELECT
                            a.academic_year,
                            g.term,
                            sub.subject_name,
                            sub.subject_code,
                            g.assessment_type,
                            g.score,
                            g.max_score
                        FROM grades g
                        JOIN enrollments e
                            ON e.enrollment_id =
                                g.enrollment_id
                        JOIN subjects sub
                            ON sub.subject_id =
                                e.subject_id
                        JOIN academic_years a
                            ON a.academic_year_id =
                                e.class_id
                        WHERE e.student_id = ?
                        ORDER BY
                            a.academic_year,
                            g.term,
                            sub.subject_name
                        """;


                    Map<String, StudentSubjectRecord>
                            records = new HashMap<>();


                    try (
                            PreparedStatement ps =
                                    con.prepareStatement(sql)
                    ) {

                        ps.setInt(1, student.id);


                        try (
                                ResultSet rs =
                                        ps.executeQuery()
                        ) {


                            while (rs.next()) {

                                String academicYear =
                                        rs.getString(
                                                "academic_year"
                                        );

                                String term =
                                        rs.getString("term");

                                String subjectName =
                                        rs.getString(
                                                "subject_name"
                                        );

                                String subjectCode =
                                        rs.getString(
                                                "subject_code"
                                        );

                                String assessmentType =
                                        rs.getString(
                                                "assessment_type"
                                        );

                                BigDecimal score =
                                        rs.getBigDecimal("score");

                                BigDecimal maxScore =
                                        rs.getBigDecimal("max_score");


                                String key =
                                        academicYear + "|"
                                        + term + "|"
                                        + subjectCode;


                                StudentSubjectRecord record =
                                        records.computeIfAbsent(
                                                key,
                                                k -> new StudentSubjectRecord(
                                                        academicYear,
                                                        term,
                                                        subjectName,
                                                        subjectCode
                                                )
                                        );


                                record.addScore(
                                        assessmentType,
                                        score,
                                        maxScore
                                );
                            }
                        }
                    }


                    BigDecimal totalPercentage =
                            BigDecimal.ZERO;

                    int countedSubjects = 0;


                    for (StudentSubjectRecord record
                            : records.values()) {

                        BigDecimal average =
                                record.calculateAverage(policy);


                        String grade = getGradeLabel(
                                policy,
                                average
                        );


                        result.rows.add(new Object[]{
                            record.academicYear,
                            record.term,
                            record.subjectName,
                            record.subjectCode,
                            record.assessmentCount,
                            average == null
                                    ? "Incomplete"
                                    : average.toString(),
                            grade
                        });


                        if (average != null) {

                            totalPercentage =
                                    totalPercentage.add(average);

                            countedSubjects++;
                        }
                    }


                    if (countedSubjects > 0) {

                        result.overallAverage =
                                totalPercentage.divide(
                                        BigDecimal.valueOf(
                                                countedSubjects
                                        ),
                                        2,
                                        RoundingMode.HALF_UP
                                );

                        result.overallGrade =
                                getGradeLabel(
                                        policy,
                                        result.overallAverage
                                );
                    }


                    result.subjectCount =
                            countedSubjects;
                }


                return result;
            }


            @Override
            protected void done() {

                try {

                    TranscriptResult result = get();


                    for (Object[] row : result.rows) {
                        transcriptModel.addRow(row);
                    }


                    subjectsCountLabel.setText(
                            "Subjects: "
                            + result.subjectCount
                    );


                    if (result.overallAverage != null) {

                        overallAverageLabel.setText(
                                "Overall Average: "
                                + result.overallAverage
                                + "%"
                        );

                        overallGradeLabel.setText(
                                "Overall Grade: "
                                + result.overallGrade
                        );
                    }


                    setStatus(
                            transcriptModel.getRowCount()
                            + " transcript result(s) loaded."
                    );


                    if (transcriptModel
                            .getRowCount() == 0) {

                        transcriptModel.addRow(
                                new Object[]{
                                    "",
                                    "",
                                    "No grades found",
                                    "",
                                    "",
                                    "",
                                    ""
                                }
                        );
                    }


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Transcript could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    // ================= CSV EXPORT =================


    private void exportClassResults() {

        exportTable(
                classResultsModel,
                "class-results.csv",
                "HVP Gatagara Class Results"
        );
    }


    private void exportTranscript() {

        exportTable(
                transcriptModel,
                "student-transcript.csv",
                "HVP Gatagara Student Transcript"
        );
    }


    private void exportTable(
            DefaultTableModel model,
            String defaultFileName,
            String reportTitle
    ) {

        if (model.getRowCount() == 0) {

            showWarning(
                    "There is no report to export."
            );

            return;
        }


        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
                new File(defaultFileName)
        );


        int result =
                chooser.showSaveDialog(this);


        if (result
                != JFileChooser.APPROVE_OPTION) {
            return;
        }


        File file = chooser.getSelectedFile();


        if (!file.getName().toLowerCase()
                .endsWith(".csv")) {

            file = new File(
                    file.getAbsolutePath() + ".csv"
            );
        }


        try (
                PrintWriter writer =
                        new PrintWriter(
                                file,
                                "UTF-8"
                        )
        ) {

            writer.println(reportTitle);
            writer.println();


            for (
                    int column = 0;
                    column < model.getColumnCount();
                    column++
            ) {

                if (column > 0) {
                    writer.print(",");
                }


                writer.print(
                        escapeCsv(
                                model.getColumnName(column)
                        )
                );
            }


            writer.println();


            for (
                    int row = 0;
                    row < model.getRowCount();
                    row++
            ) {

                for (
                        int column = 0;
                        column < model.getColumnCount();
                        column++
                ) {

                    if (column > 0) {
                        writer.print(",");
                    }


                    Object value = model.getValueAt(
                            row,
                            column
                    );


                    writer.print(
                            escapeCsv(
                                    String.valueOf(value)
                            )
                    );
                }


                writer.println();
            }


            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully:\n"
                    + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );


        } catch (Exception error) {

            showError(
                    "The report could not be exported.",
                    error
            );
        }
    }


    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }


        String safeValue = value.replace(
                "\"",
                "\"\""
        );


        if (safeValue.contains(",")
                || safeValue.contains("\"")
                || safeValue.contains("\n")
                || safeValue.contains("\r")) {

            safeValue = "\"" + safeValue + "\"";
        }


        return safeValue;
    }


    // ================= ERRORS =================


    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Academic Reports",
                JOptionPane.WARNING_MESSAGE
        );
    }


    private void showError(
            String title,
            Exception error
    ) {

        Throwable cause = error;


        if (error instanceof ExecutionException
                && error.getCause() != null) {

            cause = error.getCause();
        }


        cause.printStackTrace();


        JOptionPane.showMessageDialog(
                this,
                title + "\n\n"
                + cause.getMessage(),
                "Report Error",
                JOptionPane.ERROR_MESSAGE
        );
    }


    // ================= RESULT MODELS =================


    private static class StudentSubjectRecord {


        String academicYear = "";
        String term = "";
        String studentName = "";
        String admission = "";
        String subjectName = "";
        String subjectCode = "";


        int assessmentCount;


        final Map<String, List<BigDecimal[]>> scores =
                new HashMap<>();


        StudentSubjectRecord(
                String first,
                String second,
                String third,
                String fourth
        ) {

            if (first.contains(" ") && second.length() <= 20) {

                this.studentName = first;
                this.admission = second;
                this.subjectName = third;
                this.subjectCode = fourth;

            } else {

                this.academicYear = first;
                this.term = second;
                this.subjectName = third;
                this.subjectCode = fourth;
            }
        }


        void addScore(
                String assessmentType,
                BigDecimal score,
                BigDecimal maxScore
        ) {

            List<BigDecimal[]> list =
                    scores.computeIfAbsent(
                            assessmentType,
                            k -> new ArrayList<>()
                    );


            list.add(new BigDecimal[]{
                score,
                maxScore
            });


            assessmentCount++;
        }


        BigDecimal calculateAverage(
                GradingPolicy policy
        ) {

            if (policy == null
                    || policy.weights.isEmpty()) {
                return null;
            }


            BigDecimal weightedTotal =
                    BigDecimal.ZERO;

            BigDecimal weightTotal =
                    BigDecimal.ZERO;


            for (
                    Map.Entry<String, List<BigDecimal[]>> entry
                            : scores.entrySet()
            ) {

                String assessmentType =
                        entry.getKey();

                BigDecimal weight =
                        policy.weights.get(
                                assessmentType
                        );


                if (weight == null) {
                    continue;
                }


                BigDecimal percentageTotal =
                        BigDecimal.ZERO;


                for (BigDecimal[] scorePair
                        : entry.getValue()) {

                    BigDecimal score = scorePair[0];
                    BigDecimal maxScore = scorePair[1];


                    if (maxScore.compareTo(
                            BigDecimal.ZERO) == 0) {
                        continue;
                    }


                    percentageTotal =
                            percentageTotal.add(
                                    score.multiply(
                                            new BigDecimal("100")
                                    ).divide(
                                            maxScore,
                                            4,
                                            RoundingMode.HALF_UP
                                    )
                            );
                }


                BigDecimal averagePercentage =
                        percentageTotal.divide(
                                BigDecimal.valueOf(
                                        entry.getValue().size()
                                ),
                                4,
                                RoundingMode.HALF_UP
                        );


                weightedTotal =
                        weightedTotal.add(
                                averagePercentage.multiply(weight)
                        );

                weightTotal =
                        weightTotal.add(weight);
            }


            if (weightTotal.compareTo(
                    BigDecimal.ZERO) == 0) {
                return null;
            }


            return weightedTotal.divide(
                    weightTotal,
                    2,
                    RoundingMode.HALF_UP
            );
        }
    }


    private static class GradingPolicy {


        int policyId = -1;
        String missingRule = "EXCLUDE";

        final Map<String, BigDecimal> weights =
                new HashMap<>();

        final List<GradeBand> bands =
                new ArrayList<>();
    }


    private static class GradeBand {


        final String label;
        final BigDecimal minimum;
        final BigDecimal maximum;


        GradeBand(
                String label,
                BigDecimal minimum,
                BigDecimal maximum
        ) {

            this.label = label;
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }


    private static class TranscriptResult {


        final List<Object[]> rows =
                new ArrayList<>();

        BigDecimal overallAverage;
        String overallGrade;
        int subjectCount;
    }


    private static class AcademicYearItem {


        final int id;
        final String year;


        AcademicYearItem(
                int id,
                String year
        ) {

            this.id = id;
            this.year = year;
        }


        @Override
        public String toString() {
            return year;
        }
    }


    private static class ClassItem {


        final int id;
        final String name;


        ClassItem(
                int id,
                String name
        ) {

            this.id = id;
            this.name = name;
        }


        @Override
        public String toString() {
            return name;
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