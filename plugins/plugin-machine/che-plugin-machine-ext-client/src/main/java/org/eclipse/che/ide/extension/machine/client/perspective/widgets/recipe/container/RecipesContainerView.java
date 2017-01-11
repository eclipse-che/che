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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * @author Valeriy Svydenko
 */
@ImplementedBy(RecipesContainerViewImpl.class)
public interface RecipesContainerView extends View<RecipesContainerView.ActionDelegate>{
    interface ActionDelegate {
    }

    /**
     * Show a given widget in the special place in the container.
     *
     * @param panel
     *         recipes panel that needs to be shown
     */
    void showWidget(@NotNull IsWidget panel);
}
