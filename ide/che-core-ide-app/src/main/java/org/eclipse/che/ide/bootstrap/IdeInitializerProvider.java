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
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.context.QueryParameters;

/** Provides {@link IdeInitializer} depending on the loading mode (general/factory). */
@Singleton
class IdeInitializerProvider implements Provider<IdeInitializer> {

    private final QueryParameters       queryParameters;
    private final GeneralIdeInitializer generalIdeInitializer;
    private final FactoryIdeInitializer factoryIdeInitializer;

    @Inject
    IdeInitializerProvider(QueryParameters queryParameters,
                           GeneralIdeInitializer generalIdeInitializer,
                           FactoryIdeInitializer factoryIdeInitializer) {
        this.queryParameters = queryParameters;
        this.generalIdeInitializer = generalIdeInitializer;
        this.factoryIdeInitializer = factoryIdeInitializer;
    }

    @Override
    public IdeInitializer get() {
        return isFactoryMode() ? factoryIdeInitializer : generalIdeInitializer;
    }

    /** Checks whether the IDE receives a Factory. */
    private boolean isFactoryMode() {
        return !queryParameters.getByName("factory").isEmpty();
    }
}
