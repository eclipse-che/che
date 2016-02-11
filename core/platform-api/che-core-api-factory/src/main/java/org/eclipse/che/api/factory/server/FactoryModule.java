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
package org.eclipse.che.api.factory.server;

import com.google.inject.AbstractModule;


/**
 * @author andrew00x
 */
public class FactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(org.eclipse.che.api.factory.server.FactoryService.class);
        bind(FactoryCreateValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
        bind(FactoryAcceptValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
        bind(FactoryEditValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
    }
}
