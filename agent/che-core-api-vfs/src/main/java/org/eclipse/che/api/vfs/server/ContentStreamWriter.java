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

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * Serializer for ContentStream. Copy headers and content provided by method {@link ContentStream#getStream()} to HTTP
 * output stream.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 */
@Provider
@Singleton
public final class ContentStreamWriter implements MessageBodyWriter<ContentStream> {
    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ContentStream.class.isAssignableFrom(type);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public long getSize(ContentStream t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return t.getLength();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.OutputStream)
     */
    @Override
    public void writeTo(ContentStream t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                      WebApplicationException {
        String mimeType = t.getMimeType();
        if (mimeType != null) {
            httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mimeType);
        }
        httpHeaders.putSingle(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + t.getFileName() + '"');
        Date lastModificationDate = t.getLastModificationDate();
        if (lastModificationDate != null) {
            httpHeaders.putSingle(HttpHeaders.LAST_MODIFIED, t.getLastModificationDate());
        }

        httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, "public, no-cache, no-store, no-transform");

        try (InputStream content = t.getStream()) {
            byte[] buf = new byte[8192];
            int rd;
            while ((rd = content.read(buf)) != -1) {
                entityStream.write(buf, 0, rd);
            }
            entityStream.flush();
        }
    }
}
