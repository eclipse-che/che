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
package org.eclipse.che.ide.part.widgets.panemenu;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;

import javax.validation.constraints.NotNull;

/**
 * The factory creates instances of {@link EditorPaneMenuItem} to display items of editor pane menu.
 *
 * @author Roman Nikitenko
 */
public class EditorPaneMenuItemFactory {

    /**
     * Creates implementation of {@link EditorPaneMenuItem} to display some {@code action} as item of editor pane menu.
     *
     * @param action
     *         action to display as item of editor pane menu.
     * @return an instance of {@link EditorPaneMenuItem}
     */
    public EditorPaneMenuItem<Action> createMenuItem(@NotNull Action action) {
        return new PaneMenuActionItemWidget(action);
    }

    /**
     * Creates implementation of {@link EditorPaneMenuItem} to display some {@code tabItem} as item of editor pane menu.
     *
     * @param tabItem
     *         item of opened file to display as item of editor pane menu.
     * @return an instance of {@link EditorPaneMenuItem}
     */
    public EditorPaneMenuItem<TabItem> createMenuItem(@NotNull TabItem tabItem) {
        return new PaneMenuTabItemWidget(tabItem);
    }
}
