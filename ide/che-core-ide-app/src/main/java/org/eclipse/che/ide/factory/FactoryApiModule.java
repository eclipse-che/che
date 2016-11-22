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
package org.eclipse.che.ide.factory;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.factory.FactoryServiceClient;

/**
 * GIN module for configuring Factory API components.
 *
 * @author Artem Zatsarynnyi
 */
public class FactoryApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(FactoryServiceClient.class).to(FactoryServiceClientImpl.class).in(Singleton.class);
    }
}
