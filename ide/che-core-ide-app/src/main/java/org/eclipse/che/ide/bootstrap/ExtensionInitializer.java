/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.ide.api.extension.ExtensionsInitializedEvent;
import org.eclipse.che.ide.client.ExtensionManager;
import org.eclipse.che.ide.util.loging.Log;

/**
 * {@link ExtensionInitializer} responsible for bringing up Extensions. It uses {@link
 * ExtensionManager} to acquire Extension description and dependencies.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
class ExtensionInitializer {

  private final ExtensionManager extensionManager;
  private final EventBus eventBus;

  @Inject
  ExtensionInitializer(ExtensionManager extensionManager, EventBus eventBus) {
    this.extensionManager = extensionManager;
    this.eventBus = eventBus;
  }

  void startExtensions() {
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
}
