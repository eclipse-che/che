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

/** @author Anatolii Bazko */
public interface TestCommandsConstants {
  String CUSTOM = "custom";
  String MAVEN = "mvn";
  String GWT = "gwt";
  String RUN_COMMAND = "run";
  String BUILD_COMMAND = "build";
  String CLEAN_BUILD_COMMAND = "clean build";
  String DEBUG_COMMAND = "debug";
  String BUILD_AND_RUN_COMMAND = "build and run";
  String STOP_TOMCAT_COMMAND = "stop tomcat";
  String RUN_TOMCAT_COMMAND = "run tomcat";
  String INSTALL_DEPENDENCIES_COMMAND = "install dependencies";
  String UPDATE_DEPENDENCIES_COMMAND = "update dependencies";
  String START_APACHE_COMMAND = "start apache";
  String STOP_APACHE_COMMAND = "stop apache";
  String RESTART_APACHE_COMMAND = "restart apache";
}
