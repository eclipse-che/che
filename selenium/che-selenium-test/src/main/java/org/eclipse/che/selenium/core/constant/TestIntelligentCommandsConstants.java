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
package org.eclipse.che.selenium.core.constant;

import static java.lang.String.format;

/** */
public final class TestIntelligentCommandsConstants {

  private TestIntelligentCommandsConstants() {
    // this prevents class instance creation
    throw new AssertionError();
  }

  public enum CommandItem {
    RUN_COMMAND_ITEM("%s:run"),
    GRADLE_RUN_COMMAND_ITEM("%s:gradle run"),
    BUILD_COMMAND_ITEM("%s:build"),
    MAVEN_BUILD_COMMAND_ITEM("%s:maven build"),
    BUILD_AND_RUN_COMMAND_ITEM("%s:build and run"),
    MAVEN_BUILD_AND_RUN_COMMAND_ITEM("%s:maven build and run"),
    BUILD_AND_DEPLOY_COMMAND_ITEM("%s:build and deploy"),
    STOP_TOMCAT_COMMAND_ITEM("%s:stop tomcat"),
    RUN_TOMCAT_COMMAND_ITEM("%s:run tomcat"),
    DEBUG_COMMAND_ITEM("%s:debug"),
    INSTALL_DEPENDENCIES_COMMAND_ITEM("%s:install dependencies"),
    UPDATE_DEPENDENCIES_COMMAND_ITEM("%s:update dependencies");

    private final String itemTemplate;

    CommandItem(String itemTemplate) {
      this.itemTemplate = itemTemplate;
    }

    public String getItem(String projectName) {
      return format(itemTemplate, projectName);
    }
  }

  public static class CommandsGoals {

    private CommandsGoals() {}

    public static final String COMMON_GOAL = "Common";
    public static final String DEPLOY_GOAL = "Deploy";
    public static final String DEBUG_GOAL = "Debug";
    public static final String RUN_GOAL = "Run";
    public static final String TEST_GOAL = "Test";
    public static final String BUILD_GOAL = "Build";
    public static final String NEW_COMMAND_GOAL = "New Command Goal...";
  }

  public static class CommandsTypes {

    private CommandsTypes() {}

    public static final String GWT_TYPE = "GWT";
    public static final String GWT_SDM_FOR_CHE_TYPE = "GWT SDM for Che";
    public static final String CUSTOM_TYPE = "Custom";
    public static final String JAVA_TYPE = "Java";
    public static final String MAVEN_TYPE = "Maven";
  }

  public static class CommandsDefaultNames {

    private CommandsDefaultNames() {}

    public static final String GWT_NAME = "newGWT";
    public static final String JAVA_NAME = "newJava";
    public static final String MAVEN_NAME = "newMaven";
    public static final String CUSTOM_NAME = "newCustom";
    public static final String GWT_SDM_FOR_CHE = "newGWT SDM for Che";
  }
}
