/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;

/**
 * Defines data access object contract for {@link WorkerImpl}.
 *
 * @author Sergii Leschenko
 */
public interface WorkerDao {

  /**
   * Stores (adds or updates) worker.
   *
   * @param worker worker to store
   * @return optional with updated worker, other way empty optional must be returned
   * @throws NullPointerException when {@code worker} is null
   * @throws ServerException when any other error occurs during worker storing
   */
  Optional<WorkerImpl> store(WorkerImpl worker) throws ServerException;

  /**
   * Gets worker by user and workspace
   *
   * @param workspaceId workspace identifier
   * @param userId user identifier
   * @return worker instance, never null
   * @throws NullPointerException when {@code workspace} or {@code user} is null
   * @throws NotFoundException when worker with given {@code workspace} and {@code user} was not
   *     found
   * @throws ServerException when any other error occurs during worker fetching
   */
  WorkerImpl getWorker(String workspaceId, String userId) throws ServerException, NotFoundException;

  /**
   * Removes worker
   *
   * <p>Doesn't throw an exception when worker with given {@code workspace} and {@code user} does
   * not exist
   *
   * @param workspaceId workspace identifier
   * @param userId user identifier
   * @throws NullPointerException when {@code workspace} or {@code user} is null
   * @throws ServerException when any other error occurs during worker removing
   */
  void removeWorker(String workspaceId, String userId) throws ServerException;

  /**
   * Gets workers by workspace
   *
   * @param workspaceId workspace identifier
   * @param maxItems the maximum number of workers to return
   * @param skipCount the number of workers to skip
   * @return list of workers instance
   * @throws NullPointerException when {@code workspace} is null
   * @throws ServerException when any other error occurs during worker fetching
   */
  Page<WorkerImpl> getWorkers(String workspaceId, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Gets workers by user
   *
   * @param userId workspace identifier
   * @return list of workers instance
   * @throws NullPointerException when {@code user} is null
   * @throws ServerException when any other error occurs during worker fetching
   */
  List<WorkerImpl> getWorkersByUser(String userId) throws ServerException;
}
