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
package org.eclipse.che.plugin.java.plain.client.wizard.selector;

import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Delegate which handles result of the node selection.
 *
 * @author Valeriy Svydenko
 */
public interface SelectionDelegate {

  /**
   * Fires when some nodes was selected.
   *
   * @param selectedNodes list of the selected nodes
   */
  void onNodeSelected(List<Node> selectedNodes);
}
