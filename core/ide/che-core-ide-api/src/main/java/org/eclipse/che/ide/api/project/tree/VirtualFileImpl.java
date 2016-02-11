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
package org.eclipse.che.ide.api.project.tree;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;

/**
 * Implementation of {@link VirtualFile}. Keeps only necessary information.
 *
 * @author Valeriy Svydenko
 */
public class VirtualFileImpl implements VirtualFile {
    private final VirtualFileInfo fileInfo;

    public VirtualFileImpl(VirtualFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    @Override
    public String getPath() {
        if (fileInfo.getPath() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getPath();
        }
    }

    @Override
    public String getName() {
        if (fileInfo.getName() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getName();
        }
    }

    @Override
    public String getDisplayName() {
        if (fileInfo.getDisplayName() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getDisplayName();
        }
    }

    @Override
    public String getMediaType() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return fileInfo.isReadOnly();
    }

    @Override
    public HasProjectConfig getProject() {
        if (fileInfo.getProject() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getProject();
        }
    }

    @Override
    public String getContentUrl() {
        if (fileInfo.getContentUrl() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getContentUrl();
        }
    }

    @Override
    public Promise<String> getContent() {
        if (fileInfo.getContent() == null) {
            throw new UnsupportedOperationException();
        } else {
            return Promises.resolve(fileInfo.getContent());
        }
    }

    @Override
    public Promise<Void> updateContent(String content) {
        if (fileInfo.getUpdateContent() == null) {
            throw new UnsupportedOperationException();
        } else {
            return fileInfo.getUpdateContent();
        }
    }
}
