// src/main/java/Tables/ParticipantLocalRestriction.java
// Stores active and resolved participant restrictions scoped to one promoter.
package Tables;

import java.util.Date;

public class ParticipantLocalRestriction implements java.io.Serializable {

  public static final String KIND_LOCAL_DEFAULT = "LOCAL_DEFAULT";
  public static final String KIND_LOCAL_BLOCK = "LOCAL_BLOCK";

  private Long id;
  private Participant participant;
  private Participant promoter;
  private String kind;
  private boolean active;
  private String reason;
  private Date createdAt;
  private Date resolvedAt;
  private Participant createdByParticipant;
  private Participant resolvedByParticipant;

  public ParticipantLocalRestriction() {
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

  public Participant getPromoter() {
    return promoter;
  }

  public void setPromoter( Participant promoter ) {
    this.promoter = promoter;
  }

  public String getKind() {
    return kind;
  }

  public void setKind( String kind ) {
    this.kind = kind;
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
