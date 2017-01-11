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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.extension.ExtensionDescription;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.extension.ExtensionsInitializedEvent;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link ExtensionInitializer} responsible for bringing up Extensions. It uses ExtensionRegistry to acquire
 * Extension description and dependencies.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
public class ExtensionInitializer {
    protected final ExtensionRegistry extensionRegistry;

    private final ExtensionManager extensionManager;
    private final EventBus         eventBus;

    @Inject
    public ExtensionInitializer(final ExtensionRegistry extensionRegistry,
                                final ExtensionManager extensionManager,
                                EventBus eventBus) {
        this.extensionRegistry = extensionRegistry;
        this.extensionManager = extensionManager;
        this.eventBus = eventBus;
    }

    public void startExtensions() {
        Map<String, Provider> providers = extensionManager.getExtensions();
        for (Entry<String, Provider> entry : providers.entrySet()) {
            final String extensionFqn = entry.getKey();
            final Provider extensionProvider = entry.getValue();

            try {
                // Order of startup is managed by GIN dependency injection framework
                extensionProvider.get();
            } catch (Throwable e) {
                Log.error(ExtensionInitializer.class, "Can't initialize extension: " + extensionFqn, e);
            }
        }

        eventBus.fireEvent(new ExtensionsInitializedEvent());
    }

    public Map<String, ExtensionDescription> getExtensionDescriptions() {
        return extensionRegistry.getExtensionDescriptions();
    }
}
