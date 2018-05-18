/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.camel.server.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.camel.server.languageserver.CamelLanguageServerConfig;

/** Apache Camel module for server side of Camel Language Server */
@DynaModule
public class CamelModule extends AbstractModule {
  public static final String LANGUAGE_ID = "LANGUAGE_ID_APACHE_CAMEL";

  @Override
  protected void configure() {
    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.camel.server.languageserver")
        .to(CamelLanguageServerConfig.class)
        .asEagerSingleton();
  }
}
