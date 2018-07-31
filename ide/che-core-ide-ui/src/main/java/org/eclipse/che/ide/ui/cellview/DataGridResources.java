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
package org.eclipse.che.ide.ui.cellview;

import com.google.gwt.user.cellview.client.DataGrid;

/** @author Evgen Vidolob */
public interface DataGridResources extends DataGrid.Resources {

  public interface DataGridStyle extends DataGrid.Style {}

  @Override
  @Source({"dataGrid.css", "org/eclipse/che/ide/api/ui/style.css"})
  DataGridStyle dataGridStyle();
}
