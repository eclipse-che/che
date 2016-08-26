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
package org.eclipse.che.ide.ui.multisplitpanel.actions;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;

/**
 * Action for removing all widgets in the given {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public class RemoveAllWidgetsInPaneAction extends Action {

    private final SubPanel subPanel;

    public RemoveAllWidgetsInPaneAction(SubPanel subPanel) {
        super("Close All Tabs In Pane", "Close All Tabs In Pane", null, null);
        this.subPanel = subPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (WidgetToShow widget : subPanel.getAllWidgets()) {
            subPanel.removeWidget(widget);
        }
    }
}
