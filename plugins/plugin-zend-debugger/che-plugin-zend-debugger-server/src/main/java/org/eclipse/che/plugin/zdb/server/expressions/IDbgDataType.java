/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.expressions;

/**
 * Interface for variables/values that are responsible for handling the particular PHP data type.
 *
 * @author Bartlomiej Laczkowski
 */
public interface IDbgDataType {

  /** PHP data type enum. */
  public enum DataType {
    PHP_BOOL("bool"),
    PHP_INT("int"),
    PHP_FLOAT("float"),
    PHP_STRING("string"),
    PHP_NULL("null"),
    PHP_ARRAY("array"),
    PHP_OBJECT("object"),
    PHP_RESOURCE("resource"),
    PHP_VIRTUAL_CLASS("class"),
    PHP_UNINITIALIZED("<uninitialized>");

    private String type;

    private DataType(String type) {
      this.type = type;
    }

    public String getText() {
      return type;
    }

    /**
     * Finds data type enum element by corresponding string value.
     *
     * @param type
     * @return data type enum element
     */
    public static DataType find(String type) {
      for (DataType t : values()) {
        if (t.getText().equalsIgnoreCase(type)) return t;
      }
      return PHP_UNINITIALIZED;
    }
  }

  /**
   * Returns related PHP data type.
   *
   * @return related PHP data type
   */
  public DataType getDataType();
}
