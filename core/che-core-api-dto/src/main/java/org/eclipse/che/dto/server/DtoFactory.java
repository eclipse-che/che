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
package org.eclipse.che.dto.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.eclipse.che.commons.lang.reflect.ParameterizedTypeImpl;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonArray;
import org.eclipse.che.dto.shared.JsonStringMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides implementations of DTO interfaces.
 *
 * @author andrew00x
 */
public final class DtoFactory {
    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private static final LoadingCache<Type, ParameterizedType> listTypeCache = CacheBuilder.newBuilder().concurrencyLevel(16).build(
            new CacheLoader<Type, ParameterizedType>() {
                @Override
                public ParameterizedType load(Type type) {
                    return new ParameterizedTypeImpl(List.class, type);
                }
            });

    private static final LoadingCache<Type, ParameterizedType> mapTypeCache = CacheBuilder.newBuilder().concurrencyLevel(16).build(
            new CacheLoader<Type, ParameterizedType>() {
                @Override
                public ParameterizedType load(Type type) {
                    return new ParameterizedTypeImpl(Map.class, String.class, type);
                }
            });

    private static final DtoFactory INSTANCE = new DtoFactory();

    public static DtoFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates new instance of class which implements specified DTO interface.
     *
     * @param dtoInterface
     *         DTO interface
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public static <T> T newDto(Class<T> dtoInterface) {
        return getInstance().createDto(dtoInterface);
    }

    private final Map<Class<?>, DtoProvider<?>> dtoInterface2Providers = new ConcurrentHashMap<>();
    // Additional mapping for implementation of DTO interfaces.
    // It helps avoid reflection when need create copy of exited DTO instance.
    private final Map<Class<?>, DtoProvider<?>> dtoImpl2Providers      = new ConcurrentHashMap<>();

    /**
     * Created deep copy of DTO object.
     *
     * @param origin
     *         origin DTO object
     * @return copy
     * @throws IllegalArgumentException
     *         if specified object doesn't implement DTO interface annotated with {@link org.eclipse.che.dto.shared.DTO &#064DTO} or if
     *         specified instance implements more than one interface annotated with {@link org.eclipse.che.dto.shared.DTO &#064DTO}
     */
    @SuppressWarnings("unchecked")
    public <T> T clone(T origin) {
        final Class<?> implClass = origin.getClass();
        DtoProvider provider = dtoImpl2Providers.get(implClass);
        if (provider == null) {
            Class<?> dtoInterface = null;
            Class<?>[] interfaces = implClass.getInterfaces();
            if (interfaces.length == 0) {
                return null;
            }

            for (Class<?> i : interfaces) {
                if (i.isAnnotationPresent(DTO.class)) {
                    if (dtoInterface != null) {
                        throw new IllegalArgumentException("Unable determine DTO interface. Type " + implClass.getName() +
                                                           " implements or extends more than one interface annotated with @DTO annotation.");
                    }
                    dtoInterface = i;
                }
            }

            if (dtoInterface != null) {
                provider = getDtoProvider(dtoInterface);
            }
        }

        if (provider == null) {
            throw new IllegalArgumentException("Unknown DTO type " + implClass);
        }

        return (T)provider.clone(origin);
    }

    /**
     * Shortcut for {@code DtoFactory.getInstance().clone(T dtoObject)}
     *
     * @see #clone(Object)
     */
    public static <T> T cloneDto(T origin) {
        return getInstance().clone(origin);
    }

    public <T> String toJson(T dto) {
        if (dto instanceof JsonSerializable) {
            return ((JsonSerializable)dto).toJson();
        }
        throw new IllegalArgumentException("JsonSerializable instance required. ");
    }

    public <T> JsonElement toJsonElement(T dto) {
        if (dto instanceof JsonSerializable) {
            return ((JsonSerializable)dto).toJsonElement();
        }
        throw new IllegalArgumentException("JsonSerializable instance required. ");
    }

    /**
     * Creates new instance of class which implements specified DTO interface.
     *
     * @param dtoInterface
     *         DTO interface
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public <T> T createDto(Class<T> dtoInterface) {
        return getDtoProvider(dtoInterface).newInstance();
    }

    //

    /**
     * Creates new instance of class which implements specified DTO interface, parses specified JSON string and uses parsed data for
     * initializing fields of DTO object.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public <T> T createDtoFromJson(String json, Class<T> dtoInterface) {
        return getDtoProvider(dtoInterface).fromJson(json);
    }

    /**
     * Creates new instance of class which implements specified DTO interface, uses the specific JSON data for
     * initializing fields of DTO object.
     *
     * @param json
     *            JSON data
     * @param dtoInterface
     *            DTO interface
     * @throws IllegalArgumentException
     *             if can't provide any implementation for specified interface
     */
    public <T> T createDtoFromJson(JsonElement json, Class<T> dtoInterface) {
        return getDtoProvider(dtoInterface).fromJson(json);
    }

    /**
     * Creates new instance of class which implements specified DTO interface, parses specified JSON data and uses parsed data for
     * initializing fields of DTO object.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     * @throws IOException
     *         if an i/o error occurs
     */
    public <T> T createDtoFromJson(Reader json, Class<T> dtoInterface) throws IOException {
        DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(json);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return dtoProvider.fromJson(sb.toString());
    }

