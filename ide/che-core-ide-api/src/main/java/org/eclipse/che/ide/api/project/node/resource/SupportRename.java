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
package org.eclipse.che.ide.api.project.node.resource;

import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;

/**
 * Indicates node which allow renaming.
 *
 * @author Vlad Zhukovskiy
 */
public interface SupportRename<DataObject> {
    /**
     * Return rename processor. To detail information {@see RenameProcessor}.
     *
     * @return rename processor
     */
    @Nullable
    RenameProcessor<DataObject> getRenameProcessor();

    /**
     * Perform rename operation.
     *
     * @param newName
     *         new name for the data object
     */
    void rename(@NotNull String newName);
}
