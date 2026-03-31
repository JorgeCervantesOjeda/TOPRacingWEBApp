package Model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordGeneratorTest {

  @Test
  void generatePasswordIncludesExpectedCharacterClasses() {
    PasswordGenerator generator = new PasswordGenerator( 8,
                                                         12 );

    char[] password = generator.generatePassword();
    String value = new String( password );

    assertTrue( value.length() >= 8 && value.length() <= 12 );
    assertTrue( value.chars().anyMatch( Character::isUpperCase ) );
    assertTrue( value.chars().anyMatch( Character::isLowerCase ) );
    assertTrue( value.chars().anyMatch( Character::isDigit ) );
    assertTrue( value.chars().anyMatch( ch -> "()-_.".indexOf( ch ) >= 0 ) );
  }

  @Test
  void rejectsMinimumLengthBelowPresetRequirements() {
    assertThrows( IllegalArgumentException.class,
                  () -> new PasswordGenerator( 3,
                                              8 ) );
  }
}
