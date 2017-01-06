/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.env.local.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.env.local.client.CheConnectionClosedInformer;
import org.eclipse.che.env.local.client.CheWorkspaceStoppedHandler;
import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Vitaly Parfonov
 */
@ExtensionGinModule
public class LocalEnvironmentGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ConnectionClosedInformer.class).to(CheConnectionClosedInformer.class).in(Singleton.class);
    }
}
