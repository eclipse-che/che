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
