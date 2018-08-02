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
package org.eclipse.che.plugin.typescript.dto.model;

import static org.eclipse.che.plugin.typescript.dto.DTOHelper.convertType;

import com.google.gson.internal.Primitives;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * A field model will used for providing class generation of a DTO
 *
 * @author Florent Benoit
 */
public class FieldAttributeModel {

  /** Typescript value of the type of the field */
  private final String typeName;

  /** For Map, List object, need to initialize field first. Like new Field<>() */
  private boolean needInitialize;

  /** Name of the field */
  private String fieldName;

  /** Java Type of the object (used internally) */
  private Type type;

  /** This field type is a List of objects ? */
  private boolean isList;

  /** This field type is a simple primitive */
  private boolean isPrimitive;

  /** This field type is a map */
  private boolean isMap;

  /** This list type is in fact a list of DTOs */
  private boolean isListOfDto;

  /** This map type is a map of DTOs */
  private boolean isMapOfDto;

  /**
   * The type is a DTO or a list of DTO and then this value is the name of the DTO implementation
   */
  private String dtoImpl;

  /** type is a DTO object. */
  private boolean isDto;

  /** type is a Enum object. */
  private boolean isEnum;

  /**
   * Build a new field model based on the name and Java type
   *
   * @param fieldName the name of the field
   * @param type the Java raw type that will allow further analyzes
   */
  public FieldAttributeModel(String fieldName, Type type) {
    this.fieldName = fieldName;
    this.type = type;
    this.typeName = convertType(type);

    if (typeName.startsWith("Array<") || typeName.startsWith("Map<")) {
      this.needInitialize = true;
    }

    if (this.type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) this.type;
      Type rawType = parameterizedType.getRawType();
      analyzeParametrizedType(parameterizedType, rawType);
    } else if (Primitives.isPrimitive(this.type)
        || Primitives.isWrapperType(this.type)
        || String.class.equals(this.type)) {
      this.isPrimitive = true;
    } else if (this.type instanceof Class && ((Class) this.type).isAnnotationPresent(DTO.class)) {
      this.isDto = true;
      dtoImpl = this.type.getTypeName() + "Impl";
    } else if (this.type instanceof Class && ((Class) this.type).isEnum()) {
      this.isEnum = true;
    }
  }

  /**
   * Analyze a complex parametrized type attribute (which can be a list or map for example)
   *
   * @param parameterizedType
   * @param rawType
   */
  protected void analyzeParametrizedType(ParameterizedType parameterizedType, Type rawType) {
    if (List.class.equals(rawType)) {
      this.isList = true;
      if (parameterizedType.getActualTypeArguments()[0] instanceof Class
          && ((Class) parameterizedType.getActualTypeArguments()[0])
              .isAnnotationPresent(DTO.class)) {
        isListOfDto = true;
        dtoImpl = convertType(parameterizedType.getActualTypeArguments()[0]) + "Impl";
      }
    } else if (Map.class.equals(rawType)) {
      isMap = true;
      if (parameterizedType.getActualTypeArguments()[1] instanceof Class
          && ((Class) parameterizedType.getActualTypeArguments()[1])
              .isAnnotationPresent(DTO.class)) {
        isMapOfDto = true;
        dtoImpl = convertType(parameterizedType.getActualTypeArguments()[1]) + "Impl";
      }
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public Type getType() {
    return type;
  }

  public boolean isList() {
    return isList;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }

  public boolean isMap() {
    return isMap;
  }

  public boolean isListOfDto() {
    return isListOfDto;
  }

  public boolean isMapOfDto() {
    return isMapOfDto;
  }

  public String getDtoImpl() {
    return dtoImpl;
  }

  public boolean isDto() {
    return isDto;
  }

  public boolean isNeedInitialize() {
    return needInitialize;
  }

  public boolean isEnum() {
    return isEnum;
  }

  public String getName() {
    return this.fieldName;
  }

  public String getSimpleType() {
    return this.typeName;
  }
}
