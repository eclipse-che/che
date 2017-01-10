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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * DTO for testing that the {@link org.eclipse.che.dto.generator.DtoGenerator}
 * correctly generates server implementations for object graphs (nested lists, and maps).
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface ComplicatedDto {

    public enum SimpleEnum {
        ONE, TWO, THREE
    }

    List<String> getStrings();

    ComplicatedDto withStrings(List<String> strings);

    SimpleEnum getSimpleEnum();

    ComplicatedDto withSimpleEnum(SimpleEnum simpleEnum);

    Map<String, SimpleDto> getMap();

    ComplicatedDto withMap(Map<String, SimpleDto> map);

    List<SimpleDto> getSimpleDtos();

    ComplicatedDto withSimpleDtos(List<SimpleDto> listDtos);

    List<List<SimpleEnum>> getArrayOfArrayOfEnum();

    ComplicatedDto withArrayOfArrayOfEnum(List<List<SimpleEnum>> arrayOfArrayOfEnum);
}
