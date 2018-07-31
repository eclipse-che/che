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
package org.eclipse.che.ide.editor.orion.client.signature;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
public interface SignatureHelpResources extends ClientBundle {

  @Source("SignatureHelp.css")
  SignatureHelpCss css();

  @Source("arrow.svg")
  SVGResource arrow();

  interface SignatureHelpCss extends CssResource {

    String next();

    String visible();

    String buttons();

    String overloads();

    String previous();

    String documentation();

    String multiple();

    String active();

    String main();

    String wrapper();

    @ClassName("documentation-parameter")
    String documentationParameter();

    String signatures();

    String button();

    @ClassName("parameter-hints-widget")
    String parameterHintsWidget();

    String parameter();
  }
}
