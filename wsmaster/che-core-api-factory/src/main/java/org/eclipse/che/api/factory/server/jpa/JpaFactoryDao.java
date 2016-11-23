/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.che.core.db.event.CascadeRemovalEventSubscriber;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * @author Anton Korneta
 */
@Singleton
public class JpaFactoryDao implements FactoryDao {
    private static final Logger LOG = LoggerFactory.getLogger(JpaFactoryDao.class);

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public FactoryImpl create(FactoryImpl factory) throws ConflictException, ServerException {
        requireNonNull(factory);
        try {
            doCreate(factory);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException(ex.getLocalizedMessage());
        } catch (IntegrityConstraintViolationException ex) {
            throw new ConflictException("Could not create factory with creator that refers on non-existent user");
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
        return factory;
    }

    @Override
    public FactoryImpl update(FactoryImpl update) throws NotFoundException, ConflictException, ServerException {
        requireNonNull(update);
        try {
            return doUpdate(update);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException(ex.getLocalizedMessage());
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id);
        try {
            doRemove(id);
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    @Transactional
    public FactoryImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id);
        try {
            final FactoryImpl factory = managerProvider.get().find(FactoryImpl.class, id);
            if (factory == null) {
                throw new NotFoundException(format("Factory with id '%s' doesn't exist", id));
            }
            return factory;
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    @Transactional
    public List<FactoryImpl> getByAttribute(int maxItems,
                                            int skipCount,
                                            List<Pair<String, String>> attributes) throws ServerException {
        try {
            LOG.info("FactoryDao#getByAttributes #maxItems: {} #skipCount: {}, #attributes: {}", maxItems, skipCount, attributes);
            final Map<String, String> params = new HashMap<>();
            String query = "SELECT factory FROM Factory factory";
            if (!attributes.isEmpty()) {
                final StringJoiner matcher = new StringJoiner(" AND ", " WHERE ", " ");
                int i = 0;
                for (Pair<String, String> attribute : attributes) {
                    final String parameterName = "parameterName" + i++;
                    params.put(parameterName, attribute.second);
                    matcher.add("factory." + attribute.first + " = :" + parameterName);
                }
                query = query + matcher;
            }
            final TypedQuery<FactoryImpl> typedQuery = managerProvider.get()
                                                                      .createQuery(query, FactoryImpl.class)
                                                                      .setFirstResult(skipCount)
                                                                      .setMaxResults(maxItems);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                typedQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return typedQuery.getResultList();
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Transactional
    protected void doCreate(FactoryImpl factory) {
        final EntityManager manager = managerProvider.get();
        if (factory.getWorkspace() != null) {
            factory.getWorkspace().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
        manager.persist(factory);
    }

    @Transactional
    protected FactoryImpl doUpdate(FactoryImpl update) throws NotFoundException {
        final EntityManager manager = managerProvider.get();
        if (manager.find(FactoryImpl.class, update.getId()) == null) {
            throw new NotFoundException(format("Could not update factory with id %s because it doesn't exist", update.getId()));
        }
        if (update.getWorkspace() != null) {
            update.getWorkspace().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
        return manager.merge(update);
    }

    @Transactional
    protected void doRemove(String id) {
        final EntityManager manager = managerProvider.get();
        final FactoryImpl factory = manager.find(FactoryImpl.class, id);
        if (factory != null) {
            manager.remove(factory);
        }
    }

    @Singleton
    public static class RemoveFactoriesBeforeUserRemovedEventSubscriber
            extends CascadeRemovalEventSubscriber<BeforeUserRemovedEvent> {
        @Inject
        private FactoryDao   factoryDao;
        @Inject
        private EventService eventService;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeUserRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
        }

        @Override
        public void onRemovalEvent(BeforeUserRemovedEvent event) throws Exception {
            final Pair<String, String> factoryCreator = Pair.of("creator.userId", event.getUser().getId());
            for (FactoryImpl factory : factoryDao.getByAttribute(0, 0, singletonList(factoryCreator))) {
                factoryDao.remove(factory.getId());
            }
        }
    }
}
