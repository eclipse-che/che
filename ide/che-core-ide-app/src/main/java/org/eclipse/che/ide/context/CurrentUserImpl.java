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
package org.eclipse.che.ide.context;

import java.util.Map;
import org.eclipse.che.ide.api.app.CurrentUser;

/** Implementation of the {@link CurrentUser}. */
public class CurrentUserImpl implements CurrentUser {

  private String id;
  private Map<String, String> preferences;

  public CurrentUserImpl() {}

  public CurrentUserImpl(String id, Map<String, String> preferences) {
    this.id = id;
    this.preferences = preferences;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getPreferences() {
    return preferences;
  }

  public void setPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }
}
