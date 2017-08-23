/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.theme;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;

/** GIN module for configuring Theme API components. */
public class ThemeApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ThemeAgent.class).to(ThemeAgentImpl.class).in(Singleton.class);
    bind(ThemeAgent.class).asEagerSingleton();

    GinMultibinder<Theme> themeBinder = GinMultibinder.newSetBinder(binder(), Theme.class);
    themeBinder.addBinding().to(DarkTheme.class);
    themeBinder.addBinding().to(LightTheme.class);
  }
}
