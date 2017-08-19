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
