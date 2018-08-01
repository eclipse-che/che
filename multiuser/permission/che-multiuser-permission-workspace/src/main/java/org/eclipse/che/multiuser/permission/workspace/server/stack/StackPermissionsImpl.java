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
package org.eclipse.che.multiuser.permission.workspace.server.stack;

import java.util.ArrayList;
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
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Stack permissions data object.
 *
 * @author Max Shaposhnik
 */
@Entity(name = "StackPermissions")
@NamedQueries({
  @NamedQuery(
    name = "StackPermissions.getByStackId",
    query = "SELECT stack " + "FROM StackPermissions stack " + "WHERE stack.stackId = :stackId "
  ),
  @NamedQuery(
    name = "StackPermissions.getCountByStackId",
    query =
        "SELECT COUNT(stack) " + "FROM StackPermissions stack " + "WHERE stack.stackId = :stackId "
  ),
  @NamedQuery(
    name = "StackPermissions.getByUserId",
    query = "SELECT stack " + "FROM StackPermissions stack " + "WHERE stack.userId = :userId "
  ),
  @NamedQuery(
    name = "StackPermissions.getByUserAndStackId",
    query =
        "SELECT stack "
            + "FROM StackPermissions stack "
            + "WHERE stack.stackId = :stackId "
            + "AND stack.userId = :userId "
  ),
  @NamedQuery(
    name = "StackPermissions.getByStackIdPublic",
    query =
        "SELECT stack "
            + "FROM StackPermissions stack "
            + "WHERE stack.stackId = :stackId "
            + "AND stack.userId IS NULL "
  )
})
@Table(name = "che_stack_permissions")
public class StackPermissionsImpl extends AbstractPermissions {

  @Column(name = "stack_id")
  private String stackId;

  @ManyToOne
  @JoinColumn(name = "stack_id", insertable = false, updatable = false)
  private StackImpl stack;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "actions")
  @CollectionTable(
    name = "che_stack_permissions_actions",
    joinColumns = @JoinColumn(name = "stack_permissions_id")
  )
  protected List<String> actions;

  public StackPermissionsImpl() {}

  public StackPermissionsImpl(Permissions permissions) {
    this(permissions.getUserId(), permissions.getInstanceId(), permissions.getActions());
  }

  public StackPermissionsImpl(String userId, String instanceId, List<String> allowedActions) {
    super(userId);
    this.stackId = instanceId;
    if (allowedActions != null) {
      this.actions = new ArrayList<>(allowedActions);
    }
  }

  @Override
  public String getInstanceId() {
    return stackId;
  }

  @Override
  public String getDomainId() {
    return StackDomain.DOMAIN_ID;
  }

  @Override
  public List<String> getActions() {
    return actions;
  }

  @Override
  public String toString() {
    return "StackPermissionsImpl{"
        + "userId='"
        + getUserId()
        + '\''
        + ", stackId='"
        + stackId
        + '\''
        + ", actions="
        + actions
        + '}';
  }
}
