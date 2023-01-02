package tomcat.utility;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Encryptor {
    private static final Random RANDOM = new Random();

    public static String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String res = no.toString(16);

            while (res.length() < 32) {
                res = "0" + res;
            }

            return res;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int generateRandom() {
        return RANDOM.nextInt();
    }
}
