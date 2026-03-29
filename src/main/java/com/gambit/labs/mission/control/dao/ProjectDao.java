package com.gambit.labs.mission.control.dao;

import com.gambit.labs.mission.control.utils.ObjectUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ProjectDao {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "prefix")
  private String prefix;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_user_id")
  private UserDao assignedUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private MissionStatus status;

  @Column(name = "blocked_reason")
  private String blockedReason;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Instant dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Instant dateModified;

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
