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
package org.eclipse.che.core.db.cascade.event;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;

/**
 * Cascade event about an entity persisting.
 *
 * <p>{@link ConflictException} or {@link ServerException} can be rethrown during exception
 * propagating.
 *
 * @author Sergii Leschenko
 */
public abstract class PersistEvent extends CascadeEvent {
  @Override
  public void propagateException() throws ConflictException, ServerException {
    if (context.isFailed()) {
      try {
        throw context.getCause();
      } catch (ConflictException | ServerException e) {
        throw e;
      } catch (Exception e) {
        throw new ServerException(e.getLocalizedMessage(), e);
      }
    }
  }
}
