/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;

/**
 * Table-based storage of machine security tokens. Table rows is workspace id's, columns - user
 * id's. Table is synchronized externally as required by its javadoc.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @see HashBasedTable
 */
@Singleton
public class MachineTokenRegistry {

  private final Table<String, String, String> tokens = HashBasedTable.create();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Generates new machine security token for given user and workspace.
   *
   * @param userId id of user to generate token for
   * @param workspaceId id of workspace to generate token for
   * @return generated token value
   */
  public String generateToken(String userId, String workspaceId) {
    lock.writeLock().lock();
    try {
      final String token = generate("machine", 128);
      tokens.put(workspaceId, userId, token);
      return token;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Gets or creates machine security token for user and workspace. For running workspace, there is
   * always at least one token for user who performed start.
   *
   * @param userId id of user to get token
   * @param workspaceId id of workspace to get token
   * @return machine security token for for given user and workspace
   * @throws NotFoundException when there is no running workspace with given id
   */
  public String getOrCreateToken(String userId, String workspaceId) throws NotFoundException {
    lock.writeLock().lock();
    try {
      final Map<String, String> wsRow = tokens.row(workspaceId);
      if (wsRow.isEmpty()) {
        throw new NotFoundException(format("No running workspace found with id %s", workspaceId));
      }
      return wsRow.get(userId) == null ? generateToken(userId, workspaceId) : wsRow.get(userId);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Gets userId by machine token
   *
   * @return user identifier
   * @throws NotFoundException when no token exists for given user and workspace
   */
  public String getUserId(String token) throws NotFoundException {
    lock.readLock().lock();
    try {
      for (Table.Cell<String, String, String> tokenCell : tokens.cellSet()) {
        if (tokenCell.getValue().equals(token)) {
          return tokenCell.getColumnKey();
        }
      }
      throw new NotFoundException("User not found for token " + token);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Invalidates machine security tokens for all users of given workspace.
   *
   * @param workspaceId workspace to invalidate tokens
   * @return the copy of the tokens row, where row is a map where key is user id and value is token
   */
  public Map<String, String> removeTokens(String workspaceId) {
    lock.writeLock().lock();
    try {
      final Map<String, String> rowCopy = new HashMap<>(tokens.row(workspaceId));
      tokens.row(workspaceId).clear();
      return rowCopy;
    } finally {
      lock.writeLock().unlock();
    }
  }
}
