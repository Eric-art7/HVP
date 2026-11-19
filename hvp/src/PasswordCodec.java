
/**
 * PasswordCodec provides password hashing for accounts created or
 * updated through the Admin Parent Management screen.
 *
 * It delegates to LoginApp.PasswordUtil so that every password uses
 * exactly the same PBKDF2 algorithm, salt format, iteration count,
 * key length, and hash format as the application login system.
 */
public final class PasswordCodec {

    /**
     * Private constructor prevents creation of PasswordCodec objects.
     * This class is intended to be used only through its static method.
     */
    private PasswordCodec() {
    }

    /**
     * Hashes a plain-text password using the application's existing
     * PBKDF2WithHmacSHA256 implementation.
     *
     * The returned values must be stored as follows:
     * - index 0 in users.password_hash
     * - index 1 in users.password_salt
     *
     * @param password the plain-text password entered by the administrator
     * @return a two-element String array:
     *         index 0 contains the password hash
     *         index 1 contains the password salt
     * @throws IllegalArgumentException if the password is null or empty
     * @throws RuntimeException if password hashing fails
     */
    public static String[] hash(String password) {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or empty."
            );
        }

        /*
         * Generate a new random salt using the same method used by
         * the application's signup and login system.
         */
        String salt = LoginApp.PasswordUtil.generateSalt();

        /*
         * Hash the password using the same PBKDF2WithHmacSHA256
         * algorithm, iteration count, key length, and Base64 encoding
         * used by LoginApp.PasswordUtil.verifyPassword(...).
         */
        String hash = LoginApp.PasswordUtil.hashPassword(
                password,
                salt
        );

        /*
         * Return both values so AdminParentManagementPanel can store:
         * hashed[0] in users.password_hash
         * hashed[1] in users.password_salt
         */
        return new String[]{
                hash,
                salt
        };
    }
}