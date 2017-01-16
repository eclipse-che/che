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
package org.eclipse.che.ide.ext.java.client.resource;

import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.RenamingSupport;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Disable rename operation for the children of the source folder.
 *
 * @author Valeriy Svydenko
 */
public class JavaSourceRenameValidator implements RenamingSupport {
    @Override
    public boolean isRenameAllowed(Resource resource) {
        boolean inSource = resource.getParentWithMarker(SourceFolderMarker.ID).isPresent();
        boolean isJavaClass = resource.isFile() && "java".equals(((File)resource).getExtension());

        return !inSource || !resource.isFolder() && !isJavaClass;
    }
}
