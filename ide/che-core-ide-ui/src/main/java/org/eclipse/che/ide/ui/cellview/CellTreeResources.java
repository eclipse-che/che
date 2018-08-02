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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.CellTree;

/** @author Valeriy Svydenko */
public interface CellTreeResources extends CellTree.Resources {
  interface CellTreeStyle extends CellTree.Style {}

  @Override
  @ClientBundle.Source({"cellTree.css", "org/eclipse/che/ide/api/ui/style.css"})
  CellTreeStyle cellTreeStyle();
}
