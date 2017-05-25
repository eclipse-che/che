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
package org.eclipse.che.api.workspace.server.spi.dummy;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;

import java.util.Collections;

@Singleton
public class DummyRuntimeInfrastructure extends RuntimeInfrastructure {

    public DummyRuntimeInfrastructure(EventService eventService) {
        super("dummy", Collections.singletonList("dummy"), eventService);
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        return null;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException, InfrastructureException {
        return null;
    }
}
