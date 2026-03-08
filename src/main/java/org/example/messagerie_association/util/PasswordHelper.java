package org.example.messagerie_association.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHelper {

    private static final int SALT_LEN = 16;
    private static final String ALG = "SHA-256";

    public static String hash(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            byte[] salt = new byte[SALT_LEN];
            new SecureRandom().nextBytes(salt);
            byte[] hash = digest(plainPassword.getBytes(StandardCharsets.UTF_8), salt);
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }

    public static boolean verify(String plainPassword, String stored) {
        if (plainPassword == null || stored == null) return false;
        if (!stored.contains("$")) return plainPassword.equals(stored);
        try {
            String[] parts = stored.split("\\$", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = digest(plainPassword.getBytes(StandardCharsets.UTF_8), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] digest(byte[] password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALG);
        md.update(salt);
        return md.digest(password);
    }
}
