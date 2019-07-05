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

import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_NULL;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType;

/**
 * Container for storing expression evaluation result.
 *
 * @author Bartlomiej Laczkowski
 */
class ZendDbgExpressionResult {

  static final ZendDbgExpressionResult NULL =
      new ZendDbgExpressionResult(PHP_NULL.getText(), DataType.PHP_NULL);

  private final String value;
  private final DataType dataType;
  private final int childrenCount;
  private final List<IDbgExpression> children;

  ZendDbgExpressionResult(String value, DataType dataType) {
    this(value, dataType, 0);
  }

  ZendDbgExpressionResult(String value, DataType dataType, int childrenCount) {
    this(value, dataType, childrenCount, null);
  }

  ZendDbgExpressionResult(
      String value, DataType dataType, int childrenCount, List<IDbgExpression> children) {
    super();
    this.value = value;
    this.dataType = dataType;
    this.childrenCount = childrenCount;
    this.children = children != null ? children : Collections.emptyList();
  }

  /**
   * Returns textual value for expression result.
   *
   * @return textual value for expression result
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns PHP data type for expression result.
   *
   * @return PHP data type for expression result
   */
  public DataType getDataType() {
    return dataType;
  }

  /**
   * Returns number of child elements for expression result.
   *
   * @return number of child elements for expression result
   */
  public int getChildrenCount() {
    return childrenCount;
  }

  /**
   * Returns child elements for expression result.
   *
   * @return child elements for expression result
   */
  public List<IDbgExpression> getChildren() {
    return children;
  }
}
