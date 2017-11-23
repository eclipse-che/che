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

package org.eclipse.che.ide.js.api.parts;

import com.google.gwt.dom.client.Element;
import javax.validation.constraints.NotNull;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Part is a main UI block of the IDE.
 *
 * @author Yevhen Vydolob
 */
@JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
public interface Part {

  /** @return Title of the Part */
  @NotNull
  String getTitle();

  /**
   * Returns count of unread notifications. Is used to display a badge on part button.
   *
   * @return count of unread notifications
   */
  int getUnreadNotificationsCount();

  /**
   * Returns the title tool tip text of this part. An empty string result indicates no tool tip. If
   * this value changes the part must fire a property listener event with <code>PROP_TITLE</code>.
   *
   * <p>The tool tip text is used to populate the title bar of this part's visual container.
   *
   * @return the part title tool tip (not <code>null</code>)
   */
  @Nullable
  String getTitleToolTip();

  /**
   * Return size of part. If current part is vertical panel then size is height. If current part is
   * horizontal panel then size is width.
   *
   * @return size of part
   */
  int getSize();

  /**
   * This method is called when Part is opened. Note: this method is NOT called when part gets
   * focused. It is called when new tab in PartStack created.
   */
  void onOpen();

  /**
   * The view element that represent UI
   *
   * @return the view element
   */
  Element getView();

  /**
   * The image id of this part.
   *
   * <p>The title image is usually used to populate the title bar of this part's visual container.
   *
   * @return the image id
   */
  String getImageId();
}
