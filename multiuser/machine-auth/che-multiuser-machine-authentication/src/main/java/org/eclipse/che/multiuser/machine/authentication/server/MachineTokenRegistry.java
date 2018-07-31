/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import static io.jsonwebtoken.SignatureAlgorithm.RS256;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.multiuser.machine.authentication.shared.Constants;

/**
 * Table-based storage of machine security tokens. Table rows is workspace id's, columns - user
 * id's. Table is synchronized externally as required by its javadoc.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @see HashBasedTable
 */
@Singleton
public class MachineTokenRegistry {

  private final SignatureKeyManager signatureKeyManager;
  private final UserManager userManager;
  private final Table<String, String, String> tokens;
  private final ReadWriteLock lock;

  @Inject
  public MachineTokenRegistry(SignatureKeyManager signatureKeyManager, UserManager userManager) {
    this.signatureKeyManager = signatureKeyManager;
    this.userManager = userManager;
    this.tokens = HashBasedTable.create();
    this.lock = new ReentrantReadWriteLock();
  }

  /**
   * Gets or creates machine security token for user and workspace. For running workspace, there is
   * always at least one token for user who performed start.
   *
   * @param userId id of user to get token
   * @param workspaceId id of workspace to get token
   * @return machine security token for for given user and workspace
   * @throws IllegalStateException when user with given id not found or any errors occurs
   */
  public String getOrCreateToken(String userId, String workspaceId) {
    lock.writeLock().lock();
    try {
      final Map<String, String> wsRow = tokens.row(workspaceId);
      String token = wsRow.get(userId);
      if (token == null) {
        token = createToken(userId, workspaceId);
      }
      return token;
    } catch (NotFoundException | ServerException ex) {
      throw new IllegalStateException(
          format(
              "Failed to generate machine token for user '%s' and workspace '%s'. Cause: '%s'",
              userId, workspaceId, ex.getMessage()),
          ex);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /** Creates new token with given data. */
  private String createToken(String userId, String workspaceId)
      throws NotFoundException, ServerException {
    final PrivateKey privateKey = signatureKeyManager.getKeyPair().getPrivate();
    final User user = userManager.getById(userId);
    final Map<String, Object> header = new HashMap<>(2);
    header.put("kind", MACHINE_TOKEN_KIND);
    header.put("kid", workspaceId);
    final Map<String, Object> claims = new HashMap<>();
    // to ensure that each token is unique
    claims.put(Claims.ID, UUID.randomUUID().toString());
    claims.put(Constants.USER_ID_CLAIM, userId);
    claims.put(Constants.USER_NAME_CLAIM, user.getName());
    claims.put(Constants.WORKSPACE_ID_CLAIM, workspaceId);
    // jwtproxy required claims
    claims.put(Claims.ISSUER, "wsmaster");
    claims.put(Claims.AUDIENCE, workspaceId);
    claims.put(Claims.EXPIRATION, Instant.now().plus(365, DAYS).getEpochSecond());
    claims.put(Claims.NOT_BEFORE, -1); // always
    claims.put(Claims.ISSUED_AT, Instant.now().getEpochSecond());
    final String token =
        Jwts.builder().setClaims(claims).setHeader(header).signWith(RS256, privateKey).compact();
    tokens.put(workspaceId, userId, token);
    return token;
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
