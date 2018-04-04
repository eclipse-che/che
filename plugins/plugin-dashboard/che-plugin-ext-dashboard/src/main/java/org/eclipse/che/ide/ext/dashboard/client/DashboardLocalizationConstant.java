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
package org.eclipse.che.ide.ext.dashboard.client;

import com.google.gwt.i18n.client.Messages;

/** @author vzhukovskii@codenvy.com */
public interface DashboardLocalizationConstant extends Messages {

  @Key("open.dashboard.toolbar-button.title")
  String openDashboardToolbarButtonTitle();

  @Key("open.dashboard.url.workspace")
  String openDashboardUrlWorkspace(String workspaceID);

  @Key("open.dashboard.url.workspaces")
  String openDashboardUrlWorkspaces();

  @Key("show.dashboard.navbar.toolbar-button.title")
  String showDashboardNavBarToolbarButtonTitle();

  @Key("hide.dashboard.navbar.toolbar-button.title")
  String hideDashboardNavBarToolbarButtonTitle();
}
