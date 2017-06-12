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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface RuntimeId {

    String getWorkspace();

    void setWorkspace(String workspace);

    RuntimeId withWorkspace(String workspace);


    String getEnvironment();

    void setEnvironment(String environment);

    RuntimeId withEnvironment(String environment);


    String getOwner();

    void setOwner(String owner);

    RuntimeId withOwner(String owner);
}
