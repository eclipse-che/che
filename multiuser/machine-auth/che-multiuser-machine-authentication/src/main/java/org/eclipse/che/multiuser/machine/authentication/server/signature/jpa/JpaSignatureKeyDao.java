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
package org.eclipse.che.multiuser.machine.authentication.server.signature.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.inject.persist.Transactional;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;

/**
 * JPA based implementation of {@link SignatureKeyDao}.
 *
 * @author Anton Korneta
 */
@Singleton
public class JpaSignatureKeyDao implements SignatureKeyDao {

  private final Provider<EntityManager> managerProvider;

  @Inject
  public JpaSignatureKeyDao(Provider<EntityManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  @Override
  public SignatureKeyPairImpl create(SignatureKeyPairImpl keyPair)
      throws ConflictException, ServerException {
    requireNonNull(keyPair, "Required non-null key pair");
    try {
      doCreate(keyPair);
    } catch (DuplicateKeyException dkEx) {
      throw new ConflictException(
          format("Signature key pair with id '%s' already exists", keyPair.getId()));
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
    return new SignatureKeyPairImpl(keyPair);
  }

  @Transactional
  protected void doCreate(SignatureKeyPairImpl key) {
    final EntityManager manager = managerProvider.get();
    manager.persist(key);
    manager.flush();
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id, "Required non-null key pair");
    try {
      doRemove(id);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }

  @Transactional
  protected void doRemove(String id) {
    final SignatureKeyPairImpl keyPair = managerProvider.get().find(SignatureKeyPairImpl.class, id);
    if (keyPair != null) {
      final EntityManager manager = managerProvider.get();
      manager.remove(keyPair);
      manager.flush();
    }
  }

  @Override
  @Transactional
  public Page<SignatureKeyPairImpl> getAll(int maxItems, long skipCount) throws ServerException {
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(
        skipCount >= 0,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
    try {
      final EntityManager manager = managerProvider.get();
      final List<SignatureKeyPairImpl> list =
          manager
              .createNamedQuery("SignKeyPair.getAll", SignatureKeyPairImpl.class)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(SignatureKeyPairImpl::new)
              .collect(toList());
      final long count =
          manager.createNamedQuery("SignKeyPair.getAllCount", Long.class).getSingleResult();
      return new Page<>(list, skipCount, maxItems, count);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }
}
