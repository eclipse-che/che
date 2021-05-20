/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;

/**
 * Command that can be used to create {@link Process} in a machine
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 */
public interface Command {

  /**
   * {@link Command} attribute which indicates the working directory where the given command must be
   * run
   */
  String WORKING_DIRECTORY_ATTRIBUTE = "workingDir";

  /**
   * {@link Command} attribute which indicates in which machine command must be run. It is optional,
   * IDE should asks user to choose machine if null.
   */
  String MACHINE_NAME_ATTRIBUTE = "machineName";

  /**
   * Optional {@link Command} attribute to store full url of the view of the command. This url
   * should be opened on command run.
   */
  String PREVIEW_URL_ATTRIBUTE = "previewUrl";

  /**
   * {@link Command} attribute which indicates in which plugin command must be run. If specified
   * plugin has multiple containers then first containers should be used. Attribute value has the
   * following format: `{PLUGIN_PUBLISHER}/{PLUGIN_NAME}/{PLUGIN_VERSION}`. For example:
   * eclipse/sample-plugin/0.0.1
   */
  String PLUGIN_ATTRIBUTE = "plugin";

  /**
   * An attribute of the command to store the original path to the file that contains the editor
   * specific configuration.
   */
  String COMMAND_ACTION_REFERENCE_ATTRIBUTE = "actionReference";

  /** The contents of editor-specific content. */
  String COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE = "actionReferenceContent";

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

  /** @return preview url of the command or null if no preview url specified */
  PreviewUrl getPreviewUrl();

  /**
   * Returns attributes related to this command.
   *
   * @return command attributes
   */
  Map<String, String> getAttributes();
}
