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
 * Defines the contract for the factory restrictions.
 *
 * @author Anton Korneta
 */
public interface Policies {

    /**
     * Restrict access if referer header doesn't match this field
     */
    String getReferer();

    /**
     * Restrict access for factories used earlier then author supposes
     */
    Long getSince();

    /**
     * Restrict access for factories used later then author supposes
     */
    Long getUntil();

    /**
     * Re-open projects on factory 2-nd click
     */
    String getMatch();

    /**
     * Workspace creation strategy
     */
    String getCreate();
}
