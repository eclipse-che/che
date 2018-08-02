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
