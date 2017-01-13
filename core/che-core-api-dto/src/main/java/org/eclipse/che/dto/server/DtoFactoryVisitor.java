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
package org.eclipse.che.dto.server;

/**
 * Visitor pattern. Generally needed to register DtoProviders by generated code in DtoFactory. Class which contains generated code for
 * server side implements this interface. When DtoFactory class is loaded it looks up for all implementation of this interface and calls
 * method {@link #accept(DtoFactory)}.
 *
 * @author andrew00x
 */
public interface DtoFactoryVisitor {
    void accept(DtoFactory dtoFactory);
}
