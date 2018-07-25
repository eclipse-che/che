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
package org.eclipse.che.ide.api.editor.codeassist;

import elemental.dom.Element;

/** Action triggered when completion proposal additional info must be displayed. */
public interface AdditionalInfoCallback {

  /**
   * Display the proposal additional info.
   *
   * @param pixelX the x coordinate
   * @param pixelY the y coordinate
   * @param info the info message to show
   * @return the element used to display the information
   */
  Element onAdditionalInfoNeeded(float pixelX, float pixelY, Element infoWidget);
}
