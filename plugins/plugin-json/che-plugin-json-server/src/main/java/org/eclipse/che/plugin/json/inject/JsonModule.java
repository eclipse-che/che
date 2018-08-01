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
package org.eclipse.che.plugin.json.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.json.languageserver.JsonLanguageServerConfig;

/** @author Anatolii Bazko */
@DynaModule
public class JsonModule extends AbstractModule {
  public static final String LANGUAGE_ID = "json";

  @Override
  protected void configure() {
    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.json.languageserver")
        .to(JsonLanguageServerConfig.class)
        .asEagerSingleton();
  }
}
