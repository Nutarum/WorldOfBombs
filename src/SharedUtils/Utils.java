
package SharedUtils;

import java.security.MessageDigest;

public class Utils {
           //Receive a string, and return its hash in sha256
    public static String hash(String password){
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(password.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] digest = sha256.digest();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<digest.length;i++){
            sb.append(String.format("%02x", digest[i]));
        }
        String hash=sb.toString();

        return hash;
    }
}
