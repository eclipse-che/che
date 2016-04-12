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
package org.eclipse.che.ide.ext.java.jdi.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.java.jdi.client.configuration.JavaDebugConfigurationPageView;
import org.eclipse.che.ide.ext.java.jdi.client.configuration.JavaDebugConfigurationPageViewImpl;
import org.eclipse.che.ide.ext.java.jdi.client.configuration.JavaDebugConfigurationType;

/**
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class JavaRuntimeGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class).addBinding().to(JavaDebugConfigurationType.class);
        bind(JavaDebugConfigurationPageView.class).to(JavaDebugConfigurationPageViewImpl.class).in(Singleton.class);
    }
}
