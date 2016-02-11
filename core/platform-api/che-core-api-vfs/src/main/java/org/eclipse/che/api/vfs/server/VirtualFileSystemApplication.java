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

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CodenvyJsonProvider;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** @author andrew00x */
public class VirtualFileSystemApplication extends Application {
    private final Set<Object> singletons;

    private final Set<Class<?>> classes;

    @SuppressWarnings("unchecked")
    public VirtualFileSystemApplication() {
        classes = new HashSet<>(2);
        classes.add(VirtualFileSystemFactory.class);
        singletons = new HashSet<>(4);
        singletons.add(new ContentStreamWriter());
        singletons.add(new ApiExceptionMapper());
        singletons.add(new CodenvyJsonProvider(Collections.singleton(ContentStream.class)));
    }

    /** @see javax.ws.rs.core.Application#getClasses() */
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    /** @see javax.ws.rs.core.Application#getSingletons() */
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
