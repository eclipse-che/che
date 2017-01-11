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
package org.eclipse.che.ide.resources;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.resources.impl.ResourceManager;

/**
 * Resource management component. Initializes with workspace agent.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Singleton
public class ResourceManagerComponent implements WsAgentComponent {

    private ResourceManagerInitializer initializer;

    @Inject
    public ResourceManagerComponent(ResourceManagerInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void start(final Callback<WsAgentComponent, Exception> callback) {
        initializer.initResourceManager(new Callback<ResourceManager, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(ResourceManager manager) {
                callback.onSuccess(ResourceManagerComponent.this);
            }
        });
    }
}
