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
