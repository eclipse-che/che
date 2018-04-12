/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server.appstate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;

import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.fs.server.FsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to load or persist serialized IDE state by user identifier. It is expected that incoming
 * IDE state object is valid, so the manager doesn't perform any validations.
 *
 * @author Roman Nikitenko
 */
public class AppStateManager {
  private static final Logger LOG = LoggerFactory.getLogger(AppStateManager.class);

  private static final String USER_DIR_PREFIX = "user_";
  private static final String APP_STATE_HOLDER = "appState";

  private FsManager fsManager;

  @Inject
  public AppStateManager(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  /**
   * Load saved IDE state of current workspace for given user in JSON format. Note: it is expected
   * that saved IDE state object is valid, so any validations are not performed. Empty string will
   * be returned when IDE state is not found.
   *
   * @param userId user identifier
   * @return saved IDE state of current workspace for given user in JSON format
   * @throws ValidationException when user identifier is {@code null} or empty
   * @throws ServerException when any server error occurs
   */
  public String loadAppState(String userId) throws ValidationException, ServerException {
    checkUserIdentifier(userId);

    String appStateHolderPath = getAppStateHolderPath(userId);
    try {
      if (fsManager.existsAsFile(appStateHolderPath)) {
        return fsManager.readAsString(appStateHolderPath);
      }
    } catch (NotFoundException | ConflictException e) {
      LOG.error("Can not load app state for user %s, the reason is: %s", userId, e.getCause());
      throw new ServerException("Can not load app state for user " + userId);
    }
    return "";
  }

  /**
   * Save IDE state of current workspace for given user. Note: it is expected that incoming IDE
   * state object is valid, so any validations are not performed.
   *
   * @param userId user identifier
   * @param json IDE state in JSON format
   * @throws ValidationException when user identifier is {@code null} or empty
   * @throws ServerException when any server error occurs
   */
  public void saveState(String userId, String json) throws ValidationException, ServerException {
    checkUserIdentifier(userId);

    String appStateHolderPath = getAppStateHolderPath(userId);
    try {
      if (fsManager.existsAsFile(appStateHolderPath)) {
        fsManager.update(appStateHolderPath, json);
      } else {
        fsManager.createFile(appStateHolderPath, json, false, true);
      }
    } catch (NotFoundException | ConflictException e) {
      LOG.error("Can not save app state for user %s, the reason is: %s", userId, e.getCause());
      throw new ServerException("Can not save app state for user " + userId);
    }
  }

  /**
   * Checks user identifier is not {@code null} or empty.
   *
   * @param userId user identifier
   * @throws ValidationException when user identifier is {@code null} or empty.
   */
  private static void checkUserIdentifier(String userId) throws ValidationException {
    if (isNullOrEmpty(userId)) {
      throw new ValidationException("User ID should be defined");
    }
  }

  private String getAppStateHolderPath(@NotNull String userId) {
    String userDir = USER_DIR_PREFIX + userId;
    return resolve(absolutize(CHE_DIR), format("%s/%s", userDir, APP_STATE_HOLDER));
  }
}
