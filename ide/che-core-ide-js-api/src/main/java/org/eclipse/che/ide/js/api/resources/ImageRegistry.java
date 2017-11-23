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

package org.eclipse.che.ide.js.api.resources;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.js.api.Disposable;

/**
 * Holds and manages all IDE icon resources, each resource mapped to their id. We support 3 way to
 * provide image: URL, HTML, image element factory
 *
 * @author Yevhen Vydolob
 */
@JsType
public interface ImageRegistry {

  /**
   * Register image url.
   *
   * @param id the image id
   * @param url the image url
   */
  Disposable registerUrl(String id, String url);

  /**
   * Register image html. For example html may be some FontAwesome icon
   *
   * @param id the image id
   * @param html the image html
   */
  Disposable registerHtml(String id, String html);

  /**
   * Register image factory.Register image factory. For example : factory may provided by GWT plugin
   * which use ClientBundle for images or plugin may construct image element manually.
   *
   * @param id the image id
   * @param factory the image factory
   */
  Disposable registerFactory(String id, ImageElementFactory factory);

  /**
   * Returns new image element each time
   *
   * @param id the image id
   * @return the image element or null if no image provided
   */
  Element getImage(String id);
}
