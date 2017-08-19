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

  @Key("workspaceStopped.description")
  String workspaceStoppedDescription();

  @Key("downloadOutputs")
  String downloadOutputs();
}
