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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import org.eclipse.che.workspace.infrastructure.docker.environment.compose.ComposeEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile.DockerfileEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironmentParser;

/**
 * @author Alexander Garagatyi
 */
public class DockerEnvironmentTypeModule extends AbstractModule {
    @Override
    protected void configure() {
        // Environment type
        MapBinder<String, TypeSpecificEnvironmentParser> envParserMapBinder =
                MapBinder.newMapBinder(binder(), String.class, TypeSpecificEnvironmentParser.class);
        envParserMapBinder.addBinding("compose").to(ComposeEnvironmentParser.class);
        envParserMapBinder.addBinding("dockerfile").to(DockerfileEnvironmentParser.class);
        envParserMapBinder.addBinding("dockerimage").to(DockerImageEnvironmentParser.class);
    }
}
