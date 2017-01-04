/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.web;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.resources.client.ClientBundle;

/** @author Nikolay Zamosenchuk */
public interface WebExtensionResource extends ClientBundle {

    @Source("css.svg")
    SVGResource cssFile();

    @Source("less.svg")
    SVGResource lessFile();

    @Source("html.svg")
    SVGResource htmlFile();

    @Source("js.svg")
    SVGResource jsFile();

    @Source("php.svg")
    SVGResource phpFile();

    @Source("category/js.svg")
    SVGResource samplesCategoryJs();
}
