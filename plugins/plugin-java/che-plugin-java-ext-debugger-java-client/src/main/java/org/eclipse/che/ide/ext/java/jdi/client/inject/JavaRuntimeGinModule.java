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
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerServiceClient;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerToolbar;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerView;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerViewImpl;
import org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebuggerServiceClientImpl;
import org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue.ChangeValueView;
import org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue.ChangeValueViewImpl;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionView;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionViewImpl;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.storage.BrowserLocalStorageProviderImpl;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class JavaRuntimeGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(DebuggerServiceClient.class).to(JavaDebuggerServiceClientImpl.class).in(Singleton.class);
        bind(DebuggerView.class).to(DebuggerViewImpl.class).in(Singleton.class);
        bind(EvaluateExpressionView.class).to(EvaluateExpressionViewImpl.class).in(Singleton.class);
        bind(ChangeValueView.class).to(ChangeValueViewImpl.class).in(Singleton.class);
        bind(LocalStorageProvider.class).to(BrowserLocalStorageProviderImpl.class).in(Singleton.class);
        bind(ToolbarPresenter.class).annotatedWith(DebuggerToolbar.class).to(ToolbarPresenter.class).in(Singleton.class);
    }
}
