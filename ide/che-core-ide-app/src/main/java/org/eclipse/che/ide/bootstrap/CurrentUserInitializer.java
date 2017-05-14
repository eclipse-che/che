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
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.preferences.PreferencesManagerImpl;

import java.util.Map;

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
    private final UserProfileServiceClient userProfileServiceClient;
    private final PreferencesManager       preferencesManager;
    private final CoreLocalizationConstant messages;

    @Inject
    CurrentUserInitializer(CurrentUser currentUser,
                           UserProfileServiceClient userProfileServiceClient,
                           PreferencesManagerImpl preferencesManager,
                           CoreLocalizationConstant messages) {
        this.currentUser = currentUser;
        this.userProfileServiceClient = userProfileServiceClient;
        this.preferencesManager = preferencesManager;
        this.messages = messages;
    }

    Promise<Void> init() {
        return loadProfile().thenPromise(aVoid -> loadPreferences());
    }

    private Promise<Void> loadProfile() {
        return userProfileServiceClient.getCurrentProfile()
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
}
