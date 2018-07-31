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
package org.eclipse.che.ide.ui.smartTree.presentation;

/**
 * Provider for the new type of presentation represented by {@link NewNodePresentation}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.19.0
 */
public interface HasNewPresentation {

  /**
   * Returns a new type of node presentation.
   *
   * @return presentation
   * @see NewNodePresentation
   */
  NewNodePresentation getPresentation();
}
