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
package org.eclipse.che.env.local.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.env.local.client.ActionDenyAccessDialogLocalEnv;
import org.eclipse.che.env.local.client.ActionPermitLocalEnv;
import org.eclipse.che.ide.api.action.permits.ActionDenyAccessDialog;
import org.eclipse.che.ide.api.action.permits.ActionPermit;
import org.eclipse.che.ide.api.action.permits.Build;
import org.eclipse.che.ide.api.action.permits.Run;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Vitaly Parfonov
 */
@ExtensionGinModule
public class LocalEnvironmentGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ActionPermit.class).annotatedWith(Build.class).to(ActionPermitLocalEnv.class).in(Singleton.class);
        bind(ActionDenyAccessDialog.class).annotatedWith(Build.class).to(ActionDenyAccessDialogLocalEnv.class).in(Singleton.class);
        bind(ActionPermit.class).annotatedWith(Run.class).to(ActionPermitLocalEnv.class).in(Singleton.class);
        bind(ActionDenyAccessDialog.class).annotatedWith(Run.class).to(ActionDenyAccessDialogLocalEnv.class).in(Singleton.class);
    }
}