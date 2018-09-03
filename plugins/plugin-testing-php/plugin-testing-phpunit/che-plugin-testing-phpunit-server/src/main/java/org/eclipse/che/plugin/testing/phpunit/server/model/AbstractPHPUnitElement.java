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

import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Abstract implementation for PHP unit model elements.
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractPHPUnitElement {

  protected String file = "";
  protected boolean isFiltered = false;
  protected int line = 0;
  protected AbstractPHPUnitElement parent;

  public AbstractPHPUnitElement(final Map<?, ?> properties, final AbstractPHPUnitElement parent) {
    if (properties != null) init(properties);
    if (parent != null) setParent(parent);
  }

  /**
   * Sets this element parent.
   *
   * @param parent
   */
  public void setParent(final AbstractPHPUnitElement parent) {
    this.parent = parent;
  }

  /**
   * Returns related element file path.
   *
   * @return related element file path
   */
  public String getFile() {
    return file;
  }

  /**
   * Returns related element file line.
   *
   * @return related element file line
   */
  public int getLine() {
    return line;
  }

  /**
   * Returns element's parent.
   *
   * @return element's parent
   */
  public AbstractPHPUnitElement getParent() {
    return parent;
  }

  /**
   * Checks if this element is filtered.
   *
   * @return <code>true</code> if this element is filtered, <code>false</code> otherwise
   */
  public boolean isFiltered() {
    return isFiltered;
  }

  private void init(final Map<?, ?> properties) {
    final String pFile = (String) properties.get(PHPUnitMessageParser.PROPERTY_FILE);
    if (pFile != null) {
      file = (String) properties.get(PHPUnitMessageParser.PROPERTY_FILE);
      line = Integer.parseInt((String) properties.get(PHPUnitMessageParser.PROPERTY_LINE));
    }
    if (properties.get(PHPUnitMessageParser.PROPERTY_FILTERED) != null) {
      isFiltered = true;
    }
  }
}
