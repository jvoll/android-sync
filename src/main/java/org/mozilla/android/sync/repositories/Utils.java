package org.mozilla.android.sync.repositories;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public class Utils {
  
  public static String generateGuid() {
    Base64 base64 = new Base64(true);
    byte[] encodedBytes = base64.encode(generateRandomBytes(9));
    return new String(encodedBytes).replace("\r", "").replace("\n", "");
  }
  
  private static byte[] generateRandomBytes(int length) {
    byte[] bytes = new byte[length];
    Random random = new Random(System.nanoTime());
    random.nextBytes(bytes);
    return bytes;
  }
  
  // Returns the current epoch timestamp (in seconds)
  public static long currentEpoch() {
    return System.currentTimeMillis()/1000;
  }

}
