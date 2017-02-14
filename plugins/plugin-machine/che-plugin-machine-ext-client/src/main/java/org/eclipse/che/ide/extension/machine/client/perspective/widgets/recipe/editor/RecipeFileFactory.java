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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.api.resources.VirtualFile;

import javax.validation.constraints.NotNull;

/**
 * The factory that provides an ability to create instances of {@link VirtualFile}. The main idea of this class is to simplify work flow of
 * using  {@link VirtualFile}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class RecipeFileFactory {

    public static final String NAME = "Dockerfile";
    public static final String PATH = "machine_recipe";

    public RecipeFileFactory() {
    }

    /**
     * Create a new instance of {@link VirtualFile} for a given href.
     *
     * @param content
     *         script of the recipe
     * @return an instance of {@link VirtualFile}
     * @throws IllegalStateException
     *         when no project is opened
     */
    @NotNull
    public VirtualFile newInstance(@NotNull String content) {
        return newInstance(content, NAME, PATH);
    }

    @NotNull
    private VirtualFile newInstance(@NotNull String content, @NotNull String name, @NotNull String path) {
        return new SyntheticFile(name, content);
    }

}
