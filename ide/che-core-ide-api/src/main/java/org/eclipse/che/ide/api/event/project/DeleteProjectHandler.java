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
package org.eclipse.che.ide.api.event.project;

import com.google.gwt.event.shared.EventHandler;

/**
 * Special handler which is called when project is deleted.
 *
 * @author Dmitry Shnurenko
 */
public interface DeleteProjectHandler extends EventHandler {

    /**
     * Performs some actions when user removes a project.
     *
     * @param event
     *         contains information about deleted project
     */
    void onProjectDeleted(DeleteProjectEvent event);
}
