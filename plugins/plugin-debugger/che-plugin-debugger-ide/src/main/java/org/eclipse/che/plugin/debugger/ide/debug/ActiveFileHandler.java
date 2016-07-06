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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.api.debug.shared.model.Location;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public interface ActiveFileHandler {

    /**
     * Open file, scroll to the debug line and do some actions from {@code callback}
     *
     * @param location
     *         location to the source file. See more {@link Location}
     * @param callback
     *         some action which should be performed after opening file
     */
    void openFile(Location location, AsyncCallback<VirtualFile> callback);
}
