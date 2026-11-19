public class AttendancePanel extends CrudPanel {

    public AttendancePanel() {
        super(
            "Student Attendance",
            """
            SELECT a.attendance_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   c.class_name, a.attendance_date, a.status, a.remarks
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN classes c ON a.class_id = c.class_id
            """,
            new String[]{"ID", "Admission No", "Student", "Class",
                         "Date", "Status", "Remarks"},
            new String[]{"Student ID", "Class ID", "Date (YYYY-MM-DD)",
                         "Status (Present/Absent/Late/Excused)", "Remarks"},
            """
            INSERT INTO attendance
            (student_id, class_id, attendance_date, status, remarks)
            VALUES (?, ?, ?, ?, ?)
            """,
            "DELETE FROM attendance WHERE attendance_id = ?",
            """
            SELECT a.attendance_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   c.class_name, a.attendance_date, a.status, a.remarks
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN classes c ON a.class_id = c.class_id
            """
        );
    }
}