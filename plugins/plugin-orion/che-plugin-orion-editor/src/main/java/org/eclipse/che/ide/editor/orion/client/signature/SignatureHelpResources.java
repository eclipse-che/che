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
package org.eclipse.che.ide.editor.orion.client.signature;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Evgen Vidolob
 */
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
