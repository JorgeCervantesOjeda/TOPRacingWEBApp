// src/test/java/Model/ParticipantAccessStatusTest.java
// Verifies participant access status separation between legacy defaults and exclusions.
package Model;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import Tables.Participant;
import org.junit.jupiter.api.Test;

class ParticipantAccessStatusTest {

  @Test
  void getValidParticipantAllowsLegacyDefaulterWithoutGlobalExclusion() {
    ModelBean model = spy( new ModelBean() );
    Participant loginAttempt = participant( 10L,
                                            "pw" );
    Participant persisted = participant( 10L,
                                         "pw" );
    persisted.setDefaulter( 1 );

    doReturn( persisted ).when( model )
      .getParticipantByEmail( loginAttempt );
    doReturn( false ).when( model )
      .hasActiveGlobalExclusion( persisted );

    Participant valid = model.getValidParticipant( loginAttempt );

    assertSame( persisted,
                valid );
  }

  @Test
  void getValidParticipantRejectsActiveGlobalExclusion() {
    ModelBean model = spy( new ModelBean() );
    Participant loginAttempt = participant( 11L,
                                            "pw" );
    Participant persisted = participant( 11L,
                                         "pw" );

    doReturn( persisted ).when( model )
      .getParticipantByEmail( loginAttempt );
    doReturn( true ).when( model )
      .hasActiveGlobalExclusion( persisted );

    Participant valid = model.getValidParticipant( loginAttempt );

    assertNull( valid );
  }

  private static Participant participant( Long id,
                                          String password ) {
    Participant participant = new Participant();
    participant.setId( id );
    participant.setEmail( "user" + id + "@example.com" );
    participant.setPassword( password );
    return participant;
  }
}
