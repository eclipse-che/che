/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.statepersistance;

import elemental.json.JsonFactory;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Service allows to load or persist IDE state for current user.
 *
 * @author Roman Nikitenko
 */
public interface AppStateServiceClient {

  /**
   * Load saved IDE state for current workspace in JSON format. Use {@link
   * JsonFactory#parse(String)} to get corresponding object. Note: it is expected that saved IDE
   * state object is valid, so any validations are not performed.
   */
  Promise<String> loadState();

  /**
   * Save IDE state for current workspace.
   *
   * @param state IDE state in JSON format.
   */
  Promise<Void> saveState(String state);
}
