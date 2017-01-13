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
package org.eclipse.che.ide.ui.zeroclipboard;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Oleksii Orel
 */
public interface ZeroClipboardResources extends ClientBundle {
    interface Css extends CssResource {
        String clipboardButton();
    }

    @Source({"ZeroClipboard.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css clipboardCss();

    @Source("clipboard.svg")
    SVGResource clipboard();
}
