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
package org.eclipse.che.ide.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.dto.DtoFactoryVisitor;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Class responsible for register DTO providers. It uses {@link DtoFactoryVisitorRegistry} to acquire
 * {@link DtoFactoryVisitor}s.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DtoRegistrar {
    private final DtoFactory                dtoFactory;
    private final DtoFactoryVisitorRegistry dtoFactoryVisitorRegistry;

    @Inject
    public DtoRegistrar(DtoFactory dtoFactory, DtoFactoryVisitorRegistry dtoFactoryVisitorRegistry) {
        this.dtoFactory = dtoFactory;
        this.dtoFactoryVisitorRegistry = dtoFactoryVisitorRegistry;
    }

    public void registerDtoProviders() {
        Map<String, Provider> dtoVisitors = dtoFactoryVisitorRegistry.getDtoFactoryVisitors();

        for (Entry<String, Provider> entry : dtoVisitors.entrySet()) {
            ((DtoFactoryVisitor)entry.getValue().get()).accept(dtoFactory);
        }
    }
}