    /**
     * Creates new instance of class which implements specified DTO interface, parses specified JSON data and uses parsed data for
     * initializing fields of DTO object.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     * @throws IOException
     *         if an i/o error occurs
     */
    public <T> T createDtoFromJson(InputStream json, Class<T> dtoInterface) throws IOException {
        return createDtoFromJson(new InputStreamReader(json), dtoInterface);
    }

    //

    /**
     * Parses the JSON data from the specified sting into list of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return list of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public <T> JsonArray<T> createListDtoFromJson(String json, Class<T> dtoInterface) {
        final DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
        final List<JsonElement> list = gson.fromJson(json, listTypeCache.getUnchecked(JsonElement.class));
        final List<T> result = new ArrayList<>(list.size());
        for (JsonElement e : list) {
            result.add(dtoProvider.fromJson(e));
        }
        return new JsonArrayImpl<>(result);
    }


    /**
     * Parses the JSON data from the specified reader into list of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return list of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public <T> JsonArray<T> createListDtoFromJson(Reader json, Class<T> dtoInterface) throws IOException {
        final DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
        final List<JsonElement> list;
        try {
            list = gson.fromJson(json, listTypeCache.getUnchecked(JsonElement.class));
        } catch (JsonSyntaxException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException)cause;
            }
            throw e;
        }
        final List<T> result = new ArrayList<>(list.size());
        for (JsonElement e : list) {
            result.add(dtoProvider.fromJson(e));
        }
        return new JsonArrayImpl<>(result);
    }

    /**
     * Parses the JSON data from the specified stream into list of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return list of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     * @throws IOException
     *         if an i/o error occurs
     */
    public <T> JsonArray<T> createListDtoFromJson(InputStream json, Class<T> dtoInterface) throws IOException {
        return createListDtoFromJson(new InputStreamReader(json), dtoInterface);
    }

    //

    /**
     * Parses the JSON data from the specified sting into map of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return map of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     */
    public <T> JsonStringMap<T> createMapDtoFromJson(String json, Class<T> dtoInterface) {
        final DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
        final Map<String, JsonElement> map = gson.fromJson(json, mapTypeCache.getUnchecked(JsonElement.class));
        final Map<String, T> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, JsonElement> e : map.entrySet()) {
            result.put(e.getKey(), dtoProvider.fromJson(e.getValue()));
        }
        return new JsonStringMapImpl<>(result);
    }


    /**
     * Parses the JSON data from the specified reader into map of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return map of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     * @throws IOException
     *         if an i/o error occurs
     */
    @SuppressWarnings("unchecked")
    public <T> JsonStringMap<T> createMapDtoFromJson(Reader json, Class<T> dtoInterface) throws IOException {
        final DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
        final Map<String, JsonElement> map;
        try {
            map = gson.fromJson(json, mapTypeCache.getUnchecked(JsonElement.class));
        } catch (JsonSyntaxException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException)cause;
            }
            throw e;
        }
        final Map<String, T> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, JsonElement> e : map.entrySet()) {
            result.put(e.getKey(), dtoProvider.fromJson(e.getValue()));
        }
        return new JsonStringMapImpl<>(result);
    }

    /**
     * Parses the JSON data from the specified stream into map of objects of the specified type.
     *
     * @param json
     *         JSON data
     * @param dtoInterface
     *         DTO interface
     * @return map of DTO
     * @throws IllegalArgumentException
     *         if can't provide any implementation for specified interface
     * @throws IOException
     *         if an i/o error occurs
     */
    public <T> JsonStringMap<T> createMapDtoFromJson(InputStream json, Class<T> dtoInterface) throws IOException {
        return createMapDtoFromJson(new InputStreamReader(json), dtoInterface);
    }

    //

    @SuppressWarnings("unchecked")
    private <T> DtoProvider<T> getDtoProvider(Class<T> dtoInterface) {
        DtoProvider<?> dtoProvider = dtoInterface2Providers.get(dtoInterface);
        if (dtoProvider == null) {
            throw new IllegalArgumentException("Unknown DTO type " + dtoInterface);
        }
        return (DtoProvider<T>)dtoProvider;
    }

    /**
     * Registers DtoProvider for DTO interface.
     *
     * @param dtoInterface
     *         DTO interface
     * @param provider
     *         provider for DTO interface
     * @see DtoProvider
     */
    public void registerProvider(Class<?> dtoInterface, DtoProvider<?> provider) {
        dtoInterface2Providers.put(dtoInterface, provider);
        dtoImpl2Providers.put(provider.getImplClass(), provider);
    }

    /**
     * Unregisters DtoProvider.
     *
     * @see #registerProvider(Class, DtoProvider)
     */
    public DtoProvider<?> unregisterProvider(Class<?> dtoInterface) {
        final DtoProvider<?> dtoProvider = dtoInterface2Providers.remove(dtoInterface);
        if (dtoProvider != null) {
            dtoImpl2Providers.remove(dtoProvider.getImplClass());
        }
        return dtoProvider;
    }

    /** Test weather or not this DtoFactory has any DtoProvider which can provide implementation of DTO interface. */
    public boolean hasProvider(Class<?> dtoInterface) {
        return dtoInterface2Providers.get(dtoInterface) != null;
    }

    static {
        for (DtoFactoryVisitor visitor : ServiceLoader.load(DtoFactoryVisitor.class)) {
            visitor.accept(INSTANCE);
        }
    }

    private DtoFactory() {
    }
}
