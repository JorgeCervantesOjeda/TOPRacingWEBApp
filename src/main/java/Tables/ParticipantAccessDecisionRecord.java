// src/main/java/Tables/ParticipantAccessDecisionRecord.java
// Stores verifiable records for participant access decisions and resolutions.
package Tables;

import java.util.Date;

public class ParticipantAccessDecisionRecord implements java.io.Serializable {

  public static final String EVENT_GLOBAL_EXCLUSION_CREATED =
    "GLOBAL_EXCLUSION_CREATED";
  public static final String EVENT_GLOBAL_EXCLUSION_RESOLVED =
    "GLOBAL_EXCLUSION_RESOLVED";
  public static final String EVENT_LOCAL_DEFAULT_CREATED =
    "LOCAL_DEFAULT_CREATED";
  public static final String EVENT_LOCAL_DEFAULT_RESOLVED =
    "LOCAL_DEFAULT_RESOLVED";
  public static final String EVENT_LOCAL_BLOCK_CREATED =
    "LOCAL_BLOCK_CREATED";
  public static final String EVENT_LOCAL_BLOCK_RESOLVED =
    "LOCAL_BLOCK_RESOLVED";

  private Long id;
  private String eventType;
  private Participant actorParticipant;
  private Participant targetParticipant;
  private Participant promoter;
  private Regatta regatta;
  private Registration registration;
  private ParticipantGlobalExclusion globalExclusion;
  private ParticipantLocalRestriction localRestriction;
  private String reason;
  private String effect;
  private Date createdAt;

  public ParticipantAccessDecisionRecord() {
  }

  public Long getId() {
    return id;
  }

  public void setId( Long id ) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType( String eventType ) {
    this.eventType = eventType;
  }

  public Participant getActorParticipant() {
    return actorParticipant;
  }

  public void setActorParticipant( Participant actorParticipant ) {
    this.actorParticipant = actorParticipant;
  }

  public Participant getTargetParticipant() {
    return targetParticipant;
  }

  public void setTargetParticipant( Participant targetParticipant ) {
    this.targetParticipant = targetParticipant;
  }

  public Participant getPromoter() {
    return promoter;
  }

  public void setPromoter( Participant promoter ) {
    this.promoter = promoter;
  }

  public Regatta getRegatta() {
    return regatta;
  }

  public void setRegatta( Regatta regatta ) {
    this.regatta = regatta;
  }

  public Registration getRegistration() {
    return registration;
  }

  public void setRegistration( Registration registration ) {
    this.registration = registration;
  }

  public ParticipantGlobalExclusion getGlobalExclusion() {
    return globalExclusion;
  }

  public void setGlobalExclusion( ParticipantGlobalExclusion globalExclusion ) {
    this.globalExclusion = globalExclusion;
  }

  public ParticipantLocalRestriction getLocalRestriction() {
    return localRestriction;
  }

  public void setLocalRestriction( ParticipantLocalRestriction localRestriction ) {
    this.localRestriction = localRestriction;
  }

  public String getReason() {
    return reason;
  }

  public void setReason( String reason ) {
    this.reason = reason;
  }

  public String getEffect() {
    return effect;
  }

  public void setEffect( String effect ) {
    this.effect = effect;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt( Date createdAt ) {
    this.createdAt = createdAt;
  }
}
