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
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.resource.Path;

/**
 * Implementation for {@link VirtualFile} which describe resource which doesn't exist on file system and is auto generated.
 * For example it may be effective version of such resource.
 * <p/>
 * This file is read only and doesn't have link to the content url.
 * Calling {@link #updateContent(String)} will cause {@link UnsupportedOperationException}.
 *
 * @author Vlad Zhukovskiy
 * @see VirtualFile
 */
@Beta
public class SyntheticFile implements VirtualFile {

    private String name;
    private String content;
    private String displayName;

    public SyntheticFile(String name, String content) {
        this(name, name, content);
    }

    public SyntheticFile(String name, String displayName, String content) {
        this.name = name;
        this.displayName = displayName;
        this.content = content;
    }

    @Override
    public Path getLocation() {
        return Path.valueOf(name);
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
        throw new UnsupportedOperationException("Synthetic file is read only");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyntheticFile)) return false;
        SyntheticFile that = (SyntheticFile)o;
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
