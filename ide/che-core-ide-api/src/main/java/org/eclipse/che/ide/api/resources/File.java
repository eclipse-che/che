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
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.resource.Path;

/**
 * Files are leaf resources which contain data. The contents of a file resource is stored as a file in the local file system.
 * <p/>
 * File extends also {@link VirtualFile}, so this resource can be easily opened in editor.
 * <p/>
 * File instance can be obtained by calling {@link Container#getFile(Path)} or by {@link Container#getChildren(boolean)}.
 * <p/>
 * Note. This interface is not intended to be implemented by clients.
 *
 * @author Vlad Zhukovskyi
 * @see VirtualFile
 * @see Container#getFile(Path)
 * @since 4.4.0
 */
@Beta
public interface File extends Resource, VirtualFile, ModificationTracker {

    /** @see VirtualFile#getDisplayName() */
    @Override
    String getDisplayName();

    /** @see VirtualFile#isReadOnly() */
    @Override
    boolean isReadOnly();

    /** @see VirtualFile#getContentUrl() */
    @Override
    String getContentUrl();

    /** @see VirtualFile#getContent() */
    @Override
    Promise<String> getContent();

    /** @see VirtualFile#updateContent(String) */
    @Override
    Promise<Void> updateContent(String content);

    /**
     * Returns the file extension portion of this resource's name or {@code null} if it does not have one.
     *
     * @return a string file extension or {@code null}
     * @see #getName()
     * @since 4.4.0
     */
    String getExtension();

    /**
     * Returns the name without the extension. If file name contains '.' the substring till the last '.' is
     * returned. Otherwise the same value as {@link #getName()} method returns is returned.
     *
     * @return the name without extension
     * @see #getExtension()
     * @see #getName()
     * @since 4.4.0
     */
    String getNameWithoutExtension();
}
