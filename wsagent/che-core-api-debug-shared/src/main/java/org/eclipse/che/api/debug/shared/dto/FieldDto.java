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

import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/** @author andrew00x */
@DTO
public interface FieldDto extends Field {
    boolean isIsFinal();

    void setIsFinal(boolean value);

    FieldDto withIsFinal(boolean value);

    boolean isIsStatic();

    void setIsStatic(boolean value);

    FieldDto withIsStatic(boolean value);

    boolean isIsTransient();

    void setIsTransient(boolean value);

    FieldDto withIsTransient(boolean value);

    boolean isIsVolatile();

    void setIsVolatile(boolean value);

    FieldDto withIsVolatile(boolean value);

    String getName();

    void setName(String name);

    FieldDto withName(String name);

    boolean isExistInformation();

    void setExistInformation(boolean existInformation);

    FieldDto withExistInformation(boolean existInformation);

    String getValue();

    void setValue(String value);

    FieldDto withValue(String value);

    String getType();

    void setType(String type);

    FieldDto withType(String type);

    VariablePathDto getVariablePath();

    void setVariablePath(VariablePathDto variablePath);

    FieldDto withVariablePath(VariablePathDto variablePath);

    boolean isPrimitive();

    void setPrimitive(boolean primitive);

    FieldDto withPrimitive(boolean primitive);

    List<VariableDto> getVariables();

    void setVariables(List<VariableDto> variables);

    FieldDto withVariables(List<VariableDto> variables);
}
