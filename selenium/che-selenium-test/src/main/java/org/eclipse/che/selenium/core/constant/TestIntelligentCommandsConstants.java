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
package org.eclipse.che.selenium.core.constant;

/** */
public final class TestIntelligentCommandsConstants {

  private TestIntelligentCommandsConstants() {
    // this prevents class instance creation
    throw new AssertionError();
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
