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
package org.eclipse.che.ide.ui.smartTree.presentation;

import javax.validation.constraints.NotNull;

/**
 * Indicates that specified node can has presentation to allow customize various parameters, e.g.
 * node icon, presentable text, info text, etc.
 *
 * @author Vlad Zhukovskiy
 * @deprecated use {@link HasNewPresentation} instead
 */
@Deprecated
public interface HasPresentation {
  /**
   * Method called during node rendering.
   *
   * @param presentation node presentation
   */
  void updatePresentation(@NotNull NodePresentation presentation);

  NodePresentation getPresentation(boolean update);
}
