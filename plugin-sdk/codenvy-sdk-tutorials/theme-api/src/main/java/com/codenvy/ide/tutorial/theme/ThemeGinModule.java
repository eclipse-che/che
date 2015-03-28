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
package com.codenvy.ide.tutorial.theme;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.theme.Theme;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

/**
 * @author Evgen Vidolob
 */
@ExtensionGinModule
public class ThemeGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder<Theme> themeBinder = GinMultibinder.newSetBinder(binder(), Theme.class);
        themeBinder.addBinding().to(DarkThemeExt.class);
    }
}
