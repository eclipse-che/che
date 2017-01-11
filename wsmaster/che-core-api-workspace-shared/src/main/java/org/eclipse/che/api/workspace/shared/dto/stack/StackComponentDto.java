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
package org.eclipse.che.api.workspace.shared.dto.stack;


import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Alexander Andrienko
 */
@DTO
public interface StackComponentDto extends StackComponent {

    void setName(String name);

    StackComponentDto withName(String name);

    void setVersion(String version);

    StackComponentDto withVersion(String version);
}
