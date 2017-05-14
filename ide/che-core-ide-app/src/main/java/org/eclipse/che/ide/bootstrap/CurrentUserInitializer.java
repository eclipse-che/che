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
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.preferences.PreferencesManagerImpl;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Initializes the {@link CurrentUser}:
 * <ul>
 * <li>loads user's profile;</li>
 * <li>loads user's preferences.</li>
 * </ul>
 */
@Singleton
class CurrentUserInitializer {

    private final CurrentUser              currentUser;
    private final PreferencesManager       preferencesManager;
    private final CoreLocalizationConstant messages;
    private final AppContext               appContext;
    private final AsyncRequestFactory      asyncRequestFactory;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;

    @Inject
    CurrentUserInitializer(CurrentUser currentUser,
                           PreferencesManagerImpl preferencesManager,
                           CoreLocalizationConstant messages,
                           AppContext appContext,
                           AsyncRequestFactory asyncRequestFactory,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.currentUser = currentUser;
        this.preferencesManager = preferencesManager;
        this.messages = messages;
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    Promise<Void> init() {
        return loadProfile().thenPromise(aVoid -> loadPreferences());
    }

    private Promise<Void> loadProfile() {
        return getUserProfile()
                .then((Function<ProfileDto, Void>)profile -> {
                    currentUser.setProfile(profile);
                    return null;
                })
                .catchError((Operation<PromiseError>)arg -> {
                    throw new OperationException("Unable to load user's profile: " + arg.getCause());
                });
    }

    private Promise<Void> loadPreferences() {
        return preferencesManager.loadPreferences()
                                 .then((Function<Map<String, String>, Void>)preferences -> {
                                     currentUser.setPreferences(preferences);
                                     return null;
                                 })
                                 .catchError((Operation<PromiseError>)arg -> {
                                     throw new OperationException(messages.unableToLoadPreference() + ": " + arg.getCause());
                                 });
    }

    private Promise<ProfileDto> getUserProfile() {
        return asyncRequestFactory.createGetRequest(appContext.getMasterEndpoint() + "/profile/")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ProfileDto.class));
    }
}
