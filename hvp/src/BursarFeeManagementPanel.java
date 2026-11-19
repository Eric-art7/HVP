import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
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

public class BursarFeeManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color BACKGROUND = new Color(243, 246, 251);

    private final String bursarUsername;

    private JComboBox<StudentItem> studentSelector;
    private JComboBox<YearItem> yearSelector;
    private JComboBox<String> termSelector;

    private JTextField chargeTypeField;
    private JTextField chargeAmountField;
    private JTextField currencyField;
    private JTextField dueDateField;

    private JComboBox<String> adjustmentTypeSelector;
    private JTextField adjustmentAmountField;
    private JTextField adjustmentRemarksField;

    private DefaultTableModel chargeModel;
    private JTable chargeTable;

    private DefaultTableModel adjustmentModel;
    private JTable adjustmentTable;

    private DefaultTableModel paymentModel;
    private JTable paymentTable;

    private DefaultTableModel allocationModel;
    private JTable allocationTable;

    private DefaultTableModel balanceModel;
    private JTable balanceTable;

    private JButton refreshButton;
    private JButton addChargeButton;
    private JButton addAdjustmentButton;
    private JButton allocatePaymentButton;

    private JLabel statusLabel;

    public BursarFeeManagementPanel(String bursarUsername) {
        this.bursarUsername = bursarUsername;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadStudents();
        loadYears();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JLabel title = new JLabel("Fee Management");
        title.setFont(AppSupport.font(Font.BOLD, 26));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Maintain fee charges, adjustments, payment allocations, "
                + "and student balances"
        );
        subtitle.setFont(AppSupport.font(Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        studentSelector = new JComboBox<>();
        studentSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        studentSelector.setPreferredSize(new Dimension(330, 32));
        studentSelector.addActionListener(
                event -> loadSelectedStudentData()
        );

        yearSelector = new JComboBox<>();
        yearSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        yearSelector.setPreferredSize(new Dimension(210, 32));
        yearSelector.addActionListener(
                event -> loadSelectedStudentData()
        );

        termSelector = new JComboBox<>(new String[]{
            "Term 1", "Term 2", "Term 3"
        });
        termSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        termSelector.setPreferredSize(new Dimension(160, 32));
        termSelector.addActionListener(
                event -> loadSelectedStudentData()
        );

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(AppSupport.font(Font.BOLD, 13));
        refreshButton.addActionListener(event -> {
            loadStudents();
            loadYears();
        });

        controls.add(new JLabel("Student:"));
        controls.add(studentSelector);
        controls.add(new JLabel("Year:"));
        controls.add(yearSelector);
        controls.add(new JLabel("Term:"));
        controls.add(termSelector);
        controls.add(refreshButton);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppSupport.font(Font.BOLD, 14));

        tabs.addTab("Fee Charges", createChargesTab());
        tabs.addTab("Adjustments", createAdjustmentsTab());
        tabs.addTab("Payments", createPaymentsTab());
        tabs.addTab("Allocations", createAllocationsTab());
        tabs.addTab("Student Balance", createBalanceTab());

        return tabs;
    }

    private JPanel createChargesTab() {
        chargeModel = new DefaultTableModel(
                new String[]{
                    "Charge ID", "Term", "Charge Type", "Amount",
                    "Currency", "Due Date"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        chargeTable = createTable(chargeModel);

        JPanel form = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        form.setOpaque(false);

        chargeTypeField = createTextField(180);
        chargeAmountField = createTextField(130);
        currencyField = createTextField(90);
        dueDateField = createTextField(130);

        currencyField.setText("RWF");

        addChargeButton = new JButton("Add Fee Charge");
        addChargeButton.setFont(AppSupport.font(Font.BOLD, 13));
        addChargeButton.addActionListener(
                event -> addFeeCharge()
        );

        form.add(new JLabel("Charge type:"));
        form.add(chargeTypeField);
        form.add(new JLabel("Amount:"));
        form.add(chargeAmountField);
        form.add(new JLabel("Currency:"));
        form.add(currencyField);
        form.add(new JLabel("Due date (YYYY-MM-DD):"));
        form.add(dueDateField);
        form.add(addChargeButton);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(chargeTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdjustmentsTab() {
        adjustmentModel = new DefaultTableModel(
                new String[]{
                    "Adjustment ID", "Term", "Type", "Amount",
                    "Currency", "Remarks"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        adjustmentTable = createTable(adjustmentModel);

        JPanel form = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        form.setOpaque(false);

        adjustmentTypeSelector = new JComboBox<>(new String[]{
            "Discount", "Scholarship", "Correction", "Refund"
        });
        adjustmentTypeSelector.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        adjustmentTypeSelector.setPreferredSize(
                new Dimension(180, 32)
        );

        adjustmentAmountField = createTextField(130);
        adjustmentRemarksField = createTextField(240);

        addAdjustmentButton = new JButton("Add Adjustment");
        addAdjustmentButton.setFont(AppSupport.font(Font.BOLD, 13));
        addAdjustmentButton.addActionListener(
                event -> addAdjustment()
        );

        form.add(new JLabel("Type:"));
        form.add(adjustmentTypeSelector);
        form.add(new JLabel("Amount:"));
        form.add(adjustmentAmountField);
        form.add(new JLabel("Remarks:"));
        form.add(adjustmentRemarksField);
        form.add(addAdjustmentButton);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(form, BorderLayout.NORTH);
        panel.add(
                new JScrollPane(adjustmentTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createPaymentsTab() {
        paymentModel = new DefaultTableModel(
                new String[]{
                    "Payment ID", "Date", "Amount", "Method",
                    "Receipt", "Term", "Allocated",
                    "Unallocated"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTable = createTable(paymentModel);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel note = new JLabel(
                "Only payments with an unallocated balance can be "
                + "allocated to fee charges."
        );
        note.setFont(AppSupport.font(Font.PLAIN, 12));
        note.setBorder(new EmptyBorder(8, 8, 0, 8));

        panel.add(note, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAllocationsTab() {
        allocationModel = new DefaultTableModel(
                new String[]{
                    "Allocation ID", "Payment ID", "Charge ID",
                    "Charge Type", "Amount Allocated"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        allocationTable = createTable(allocationModel);

        allocatePaymentButton = new JButton(
                "Allocate Selected Payment"
        );
        allocatePaymentButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );
        allocatePaymentButton.addActionListener(
                event -> showAllocatePaymentDialog()
        );

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        actions.setOpaque(false);
        actions.add(allocatePaymentButton);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(actions, BorderLayout.NORTH);
        panel.add(
                new JScrollPane(allocationTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createBalanceTab() {
        balanceModel = new DefaultTableModel(
                new String[]{
                    "Currency", "Term", "Charges", "Adjustments",
                    "Allocated Payments", "Outstanding Balance"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        balanceTable = createTable(balanceModel);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel note = new JLabel(
                "Balances are grouped by currency and term. "
                + "A positive balance means money is still owed."
        );
        note.setFont(AppSupport.font(Font.PLAIN, 12));
        note.setBorder(new EmptyBorder(8, 8, 0, 8));

        panel.add(note, BorderLayout.NORTH);
        panel.add(new JScrollPane(balanceTable), BorderLayout.CENTER);

        return panel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(AppSupport.font(Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(
                AppSupport.font(Font.BOLD, 14)
        );
        table.getTableHeader().setBackground(DARK_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private JTextField createTextField(int width) {
        JTextField field = new JTextField();
        field.setFont(AppSupport.font(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(width, 32));

        return field;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel user = new JLabel(
                "Bursar: " + bursarUsername
        );
        user.setFont(AppSupport.font(Font.BOLD, 14));
        user.setForeground(DARK_BLUE);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(AppSupport.font(Font.PLAIN, 13));

        footer.add(user, BorderLayout.NORTH);
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
                     PreparedStatement ps =
                             con.prepareStatement(sql);
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

                    loadSelectedStudentData();

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Students could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadYears() {
        yearSelector.removeAllItems();

        SwingWorker<List<YearItem>, Void> worker =
                new SwingWorker<List<YearItem>, Void>() {

            @Override
            protected List<YearItem> doInBackground()
                    throws Exception {

                List<YearItem> years = new ArrayList<>();

                String sql = """
                    SELECT
                        academic_year_id,
                        academic_year
                    FROM academic_years
                    ORDER BY academic_year DESC
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        years.add(new YearItem(
                                rs.getInt("academic_year_id"),
                                rs.getString("academic_year")
                        ));
                    }
                }

                return years;
            }

            @Override
            protected void done() {
                try {
                    for (YearItem year : get()) {
                        yearSelector.addItem(year);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Academic years could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadSelectedStudentData() {
        StudentItem student =
                (StudentItem) studentSelector.getSelectedItem();

        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        String term = String.valueOf(
                termSelector.getSelectedItem()
        );

        chargeModel.setRowCount(0);
        adjustmentModel.setRowCount(0);
        paymentModel.setRowCount(0);
        allocationModel.setRowCount(0);
        balanceModel.setRowCount(0);

        if (student == null || year == null) {
            return;
        }

        loadCharges(student.id, year.id, term);
        loadAdjustments(student.id, year.id, term);
        loadPayments(student.id, year.id);
        loadAllocations(student.id, year.id);
        loadBalance(student.id, year.id);
    }

    private void loadCharges(
            int studentId,
            int yearId,
            String term
    ) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        charge_id,
                        term,
                        charge_type,
                        amount,
                        currency_code,
                        due_date
                    FROM fee_charges
                    WHERE student_id = ?
                      AND academic_year_id = ?
                      AND term = ?
                    ORDER BY charge_id
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, yearId);
                    ps.setString(3, term);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("charge_id"),
                                rs.getString("term"),
                                rs.getString("charge_type"),
                                rs.getBigDecimal("amount"),
                                rs.getString("currency_code"),
                                rs.getDate("due_date")
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        chargeModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Fee charges could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadAdjustments(
            int studentId,
            int yearId,
            String term
    ) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        adjustment_id,
                        term,
                        adjustment_type,
                        amount,
                        currency_code,
                        remarks
                    FROM fee_adjustments
                    WHERE student_id = ?
                      AND academic_year_id = ?
                      AND term = ?
                    ORDER BY adjustment_id
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, yearId);
                    ps.setString(3, term);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("adjustment_id"),
                                rs.getString("term"),
                                rs.getString("adjustment_type"),
                                rs.getBigDecimal("amount"),
                                rs.getString("currency_code"),
                                rs.getString("remarks")
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        adjustmentModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Fee adjustments could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadPayments(
            int studentId,
            int yearId
    ) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        p.payment_id,
                        p.payment_date,
                        p.amount,
                        p.payment_method,
                        p.receipt_number,
                        p.term,
                        COALESCE(SUM(pa.amount_allocated), 0)
                            AS allocated,
                        p.amount - COALESCE(
                            SUM(pa.amount_allocated), 0
                        ) AS unallocated
                    FROM payments p
                    JOIN academic_years ay
                        ON p.payment_date
                           BETWEEN ay.start_date AND ay.end_date
                    LEFT JOIN payment_allocations pa
                        ON pa.payment_id = p.payment_id
                    WHERE p.student_id = ?
                      AND ay.academic_year_id = ?
                    GROUP BY
                        p.payment_id,
                        p.payment_date,
                        p.amount,
                        p.payment_method,
                        p.receipt_number,
                        p.term
                    ORDER BY p.payment_date DESC,
                             p.payment_id DESC
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, yearId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("payment_id"),
                                rs.getDate("payment_date"),
                                rs.getBigDecimal("amount"),
                                rs.getString("payment_method"),
                                rs.getString("receipt_number"),
                                rs.getString("term"),
                                rs.getBigDecimal("allocated"),
                                rs.getBigDecimal("unallocated")
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        paymentModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Payments could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadAllocations(
            int studentId,
            int yearId
    ) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        pa.allocation_id,
                        pa.payment_id,
                        pa.charge_id,
                        fc.charge_type,
                        pa.amount_allocated
                    FROM payment_allocations pa
                    JOIN fee_charges fc
                        ON fc.charge_id = pa.charge_id
                    WHERE fc.student_id = ?
                      AND fc.academic_year_id = ?
                    ORDER BY pa.allocation_id DESC
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, yearId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("allocation_id"),
                                rs.getInt("payment_id"),
                                rs.getInt("charge_id"),
                                rs.getString("charge_type"),
                                rs.getBigDecimal(
                                        "amount_allocated"
                                )
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        allocationModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Payment allocations could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadBalance(
            int studentId,
            int yearId
    ) {
        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        fc.currency_code,
                        fc.term,
                        SUM(fc.amount) AS total_charges,
                        COALESCE(SUM(fa.amount), 0)
                            AS total_adjustments,
                        COALESCE(SUM(pa.amount_allocated), 0)
                            AS total_paid,
                        ROUND(
                            SUM(fc.amount)
                            - COALESCE(SUM(fa.amount), 0)
                            - COALESCE(SUM(pa.amount_allocated), 0),
                            2
                        ) AS outstanding_balance
                    FROM fee_charges fc
                    LEFT JOIN fee_adjustments fa
                        ON fa.student_id = fc.student_id
                       AND fa.academic_year_id =
                           fc.academic_year_id
                       AND fa.term = fc.term
                       AND fa.currency_code = fc.currency_code
                    LEFT JOIN payment_allocations pa
                        ON pa.charge_id = fc.charge_id
                    WHERE fc.student_id = ?
                      AND fc.academic_year_id = ?
                    GROUP BY
                        fc.currency_code,
                        fc.term
                    ORDER BY
                        fc.currency_code,
                        FIELD(
                            fc.term,
                            'Term 1', 'Term 2', 'Term 3'
                        )
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, yearId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getString("currency_code"),
                                rs.getString("term"),
                                rs.getBigDecimal("total_charges"),
                                rs.getBigDecimal(
                                        "total_adjustments"
                                ),
                                rs.getBigDecimal("total_paid"),
                                rs.getBigDecimal(
                                        "outstanding_balance"
                                )
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] row : get()) {
                        balanceModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "Student balances could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void addFeeCharge() {
        StudentItem student =
                (StudentItem) studentSelector.getSelectedItem();

        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        String term = String.valueOf(
                termSelector.getSelectedItem()
        );

        String chargeType =
                chargeTypeField.getText().trim();

        String amountText =
                chargeAmountField.getText().trim();

        String currency =
                currencyField.getText().trim();

        String dueDate =
                dueDateField.getText().trim();

        if (student == null || year == null) {
            showWarning(
                    "Select a student and academic year."
            );
            return;
        }

        if (chargeType.isEmpty() || amountText.isEmpty()
                || currency.isEmpty()) {
            showWarning(
                    "Charge type, amount, and currency are required."
            );
            return;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountText);
        } catch (Exception error) {
            showWarning("Enter a valid charge amount.");
            return;
        }

        if (amount.signum() < 0) {
            showWarning("The charge amount cannot be negative.");
            return;
        }

        if (currency.length() != 3) {
            showWarning("Currency must be a 3-letter code.");
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO fee_charges (
                        student_id,
                        academic_year_id,
                        term,
                        charge_type,
                        amount,
                        currency_code,
                        due_date
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, student.id);
                    ps.setInt(2, year.id);
                    ps.setString(3, term);
                    ps.setString(4, chargeType);
                    ps.setBigDecimal(5, amount);
                    ps.setString(6, currency.toUpperCase());

                    if (dueDate.isEmpty()) {
                        ps.setNull(7, java.sql.Types.DATE);
                    } else {
                        ps.setDate(
                                7,
                                java.sql.Date.valueOf(dueDate)
                        );
                    }

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Fee charge added: "
                                + chargeType);

                        chargeTypeField.setText("");
                        chargeAmountField.setText("");
                        dueDateField.setText("");

                        loadCharges(
                                student.id,
                                year.id,
                                term
                        );
                        loadBalance(student.id, year.id);

                    } else {
                        showWarning(
                                "The fee charge could not be added."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "The fee charge could not be added.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void addAdjustment() {
        StudentItem student =
                (StudentItem) studentSelector.getSelectedItem();

        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        String term = String.valueOf(
                termSelector.getSelectedItem()
        );

        String type = String.valueOf(
                adjustmentTypeSelector.getSelectedItem()
        );

        String amountText =
                adjustmentAmountField.getText().trim();

        String remarks =
                adjustmentRemarksField.getText().trim();

        if (student == null || year == null) {
            showWarning(
                    "Select a student and academic year."
            );
            return;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountText);
        } catch (Exception error) {
            showWarning("Enter a valid adjustment amount.");
            return;
        }

        if (amount.signum() < 0) {
            showWarning(
                    "The adjustment amount cannot be negative."
            );
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO fee_adjustments (
                        student_id,
                        academic_year_id,
                        term,
                        adjustment_type,
                        amount,
                        currency_code,
                        remarks
                    )
                    VALUES (?, ?, ?, ?, ?, 'RWF', ?)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, student.id);
                    ps.setInt(2, year.id);
                    ps.setString(3, term);
                    ps.setString(4, type);
                    ps.setBigDecimal(5, amount);
                    ps.setString(6, remarks.isEmpty()
                            ? null : remarks);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Adjustment added: " + type);

                        adjustmentAmountField.setText("");
                        adjustmentRemarksField.setText("");

                        loadAdjustments(
                                student.id,
                                year.id,
                                term
                        );
                        loadBalance(student.id, year.id);

                    } else {
                        showWarning(
                                "The adjustment could not be added."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "The adjustment could not be added.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void showAllocatePaymentDialog() {
        StudentItem student =
                (StudentItem) studentSelector.getSelectedItem();

        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        if (student == null || year == null) {
            showWarning(
                    "Select a student and academic year."
            );
            return;
        }

        int paymentRow = paymentTable.getSelectedRow();

        if (paymentRow < 0) {
            showWarning(
                    "Select a payment in the Payments tab first."
            );
            return;
        }

        int paymentModelRow = paymentTable
                .convertRowIndexToModel(paymentRow);

        int paymentId = (int) paymentModel
                .getValueAt(paymentModelRow, 0);

        BigDecimal unallocated = (BigDecimal) paymentModel
                .getValueAt(paymentModelRow, 7);

        if (unallocated == null
                || unallocated.signum() <= 0) {
            showWarning(
                    "The selected payment has no unallocated amount."
            );
            return;
        }

        int chargeRow = chargeTable.getSelectedRow();

        if (chargeRow < 0) {
            showWarning(
                    "Select a fee charge in the Fee Charges tab first."
            );
            return;
        }

        int chargeModelRow = chargeTable
                .convertRowIndexToModel(chargeRow);

        int chargeId = (int) chargeModel
                .getValueAt(chargeModelRow, 0);

        String chargeType = String.valueOf(
                chargeModel.getValueAt(chargeModelRow, 2)
        );

        String amountText = JOptionPane.showInputDialog(
                this,
                "Allocate amount to " + chargeType
                        + "\nAvailable unallocated: "
                        + unallocated,
                unallocated.toPlainString()
        );

        if (amountText == null) {
            return;
        }

        BigDecimal allocation;

        try {
            allocation = new BigDecimal(amountText.trim());
        } catch (Exception error) {
            showWarning("Enter a valid allocation amount.");
            return;
        }

        if (allocation.signum() <= 0) {
            showWarning(
                    "The allocation must be greater than zero."
            );
            return;
        }

        if (allocation.compareTo(unallocated) > 0) {
            showWarning(
                    "The allocation cannot exceed the payment's "
                    + "unallocated amount."
            );
            return;
        }

        allocatePayment(
                student.id,
                year.id,
                paymentId,
                chargeId,
                allocation
        );
    }

    private void allocatePayment(
            int studentId,
            int yearId,
            int paymentId,
            int chargeId,
            BigDecimal amount
    ) {
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO payment_allocations (
                        payment_id,
                        charge_id,
                        amount_allocated
                    )
                    VALUES (?, ?, ?)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, paymentId);
                    ps.setInt(2, chargeId);
                    ps.setBigDecimal(3, amount);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Payment allocated successfully.");

                        loadSelectedStudentData();

                    } else {
                        showWarning(
                                "The allocation could not be created."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            BursarFeeManagementPanel.this,
                            "The payment allocation could not "
                                    + "be created.",
                            error
                    );
                }
            }
        };

        worker.execute();
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
}