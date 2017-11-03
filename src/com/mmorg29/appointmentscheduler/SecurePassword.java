package com.mmorg29.appointmentscheduler;

import com.mmorg29.dbtools.DBManager;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author mam 
 * Class is used to create secure password hashes and validate user login information. 
 * Due to database limitations, in particular the size of the password field (40), security is not as strong as it should be. 
 * Passwords are created using PBKDF2WithHmacSHA1 algorithm from the javax.crypto package. 
 * All methods are static to allow for use with out creating an instance of this class.
 */
public class SecurePassword {

    public static void verifyUserAndPassword(String userName, String enteredPassword) throws InvalidLoginException {
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement();
                ResultSet rs = statement.executeQuery("SELECT * FROM " + DBManager.USER_TABLE + " WHERE userName = '" + userName + "'");) {
            //mam Make sure user is still active
            if (rs.first() && rs.getBoolean("active")) {
                //mam Always set User class data here to prevent requering database from LoginFormController class.
                //This is safe here because if login failure occurs user will not have access to main application,
                //and data will be set again when user retries login attempt.
                User.getInstance().setUserData(rs.getString("userName"), rs.getInt("userId"));
                String storedPassword = rs.getString(3);
                if(!checkSecurePassword(enteredPassword, storedPassword)){
                    throw new InvalidLoginException();
                }
            } else {
                throw new InvalidLoginException();
            }
        } catch (SQLException sqlEx) {
            throw new InvalidLoginException();
        }
    }

    public static String makeSecurePassword(String password) {
        int iterations = 1000;
        byte[] salt = getSalt();
        char[] chars = password.toCharArray();

        String passwordHash = securePasswordHash(iterations, salt, chars);
        String saltString = toHex(salt);
        return iterations + ":" + saltString + ":" + passwordHash;
    }

    private static boolean checkSecurePassword(String enteredPassword, String storedPassword) {
        String[] storedPasswordParts = storedPassword.split(":");
        //mam stored password was created from another program or method therefore contains only the password itself
        //so check if it matches the entered password
        if(storedPasswordParts.length == 1) {
            return storedPassword.equals(enteredPassword);
        }
        
        int isHashed = Integer.parseInt(storedPasswordParts[2]);
        if (isHashed == 1) {
            int iterations = Integer.parseInt(storedPasswordParts[0]);
            byte[] salt = fromHex(storedPasswordParts[1]);
            char[] enteredPasswordChars = enteredPassword.toCharArray();
            String enteredPasswordHash = securePasswordHash(iterations, salt, enteredPasswordChars);
            return (storedPasswordParts[2] + ":" + storedPasswordParts[3]).equals(enteredPasswordHash);
        } else {
            //mam password was not able to be hashed when saved to DB so just check if it matches the entered password
            return storedPasswordParts[3].equals(enteredPassword);
        }
    }

    private static String securePasswordHash(int iterations, byte[] salt, char[] password) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 64);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] passHash = secretKeyFactory.generateSecret(spec).getEncoded();
            return 1 + ":" + toHex(passHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            //mam 2017-08-10 Just return unsecure password with a 0 indicating no Hash was performed.
            String pword = new String(password);
            return 0 + ":" + pword;
        }
    }

    private static byte[] getSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] input) {
        StringBuilder stringBuilder = new StringBuilder(input.length * 2);
        Formatter formatter = new Formatter(stringBuilder);
        for (byte b : input) {
            formatter.format("%02x", b);
        }
        return stringBuilder.toString();
    }

    private static byte[] fromHex(String input) {
        byte[] output = new byte[input.length() / 2];
        for (int i = 0; i < input.length(); i += 2) {
            output[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4)
                    + Character.digit(input.charAt(i + 1), 16));
        }
        return output;
    }

}
