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
package org.eclipse.che.commons.test.tck.repository;

import java.util.Collection;

/**
 * The interface which allows to create TCK tests for DAO interfaces by providing operations for
 * creating/removing batch of elements. The interface is designed to work with entities, which means
 * that entity marshaling and unmarshalling to/from db objects must be tested by implementation
 * separately.
 *
 * @param <T> the type of the object managed by the repository
 * @author Yevhenii Voevodin
 */
public interface TckRepository<T> {

  /**
   * Creates all the given {@code entities} in the storage.
   *
   * <p>Note that implementation must fail if it is impossible to create any of the given entities.
   *
   * @param entities elements to create
   * @throws TckRepositoryException when any error occurs during the storing
   */
  void createAll(Collection<? extends T> entities) throws TckRepositoryException;

  /**
   * Clears the storage.
   *
   * <p>Note that implementation must fail if it is impossible to remove all the entities.
   *
   * @throws TckRepositoryException when any error occurs during the clearing
   */
  void removeAll() throws TckRepositoryException;
}
