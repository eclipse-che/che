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
package org.eclipse.che.ide.api.app;

import java.util.Map;

/**
 * Class that contain action id and parsed params form initial URL.
 *
 * @author Vitalii Parfonov
 */
public class StartUpAction {

  private String actionId;
  private Map<String, String> parameters;

  public StartUpAction(String actionId, Map<String, String> parameters) {
    this.actionId = actionId;
    this.parameters = parameters;
  }

  public String getActionId() {
    return actionId;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }
}
