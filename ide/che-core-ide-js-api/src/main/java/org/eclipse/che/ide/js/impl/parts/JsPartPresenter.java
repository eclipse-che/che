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

package org.eclipse.che.ide.js.impl.parts;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import org.eclipse.che.ide.api.HasImageElement;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.js.api.parts.Part;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Yevhen Vydolob */
public class JsPartPresenter extends BasePresenter implements HasImageElement {

  private final Part part;
  private final ImageRegistry imageRegistry;
  private final JsPartView view;

  public JsPartPresenter(Part part, ImageRegistry imageRegistry) {
    this.part = part;
    this.imageRegistry = imageRegistry;
    view = new JsPartView();
    view.setDelegate(this);
    view.setContentElement(part.getView());
  }

  @Override
  public String getTitle() {
    return part.getTitle();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return part.getTitleToolTip();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public int getSize() {
    return part.getSize();
  }

  @Override
  public SVGResource getTitleImage() {
    return super.getTitleImage();
  }

  @Override
  public int getUnreadNotificationsCount() {
    return part.getUnreadNotificationsCount();
  }

  @Override
  public void onOpen() {
    part.onOpen();
  }

  @Override
  public Element getImageElement() {
    return imageRegistry.getImage(part.getImageId());
  }
}
