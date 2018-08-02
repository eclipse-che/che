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
package org.eclipse.che.ide.ui.smartTree.data;

import java.util.List;
import java.util.Map;

/**
 * Store attributes in specified node.
 *
 * @author Vlad Zhukovskiy
 */
public interface HasAttributes {
  /**
   * Get attributes.
   *
   * @return attributes map
   */
  Map<String, List<String>> getAttributes();

  /**
   * Store attributes.
   *
   * @param attributes attributes map
   */
  void setAttributes(Map<String, List<String>> attributes);
}
