import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class HomeFeedPanel extends JPanel {


    private static final long serialVersionUID = 1L;


    private static final Color DARK_BLUE =
            new Color(15, 40, 80);


    private static final Color BACKGROUND =
            new Color(243, 246, 251);


    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");


    private final String username;


    private JPanel feedContainer;
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JComboBox<String> typeSelector;
    private JButton refreshButton;


    public HomeFeedPanel(
            String username,
            String fullName,
            String role
    ) {
        this.username = username;


        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));


        add(createHeader(fullName), BorderLayout.NORTH);
        add(createFeedArea(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);


        loadFeed();
    }


    // ================= HEADER =================


    private JPanel createHeader(String fullName) {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);


        JPanel titles = new JPanel(new BorderLayout(0, 6));
        titles.setOpaque(false);


        welcomeLabel = new JLabel(
                "Welcome, " + safeText(fullName, username)
        );
        welcomeLabel.setFont(
                AppSupport.font(Font.BOLD, 26)
        );
        welcomeLabel.setForeground(DARK_BLUE);


        JLabel subtitle = new JLabel(
                "School announcements, invitations, and recommendations"
        );
        subtitle.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        subtitle.setForeground(new Color(100, 115, 135));


        titles.add(welcomeLabel, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);


        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        controls.setOpaque(false);


        JLabel typeLabel = new JLabel("Show:");
        typeLabel.setFont(
                AppSupport.font(Font.BOLD, 14)
        );


        typeSelector = new JComboBox<>(new String[]{
            "All Types",
            "Announcement",
            "Invitation",
            "Recommendation"
        });
        typeSelector.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        typeSelector.addActionListener(event -> loadFeed());


        refreshButton = new JButton("Refresh");
        refreshButton.setFont(
                AppSupport.font(Font.BOLD, 13)
        );
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(event -> loadFeed());


        controls.add(typeLabel);
        controls.add(typeSelector);
        controls.add(refreshButton);


        header.add(titles, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);


        return header;
    }


    // ================= FEED AREA =================


    private JScrollPane createFeedArea() {
        feedContainer = new JPanel();
        feedContainer.setBackground(BACKGROUND);
        feedContainer.setLayout(
                new BoxLayout(feedContainer, BoxLayout.Y_AXIS)
        );
        feedContainer.setBorder(
                new EmptyBorder(0, 0, 0, 10)
        );


        JScrollPane scrollPane = new JScrollPane(feedContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);


        return scrollPane;
    }


    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(
                new EmptyBorder(12, 15, 12, 15)
        );


        statusLabel = new JLabel("Loading school updates...");
        statusLabel.setFont(
                AppSupport.font(Font.PLAIN, 14)
        );
        statusLabel.setForeground(DARK_BLUE);


        footer.add(statusLabel, BorderLayout.CENTER);


        return footer;
    }


    // ================= DATA LOADING =================


    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        typeSelector.setEnabled(!busy);
    }


    private void loadFeed() {
        final String selectedType =
                (String) typeSelector.getSelectedItem();


        setBusy(true);
        feedContainer.removeAll();
        statusLabel.setText("Loading school updates...");
        refreshFeedArea();


        SwingWorker<FeedResult, Void> worker =
                new SwingWorker<FeedResult, Void>() {


            @Override
            protected FeedResult doInBackground()
                    throws Exception {


                FeedResult result = new FeedResult();


                try (Connection con = Conn.getConnection()) {


                    String accountSql = """
                        SELECT
                            u.full_name,
                            u.is_active,
                            r.role_name
                        FROM users u
                        JOIN roles r
                            ON r.role_id = u.role_id
                        WHERE u.username = ?
                        """;


                    try (PreparedStatement ps =
                            con.prepareStatement(accountSql)) {


                        ps.setQueryTimeout(30);
                        ps.setString(1, username);


                        try (ResultSet rs = ps.executeQuery()) {


                            if (!rs.next()) {
                                result.notice =
                                        "This account is unavailable.";
                                return result;
                            }


                            if (!rs.getBoolean("is_active")) {
                                result.notice =
                                        "This account is inactive. "
                                        + "Please contact the school administrator.";
                                return result;
                            }


                            result.fullName =
                                    rs.getString("full_name");
                            result.role =
                                    rs.getString("role_name");
                        }
                    }


                    StringBuilder feedSql = new StringBuilder();


                    feedSql.append("""
                        SELECT
                            f.title,
                            f.message,
                            f.feed_type,
                            f.posted_by,
                            f.posted_at
                        FROM school_feed f
                        WHERE (
                            f.target_role = 'All'
                            OR f.target_role = ?
                        )
                        """);


                    boolean filterType =
                            selectedType != null
                            && !"All Types".equals(selectedType);


                    if (filterType) {
                        feedSql.append(
                                " AND f.feed_type = ? "
                        );
                    }


                    feedSql.append("""
                        ORDER BY
                            f.posted_at DESC,
                            f.feed_id DESC
                        LIMIT 100
                        """);


                    try (PreparedStatement ps =
                            con.prepareStatement(
                                    feedSql.toString()
                            )) {


                        ps.setQueryTimeout(30);
                        ps.setString(1, result.role);


                        if (filterType) {
                            ps.setString(2, selectedType);
                        }


                        try (ResultSet rs = ps.executeQuery()) {


                            while (rs.next()) {


                                FeedItem item = new FeedItem();


                                item.title =
                                        rs.getString("title");
                                item.message =
                                        rs.getString("message");
                                item.type =
                                        rs.getString("feed_type");
                                item.postedBy =
                                        rs.getString("posted_by");
                                item.postedAt =
                                        rs.getTimestamp("posted_at");


                                result.items.add(item);
                            }
                        }
                    }
                }


                return result;
            }


            @Override
            protected void done() {
                try {
                    FeedResult result = get();


                    if (result.notice != null) {
                        addNotice(result.notice);
                        statusLabel.setText(
                                "School updates are unavailable."
                        );
                        return;
                    }


                    welcomeLabel.setText(
                            "Welcome, "
                                    + safeText(
                                            result.fullName,
                                            username
                                    )
                    );


                    if (result.items.isEmpty()) {
                        addNotice(
                                "No updates match this selection. "
                                + "Please check again later."
                        );
                    } else {


                        for (FeedItem item : result.items) {
                            feedContainer.add(
                                    createFeedCard(item)
                            );
                            feedContainer.add(
                                    Box.createVerticalStrut(15)
                            );
                        }
                    }


                    statusLabel.setText(
                            safeText(result.role, "User")
                                    + " | Updates shown: "
                                    + result.items.size()
                                    + " | Up to 100 newest matching posts"
                    );


                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading interrupted.");


                } catch (ExecutionException error) {


                    Throwable cause = error.getCause();


                    if (cause == null) {
                        cause = error;
                    }


                    cause.printStackTrace();


                    String message = cause.getMessage();


                    if (message == null
                            || message.trim().isEmpty()) {
                        message = cause.getClass().getName();
                    }


                    addNotice(
                            "School updates could not be loaded.\n\n"
                            + "Error details:\n"
                            + message
                    );


                    statusLabel.setText(
                            "Could not load school updates."
                    );


                } finally {
                    setBusy(false);
                    refreshFeedArea();
                }
            }
        };


        worker.execute();
    }


    // ================= FEED CARDS =================


    private JPanel createFeedCard(FeedItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(LEFT_ALIGNMENT);


        Color typeColor = getTypeColor(item.type);


        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 5, 0, 0, typeColor
                        ),
                        new EmptyBorder(16, 18, 16, 18)
                )
        );


        card.setPreferredSize(new Dimension(700, 260));
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 260)
        );


        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);


        JLabel typeLabel = new JLabel(
                safeText(item.type, "Update")
                        .toUpperCase(Locale.ROOT)
        );
        typeLabel.setFont(
                AppSupport.font(Font.BOLD, 12)
        );
        typeLabel.setForeground(typeColor);


        JTextArea titleArea = createReadOnlyText(
                safeText(item.title, "School update"),
                Font.BOLD,
                18
        );
        titleArea.setRows(2);


        heading.add(typeLabel, BorderLayout.NORTH);
        heading.add(titleArea, BorderLayout.CENTER);


        JTextArea messageArea = createReadOnlyText(
                safeText(item.message, ""),
                Font.PLAIN,
                14
        );
        messageArea.setRows(4);


        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(null);
        messageScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );


        String postedDate = item.postedAt == null
                ? "Date unavailable"
                : DATE_FORMAT.format(
                        item.postedAt.toLocalDateTime()
                );


        JLabel footer = new JLabel(
                "Posted by "
                        + safeText(
                                item.postedBy,
                                "School administration"
                        )
                        + " | "
                        + postedDate
        );
        footer.setFont(
                AppSupport.font(Font.PLAIN, 12)
        );
        footer.setForeground(new Color(100, 115, 135));


        card.add(heading, BorderLayout.NORTH);
        card.add(messageScroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);


        return card;
    }


    private JTextArea createReadOnlyText(
            String text,
            int style,
            int size
    ) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppSupport.font(style, size));
        area.setForeground(DARK_BLUE);
        area.setBackground(Color.WHITE);
        area.setBorder(null);
        area.setCaretPosition(0);


        return area;
    }


    private void addNotice(String message) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 250)
        );


        JTextArea text = createReadOnlyText(
                message,
                Font.PLAIN,
                15
        );


        card.add(text, BorderLayout.CENTER);
        feedContainer.add(card);
    }


    private void refreshFeedArea() {
        feedContainer.revalidate();
        feedContainer.repaint();
    }


    private Color getTypeColor(String type) {
        if ("Invitation".equals(type)) {
            return new Color(25, 135, 84);
        }


        if ("Recommendation".equals(type)) {
            return new Color(150, 100, 0);
        }


        return new Color(0, 100, 210);
    }


    private static String safeText(
            String value,
            String fallback
    ) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }


    // ================= RESULT MODELS =================


    private static class FeedItem {
        String title;
        String message;
        String type;
        String postedBy;
        Timestamp postedAt;
    }


    private static class FeedResult {
        String fullName;
        String role;
        String notice;


        final List<FeedItem> items = new ArrayList<>();
    }
}