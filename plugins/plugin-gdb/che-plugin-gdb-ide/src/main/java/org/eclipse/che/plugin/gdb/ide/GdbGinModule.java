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
package org.eclipse.che.plugin.gdb.ide;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.gdb.ide.configuration.GdbConfigurationPageView;
import org.eclipse.che.plugin.gdb.ide.configuration.GdbConfigurationPageViewImpl;
import org.eclipse.che.plugin.gdb.ide.configuration.GdbConfigurationType;

/**
 * @author Anatolii Bazko
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class GdbGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class)
        .addBinding()
        .to(GdbConfigurationType.class);
    bind(GdbConfigurationPageView.class).to(GdbConfigurationPageViewImpl.class).in(Singleton.class);
  }
}
