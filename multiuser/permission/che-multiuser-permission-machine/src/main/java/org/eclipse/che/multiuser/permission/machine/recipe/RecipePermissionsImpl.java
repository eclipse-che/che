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
package org.eclipse.che.multiuser.permission.machine.recipe;

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
import org.eclipse.che.api.recipe.OldRecipeImpl;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Recipe permissions data object.
 *
 * @author Max Shaposhnik
 */
@Entity(name = "RecipePermissions")
@NamedQueries({
  @NamedQuery(
    name = "RecipePermissions.getByRecipeId",
    query =
        "SELECT recipePermission "
            + "FROM RecipePermissions recipePermission "
            + "WHERE recipePermission.recipeId = :recipeId "
  ),
  @NamedQuery(
    name = "RecipePermissions.getCountByRecipeId",
    query =
        "SELECT COUNT(recipePermission) "
            + "FROM RecipePermissions recipePermission "
            + "WHERE recipePermission.recipeId = :recipeId "
  ),
  @NamedQuery(
    name = "RecipePermissions.getByUserId",
    query =
        "SELECT recipePermission "
            + "FROM RecipePermissions recipePermission "
            + "WHERE recipePermission.userId = :userId "
  ),
  @NamedQuery(
    name = "RecipePermissions.getByUserAndRecipeId",
    query =
        "SELECT recipePermission "
            + "FROM RecipePermissions recipePermission "
            + "WHERE recipePermission.recipeId = :recipeId "
            + "AND recipePermission.userId = :userId"
  ),
  @NamedQuery(
    name = "RecipePermissions.getByRecipeIdPublic",
    query =
        "SELECT recipePermission "
            + "FROM RecipePermissions recipePermission "
            + "WHERE recipePermission.recipeId = :recipeId "
            + "AND recipePermission.userId IS NULL "
  )
})
@Table(name = "che_recipe_permissions")
public class RecipePermissionsImpl extends AbstractPermissions {

  @Column(name = "recipe_id")
  private String recipeId;

  @ManyToOne
  @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
  private OldRecipeImpl recipe;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "actions")
  @CollectionTable(
    name = "che_recipe_permissions_actions",
    joinColumns = @JoinColumn(name = "recipe_permissions_id")
  )
  protected List<String> actions;

  public RecipePermissionsImpl() {}

  public RecipePermissionsImpl(Permissions permissions) {
    this(permissions.getUserId(), permissions.getInstanceId(), permissions.getActions());
  }

  public RecipePermissionsImpl(String userId, String instanceId, List<String> allowedActions) {
    super(userId);
    this.recipeId = instanceId;
    if (allowedActions != null) {
      this.actions = new ArrayList<>(allowedActions);
    }
  }

  @Override
  public String getInstanceId() {
    return recipeId;
  }

  @Override
  public String getDomainId() {
    return RecipeDomain.DOMAIN_ID;
  }

  @Override
  public List<String> getActions() {
    return actions;
  }

  @Override
  public String toString() {
    return "RecipePermissionsImpl{"
        + "userId='"
        + getUserId()
        + '\''
        + ", recipeId='"
        + recipeId
        + '\''
        + ", actions="
        + actions
        + '}';
  }
}
