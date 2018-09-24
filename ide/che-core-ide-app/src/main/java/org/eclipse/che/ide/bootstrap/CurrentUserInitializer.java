/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
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
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.CurrentUserImpl;
import org.eclipse.che.ide.preferences.PreferencesManagerImpl;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

/**
 * Initializes the {@link CurrentUser}:
 *
 * <ul>
 *   <li>loads user's profile;
 *   <li>loads user's preferences.
 * </ul>
 */
@Singleton
class CurrentUserInitializer {

  private final PreferencesManager preferencesManager;
  private final CoreLocalizationConstant messages;
  private final AppContext appContext;
  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

  @Inject
  CurrentUserInitializer(
      PreferencesManagerImpl preferencesManager,
      CoreLocalizationConstant messages,
      AppContext appContext,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
    this.preferencesManager = preferencesManager;
    this.messages = messages;
    this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
  }

  Promise<Void> init() {
    final CurrentUserImpl user = new CurrentUserImpl();

    return loadProfile()
        .thenPromise(
            profile -> {
              user.setId(profile.getUserId());

              return loadPreferences()
                  .then(
                      preferences -> {
                        updateGitCommitterPreferencesIfEmpty(preferences, profile);
                      });
            })
        .then(
            (Function<Map<String, String>, Void>)
                preferences -> {
                  user.setPreferences(preferences);

                  ((AppContextImpl) appContext).setCurrentUser(user);

                  return null;
                });
  }

  private Promise<ProfileDto> loadProfile() {
    return getUserProfile()
        .catchError(
            (Operation<PromiseError>)
                arg -> {
                  throw new OperationException("Unable to load user's profile: " + arg.getCause());
                });
  }

  private Promise<Map<String, String>> loadPreferences() {
    return preferencesManager
        .loadPreferences()
        .catchError(
            (Operation<PromiseError>)
                arg -> {
                  throw new OperationException(
                      messages.unableToLoadPreference() + ": " + arg.getCause());
                });
  }

  private void updateGitCommitterPreferencesIfEmpty(
      Map<String, String> preferences, ProfileDto profile) {
    String name = profile.getAttributes().get("name");
    String email = profile.getEmail();

    if (name == null || email == null) {
      return;
    }

    String gitCommitterNamePreference = "git.committer.name";
    String gitCommitterEmailPreference = "git.committer.email";

    String gitCommitterName = preferences.get(gitCommitterNamePreference);
    String gitCommitterEmail = preferences.get(gitCommitterEmailPreference);

    if (gitCommitterName == null || gitCommitterEmail == null) {
      if (gitCommitterName == null) {
        preferencesManager.setValue(gitCommitterNamePreference, name);
      }
      if (gitCommitterEmail == null) {
        preferencesManager.setValue(gitCommitterEmailPreference, email);
      }
      preferencesManager.flushPreferences();
    }
  }

  private Promise<ProfileDto> getUserProfile() {
    return asyncRequestFactory
        .createGetRequest(appContext.getMasterApiEndpoint() + "/profile/")
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newUnmarshaller(ProfileDto.class));
  }
}
