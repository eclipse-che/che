/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.stack;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.api.permission.shared.model.Permissions;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

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
@Table(
  indexes = {@Index(columnList = "userId, stackId", unique = true), @Index(columnList = "stackId")}
)
public class StackPermissionsImpl extends AbstractPermissions {

  @Column(name = "stackid")
  private String stackId;

  @ManyToOne
  @JoinColumn(name = "stackid", insertable = false, updatable = false)
  private StackImpl stack;

  public StackPermissionsImpl() {}

  public StackPermissionsImpl(Permissions permissions) {
    this(permissions.getUserId(), permissions.getInstanceId(), permissions.getActions());
  }

  public StackPermissionsImpl(String userId, String instanceId, List<String> allowedActions) {
    super(userId, allowedActions);
    this.stackId = instanceId;
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
