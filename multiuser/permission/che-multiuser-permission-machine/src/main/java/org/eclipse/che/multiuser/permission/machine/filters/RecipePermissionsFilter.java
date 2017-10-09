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
package org.eclipse.che.multiuser.permission.machine.filters;

import static org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain.DELETE;
import static org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain.READ;
import static org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain.SEARCH;
import static org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain.UPDATE;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link RecipeService} by users' permissions
 *
 * <p>Filter should contain rules for protecting of all methods of {@link RecipeService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/recipe{path:(?!/script)(/.*)?}")
public class RecipePermissionsFilter extends CheMethodInvokerFilter {
  @Override
  public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments)
      throws ForbiddenException, ServerException {
    final String methodName = genericResourceMethod.getMethod().getName();

    final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    String action;
    String recipeId;

    switch (methodName) {
      case "getRecipe":
      case "getRecipeScript":
        recipeId = ((String) arguments[0]);
        action = READ;

        if (currentSubject.hasPermission(DOMAIN_ID, recipeId, SEARCH)) {
          //allow to read recipe if user has 'search' permission
          return;
        }
        break;

      case "updateRecipe":
        RecipeUpdate recipeUpdate = (RecipeUpdate) arguments[0];
        recipeId = recipeUpdate.getId();
        action = UPDATE;
        break;

      case "removeRecipe":
        recipeId = ((String) arguments[0]);
        action = DELETE;
        break;

      case "createRecipe":
      case "searchRecipes":
        //available for all
        return;
      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }

    if (!currentSubject.hasPermission(DOMAIN_ID, recipeId, action)) {
      throw new ForbiddenException(
          "The user does not have permission to " + action + " recipe with id '" + recipeId + "'");
    }
  }
}
