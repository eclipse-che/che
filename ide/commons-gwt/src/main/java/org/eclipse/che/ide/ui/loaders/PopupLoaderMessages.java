/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.loaders;

import com.google.gwt.i18n.client.Messages;

/**
 * Messages for Popup loader widget.
 *
 * @author Vitaliy Guliy
 */
public interface PopupLoaderMessages extends Messages {

  @Key("startingWorkspaceRuntime.title")
  String startingWorkspaceRuntime();

  @Key("startingWorkspaceRuntime.description")
  String startingWorkspaceRuntimeDescription();

  @Key("startingWorkspaceAgent.title")
  String startingWorkspaceAgent();

  @Key("startingWorkspaceAgent.description")
  String startingWorkspaceAgentDescription();

  @Key("stoppingWorkspace.title")
  String stoppingWorkspace();

  @Key("stoppingWorkspace.description")
  String stoppingWorkspaceDescription();

  @Key("creatingProject.title")
  String creatingProject();

  @Key("creatingProject.description")
  String creatingProjectDescription();

  @Key("workspaceStopped.title")
  String workspaceStopped();

  @Key("wsAgentStopped.title")
  String wsAgentStopped();

  @Key("workspaceStopped.description")
  String workspaceStoppedDescription();

  @Key("wsAgentStopped.description")
  String wsAgentStoppedDescription();

  @Key("downloadOutputs")
  String downloadOutputs();
}
