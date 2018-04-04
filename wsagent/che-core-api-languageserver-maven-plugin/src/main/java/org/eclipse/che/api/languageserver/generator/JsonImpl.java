/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.generator;

import java.io.PrintWriter;

/**
 * Interface that abstracts different implementations of json support.
 *
 * @author Thomas Mäder
 */
public interface JsonImpl {

  /** @return the class name of a general json value. */
  String element();

  /** @return the class name of a json object. */
  String object();

  /** @return the class name of a json array. */
  String array();

  /** @return expression designating the json null value. */
  String nullValue();

  /** @return the class name of a json string value. */
  Object string();

  /** @return the class name of a json number value. */
  Object number();

  /** @return an expression converting the given expression to a json boolean */
  String boolValue(String value);

  /** @return an expression converting the given json element to a json object */
  Object objectValue(String value);

  /** @return an expression converting the given json element to a boolean */
  String asBoolean(String valueName);

  /** @return an expression converting the given json element to a double */
  String asDouble(String valueName);

  /** @return an expression converting the given json element to a String */
  String asString(String valueName);

  /** @return an expression converting the given json element to a json array */
  String arrayValue(String jsonValName);

  /** @return the name of the method on a json object to set a field value */
  String put();

  /**
   * @param arrayName the name of the array
   * @param valueName the name of the value to add
   * @return code that adds a value to json array.
   */
  String add(String arrayName, String valueName);

  /**
   * Write an iteration over the given json object to the output writer, delegating handling of
   * individual properties to the given handler
   *
   * @param indent the base indent to use
   * @param out where to write text
   * @param objectName the name of the json object to iterate over
   * @param handler the handler to delegate property writing to.
   */
  void iterateObject(String indent, PrintWriter out, String objectName, PropertyHandler handler);

  /**
   * Generate code to check whether the given value is the json null value
   *
   * @param jsonValName
   * @return
   */
  String isNull(String jsonValName);

  /** @return an expression converting the given string to a json value */
  String parse(String string);

  /** @return write imports for all classes referenced in the implementation of this interface */
  void writeImports(PrintWriter out);

  /**
   * An interface to handle the fields in a json object
   *
   * @author Thomas Mäder
   */
  interface PropertyHandler {
    void handle(String keyName, String valueName);
  }
}
