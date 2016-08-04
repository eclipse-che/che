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
package org.eclipse.che.api.factory.server.builder;

import com.google.common.base.CaseFormat;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.factory.server.FactoryConstants;
import org.eclipse.che.api.factory.server.LegacyConverter;
import org.eclipse.che.api.factory.server.ValueHelper;
import org.eclipse.che.api.factory.server.impl.SourceStorageParametersValidator;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation;
import static org.eclipse.che.api.core.factory.FactoryParameter.Version;

/**
 * Tool to easy convert Factory object to json and vise versa.
 * Also it provides factory parameters compatibility.
 *
 * @author Sergii Kabashniuk
 * @author Alexander Garagatyi
 */
@Singleton
public class FactoryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryBuilder.class);

    /** List contains all possible implementation of factory legacy converters. */
    static final List<LegacyConverter> LEGACY_CONVERTERS;

    static {
        List<LegacyConverter> l = new ArrayList<>(1);
        l.add(factory -> {});
        LEGACY_CONVERTERS = Collections.unmodifiableList(l);
    }

    private final SourceStorageParametersValidator sourceStorageParametersValidator;

    @Inject
    public FactoryBuilder(SourceStorageParametersValidator sourceStorageParametersValidator) {
        this.sourceStorageParametersValidator = sourceStorageParametersValidator;
    }

    /**
     * Build factory from json and validate its compatibility.
     *
     * @param json
     *         - json Reader from encoded factory.
     * @return - Factory object represented by given factory json.
     */
    public FactoryDto build(Reader json) throws IOException, ApiException {
        FactoryDto factory = DtoFactory.getInstance()
                                    .createDtoFromJson(json, FactoryDto.class);
        checkValid(factory);
        return factory;
    }

    /**
     * Build factory from json and validate its compatibility.
     *
     * @param json
     *         - json string from encoded factory.
     * @return - Factory object represented by given factory json.
     */
    public FactoryDto build(String json) throws ApiException {
        FactoryDto factory = DtoFactory.getInstance()
                                    .createDtoFromJson(json, FactoryDto.class);
        checkValid(factory);
        return factory;
    }

    /**
     * Build factory from json and validate its compatibility.
     *
     * @param json
     *         - json  InputStream from encoded factory.
     * @return - Factory object represented by given factory json.
     */
    public FactoryDto build(InputStream json) throws IOException, ConflictException {
        FactoryDto factory = DtoFactory.getInstance()
                                    .createDtoFromJson(json, FactoryDto.class);
        checkValid(factory);
        return factory;
    }

    /**
     * Validate factory compatibility at creation time.
     *
     * @param factory
     *         - factory object to validate
     * @throws ConflictException
     */
    public void checkValid(FactoryDto factory) throws ConflictException {
       checkValid(factory,  false);
    }

    /**
     * Validate factory compatibility.
     *
     * @param factory
     *         - factory object to validate
     * @param isUpdate
     *         - indicates is validation performed on update time.
     *           Set-by-server variables are allowed during update.
     * @throws ConflictException
     */
    public void checkValid(FactoryDto factory, boolean isUpdate) throws ConflictException {
        if (null == factory) {
            throw new ConflictException(FactoryConstants.UNPARSABLE_FACTORY_MESSAGE);
        }
        if (factory.getV() == null) {
            throw new ConflictException(FactoryConstants.INVALID_VERSION_MESSAGE);
        }

        Version v;
        try {
            v = Version.fromString(factory.getV());
        } catch (IllegalArgumentException e) {
            throw new ConflictException(FactoryConstants.INVALID_VERSION_MESSAGE);
        }

        Class usedFactoryVersionMethodProvider;
        switch (v) {
            case V4_0:
                usedFactoryVersionMethodProvider = FactoryDto.class;
                break;
            default:
                throw new ConflictException(FactoryConstants.INVALID_VERSION_MESSAGE);
        }
        validateCompatibility(factory, null, FactoryDto.class, usedFactoryVersionMethodProvider, v, "", isUpdate);
    }

    /**
     * Convert factory of given version to the latest factory format.
     *
     * @param factory
     *         - given factory.
     * @return - factory in latest format.
     * @throws org.eclipse.che.api.core.ApiException
     */
    public FactoryDto convertToLatest(FactoryDto factory) throws ApiException {
        FactoryDto resultFactory = DtoFactory.getInstance().clone(factory).withV("4.0");
        for (LegacyConverter converter : LEGACY_CONVERTERS) {
            converter.convert(resultFactory);
        }
        return resultFactory;
    }


    /**
     * Validate compatibility of factory parameters.
     *
     * @param object
     *         - object to validate factory parameters
     * @param parent
     *         - parent object
     * @param methodsProvider
     *         - class that provides methods with {@link org.eclipse.che.api.core.factory.FactoryParameter}
     *         annotations
     * @param allowedMethodsProvider
     *         - class that provides allowed methods
     * @param version
     *         - version of factory
     * @param parentName
     *         - parent parameter queryParameterName
     * @throws org.eclipse.che.api.core.ConflictException
     */
    void validateCompatibility(Object object,
                               Object parent,
                               Class methodsProvider,
                               Class allowedMethodsProvider,
                               Version version,
                               String parentName,
                               boolean isUpdate) throws ConflictException {
        // validate source
        if (SourceStorageDto.class.equals(methodsProvider) && !hasSubprojectInPath(parent)) {
            sourceStorageParametersValidator.validate((SourceStorage)object, version);
        }

        // get all methods recursively
        for (Method method : methodsProvider.getMethods()) {
            FactoryParameter factoryParameter = method.getAnnotation(FactoryParameter.class);
            // is it factory parameter

            if (factoryParameter == null) {
                continue;
            }
            String fullName = (parentName.isEmpty() ? "" : (parentName + ".")) + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                                                                                                           method.getName().startsWith("is")
                                                                                                           ? method.getName()
                                                                                                                   .substring(2)
                                                                                                           : method.getName()
                                                                                                                   .substring(3)
                                                                                                                   .toLowerCase());
            // check that field is set
            Object parameterValue;
            try {
                parameterValue = method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                throw new ConflictException(FactoryConstants.INVALID_PARAMETER_MESSAGE);
            }



            // if value is null or empty collection or default value for primitives
            if (ValueHelper.isEmpty(parameterValue)) {
                // field must not be a mandatory, unless it's ignored or deprecated or doesn't suit to the version
                if (Obligation.MANDATORY.equals(factoryParameter.obligation()) &&
                    factoryParameter.deprecatedSince()
                                    .compareTo(version) > 0 &&
                    factoryParameter.ignoredSince()
                                    .compareTo(version) > 0 &&
                    method.getDeclaringClass()
                          .isAssignableFrom(allowedMethodsProvider)) {
                    throw new ConflictException(String.format(FactoryConstants.MISSING_MANDATORY_MESSAGE, method.getName()));
                }
            } else if (!method.getDeclaringClass()
                              .isAssignableFrom(allowedMethodsProvider)) {
                throw new ConflictException(String.format(FactoryConstants.PARAMETRIZED_INVALID_PARAMETER_MESSAGE, fullName, version));
            } else {
                // is parameter deprecated
                if (factoryParameter.deprecatedSince().compareTo(version) <= 0 || (!isUpdate && factoryParameter.setByServer())) {
                    throw new ConflictException(
                            String.format(FactoryConstants.PARAMETRIZED_INVALID_PARAMETER_MESSAGE, fullName, version));
                }

                // use recursion if parameter is DTO object
                if (method.getReturnType().isAnnotationPresent(DTO.class)) {
                    // validate inner objects such Git ot ProjectAttributes
                    validateCompatibility(parameterValue, object, method.getReturnType(), method.getReturnType(), version, fullName, isUpdate);
                } else if (Map.class.isAssignableFrom(method.getReturnType())) {
                    Type tp = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[1];

                    Class secMapParamClass = (tp instanceof ParameterizedType) ? (Class)((ParameterizedType)tp).getRawType() : (Class)tp;
                    if (!String.class.equals(secMapParamClass) && !List.class.equals(secMapParamClass)) {
                        if (secMapParamClass.isAnnotationPresent(DTO.class)) {
                            Map<Object, Object> map = (Map)parameterValue;
                            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                                validateCompatibility(entry.getValue(), object, secMapParamClass, secMapParamClass, version,
                                                      fullName + "." + entry.getKey(), isUpdate);
                            }
                        } else {
                            throw new RuntimeException("This type of fields is not supported by factory.");
                        }
                    }
                } else if (List.class.isAssignableFrom(method.getReturnType())) {
                    Type tp = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];

                    Class secListParamClass = (tp instanceof ParameterizedType) ? (Class)((ParameterizedType)tp).getRawType() : (Class)tp;
                    if (!String.class.equals(secListParamClass) && !List.class.equals(secListParamClass)) {
                        if (secListParamClass.isAnnotationPresent(DTO.class)) {
                            List<Object> list = (List)parameterValue;
                            for (Object entry : list) {
                                validateCompatibility(entry, object, secListParamClass, secListParamClass, version, fullName, isUpdate);
                            }
                        } else {
                            throw new RuntimeException("This type of fields is not supported by factory.");
                        }
                    }
                }
            }
        }
    }

    private boolean hasSubprojectInPath(Object parent) {
        return parent != null
            && ProjectConfig.class.isAssignableFrom(parent.getClass())
            && ((ProjectConfig)parent).getPath().indexOf('/', 1) != -1;
    }
}
