public class StudentsPanel extends CrudPanel {

    public StudentsPanel() {
        super(
            "Manage Students",
            """
            SELECT student_id, admission_number, first_name, last_name,
                   gender, date_of_birth, class_id, guardian_name,
                   guardian_phone, enrollment_date
            FROM students
            """,
            new String[]{"ID", "Admission No", "First Name", "Last Name", "Gender",
                         "Date of Birth", "Class ID", "Guardian", "Guardian Phone", "Enrolled"},
            new String[]{"Admission Number", "First Name", "Last Name", "Gender",
                         "Date of Birth (YYYY-MM-DD)", "Class ID", "Guardian Name",
                         "Guardian Phone", "Enrollment Date (YYYY-MM-DD)"},
            """
            INSERT INTO students
            (admission_number, first_name, last_name, gender, date_of_birth,
             class_id, guardian_name, guardian_phone, enrollment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            "DELETE FROM students WHERE student_id = ?",
            """
            SELECT student_id, admission_number, first_name, last_name,
                   gender, date_of_birth, class_id, guardian_name,
                   guardian_phone, enrollment_date
            FROM students
            """
        );
    }
}