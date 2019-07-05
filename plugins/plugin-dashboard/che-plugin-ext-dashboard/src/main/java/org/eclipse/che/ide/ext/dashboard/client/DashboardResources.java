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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Dashboard extension resources (css styles, images).
 *
 * @author Oleksii Orel
 */
public interface DashboardResources extends ClientBundle {
  interface DashboardCSS extends CssResource {

    String dashboardArrow();
  }

  @Source({"Dashboard.css", "org/eclipse/che/ide/api/ui/style.css"})
  DashboardCSS dashboardCSS();
}
