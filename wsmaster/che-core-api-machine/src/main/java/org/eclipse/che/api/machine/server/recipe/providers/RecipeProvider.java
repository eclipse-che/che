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
package org.eclipse.che.api.machine.server.recipe.providers;

import com.google.inject.Provider;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Anton Korneta
 */
public class RecipeProvider implements Provider<String> {
    @Inject
    @Nullable
    @Named("local.recipe.path")
    public String path;

    @Override
    public String get() {
        return path;
    }
}
