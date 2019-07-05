/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationPageView;
import org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationPageViewImpl;
import org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType;

/**
 * Zend debugger runtime GIN module.
 *
 * @author Bartlomiej Laczkowski
 */
@ExtensionGinModule
public class ZendDbgGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class)
        .addBinding()
        .to(ZendDbgConfigurationType.class);
    bind(ZendDbgConfigurationPageView.class)
        .to(ZendDbgConfigurationPageViewImpl.class)
        .in(Singleton.class);
  }
}
