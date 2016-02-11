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
package org.eclipse.che.ide.extension.machine.client.outputspanel;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View of {@link OutputsContainerPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface OutputsContainerView extends View<OutputsContainerView.ActionDelegate> {

    /** Add console widget with the specified title. */
    void addConsole(String title, IsWidget widget);

    /** Add console widget with the specified title in the specified place. */
    void insertConsole(String title, IsWidget widget, int position);

    /** Show console by the given index. */
    void showConsole(int index);

    /** Remove console by the given index. */
    void removeConsole(int index);

    /** Remove all consoles. */
    void removeAllConsoles();

    /**
     * Set view's title.
     *
     * @param title
     *         new title
     */
    void setTitle(String title);

    void setVisible(boolean visible);

    interface ActionDelegate extends BaseActionDelegate {

        /** Called when console with the given {@code index} has been selected. */
        void onConsoleSelected(int index);

        /** Called when console with the given {@code index} going to close. */
        void onConsoleClose(int index);
    }

}
