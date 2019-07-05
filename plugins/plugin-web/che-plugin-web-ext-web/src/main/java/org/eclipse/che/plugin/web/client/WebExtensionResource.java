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
package org.eclipse.che.plugin.web.client;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Nikolay Zamosenchuk
 * @author Sébastien Demanou
 */
public interface WebExtensionResource extends ClientBundle {

  @Source("css.svg")
  SVGResource cssFile();

  @Source("less.svg")
  SVGResource lessFile();

  @Source("html.svg")
  SVGResource htmlFile();

  @Source("vue.svg")
  SVGResource vueFile();

  @Source("js.svg")
  SVGResource jsFile();

  @Source("php.svg")
  SVGResource phpFile();

  @Source("category/js.svg")
  SVGResource samplesCategoryJs();

  @Source("ts.svg")
  SVGResource tsFile();

  @Source("jsx.svg")
  SVGResource jsxFile();

  @Source("es6.svg")
  SVGResource es6File();
}
