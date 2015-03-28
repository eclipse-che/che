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
package com.codenvy.ide.tutorial.gin.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.tutorial.gin.annotation.MyString;
import com.codenvy.ide.tutorial.gin.annotation.MyStringProvider;
import com.codenvy.ide.tutorial.gin.annotation.SimpleClass;
import com.codenvy.ide.tutorial.gin.annotation.SimpleImplementation;
import com.codenvy.ide.tutorial.gin.annotation.SimpleInterface;
import com.codenvy.ide.tutorial.gin.factory.MyFactory;
import com.codenvy.ide.tutorial.gin.factory.assited.SomeImplementationWithAssistedParam;
import com.codenvy.ide.tutorial.gin.factory.assited.SomeInterface;
import com.codenvy.ide.tutorial.gin.sample.MyImplementation;
import com.codenvy.ide.tutorial.gin.sample.MyInterface;
import com.codenvy.ide.tutorial.gin.singleton.MySingletonImplementation;
import com.codenvy.ide.tutorial.gin.singleton.MySingletonInterface;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@ExtensionGinModule
public class GinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(MyInterface.class).to(MyImplementation.class);
        bind(MySingletonInterface.class).to(MySingletonImplementation.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().implement(SomeInterface.class, SomeImplementationWithAssistedParam.class)
                                             .build(MyFactory.class));
        bind(String.class).annotatedWith(MyString.class).toProvider(MyStringProvider.class).in(Singleton.class);
        bind(SimpleInterface.class).annotatedWith(SimpleClass.class).to(SimpleImplementation.class).in(Singleton.class);
    }

    @Provides
    @Named("myString")
    @Singleton
    protected String provideMyString() {
        return "my string value from named annotation";
    }

    @Provides
    @Singleton
    protected String provideStringValue(/*ConsolePart console*/) {
        Log.info(GinModule.class, "initialize string value in gin module");
        return "my string value from provider method";
    }
}