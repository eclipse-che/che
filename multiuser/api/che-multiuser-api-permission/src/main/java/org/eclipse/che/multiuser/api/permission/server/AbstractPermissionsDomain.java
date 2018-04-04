/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;

/**
 * Abstract implementation for {@link PermissionsDomain}
 *
 * <p>Note: It supports "setPermission" by default
 *
 * @author Sergii Leschenko
 */
public abstract class AbstractPermissionsDomain<T extends AbstractPermissions>
    implements PermissionsDomain {
  public static final String SET_PERMISSIONS = "setPermissions";

  private final String id;
  private final List<String> allowedActions;
  private final boolean requiresInstance;

  protected AbstractPermissionsDomain(String id, List<String> allowedActions) {
    this(id, allowedActions, true);
  }

  protected AbstractPermissionsDomain(
      String id, List<String> allowedActions, boolean requiresInstance) {
    this.id = id;
    Set<String> resultActions = new HashSet<>(allowedActions);
    resultActions.add(SET_PERMISSIONS);
    this.allowedActions = ImmutableList.copyOf(resultActions);
    this.requiresInstance = requiresInstance;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<String> getAllowedActions() {
    return allowedActions;
  }

  @Override
  public Boolean isInstanceRequired() {
    return requiresInstance;
  }

  /**
   * Creates new instance of the entity related to this domain.
   *
   * @return new entity instance related to this domain
   * @throws IllegalArgumentException when instance id is null when it's required
   */
  public T newInstance(String userId, String instanceId, List<String> allowedActions) {
    if (isInstanceRequired() && instanceId == null) {
      throw new IllegalArgumentException("Given domain requires non nullable value for instanceId");
    }
    return doCreateInstance(userId, instanceId, allowedActions);
  }

  protected abstract T doCreateInstance(
      String userId, String instanceId, List<String> allowedActions);

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AbstractPermissionsDomain)) return false;
    final AbstractPermissionsDomain other = (AbstractPermissionsDomain) obj;
    return Objects.equals(id, other.id) && Objects.equals(allowedActions, other.allowedActions);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(allowedActions);
    return hash;
  }

  @Override
  public String toString() {
    return "PermissionsDomain{" + "id='" + id + '\'' + ", allowedActions=" + allowedActions + "}";
  }
}
