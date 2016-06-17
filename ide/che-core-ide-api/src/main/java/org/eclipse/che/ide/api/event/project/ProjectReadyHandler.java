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
 * A handler for handling {@link ProjectReadyEvent}.
 *
 * @author Nikolay Zamosenchuk
 */
public interface ProjectReadyHandler extends EventHandler {
    /**
     * Called on project open and fully initialize.
     *
     * @param event
     *         the fired {@link ProjectReadyEvent}
     */
    void onProjectReady(ProjectReadyEvent event);
}
