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
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Anatolii Bazko
 */
@DTO
public interface MethodDto extends Method {
    @Override
    String getName();

    void setName(String name);

    MethodDto withName(String name);

    @Override
    List<VariableDto> getArguments();

    void setArguments(List<VariableDto> arguments);

    MethodDto withArguments(List<VariableDto> arguments);
}
