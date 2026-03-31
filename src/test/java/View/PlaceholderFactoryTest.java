package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import Tables.Venue;
import org.junit.jupiter.api.Test;

class PlaceholderFactoryTest {

  @Test
  void participantPlaceholderStartsAnonymousAndNonDefaulter() {
    Participant participant = PlaceholderFactory.participant();

    assertEquals( 0L,
                  participant.getId() );
    assertEquals( "",
                  participant.getNamesGiven() );
    assertEquals( "",
                  participant.getNamesFamily() );
    assertEquals( 0,
                  participant.getDefaulter() );
  }

  @Test
  void regattaPlaceholderBuildsNestedDefaults() {
    Regatta regatta = PlaceholderFactory.regatta();

    assertNotNull( regatta.getParticipant() );
    assertNotNull( regatta.getVariant() );
    assertNotNull( regatta.getCurrency() );
    assertEquals( 0L,
                  regatta.getParticipant().getId() );
  }

  @Test
  void registrationPlaceholderBuildsOwnershipGraph() {
    Registration registration = PlaceholderFactory.registration();
    Venue venue = registration.getRegatta().getVariant().getVenue();

    assertNotNull( registration.getCar() );
    assertNotNull( registration.getParticipantByIdOwner() );
    assertNotNull( registration.getParticipantByIdBuyer() );
    assertNotNull( venue );
    assertEquals( 0L,
                  venue.getId() );
  }
}
