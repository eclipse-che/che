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
package org.eclipse.che.ide.ui.multisplitpanel.panel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;

/**
 * View of {@link SubPanelPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelView extends View<SubPanelView.ActionDelegate> {

    /**
     * Split panel horizontally on two sub-panels
     * and set the given {@code widget} for additional panel.
     */
    void splitHorizontally(IsWidget view);

    /**
     * Split panel vertically on two sub-panels
     * and set the given {@code widget} for additional panel.
     */
    void splitVertically(IsWidget view);

    /**
     * Add the given {@code widget} to this panel.
     *
     * @param widget
     *         widget to add
     * @param removable
     *         whether the {@code widget} may be removed by user from the UI
     */
    void addWidget(WidgetToShow widget, boolean removable);

    /** Show (activate) the {@code widget} if it exists on this panel. */
    void activateWidget(WidgetToShow widget);

    /**
     * Remove the given {@code widget} from this panel.
     *
     * @param widget
     *         widget to remove
     */
    void removeWidget(WidgetToShow widget);

    /**
     * Close panel. It may mean removing panel from it's
     * parent in case this panel is the last one or
     * removing only central part and moving additional part to the center.
     */
    void closePanel();

    /** Remove child panel. */
    void removeChildSubPanel(Widget widget);

    /** Set parent {@link SubPanelView} in case this panel is 'child' of another panel. */
    void setParentPanel(SubPanelView parentPanel);

    interface ActionDelegate {

        /** Called when the {@code widget} gains the focus. */
        void onWidgetFocused(IsWidget widget);

        /** Called when the {@code widget} is going to be removed from the panel. */
        void onWidgetRemoving(IsWidget widget, SubPanel.RemoveCallback removeCallback);
    }
}
