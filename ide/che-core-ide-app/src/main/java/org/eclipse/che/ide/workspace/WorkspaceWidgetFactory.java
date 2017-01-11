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
package org.eclipse.che.ide.workspace;

import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;

/**
 * Special factory which allows creating recipe widgets and workspace widgets.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public interface WorkspaceWidgetFactory {

    /**
     * Creates view representation of tag using special descriptor.
     *
     * @param descriptor
     *         descriptor which contains all information about tag
     * @return an instance of {@link RecipeWidget}
     */
    RecipeWidget create(RecipeDescriptor descriptor);

    /**
     * Creates view representation of workspace.
     *
     * @param workspace
     *         descriptor which contains all information about workspace
     * @return an instance of {@link WorkspaceWidget}
     */
    WorkspaceWidget create(WorkspaceDto workspace);
}
