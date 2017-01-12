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
package org.eclipse.che.core.db.cascade.event;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Cascade event about an entity updating.
 *
 * <p>{@link NotFoundException}, {@link ConflictException} or {@link ServerException}
 * can be rethrown during exception propagating.
 *
 * @author Sergii Leschenko
 */
public abstract class UpdateEvent extends CascadeEvent {
    @Override
    public void propagateException() throws NotFoundException, ConflictException, ServerException {
        if (context.isFailed()) {
            try {
                throw context.getCause();
            } catch (NotFoundException | ConflictException | ServerException e) {
                throw e;
            } catch (Exception e) {
                throw new ServerException(e.getLocalizedMessage(), e);
            }
        }
    }
}
