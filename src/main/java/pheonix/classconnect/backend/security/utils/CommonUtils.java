package pheonix.classconnect.backend.security.utils;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommonUtils {

    public static String getRealRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if(ip == null) ip = request.getHeader("X-Forwarded-For");
        if(ip == null) ip = request.getHeader("Proxy-Client-IP");
        if(ip == null) ip = request.getHeader("WL-Proxy-Client-IP");
        if(ip == null) ip = request.getHeader("HTTP_CLIENT_IP");
        if(ip == null) ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if(ip == null) ip = request.getRemoteAddr();

        return ip;
    }

    public static Map<String, String> genAuthMap(
            char[] secretCharArray, String userNo) {

        Long timeValue = System.currentTimeMillis() / 10000;
        String keyPhrase = timeValue + userNo;
        String auth = hmacSHA1(secretCharArray, keyPhrase);

        Map<String, String> values = new HashMap<>();
        values.put("auth", auth);
        values.put("timestamp", timeValue.toString());
        values.put("userId", userNo);

        return values;
    }

    public static Map<String, String> genAuthMap(
            char[] secretCharArray, String userNo, String timestamp) {

        Long timeValue = Long.parseLong(timestamp) / 1000;
        String keyPhrase = timeValue + userNo;
        String auth = hmacSHA1(secretCharArray, keyPhrase);

        Map<String, String> values = new HashMap<>();
        values.put("auth", auth);
        values.put("timestamp", timeValue.toString());
        values.put("userId", userNo);

        return values;
    }

    private static String hmacSHA1(char[] secretCharArray, String keyPhrase) {
        String digest;
        String type = "HmacSHA1";
        try {
            SecretKeySpec key = new SecretKeySpec(
                    (Arrays.toString(secretCharArray)).getBytes(StandardCharsets.UTF_8), type);
            Mac mac = Mac.getInstance(type);
            mac.init(key);

            byte[] bytes = mac.doFinal(keyPhrase.getBytes(StandardCharsets.US_ASCII));

            StringBuilder hash = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }

            digest = hash.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("fail to get MAC" + e);
        }

        return digest;
    }

}
