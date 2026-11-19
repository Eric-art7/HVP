public class OfficersPanel extends CrudPanel {

    public OfficersPanel() {
        super(
            "Manage Officers",
            """
            SELECT o.officer_id, o.officer_number, o.first_name, o.last_name,
                   o.position, d.department_name, o.email, o.phone,
                   o.appointment_date, o.is_active
            FROM officers o
            LEFT JOIN departments d ON o.department_id = d.department_id
            """,
            new String[]{"ID", "Officer No", "First Name", "Last Name", "Position",
                         "Department", "Email", "Phone", "Appointed", "Active"},
            new String[]{"Officer Number", "First Name", "Last Name", "Position",
                         "Department ID", "Email", "Phone",
                         "Appointment Date (YYYY-MM-DD)"},
            """
            INSERT INTO officers
            (officer_number, first_name, last_name, position, department_id,
             email, phone, appointment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            "DELETE FROM officers WHERE officer_id = ?",
            """
            SELECT o.officer_id, o.officer_number, o.first_name, o.last_name,
                   o.position, d.department_name, o.email, o.phone,
                   o.appointment_date, o.is_active
            FROM officers o
            LEFT JOIN departments d ON o.department_id = d.department_id
            """
        );
    }
}