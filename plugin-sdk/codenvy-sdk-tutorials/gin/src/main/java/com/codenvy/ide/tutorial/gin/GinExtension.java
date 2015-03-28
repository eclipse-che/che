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
package com.codenvy.ide.tutorial.gin;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.tutorial.gin.annotation.MyClassWithAnnotationParam;
import com.codenvy.ide.tutorial.gin.annotation.SimpleClass;
import com.codenvy.ide.tutorial.gin.annotation.SimpleInterface;
import com.codenvy.ide.tutorial.gin.factory.MyFactory;
import com.codenvy.ide.tutorial.gin.factory.MyFactoryClass;
import com.codenvy.ide.tutorial.gin.factory.assited.SomeInterface;
import com.codenvy.ide.tutorial.gin.named.MyClassWithNamedParam;
import com.codenvy.ide.tutorial.gin.part.TutorialHowToPresenter;
import com.codenvy.ide.tutorial.gin.sample.MyClass;
import com.codenvy.ide.tutorial.gin.sample.MyClassWithProvideParam;
import com.codenvy.ide.tutorial.gin.singleton.MySingletonClass;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

/** Extension used to demonstrate how to use GIN. */
@Singleton
@Extension(title = "Gin tutorial", version = "1.0.0")
public class GinExtension {

    @Inject
    public GinExtension(MyClass myClass,
                        MySingletonClass mySingletonClass,
                        Provider<MyClass> myClassProvider,
                        Provider<MySingletonClass> mySingletonClassProvider,
                        AsyncProvider<MyClass> myClassAsyncProvider,
                        MyFactory myFactory,
                        MyClassWithNamedParam myClassWithNamedParam,
                        MyClassWithProvideParam myClassWithProvideParam,
                        MyClassWithAnnotationParam myClassWithAnnotationParam,
                        @SimpleClass SimpleInterface simpleInterface,
                        WorkspaceAgent workspaceAgent,
                        TutorialHowToPresenter howToPresenter) {

        workspaceAgent.openPart(howToPresenter, EDITING);

        myClass.doSomething();
        mySingletonClass.doSomething();

        MyClass myClass1 = myClassProvider.get();
        MyClass myClass2 = myClassProvider.get();

        assert myClass != myClass1;
        assert myClass != myClass2;
        assert myClass1 != myClass2;

        MySingletonClass mySingletonClass1 = mySingletonClassProvider.get();
        MySingletonClass mySingletonClass2 = mySingletonClassProvider.get();

        assert mySingletonClass == mySingletonClass1;
        assert mySingletonClass == mySingletonClass2;

        myClassAsyncProvider.get(new AsyncCallback<MyClass>() {
            @Override
            public void onSuccess(MyClass result) {
                result.doSomething();
            }

            @Override
            public void onFailure(Throwable caught) {
                Log.info(GinExtension.class, caught.getMessage());
            }
        });

        MyFactoryClass myFactoryClass1 = myFactory.createMyFactoryClass("my factory class 1");
        myFactoryClass1.doSomething();
        MyFactoryClass myFactoryClass2 = myFactory.createMyFactoryClass("my factory class 2");
        myFactoryClass2.doSomething();

        assert myFactoryClass1 != myFactoryClass2;
        assert !myFactoryClass1.equals(myFactoryClass2);

        SomeInterface someInterface = myFactory.createSomeInterface("some interface 1");
        someInterface.doSomething();
        SomeInterface someInterface2 = myFactory.createSomeInterface("some interface 2");
        someInterface2.doSomething();

        assert someInterface != someInterface2;
        assert !someInterface.equals(someInterface2);

        myClassWithNamedParam.doSomething();
        myClassWithProvideParam.doSomething();
        myClassWithAnnotationParam.doSomething();
        simpleInterface.doSomething();
    }
}