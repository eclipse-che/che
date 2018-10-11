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
package org.eclipse.che.api.factory.server;

/** Message constants for factory builder. */
public class FactoryConstants {
  public static final String INVALID_PARAMETER_MESSAGE =
      "Passed in an invalid parameter. You either provided a non-valid parameter, or that parameter is not "
          + "accepted for this Factory version. For more information, please visit "
          + "http://docs.codenvy.com/user/project-lifecycle/#configuration-reference";

  public static final String INVALID_VERSION_MESSAGE =
      "You have provided an inaccurate or deprecated Factory Version. For more information, "
          + "please visit https://www.eclipse.org/che/docs/factories_json_reference.html";

  public static final String UNPARSABLE_FACTORY_MESSAGE =
      "We cannot parse the provided factory. For more information, please visit https://www.eclipse.org/che/docs/factories_json_reference.html";

  public static final String MISSING_MANDATORY_MESSAGE =
      "You are missing a mandatory parameter \"%s\".  For more information, please visit https://www.eclipse.org/che/docs/factories_json_reference.html";

  public static final String PARAMETRIZED_INVALID_PARAMETER_MESSAGE =
      "You have provided an invalid parameter \"%s\" for this version of Factory parameters \"%s\". For more "
          + "information, please visit https://www.eclipse.org/che/docs/factories_json_reference.html";

  public static final String INVALID_SINCE_MESSAGE =
      "Since date cannot occur before the current date.";

  public static final String INVALID_UNTIL_MESSAGE =
      "Until date cannot occur before the current date.";

  public static final String INVALID_SINCEUNTIL_MESSAGE =
      "Until date should occur after the Since date.";

  public static final String INVALID_ACTION_SECTION =
      "The action %s is not allowed in this IDE event section.";

  public static final String INVALID_OPENFILE_ACTION =
      "The openFile action requires 'file' property to be set.";

  public static final String INVALID_RUNCOMMAND_ACTION =
      "The runCommand action requires 'name' property to be set.";

  public static final String INVALID_FIND_REPLACE_ACTION =
      "The findReplace action requires 'in', " + "'find' and 'replace' properties to be set.";

  public static final String INVALID_WELCOME_PAGE_ACTION =
      "The openWelcomePage action requires 'greetingContentUrl' property to be set.";

  public static final String ILLEGAL_FACTORY_BY_SINCE_MESSAGE =
      "This Factory is not yet valid due to time restrictions applied by its owner. Please, "
          + "contact owner for more information.";

  public static final String ILLEGAL_FACTORY_BY_UNTIL_MESSAGE =
      "This Factory has expired due to time restrictions applied by its owner. Please, "
          + "contact owner for more information.";

  public static final String PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE =
      "The parameter %s has a value submitted %s with a value that is unexpected. For more information, "
          + "please visit https://www.eclipse.org/che/docs/workspace-data-model.html#projects";

  private FactoryConstants() {}
}
