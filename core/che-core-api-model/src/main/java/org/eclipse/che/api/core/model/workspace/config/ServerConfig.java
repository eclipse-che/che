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
package org.eclipse.che.api.core.model.workspace.config;

import static java.lang.String.join;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
