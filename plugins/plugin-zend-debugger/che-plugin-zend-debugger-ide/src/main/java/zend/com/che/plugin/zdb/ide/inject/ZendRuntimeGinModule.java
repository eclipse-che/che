/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import zend.com.che.plugin.zdb.ide.configuration.ZendDebugConfigurationPageView;
import zend.com.che.plugin.zdb.ide.configuration.ZendDebugConfigurationPageViewImpl;
import zend.com.che.plugin.zdb.ide.configuration.ZendDebugConfigurationType;

import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * Zend debugger runtime GIN module.
 * 
 * @author Bartlomiej Laczkowski
 */
@ExtensionGinModule
public class ZendRuntimeGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class).addBinding().to(ZendDebugConfigurationType.class);
        bind(ZendDebugConfigurationPageView.class).to(ZendDebugConfigurationPageViewImpl.class).in(Singleton.class);
    }
    
}
