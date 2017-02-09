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
package org.eclipse.che.plugin.svn.ide.log;

import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Vitaliy Guliy
 */
public interface ShowLogsView {

    /**
     * Sets a delegate for this view.
     *
     * @param delegate
     */
    void setDelegate(Delegate delegate);

    interface Delegate {

        void logClicked();

        void cancelClicked();

    }

    /**
     * Shows this view.
     */
    void show();

    /**
     * Hides this view.
     */
    void hide();

    /**
     * Sets revision count.
     *
     * @param revision
     */
    void setRevisionCount(String revision);

    /**
     * Returns range field.
     *
     * @return
     */
    HasValue<String> rangeField();

}
