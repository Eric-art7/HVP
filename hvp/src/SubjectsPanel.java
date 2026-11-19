public class SubjectsPanel extends CrudPanel {

    public SubjectsPanel() {
        super(
            "Manage Subjects",
            """
            SELECT subject_id, subject_name, subject_code, teacher_id
            FROM subjects
            """,
            new String[]{"ID", "Subject Name", "Subject Code", "Teacher ID"},
            new String[]{"Subject Name", "Subject Code", "Teacher ID"},
            "INSERT INTO subjects (subject_name, subject_code, teacher_id) VALUES (?, ?, ?)",
            "DELETE FROM subjects WHERE subject_id = ?",
            """
            SELECT subject_id, subject_name, subject_code, teacher_id
            FROM subjects
            """
        );
    }
}