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
import jsinterop.annotations.JsFunction;

/**
 * Factory to create some image Element, for example it's may be from GWT Image/SVG resource.
 * Should return new element each time called.
 * @author Yevhen Vydolob
 */
@JsFunction
public interface ImageElementFactory {

  Element create();
}
