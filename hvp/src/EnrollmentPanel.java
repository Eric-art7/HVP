public class EnrollmentPanel extends CrudPanel {

    public EnrollmentPanel() {
        super(
            "Manage Enrollments",
            """
            SELECT e.enrollment_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   sub.subject_name, c.class_name, e.enrollment_date
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            JOIN classes c ON e.class_id = c.class_id
            """,
            new String[]{"ID", "Admission No", "Student", "Subject", "Class", "Enrolled On"},
            new String[]{"Student ID", "Subject ID", "Class ID",
                         "Enrollment Date (YYYY-MM-DD)"},
            """
            INSERT INTO enrollments
            (student_id, subject_id, class_id, enrollment_date)
            VALUES (?, ?, ?, ?)
            """,
            "DELETE FROM enrollments WHERE enrollment_id = ?",
            """
            SELECT e.enrollment_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   sub.subject_name, c.class_name, e.enrollment_date
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            JOIN classes c ON e.class_id = c.class_id
            """
        );
    }
}