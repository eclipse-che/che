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
package org.eclipse.che.api.git;

import org.eclipse.che.api.git.shared.Branch;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Writer to serialize list of git branches to plain text in form as command line git does.
 *
 * @author andrew00x
 */
@Singleton
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class BranchListWriter implements MessageBodyWriter<Iterable<Branch>> {
    /**
     * @see MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isWriteable(Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Iterable.class.isAssignableFrom(type) && (genericType instanceof ParameterizedType)) {
            Type[] types = ((ParameterizedType)genericType).getActualTypeArguments();
            return types.length == 1 && types[0] == Branch.class;
        }
        return false;
    }

    /**
     * @see MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public long getSize(Iterable<Branch> branches,
                        Class< ? > type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
     *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(Iterable<Branch> branches,
                        Class< ? > type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        Writer writer = new OutputStreamWriter(entityStream);
        for (Branch branch : branches) {
            if (branch.isActive()) {
                writer.write('*');
            } else {
                writer.write(' ');
            }
            writer.write(' ');
            if (branch.getName().startsWith("refs/remotes")) {
                writer.write(branch.getName().substring(5)); // trim leading 'refs/'
            } else {
                writer.write(branch.getDisplayName());
            }
            writer.write('\n');
        }
        writer.flush();
    }
}
