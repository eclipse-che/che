/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.recipe;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.machine.shared.ManagedOldRecipe;
import org.eclipse.che.core.db.cascade.event.PersistEvent;

/**
 * Published after recipe instance is persisted.
 *
 * @author Anton Korneta.
 */
@EventOrigin("recipe")
public class RecipePersistedEvent extends PersistEvent {
    private final ManagedOldRecipe recipe;

    public RecipePersistedEvent(ManagedOldRecipe recipe) {
        this.recipe = recipe;
    }

    public ManagedOldRecipe getRecipe() {
        return recipe;
    }
}
