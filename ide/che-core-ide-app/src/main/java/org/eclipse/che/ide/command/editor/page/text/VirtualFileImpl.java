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

package org.eclipse.che.ide.command.editor.page.text;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;

/**
 * Copy of the {@link SyntheticFile} with ability to update it's content
 * since {@link SyntheticFile} is read only.
 *
 * @see SyntheticFile
 */
class VirtualFileImpl implements VirtualFile {

    private String name;
    private String content;
    private String displayName;

    VirtualFileImpl(String name, String content) {
        this(name, name, content);
    }

    VirtualFileImpl(String name, String displayName, String content) {
        this.name = name;
        this.displayName = displayName;
        this.content = content;
    }

    @Override
    public Path getLocation() {
        return Path.valueOf(getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getMediaType() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getContentUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<String> getContent() {
        return Promises.resolve(content);
    }

    @Override
    public Promise<Void> updateContent(String content) {
        this.content = content;
        return Promises.resolve(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirtualFileImpl)) return false;
        VirtualFileImpl that = (VirtualFileImpl)o;
        return Objects.equal(name, that.name) &&
               Objects.equal(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, content);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .toString();
    }
}
