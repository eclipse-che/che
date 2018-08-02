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
package org.eclipse.che.ide.api.extension;

import java.util.Map;

/**
 * Provides information about Extensions, their description, version and the list of dependencies.
 * Currently for information purposes only
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public interface ExtensionRegistry {

  /**
   * Returns the map of Extension ID to {@link ExtensionDescription}.
   *
   * @return
   */
  Map<String, ExtensionDescription> getExtensionDescriptions();
}
