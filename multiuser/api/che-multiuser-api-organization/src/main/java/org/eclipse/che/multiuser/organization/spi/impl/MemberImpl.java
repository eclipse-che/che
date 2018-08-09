/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.spi.impl;

import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.shared.model.Member;

/**
 * Data object for {@link Member}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Member")
@NamedQueries({
  @NamedQuery(
      name = "Member.getMember",
      query =
          "SELECT m "
              + "FROM Member m "
              + "WHERE m.userId = :userId AND m.organizationId = :organizationId"),
  @NamedQuery(
      name = "Member.getByOrganization",
      query = "SELECT m " + "FROM Member m " + "WHERE m.organizationId = :organizationId"),
  @NamedQuery(
      name = "Member.getCountByOrganizationId",
      query = "SELECT COUNT(m) " + "FROM Member m " + "WHERE m.organizationId = :organizationId"),
  @NamedQuery(
      name = "Member.getByUser",
      query = "SELECT m " + "FROM Member m " + "WHERE m.userId = :userId"),
  @NamedQuery(
      name = "Member.getOrganizations",
      query = "SELECT org " + "FROM Member m, m.organization org " + "WHERE m.userId = :userId"),
  @NamedQuery(
      name = "Member.getOrganizationsCount",
      query = "SELECT COUNT(m) " + "FROM Member m " + "WHERE m.userId = :userId ")
})
@Table(name = "che_member")
public class MemberImpl extends AbstractPermissions implements Member {
  @Column(name = "organization_id")
  private String organizationId;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "actions")
  @CollectionTable(name = "che_member_actions", joinColumns = @JoinColumn(name = "member_id"))
  protected List<String> actions;

  @ManyToOne
  @JoinColumn(
      name = "organization_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false)
  private OrganizationImpl organization;

  public MemberImpl() {}

  public MemberImpl(String userId, String organizationId, List<String> actions) {
    super(userId);
    this.organizationId = organizationId;
    if (actions != null) {
      this.actions = actions;
    }
  }

  public MemberImpl(Member member) {
    this(member.getUserId(), member.getOrganizationId(), member.getActions());
  }

  @Override
  public String getInstanceId() {
    return organizationId;
  }

  @Override
  public String getDomainId() {
    return OrganizationDomain.DOMAIN_ID;
  }

  @Override
  public List<String> getActions() {
    return actions;
  }

  @Override
  public String getOrganizationId() {
    return organizationId;
  }

  @Override
  public String toString() {
    return "MemberImpl{"
        + "userId='"
        + userId
        + '\''
        + ", organizationId='"
        + organizationId
        + '\''
        + ", actions="
        + actions
        + '}';
  }
}
