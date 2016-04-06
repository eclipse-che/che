/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.debugger.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/** @author andrew00x */
@DTO
public interface Variable {
    String getName();

    void setName(String name);

    Variable withName(String name);

    boolean isExistInformation();

    void setExistInformation(boolean existInformation);

    Variable withExistInformation(boolean existInformation);

    String getValue();

    void setValue(String value);

    Variable withValue(String value);

    String getType();

    void setType(String type);

    Variable withType(String type);

    VariablePath getVariablePath();

    void setVariablePath(VariablePath variablePath);

    Variable withVariablePath(VariablePath variablePath);

    boolean isPrimitive();

    void setPrimitive(boolean primitive);

    Variable withPrimitive(boolean primitive);

    List<Variable> getVariables();

    void setVariables(List<Variable> variables);

    Variable withVariables(List<Variable> variables);
}