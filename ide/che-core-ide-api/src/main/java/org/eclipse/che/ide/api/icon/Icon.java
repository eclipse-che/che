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
package org.eclipse.che.ide.api.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import org.eclipse.che.commons.annotation.Nullable;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Icon.
 *
 * @author Artem Zatsarynnyi
 */
public class Icon {
  private final String id;
  private final String sourcePath;
  private final SVGResource svgResource;
  private final ImageResource imageResource;

  /**
   * Create a new icon based on the specified image path.
   *
   * @param id icon id
   * @param sourcePath relative path to image within the GWT module's public folder, e.g.
   *     my-extension/icon.png
   */
  public Icon(String id, String sourcePath) {
    this.id = id;
    this.sourcePath = sourcePath;
    this.svgResource = null;
    this.imageResource = null;
  }

  /**
   * Create a new icon based on the specified {@link SVGResource}.
   *
   * @param id icon id
   * @param svgResource resource that contains SVG
   */
  public Icon(String id, SVGResource svgResource) {
    this.id = id;
    this.sourcePath = null;
    this.svgResource = svgResource;
    this.imageResource = null;
  }

  /**
   * Create a new icon based on the specified {@link ImageResource}.
   *
   * @param id icon's id
   * @param imageResource resource that contains image
   */
  public Icon(String id, ImageResource imageResource) {
    this.id = id;
    this.sourcePath = null;
    this.imageResource = imageResource;
    this.svgResource = null;
  }

  /**
   * Creates new icon.
   *
   * @param id icon id
   * @param sourcePath relative path to image within the GWT module's public folder, e.g.
   *     my-extension/icon.png
   * @param svgResource resource that contains SVG
   */
  public Icon(String id, String sourcePath, SVGResource svgResource) {
    this.id = id;
    this.sourcePath = sourcePath;
    this.svgResource = svgResource;
    this.imageResource = null;
  }

  /**
   * Icon id.
   *
   * @return icon id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns {@link Image} widget.
   *
   * @return {@link Image} widget
   */
  @Nullable
  public Image getImage() {
    if (sourcePath != null) {
      return new Image(GWT.getModuleBaseForStaticFiles() + sourcePath);
    } else if (imageResource != null) {
      return new Image(imageResource);
    } else {
      return null;
    }
  }

  /**
   * Returns {@link SVGImage} widget.
   *
   * @return {@link SVGImage} widget
   */
  @Nullable
  public SVGImage getSVGImage() {
    if (svgResource == null) {
      return null;
    }
    return new SVGImage(svgResource);
  }

  /**
   * Returns {@link SVGResource} widget.
   *
   * @return {@link SVGResource} widget
   */
  @Nullable
  public SVGResource getSVGResource() {
    return svgResource;
  }
}
