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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * The interface provides methods to control tab's content. Each container which will be added to tab container must implement
 * this interface.
 *
 * @author Dmitry Shnurenko
 */
public interface TabPresenter extends Presenter {
    /** @return view representation of tab's content */
    IsWidget getView();

    /**
     * Sets visibility of tab's content.
     *
     * @param visible
     *         <code>true</code> content is visible,<code>false</code> content isn't visible
     */
    void setVisible(boolean visible);
}
