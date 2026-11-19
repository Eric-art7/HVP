public class PaymentsPanel extends CrudPanel {

    public PaymentsPanel() {
        super(
            "Student Payments",
            """
            SELECT p.payment_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   p.amount, p.payment_date, p.payment_method,
                   p.receipt_number, p.term, p.remarks
            FROM payments p
            JOIN students s ON p.student_id = s.student_id
            """,
            new String[]{"ID", "Admission No", "Student", "Amount", "Date",
                         "Method", "Receipt No", "Term", "Remarks"},
            new String[]{"Student ID", "Amount", "Payment Date (YYYY-MM-DD)",
                         "Payment Method", "Receipt Number", "Term", "Remarks"},
            """
            INSERT INTO payments
            (student_id, amount, payment_date, payment_method,
             receipt_number, term, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            "DELETE FROM payments WHERE payment_id = ?",
            """
            SELECT p.payment_id, s.admission_number,
                   CONCAT(s.first_name, ' ', s.last_name) AS student,
                   p.amount, p.payment_date, p.payment_method,
                   p.receipt_number, p.term, p.remarks
            FROM payments p
            JOIN students s ON p.student_id = s.student_id
            """
        );
    }
}