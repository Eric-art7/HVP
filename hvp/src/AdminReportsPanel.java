import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;


public class AdminReportsPanel extends JPanel {


    private static final long serialVersionUID = 1L;


    private static final Color DARK_BLUE =
            new Color(15, 40, 80);

    private static final Color BACKGROUND =
            new Color(243, 246, 251);

    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);

    private static final Color WHITE =
            Color.WHITE;


    private final String adminUsername;


    private JLabel studentsValue;
    private JLabel teachersValue;
    private JLabel classesValue;
    private JLabel subjectsValue;


    private JTable classReportTable;
    private DefaultTableModel classReportModel;


    private JTable studentTable;
    private DefaultTableModel studentModel;


    private JButton refreshButton;
    private JButton exportStudentsButton;
    private JButton exportClassReportButton;


    public AdminReportsPanel(String adminUsername) {

        this.adminUsername = adminUsername;


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


        loadSummary();
        loadClassReport();
        loadStudents();
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


        JLabel icon = new JLabel("📊");

        icon.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        28
                )
        );


        JLabel title = new JLabel(
                "Reports and Export"
        );

        title.setFont(
                AppSupport.font(Font.BOLD, 26)
        );

        title.setForeground(DARK_BLUE);


        titlePanel.add(icon);
        titlePanel.add(title);


        JLabel subtitle = new JLabel(
                "View school statistics and export reports."
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

            loadSummary();
            loadClassReport();
            loadStudents();
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


        mainContent.add(
                createSummaryPanel(),
                BorderLayout.NORTH
        );


        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(
                AppSupport.font(Font.BOLD, 14)
        );


        tabs.addTab(
                "Students by Class",
                createClassReportTab()
        );

        tabs.addTab(
                "Student Directory",
                createStudentDirectoryTab()
        );


        mainContent.add(
                tabs,
                BorderLayout.CENTER
        );


        return mainContent;
    }


    // ================= SUMMARY =================


    private JPanel createSummaryPanel() {

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 4, 15, 15)
        );

        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(
                new Dimension(0, 130)
        );


        studentsValue = new JLabel("...");
        teachersValue = new JLabel("...");
        classesValue = new JLabel("...");
        subjectsValue = new JLabel("...");


        summaryPanel.add(
                createStatCard(
                        "Total Students",
                        "🎓",
                        studentsValue
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Total Teachers",
                        "👤",
                        teachersValue
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Total Classes",
                        "🏫",
                        classesValue
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Total Subjects",
                        "📚",
                        subjectsValue
                )
        );


        return summaryPanel;
    }


    private JPanel createStatCard(
            String title,
            String icon,
            JLabel valueLabel
    ) {

        JPanel card = new JPanel();

        card.setBackground(WHITE);
        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );


        JLabel iconLabel = new JLabel(icon);

        iconLabel.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        30
                )
        );

        iconLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        titleLabel.setForeground(
                new Color(90, 105, 125)
        );

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        valueLabel.setFont(
                AppSupport.font(Font.BOLD, 30)
        );

        valueLabel.setForeground(ACCENT_BLUE);
        valueLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());


        return card;
    }


    // ================= CLASS REPORT =================


    private JPanel createClassReportTab() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 15)
        );

        panel.setOpaque(false);


        classReportModel = new DefaultTableModel(
                new String[]{
                    "Class",
                    "Academic Year",
                    "Total Students",
                    "Male",
                    "Female",
                    "Other"
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


        classReportTable = new JTable(
                classReportModel
        );

        classReportTable.setRowHeight(32);
        classReportTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        classReportTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane scrollPane =
                new JScrollPane(classReportTable);


        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        buttonPanel.setOpaque(false);


        exportClassReportButton = new JButton(
                "Export Class Report"
        );

        exportClassReportButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        exportClassReportButton.setBackground(DARK_BLUE);
        exportClassReportButton.setForeground(WHITE);
        exportClassReportButton.setFocusPainted(false);

        exportClassReportButton.addActionListener(
                event -> exportClassReport()
        );


        buttonPanel.add(exportClassReportButton);


        panel.add(
                buttonPanel,
                BorderLayout.NORTH
        );

        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= STUDENT DIRECTORY =================


    private JPanel createStudentDirectoryTab() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 15)
        );

        panel.setOpaque(false);


        studentModel = new DefaultTableModel(
                new String[]{
                    "Admission No.",
                    "Full Name",
                    "Gender",
                    "Class",
                    "Guardian",
                    "Guardian Phone",
                    "Status"
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


        studentTable = new JTable(studentModel);

        studentTable.setRowHeight(32);
        studentTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        studentTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane scrollPane =
                new JScrollPane(studentTable);


        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        buttonPanel.setOpaque(false);


        exportStudentsButton = new JButton(
                "Export Students"
        );

        exportStudentsButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        exportStudentsButton.setBackground(DARK_BLUE);
        exportStudentsButton.setForeground(WHITE);
        exportStudentsButton.setFocusPainted(false);

        exportStudentsButton.addActionListener(
                event -> exportStudents()
        );


        buttonPanel.add(exportStudentsButton);


        panel.add(
                buttonPanel,
                BorderLayout.NORTH
        );

        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= DATA LOADING =================


    private void loadSummary() {

        studentsValue.setText("...");
        teachersValue.setText("...");
        classesValue.setText("...");
        subjectsValue.setText("...");


        SwingWorker<int[], Void> worker =
                new SwingWorker<int[], Void>() {


            @Override
            protected int[] doInBackground()
                    throws Exception {


                int[] counts = new int[4];


                try (
                        Connection con =
                                Conn.getConnection()
                ) {

                    counts[0] = getCount(
                            con,
                            "students"
                    );

                    counts[1] = getCount(
                            con,
                            "teachers"
                    );

                    counts[2] = getCount(
                            con,
                            "classes"
                    );

                    counts[3] = getCount(
                            con,
                            "subjects"
                    );
                }


                return counts;
            }


            @Override
            protected void done() {

                try {

                    int[] counts = get();


                    studentsValue.setText(
                            String.valueOf(counts[0])
                    );

                    teachersValue.setText(
                            String.valueOf(counts[1])
                    );

                    classesValue.setText(
                            String.valueOf(counts[2])
                    );

                    subjectsValue.setText(
                            String.valueOf(counts[3])
                    );


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();
                    setSummaryUnavailable();


                } catch (ExecutionException error) {

                    setSummaryUnavailable();

                    showError(
                            "Summary statistics could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private int getCount(
            Connection con,
            String table
    ) throws Exception {

        String sql = "SELECT COUNT(*) FROM " + table;


        try (
                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }


    private void setSummaryUnavailable() {

        studentsValue.setText("Unavailable");
        teachersValue.setText("Unavailable");
        classesValue.setText("Unavailable");
        subjectsValue.setText("Unavailable");
    }


    private void loadClassReport() {

        classReportModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        c.class_name,
                        a.academic_year,
                        COUNT(s.student_id) AS total_students,
                        SUM(
                            CASE WHEN s.gender = 'Male'
                            THEN 1 ELSE 0 END
                        ) AS male_count,
                        SUM(
                            CASE WHEN s.gender = 'Female'
                            THEN 1 ELSE 0 END
                        ) AS female_count,
                        SUM(
                            CASE WHEN s.gender = 'Other'
                            THEN 1 ELSE 0 END
                        ) AS other_count
                    FROM classes c
                    JOIN academic_years a
                        ON a.academic_year_id =
                            c.academic_year_id
                    LEFT JOIN students s
                        ON s.class_id = c.class_id
                    GROUP BY
                        c.class_id,
                        c.class_name,
                        a.academic_year
                    ORDER BY c.class_name
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

                        rows.add(new Object[]{
                            rs.getString("class_name"),
                            rs.getString("academic_year"),
                            rs.getInt("total_students"),
                            rs.getInt("male_count"),
                            rs.getInt("female_count"),
                            rs.getInt("other_count")
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        classReportModel.addRow(row);
                    }


                    if (classReportModel
                            .getRowCount() == 0) {

                        classReportModel.addRow(
                                new Object[]{
                                    "No classes found",
                                    "",
                                    0,
                                    0,
                                    0,
                                    0
                                }
                        );
                    }


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Class report could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private void loadStudents() {

        studentModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        s.admission_number,
                        CONCAT(
                            s.first_name,
                            ' ',
                            s.last_name
                        ) AS full_name,
                        s.gender,
                        COALESCE(
                            c.class_name,
                            'Not Assigned'
                        ) AS class_name,
                        COALESCE(
                            s.guardian_name,
                            ''
                        ) AS guardian_name,
                        COALESCE(
                            s.guardian_phone,
                            ''
                        ) AS guardian_phone,
                        s.is_active
                    FROM students s
                    LEFT JOIN classes c
                        ON c.class_id = s.class_id
                    ORDER BY s.first_name, s.last_name
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

                        rows.add(new Object[]{
                            rs.getString("admission_number"),
                            rs.getString("full_name"),
                            rs.getString("gender"),
                            rs.getString("class_name"),
                            rs.getString("guardian_name"),
                            rs.getString("guardian_phone"),
                            rs.getBoolean("is_active")
                                    ? "Active"
                                    : "Inactive"
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        studentModel.addRow(row);
                    }


                    if (studentModel
                            .getRowCount() == 0) {

                        studentModel.addRow(
                                new Object[]{
                                    "",
                                    "No students found",
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
                            "Student directory could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    // ================= CSV EXPORT =================


    private void exportStudents() {

        if (studentModel.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "There are no students to export.",
                    "Export",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        File file = chooseCsvFile(
                "students-report.csv"
        );

        if (file == null) {
            return;
        }


        String[] headers = {
            "Admission Number",
            "Full Name",
            "Gender",
            "Class",
            "Guardian",
            "Guardian Phone",
            "Status"
        };


        List<Object[]> rows = new ArrayList<>();


        for (
                int row = 0;
                row < studentModel.getRowCount();
                row++
        ) {

            Object[] rowData = new Object[
                    studentModel.getColumnCount()
            ];


            for (
                    int column = 0;
                    column < studentModel
                            .getColumnCount();
                    column++
            ) {

                rowData[column] =
                        studentModel.getValueAt(
                                row,
                                column
                        );
            }


            rows.add(rowData);
        }


        exportCsv(
                file,
                "HVP Gatagara Student Report",
                headers,
                rows
        );
    }


    private void exportClassReport() {

        if (classReportModel.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "There is no class report to export.",
                    "Export",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        File file = chooseCsvFile(
                "class-report.csv"
        );

        if (file == null) {
            return;
        }


        String[] headers = {
            "Class",
            "Academic Year",
            "Total Students",
            "Male",
            "Female",
            "Other"
        };


        List<Object[]> rows = new ArrayList<>();


        for (
                int row = 0;
                row < classReportModel
                        .getRowCount();
                row++
        ) {

            Object[] rowData = new Object[
                    classReportModel.getColumnCount()
            ];


            for (
                    int column = 0;
                    column < classReportModel
                            .getColumnCount();
                    column++
            ) {

                rowData[column] =
                        classReportModel.getValueAt(
                                row,
                                column
                        );
            }


            rows.add(rowData);
        }


        exportCsv(
                file,
                "HVP Gatagara Class Report",
                headers,
                rows
        );
    }


    private File chooseCsvFile(
            String defaultFileName
    ) {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setSelectedFile(
                new File(defaultFileName)
        );


        int result =
                chooser.showSaveDialog(this);


        if (result
                != JFileChooser.APPROVE_OPTION) {
            return null;
        }


        File file = chooser.getSelectedFile();


        if (!file.getName().toLowerCase()
                .endsWith(".csv")) {

            file = new File(
                    file.getAbsolutePath() + ".csv"
            );
        }


        return file;
    }


    private void exportCsv(
            File file,
            String reportTitle,
            String[] headers,
            List<Object[]> rows
    ) {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                file,
                                "UTF-8"
                        )
        ) {

            writer.println(reportTitle);
            writer.println();


            writer.println(
                    escapeCsv(
                            String.join(
                                    ",",
                                    headers
                            )
                    )
            );


            for (Object[] row : rows) {

                StringBuilder line =
                        new StringBuilder();


                for (
                        int index = 0;
                        index < row.length;
                        index++
                ) {

                    if (index > 0) {
                        line.append(",");
                    }


                    line.append(
                            escapeCsv(
                                    String.valueOf(
                                            row[index]
                                    )
                            )
                    );
                }


                writer.println(line);
            }


            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully:\n"
                    + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );


        } catch (Exception error) {

            error.printStackTrace();


            JOptionPane.showMessageDialog(
                    this,
                    "Could not export report:\n"
                    + error.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
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


    private void showError(
            String title,
            ExecutionException error
    ) {

        Throwable cause = error.getCause();

        if (cause == null) {
            cause = error;
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
}