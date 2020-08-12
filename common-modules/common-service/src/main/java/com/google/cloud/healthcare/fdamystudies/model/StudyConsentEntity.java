/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.model;

import com.google.cloud.healthcare.fdamystudies.common.ColumnConstraints;
import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Setter
@Getter
@Entity
@Table(name = "study_consent")
@ConditionalOnProperty(
    value = "participant.manager.entities.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class StudyConsentEntity implements Serializable {

  private static final long serialVersionUID = 6218229749598633153L;

  @ToString.Exclude
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  @Column(
      name = "study_consent_id",
      updatable = false,
      nullable = false,
      length = ColumnConstraints.ID_LENGTH)
  private String id;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "study_info_id")
  private StudyEntity study;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "user_details_id")
  private UserDetailsEntity userDetails;

  @Column(name = "status", length = ColumnConstraints.SMALL_LENGTH)
  private String status;

  @Column(name = "version", length = ColumnConstraints.SMALL_LENGTH)
  private String version;

  @Column(name = "pdf")
  @Type(type = "text")
  private String pdf;

  @Column(name = "pdfpath", length = ColumnConstraints.LARGE_LENGTH)
  private String pdfPath;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "participant_study_id")
  private ParticipantStudyEntity participantStudy;

  // represents whether pdf content is stored in db=0 or gcp=1
  @Column(name = "pdfStorage", nullable = false)
  private int pdfStorage;

  @Column(
      name = "created_on",
      insertable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp created;
}
