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
package org.eclipse.che.api.languageserver;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides all language server configuration that is configured via <code>Guice</code> map binder.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class GuiceConfigProvider implements LanguageServerConfigProvider {
  private final Map<String, LanguageServerConfig> languageServerConfigs;

  @Inject
  GuiceConfigProvider(Map<String, LanguageServerConfig> languageServerConfigs) {
    this.languageServerConfigs = languageServerConfigs;
  }

  @Override
  public Map<String, LanguageServerConfig> getAll() {
    return languageServerConfigs;
  }
}
