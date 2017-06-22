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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handles {@link WindowActionEvent}.
 *
 * @author Artem Zatsarynnyi
 */
public interface WindowActionHandler extends EventHandler {
    /**
     * Fired just before the Codenvy browser's tab closes or navigates to a different site.
     *
     * @param event
     *         {@link WindowActionEvent}
     */
    void onWindowClosing(WindowActionEvent event);

    /**
     * Fired after the Codenvy browser's tab closed or navigated to a different site.
     *
     * @param event
     *         {@link WindowActionEvent}
     */
    void onWindowClosed(WindowActionEvent event);
}
