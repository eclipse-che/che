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
package org.eclipse.che.ide.api.resources;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;

import javax.validation.constraints.NotNull;

/**
 * File that may be opened in editor.
 * This in not necessary {@link org.eclipse.che.api.project.shared.dto.ItemReference}, it's may be from some external providers.
 * @author Evgen Vidolob
 */
public interface VirtualFile {

    /** get this file path */
    @NotNull
    String getPath();

    /** get this file name*/
    @NotNull
    String getName();

    String getDisplayName();

    /** if user doesn't have wright rights, or file comes from external sources thad doesn't support modifying file content*/
    boolean isReadOnly();

    @Nullable
    HasProjectConfig getProject();

    /**
     * Some file type can't represent their content as string.
     * So virtual file provide url where it content.
     * For example if this virtual file represent image,
     * image viewer may use this URL as src for {@link com.google.gwt.user.client.ui.Image}
     * @return url
     */
    String getContentUrl();

    /** * Get content of the file which this node represents. */
    Promise<String> getContent();

    /**
     * Update content of the file.
     * Note: this method is optional, some implementations may not support updating their content
     * @param content
     *         new content of the file
     */
    Promise<Void> updateContent(String content);

}
