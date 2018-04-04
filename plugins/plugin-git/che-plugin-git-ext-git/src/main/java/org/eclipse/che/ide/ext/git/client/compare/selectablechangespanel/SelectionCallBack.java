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
package org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel;

import org.eclipse.che.ide.resource.Path;

/**
 * Delegates select action into {@link SelectableChangesPanelPresenter}.
 *
 * @author Igor Vinokur
 */
public interface SelectionCallBack {

  /**
   * Is called when item check-box changed selection state.
   *
   * @param path path of the selected item
   * @param isChecked checkbox selection state
   */
  void onSelectionChanged(Path path, boolean isChecked);
}
