/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.server;

import static java.lang.String.format;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.commons.lang.reflect.ParameterizedTypeImpl;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonArray;
import org.eclipse.che.dto.shared.JsonStringMap;

/**
 * Provides implementations of DTO interfaces.
 *
 * @author andrew00x
 */
public final class DtoFactory {

  private static final LoadingCache<Type, ParameterizedType> listTypeCache =
      CacheBuilder.newBuilder()
          .concurrencyLevel(16)
          .build(
              new CacheLoader<Type, ParameterizedType>() {
                @Override
                public ParameterizedType load(Type type) {
                  return new ParameterizedTypeImpl(List.class, type);
                }
              });

  private static final LoadingCache<Type, ParameterizedType> mapTypeCache =
      CacheBuilder.newBuilder()
          .concurrencyLevel(16)
          .build(
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
   * Ge a {@link Gson} serializer that is configured to serializes/deserializes DTOs correctly.
   *
   * @return A Gson.
   */
  public Gson getGson() {
    return dtoGson;
  }

  /**
   * Creates new instance of class which implements specified DTO interface.
   *
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public static <T> T newDto(Class<T> dtoInterface) {
    return getInstance().createDto(dtoInterface);
  }

  private final Map<Class<?>, DtoProvider<?>> dtoInterface2Providers = new ConcurrentHashMap<>();
  // Additional mapping for implementation of DTO interfaces.
  // It helps avoid reflection when need create copy of exited DTO instance.
  private final Map<Class<?>, DtoProvider<?>> dtoImpl2Providers = new ConcurrentHashMap<>();
  private final Gson dtoGson =
      buildDtoParser(
          ServiceLoader.load(TypeAdapterFactory.class).iterator(), new DtoInterfaceTAF());

  /**
   * Created deep copy of DTO object.
   *
   * @param origin origin DTO object
   * @return copy
   * @throws IllegalArgumentException if specified object doesn't implement DTO interface annotated
   *     with {@link org.eclipse.che.dto.shared.DTO &#064DTO} or if specified instance implements
   *     more than one interface annotated with {@link org.eclipse.che.dto.shared.DTO &#064DTO}
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
            throw new IllegalArgumentException(
                "Unable determine DTO interface. Type "
                    + implClass.getName()
                    + " implements or extends more than one interface annotated with @DTO annotation.");
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

    return (T) provider.clone(origin);
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
      return ((JsonSerializable) dto).toJson();
    }
    throw new IllegalArgumentException("JsonSerializable instance required. ");
  }

  public <T> JsonElement toJsonElement(T dto) {
    if (dto instanceof JsonSerializable) {
      return ((JsonSerializable) dto).toJsonElement();
    }
    throw new IllegalArgumentException("JsonSerializable instance required. ");
  }

  /**
   * Creates new instance of class which implements specified DTO interface.
   *
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> T createDto(Class<T> dtoInterface) {
    return getDtoProvider(dtoInterface).newInstance();
  }

  //

  /**
   * Creates new instance of class which implements specified DTO interface, parses specified JSON
   * string and uses parsed data for initializing fields of DTO object.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> T createDtoFromJson(String json, Class<T> dtoInterface) {
    try {
      return createDtoFromJson(new StringReader(json), dtoInterface);
    } catch (IOException e) {
      throw new RuntimeException(e); // won't happen
    }
  }

  /**
   * Creates new instance of class which implements specified DTO interface, uses the specific JSON
   * data for initializing fields of DTO object.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> T createDtoFromJson(JsonElement json, Class<T> dtoInterface) {
    return getDtoProvider(dtoInterface).fromJson(json);
  }

  /**
   * Creates new instance of class which implements specified DTO interface, parses specified JSON
   * data and uses parsed data for initializing fields of DTO object.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   * @throws IOException if an i/o error occurs
   */
  public <T> T createDtoFromJson(Reader json, Class<T> dtoInterface) throws IOException {
    getDtoProvider(dtoInterface);
    return dtoGson.fromJson(json, dtoInterface);
  }

  /**
   * Creates new instance of class which implements specified DTO interface, parses specified JSON
   * data and uses parsed data for initializing fields of DTO object.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   * @throws IOException if an i/o error occurs
   */
  public <T> T createDtoFromJson(InputStream json, Class<T> dtoInterface) throws IOException {
    return createDtoFromJson(new InputStreamReader(json), dtoInterface);
  }

  //

  /**
   * Parses the JSON data from the specified sting into list of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return list of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> JsonArray<T> createListDtoFromJson(String json, Class<T> dtoInterface) {
    try {
      return createListDtoFromJson(new StringReader(json), dtoInterface);
    } catch (IOException e) {
      throw new RuntimeException(e); // won't happen
    }
  }

  /**
   * Parses the JSON data from the specified reader into list of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return list of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> JsonArray<T> createListDtoFromJson(Reader json, Class<T> dtoInterface)
      throws IOException {
    getDtoProvider(dtoInterface);
    final List<T> list = parseDto(json, listTypeCache.getUnchecked(dtoInterface));
    return new JsonArrayImpl<>(list);
  }

  /**
   * Parses the JSON data from the specified stream into list of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return list of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   * @throws IOException if an i/o error occurs
   */
  public <T> JsonArray<T> createListDtoFromJson(InputStream json, Class<T> dtoInterface)
      throws IOException {
    return createListDtoFromJson(new InputStreamReader(json), dtoInterface);
  }

  //

  /**
   * Parses the JSON data from the specified sting into map of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return map of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> JsonStringMap<T> createMapDtoFromJson(String json, Class<T> dtoInterface) {
    try {
      return createMapDtoFromJson(new StringReader(json), dtoInterface);
    } catch (IOException e) {
      throw new RuntimeException(e); // won't happen
    }
  }

  /**
   * Parses the JSON data from the specified reader into map of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return map of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   * @throws IOException if an i/o error occurs
   */
  public <T> JsonStringMap<T> createMapDtoFromJson(Reader json, Class<T> dtoInterface)
      throws IOException {
    getDtoProvider(dtoInterface);
    final Map<String, T> map = parseDto(json, mapTypeCache.getUnchecked(dtoInterface));
    return new JsonStringMapImpl<>(map);
  }

  /**
   * Parse a JSON string that contains DTOs, propagating JSON exceptions correctly if they are
   * caused by failures in the given Reader. Real JSON syntax exceptions are propagated as-is.
   */
  private <T> T parseDto(Reader json, Type type) throws IOException {
    try {
      return dtoGson.fromJson(json, type);
    } catch (JsonSyntaxException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw e;
    }
  }

  /**
   * Parses the JSON data from the specified stream into map of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return map of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   * @throws IOException if an i/o error occurs
   */
  public <T> JsonStringMap<T> createMapDtoFromJson(InputStream json, Class<T> dtoInterface)
      throws IOException {
    return createMapDtoFromJson(new InputStreamReader(json), dtoInterface);
  }

  //

  @SuppressWarnings("unchecked")
  private <T> DtoProvider<T> getDtoProvider(Class<T> dtoInterface) {
    DtoProvider<?> dtoProvider = dtoInterface2Providers.get(dtoInterface);
    if (dtoProvider == null) {
      if (!dtoInterface.isInterface()) {
        throw new IllegalArgumentException(
            format("Only interfaces can be DTO, but %s is not an interface.", dtoInterface));
      }

      if (hasDtoAnnotation(dtoInterface)) {
        throw new IllegalArgumentException(
            format("Provider of implementation for DTO type %s is not found", dtoInterface));
      } else {
        throw new IllegalArgumentException(dtoInterface + " is not a DTO type");
      }
    }

    return (DtoProvider<T>) dtoProvider;
  }

  /**
   * Checks if dtoInterface or its parent have DTO annotation.
   *
   * @param dtoInterface DTO interface
   * @return true if only dtoInterface or one of its parent have DTO annotation.
   */
  private boolean hasDtoAnnotation(Class dtoInterface) {
    if (dtoInterface.isAnnotationPresent(DTO.class)) {
      return true;
    }

    for (Class parent : dtoInterface.getInterfaces()) {
      if (hasDtoAnnotation(parent)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Registers DtoProvider for DTO interface.
   *
   * @param dtoInterface DTO interface
   * @param provider provider for DTO interface
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

  /**
   * Test weather or not this DtoFactory has any DtoProvider which can provide implementation of DTO
   * interface.
   */
  public boolean hasProvider(Class<?> dtoInterface) {
    return dtoInterface2Providers.get(dtoInterface) != null;
  }

  /**
   * A specialization of Gson's {@link ReflectiveTypeAdapterFactory} delegates operation on DTO
   * interfaces to the corresponding implementation classes. The implementation classes generated
   * correctly by the DTO Gson.
   *
   * @author tareq.sha@gmail.com
   */
  private class DtoInterfaceTAF implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      DtoProvider<?> prov = dtoInterface2Providers.get(type.getRawType());
      if (prov != null) {
        return (TypeAdapter<T>) gson.getAdapter(prov.getImplClass());
      }
      return null;
    }
  }

  /**
   * Wraps Gson's default List/Map adapter factories serialize null List/Map fields as empty
   * instead.
   *
   * @author tareq.sha@gmail.com
   */
  private static class NullAsEmptyTAF<U> implements TypeAdapterFactory {
    final Class<?> matchedClass;
    final U defaultValue;

    NullAsEmptyTAF(Class<U> matchedClass, U defaultValue) {
      this.matchedClass = matchedClass;
      this.defaultValue = defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!matchedClass.isAssignableFrom(type.getRawType())) {
        return null;
      }
      TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          delegate.write(out, value != null ? value : (T) defaultValue);
        }

        @Override
        public T read(JsonReader in) throws IOException {
          return delegate.read(in);
        }
      };
    }
  }

  static {
    for (DtoFactoryVisitor visitor : ServiceLoader.load(DtoFactoryVisitor.class)) {
      visitor.accept(INSTANCE);
    }
  }

  private DtoFactory() {}

  private static Gson buildDtoParser(
      Iterator<TypeAdapterFactory> factoryIterator, TypeAdapterFactory... factories) {
    GsonBuilder builder = new GsonBuilder();

    for (Iterator<TypeAdapterFactory> it = factoryIterator; it.hasNext(); ) {
      TypeAdapterFactory factory = it.next();
      builder.registerTypeAdapterFactory(factory);
    }

    for (TypeAdapterFactory factory : factories) {
      builder.registerTypeAdapterFactory(factory);
    }

    if (Boolean.valueOf(System.getenv("CHE_LEGACY__DTO__JSON__SERIALIZATION"))) {
      builder.registerTypeAdapterFactory(
          new NullAsEmptyTAF<>(Collection.class, Collections.emptyList()));
      builder.registerTypeAdapterFactory(new NullAsEmptyTAF<>(Map.class, Collections.emptyMap()));
    } else {
      builder.registerTypeHierarchyAdapter(Collection.class, new NullOrEmptyCollectionAdapter());
      builder.registerTypeHierarchyAdapter(Map.class, new NullOrEmptyMapAdapter());
    }

    builder.registerTypeAdapterFactory(new SerializableInterfaceAdapterFactory());

    return builder.create();
  }
}
