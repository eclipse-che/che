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
package org.eclipse.che.plugin.embedjsexample.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.embedjsexample.ide.view.HelloWorldView;
import org.eclipse.che.plugin.embedjsexample.ide.view.HelloWorldViewImpl;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@ExtensionGinModule
public class HelloWorldViewExampleGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(HelloWorldView.class).to(HelloWorldViewImpl.class);
    }
    
}
