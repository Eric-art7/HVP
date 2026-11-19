import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyProfilePanel extends JPanel {

    private static final Color darkBlue = new Color(15, 40, 80);
    private static final Color lightBackground = new Color(243, 246, 251);
    private static final Color accentBlue = new Color(0, 123, 255);

    private final String username;

    private JLabel photoLabel;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField guardianNameField;
    private JTextField guardianPhoneField;

    private int userId;

    public MyProfilePanel(String username) {
        this.username = username;

        setLayout(new BorderLayout(20, 20));
        setBackground(lightBackground);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadUserProfile();
        loadProfilePhoto();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel heading = new JLabel("👤 My Profile");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(darkBlue);

        JLabel subtitle = new JLabel("View and update your personal information");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 115, 135));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(25, 0));
        mainPanel.setOpaque(false);

        mainPanel.add(createPhotoPanel(), BorderLayout.WEST);
        mainPanel.add(createDetailsPanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createPhotoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setPreferredSize(new Dimension(300, 0));

        photoLabel = new JLabel();
        photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setMaximumSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235), 2));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setText("No Photo");

        JButton uploadButton = new JButton("📷 Upload Photo");
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uploadButton.setBackground(accentBlue);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setMaximumSize(new Dimension(220, 42));
        uploadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadButton.addActionListener(e -> chooseAndUploadPhoto());

        JButton removeButton = new JButton("🗑️ Remove Photo");
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setMaximumSize(new Dimension(220, 42));
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> removePhoto());

        panel.add(Box.createVerticalGlue());
        panel.add(photoLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(uploadButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(removeButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        fullNameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();
        guardianNameField = new JTextField();
        guardianPhoneField = new JTextField();

        gbc.gridy = 0;
        panel.add(new JLabel("Full Name"), gbc);
        gbc.gridy = 1;
        panel.add(fullNameField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Email"), gbc);
        gbc.gridy = 3;
        panel.add(emailField, gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Phone"), gbc);
        gbc.gridy = 5;
        panel.add(phoneField, gbc);

        gbc.gridy = 6;
        panel.add(new JLabel("Address"), gbc);
        gbc.gridy = 7;
        panel.add(addressField, gbc);

        gbc.gridy = 8;
        panel.add(new JLabel("Guardian Name"), gbc);
        gbc.gridy = 9;
        panel.add(guardianNameField, gbc);

        gbc.gridy = 10;
        panel.add(new JLabel("Guardian Phone"), gbc);
        gbc.gridy = 11;
        panel.add(guardianPhoneField, gbc);

        gbc.gridy = 12;
        gbc.insets = new Insets(20, 0, 0, 0);

        JButton updateButton = new JButton("💾 Update Profile");
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        updateButton.setBackground(darkBlue);
        updateButton.setForeground(Color.WHITE);
        updateButton.setFocusPainted(false);
        updateButton.setPreferredSize(new Dimension(0, 45));
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateButton.addActionListener(e -> updateProfile());

        panel.add(updateButton, gbc);

        return panel;
    }

    private void loadUserProfile() {
        String sql = """
            SELECT u.user_id, u.full_name, u.email,
                   s.address, s.guardian_name, s.guardian_phone
            FROM users u
            LEFT JOIN students s ON u.student_id = s.student_id
            WHERE u.username = ?
        """;

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    fullNameField.setText(rs.getString("full_name"));
                    emailField.setText(rs.getString("email"));
                    addressField.setText(rs.getString("address"));
                    guardianNameField.setText(rs.getString("guardian_name"));
                    guardianPhoneField.setText(rs.getString("guardian_phone"));
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load profile: " + e.getMessage());
        }
    }

    private void loadProfilePhoto() {
        if (userId == 0) {
            return;
        }

        String sql = "SELECT photo FROM user_photos WHERE user_id = ?";

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] photoBytes = rs.getBytes("photo");

                    if (photoBytes != null) {
                        ImageIcon imageIcon = new ImageIcon(photoBytes);
                        Image image = imageIcon.getImage()
                                .getScaledInstance(200, 200, Image.SCALE_SMOOTH);

                        photoLabel.setIcon(new ImageIcon(image));
                        photoLabel.setText(null);
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load photo: " + e.getMessage());
        }
    }

    private void chooseAndUploadPhoto() {
        if (userId == 0) {
            JOptionPane.showMessageDialog(this,
                    "Profile information was not loaded. Please logout and login again.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");

        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif"));

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();

        try {
            BufferedImage image = ImageIO.read(selectedFile);

            if (image == null) {
                JOptionPane.showMessageDialog(this, "Invalid image file.");
                return;
            }

            ImageIcon imageIcon = new ImageIcon(selectedFile.getAbsolutePath());
            Image scaledImage = imageIcon.getImage()
                    .getScaledInstance(200, 200, Image.SCALE_SMOOTH);

            photoLabel.setIcon(new ImageIcon(scaledImage));
            photoLabel.setText(null);

            savePhotoToDatabase(selectedFile);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not upload photo: " + e.getMessage());
        }
    }

    private void savePhotoToDatabase(File photoFile) {
        String checkSql = "SELECT photo_id FROM user_photos WHERE user_id = ?";

        String insertSql = """
            INSERT INTO user_photos
            (user_id, photo, file_name, file_type)
            VALUES (?, ?, ?, ?)
        """;

        String updateSql = """
            UPDATE user_photos
            SET photo = ?, file_name = ?, file_type = ?, uploaded_at = NOW()
            WHERE user_id = ?
        """;

        try (Connection con = Conn.getConnection()) {

            byte[] photoBytes = readFileBytes(photoFile);
            String fileName = photoFile.getName();
            String fileType = getFileExtension(fileName);

            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, userId);

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement update = con.prepareStatement(updateSql)) {
                            update.setBytes(1, photoBytes);
                            update.setString(2, fileName);
                            update.setString(3, fileType);
                            update.setInt(4, userId);
                            update.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                            insert.setInt(1, userId);
                            insert.setBytes(2, photoBytes);
                            insert.setString(3, fileName);
                            insert.setString(4, fileType);
                            insert.executeUpdate();
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Profile photo updated successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save photo: " + e.getMessage());
        }
    }

    private void removePhoto() {
        if (userId == 0) {
            JOptionPane.showMessageDialog(this,
                    "Profile information was not loaded. Please logout and login again.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove your profile photo?",
                "Remove Photo",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM user_photos WHERE user_id = ?";

        try (Connection con = Conn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            photoLabel.setIcon(null);
            photoLabel.setText("No Photo");

            JOptionPane.showMessageDialog(this, "Profile photo removed.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not remove photo: " + e.getMessage());
        }
    }

    private byte[] readFileBytes(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        }
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");

        if (index == -1) {
            return "unknown";
        }

        return fileName.substring(index + 1).toLowerCase();
    }

    private void updateProfile() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String guardianName = guardianNameField.getText().trim();
        String guardianPhone = guardianPhoneField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name and email are required.");
            return;
        }

        String updateUser = """
            UPDATE users
            SET full_name = ?, email = ?
            WHERE user_id = ?
        """;

        String updateStudent = """
            UPDATE students
            SET guardian_name = ?, guardian_phone = ?, address = ?
            WHERE student_id = (
                SELECT student_id FROM users WHERE user_id = ?
            )
        """;

        try (Connection con = Conn.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(updateUser)) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(updateStudent)) {
                ps.setString(1, guardianName);
                ps.setString(2, guardianPhone);
                ps.setString(3, address);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Profile updated successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not update profile: " + e.getMessage());
        }
    }
}