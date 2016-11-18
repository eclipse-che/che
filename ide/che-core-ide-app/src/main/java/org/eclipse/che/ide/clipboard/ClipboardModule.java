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
package org.eclipse.che.ide.clipboard;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilderImpl;

/**
 * GIN module for configuring components related to clipboard support.
 *
 * @author Artem Zatsarynnyi
 */
public class ClipboardModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMapBinder.newMapBinder(binder(), String.class, Component.class)
                    .addBinding("ZeroClipboardInjector")
                    .to(ZeroClipboardInjector.class);

        bind(ClipboardButtonBuilder.class).to(ClipboardButtonBuilderImpl.class);
    }
}
