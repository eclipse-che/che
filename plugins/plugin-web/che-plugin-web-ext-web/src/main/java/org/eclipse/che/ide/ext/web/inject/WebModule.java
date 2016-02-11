/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.web.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.ext.web.WebExtensionResource;

import java.util.Arrays;

/**
 * Adds custom binding for Editors.
 *
 * @author Florent Benoit
 */
@ExtensionGinModule
public class WebModule extends AbstractGinModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named("CSSFileType")
    protected FileType provideCSSFile(WebExtensionResource res) {
        return new FileType("CSS file", res.cssFile(), MimeType.TEXT_CSS, "css");
    }

    @Provides
    @Singleton
    @Named("LESSFileType")
    protected FileType provideLESSFile(WebExtensionResource res) {
        return new FileType("Leaner CSS file", res.lessFile(), MimeType.TEXT_CSS, "less");
    }

    @Provides
    @Singleton
    @Named("JSFileType")
    protected FileType provideJSFile(WebExtensionResource res) {
        return new FileType("javaScript", res.jsFile(), MimeType.TEXT_JAVASCRIPT, "js");
    }

    @Provides
    @Singleton
    @Named("HTMLFileType")
    protected FileType provideHTMLFile(WebExtensionResource res) {
        return new FileType("HTML file", res.htmlFile(), MimeType.TEXT_HTML, "html");
    }

    @Provides
    @Singleton
    @Named("PHPFileType")
    protected FileType providePHPFile(WebExtensionResource res) {
        return new FileType("PHP file", res.phpFile(), Arrays.asList(MimeType.APPLICATION_X_PHP, "text/x-php"), "php");
    }
}
