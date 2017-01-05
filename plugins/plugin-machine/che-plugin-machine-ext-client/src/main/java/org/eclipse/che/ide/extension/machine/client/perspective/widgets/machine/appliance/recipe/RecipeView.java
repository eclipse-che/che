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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;

/**
 * Provides methods which allow change view representation of recipe tab.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(RecipeViewImpl.class)
public interface RecipeView extends IsWidget {

    /**
     * Sets script of the recipe in special place on view.
     *
     * @param script
     *         script of the current machine recipe
     */
    void setScript(@NotNull String script);

    /**
     * Change visibility of Recipe tab panel.
     *
     * @param visible
     *         <code>true</code> panel is visible, <code>false</code> panel isn't visible
     */
    void setVisible(boolean visible);
}
