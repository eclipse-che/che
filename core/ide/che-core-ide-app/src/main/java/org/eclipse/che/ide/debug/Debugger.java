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
package org.eclipse.che.ide.debug;

import org.eclipse.che.ide.api.project.tree.VirtualFile;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The general class which provides to manage breakpoints on server.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface Debugger {
    /**
     * Adds new breakpoint on server.
     *
     * @param file
     * @param lineNumber
     * @param callback
     */
    void addBreakpoint(VirtualFile file, int lineNumber, AsyncCallback<Breakpoint> callback);

    /**
     * Deletes breakpoint on server.
     *
     * @param file
     * @param lineNumber
     * @param callback
     */
    void deleteBreakpoint(VirtualFile file, int lineNumber, AsyncCallback<Void> callback);
}