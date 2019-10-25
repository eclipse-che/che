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
package org.eclipse.che.multiuser.api.auth.filter;

import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

@Singleton
public class SessionStore {

  private ConcurrentHashMap<String, HttpSession> userIdToSession;

  public SessionStore() {
    this.userIdToSession = new ConcurrentHashMap<>();
  }

  public synchronized HttpSession getSession(String userId) {
    return userIdToSession.get(userId);
  }

  public synchronized HttpSession saveSession(String userId, HttpSession session) {
    return userIdToSession.put(userId, session);
  }

  public synchronized void remove(String userId) {
    userIdToSession.remove(userId);
  }
}
