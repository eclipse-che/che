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
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * In-memory implementation of {@link SshDao}.
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalSshDaoImpl implements SshDao {

    public static final String FILENAME = "ssh.json";

    private final LocalStorage sshStorage;

    @VisibleForTesting
    final List<SshPairImpl> pairs;

    @Inject
    public LocalSshDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        pairs = new ArrayList<>();
        sshStorage = storageFactory.create(FILENAME);
    }

    @Override
    public synchronized void create(SshPairImpl sshPair) throws ConflictException {
        requireNonNull(sshPair);
        final Optional<SshPairImpl> any = find(sshPair.getOwner(), sshPair.getService(), sshPair.getName());
        if (any.isPresent()) {
            throw new ConflictException(format("Ssh pair with service '%s' and name %s already exist.",
                                               sshPair.getService(),
                                               sshPair.getName()));
        }
        pairs.add(sshPair);
    }

    @Override
    public synchronized SshPairImpl get(String owner, String service, String name) throws NotFoundException {
        requireNonNull(owner);
        requireNonNull(service);
        requireNonNull(name);
        final Optional<SshPairImpl> any = find(owner, service, name);
        if (any.isPresent()) {
            return new SshPairImpl(any.get());
        }
        throw new NotFoundException(format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
    }

    @Override
    public synchronized void remove(String owner, String service, String name) throws NotFoundException {
        requireNonNull(owner);
        requireNonNull(service);
        requireNonNull(name);
        final Optional<SshPairImpl> any = find(owner, service, name);
        if (!any.isPresent()) {
            throw new NotFoundException(format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
        }
        pairs.remove(any.get());
    }

    @Override
    public synchronized List<SshPairImpl> get(String owner) throws ServerException {
        requireNonNull(owner, "Required non-null owner");
        return pairs.stream()
                    .filter(p -> p.getOwner().equals(owner))
                    .map(SshPairImpl::new)
                    .collect(Collectors.toList());
    }

    @Override
    public synchronized List<SshPairImpl> get(String owner, String service) {
        requireNonNull(owner);
        requireNonNull(service);
        return pairs.stream()
                    .filter(sshPair -> sshPair.getOwner().equals(owner)
                                       && sshPair.getService().equals(service))
                    .map(SshPairImpl::new)
                    .collect(Collectors.toList());
    }

    @PostConstruct
    @VisibleForTesting
    synchronized void loadSshPairs() {
        pairs.addAll(sshStorage.loadList(new TypeToken<List<SshPairImpl>>() {}));
    }

    synchronized void saveSshPairs() throws IOException {
        sshStorage.store(pairs);
    }

    private Optional<SshPairImpl> find(String owner, String service, String name) {
        return pairs.stream()
                    .filter(sshPair -> sshPair.getOwner().equals(owner)
                                       && sshPair.getService().equals(service)
                                       && sshPair.getName().equals(name))
                    .findAny();
    }
}
