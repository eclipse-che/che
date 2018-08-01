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
package org.eclipse.che.ide.api.resources.marker;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Marker that provides the human presentable text for specific resource.
 *
 * @author Vlad Zhukovskiy
 * @see Resource#getMarker(String)
 * @since 4.4.0
 */
@Beta
public class PresentableTextMarker implements Marker {

  public static final String ID = "displayNameMarker";

  private String presentableText;

  public PresentableTextMarker(String presentableText) {
    this.presentableText = presentableText;
  }

  /** {@inheritDoc} */
  @Override
  public String getType() {
    return ID;
  }

  /**
   * Returns the human presentable text.
   *
   * @return the presentable text
   * @since 4.4.0
   */
  public String getPresentableText() {
    return presentableText;
  }
}
