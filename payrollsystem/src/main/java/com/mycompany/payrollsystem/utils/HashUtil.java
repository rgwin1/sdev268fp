package com.mycompany.payrollsystem.utils;
import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {

    // hash password with salt
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    // verify raw input against hashed password
    public static boolean verifyPassword(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }
}
