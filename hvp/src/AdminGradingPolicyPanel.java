import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Font;
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

public class AdminGradingPolicyPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color BACKGROUND = new Color(243, 246, 251);

    private final String adminUsername;

    private JComboBox<PolicyItem> policySelector;
    private JComboBox<YearItem> yearSelector;
    private JTextField policyNameField;
    private JComboBox<String> missingRuleSelector;
    private JCheckBox activeCheckBox;

    private DefaultTableModel weightModel;
    private JTable weightTable;

    private DefaultTableModel bandModel;
    private JTable bandTable;

    private JButton refreshButton;
    private JButton createPolicyButton;
    private JButton savePolicyButton;
    private JButton addWeightButton;
    private JButton deleteWeightButton;
    private JButton addBandButton;
    private JButton deleteBandButton;

    private JLabel statusLabel;
    private JLabel weightTotalLabel;

    private boolean updatingPolicySelector;

    public AdminGradingPolicyPanel(String adminUsername) {
        this.adminUsername = adminUsername;

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainArea(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadYears();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JLabel title = new JLabel("Grading Policy Management");
        title.setFont(AppSupport.font(Font.BOLD, 26));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Maintain assessment weights and official grade bands"
        );
        subtitle.setFont(AppSupport.font(Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);

        policySelector = new JComboBox<>();
        policySelector.setFont(AppSupport.font(Font.PLAIN, 14));
        policySelector.setPreferredSize(new Dimension(320, 32));
        policySelector.addActionListener(event -> {
            if (!updatingPolicySelector) {
                loadSelectedPolicy();
            }
        });

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(AppSupport.font(Font.BOLD, 13));
        refreshButton.addActionListener(event -> loadPolicies());

        controls.add(new JLabel("Policy:"));
        controls.add(policySelector);
        controls.add(refreshButton);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        header.add(controls, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createMainArea() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setOpaque(false);

        main.add(createPolicyForm(), BorderLayout.NORTH);
        main.add(createTablesArea(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createPolicyForm() {
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Policy Details");
        heading.setFont(AppSupport.font(Font.BOLD, 18));
        heading.setForeground(DARK_BLUE);

        JPanel fields = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0)
        );
        fields.setOpaque(false);

        yearSelector = new JComboBox<>();
        yearSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        yearSelector.setPreferredSize(new Dimension(220, 32));

        policyNameField = new JTextField();
        policyNameField.setFont(AppSupport.font(Font.PLAIN, 14));
        policyNameField.setPreferredSize(new Dimension(260, 32));

        missingRuleSelector = new JComboBox<>(new String[]{
            "EXCLUDE", "ZERO", "REQUIRE_MAKEUP"
        });
        missingRuleSelector.setFont(AppSupport.font(Font.PLAIN, 14));
        missingRuleSelector.setPreferredSize(new Dimension(210, 32));

        activeCheckBox = new JCheckBox("Active policy", true);
        activeCheckBox.setFont(AppSupport.font(Font.PLAIN, 14));

        createPolicyButton = new JButton("Create Policy");
        savePolicyButton = new JButton("Save Changes");

        for (JButton button : new JButton[]{
                createPolicyButton, savePolicyButton}) {
            button.setFont(AppSupport.font(Font.BOLD, 13));
            button.setFocusPainted(false);
        }

        createPolicyButton.addActionListener(
                event -> createPolicy()
        );

        savePolicyButton.addActionListener(
                event -> savePolicyChanges()
        );

        fields.add(new JLabel("Academic year:"));
        fields.add(yearSelector);
        fields.add(new JLabel("Policy name:"));
        fields.add(policyNameField);
        fields.add(new JLabel("Missing assessment rule:"));
        fields.add(missingRuleSelector);
        fields.add(activeCheckBox);
        fields.add(createPolicyButton);
        fields.add(savePolicyButton);

        form.add(heading, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);

        return form;
    }

    private JPanel createTablesArea() {
        JPanel area = new JPanel(new GridLayout(1, 2, 15, 15));
        area.setOpaque(false);

        area.add(createWeightsPanel());
        area.add(createBandsPanel());

        return area;
    }

    private JPanel createWeightsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Assessment Weights");
        heading.setFont(AppSupport.font(Font.BOLD, 18));
        heading.setForeground(DARK_BLUE);

        weightModel = new DefaultTableModel(
                new String[]{
                    "Weight ID", "Assessment Type", "Weight Percent"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        weightTable = new JTable(weightModel);
        configureTable(weightTable);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        actions.setOpaque(false);

        addWeightButton = new JButton("Add / Update Weight");
        deleteWeightButton = new JButton("Delete Weight");

        for (JButton button : new JButton[]{
                addWeightButton, deleteWeightButton}) {
            button.setFont(AppSupport.font(Font.BOLD, 13));
            button.setFocusPainted(false);
        }

        addWeightButton.addActionListener(
                event -> showAddWeightDialog()
        );

        deleteWeightButton.addActionListener(
                event -> deleteSelectedWeight()
        );

        weightTotalLabel = new JLabel("Total weight: 0.00%");
        weightTotalLabel.setFont(AppSupport.font(Font.BOLD, 14));
        weightTotalLabel.setForeground(DARK_BLUE);

        actions.add(addWeightButton);
        actions.add(deleteWeightButton);
        actions.add(weightTotalLabel);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        panel.add(
                new JScrollPane(weightTable),
                BorderLayout.SOUTH
        );

        weightTable.setPreferredScrollableViewportSize(
                new Dimension(500, 260)
        );

        return panel;
    }

    private JPanel createBandsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Official Grade Bands");
        heading.setFont(AppSupport.font(Font.BOLD, 18));
        heading.setForeground(DARK_BLUE);

        bandModel = new DefaultTableModel(
                new String[]{
                    "Band ID", "Grade", "Minimum Percent",
                    "Maximum Percent"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bandTable = new JTable(bandModel);
        configureTable(bandTable);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        actions.setOpaque(false);

        addBandButton = new JButton("Add / Update Band");
        deleteBandButton = new JButton("Delete Band");

        for (JButton button : new JButton[]{
                addBandButton, deleteBandButton}) {
            button.setFont(AppSupport.font(Font.BOLD, 13));
            button.setFocusPainted(false);
        }

        addBandButton.addActionListener(
                event -> showAddBandDialog()
        );

        deleteBandButton.addActionListener(
                event -> deleteSelectedBand()
        );

        actions.add(addBandButton);
        actions.add(deleteBandButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        panel.add(
                new JScrollPane(bandTable),
                BorderLayout.SOUTH
        );

        bandTable.setPreferredScrollableViewportSize(
                new Dimension(500, 260)
        );

        return panel;
    }

    private void configureTable(JTable table) {
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
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel admin = new JLabel(
                "Administrator: " + adminUsername
        );
        admin.setFont(AppSupport.font(Font.BOLD, 14));
        admin.setForeground(DARK_BLUE);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(AppSupport.font(Font.PLAIN, 13));

        footer.add(admin, BorderLayout.NORTH);
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

                    loadPolicies();

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "Academic years could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadPolicies() {
        updatingPolicySelector = true;

        try {
            policySelector.removeAllItems();
        } finally {
            updatingPolicySelector = false;
        }

        SwingWorker<List<PolicyItem>, Void> worker =
                new SwingWorker<List<PolicyItem>, Void>() {

            @Override
            protected List<PolicyItem> doInBackground()
                    throws Exception {

                List<PolicyItem> policies = new ArrayList<>();

                String sql = """
                    SELECT
                        policy_id,
                        policy_name,
                        academic_year_id,
                        missing_assessment_rule,
                        is_active
                    FROM grading_policies
                    ORDER BY academic_year_id DESC,
                             policy_name
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        policies.add(new PolicyItem(
                                rs.getInt("policy_id"),
                                rs.getString("policy_name"),
                                rs.getInt("academic_year_id"),
                                rs.getString(
                                        "missing_assessment_rule"
                                ),
                                rs.getBoolean("is_active")
                        ));
                    }
                }

                return policies;
            }

            @Override
            protected void done() {
                try {
                    List<PolicyItem> policies = get();

                    updatingPolicySelector = true;

                    try {
                        for (PolicyItem policy : policies) {
                            policySelector.addItem(policy);
                        }

                        if (!policies.isEmpty()) {
                            policySelector.setSelectedIndex(0);
                        }

                    } finally {
                        updatingPolicySelector = false;
                    }

                    if (policies.isEmpty()) {
                        clearPolicyDetails();
                        setStatus(
                                "No grading policies found."
                        );
                    } else {
                        loadSelectedPolicy();
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "Grading policies could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadSelectedPolicy() {
        PolicyItem policy =
                (PolicyItem) policySelector.getSelectedItem();

        clearPolicyDetails();

        if (policy == null) {
            return;
        }

        policyNameField.setText(policy.policyName);
        missingRuleSelector.setSelectedItem(
                policy.missingRule
        );
        activeCheckBox.setSelected(policy.active);

        selectYear(policy.academicYearId);

        loadWeights(policy.id);
        loadBands(policy.id);
    }

    private void selectYear(int academicYearId) {
        for (int i = 0; i < yearSelector.getItemCount(); i++) {
            YearItem year = yearSelector.getItemAt(i);

            if (year.id == academicYearId) {
                yearSelector.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearPolicyDetails() {
        policyNameField.setText("");
        missingRuleSelector.setSelectedIndex(0);
        activeCheckBox.setSelected(true);

        weightModel.setRowCount(0);
        bandModel.setRowCount(0);
        weightTotalLabel.setText("Total weight: 0.00%");
    }

    private void loadWeights(int policyId) {
        weightModel.setRowCount(0);

        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        weight_id,
                        assessment_type,
                        weight_percent
                    FROM assessment_weights
                    WHERE policy_id = ?
                    ORDER BY FIELD(
                        assessment_type,
                        'Test', 'Exam',
                        'Assignment', 'Project'
                    )
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, policyId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("weight_id"),
                                rs.getString("assessment_type"),
                                rs.getBigDecimal("weight_percent")
                            });
                        }
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    BigDecimal total = BigDecimal.ZERO;

                    for (Object[] row : get()) {
                        weightModel.addRow(row);

                        total = total.add(
                                (BigDecimal) row[2]
                        );
                    }

                    weightTotalLabel.setText(
                            "Total weight: "
                                    + total.setScale(2)
                                    + "%"
                    );

                    if (total.compareTo(
                            BigDecimal.valueOf(100)) != 0) {
                        weightTotalLabel.setForeground(
                                new Color(180, 40, 40)
                        );
                    } else {
                        weightTotalLabel.setForeground(DARK_BLUE);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "Assessment weights could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void loadBands(int policyId) {
        bandModel.setRowCount(0);

        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {

                List<Object[]> rows = new ArrayList<>();

                String sql = """
                    SELECT
                        band_id,
                        grade_label,
                        minimum_percent,
                        maximum_percent
                    FROM grade_bands
                    WHERE policy_id = ?
                    ORDER BY minimum_percent DESC
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, policyId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("band_id"),
                                rs.getString("grade_label"),
                                rs.getBigDecimal(
                                        "minimum_percent"
                                ),
                                rs.getBigDecimal(
                                        "maximum_percent"
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
                        bandModel.addRow(row);
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "Grade bands could not be loaded.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void createPolicy() {
        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        String policyName =
                policyNameField.getText().trim();

        String missingRule = String.valueOf(
                missingRuleSelector.getSelectedItem()
        );

        boolean active = activeCheckBox.isSelected();

        if (year == null || policyName.isEmpty()) {
            showWarning(
                    "Select an academic year and enter a policy name."
            );
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO grading_policies (
                        academic_year_id,
                        policy_name,
                        missing_assessment_rule,
                        is_active
                    )
                    VALUES (?, ?, ?, ?)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, year.id);
                    ps.setString(2, policyName);
                    ps.setString(3, missingRule);
                    ps.setBoolean(4, active);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Policy created: "
                                + policyName);

                        loadPolicies();

                    } else {
                        showWarning(
                                "The policy could not be created."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The grading policy could not be created.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void savePolicyChanges() {
        PolicyItem policy =
                (PolicyItem) policySelector.getSelectedItem();

        if (policy == null) {
            showWarning("Select a grading policy first.");
            return;
        }

        YearItem year =
                (YearItem) yearSelector.getSelectedItem();

        String policyName =
                policyNameField.getText().trim();

        String missingRule = String.valueOf(
                missingRuleSelector.getSelectedItem()
        );

        boolean active = activeCheckBox.isSelected();

        if (year == null || policyName.isEmpty()) {
            showWarning(
                    "Select an academic year and enter a policy name."
            );
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    UPDATE grading_policies
                    SET academic_year_id = ?,
                        policy_name = ?,
                        missing_assessment_rule = ?,
                        is_active = ?
                    WHERE policy_id = ?
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, year.id);
                    ps.setString(2, policyName);
                    ps.setString(3, missingRule);
                    ps.setBoolean(4, active);
                    ps.setInt(5, policy.id);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Policy updated: "
                                + policyName);

                        loadPolicies();

                    } else {
                        showWarning(
                                "The policy could not be updated."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The grading policy could not be updated.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void showAddWeightDialog() {
        PolicyItem policy =
                (PolicyItem) policySelector.getSelectedItem();

        if (policy == null) {
            showWarning("Select a grading policy first.");
            return;
        }

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{
                    "Test", "Exam", "Assignment", "Project"
                }
        );

        JTextField weightField = new JTextField();

        int result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                    "Assessment type:", typeBox,
                    "Weight percent:", weightField
                },
                "Add or Update Assessment Weight",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String assessmentType =
                String.valueOf(typeBox.getSelectedItem());

        String weightText = weightField.getText().trim();

        BigDecimal weight;

        try {
            weight = new BigDecimal(weightText);
        } catch (Exception error) {
            showWarning("Enter a valid weight percentage.");
            return;
        }

        if (weight.signum() <= 0
                || weight.compareTo(
                        BigDecimal.valueOf(100)) > 0) {
            showWarning(
                    "Weight must be greater than zero "
                    + "and no more than 100."
            );
            return;
        }

        saveWeight(
                policy.id,
                assessmentType,
                weight
        );
    }

    private void saveWeight(
            int policyId,
            String assessmentType,
            BigDecimal weight
    ) {
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO assessment_weights (
                        policy_id,
                        assessment_type,
                        weight_percent
                    )
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        weight_percent = VALUES(weight_percent)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, policyId);
                    ps.setString(2, assessmentType);
                    ps.setBigDecimal(3, weight);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus(assessmentType
                                + " weight saved.");
                        loadWeights(policyId);

                    } else {
                        showWarning(
                                "The weight could not be saved."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The assessment weight could not "
                                    + "be saved.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void deleteSelectedWeight() {
        int row = weightTable.getSelectedRow();

        if (row < 0) {
            showWarning("Select an assessment weight first.");
            return;
        }

        int modelRow = weightTable
                .convertRowIndexToModel(row);

        int weightId = (int) weightModel
                .getValueAt(modelRow, 0);

        String assessmentType = String.valueOf(
                weightModel.getValueAt(modelRow, 1)
        );

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete the weight for " + assessmentType + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    DELETE FROM assessment_weights
                    WHERE weight_id = ?
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, weightId);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus(assessmentType
                                + " weight deleted.");

                        PolicyItem policy =
                                (PolicyItem) policySelector
                                        .getSelectedItem();

                        if (policy != null) {
                            loadWeights(policy.id);
                        }

                    } else {
                        showWarning(
                                "The weight could not be deleted."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The assessment weight could not "
                                    + "be deleted.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void showAddBandDialog() {
        PolicyItem policy =
                (PolicyItem) policySelector.getSelectedItem();

        if (policy == null) {
            showWarning("Select a grading policy first.");
            return;
        }

        JTextField labelField = new JTextField();
        JTextField minimumField = new JTextField();
        JTextField maximumField = new JTextField();

        int result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                    "Grade label:", labelField,
                    "Minimum percent:", minimumField,
                    "Maximum percent:", maximumField
                },
                "Add or Update Grade Band",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String label = labelField.getText().trim();
        String minimumText = minimumField.getText().trim();
        String maximumText = maximumField.getText().trim();

        if (label.isEmpty()) {
            showWarning("Enter a grade label.");
            return;
        }

        BigDecimal minimum;
        BigDecimal maximum;

        try {
            minimum = new BigDecimal(minimumText);
            maximum = new BigDecimal(maximumText);
        } catch (Exception error) {
            showWarning(
                    "Enter valid minimum and maximum percentages."
            );
            return;
        }

        if (minimum.signum() < 0
                || maximum.compareTo(minimum) < 0
                || maximum.compareTo(
                        BigDecimal.valueOf(100)) > 0) {
            showWarning(
                    "The band range must be valid and cannot "
                    + "exceed 100 percent."
            );
            return;
        }

        saveBand(policy.id, label, minimum, maximum);
    }

    private void saveBand(
            int policyId,
            String label,
            BigDecimal minimum,
            BigDecimal maximum
    ) {
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    INSERT INTO grade_bands (
                        policy_id,
                        grade_label,
                        minimum_percent,
                        maximum_percent
                    )
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        minimum_percent =
                            VALUES(minimum_percent),
                        maximum_percent =
                            VALUES(maximum_percent)
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, policyId);
                    ps.setString(2, label);
                    ps.setBigDecimal(3, minimum);
                    ps.setBigDecimal(4, maximum);

                    ps.executeUpdate();
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Grade band saved: " + label);
                        loadBands(policyId);

                    } else {
                        showWarning(
                                "The grade band could not be saved."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The grade band could not be saved.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private void deleteSelectedBand() {
        int row = bandTable.getSelectedRow();

        if (row < 0) {
            showWarning("Select a grade band first.");
            return;
        }

        int modelRow = bandTable
                .convertRowIndexToModel(row);

        int bandId = (int) bandModel
                .getValueAt(modelRow, 0);

        String label = String.valueOf(
                bandModel.getValueAt(modelRow, 1)
        );

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete grade band " + label + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                String sql = """
                    DELETE FROM grade_bands
                    WHERE band_id = ?
                    """;

                try (Connection con = Conn.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setInt(1, bandId);

                    return ps.executeUpdate() == 1;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus("Grade band deleted: "
                                + label);

                        PolicyItem policy =
                                (PolicyItem) policySelector
                                        .getSelectedItem();

                        if (policy != null) {
                            loadBands(policy.id);
                        }

                    } else {
                        showWarning(
                                "The grade band could not "
                                        + "be deleted."
                        );
                    }

                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException error) {
                    AppSupport.reportError(
                            AdminGradingPolicyPanel.this,
                            "The grade band could not be deleted.",
                            error
                    );
                }
            }
        };

        worker.execute();
    }

    private static class PolicyItem {
        final int id;
        final String policyName;
        final int academicYearId;
        final String missingRule;
        final boolean active;

        PolicyItem(
                int id,
                String policyName,
                int academicYearId,
                String missingRule,
                boolean active
        ) {
            this.id = id;
            this.policyName = policyName;
            this.academicYearId = academicYearId;
            this.missingRule = missingRule;
            this.active = active;
        }

        @Override
        public String toString() {
            return policyName + " - Year "
                    + academicYearId
                    + (active ? "" : " (Inactive)");
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