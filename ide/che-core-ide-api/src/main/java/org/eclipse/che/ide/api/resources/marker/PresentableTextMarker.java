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
