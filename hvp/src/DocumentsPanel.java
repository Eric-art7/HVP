import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class DocumentsPanel extends JPanel {


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

    private static final Color WARNING_ORANGE =
            new Color(255, 159, 64);

    private static final Color GRAY_BUTTON =
            new Color(108, 117, 125);

    private static final Color WHITE =
            Color.WHITE;


    private static final long MAX_FILE_SIZE =
            20 * 1024 * 1024;


    private static final String DOCUMENT_STORAGE_FOLDER =
            "documents";


    private final String username;


    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> categorySelector;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> uploaderFilter;


    private JLabel selectedFileLabel;
    private File selectedFile;


    private JTable documentTable;
    private DefaultTableModel documentModel;
    private TableRowSorter<DefaultTableModel> sorter;


    private JButton chooseFileButton;
    private JButton uploadButton;
    private JButton openButton;
    private JButton downloadButton;
    private JButton editButton;
    private JButton deactivateButton;
    private JButton deleteButton;
    private JButton refreshButton;


    private JLabel statusLabel;
    private JLabel countLabel;


    public DocumentsPanel(String username) {

        this.username = username;


        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );


        createStorageFolder();


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


        loadDocuments();
    }


    // ================= STORAGE FOLDER =================


    private void createStorageFolder() {

        File folder = new File(
                DOCUMENT_STORAGE_FOLDER
        );


        if (!folder.exists()) {

            boolean created = folder.mkdirs();


            if (created) {

                System.out.println(
                        "Created document storage folder: "
                        + folder.getAbsolutePath()
                );
            }
        }
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


        JLabel icon = new JLabel("📁");

        icon.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        28
                )
        );


        JLabel title = new JLabel(
                "School Documents"
        );

        title.setFont(
                AppSupport.font(Font.BOLD, 26)
        );

        title.setForeground(DARK_BLUE);


        titlePanel.add(icon);
        titlePanel.add(title);


        JLabel subtitle = new JLabel(
                "Upload, search, preview, download, edit, and manage school documents."
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

        refreshButton.addActionListener(
                event -> loadDocuments()
        );


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
                createUploadPanel(),
                BorderLayout.NORTH
        );

        mainContent.add(
                createDocumentsPanel(),
                BorderLayout.CENTER
        );


        return mainContent;
    }


    // ================= UPLOAD PANEL =================


    private JPanel createUploadPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setBackground(WHITE);
        panel.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );


        JPanel fields = new JPanel(
                new BorderLayout(15, 15)
        );

        fields.setOpaque(false);


        JPanel topFields = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        topFields.setOpaque(false);


        categorySelector = new JComboBox<>(
                new String[]{
                    "Policies",
                    "Reports",
                    "Circulars",
                    "Forms",
                    "Certificates",
                    "Examinations",
                    "Finance",
                    "Academic",
                    "Other"
                }
        );

        categorySelector.setFont(
                AppSupport.font(Font.PLAIN, 15)
        );

        categorySelector.setPreferredSize(
                new Dimension(220, 42)
        );


        chooseFileButton = new JButton(
                "Choose File"
        );

        chooseFileButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        chooseFileButton.setBackground(GRAY_BUTTON);
        chooseFileButton.setForeground(WHITE);
        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setPreferredSize(
                new Dimension(150, 42)
        );

        chooseFileButton.addActionListener(
                event -> chooseFile()
        );


        selectedFileLabel = new JLabel(
                "No file selected."
        );

        selectedFileLabel.setFont(
                AppSupport.font(Font.PLAIN, 13)
        );

        selectedFileLabel.setForeground(
                new Color(100, 115, 135)
        );


        topFields.add(
                labeledField(
                        "Category",
                        categorySelector
                )
        );

        topFields.add(
                labeledField(
                        "Document File",
                        chooseFileButton
                )
        );

        topFields.add(
                selectedFileLabel
        );


        titleField = createTextField();

        titleField.setPreferredSize(
                new Dimension(0, 42)
        );


        descriptionArea = new JTextArea(3, 20);

        descriptionArea.setFont(
                AppSupport.font(Font.PLAIN, 15)
        );

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(
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


        JPanel centerFields = new JPanel(
                new BorderLayout(0, 12)
        );

        centerFields.setOpaque(false);

        centerFields.add(
                labeledField(
                        "Document Title",
                        titleField
                ),
                BorderLayout.NORTH
        );

        centerFields.add(
                labeledField(
                        "Description",
                        new JScrollPane(descriptionArea)
                ),
                BorderLayout.CENTER
        );


        JPanel buttons = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        buttons.setOpaque(false);


        uploadButton = new JButton(
                "Upload Document"
        );

        uploadButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        uploadButton.setBackground(ACCENT_BLUE);
        uploadButton.setForeground(WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setPreferredSize(
                new Dimension(180, 42)
        );

        uploadButton.addActionListener(
                event -> uploadDocument()
        );


        buttons.add(uploadButton);


        fields.add(
                topFields,
                BorderLayout.NORTH
        );

        fields.add(
                centerFields,
                BorderLayout.CENTER
        );

        fields.add(
                buttons,
                BorderLayout.SOUTH
        );


        panel.add(
                fields,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= DOCUMENTS PANEL =================


    private JPanel createDocumentsPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 15)
        );

        panel.setOpaque(false);


        JPanel controls = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        controls.setOpaque(false);


        searchField = createTextField();

        searchField.setPreferredSize(
                new Dimension(280, 38)
        );


        searchField.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {


                            @Override
                            public void insertUpdate(
                                    javax.swing.event.DocumentEvent event
                            ) {
                                applyFilters();
                            }


                            @Override
                            public void removeUpdate(
                                    javax.swing.event.DocumentEvent event
                            ) {
                                applyFilters();
                            }


                            @Override
                            public void changedUpdate(
                                    javax.swing.event.DocumentEvent event
                            ) {
                                applyFilters();
                            }
                        }
                );


        categoryFilter = new JComboBox<>();

        categoryFilter.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        categoryFilter.setPreferredSize(
                new Dimension(200, 38)
        );

        categoryFilter.addActionListener(
                event -> applyFilters()
        );


        uploaderFilter = new JComboBox<>();

        uploaderFilter.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        uploaderFilter.setPreferredSize(
                new Dimension(220, 38)
        );

        uploaderFilter.addActionListener(
                event -> applyFilters()
        );


        controls.add(
                labeledField(
                        "Search",
                        searchField
                )
        );

        controls.add(
                labeledField(
                        "Category",
                        categoryFilter
                )
        );

        controls.add(
                labeledField(
                        "Uploaded By",
                        uploaderFilter
                )
        );


        documentModel = new DefaultTableModel(
                new String[]{
                    "Document ID",
                    "Title",
                    "Description",
                    "Category",
                    "File Path",
                    "File Name",
                    "Uploaded By",
                    "Uploaded At",
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


        documentTable = new JTable(documentModel);

        documentTable.setRowHeight(32);
        documentTable.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );

        documentTable.getTableHeader()
                .setFont(
                        AppSupport.font(
                                Font.BOLD,
                                14
                        )
                );


        sorter = new TableRowSorter<>(
                documentModel
        );

        documentTable.setRowSorter(sorter);


        JScrollPane tableScroll =
                new JScrollPane(documentTable);


        JPanel actionButtons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );

        actionButtons.setOpaque(false);


        openButton = new JButton(
                "Open / Preview"
        );

        openButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        openButton.setBackground(ACCENT_BLUE);
        openButton.setForeground(WHITE);
        openButton.setFocusPainted(false);
        openButton.setPreferredSize(
                new Dimension(170, 40)
        );

        openButton.addActionListener(
                event -> openSelectedDocument()
        );


        downloadButton = new JButton(
                "Download / Save As"
        );

        downloadButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        downloadButton.setBackground(SUCCESS_GREEN);
        downloadButton.setForeground(WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setPreferredSize(
                new Dimension(200, 40)
        );

        downloadButton.addActionListener(
                event -> downloadSelectedDocument()
        );


        editButton = new JButton("Edit Details");

        editButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        editButton.setBackground(DARK_BLUE);
        editButton.setForeground(WHITE);
        editButton.setFocusPainted(false);
        editButton.setPreferredSize(
                new Dimension(150, 40)
        );

        editButton.addActionListener(
                event -> editSelectedDocument()
        );


        deactivateButton = new JButton(
                "Deactivate"
        );

        deactivateButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        deactivateButton.setBackground(WARNING_ORANGE);
        deactivateButton.setForeground(WHITE);
        deactivateButton.setFocusPainted(false);
        deactivateButton.setPreferredSize(
                new Dimension(150, 40)
        );

        deactivateButton.addActionListener(
                event -> deactivateSelectedDocument()
        );


        deleteButton = new JButton(
                "Delete Permanently"
        );

        deleteButton.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        deleteButton.setBackground(DANGER_RED);
        deleteButton.setForeground(WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setPreferredSize(
                new Dimension(200, 40)
        );

        deleteButton.addActionListener(
                event -> deleteSelectedDocument()
        );


        actionButtons.add(openButton);
        actionButtons.add(downloadButton);
        actionButtons.add(editButton);
        actionButtons.add(deactivateButton);
        actionButtons.add(deleteButton);


        JPanel bottom = new JPanel(
                new BorderLayout(0, 10)
        );

        bottom.setOpaque(false);

        bottom.add(
                actionButtons,
                BorderLayout.NORTH
        );

        bottom.add(
                tableScroll,
                BorderLayout.CENTER
        );


        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                bottom,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ================= FOOTER =================


    private JPanel createFooter() {

        JPanel footer = new JPanel(
                new BorderLayout(15, 5)
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


        JLabel user = new JLabel(
                "Uploaded by: " + username
        );

        user.setFont(
                AppSupport.font(Font.BOLD, 14)
        );

        user.setForeground(DARK_BLUE);


        statusLabel = new JLabel("Ready.");

        statusLabel.setFont(
                AppSupport.font(Font.PLAIN, 13)
        );


        countLabel = new JLabel(
                "0 active document(s)"
        );

        countLabel.setFont(
                AppSupport.font(Font.BOLD, 13)
        );

        countLabel.setForeground(ACCENT_BLUE);


        JPanel right = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        10,
                        0
                )
        );

        right.setOpaque(false);

        right.add(countLabel);


        footer.add(
                user,
                BorderLayout.NORTH
        );

        footer.add(
                statusLabel,
                BorderLayout.CENTER
        );

        footer.add(
                right,
                BorderLayout.EAST
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
                new BorderLayout(0, 5)
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


    // ================= FILE SELECTION =================


    private void chooseFile() {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Select Document"
        );


        int result =
                chooser.showOpenDialog(this);


        if (result
                != JFileChooser.APPROVE_OPTION) {
            return;
        }


        File file =
                chooser.getSelectedFile();


        if (file == null || !file.exists()) {

            showWarning(
                    "The selected file does not exist."
            );

            return;
        }


        if (!isAllowedFileType(
                file.getName()
        )) {

            showWarning(
                    "Allowed file types are PDF, Word, Excel, PowerPoint, images, and text files."
            );

            return;
        }


        if (file.length() > MAX_FILE_SIZE) {

            showWarning(
                    "The file is too large. Maximum size is 20 MB."
            );

            return;
        }


        selectedFile = file;

        selectedFileLabel.setText(
                "Selected: " + file.getName()
                + "  |  "
                + formatFileSize(file.length())
        );
    }


    private boolean isAllowedFileType(
            String fileName
    ) {

        String name =
                fileName.toLowerCase();


        return name.endsWith(".pdf")
                || name.endsWith(".doc")
                || name.endsWith(".docx")
                || name.endsWith(".xls")
                || name.endsWith(".xlsx")
                || name.endsWith(".ppt")
                || name.endsWith(".pptx")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".gif")
                || name.endsWith(".txt")
                || name.endsWith(".csv");
    }


    private String formatFileSize(long size) {

        if (size < 1024) {
            return size + " B";
        }


        if (size < 1024 * 1024) {

            return String.format(
                    "%.1f KB",
                    size / 1024.0
            );
        }


        return String.format(
                "%.1f MB",
                size / (1024.0 * 1024.0)
        );
    }


    // ================= UPLOAD =================


    private void uploadDocument() {

        String title =
                titleField.getText().trim();

        String description =
                descriptionArea.getText().trim();

        String category = String.valueOf(
                categorySelector.getSelectedItem()
        );


        if (title.isEmpty()) {

            showWarning(
                    "Enter a document title."
            );

            return;
        }


        if (selectedFile == null) {

            showWarning(
                    "Choose a document file first."
            );

            return;
        }


        String storedFileName =
                System.currentTimeMillis()
                + "_"
                + selectedFile.getName();


        Path source =
                selectedFile.toPath();

        Path destination = Paths.get(
                DOCUMENT_STORAGE_FOLDER,
                storedFileName
        );


        try {

            Files.copy(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException error) {

            showError(
                    "The document file could not be saved.",
                    error
            );

            return;
        }


        String sql = """
            INSERT INTO documents
            (
                title,
                description,
                category,
                file_name,
                file_path,
                uploaded_by,
                is_active
            )
            VALUES (?, ?, ?, ?, ?, ?, 1)
            """;


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setString(
                    4,
                    selectedFile.getName()
            );

            ps.setString(
                    5,
                    destination.toString()
            );

            ps.setString(6, username);

            ps.executeUpdate();


            clearUploadForm();
            loadDocuments();


            setStatus(
                    "Document uploaded successfully."
            );


        } catch (Exception error) {

            showError(
                    "The document could not be uploaded.",
                    error
            );
        }
    }


    private void clearUploadForm() {

        titleField.setText("");
        descriptionArea.setText("");

        selectedFile = null;

        selectedFileLabel.setText(
                "No file selected."
        );

        categorySelector.setSelectedIndex(0);
    }


    // ================= DOCUMENT ACTIONS =================


    private int getSelectedDocumentId() {

        int row =
                documentTable.getSelectedRow();


        if (row < 0) {

            showWarning(
                    "Select a document first."
            );

            return -1;
        }


        int modelRow = documentTable
                .convertRowIndexToModel(row);


        return (int) documentModel.getValueAt(
                modelRow,
                0
        );
    }


    private String getSelectedDocumentPath() {

        int row =
                documentTable.getSelectedRow();


        if (row < 0) {
            return null;
        }


        int modelRow = documentTable
                .convertRowIndexToModel(row);


        return String.valueOf(
                documentModel.getValueAt(
                        modelRow,
                        4
                )
        );
    }


    private void openSelectedDocument() {

        int documentId =
                getSelectedDocumentId();


        if (documentId < 0) {
            return;
        }


        String path =
                getSelectedDocumentPath();


        if (path == null || path.isEmpty()) {
            return;
        }


        File file = new File(path);


        if (!file.exists()) {

            showWarning(
                    "The document file cannot be found on this computer."
            );

            return;
        }


        try {

            if (Desktop.isDesktopSupported()) {

                Desktop.getDesktop()
                        .open(file);

                setStatus(
                        "Document opened."
                );

            } else {

                showWarning(
                        "This computer cannot open files directly."
                );
            }

        } catch (Exception error) {

            showError(
                    "The document could not be opened.",
                    error
            );
        }
    }


    private void downloadSelectedDocument() {

        int documentId =
                getSelectedDocumentId();


        if (documentId < 0) {
            return;
        }


        String sourcePath =
                getSelectedDocumentPath();


        if (sourcePath == null
                || sourcePath.isEmpty()) {
            return;
        }


        File sourceFile =
                new File(sourcePath);


        if (!sourceFile.exists()) {

            showWarning(
                    "The original document file cannot be found."
            );

            return;
        }


        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Save Document As"
        );

        chooser.setSelectedFile(
                new File(sourceFile.getName())
        );


        int result =
                chooser.showSaveDialog(this);


        if (result
                != JFileChooser.APPROVE_OPTION) {
            return;
        }


        File destination =
                chooser.getSelectedFile();


        try {

            Files.copy(
                    sourceFile.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );


            setStatus(
                    "Document saved to: "
                    + destination.getAbsolutePath()
            );


        } catch (IOException error) {

            showError(
                    "The document could not be downloaded.",
                    error
            );
        }
    }


    private void editSelectedDocument() {

        int documentId =
                getSelectedDocumentId();


        if (documentId < 0) {
            return;
        }


        int row =
                documentTable.getSelectedRow();

        int modelRow = documentTable
                .convertRowIndexToModel(row);


        String currentTitle = String.valueOf(
                documentModel.getValueAt(modelRow, 1)
        );

        String currentDescription =
                String.valueOf(
                        documentModel.getValueAt(
                                modelRow,
                                2
                        )
                );

        String currentCategory = String.valueOf(
                documentModel.getValueAt(modelRow, 3)
        );


        JTextField titleInput =
                new JTextField(currentTitle);

        JTextArea descriptionInput =
                new JTextArea(
                        currentDescription,
                        4,
                        20
                );

        descriptionInput.setLineWrap(true);
        descriptionInput.setWrapStyleWord(true);


        JComboBox<String> categoryInput =
                new JComboBox<>(
                        new String[]{
                            "Policies",
                            "Reports",
                            "Circulars",
                            "Forms",
                            "Certificates",
                            "Examinations",
                            "Finance",
                            "Academic",
                            "Other"
                        }
                );

        categoryInput.setSelectedItem(
                currentCategory
        );


        JPanel form = new JPanel(
                new BorderLayout(0, 10)
        );

        form.add(
                new JLabel("Title:"),
                BorderLayout.NORTH
        );

        form.add(
                titleInput,
                BorderLayout.CENTER
        );

        form.add(
                new JScrollPane(descriptionInput),
                BorderLayout.EAST
        );

        form.add(
                categoryInput,
                BorderLayout.SOUTH
        );


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        form,
                        "Edit Document Details",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );


        if (result
                != JOptionPane.OK_OPTION) {
            return;
        }


        String newTitle =
                titleInput.getText().trim();

        String newDescription =
                descriptionInput.getText().trim();

        String newCategory = String.valueOf(
                categoryInput.getSelectedItem()
        );


        if (newTitle.isEmpty()) {

            showWarning(
                    "Document title cannot be empty."
            );

            return;
        }


        String sql = """
            UPDATE documents
            SET title = ?,
                description = ?,
                category = ?
            WHERE document_id = ?
            """;


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(1, newTitle);
            ps.setString(2, newDescription);
            ps.setString(3, newCategory);
            ps.setInt(4, documentId);

            ps.executeUpdate();


            loadDocuments();


            setStatus(
                    "Document details updated."
            );


        } catch (Exception error) {

            showError(
                    "The document details could not be updated.",
                    error
            );
        }
    }


    private void deactivateSelectedDocument() {

        int documentId =
                getSelectedDocumentId();


        if (documentId < 0) {
            return;
        }


        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deactivate the selected document?",
                        "Confirm Deactivate",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );


        if (choice
                != JOptionPane.YES_OPTION) {
            return;
        }


        String sql = """
            UPDATE documents
            SET is_active = 0
            WHERE document_id = ?
            """;


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, documentId);
            ps.executeUpdate();


            loadDocuments();


            setStatus(
                    "Document deactivated."
            );


        } catch (Exception error) {

            showError(
                    "The document could not be deactivated.",
                    error
            );
        }
    }


    private void deleteSelectedDocument() {

        int documentId =
                getSelectedDocumentId();


        if (documentId < 0) {
            return;
        }


        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Permanently delete this document?\n\n"
                        + "The database record and stored file will be removed.",
                        "Confirm Permanent Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );


        if (choice
                != JOptionPane.YES_OPTION) {
            return;
        }


        String filePath =
                getSelectedDocumentPath();


        String sql =
                "DELETE FROM documents WHERE document_id = ?";


        try (
                Connection con =
                        Conn.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, documentId);
            ps.executeUpdate();


            if (filePath != null
                    && !filePath.isEmpty()) {

                try {

                    Files.deleteIfExists(
                            Paths.get(filePath)
                    );

                } catch (IOException ignored) {
                }
            }


            loadDocuments();


            setStatus(
                    "Document permanently deleted."
            );


        } catch (Exception error) {

            showError(
                    "The document could not be deleted.",
                    error
            );
        }
    }


    // ================= LOAD DOCUMENTS =================


    private void loadDocuments() {

        documentModel.setRowCount(0);

        categoryFilter.removeAllItems();
        uploaderFilter.removeAllItems();


        categoryFilter.addItem(
                "All Categories"
        );

        uploaderFilter.addItem("All Users");


        SwingWorker<List<Object[]>, Void> worker =
                new SwingWorker<List<Object[]>, Void>() {


            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {


                List<Object[]> rows =
                        new ArrayList<>();


                String sql = """
                    SELECT
                        document_id,
                        title,
                        description,
                        category,
                        file_path,
                        file_name,
                        uploaded_by,
                        uploaded_at,
                        is_active
                    FROM documents
                    ORDER BY uploaded_at DESC,
                        document_id DESC
                    """;


                try (
                        Connection con =
                                Conn.getConnection();

                        Statement statement =
                                con.createStatement();

                        ResultSet rs =
                                statement.executeQuery(sql)
                ) {


                    while (rs.next()) {

                        boolean active =
                                rs.getInt("is_active") == 1;


                        rows.add(new Object[]{
                            rs.getInt("document_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("category"),
                            rs.getString("file_path"),
                            rs.getString("file_name"),
                            rs.getString("uploaded_by"),
                            rs.getTimestamp("uploaded_at"),
                            active
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

                    List<Object[]> rows = get();


                    int activeCount = 0;


                    for (Object[] row : rows) {

                        documentModel.addRow(row);


                        String category =
                                String.valueOf(row[3]);

                        String uploader =
                                String.valueOf(row[6]);


                        if (!categoryIsEmpty(category)) {

                            if (categoryFilter
                                    .getItemCount() == 1
                                    || !categoryExists(category)) {

                                categoryFilter.addItem(
                                        category
                                );
                            }
                        }


                        if (!uploaderIsEmpty(uploader)) {

                            if (uploaderFilter
                                    .getItemCount() == 1
                                    || !uploaderExists(uploader)) {

                                uploaderFilter.addItem(
                                        uploader
                                );
                            }
                        }


                        if ("Active".equals(row[8])) {
                            activeCount++;
                        }
                    }


                    countLabel.setText(
                            activeCount
                            + " active document(s)"
                    );


                    setStatus(
                            documentModel.getRowCount()
                            + " document record(s) loaded."
                    );


                    applyFilters();


                } catch (InterruptedException error) {

                    Thread.currentThread().interrupt();


                } catch (ExecutionException error) {

                    showError(
                            "Documents could not be loaded.",
                            error
                    );
                }
            }
        };


        worker.execute();
    }


    private boolean categoryIsEmpty(
            String value
    ) {

        return value == null
                || value.trim().isEmpty()
                || "null".equals(value);
    }


    private boolean uploaderIsEmpty(
            String value
    ) {

        return value == null
                || value.trim().isEmpty()
                || "null".equals(value);
    }


    private boolean categoryExists(
            String value
    ) {

        for (int i = 0;
                i < categoryFilter.getItemCount();
                i++) {

            if (value.equals(
                    categoryFilter.getItemAt(i)
            )) {
                return true;
            }
        }


        return false;
    }


    private boolean uploaderExists(
            String value
    ) {

        for (int i = 0;
                i < uploaderFilter.getItemCount();
                i++) {

            if (value.equals(
                    uploaderFilter.getItemAt(i)
            )) {
                return true;
            }
        }


        return false;
    }


    // ================= FILTERS =================


    private void applyFilters() {

        List<RowFilter<Object, Object>> filters =
                new ArrayList<>();


        String text =
                searchField.getText().trim();


        if (!text.isEmpty()) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)" + text
                    )
            );
        }


        String category = String.valueOf(
                categoryFilter.getSelectedItem()
        );


        if (category != null
                && !category.equals("All Categories")) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)^" + category + "$",
                            3
                    )
            );
        }


        String uploader = String.valueOf(
                uploaderFilter.getSelectedItem()
        );


        if (uploader != null
                && !uploader.equals("All Users")) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)^" + uploader + "$",
                            6
                    )
            );
        }


        if (filters.isEmpty()) {

            sorter.setRowFilter(null);

        } else {

            sorter.setRowFilter(
                    RowFilter.andFilter(filters)
            );
        }
    }


    // ================= ERRORS =================


    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "School Documents",
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
                "Document Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}