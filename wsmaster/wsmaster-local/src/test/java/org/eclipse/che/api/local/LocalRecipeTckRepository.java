/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Anton Korneta
 */
public class LocalRecipeTckRepository implements TckRepository<RecipeImpl> {

    @Inject
    private LocalRecipeDaoImpl recipeDao;

    @Override
    public void createAll(Collection<? extends RecipeImpl> recipes) throws TckRepositoryException {
        for (RecipeImpl recipe : recipes) {
            recipeDao.recipes.put(recipe.getId(), new RecipeImpl(recipe));
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        recipeDao.recipes.clear();
    }
}
