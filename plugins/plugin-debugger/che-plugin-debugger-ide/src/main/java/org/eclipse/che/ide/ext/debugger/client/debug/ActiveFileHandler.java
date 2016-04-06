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
package org.eclipse.che.ide.ext.debugger.client.debug;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.project.tree.VirtualFile;

import java.util.List;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public interface ActiveFileHandler {

    void openFile(final List<String> filePaths,
                  final String className,
                  final int lineNumber,
                  final AsyncCallback<VirtualFile> callback);}
