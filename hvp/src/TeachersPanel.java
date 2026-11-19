public class TeachersPanel extends CrudPanel {

    public TeachersPanel() {
        super(
            "Manage Teachers",
            """
            SELECT teacher_id, teacher_number, first_name, last_name,
                   gender, email, phone, hire_date, is_active
            FROM teachers
            """,
            new String[]{"ID", "Teacher No", "First Name", "Last Name", "Gender",
                         "Email", "Phone", "Hire Date", "Active"},
            new String[]{"Teacher Number", "First Name", "Last Name", "Gender",
                         "Email", "Phone", "Hire Date (YYYY-MM-DD)"},
            """
            INSERT INTO teachers
            (teacher_number, first_name, last_name, gender, email, phone, hire_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            "DELETE FROM teachers WHERE teacher_id = ?",
            """
            SELECT teacher_id, teacher_number, first_name, last_name,
                   gender, email, phone, hire_date, is_active
            FROM teachers
            """
        );
    }
}