/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Abstract implementation for PHP unit test event elements.
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractPHPUnitTestEvent extends AbstractPHPUnitElement {

  protected String message;
  protected String diff;
  protected List<PHPUnitTraceFrame> trace;

  public AbstractPHPUnitTestEvent(final Map<?, ?> event, final AbstractPHPUnitElement parent) {
    super(event, parent);
    message = (String) event.get(PHPUnitMessageParser.PROPERTY_MESSAGE);
    diff = (String) event.get(PHPUnitMessageParser.PROPERTY_DIFF);
    final Map<?, ?> mTrace = (Map<?, ?>) event.get(PHPUnitMessageParser.PROPERTY_TRACE);
    if (mTrace == null || mTrace.isEmpty()) return;
    trace = new ArrayList<PHPUnitTraceFrame>(mTrace.size());
    for (int i = 0; i < mTrace.size(); ++i) {
      trace.add(new PHPUnitTraceFrame((Map<?, ?>) mTrace.get(String.valueOf(i)), this));
    }
  }

  /**
   * Returns element description.
   *
   * @return element description
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns diff.
   *
   * @return diff
   */
  public String getDiff() {
    return diff;
  }

  /**
   * Returns element related trace.
   *
   * @return element related trace
   */
  public List<PHPUnitTraceFrame> getTrace() {
    return trace;
  }
}
