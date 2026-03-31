package Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CryptoTest {

  @Test
  void encryptAndDecryptRoundTrip() {
    Crypto crypto = new Crypto();
    String plainText = "local-test-secret";

    String encrypted = crypto.encryptString( plainText );

    assertNotEquals( plainText,
                     encrypted );
    assertEquals( plainText,
                  crypto.decryptString( encrypted ) );
  }
}
