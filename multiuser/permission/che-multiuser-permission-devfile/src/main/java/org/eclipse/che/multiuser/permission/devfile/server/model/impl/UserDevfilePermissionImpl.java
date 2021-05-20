/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain;
import org.eclipse.che.multiuser.permission.devfile.server.model.UserDevfilePermission;

/** Data object for {@link UserDevfilePermission} */
@Entity(name = "UserDevfilePermission")
@NamedQueries({
  @NamedQuery(
      name = "UserDevfilePermission.getByUserDevfileId",
      query =
          "SELECT permission "
              + "FROM UserDevfilePermission permission "
              + "WHERE permission.userDevfileId = :userDevfileId "),
  @NamedQuery(
      name = "UserDevfilePermission.getCountByUserDevfileId",
      query =
          "SELECT COUNT(permission) "
              + "FROM UserDevfilePermission permission "
              + "WHERE permission.userDevfileId = :userDevfileId "),
  @NamedQuery(
      name = "UserDevfilePermission.getByUserId",
      query =
          "SELECT permission "
              + "FROM  UserDevfilePermission permission "
              + "WHERE permission.userId = :userId "),
  @NamedQuery(
      name = "UserDevfilePermission.getByUserAndUserDevfileId",
      query =
          "SELECT permission  "
              + "FROM UserDevfilePermission permission "
              + "WHERE permission.userId = :userId "
              + "AND permission.userDevfileId = :userDevfileId ",
      hints = {@QueryHint(name = "eclipselink.query-results-cache", value = "true")})
})
@Table(name = "che_userdevfile_permissions")
public class UserDevfilePermissionImpl extends AbstractPermissions
    implements UserDevfilePermission {

  @Column(name = "userdevfile_id")
  private String userDevfileId;

  @ManyToOne
  @JoinColumn(name = "userdevfile_id", insertable = false, updatable = false)
  private UserDevfileImpl userDevfile;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "actions")
  @CollectionTable(
      name = "che_userdevfile_permissions_actions",
      joinColumns = @JoinColumn(name = "userdevfile_permissions_id"))
  protected List<String> actions;

  public UserDevfilePermissionImpl() {}

  public UserDevfilePermissionImpl(String userDevfileId, String userId, List<String> actions) {
    super(userId);
    this.userDevfileId = userDevfileId;
    if (actions != null) {
      this.actions = new ArrayList<>(actions);
    }
  }

  public UserDevfilePermissionImpl(UserDevfilePermission userDevfilePermission) {
    this(
        userDevfilePermission.getUserDevfileId(),
        userDevfilePermission.getUserId(),
        userDevfilePermission.getActions());
  }

  @Override
  public String getInstanceId() {
    return userDevfileId;
  }

  @Override
  public String getDomainId() {
    return UserDevfileDomain.DOMAIN_ID;
  }

  @Override
  public List<String> getActions() {
    return actions;
  }

  @Override
  public String getUserDevfileId() {
    return userDevfileId;
  }

  @Override
  public String toString() {
    return "UserDevfilePermissionImpl{"
        + "userDevfileId='"
        + userDevfileId
        + '\''
        + ", userDevfile="
        + userDevfile
        + ", actions="
        + actions
        + "} "
        + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    UserDevfilePermissionImpl that = (UserDevfilePermissionImpl) o;
    return Objects.equals(userDevfileId, that.userDevfileId)
        && Objects.equals(actions, that.actions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), userDevfileId, actions);
  }
}
