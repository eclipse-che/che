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
package org.eclipse.che.ide.api.event.project;

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler for handling {@link CloseCurrentProjectEvent}.
 *
 * @author Artem Zatsarynnyi
 * @deprecated since project explorer has all projects opened
 */
@Deprecated
public interface CloseCurrentProjectHandler extends EventHandler {
    /**
     * Called when someone is going to close the currently opened project.
     *
     * @param event
     *         the fired {@link CloseCurrentProjectEvent}
     */
    void onCloseCurrentProject(CloseCurrentProjectEvent event);
}
