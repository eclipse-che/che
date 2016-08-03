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

import org.eclipse.che.ide.api.mvp.View;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelView extends View<SubPanelView.ActionDelegate> {

    void splitHorizontally(IsWidget view);

    void splitVertically(IsWidget view);

    void addWidget(IsWidget w);

    void activateWidget(IsWidget w);

    void removeCentralPanel();

    void removeChildSubPanel(Widget w);

    void setParent(SubPanelView w);

    void removeWidget(IsWidget widget);

    interface ActionDelegate {

        void onFocused();

        void onSplitHorizontallyClicked();

        void onSplitVerticallyClicked();

        void onClosePaneClicked();
    }
}
