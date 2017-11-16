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

package org.eclipse.che.ide.api.theme;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Provider interface for {@link Theme}, use {@link Promise} to provide theme instance.
 *
 * @author Yevhen Vydolob
 */
public interface ThemeProvider {

  /** @return the theme id */
  String getId();

  /** @return the description of the theme */
  String getDescription();

  Promise<Theme> loadTheme();
}
