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

import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Test warning model element implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestWarning extends AbstractPHPUnitTestEvent {

  private String code = ""; // $NON-NLS-1$

  public PHPUnitTestWarning(final Map<?, ?> warning, final AbstractPHPUnitElement parent) {
    super(warning, parent);
    code = (String) warning.get(PHPUnitMessageParser.PROPERTY_CODE);
  }

  /**
   * Returns warning related code.
   *
   * @return warning related code
   */
  public String getCode() {
    return code;
  }
}
