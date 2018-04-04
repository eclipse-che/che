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
