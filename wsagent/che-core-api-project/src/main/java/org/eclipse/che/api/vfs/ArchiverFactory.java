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

public class ArchiverFactory {
    public Archiver createArchiver(VirtualFile folder, String archiveType) {
        if (archiveType == null) {
            throw new IllegalArgumentException("Archive type might not be null");
        }
        if ("zip".equals(archiveType.toLowerCase())) {
            return new ZipArchiver(folder);
        } else if ("tar".equals(archiveType.toLowerCase())) {
            return new TarArchiver(folder);
        }
        throw new IllegalArgumentException(String.format("Unsupported archive type %s", archiveType));
    }
}
