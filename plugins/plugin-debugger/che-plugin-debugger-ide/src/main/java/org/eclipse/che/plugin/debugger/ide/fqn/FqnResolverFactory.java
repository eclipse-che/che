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
package org.eclipse.che.plugin.debugger.ide.fqn;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** @author Evgen Vidolob */
@Singleton
public class FqnResolverFactory implements FqnResolverObservable {
    private final List<FqnResolverObserver> listeners;
    private final Map<String, FqnResolver>  resolvers;

    /** Create factory. */
    @Inject
    protected FqnResolverFactory() {
        this.resolvers = new HashMap<>();
        this.listeners = new LinkedList<>();
    }

    public void addResolver(@NotNull String fileExtension, @NotNull FqnResolver resolver) {
        resolvers.put(fileExtension, resolver);
        onFqnResolverAdded(resolver);
    }

    private void onFqnResolverAdded(@NotNull FqnResolver resolver) {
        for (FqnResolverObserver fqnResolverObserver : listeners) {
            fqnResolverObserver.onFqnResolverAdded(resolver);
        }
    }

    @Nullable
    public FqnResolver getResolver(@NotNull String fileExtension) {
        return resolvers.get(fileExtension);
    }

    @Override
    public void addFqnResolverObserver(FqnResolverObserver fqnResolverObserver) {
        listeners.add(fqnResolverObserver);
    }

    @Override
    public void removeFqnResolverObserver(FqnResolverObserver fqnResolverObserver) {
        listeners.remove(fqnResolverObserver);
    }
}
