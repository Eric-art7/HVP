public class ClassesPanel extends CrudPanel {

    public ClassesPanel() {
        super(
            "Manage Classes",
            """
            SELECT c.class_id, c.class_name, a.academic_year,
                   a.start_date, a.end_date
            FROM classes c
            JOIN academic_years a ON c.academic_year_id = a.academic_year_id
            """,
            new String[]{"ID", "Class Name", "Academic Year", "Start Date", "End Date"},
            new String[]{"Class Name", "Academic Year ID"},
            "INSERT INTO classes (class_name, academic_year_id) VALUES (?, ?)",
            "DELETE FROM classes WHERE class_id = ?",
            """
            SELECT c.class_id, c.class_name, a.academic_year,
                   a.start_date, a.end_date
            FROM classes c
            JOIN academic_years a ON c.academic_year_id = a.academic_year_id
            """
        );
    }
}