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
package org.eclipse.che.ide.extension.machine.client.inject.factories;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;

/**
 * Special factory for creating different widgets.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public interface WidgetsFactory {

    /**
     * Creates widget for tab header.
     *
     * @param tabName
     *         name which need set to tab
     * @return an instance of {@link TabHeader}
     */
    TabHeader createTabHeader(@NotNull String tabName);

    /**
     * Creates property button widget.
     *
     * @param title
     *         title of button
     * @param background
     *         background of button
     * @return an instance of {@link EditorButtonWidget}
     */
    @NotNull
    EditorButtonWidget createEditorButton(@NotNull String title, @NotNull EditorButtonWidgetImpl.Background background);

}
