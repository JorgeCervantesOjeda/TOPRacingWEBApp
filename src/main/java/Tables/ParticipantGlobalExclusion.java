// src/main/java/Tables/ParticipantGlobalExclusion.java
// Stores active and resolved global participant exclusions.
package Tables;

import java.util.Date;

public class ParticipantGlobalExclusion implements java.io.Serializable {

  private Long id;
  private Participant participant;
  private boolean active;
  private String reason;
  private Date createdAt;
  private Date resolvedAt;
  private Participant createdByParticipant;
  private Participant resolvedByParticipant;

  public ParticipantGlobalExclusion() {
  }

  public Long getId() {
    return id;
  }

  public void setId( Long id ) {
    this.id = id;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant( Participant participant ) {
    this.participant = participant;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive( boolean active ) {
    this.active = active;
  }

  public String getReason() {
    return reason;
  }

  public void setReason( String reason ) {
    this.reason = reason;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt( Date createdAt ) {
    this.createdAt = createdAt;
  }

  public Date getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt( Date resolvedAt ) {
    this.resolvedAt = resolvedAt;
  }

  public Participant getCreatedByParticipant() {
    return createdByParticipant;
  }

  public void setCreatedByParticipant( Participant createdByParticipant ) {
    this.createdByParticipant = createdByParticipant;
  }

  public Participant getResolvedByParticipant() {
    return resolvedByParticipant;
  }

  public void setResolvedByParticipant( Participant resolvedByParticipant ) {
    this.resolvedByParticipant = resolvedByParticipant;
  }
}
