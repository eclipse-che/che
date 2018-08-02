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
package org.eclipse.che.multiuser.resource.api;

import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Tracks usage of resources of specified type.
 *
 * @author Sergii Leschenko
 */
public interface ResourceUsageTracker {
  /**
   * Returns used resource by given account.
   *
   * @param accountId account id to fetch used resource
   * @return used resource by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurs on used resources fetching
   */
  Optional<Resource> getUsedResource(String accountId) throws NotFoundException, ServerException;
}
