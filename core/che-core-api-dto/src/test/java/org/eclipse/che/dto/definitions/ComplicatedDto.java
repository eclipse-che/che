/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.definitions;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for testing that the {@link org.eclipse.che.dto.generator.DtoGenerator} correctly generates
 * server implementations for object graphs (nested lists, and maps).
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface ComplicatedDto {

  public enum SimpleEnum {
    ONE,
    TWO,
    THREE
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
