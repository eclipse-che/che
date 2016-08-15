/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.multisplitpanel.WidgetToShow;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View of {@link SubPanelPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelView extends View<SubPanelView.ActionDelegate> {

    void splitHorizontally(IsWidget view);

    void splitVertically(IsWidget view);

    void addWidget(WidgetToShow widget);

    void activateWidget(WidgetToShow widget);

    void removeWidget(WidgetToShow widget);

    void removeCentralPanel();

    void removeChildSubPanel(Widget w);

    void setParentPanel(SubPanelView parentPanel);

    interface ActionDelegate {

        /** Called when the {@code widget} gains the focus. */
        void onWidgetFocused(IsWidget widget);

        /** Called when the {@code widget} has been removed from the panel. */
        void onWidgetRemoved(IsWidget widget);
    }
}
