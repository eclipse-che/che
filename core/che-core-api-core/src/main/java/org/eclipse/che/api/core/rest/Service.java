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
package org.eclipse.che.api.core.rest;

import com.google.common.collect.ListMultimap;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.OPTIONS;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.annotations.Valid;
import org.eclipse.che.api.core.rest.shared.ParameterType;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor;
import org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
import org.eclipse.che.api.core.util.PagingUtil;
import org.eclipse.che.dto.server.DtoFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

/**
 * Base class for all API services.
 *
 * @author andrew00x
 */
public abstract class Service {
    @Context
    protected UriInfo uriInfo;

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceDescriptor getServiceDescriptor() {
        return generateServiceDescriptor(uriInfo, getClass());
    }

    public ServiceContext getServiceContext() {
        return new ServiceContextImpl(uriInfo.getBaseUriBuilder(), getClass());
    }

    protected ServiceDescriptor createServiceDescriptor() {
        return DtoFactory.getInstance().createDto(ServiceDescriptor.class);
    }

    /**
     * Generates link header value based on given {@code page}
     * and uri returned by {@code uriInfo.getRequestUri()}.
     *
     * @param page
     *         page to create link header
     * @return link header value
     */
    protected String createLinkHeader(Page<?> page) {
        return PagingUtil.createLinkHeader(page, uriInfo.getRequestUri());
    }

    /**
     * Creates uri from the given parameters and
     * delegates execution to the {@link PagingUtil#createLinkHeader(Page, URI)} method.
     *
     * @param page
     *         page to create link header
     * @param method
     *         rest service method name like {@link UriBuilder#path(Class, String) path} argument
     * @param queryParams
     *         query parameters map, if multiple query params needed then
     *         {@link #createLinkHeader(Page, String, ListMultimap, Object...)}
     *         method should be used instead
     * @param pathParams
     *         path param values like {f@link UriBuilder#build(Object...)} method arguments
     */
    protected String createLinkHeader(Page<?> page,
                                      String method,
                                      Map<String, Object> queryParams,
                                      Object... pathParams) {
        final UriBuilder ub = getServiceContext().getServiceUriBuilder().path(getClass(), method);
        for (Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
            ub.queryParam(queryParam.getKey(), queryParam.getValue());
        }
        return PagingUtil.createLinkHeader(page, ub.build(pathParams));
    }

    /**
     * This method is the same to the {@link #createLinkHeader(Page, String, Map, Object...)}
     * except of receiving query parameters.
     */
    protected String createLinkHeader(Page<?> page, String method, Object... pathParams) {
        return createLinkHeader(page, method, emptyMap(), pathParams);
    }

    /**
     * This method is the same to {@link #createLinkHeader(Page, String, Map, Object...)}
     * except of receiving multivalued query parameters.
     */
    protected String createLinkHeader(Page<?> page,
                                      String method,
                                      ListMultimap<String, Object> queryParams,
                                      Object... pathParams) {
        final UriBuilder ub = getServiceContext().getServiceUriBuilder().path(getClass(), method);
        for (Map.Entry<String, Object> queryParam : queryParams.entries()) {
            ub.queryParam(queryParam.getKey(), queryParam.getValue());
        }
        return PagingUtil.createLinkHeader(page, ub.build(pathParams));
    }

    private static final Set<String> JAX_RS_ANNOTATIONS;

    static {
        List<String> tmp = new ArrayList<>(8);
        tmp.add(CookieParam.class.getName());
        tmp.add(Context.class.getName());
        tmp.add(HeaderParam.class.getName());
        tmp.add(MatrixParam.class.getName());
        tmp.add(PathParam.class.getName());
        tmp.add(QueryParam.class.getName());
        tmp.add(FormParam.class.getName());
        tmp.add("org.everrest.core.Property");
        JAX_RS_ANNOTATIONS = new HashSet<>(tmp);
    }

    private ServiceDescriptor generateServiceDescriptor(UriInfo uriInfo, Class<? extends Service> service) {
        final List<Link> links = new ArrayList<>();
        for (Method method : service.getMethods()) {
            final GenerateLink generateLink = method.getAnnotation(GenerateLink.class);
            if (generateLink != null) {
                try {
                    links.add(generateLinkForMethod(uriInfo, generateLink.rel(), method));
                } catch (RuntimeException ignored) {
                }
            }
        }
        final Description description = service.getAnnotation(Description.class);
        final ServiceDescriptor dto = createServiceDescriptor()
                .withHref(uriInfo.getRequestUriBuilder().replaceQuery(null).build().toString())
                .withLinks(links)
                .withVersion(Constants.API_VERSION);
        if (description != null) {
            dto.setDescription(description.value());
        }
        return dto;
    }

