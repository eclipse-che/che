/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.jdb.ide.configuration.JavaDebugConfigurationPageView;
import org.eclipse.che.plugin.jdb.ide.configuration.JavaDebugConfigurationPageViewImpl;
import org.eclipse.che.plugin.jdb.ide.configuration.JavaDebugConfigurationType;

/**
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class JavaRuntimeGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class)
        .addBinding()
        .to(JavaDebugConfigurationType.class);
    bind(JavaDebugConfigurationPageView.class)
        .to(JavaDebugConfigurationPageViewImpl.class)
        .in(Singleton.class);
  }
}
