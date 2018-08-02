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
package org.eclipse.che.ide.ui.cellview;

import com.google.gwt.user.cellview.client.CellTable;

/** @author Evgen Vidolob */
public interface CellTableResources extends CellTable.Resources {
  public interface CellTableStyle extends CellTable.Style {}

  @Override
  @Source({"cellTable.css", "org/eclipse/che/ide/api/ui/style.css"})
  CellTableStyle cellTableStyle();
}
