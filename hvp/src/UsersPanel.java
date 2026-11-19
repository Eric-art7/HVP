public class UsersPanel extends CrudPanel {

    public UsersPanel() {
        super(
            "Manage Users",
            """
            SELECT user_id, username, email, full_name, role_id,
                   is_active, created_at
            FROM users
            """,
            new String[]{"ID", "Username", "Email", "Full Name", "Role ID", "Active", "Created"},
            new String[]{"Username", "Email", "Full Name", "Role ID"},
            "INSERT INTO users (username, email, full_name, role_id) VALUES (?, ?, ?, ?)",
            "DELETE FROM users WHERE user_id = ?",
            """
            SELECT user_id, username, email, full_name, role_id,
                   is_active, created_at
            FROM users
            """
        );
    }
}