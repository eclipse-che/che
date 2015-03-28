/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.demo;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/** Client resources. */
public interface GistExtensionResources extends ClientBundle {
    public interface GistCSS extends CssResource {
        String textFont();
    }

    @Source({"Gist.css", "org/eclipse/che/ide/api/ui/style.css"})
    GistCSS gistCSS();

    @Source("github.png")
    ImageResource github();
}