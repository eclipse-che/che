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
package org.eclipse.che.ide.user;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.api.user.UserServiceClient;

/**
 * GIN module for configuring User API components.
 *
 * @author Artem Zatsarynnyi
 */
public class UserApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(UserServiceClient.class).to(UserServiceClientImpl.class).in(Singleton.class);
        bind(UserProfileServiceClient.class).to(UserProfileServiceClientImpl.class).in(Singleton.class);

        GinMapBinder.newMapBinder(binder(), String.class, Component.class).addBinding("Profile").to(ProfileComponent.class);
    }
}
