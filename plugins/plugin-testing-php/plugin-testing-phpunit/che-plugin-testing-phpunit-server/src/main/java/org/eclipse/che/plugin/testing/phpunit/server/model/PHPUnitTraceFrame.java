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
package org.eclipse.che.plugin.testing.phpunit.server.model;

import java.nio.file.Paths;
import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Trace frame model element implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTraceFrame extends AbstractPHPUnitElement {

  private String traceFunction = ""; // $NON-NLS-1$
  private String traceClass = ""; // $NON-NLS-1$
  private String traceType = PHPUnitMessageParser.CALL_DYNAMIC;

  public PHPUnitTraceFrame(final Map<?, ?> frame, final AbstractPHPUnitTestEvent parent) {
    super(frame, parent);
    traceFunction = (String) frame.get("function"); // $NON-NLS-1$
    traceClass = (String) frame.get("class"); // $NON-NLS-1$
    traceType = (String) frame.get("type"); // $NON-NLS-1$
  }

  /**
   * Returns trace frame function name.
   *
   * @return trace frame function name
   */
  public String getFunction() {
    return traceFunction;
  }

  /**
   * Returns trace frame class name.
   *
   * @return trace frame class name
   */
  public String getClassName() {
    return traceClass;
  }

  /**
   * Returns trace frame type.
   *
   * @return trace frame type
   */
  public String getTraceType() {
    return traceType;
  }

  @Override
  public String toString() {
    final StringBuffer result = new StringBuffer(1);
    if (traceClass != null) result.append(traceClass);
    if (traceType != null) result.append(traceType);
    result.append(traceFunction);
    if (file != null) {
      String fileName = Paths.get(file).getFileName().toString();
      result.append('(' + fileName + ':' + line + ')');
    } else {
      result.append("()");
    }
    return result.toString();
  }
}
