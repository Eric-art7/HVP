public class GradesPanel extends CrudPanel {

    public GradesPanel() {
        super(
            "Enter Grades",
            """
            SELECT g.grade_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   sub.subject_name, g.term, g.assessment_type,
                   g.score, g.max_score, g.grade_date
            FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            JOIN students s ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            """,
            new String[]{"ID", "Admission No", "Student", "Subject", "Term",
                         "Assessment", "Score", "Max Score", "Date"},
            new String[]{"Enrollment ID", "Term (Term 1/2/3)",
                         "Assessment (Test/Exam/Assignment/Project)",
                         "Score", "Max Score", "Grade Date (YYYY-MM-DD)"},
            """
            INSERT INTO grades
            (enrollment_id, term, assessment_type, score, max_score, grade_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            "DELETE FROM grades WHERE grade_id = ?",
            """
            SELECT g.grade_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   sub.subject_name, g.term, g.assessment_type,
                   g.score, g.max_score, g.grade_date
            FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            JOIN students s ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            """
        );
    }
}