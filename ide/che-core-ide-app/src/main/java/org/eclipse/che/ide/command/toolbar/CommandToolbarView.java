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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;

/** View for command toolbar. */
public interface CommandToolbarView extends View<CommandToolbarView.ActionDelegate> {

    AcceptsOneWidget getCommandsPanelContainer();

    AcceptsOneWidget getProcessesListContainer();

    AcceptsOneWidget getPreviewUrlsListContainer();

    void addButton(MenuPopupButton button);

    interface ActionDelegate {
    }
}
