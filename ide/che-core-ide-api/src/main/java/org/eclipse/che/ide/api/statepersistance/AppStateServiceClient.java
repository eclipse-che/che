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
