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
package org.eclipse.che.plugin.docker.machine.local.interceptor;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;

import static org.eclipse.che.inject.Matchers.names;

/**
 * If installed intercepts docker machine creation and make everything needed to create
 * machine if base image of Dockerfile that should be pulled already cached
 * and network is down.
 *
 * @author Alexander Garagatyi
 */
public class AllowOfflineMachineCreationModule extends AbstractModule {
    @Override
    protected void configure() {
        org.eclipse.che.plugin.docker.machine.local.interceptor.EnableOfflineDockerMachineBuildInterceptor offlineMachineBuildInterceptor =
                new org.eclipse.che.plugin.docker.machine.local.interceptor.EnableOfflineDockerMachineBuildInterceptor();
        requestInjection(offlineMachineBuildInterceptor);
        bindInterceptor(Matchers.subclassesOf(DockerInstanceProvider.class), names("buildImage"), offlineMachineBuildInterceptor);
    }
}
