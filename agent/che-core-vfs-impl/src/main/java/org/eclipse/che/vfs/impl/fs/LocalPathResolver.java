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
package org.eclipse.che.vfs.impl.fs;

import javax.inject.Singleton;

/**
 * Resolves location of virtual filesystem item on local filesystem.
 *
 * @author Vitaly Parfonov
 */
@Singleton
public class LocalPathResolver {
    public String resolve(VirtualFileImpl virtualFile) {
        return virtualFile.getIoFile().getAbsolutePath();
    }
}