    private Link generateLinkForMethod(UriInfo uriInfo, String linkRel, Method method, Object... pathParameters) {
        String httpMethod = null;
        final HttpMethod httpMethodAnnotation = getMetaAnnotation(method, HttpMethod.class);
        if (httpMethodAnnotation != null) {
            httpMethod = httpMethodAnnotation.value();
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException(
                    format("Method '%s' has not any HTTP method annotation and may not be used to produce link.", method.getName()));
        }

        final Consumes consumes = getAnnotation(method, Consumes.class);
        final Produces produces = getAnnotation(method, Produces.class);

        final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        final LinkedList<String> matchedURIs = new LinkedList<>(uriInfo.getMatchedURIs());
        // Get path to the root resource.
        if (uriInfo.getMatchedResources().size() < matchedURIs.size()) {
            matchedURIs.remove();
        }

        while (!matchedURIs.isEmpty()) {
            baseUriBuilder.path(matchedURIs.pollLast());
        }

        final Path path = method.getAnnotation(Path.class);
        if (path != null) {
            baseUriBuilder.path(path.value());
        }

        final Link link = DtoFactory.getInstance().createDto(Link.class)
                                    .withRel(linkRel)
                                    .withHref(baseUriBuilder.build(pathParameters).toString())
                                    .withMethod(httpMethod);
        if (consumes != null) {
            link.setConsumes(consumes.value()[0]);
        }
        if (produces != null) {
            link.setProduces(produces.value()[0]);
        }

        Class<?>[] parameterClasses = method.getParameterTypes();
        if (parameterClasses.length > 0) {
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterClasses.length; i++) {
                if (annotations[i].length > 0) {
                    boolean isBodyParameter = false;
                    QueryParam queryParam = null;
                    Description description = null;
                    Required required = null;
                    Valid valid = null;
                    DefaultValue defaultValue = null;
                    for (int j = 0; j < annotations[i].length; j++) {
                        Annotation annotation = annotations[i][j];
                        isBodyParameter |= !JAX_RS_ANNOTATIONS.contains(annotation.annotationType().getName());
                        Class<?> annotationType = annotation.annotationType();
                        if (annotationType == QueryParam.class) {
                            queryParam = (QueryParam)annotation;
                        } else if (annotationType == Description.class) {
                            description = (Description)annotation;
                        } else if (annotationType == Required.class) {
                            required = (Required)annotation;
                        } else if (annotationType == Valid.class) {
                            valid = (Valid)annotation;
                        } else if (annotationType == DefaultValue.class) {
                            defaultValue = (DefaultValue)annotation;
                        }
                    }
                    if (queryParam != null) {
                        LinkParameter parameter = DtoFactory.getInstance().createDto(LinkParameter.class)
                                                            .withName(queryParam.value())
                                                            .withRequired(required != null)
                                                            .withType(getParameterType(parameterClasses[i]));
                        if (defaultValue != null) {
                            parameter.setDefaultValue(defaultValue.value());
                        }
                        if (description != null) {
                            parameter.setDescription(description.value());
                        }
                        if (valid != null) {
                            parameter.setValid(Arrays.asList(valid.value()));
                        }
                        link.getParameters().add(parameter);
                    } else if (isBodyParameter) {
                        if (description != null) {
                            link.setRequestBody(DtoFactory.getInstance().createDto(RequestBodyDescriptor.class)
                                                          .withDescription(description.value()));
                        }
                    }
                }
            }
        }
        return link;
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        T annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            for (Class<?> c = method.getDeclaringClass().getSuperclass();
                 annotation == null && c != null && c != Object.class;
                 c = c.getSuperclass()) {
                Method inherited = null;
                try {
                    inherited = c.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (inherited != null) {
                    annotation = inherited.getAnnotation(annotationClass);
                }
            }
        }
        return annotation;
    }

    private <T extends Annotation> T getMetaAnnotation(Method method, Class<T> metaAnnotationClass) {
        T annotation = null;
        for (Annotation a : method.getAnnotations()) {
            annotation = a.annotationType().getAnnotation(metaAnnotationClass);
            if (annotation != null) {
                break;
            }
        }
        if (annotation == null) {
            for (Class<?> c = method.getDeclaringClass().getSuperclass();
                 annotation == null && c != null && c != Object.class;
                 c = c.getSuperclass()) {
                Method inherited = null;
                try {
                    inherited = c.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (inherited != null) {
                    for (Annotation a : inherited.getAnnotations()) {
                        annotation = a.annotationType().getAnnotation(metaAnnotationClass);
                        if (annotation != null) {
                            break;
                        }
                    }
                }
            }
        }
        return annotation;
    }

    private ParameterType getParameterType(Class<?> clazz) {
        if (clazz == String.class) {
            return ParameterType.String;
        }
        // restriction for collections which allowed for QueryParam annotation
        if (clazz == List.class || clazz == Set.class || clazz == SortedSet.class) {
            return ParameterType.Array;
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return ParameterType.Boolean;
        }
        if (clazz == short.class || clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class ||
            clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Float.class || clazz == Double.class) {
            return ParameterType.Number;
        }
        return ParameterType.Object;
    }

    private static class ServiceContextImpl implements ServiceContext {
        final UriBuilder uriBuilder;
        final Class      serviceClass;

        ServiceContextImpl(UriBuilder uriBuilder, Class serviceClass) {
            this.uriBuilder = uriBuilder;
            this.serviceClass = serviceClass;
        }

        @Override
        public UriBuilder getServiceUriBuilder() {
            return uriBuilder.clone().path(serviceClass);
        }

        @Override
        public UriBuilder getBaseUriBuilder() {
            return uriBuilder.clone();
        }
    }
}
