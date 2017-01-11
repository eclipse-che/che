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
package org.eclipse.che.api.vfs.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for InputStream which prevent close of wrapped stream.
 * <p/>
 * For example,  useful if need read content of ZipEntry but prevent close ZipInputStream.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public final class NotClosableInputStream extends FilterInputStream {
    public NotClosableInputStream(InputStream delegate) {
        super(delegate);
    }

    /** @see java.io.InputStream#close() */
    @Override
    public void close() throws IOException {
    }
}

