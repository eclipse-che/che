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
package org.eclipse.che.ide.ui.button;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Igor Vinokur
 */
public interface ButtonResources extends ClientBundle {

    interface Css extends CssResource {
        String activeConsoleButton();

        String mainButtonIcon();

        String whiteColor();
    }

    @Source("button.css")
    Css buttonCss();
}
