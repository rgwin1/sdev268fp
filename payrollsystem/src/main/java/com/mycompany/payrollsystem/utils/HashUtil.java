package com.mycompany.payrollsystem.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * utility class for password hashing and verification.
 * uses BCrypt to securely hash and check passwords.
 */
public class HashUtil {

    /**
     * hashes a plain text password using BCrypt with a salt.
     *
     * @param plainText the raw password entered by the user
     * @return the hashed password string with embedded salt
     */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    /**
     * verifies a raw password against a stored BCrypt hash.
     *
     * @param plainText the raw password entered by the user
     * @param hashed the stored hashed password from the database
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }
}
