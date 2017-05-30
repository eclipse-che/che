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

import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/** @author andrew00x */
@DTO
public interface VariableDto extends Variable {
    String getName();

    void setName(String name);

    VariableDto withName(String name);

    boolean isExistInformation();

    void setExistInformation(boolean existInformation);

    VariableDto withExistInformation(boolean existInformation);

    String getValue();

    void setValue(String value);

    VariableDto withValue(String value);

    String getType();

    void setType(String type);

    VariableDto withType(String type);

    VariablePathDto getVariablePath();

    void setVariablePath(VariablePathDto variablePath);

    VariableDto withVariablePath(VariablePathDto variablePath);

    boolean isPrimitive();

    void setPrimitive(boolean primitive);

    VariableDto withPrimitive(boolean primitive);

    List<VariableDto> getVariables();

    void setVariables(List<VariableDto> variables);

    VariableDto withVariables(List<VariableDto> variables);
}
