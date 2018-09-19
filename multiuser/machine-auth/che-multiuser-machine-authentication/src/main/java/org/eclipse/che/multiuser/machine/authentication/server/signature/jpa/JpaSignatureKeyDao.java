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
package org.eclipse.che.multiuser.machine.authentication.server.signature.jpa;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
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
    } catch (IntegrityConstraintViolationException x) {
      throw new ConflictException(
          format(
              "Unable to create signature key pair because referenced workspace with id '%s' doesn't exist",
              keyPair.getWorkspaceId()));

    } catch (DuplicateKeyException dkEx) {
      throw new ConflictException(
          format("Signature key pair for workspace '%s' already exists", keyPair.getWorkspaceId()));
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
  public void remove(String workspaceId) throws ServerException {
    requireNonNull(workspaceId, "Required non-null workspace Id");
    try {
      doRemove(workspaceId);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }

  @Transactional
  protected void doRemove(String workspaceId) {
    final EntityManager manager = managerProvider.get();
    final SignatureKeyPairImpl keyPair = manager.find(SignatureKeyPairImpl.class, workspaceId);
    if (keyPair != null) {
      manager.remove(keyPair);
      manager.flush();
    }
  }

  @Override
  @Transactional
  public SignatureKeyPairImpl get(String workspaceId) throws NotFoundException, ServerException {
    final EntityManager manager = managerProvider.get();
    try {
      return new SignatureKeyPairImpl(
          manager
              .createNamedQuery("SignKeyPair.getAll", SignatureKeyPairImpl.class)
              .setParameter("workspaceId", workspaceId)
              .getSingleResult());
    } catch (NoResultException x) {
      throw new NotFoundException(
          format("Signature key pair for workspace '%s' doesn't exist", workspaceId));
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }

  @Singleton
  public static class RemoveKeyPairsBeforeWorkspaceRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> {
    @Inject private EventService eventService;
    @Inject private SignatureKeyDao signatureKeyDao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
      signatureKeyDao.remove(event.getWorkspace().getId());
    }
  }
}
