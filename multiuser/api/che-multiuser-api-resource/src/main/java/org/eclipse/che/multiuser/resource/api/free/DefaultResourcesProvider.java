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
package org.eclipse.che.multiuser.resource.api.free;

import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Provides default resources which should be are available for usage by account when admin doesn't
 * override limit by {@link FreeResourcesLimitService}.
 *
 * @author Sergii Leschenko
 */
public interface DefaultResourcesProvider {
  /** Provides default resources are available for usage by account */
  List<ResourceImpl> getResources(String accountId) throws ServerException, NotFoundException;

  /** Returns account type for which this class provides default resources. */
  String getAccountType();
}
