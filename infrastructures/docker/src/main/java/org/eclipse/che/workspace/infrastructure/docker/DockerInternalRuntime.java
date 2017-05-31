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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class DockerInternalRuntime extends InternalRuntime {
    private final DockerRuntimeContext.StartSynchronizer startSynchronizer;
    private final Map<String, String>                    properties;


    public DockerInternalRuntime(DockerRuntimeContext context,
                                 URLRewriter urlRewriter,
                                 DockerRuntimeContext.StartSynchronizer startSynchronizer) {
        super(context, urlRewriter);
        this.properties = new HashMap<>();
        this.startSynchronizer = startSynchronizer;

    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return Collections.unmodifiableMap(startSynchronizer.getMachines());
    }


    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
