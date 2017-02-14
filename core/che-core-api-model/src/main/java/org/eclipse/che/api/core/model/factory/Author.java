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
package org.eclipse.che.api.core.model.factory;

/**
 * Defines the contract for the factory creator instance.
 *
 * @author Anton Korneta
 */
public interface Author {

    /**
     * Identifier of the user who created factory, it is mandatory
     */
    String getUserId();

    /**
     * Creation time of factory, set by the server (in milliseconds, from Unix epoch, no timezone)
     */
    Long getCreated();
}
