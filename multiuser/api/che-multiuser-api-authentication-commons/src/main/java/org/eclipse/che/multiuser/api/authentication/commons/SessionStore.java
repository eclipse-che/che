/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.authentication.commons;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

/**
 * Thread safe {@link HttpSession} storage based on {@link ConcurrentHashMap}. Sessions are stored
 * per user Id, and should be externally aligned with catalina session manager using {@link
 * javax.servlet.http.HttpSessionListener} etc.
 */
@Singleton
public class SessionStore {

  private final ConcurrentHashMap<String, HttpSession> userIdToSession;

  public SessionStore() {
    this.userIdToSession = new ConcurrentHashMap<>();
  }

  /** Fetches stored session if present or creates new using provided function */
  public HttpSession getSession(
      String userId, Function<? super String, ? extends HttpSession> createSessionFunction) {
    return userIdToSession.computeIfAbsent(userId, createSessionFunction);
  }

  /** Fetches stored session if present or returns {@code null} otherwise */
  public HttpSession getSession(String userId) {
    return userIdToSession.get(userId);
  }

  /** Removes stored session */
  public void remove(String userId) {
    userIdToSession.remove(userId);
  }
}
