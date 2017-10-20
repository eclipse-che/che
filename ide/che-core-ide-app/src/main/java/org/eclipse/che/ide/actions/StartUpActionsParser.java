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
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Utility class that parse IDE URL and get start up parameters form it according specific rules.
 * URL for parsing comes from {@link Window.Location}.
 *
 * <p>For example: after parsing URL like it
 * http://localhost:8080/ide/dev6?action=createProject:projectName=test;projectType=maven we will
 * get list with one action {@link StartUpAction} with ID 'createProject' and two parameters
 * 'projectName' :: 'test' 'projectType' :: 'maven'
 *
 * @author Vitalii Parfonov
 */
public class StartUpActionsParser {

  public static List<StartUpAction> getStartUpActions() {
    final Map<String, List<String>> parameterMap = Window.Location.getParameterMap();
    List<StartUpAction> startUpActions = new ArrayList<>();
    if (!parameterMap.isEmpty() && parameterMap.containsKey("action")) {
      final List<String> actions = parameterMap.get("action");
      for (String action : actions) {
        final StartUpAction startUpAction = parseActionQuery(action);
        startUpActions.add(startUpAction);
      }
    }
    return startUpActions;
  }

  protected static StartUpAction parseActionQuery(String action) {
    String actionId;
    String params;
    if (action.contains(":")) { // action has parameters
      final String[] split = action.split(":");
      actionId = split[0];
      params = split[1];
      return new StartUpAction(actionId, parseActionParameters(params));
    } else {
      return new StartUpAction(action, null);
    }
  }

  protected static Map<String, String> parseActionParameters(String actionParam) {
    Log.info(StartUpActionsParser.class, " parametersMap " + actionParam);
    final String[] parametersQuery = actionParam.split(";");
    Map<String, String> params = new HashMap<>(parametersQuery.length);
    for (int i = 0; i < parametersQuery.length; i++) {
      final String parameterString = parametersQuery[i];
      final String[] param = parameterString.split("=");
      final String paramName = param[0];
      if (param.length > 1) {
        final String paramValue = param[1];
        params.put(paramName, paramValue);
      } else {
        params.put(paramName, null);
      }
    }
    return params;
  }
}
