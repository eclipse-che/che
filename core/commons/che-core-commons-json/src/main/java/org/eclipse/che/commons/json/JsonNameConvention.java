/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.json;

/**
 * Abstraction to provide name transformation between JSON and Java names. It helps correct
 * translate JSON names to correct name of Java fields or methods, e.g translate Java camel-case
 * name to lowercase JSON names with '-' or '_' separator. Pass implementation of this interface to
 * methods of {@link JsonHelper} to get required behaviour whe serialize or deserialize objects
 * to|from JSON.
 *
 * @see JsonNameConventions
 * @see NameConventionJsonParser
 * @see NameConventionJsonWriter
 */
public interface JsonNameConvention {
  /**
   * Translate Java field name to JSON name, e.g. 'userName' -> 'user_name'
   *
   * @param javaName Java field name
   * @return JSON name
   */
  String toJsonName(String javaName);

  /**
   * Translate JSON name to Java field name, e.g. 'user_name' -> 'userName'
   *
   * @param jsonName JSON name
   * @return Java field name
   */
  String toJavaName(String jsonName);
}
