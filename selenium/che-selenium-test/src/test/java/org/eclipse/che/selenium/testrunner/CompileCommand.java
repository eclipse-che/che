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
package org.eclipse.che.selenium.testrunner;

import static org.eclipse.che.selenium.core.constant.TestGoalsConstants.TEST;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Command;

/** @author Vitalii Parfonov */
public class CompileCommand implements Command {

  private final String name;
  private final String type;
  private final String commandLine;
  private final Map<String, String> attribute;

  public CompileCommand(
      String name, String type, String commandLine, Map<String, String> attribute) {
    this.name = name;
    this.type = type;
    this.commandLine = commandLine;
    this.attribute = attribute;
  }

  public CompileCommand() {
    name = "test-compile";
    type = "mvn";
    commandLine = "mvn test-compile -f ${current.project.path}";
    attribute = new HashMap<>();
    attribute.put("goal", TEST);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCommandLine() {
    return commandLine;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attribute;
  }
}
