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
package org.eclipse.che.api.vfs;

import org.eclipse.che.api.core.ServerException;

/**
 * This interface defines the visit method. When an implementation of this interface is passed to {@link
 * VirtualFile#accept(VirtualFileVisitor)} the <code>visit</code> method is called.
 *
 * @author andrew00x
 */
public interface VirtualFileVisitor {
    /**
     * This method is called when the VirtualFileVisitor is passed to the {@link VirtualFile#accept(VirtualFileVisitor) accept} method of a
     * {@link VirtualFile}.
     *
     * @param virtualFile
     *         VirtualFile which is accepting this visitor
     */
    void visit(VirtualFile virtualFile) throws ServerException;
}
