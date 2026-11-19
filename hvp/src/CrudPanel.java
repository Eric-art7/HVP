import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public abstract class CrudPanel extends JPanel {

    private final String title;
    private final String selectSql;
    private final String[] columnNames;
    private final String[] fieldLabels;
    private final String insertSql;
    private final String deleteSql;
    private final String searchSql;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField[] inputFields;
    private JTextField searchField;

    public CrudPanel(String title, String selectSql, String[] columnNames,
                     String[] fieldLabels, String insertSql,
                     String deleteSql, String searchSql) {

        this.title = title;
        this.selectSql = selectSql;
        this.columnNames = columnNames;
        this.fieldLabels = fieldLabels;
        this.insertSql = insertSql;
        this.deleteSql = deleteSql;
        this.searchSql = searchSql;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(243, 246, 251));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);

        loadData();
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);

        JLabel heading = new JLabel("📊 " + title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(new Color(15, 40, 80));

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(350, 40));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search...");

        JButton searchButton = new JButton("🔍 Search");
        searchButton.setBackground(new Color(0, 123, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchButton.addActionListener(e -> searchRecords());
        searchField.addActionListener(e -> searchRecords());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        panel.add(heading, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    // ================= TABLE =================

    private JPanel createTablePanel() {
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(15, 40, 80));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ================= ADD / DELETE =================

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(340, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);

        inputFields = new JTextField[fieldLabels.length];

        for (int i = 0; i < fieldLabels.length; i++) {
            gbc.gridy = i * 2;
            panel.add(new JLabel(fieldLabels[i]), gbc);

            gbc.gridy = i * 2 + 1;
            inputFields[i] = new JTextField();
            inputFields[i].setPreferredSize(new Dimension(0, 38));
            panel.add(inputFields[i], gbc);
        }

        gbc.gridy = fieldLabels.length * 2;
        gbc.insets = new Insets(15, 0, 8, 0);

        JButton addButton = new JButton("➕ Add Record");
        styleButton(addButton, new Color(0, 123, 255));
        addButton.addActionListener(e -> addRecord());
        panel.add(addButton, gbc);

        gbc.gridy = fieldLabels.length * 2 + 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        JButton deleteButton = new JButton("🗑️ Delete Selected");
        styleButton(deleteButton, new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deleteSelectedRecord());
        panel.add(deleteButton, gbc);

        gbc.gridy = fieldLabels.length * 2 + 2;
        gbc.insets = new Insets(8, 0, 0, 0);

        JButton refreshButton = new JButton("🔄 Refresh");
        styleButton(refreshButton, new Color(15, 40, 80));
        refreshButton.addActionListener(e -> loadData());
        panel.add(refreshButton, gbc);

        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(0, 42));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ================= DATABASE OPERATIONS =================

    private void loadData() {
        model.setRowCount(0);

        try (Connection con = Conn.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(selectSql)) {

            while (rs.next()) {
                Object[] row = new Object[columnNames.length];

                for (int i = 0; i < columnNames.length; i++) {
                    row[i] = rs.getObject(i + 1);
                }

                model.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load data: " + e.getMessage());
        }
    }

    private void searchRecords() {
        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }

    private void addRecord() {
        for (JTextField field : inputFields) {
            if (field.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
        }

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {

            for (int i = 0; i < inputFields.length; i++) {
                ps.setString(i + 1, inputFields[i].getText().trim());
            }

            ps.executeUpdate();

            for (JTextField field : inputFields) {
                field.setText("");
            }

            loadData();

            JOptionPane.showMessageDialog(this, "Record added successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not add record: " + e.getMessage());
        }
    }

    private void deleteSelectedRecord() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object idValue = model.getValueAt(modelRow, 0);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete record ID " + idValue + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {

            ps.setObject(1, idValue);
            ps.executeUpdate();

            loadData();

            JOptionPane.showMessageDialog(this, "Record deleted successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete record: " + e.getMessage());
        }
    }
}