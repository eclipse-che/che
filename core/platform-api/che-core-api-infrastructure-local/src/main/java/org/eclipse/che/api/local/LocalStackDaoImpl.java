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
package org.eclipse.che.api.local;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.stack.Stack;
import org.eclipse.che.api.workspace.server.spi.StackDao;

import org.eclipse.che.commons.annotation.Nullable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Implementation local storage for {@link Stack}
 *
 * @author Alexander Andrienko
 */
@Singleton
public class LocalStackDaoImpl implements StackDao {

    private final StackLocalStorage      stackStorage;
    private final Map<String, StackImpl> stacks;
    private final ReadWriteLock          lock;

    @Inject
    public LocalStackDaoImpl(StackLocalStorage stackLocalStorage) throws IOException {
        this.stackStorage = stackLocalStorage;
        this.stacks = new LinkedHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @PostConstruct
    public void start() {
        lock.readLock().lock();
        stacks.putAll(stackStorage.loadMap());
        lock.readLock().unlock();
    }

    @PreDestroy
    public void stop() throws IOException {
        lock.writeLock().lock();
        stackStorage.store(stacks);
        lock.writeLock().unlock();
    }

    @Override
    public void create(StackImpl stack) throws ConflictException, ServerException {
        requireNonNull(stack, "Stack required");
        lock.writeLock().lock();
        try {
            if (stacks.containsKey(stack.getId())) {
                throw new ConflictException(format("Stack with id %s is already exist", stack.getId()));
            }
            stacks.put(stack.getId(), stack);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public StackImpl getById(String id) throws NotFoundException {
        requireNonNull(id, "Stack id required");
        lock.readLock().lock();
        try {
            final StackImpl stack = stacks.get(id);
            if (stack == null) {
                throw new NotFoundException(format("Stack with id %s was not found", id));
            }
            return new StackImpl(stack);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Stack id required");
        lock.writeLock().lock();
        try {
            stacks.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(StackImpl update) throws NotFoundException, ServerException {
        requireNonNull(update, "Stack required");
        requireNonNull(update.getId(), "Stack id required");
        lock.writeLock().lock();
        try {
            String updateId = update.getId();
            if (!stacks.containsKey(updateId)) {
                throw new NotFoundException(format("Stack with id %s was not found", updateId));
            }
            stacks.replace(updateId, update);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<StackImpl> getByCreator(String creator, int skipCount, int maxItems) {
        requireNonNull(creator, "Stack creator required");
        lock.readLock().lock();
        try {
            return stacks.values().stream()
                         .skip(skipCount)
                         .filter(stack -> creator.equals(stack.getCreator()))
                         .limit(maxItems)
                         .map(StackImpl::new)
                         .collect(toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<StackImpl> searchStacks(@Nullable List<String> tags, int skipCount, int maxItems) {
        lock.readLock().lock();
        try {
            return stacks.values().stream()
                         .skip(skipCount)
                         .filter(decoratedStack -> tags == null || decoratedStack.getTags().containsAll(tags))
                         .limit(maxItems)
                         .map(StackImpl::new)
                         .collect(toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
