/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.shared.stack.Stack;
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
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Implementation local storage for {@link Stack}
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Alexander Andrienko
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalStackDaoImpl implements StackDao {

    @VisibleForTesting
    final Map<String, StackImpl> stacks;

    private final StackLocalStorage stackStorage;

    @Inject
    public LocalStackDaoImpl(StackLocalStorage stackLocalStorage) throws IOException {
        this.stackStorage = stackLocalStorage;
        this.stacks = new LinkedHashMap<>();
    }

    @PostConstruct
    public synchronized void start() {
        stacks.putAll(stackStorage.loadMap());
    }

    public synchronized void saveStacks() throws IOException {
        stackStorage.store(stacks);
    }

    @Override
    public synchronized void create(StackImpl stack) throws ConflictException, ServerException {
        requireNonNull(stack, "Stack required");
        if (stacks.containsKey(stack.getId())) {
            throw new ConflictException(format("Stack with id %s is already exist", stack.getId()));
        }
        if (stacks.values()
                  .stream()
                  .anyMatch(s -> s.getName().equals(stack.getName()))) {
            throw new ConflictException(format("Stack with name '%s' already exists", stack.getName()));
        }
        stacks.put(stack.getId(), new StackImpl(stack));
    }

    @Override
    public synchronized StackImpl getById(String id) throws NotFoundException {
        requireNonNull(id, "Stack id required");
        final StackImpl stack = stacks.get(id);
        if (stack == null) {
            throw new NotFoundException(format("Stack with id %s was not found", id));
        }
        return new StackImpl(stack);
    }

    @Override
    public synchronized void remove(String id) throws ServerException {
        requireNonNull(id, "Stack id required");
        stacks.remove(id);
    }

    @Override
    public synchronized StackImpl update(StackImpl update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Stack required");
        requireNonNull(update.getId(), "Stack id required");
        String updateId = update.getId();
        if (!stacks.containsKey(updateId)) {
            throw new NotFoundException(format("Stack with id %s was not found", updateId));
        }
        if (stacks.values()
                  .stream()
                  .anyMatch(stack -> stack.getName().equals(update.getName()) && !stack.getId().equals(updateId))) {
            throw new ConflictException(format("Stack with name '%s' already exists", updateId));
        }
        stacks.replace(updateId, new StackImpl(update));
        return new StackImpl(update);
    }

    @Override
    public synchronized List<StackImpl> searchStacks(String user, @Nullable List<String> tags, int skipCount, int maxItems) {
        Stream<StackImpl> stream = stacks.values()
                                         .stream()
                                         .skip(skipCount)
                                         .filter(s -> tags == null || s.getTags().containsAll(tags));
        if (maxItems != 0) {
            stream = stream.limit(maxItems);
        }
        return stream.map(StackImpl::new).collect(toList());
    }
}
