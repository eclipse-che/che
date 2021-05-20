/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.config;

import static java.lang.String.join;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Configuration of server that can be started inside of machine.
 *
 * @author Alexander Garagatyi
 */
public interface ServerConfig {

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can identify server as internal or
   * external. Attribute value {@code true} makes a server internal, any other value or lack of the
   * attribute makes the server external.
   */
  String INTERNAL_SERVER_ATTRIBUTE = "internal";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can identify server as secure or
   * non-secure. Requests to secure servers will be authenticated and must contain machine token.
   * Attribute value {@code true} makes a server secure, any other value or lack of the attribute
   * makes the server non-secure.
   */
  String SECURE_SERVER_ATTRIBUTE = "secure";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can contain an comma-separated
   * list of URI-s which are considered as non-secure on the given server and can be accessible with
   * unauthenticated requests.
   */
  String UNSECURED_PATHS_ATTRIBUTE = "unsecuredPaths";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which indicates whether authentication
   * with cookies is allowed or not. Attribute value {@code true} cookies authentication enabled,
   * any other value or lack of the attribute denies to use cookies authentication.
   */
  String SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE = "cookiesAuthEnabled";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which sets the server as unique, meaning
   * that, if exposed, it has its own endpoint even if it shares the same port with other servers.
   */
  String UNIQUE_SERVER_ATTRIBUTE = "unique";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can identify endpoint as
   * discoverable(i.e. it is accessible by its name from workspace's containers). Attribute value
   * {@code true} makes a endpoint discoverable, any other value or lack of the attribute makes the
   * server non-discoverable.
   */
  String DISCOVERABLE_SERVER_ATTRIBUTE = "discoverable";

  /**
   * This attribute is used to remember {@link Endpoint#getName()} inside {@link ServerConfig} for
   * internal use.
   */
  String SERVER_NAME_ATTRIBUTE = "serverName";

  /**
   * This attribute is used to remember name of the service for single-host gateway configuration.
   * It's used internally only, so the attribute is removed from {@link ServerConfig}'s attributes
   * before going outside.
   */
  String SERVICE_NAME_ATTRIBUTE = "serviceName";

  /**
   * This attribute is used to remember port of the service for single-host gateway configuration.
   * It's used internally only, so the attribute is removed from {@link ServerConfig}'s attributes
   * before going outside.
   */
  String SERVICE_PORT_ATTRIBUTE = "servicePort";

  /**
   * This attributes is marking that server should be exposed on subdomain if we're on single-host.
   * It has no effect on other server exposure strategies.
   */
  String REQUIRE_SUBDOMAIN = "requireSubdomain";

  /** Attribute that specifies the base location of the JWT authenticating callback. */
  String ENDPOINT_ORIGIN = "endpointOrigin";

  /**
   * Port used by server.
   *
   * <p>It may contain protocol(tcp or udp) after '/' symbol. If protocol is missing tcp will be
   * used by default. Example:
   *
   * <ul>
   *   <li>8080/tcp
   *   <li>8080/udp
   *   <li>8080
   * </ul>
   */
  String getPort();

  /**
   * Protocol for configuring preview url of this server.
   *
   * <p>Example:
   *
   * <ul>
   *   <li>http
   *   <li>https
   *   <li>tcp
   *   <li>udp
   *   <li>ws
   *   <li>wss
   * </ul>
   */
  String getProtocol();

  /** Path used by server. */
  @Nullable
  String getPath();

  /** Attributes of the server */
  Map<String, String> getAttributes();

  /**
   * Determines whether the attributes configure the server to be internal.
   *
   * @param attributes the attributes with additional server configuration
   * @see #INTERNAL_SERVER_ATTRIBUTE
   */
  static boolean isInternal(Map<String, String> attributes) {
    return AttributesEvaluator.booleanAttr(attributes, INTERNAL_SERVER_ATTRIBUTE, false);
  }

  /**
   * Sets the "internal" flag in the provided attributes to the provided value.
   *
   * @param attributes the attributes with the additional server configuration
   */
  static void setInternal(Map<String, String> attributes, boolean value) {
    attributes.put(INTERNAL_SERVER_ATTRIBUTE, Boolean.toString(value));
  }

  /**
   * Determines whether the attributes configure the server to be secure.
   *
   * @param attributes the attributes with additional server configuration
   * @see #SECURE_SERVER_ATTRIBUTE
   */
  static boolean isSecure(Map<String, String> attributes) {
    return AttributesEvaluator.booleanAttr(attributes, SECURE_SERVER_ATTRIBUTE, false);
  }

  /**
   * Sets the "secure" flag in the provided attributes to the provided value.
   *
   * @param attributes the attributes with the additional server configuration
   */
  static void setSecure(Map<String, String> attributes, boolean value) {
    attributes.put(SECURE_SERVER_ATTRIBUTE, Boolean.toString(value));
  }

  /**
   * Determines whether the attributes configure the server to be unique.
   *
   * @param attributes the attributes with additional server configuration
   * @see #UNIQUE_SERVER_ATTRIBUTE
   */
  static boolean isUnique(Map<String, String> attributes) {
    return AttributesEvaluator.booleanAttr(attributes, UNIQUE_SERVER_ATTRIBUTE, false);
  }

  /**
   * Sets the "unique" flag in the provided attributes to the provided value.
   *
   * @param attributes the attributes with the additional server configuration
   */
  static void setUnique(Map<String, String> attributes, boolean value) {
    attributes.put(UNIQUE_SERVER_ATTRIBUTE, Boolean.toString(value));
  }

  /**
   * Determines whether the attributes configure the server to be discoverable.
   *
   * @param attributes the attributes with additional server configuration
   * @see #DISCOVERABLE_SERVER_ATTRIBUTE
   */
  static boolean isDiscoverable(Map<String, String> attributes) {
    return AttributesEvaluator.booleanAttr(attributes, DISCOVERABLE_SERVER_ATTRIBUTE, false);
  }

  /**
   * Determines whether the attributes configure the server to be authenticated using JWT cookies. A
   * null value means that the attributes don't require any particular authentication.
   *
   * @param attributes the attributes with additional server configuration
   * @see #SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE
   */
  static @Nullable Boolean isCookiesAuthEnabled(Map<String, String> attributes) {
    String val = attributes.get(SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE);
    return val == null ? null : Boolean.parseBoolean(val);
  }

  /**
   * Sets the "cookiesAuthEnabled" flag in the provided attributes to the provided value. A null
   * value means that the attributes don't require any particular authentication.
   *
   * @param attributes the attributes with the additional server configuration
   */
  static void setCookiesAuthEnabled(Map<String, String> attributes, @Nullable Boolean value) {
    if (value == null) {
      attributes.remove(SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE);
    } else {
      attributes.put(SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE, Boolean.toString(value));
    }
  }

  /**
   * This is checking if the attributes configure the server to be exposed on a subdomain if we're
   * on single-host. It has no effect on other server exposure strategies.
   */
  static boolean isRequireSubdomain(Map<String, String> attributes) {
    return AttributesEvaluator.booleanAttr(attributes, REQUIRE_SUBDOMAIN, false);
  }

  /**
   * Modify the attributes to configure the server to be exposed on a subdomain if we're on
   * single-host. It has no effect on other server exposure strategies.
   */
  static void setRequireSubdomain(Map<String, String> attributes, boolean value) {
    if (value) {
      attributes.put(REQUIRE_SUBDOMAIN, Boolean.TRUE.toString());
    } else {
      attributes.remove(REQUIRE_SUBDOMAIN);
    }
  }

  /**
   * Returns the base location of the JWT authenticating callback.
   *
   * @param attributes the server attributes
   */
  static @Nullable String getEndpointOrigin(Map<String, String> attributes) {
    return attributes.get(ENDPOINT_ORIGIN);
  }

  /**
   * Sets the base location of the JWT authenticating callback.
   *
   * @param attributes the server attributes
   * @param value the auth origin or null if none should be used
   */
  static void setEndpointOrigin(Map<String, String> attributes, @Nullable String value) {
    if (value == null) {
      attributes.remove(ENDPOINT_ORIGIN);
    } else {
      attributes.putIfAbsent(ENDPOINT_ORIGIN, value);
    }
  }

  /**
   * Finds the unsecured paths configuration in the provided attributes.s
   *
   * @param attributes the attributes with additional server configuration
   * @see #UNSECURED_PATHS_ATTRIBUTE
   */
  static List<String> getUnsecuredPaths(Map<String, String> attributes) {
    if (attributes == null) {
      return emptyList();
    }

    String paths = attributes.get(UNSECURED_PATHS_ATTRIBUTE);
    if (paths == null) {
      return emptyList();
    }

    return Arrays.asList(paths.split("\\s*,\\s*"));
  }

  static void setUnsecuredPaths(Map<String, String> attributes, List<String> value) {
    attributes.put(UNSECURED_PATHS_ATTRIBUTE, join(",", value));
  }

  /** @see #isInternal(Map) */
  default boolean isInternal() {
    return isInternal(getAttributes());
  }

  /** @see #isSecure(Map) */
  default boolean isSecure() {
    return isSecure(getAttributes());
  }

  /** @see #isUnique(Map) */
  default boolean isUnique() {
    return isUnique(getAttributes());
  }

  /** @see #isCookiesAuthEnabled(Map) */
  default @Nullable Boolean isCookiesAuthEnabled() {
    return isCookiesAuthEnabled(getAttributes());
  }

  /** @see #getUnsecuredPaths(Map) */
  default List<String> getUnsecuredPaths() {
    return getUnsecuredPaths(getAttributes());
  }

  /** @see #isDiscoverable(Map) */
  default boolean isDiscoverable() {
    return isDiscoverable(getAttributes());
  }

  /** @see #isRequireSubdomain(Map) */
  default boolean isRequireSubdomain() {
    return isRequireSubdomain(getAttributes());
  }

  /** @see #getEndpointOrigin(Map) */
  default String getEndpointOrigin() {
    return getEndpointOrigin(getAttributes());
  }
}

// helper class for the default methods in the above interface
class AttributesEvaluator {
  static boolean booleanAttr(Map<String, String> attrs, String name, boolean defaultValue) {
    if (attrs == null) {
      return defaultValue;
    }

    String attr = attrs.get(name);
    if (attr == null) {
      return defaultValue;
    }

    return Boolean.parseBoolean(attr);
  }
}
