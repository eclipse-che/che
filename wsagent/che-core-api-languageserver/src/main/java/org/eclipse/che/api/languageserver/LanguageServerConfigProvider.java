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

/**
 * Provides a map of language server configurations, where the key is the language server id and the
 * value is the configuration itself.
 *
 * @author Dmytro Kulieshov
 */
public interface LanguageServerConfigProvider {
  /**
   * Gets all language server configurations that are configured for this provider.
   *
   * @return map of language server configurations
   */
  Map<String, LanguageServerConfig> getAll();
}
