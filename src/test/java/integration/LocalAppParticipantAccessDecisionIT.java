// src/test/java/integration/LocalAppParticipantAccessDecisionIT.java
// Verifies persistent access decision records for participant restrictions.
package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Model.ModelBean;
import Tables.Participant;
import Tables.ParticipantAccessDecisionRecord;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppParticipantAccessDecisionIT {

  @Test
  void globalExclusionAndResolutionCreateDecisionRecords() {
    ModelBean model = new ModelBean();
    Participant actor = createSavedParticipant( model,
                                                "access-actor" );
    Participant target = createSavedParticipant( model,
                                                 "access-target" );

    model.recordGlobalExclusion( target,
                                 actor,
                                 "admin-confirmed-global-exclusion" );
    model.resolveActiveGlobalExclusions( target,
                                         actor );

    List<ParticipantAccessDecisionRecord> records =
      model.getParticipantAccessDecisionRecords( target );

    assertEquals( 2,
                  records.size() );
    assertTrue( records.stream()
      .anyMatch( record
        -> ParticipantAccessDecisionRecord.EVENT_GLOBAL_EXCLUSION_CREATED.equals(
          record.getEventType() )
           && record.getTargetParticipant().getId().equals( target.getId() )
           && record.getActorParticipant().getId().equals( actor.getId() )
           && "admin-confirmed-global-exclusion".equals( record.getReason() )
           && "GLOBAL_EXCLUSION_ACTIVE".equals( record.getEffect() ) ) );
    assertTrue( records.stream()
      .anyMatch( record
        -> ParticipantAccessDecisionRecord.EVENT_GLOBAL_EXCLUSION_RESOLVED.equals(
          record.getEventType() )
           && record.getTargetParticipant().getId().equals( target.getId() )
           && record.getActorParticipant().getId().equals( actor.getId() )
           && "GLOBAL_EXCLUSION_INACTIVE".equals( record.getEffect() ) ) );
  }

  @Test
  void localDefaultCreatesPromoterScopedDecisionRecords() {
    ModelBean model = new ModelBean();
    Participant actor = createSavedParticipant( model,
                                                "local-actor" );
    Participant target = createSavedParticipant( model,
                                                 "local-target" );
    Participant promoter = createSavedParticipant( model,
                                                   "local-promoter" );

    model.setParticipantAsLocalDefaulter( target,
                                          promoter,
                                          actor,
                                          "local-default-for-delivery" );

    List<ParticipantAccessDecisionRecord> records =
      model.getParticipantAccessDecisionRecords( target );

    assertTrue( records.stream()
      .anyMatch( record
        -> ParticipantAccessDecisionRecord.EVENT_LOCAL_DEFAULT_CREATED.equals(
          record.getEventType() )
           && record.getPromoter().getId().equals( promoter.getId() )
           && "LOCAL_DEFAULT_ACTIVE".equals( record.getEffect() ) ) );
    assertTrue( records.stream()
      .anyMatch( record
        -> ParticipantAccessDecisionRecord.EVENT_LOCAL_BLOCK_CREATED.equals(
          record.getEventType() )
           && record.getPromoter().getId().equals( promoter.getId() )
           && "LOCAL_BLOCK_ACTIVE".equals( record.getEffect() ) ) );
  }

  private static Participant createSavedParticipant( ModelBean model,
                                                     String label ) {
    String unique = label + "-" + UUID.randomUUID();
    Participant participant = model.createParticipant();
    participant.setPassword( "Access-123" );
    participant.setNamesGiven( "Codex" );
    participant.setNamesFamily( unique );
    participant.setEmail( unique + "@example.com" );
    participant.setPhone( "5555555555" );
    participant.setConfirmed( true );
    participant.acceptCurrentTerms();
    participant.setDefaulter( 0 );
    return model.save( participant,
                       false );
  }
}
