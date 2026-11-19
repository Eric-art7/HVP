import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;


public class BursarFinancePanel extends JPanel {


    private static final long serialVersionUID = 1L;


    private static final Color DARK_BLUE =
            new Color(15, 40, 80);

    private static final Color BACKGROUND =
            new Color(243, 246, 251);

    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);

    private static final Color SUCCESS_GREEN =
            new Color(25, 135, 84);

    private static final Color WARNING_ORANGE =
            new Color(217, 119, 6);

    private static final Color DANGER_RED =
            new Color(220, 53, 69);

    private static final Color WHITE =
            Color.WHITE;


    private final String bursarUsername;
    private final int initialTab;


    private JLabel chargedValue;
    private JLabel paidValue;
    private JLabel adjustmentsValue;
    private JLabel outstandingValue;


    private JComboBox<StudentItem> chargeStudentSelector;
    private JComboBox<AcademicYearItem> chargeYearSelector;
    private JComboBox<String> chargeTermSelector;
    private JTextField chargeTypeField;
    private JTextField chargeAmountField;
    private JTextField chargeDueDateField;


    private JComboBox<StudentItem> paymentStudentSelector;
    private JComboBox<String> paymentTermSelector;
    private JComboBox<String> paymentMethodSelector;
    private JTextField paymentAmountField;
    private JTextField paymentDateField;
    private JTextField receiptField;
    private JTextField paymentRemarksField;


    private JTable chargesTable;
    private DefaultTableModel chargesModel;


    private JTable paymentsTable;
    private DefaultTableModel paymentsModel;


    private JTable outstandingTable;
    private DefaultTableModel outstandingModel;


    private JButton refreshButton;
    private JButton addChargeButton;
    private JButton recordPaymentButton;
    private JButton exportOutstandingButton;


    private JLabel statusLabel;


    public BursarFinancePanel(String bursarUsername) {
        this(bursarUsername, 0);
    }


    public BursarFinancePanel(
            String bursarUsername,
            int initialTab
    ) {

        this.bursarUsername = bursarUsername;
        this.initialTab = initialTab;


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


        loadStudentsForSelectors();
        loadAcademicYears();
        loadSummary();
        loadCharges();
        loadPayments();
        loadOutstanding();
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


        JLabel icon = new JLabel("🧮");

        icon.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        28
                )
        );


        JLabel title = new JLabel(
                "Bursar Finance"
        );

        title.setFont(
                AppSupport.font(Font.BOLD, 26)
        );

        title.setForeground(DARK_BLUE);


        titlePanel.add(icon);
        titlePanel.add(title);


        JLabel subtitle = new JLabel(
                "Manage school fees, payments, and balances."
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
            loadCharges();
            loadPayments();
            loadOutstanding();
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
                "Fee Charges",
                createChargesTab()
        );

        tabs.addTab(
                "Payments",
                createPaymentsTab()
        );

        tabs.addTab(
                "Outstanding Balances",
                createOutstandingTab()
        );


        if (initialTab >= 0
                && initialTab < tabs.getTabCount()) {

            tabs.setSelectedIndex(initialTab);
        }


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


        chargedValue = new JLabel("...");
        paidValue = new JLabel("...");
        adjustmentsValue = new JLabel("...");
        outstandingValue = new JLabel("...");


        summaryPanel.add(
                createStatCard(
                        "Total Charged",
                        "🧾",
                        chargedValue,
                        ACCENT_BLUE
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Total Paid",
                        "💰",
                        paidValue,
                        SUCCESS_GREEN
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Adjustments",
                        "📉",
                        adjustmentsValue,
                        WARNING_ORANGE
                )
        );

        summaryPanel.add(
                createStatCard(
                        "Outstanding",
                        "⚠",
                        outstandingValue,
                        DANGER_RED
                )
        );


        return summaryPanel;
    }


    private JPanel createStatCard(
            String title,
            String icon,
            JLabel valueLabel,
            Color valueColor
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
                AppSupport.font(Font.BOLD, 28)
        );

        valueLabel.setForeground(valueColor);
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


    // ================= CHARGES TAB =================


    private JPanel createChargesTab() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setOpaque(false);


        chargesModel = new DefaultTableModel(
                new String[]{
                    "Charge ID",
                    "Student",
                    "Admission No.",
                    "Academic Year",
                    "Term",
                    "Charge Type",
                    "Amount",
                    "Due Date"
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


        chargesTable = new JTable(chargesModel);

        chargesTable.setRowHeight(32);
        chargesTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        chargesTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane tableScroll =
                new JScrollPane(chargesTable);


        JPanel formPanel =
                createChargeFormPanel();


        panel.add(
                formPanel,
                BorderLayout.NORTH
        );

        panel.add(
                tableScroll,
                BorderLayout.CENTER
        );


        return panel;
    }


    private JPanel createChargeFormPanel() {

        JPanel form = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        form.setBackground(WHITE);
        form.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        chargeStudentSelector =
                new JComboBox<>();

        chargeYearSelector =
                new JComboBox<>();

        chargeTermSelector = new JComboBox<>(
                new String[]{
                    "Term 1",
                    "Term 2",
                    "Term 3"
                }
        );

        chargeTypeField = createTextField();
        chargeAmountField = createTextField();
        chargeDueDateField = createTextField();


        chargeStudentSelector.setPreferredSize(
                new Dimension(230, 38)
        );

        chargeYearSelector.setPreferredSize(
                new Dimension(150, 38)
        );

        chargeTermSelector.setPreferredSize(
                new Dimension(120, 38)
        );

        chargeTypeField.setPreferredSize(
                new Dimension(180, 38)
        );

        chargeAmountField.setPreferredSize(
                new Dimension(120, 38)
        );

        chargeDueDateField.setPreferredSize(
                new Dimension(140, 38)
        );


        addChargeButton = new JButton(
                "Add Charge"
        );

        addChargeButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        addChargeButton.setBackground(ACCENT_BLUE);
        addChargeButton.setForeground(WHITE);
        addChargeButton.setFocusPainted(false);
        addChargeButton.setPreferredSize(
                new Dimension(130, 38)
        );

        addChargeButton.addActionListener(
                event -> addFeeCharge()
        );


        form.add(
                labeledField(
                        "Student",
                        chargeStudentSelector
                )
        );

        form.add(
                labeledField(
                        "Academic Year",
                        chargeYearSelector
                )
        );

        form.add(
                labeledField(
                        "Term",
                        chargeTermSelector
                )
        );

        form.add(
                labeledField(
                        "Charge Type",
                        chargeTypeField
                )
        );

        form.add(
                labeledField(
                        "Amount",
                        chargeAmountField
                )
        );

        form.add(
                labeledField(
                        "Due Date",
                        chargeDueDateField
                )
        );

        form.add(addChargeButton);


        return form;
    }


    // ================= PAYMENTS TAB =================


    private JPanel createPaymentsTab() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setOpaque(false);


        paymentsModel = new DefaultTableModel(
                new String[]{
                    "Payment ID",
                    "Student",
                    "Admission No.",
                    "Amount",
                    "Payment Date",
                    "Method",
                    "Receipt No.",
                    "Term",
                    "Remarks"
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


        paymentsTable = new JTable(paymentsModel);

        paymentsTable.setRowHeight(32);
        paymentsTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        paymentsTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane tableScroll =
                new JScrollPane(paymentsTable);


        JPanel formPanel =
                createPaymentFormPanel();


        panel.add(
                formPanel,
                BorderLayout.NORTH
        );

        panel.add(
                tableScroll,
                BorderLayout.CENTER
        );


        return panel;
    }


    private JPanel createPaymentFormPanel() {

        JPanel form = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        form.setBackground(WHITE);
        form.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        paymentStudentSelector =
                new JComboBox<>();

        paymentTermSelector = new JComboBox<>(
                new String[]{
                    "Term 1",
                    "Term 2",
                    "Term 3"
                }
        );

        paymentMethodSelector = new JComboBox<>(
                new String[]{
                    "Cash",
                    "Bank",
                    "Mobile Money",
                    "Cheque"
                }
        );

        paymentAmountField = createTextField();
        paymentDateField = createTextField();
        receiptField = createTextField();
        paymentRemarksField = createTextField();


        paymentStudentSelector.setPreferredSize(
                new Dimension(230, 38)
        );

        paymentTermSelector.setPreferredSize(
                new Dimension(120, 38)
        );

        paymentMethodSelector.setPreferredSize(
                new Dimension(150, 38)
        );

        paymentAmountField.setPreferredSize(
                new Dimension(120, 38)
        );

        paymentDateField.setPreferredSize(
                new Dimension(140, 38)
        );

        receiptField.setPreferredSize(
                new Dimension(150, 38)
        );

        paymentRemarksField.setPreferredSize(
                new Dimension(180, 38)
        );


        recordPaymentButton = new JButton(
                "Record Payment"
        );

        recordPaymentButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        recordPaymentButton.setBackground(
                SUCCESS_GREEN
        );

        recordPaymentButton.setForeground(WHITE);
        recordPaymentButton.setFocusPainted(false);
        recordPaymentButton.setPreferredSize(
                new Dimension(160, 38)
        );

        recordPaymentButton.addActionListener(
                event -> recordPayment()
        );


        form.add(
                labeledField(
                        "Student",
                        paymentStudentSelector
                )
        );

        form.add(
                labeledField(
                        "Amount",
                        paymentAmountField
                )
        );

        form.add(
                labeledField(
                        "Payment Date",
                        paymentDateField
                )
        );

        form.add(
                labeledField(
                        "Method",
                        paymentMethodSelector
                )
        );

        form.add(
                labeledField(
                        "Receipt No.",
                        receiptField
                )
        );

        form.add(
                labeledField(
                        "Term",
                        paymentTermSelector
                )
        );

        form.add(
                labeledField(
                        "Remarks",
                        paymentRemarksField
                )
        );

        form.add(recordPaymentButton);


        return form;
    }


    // ================= OUTSTANDING TAB =================


    private JPanel createOutstandingTab() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 15)
        );

        panel.setOpaque(false);


        outstandingModel = new DefaultTableModel(
                new String[]{
                    "Student",
                    "Admission No.",
                    "Class",
                    "Total Charged",
                    "Adjustments",
                    "Total Paid",
                    "Outstanding"
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


        outstandingTable = new JTable(
                outstandingModel
        );

        outstandingTable.setRowHeight(32);
        outstandingTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        outstandingTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        JScrollPane tableScroll =
                new JScrollPane(outstandingTable);


        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        buttonPanel.setOpaque(false);


        exportOutstandingButton = new JButton(
                "Export Outstanding Report"
        );

        exportOutstandingButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        exportOutstandingButton.setBackground(DARK_BLUE);
        exportOutstandingButton.setForeground(WHITE);
        exportOutstandingButton.setFocusPainted(false);

        exportOutstandingButton.addActionListener(
                event -> exportOutstandingReport()
        );


        buttonPanel.add(exportOutstandingButton);


        panel.add(
                buttonPanel,
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


        JLabel bursar = new JLabel(
                "Bursar: " + bursarUsername
        );

        bursar.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        bursar.setForeground(DARK_BLUE);


        statusLabel = new JLabel("Ready.");

        statusLabel.setFont(
                AppSupport.font(Font.PLAIN, 13)
        );


        footer.add(
                bursar,
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


    private JTextField createTextField() {

        JTextField field = new JTextField();

        field.setFont(
                AppSupport.font(Font.PLAIN, 15)
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        180,
                                        200,
                                        230
                                )
                        ),
                        new EmptyBorder(
                                8,
                                12,
                                8,
                                12
                        )
                )
        );


        return field;
    }


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


    private void loadStudentsForSelectors() {

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

                    List<StudentItem> students =
                            get();


                    for (StudentItem student
                            : students) {

                        chargeStudentSelector
                                .addItem(student);

                        paymentStudentSelector
                                .addItem(student);
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


    private void loadAcademicYears() {

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

                        chargeYearSelector
                                .addItem(year);
                    }


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


    // ================= SUMMARY =================


    private void loadSummary() {

        chargedValue.setText("...");
        paidValue.setText("...");
        adjustmentsValue.setText("...");
        outstandingValue.setText("...");


        SwingWorker<BigDecimal[], Void> worker =
                new SwingWorker<BigDecimal[], Void>() {


            @Override
            protected BigDecimal[] doInBackground()
                    throws Exception {


                BigDecimal[] values =
                        new BigDecimal[4];


                try (
                        Connection con =
                                Conn.getConnection()
                ) {

                    values[0] = getSingleSum(
                            con,
                            "SELECT COALESCE(SUM(amount), 0) FROM fee_charges"
                    );

                    values[1] = getSingleSum(
                            con,
                            "SELECT COALESCE(SUM(amount), 0) FROM payments"
                    );

                    values[2] = getSingleSum(
                            con,
                            "SELECT COALESCE(SUM(amount), 0) FROM fee_adjustments"
                    );


                    values[3] = values[0]
                            .subtract(values[2])
                            .subtract(values[1]);


                    if (values[3].compareTo(
                            BigDecimal.ZERO) < 0) {

                        values[3] = BigDecimal.ZERO;
                    }
                }


                return values;
            }


            @Override
            protected void done() {

                try {

                    BigDecimal[] values = get();


                    chargedValue.setText(
                            formatMoney(values[0])
                    );

                    paidValue.setText(
                            formatMoney(values[1])
                    );

                    adjustmentsValue.setText(
                            formatMoney(values[2])
                    );

                    outstandingValue.setText(
                            formatMoney(values[3])
                    );


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Financial summary could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private BigDecimal getSingleSum(
            Connection con,
            String sql
    ) throws SQLException {

        try (
                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

            return BigDecimal.ZERO;
        }
    }


    private String formatMoney(
            BigDecimal value
    ) {

        if (value == null) {
            value = BigDecimal.ZERO;
        }


        return "RWF "
                + String.format(
                        "%,.2f",
                        value
                );
    }


    // ================= CHARGES =================


    private void loadCharges() {

        chargesModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        fc.charge_id,
                        CONCAT(
                            s.first_name,
                            ' ',
                            s.last_name
                        ) AS student_name,
                        s.admission_number,
                        a.academic_year,
                        fc.term,
                        fc.charge_type,
                        fc.amount,
                        fc.due_date
                    FROM fee_charges fc
                    JOIN students s
                        ON s.student_id =
                            fc.student_id
                    JOIN academic_years a
                        ON a.academic_year_id =
                            fc.academic_year_id
                    ORDER BY
                        fc.created_at DESC,
                        fc.charge_id DESC
                    LIMIT 300
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
                            rs.getInt("charge_id"),
                            rs.getString("student_name"),
                            rs.getString("admission_number"),
                            rs.getString("academic_year"),
                            rs.getString("term"),
                            rs.getString("charge_type"),
                            formatMoney(
                                    rs.getBigDecimal("amount")
                            ),
                            rs.getDate("due_date")
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        chargesModel.addRow(row);
                    }


                    setStatus(
                            chargesModel.getRowCount()
                            + " fee charge(s) loaded."
                    );


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Fee charges could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private void addFeeCharge() {

        StudentItem student =
                (StudentItem) chargeStudentSelector
                        .getSelectedItem();

        AcademicYearItem year =
                (AcademicYearItem) chargeYearSelector
                        .getSelectedItem();

        String term = String.valueOf(
                chargeTermSelector.getSelectedItem()
        );

        String chargeType =
                chargeTypeField.getText().trim();

        String amountText =
                chargeAmountField.getText().trim();

        String dueDate =
                chargeDueDateField.getText().trim();


        if (student == null || year == null) {

            showWarning(
                    "Select a student and academic year."
            );

            return;
        }


        if (chargeType.isEmpty()
                || amountText.isEmpty()) {

            showWarning(
                    "Enter both charge type and amount."
            );

            return;
        }


        BigDecimal amount;

        try {

            amount = new BigDecimal(amountText);


            if (amount.compareTo(
                    BigDecimal.ZERO) <= 0) {

                throw new NumberFormatException();
            }


        } catch (NumberFormatException error) {

            showWarning(
                    "Enter a valid positive amount."
            );

            return;
        }


        LocalDate parsedDueDate = null;


        if (!dueDate.isEmpty()) {

            try {

                parsedDueDate =
                        LocalDate.parse(dueDate);

            } catch (Exception error) {

                showWarning(
                        "Enter due date as YYYY-MM-DD."
                );

                return;
            }
        }


        String sql = """
            INSERT INTO fee_charges
            (
                student_id,
                academic_year_id,
                term,
                charge_type,
                amount,
                currency_code,
                due_date
            )
            VALUES (?, ?, ?, ?, ?, 'RWF', ?)
            """;


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, student.id);
            ps.setInt(2, year.id);
            ps.setString(3, term);
            ps.setString(4, chargeType);
            ps.setBigDecimal(5, amount);

            if (parsedDueDate == null) {
                ps.setNull(
                        6,
                        java.sql.Types.DATE
                );

            } else {

                ps.setDate(
                        6,
                        java.sql.Date.valueOf(
                                parsedDueDate
                        )
                );
            }


            ps.executeUpdate();


            chargeTypeField.setText("");
            chargeAmountField.setText("");
            chargeDueDateField.setText("");


            loadCharges();
            loadSummary();
            loadOutstanding();


            setStatus(
                    "Fee charge added for "
                    + student.name + "."
            );


        } catch (Exception error) {

            showError(
                    "The fee charge could not be added.",
                    error
            );
        }
    }


    // ================= PAYMENTS =================


    private void loadPayments() {

        paymentsModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        p.payment_id,
                        CONCAT(
                            s.first_name,
                            ' ',
                            s.last_name
                        ) AS student_name,
                        s.admission_number,
                        p.amount,
                        p.payment_date,
                        p.payment_method,
                        p.receipt_number,
                        p.term,
                        p.remarks
                    FROM payments p
                    JOIN students s
                        ON s.student_id =
                            p.student_id
                    ORDER BY
                        p.payment_date DESC,
                        p.payment_id DESC
                    LIMIT 300
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
                            rs.getInt("payment_id"),
                            rs.getString("student_name"),
                            rs.getString("admission_number"),
                            formatMoney(
                                    rs.getBigDecimal("amount")
                            ),
                            rs.getDate("payment_date"),
                            rs.getString("payment_method"),
                            rs.getString("receipt_number"),
                            rs.getString("term"),
                            rs.getString("remarks")
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        paymentsModel.addRow(row);
                    }


                    setStatus(
                            paymentsModel.getRowCount()
                            + " payment(s) loaded."
                    );


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Payments could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private void recordPayment() {

        StudentItem student =
                (StudentItem) paymentStudentSelector
                        .getSelectedItem();

        String amountText =
                paymentAmountField.getText().trim();

        String paymentDate =
                paymentDateField.getText().trim();

        String method = String.valueOf(
                paymentMethodSelector.getSelectedItem()
        );

        String receipt =
                receiptField.getText().trim();

        String term = String.valueOf(
                paymentTermSelector.getSelectedItem()
        );

        String remarks =
                paymentRemarksField.getText().trim();


        if (student == null) {

            showWarning("Select a student.");

            return;
        }


        BigDecimal amount;


        try {

            amount = new BigDecimal(amountText);


            if (amount.compareTo(
                    BigDecimal.ZERO) <= 0) {

                throw new NumberFormatException();
            }


        } catch (NumberFormatException error) {

            showWarning(
                    "Enter a valid positive payment amount."
            );

            return;
        }


        LocalDate parsedPaymentDate;


        try {

            parsedPaymentDate =
                    LocalDate.parse(paymentDate);

        } catch (Exception error) {

            showWarning(
                    "Enter payment date as YYYY-MM-DD."
            );

            return;
        }


        String sql = """
            INSERT INTO payments
            (
                student_id,
                amount,
                payment_date,
                payment_method,
                receipt_number,
                term,
                remarks
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, student.id);
            ps.setBigDecimal(2, amount);
            ps.setDate(
                    3,
                    java.sql.Date.valueOf(
                            parsedPaymentDate
                    )
            );

            ps.setString(4, method);

            if (receipt.isEmpty()) {
                ps.setNull(
                        5,
                        java.sql.Types.VARCHAR
                );

            } else {
                ps.setString(5, receipt);
            }

            ps.setString(6, term);

            if (remarks.isEmpty()) {
                ps.setNull(
                        7,
                        java.sql.Types.VARCHAR
                );

            } else {
                ps.setString(7, remarks);
            }


            ps.executeUpdate();


            clearPaymentForm();


            loadPayments();
            loadSummary();
            loadOutstanding();


            setStatus(
                    "Payment recorded for "
                    + student.name + "."
            );


        } catch (Exception error) {

            showError(
                    "The payment could not be recorded.",
                    error
            );
        }
    }


    private void clearPaymentForm() {

        paymentAmountField.setText("");
        paymentDateField.setText("");
        receiptField.setText("");
        paymentRemarksField.setText("");
    }


    // ================= OUTSTANDING =================


    private void loadOutstanding() {

        outstandingModel.setRowCount(0);


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        CONCAT(
                            s.first_name,
                            ' ',
                            s.last_name
                        ) AS student_name,
                        s.admission_number,
                        COALESCE(
                            c.class_name,
                            'Not Assigned'
                        ) AS class_name,
                        COALESCE(charges.total, 0) AS total_charged,
                        COALESCE(adjustments.total, 0) AS total_adjustments,
                        COALESCE(payments.total, 0) AS total_paid
                    FROM students s
                    LEFT JOIN classes c
                        ON c.class_id = s.class_id
                    LEFT JOIN
                    (
                        SELECT
                            student_id,
                            SUM(amount) AS total
                        FROM fee_charges
                        GROUP BY student_id
                    ) charges
                        ON charges.student_id =
                            s.student_id
                    LEFT JOIN
                    (
                        SELECT
                            student_id,
                            SUM(amount) AS total
                        FROM fee_adjustments
                        GROUP BY student_id
                    ) adjustments
                        ON adjustments.student_id =
                            s.student_id
                    LEFT JOIN
                    (
                        SELECT
                            student_id,
                            SUM(amount) AS total
                        FROM payments
                        GROUP BY student_id
                    ) payments
                        ON payments.student_id =
                            s.student_id
                    WHERE s.is_active = 1
                    ORDER BY student_name
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

                        BigDecimal charged =
                                rs.getBigDecimal(
                                        "total_charged"
                                );

                        BigDecimal adjustments =
                                rs.getBigDecimal(
                                        "total_adjustments"
                                );

                        BigDecimal paid =
                                rs.getBigDecimal(
                                        "total_paid"
                                );


                        BigDecimal outstanding =
                                charged
                                        .subtract(adjustments)
                                        .subtract(paid);


                        if (outstanding.compareTo(
                                BigDecimal.ZERO) < 0) {

                            outstanding =
                                    BigDecimal.ZERO;
                        }


                        rows.add(new Object[]{
                            rs.getString("student_name"),
                            rs.getString("admission_number"),
                            rs.getString("class_name"),
                            formatMoney(charged),
                            formatMoney(adjustments),
                            formatMoney(paid),
                            formatMoney(outstanding)
                        });
                    }
                }


                return rows;
            }


            @Override
            protected void done() {

                try {

                    for (Object[] row : get()) {
                        outstandingModel.addRow(row);
                    }


                    setStatus(
                            outstandingModel.getRowCount()
                            + " student balance(s) loaded."
                    );


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Outstanding balances could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    // ================= CSV EXPORT =================


    private void exportOutstandingReport() {

        if (outstandingModel.getRowCount() == 0) {

            showWarning(
                    "There is no outstanding report to export."
            );

            return;
        }


        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
                new File("outstanding-balances.csv")
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


        String[] headers = {
            "Student",
            "Admission Number",
            "Class",
            "Total Charged",
            "Adjustments",
            "Total Paid",
            "Outstanding"
        };


        try (
                PrintWriter writer =
                        new PrintWriter(
                                file,
                                "UTF-8"
                        )
        ) {

            writer.println(
                    "HVP Gatagara Outstanding Balances Report"
            );

            writer.println();


            writer.println(
                    escapeCsv(
                            String.join(
                                    ",",
                                    headers
                            )
                    )
            );


            for (
                    int row = 0;
                    row < outstandingModel
                            .getRowCount();
                    row++
            ) {

                StringBuilder line =
                        new StringBuilder();


                for (
                        int column = 0;
                        column < outstandingModel
                                .getColumnCount();
                        column++
                ) {

                    if (column > 0) {
                        line.append(",");
                    }


                    line.append(
                            escapeCsv(
                                    String.valueOf(
                                            outstandingModel
                                                    .getValueAt(
                                                            row,
                                                            column
                                                    )
                                    )
                            )
                    );
                }


                writer.println(line);
            }


            JOptionPane.showMessageDialog(
                    this,
                    "Outstanding report exported:\n"
                    + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );


        } catch (Exception error) {

            showError(
                    "The outstanding report could not be exported.",
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
                "Bursar Finance",
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
                "Finance Error",
                JOptionPane.ERROR_MESSAGE
        );
    }


    // ================= ITEM MODELS =================


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
}