import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;



public class Dashboard extends JFrame {



    private static final long serialVersionUID = 1L;



    private static final Color DARK_BLUE =
            new Color(15, 40, 80);


    private static final Color SIDEBAR_HOVER =
            new Color(25, 60, 110);


    private static final Color BACKGROUND =
            new Color(243, 246, 251);


    private static final Color ACCENT_BLUE =
            new Color(0, 123, 255);


    private static final Color WHITE =
            Color.WHITE;



    private final String username;
    private final String fullName;
    private final String role;



    private JPanel contentPanel;



    public Dashboard(
            String username,
            String fullName,
            String role
    ) {


        this.username = username;
        this.fullName = fullName;


        this.role = role == null
                ? ""
                : role.trim();



        setTitle(
                "HVP Gatagara High School - "
                + this.role + " Dashboard"
        );



        setSize(1300, 750);
        setMinimumSize(
                new Dimension(1000, 650)
        );


        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );


        setLocationRelativeTo(null);
        setLayout(new BorderLayout());



        add(
                createMainPanel(),
                BorderLayout.CENTER
        );


        add(
                createSidebar(),
                BorderLayout.WEST
        );



        openPage("Home");
    }



    // ================= MAIN AREA =================



    private JPanel createMainPanel() {


        JPanel mainPanel = new JPanel(
                new BorderLayout()
        );


        mainPanel.setBackground(BACKGROUND);



        mainPanel.add(
                createTopBar(),
                BorderLayout.NORTH
        );



        contentPanel = new JPanel(
                new BorderLayout()
        );


        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(
                new EmptyBorder(
                        20,
                        25,
                        20,
                        25
                )
        );



        mainPanel.add(
                contentPanel,
                BorderLayout.CENTER
        );



        return mainPanel;
    }



    private JPanel createTopBar() {


        JPanel topBar = new JPanel(
                new BorderLayout(15, 0)
        );


        topBar.setBackground(WHITE);
        topBar.setBorder(
                new EmptyBorder(
                        15,
                        25,
                        15,
                        25
                )
        );



        JPanel titlePanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        0
                )
        );


        titlePanel.setOpaque(false);



        JLabel schoolIcon = createIconLabel(
                "🏫",
                22,
                DARK_BLUE
        );



        JLabel schoolTitle = new JLabel(
                "HVP Gatagara High School"
        );


        schoolTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20
                )
        );


        schoolTitle.setForeground(DARK_BLUE);



        titlePanel.add(schoolIcon);
        titlePanel.add(schoolTitle);



        JPanel userPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        12,
                        0
                )
        );


        userPanel.setOpaque(false);



        JLabel userIcon = createIconLabel(
                "👤",
                18,
                DARK_BLUE
        );



        JLabel userInfo = new JLabel(
                fullName + "  |  " + role
        );


        userInfo.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );


        userInfo.setForeground(DARK_BLUE);



        JButton logoutButton = new JButton(
                "Logout"
        );


        logoutButton.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );


        logoutButton.setBackground(
                new Color(220, 53, 69)
        );


        logoutButton.setForeground(WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );


        logoutButton.addActionListener(
                event -> logout()
        );



        userPanel.add(userIcon);
        userPanel.add(userInfo);
        userPanel.add(logoutButton);



        topBar.add(
                titlePanel,
                BorderLayout.WEST
        );


        topBar.add(
                userPanel,
                BorderLayout.EAST
        );



        return topBar;
    }



    // ================= SIDEBAR =================



    private JScrollPane createSidebar() {


        JPanel sidebar = new JPanel();


        sidebar.setBackground(DARK_BLUE);
        sidebar.setLayout(
                new BoxLayout(
                        sidebar,
                        BoxLayout.Y_AXIS
                )
        );


        sidebar.setBorder(
                new EmptyBorder(
                        25,
                        15,
                        25,
                        15
                )
        );



        JPanel schoolHeading = new JPanel();


        schoolHeading.setOpaque(false);
        schoolHeading.setLayout(
                new BoxLayout(
                        schoolHeading,
                        BoxLayout.Y_AXIS
                )
        );


        schoolHeading.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        schoolHeading.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        100
                )
        );



        JLabel schoolIcon = createIconLabel(
                "🏫",
                28,
                WHITE
        );


        schoolIcon.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );



        JLabel schoolName = new JLabel(
                "HVP GATAGARA"
        );


        schoolName.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20
                )
        );


        schoolName.setForeground(WHITE);
        schoolName.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );



        JLabel subtitle = new JLabel(
                "Management System"
        );


        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );


        subtitle.setForeground(
                new Color(180, 205, 240)
        );


        subtitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );



        schoolHeading.add(schoolIcon);
        schoolHeading.add(
                Box.createVerticalStrut(8)
        );


        schoolHeading.add(schoolName);
        schoolHeading.add(
                Box.createVerticalStrut(4)
        );


        schoolHeading.add(subtitle);



        sidebar.add(schoolHeading);
        sidebar.add(
                Box.createVerticalStrut(25)
        );



        for (String[] menu : getMenus()) {


            sidebar.add(
                    createSidebarButton(
                            menu[0],
                            menu[1]
                    )
            );


            sidebar.add(
                    Box.createVerticalStrut(8)
            );
        }



        sidebar.add(Box.createVerticalGlue());



        JScrollPane scrollPane =
                new JScrollPane(sidebar);


        scrollPane.setPreferredSize(
                new Dimension(280, 0)
        );


        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants
                        .HORIZONTAL_SCROLLBAR_NEVER
        );


        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(18);


        scrollPane.getViewport()
                .setBackground(DARK_BLUE);



        return scrollPane;
    }



    private List<String[]> getMenus() {


        List<String[]> menus =
                new ArrayList<>();



        menus.add(
                new String[]{"Home", "🏠"}
        );



        switch (role) {



            case "Admin" -> {


                menus.add(new String[]{
                    "Manage Users",
                    "👥"
                });


                menus.add(new String[]{
                    "Manage Students",
                    "🎓"
                });


                menus.add(new String[]{
                    "Manage Teachers",
                    "👤"
                });


                menus.add(new String[]{
                    "Manage Classes",
                    "🏫"
                });


                menus.add(new String[]{
                    "Manage Subjects",
                    "📚"
                });


                menus.add(new String[]{
                    "Manage Officers",
                    "🛡"
                });


                menus.add(new String[]{
                    "Grading Policies",
                    "📝"
                });


                menus.add(new String[]{
                    "Parent Management",
                    "👨‍👩‍👧"
                });


                menus.add(new String[]{
                    "Enrollments",
                    "📋"
                });


                menus.add(new String[]{
                    "Timetable",
                    "⏰"
                });


                menus.add(new String[]{
                    "Reports",
                    "📊"
                });
            }



            case "Teacher" -> {


                menus.add(new String[]{
                    "My Classes",
                    "🏫"
                });


                menus.add(new String[]{
                    "My Subjects",
                    "📚"
                });


                menus.add(new String[]{
                    "Enter Grades",
                    "📝"
                });


                menus.add(new String[]{
                    "Attendance",
                    "📅"
                });


                menus.add(new String[]{
                    "Timetable",
                    "⏰"
                });


                menus.add(new String[]{
                    "Students",
                    "🎓"
                });
            }



            case "Secretary" -> {


                menus.add(new String[]{
                    "Student Records",
                    "🎓"
                });


                menus.add(new String[]{
                    "Enrollments",
                    "📋"
                });


                menus.add(new String[]{
                    "Timetable",
                    "⏰"
                });


                menus.add(new String[]{
                    "Documents",
                    "📁"
                });


                menus.add(new String[]{
                    "Reports",
                    "📊"
                });
            }



            case "Bursar" -> {


                menus.add(new String[]{
                    "Fee Management",
                    "🧮"
                });


                menus.add(new String[]{
                    "School Fees",
                    "💰"
                });


                menus.add(new String[]{
                    "Payments",
                    "🧾"
                });


                menus.add(new String[]{
                    "Financial Reports",
                    "📊"
                });
            }



            case "Registrar" -> {


                menus.add(new String[]{
                    "Admissions",
                    "📥"
                });


                menus.add(new String[]{
                    "Student Records",
                    "🎓"
                });


                menus.add(new String[]{
                    "Enrollments",
                    "📋"
                });


                menus.add(new String[]{
                    "Timetable",
                    "⏰"
                });


                menus.add(new String[]{
                    "Academic Reports",
                    "📊"
                });


                menus.add(new String[]{
                    "Transcripts",
                    "📄"
                });
            }



            case "Student" -> {


                menus.add(new String[]{
                    "My Profile",
                    "👤"
                });


                menus.add(new String[]{
                    "My Registered Subjects",
                    "📚"
                });


                menus.add(new String[]{
                    "My Grades",
                    "📝"
                });


                menus.add(new String[]{
                    "My Attendance",
                    "📅"
                });


                menus.add(new String[]{
                    "Timetable",
                    "⏰"
                });
            }



            case "Parent" -> {


                menus.add(new String[]{
                    "My Children",
                    "🎓"
                });
            }



            default -> {
                // Home remains available.
            }
        }



        return menus;
    }



    private JButton createSidebarButton(
            String text,
            String icon
    ) {


        JButton button = new JButton(text);


        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );


        button.setForeground(WHITE);
        button.setBackground(DARK_BLUE);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );


        button.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        button.setBorder(
                new EmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );


        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );


        button.setPreferredSize(
                new Dimension(240, 45)
        );


        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );



        if ("Timetable".equals(text)) {


            button.setIcon(
                    new ClockIcon(
                            20,
                            WHITE
                    )
            );


        } else {


            button.setIcon(
                    new EmojiIcon(
                            icon,
                            20,
                            WHITE
                    )
            );
        }



        button.setIconTextGap(12);



        button.addMouseListener(
                new MouseAdapter() {



                    @Override
                    public void mouseEntered(
                            MouseEvent event
                    ) {


                        button.setBackground(
                                SIDEBAR_HOVER
                        );
                    }



                    @Override
                    public void mouseExited(
                            MouseEvent event
                    ) {


                        button.setBackground(
                                DARK_BLUE
                        );
                    }
                }
        );



        button.addActionListener(
                event -> openPage(text)
        );



        return button;
    }



    private JLabel createIconLabel(
            String icon,
            int size,
            Color color
    ) {


        return new JLabel(
                new EmojiIcon(
                        icon,
                        size,
                        color
                )
        );
    }



    // ================= PAGE NAVIGATION =================



    private void openPage(String pageName) {


        contentPanel.removeAll();



        switch (pageName) {



            case "Home" ->
                    showHomeFeed();



            // ================= ADMIN =================



            case "Manage Users" ->
                    contentPanel.add(
                            new UsersPanel(),
                            BorderLayout.CENTER
                    );



            case "Manage Students",
                    "Student Records",
                    "Admissions",
                    "Students" ->
                    contentPanel.add(
                            new StudentsPanel(),
                            BorderLayout.CENTER
                    );



            case "Manage Teachers" ->
                    contentPanel.add(
                            new TeachersPanel(),
                            BorderLayout.CENTER
                    );



            case "Manage Classes" ->
                    contentPanel.add(
                            new ClassesPanel(),
                            BorderLayout.CENTER
                    );



            case "Manage Subjects" ->
                    contentPanel.add(
                            new SubjectsPanel(),
                            BorderLayout.CENTER
                    );



            case "Manage Officers" ->
                    contentPanel.add(
                            new OfficersPanel(),
                            BorderLayout.CENTER
                    );



            case "Grading Policies" ->
                    contentPanel.add(
                            new AdminGradingPolicyPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "Parent Management" ->
                    contentPanel.add(
                            new AdminParentManagementPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "Enrollments" ->
                    contentPanel.add(
                            new EnrollmentPanel(),
                            BorderLayout.CENTER
                    );



            case "Timetable" ->
                    contentPanel.add(
                            new TimetablePanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "Reports" ->
                    contentPanel.add(
                            new AdminReportsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            // ================= TEACHER =================



            case "My Classes" ->
                    contentPanel.add(
                            new MyClassesPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "My Subjects" ->
                    contentPanel.add(
                            new MySubjectsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "Enter Grades" ->
                    contentPanel.add(
                            new GradesPanel(),
                            BorderLayout.CENTER
                    );



            case "Attendance" ->
                    contentPanel.add(
                            new AttendancePanel(),
                            BorderLayout.CENTER
                    );



            // ================= SECRETARY =================



            case "Documents" ->
                    contentPanel.add(
                            new DocumentsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            // ================= BURSAR =================



            case "Fee Management" ->
                    contentPanel.add(
                            new BursarFinancePanel(
                                    username,
                                    0
                            ),
                            BorderLayout.CENTER
                    );



            case "School Fees" ->
                    contentPanel.add(
                            new BursarFinancePanel(
                                    username,
                                    0
                            ),
                            BorderLayout.CENTER
                    );



            case "Payments" ->
                    contentPanel.add(
                            new BursarFinancePanel(
                                    username,
                                    1
                            ),
                            BorderLayout.CENTER
                    );



            case "Financial Reports" ->
                    contentPanel.add(
                            new BursarFinancePanel(
                                    username,
                                    2
                            ),
                            BorderLayout.CENTER
                    );



            // ================= REGISTRAR =================



            case "Academic Reports" ->
                    contentPanel.add(
                            new RegistrarReportsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "Transcripts" ->
                    contentPanel.add(
                            new RegistrarReportsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            // ================= STUDENT =================



            case "My Profile" ->
                    contentPanel.add(
                            new MyProfilePanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "My Registered Subjects" ->
                    contentPanel.add(
                            new MyRegisteredSubjectsPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "My Grades" ->
                    contentPanel.add(
                            new MyGradesPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            case "My Attendance" ->
                    contentPanel.add(
                            new MyAttendancePanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            // ================= PARENT =================



            case "My Children" ->
                    contentPanel.add(
                            new ParentChildrenPanel(
                                    username
                            ),
                            BorderLayout.CENTER
                    );



            default ->
                    showHomeFeed();
        }



        contentPanel.revalidate();
        contentPanel.repaint();
    }



    // ================= HOME FEED =================



    private void showHomeFeed() {


        contentPanel.add(
                new HomeFeedPanel(
                        username,
                        fullName,
                        role
                ),
                BorderLayout.CENTER
        );
    }



    // ================= PLACEHOLDERS =================



    private void showPlaceholder(
            String title,
            String description
    ) {


        JPanel panel = new JPanel();


        panel.setBackground(WHITE);
        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );


        panel.setBorder(
                new EmptyBorder(
                        35,
                        35,
                        35,
                        35
                )
        );



        JLabel titleLabel = new JLabel(title);


        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        26
                )
        );


        titleLabel.setForeground(DARK_BLUE);
        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );



        JLabel descriptionLabel =
                new JLabel(description);


        descriptionLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );


        descriptionLabel.setForeground(
                new Color(90, 105, 125)
        );


        descriptionLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );



        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(
                Box.createVerticalStrut(15)
        );


        panel.add(descriptionLabel);
        panel.add(Box.createVerticalGlue());



        contentPanel.add(
                panel,
                BorderLayout.CENTER
        );
    }



    // ================= LOGOUT =================



    private void logout() {


        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to log out?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );



        if (choice == JOptionPane.YES_OPTION) {


            LoginApp login = new LoginApp();
            login.setVisible(true);
            dispose();
        }
    }



    // ================= ICONS =================



    private static class EmojiIcon
            implements Icon {



        private final String text;
        private final int size;
        private final Color color;
        private final Font font;



        EmojiIcon(
                String text,
                int size,
                Color color
        ) {


            this.text = text;
            this.size = size;
            this.color = color;


            this.font = new Font(
                    "Segoe UI Emoji",
                    Font.PLAIN,
                    size
            );
        }



        @Override
        public int getIconWidth() {
            return size + 8;
        }



        @Override
        public int getIconHeight() {
            return size + 8;
        }



        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {


            Graphics2D graphics2D =
                    (Graphics2D) graphics.create();



            try {


                graphics2D.setRenderingHint(
                        RenderingHints
                                .KEY_TEXT_ANTIALIASING,
                        RenderingHints
                                .VALUE_TEXT_ANTIALIAS_ON
                );



                graphics2D.setFont(font);
                graphics2D.setColor(color);



                FontMetrics metrics =
                        graphics2D.getFontMetrics();



                int baseline = y
                        + (getIconHeight()
                        - metrics.getHeight()) / 2
                        + metrics.getAscent();



                graphics2D.drawString(
                        text,
                        x + 2,
                        baseline
                );



            } finally {
                graphics2D.dispose();
            }
        }
    }



    private static class ClockIcon
            implements Icon {



        private final int size;
        private final Color color;



        ClockIcon(
                int size,
                Color color
        ) {


            this.size = size;
            this.color = color;
        }



        @Override
        public int getIconWidth() {
            return size + 8;
        }



        @Override
        public int getIconHeight() {
            return size + 8;
        }



        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {


            Graphics2D graphics2D =
                    (Graphics2D) graphics.create();



            try {


                graphics2D.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints
                                .VALUE_ANTIALIAS_ON
                );



                graphics2D.setColor(color);
                graphics2D.setStroke(
                        new BasicStroke(
                                2.0f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );



                int left = x + 4;
                int top = y + 4;


                int centerX = left + size / 2;
                int centerY = top + size / 2;



                graphics2D.drawOval(
                        left + 1,
                        top + 1,
                        size - 3,
                        size - 3
                );



                graphics2D.drawLine(
                        centerX,
                        centerY,
                        centerX,
                        top + size / 4
                );



                graphics2D.drawLine(
                        centerX,
                        centerY,
                        left + size * 3 / 4,
                        centerY
                );



            } finally {
                graphics2D.dispose();
            }
        }
    }
}