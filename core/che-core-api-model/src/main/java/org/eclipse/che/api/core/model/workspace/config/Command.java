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
package org.eclipse.che.api.core.model.workspace.config;

import java.util.Map;

/**
 * Command that can be used to create {@link Process} in a machine
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 */
public interface Command {

  /**
   * Returns command name (i.e. 'start tomcat') The name should be unique per user in one workspace,
   * which means that user may create only one command with the same name in the same workspace
   */
  String getName();

  /**
   * Returns command line (i.e. 'mvn clean install') which is going to be executed
   *
   * <p>Serves as a base for {@link Process} creation.
   */
  String getCommandLine();

  /** Returns command type (i.e. 'maven') */
  String getType();

  /**
   * Returns attributes related to this command.
   *
   * @return command attributes
   */
  Map<String, String> getAttributes();
}
