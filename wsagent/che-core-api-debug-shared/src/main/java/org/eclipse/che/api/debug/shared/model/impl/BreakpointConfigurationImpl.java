/*
 * ******************************************************************************
 *  * Copyright (c) 2012-2017 Red Hat, Inc.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *   Red Hat, Inc. - initial API and implementation
 *   ******************************************************************************
 */
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;

/** @author Igor Vinokur */
public class BreakpointConfigurationImpl implements BreakpointConfiguration {

  private final int hitCount;
  private String hitCondition;

  public BreakpointConfigurationImpl(String hitCondition, int hitCount) {
    this.hitCondition = hitCondition;
    this.hitCount = hitCount;
  }

  public BreakpointConfigurationImpl(BreakpointConfiguration breakpointConfiguration) {
    this.hitCondition = breakpointConfiguration.getCondition();
    this.hitCount = breakpointConfiguration.getHitCount();
  }
  @Override
  public String getCondition() {
    return hitCondition;
  }

  @Override
  public void setCondition(String condition) {
    this.hitCondition = condition;
  }

  @Override
  public int getHitCount() {
    return hitCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BreakpointConfigurationImpl)) return false;

    BreakpointConfigurationImpl that = (BreakpointConfigurationImpl) o;

    if (hitCount != that.hitCount) return false;
    return !(hitCondition != null ? !hitCondition.equals(that.hitCondition) : that.hitCondition != null);
  }

  @Override
  public int hashCode() {
    int result = 31 * hitCount;
    result = 31 * result + (hitCondition != null ? hitCondition.hashCode() : 0);
    return result;
  }
}
