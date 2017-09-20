// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util;

import com.google.gwt.resources.client.ImageResource;
import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;
import elemental.html.DivElement;
import org.eclipse.che.ide.util.dom.Elements;

/** Utilities for applying {@link com.google.gwt.resources.client.ImageResource}s to elements. */
public class ImageResourceUtils {

  /**
   * Applies the image resource to the specified element. The image will be centered, and the height
   * and width will be set to the height and width of the image.
   */
  public static void applyImageResource(Element elem, ImageResource image) {
    applyImageResource(elem, image, "center", "center");
    elem.getStyle().setHeight(image.getHeight(), "px");
    elem.getStyle().setWidth(image.getWidth(), "px");
  }

  /**
   * Applies the image resource to the specified element with the specified horizontal and vertical
   * background positions.
   *
   * <p>The height and width of the element are not modified under the presumption that if you
   * specify the horizontal and vertical position, the image will not be the only content of the
   * element.
   */
  public static void applyImageResource(
      Element elem, ImageResource image, String hPos, String vPos) {
    CSSStyleDeclaration style = elem.getStyle();
    style.setBackgroundImage("url(" + image.getSafeUri().asString() + ")");
    style.setProperty("background-repeat", "no-repeat");
    style.setProperty("background-position", hPos + " " + vPos);
    style.setOverflow("hidden");
  }

  /** Creates a div from the specified {@link com.google.gwt.resources.client.ImageResource}. */
  public static DivElement createImageElement(ImageResource image) {
    DivElement elem = Elements.createDivElement();
    applyImageResource(elem, image);
    return elem;
  }
}
