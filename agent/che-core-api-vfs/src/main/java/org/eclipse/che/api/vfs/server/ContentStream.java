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
package org.eclipse.che.api.vfs.server;

import java.io.InputStream;
import java.util.Date;

/**
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 */
public final class ContentStream {
    private final String fileName;

    private final InputStream stream;

    private final String mimeType;

    private final long length;

    private final Date lastModificationDate;

    public ContentStream(String fileName, InputStream stream, String mimeType, long length, Date lastModificationDate) {
        this.fileName = fileName;
        this.stream = stream;
        this.mimeType = mimeType;
        this.length = length;
        this.lastModificationDate = lastModificationDate;
    }

    public ContentStream(String fileName, InputStream stream, String mimeType, Date lastModificationDate) {
        this(fileName, stream, mimeType, -1, lastModificationDate);
    }

    public ContentStream(String fileName, InputStream stream, String mimeType) {
        this(fileName, stream, mimeType, -1, new Date());
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getLength() {
        return length;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }
}
