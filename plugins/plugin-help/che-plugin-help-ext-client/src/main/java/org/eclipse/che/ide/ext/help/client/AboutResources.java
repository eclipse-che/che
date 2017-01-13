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
package org.eclipse.che.ide.ext.help.client;

import com.google.gwt.resources.client.ClientBundle;

import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

public interface AboutResources extends ClientBundle {

    @Source("actions/about.svg")
    SVGResource about();

    @Source("actions/support.svg")
    SVGResource getSupport();

    @Source("About.css")
    AboutCss aboutCss();

    interface AboutCss extends CssResource {
        String emptyBorder();

        String label();

        String spacing();

        String value();

        String mainText();

        String logo();
    }
}
