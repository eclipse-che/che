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
package org.eclipse.che.plugin.typescript.dto.model;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.typescript.dto.DTOHelper.convertType;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.getGetterFieldName;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.getSetterFieldName;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.getWithFieldName;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.isDtoGetter;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.isDtoSetter;
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.isDtoWith;

/***
 * Model of the DTO
 * It includes attributes/fields and methods.
 *
 * @author Florent Benoit
 */
public class DtoModel {

    /**
     * DTO instance (interface).
     */
    private Class dto;

    /**
     * Model of methods for this interface.
     */
    private List<MethodModel> methods;

    /**
     * Map of all attributes found when scanning methods
     */
    private Map<String, Type> fieldAttributes = new HashMap<>();

    /**
     * Model for the attributes of this interface (for generating implementation)
     */
    private List<FieldAttributeModel> fieldAttributeModels;

    /**
     * Build a new model for the given DTO class by scanning it.
     *
     * @param dto
     *         the interface with {@link org.eclipse.che.dto.shared.DTO} annotation
     */
    public DtoModel(Class dto) {
        this.dto = dto;
        this.methods = new ArrayList<>();
        this.fieldAttributeModels = new ArrayList<>();
        analyze();
    }

    /**
     * Scan all getter/setter/with methods that are not inherited
     */
    protected void analyze() {
        Arrays.asList(this.dto.getMethods()).stream()
              .filter(method -> !method.isBridge() && (isDtoGetter(method) || isDtoSetter(method) || isDtoWith(method)))
              .forEach(method -> {
                  MethodModel methodModel = new MethodModel(method);

                  // check method with same name already exist
                  if (!methods.contains(methodModel)) {
                      methods.add(methodModel);
                      if (isDtoGetter(method)) {
                          analyzeDtoGetterMethod(method, methodModel);
                      } else if (isDtoSetter(method)) {
                          analyzeDtoSetterMethod(method, methodModel);
                      } else if (isDtoWith(method)) {
                          analyzeDtoWithMethod(method, methodModel);
                      }
                  }
              });

        // now convert map into list
        fieldAttributes.entrySet().stream().forEach(field -> fieldAttributeModels.add(new FieldAttributeModel(field.getKey(), field.getValue())));

    }

    /**
     * Populate model from given reflect getter method
     * @param method the method to analyze
     * @param methodModel the model to update
     */
    protected void analyzeDtoGetterMethod(Method method, MethodModel methodModel) {
        methodModel.setGetter(true);
        Type fieldType = method.getGenericReturnType();
        String fieldName = getGetterFieldName(method);
        fieldAttributes.put(fieldName, fieldType);
        methodModel.setFieldName(fieldName);
        methodModel.setFieldType(convertType(fieldType));
    }

    /**
     * Populate model from given reflect setter method
     * @param method the method to analyze
     * @param methodModel the model to update
     */
    protected void analyzeDtoSetterMethod(Method method, MethodModel methodModel) {
        methodModel.setSetter(true);
        // add the parameter
        Type fieldType = method.getGenericParameterTypes()[0];
        String fieldName = getSetterFieldName(method);
        fieldAttributes.put(fieldName, fieldType);
        methodModel.setFieldName(fieldName);
        methodModel.setFieldType(convertType(fieldType));

    }

    /**
     * Populate model from given reflect with method
     * @param method the method to analyze
     * @param methodModel the model to update
     */
    protected void analyzeDtoWithMethod(Method method, MethodModel methodModel) {
        methodModel.setWith(true);
        // add the parameter
        Type fieldType = method.getGenericParameterTypes()[0];
        String fieldName = getWithFieldName(method);
        fieldAttributes.put(fieldName, fieldType);
        methodModel.setFieldName(fieldName);
        methodModel.setFieldType(convertType(fieldType));
    }

        /**
         * @return model of attributes
         */
    public List<FieldAttributeModel> getFieldAttributeModels() {
        return fieldAttributeModels;
    }

    /**
     * Gets the package name of this interface
     *
     * @return the package name of this interface
     */
    public String getPackageName() {
        return this.dto.getPackage().getName();
    }

    /**
     * Gets the short (simple) name of the interface. Like HelloWorld if FQN class is foo.bar.HelloWorld
     *
     * @return the name of the interface
     */
    public String getSimpleName() {
        return this.dto.getSimpleName();
    }

    /**
     * Gets the FQN of this interface like foo.bar.HelloWorld
     *
     * @return the FQN name of this DTO interface
     */
    public String getName() {
        return this.dto.getName();
    }

    /**
     * Provides the model for every methods of the DTO that are getter/setter/with methods
     *
     * @return the list
     */
    public List<MethodModel> getMethods() {
        return this.methods;
    }

}
