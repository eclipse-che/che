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
package org.eclipse.che.api.machine.server.recipe;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;

/**
 * Domain for storing recipes' permissions
 *
 * @author Sergii Leschenko
 */
public class RecipeDomain extends AbstractPermissionsDomain<RecipePermissionsImpl> {
  public static final String DOMAIN_ID = "recipe";

  public static final String READ = "read";
  public static final String SEARCH = "search";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";

  private static final List<String> ACTIONS =
      ImmutableList.of(SET_PERMISSIONS, READ, SEARCH, UPDATE, DELETE);

  /** Returns all the available actions for {@link RecipeDomain}. */
  public static List<String> getActions() {
    return ACTIONS;
  }

  public RecipeDomain() {
    super(DOMAIN_ID, ACTIONS);
  }

  @Override
  public RecipePermissionsImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new RecipePermissionsImpl(userId, instanceId, allowedActions);
  }
}
