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
package org.eclipse.che.plugin.traefik;

import com.google.inject.AbstractModule;

import org.eclipse.che.plugin.docker.client.DockerConnector;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/**
 * The Module for Traefik components.
 *
 * @author Florent Benoit
 */
public class TraefikModule extends AbstractModule {

    /**
     * Configure the traefik components
     */
    @Override
    protected void configure() {
        // add an interceptor to intercept createContainer calls and then get the final labels
        final CreateContainerInterceptor createContainerInterceptor = new CreateContainerInterceptor();
        requestInjection(createContainerInterceptor);
        bindInterceptor(subclassesOf(DockerConnector.class), names("createContainer"), createContainerInterceptor);
    }
}
