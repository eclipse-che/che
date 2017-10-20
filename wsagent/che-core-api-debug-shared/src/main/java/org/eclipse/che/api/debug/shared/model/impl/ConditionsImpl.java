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

import org.eclipse.che.api.debug.shared.model.Conditions;

/** @author Igor Vinokur */
public class ConditionsImpl implements Conditions {

private final int hitCount;
private String condition;

public ConditionsImpl(String condition, int hitCount) {
    this.condition = condition;
    this.hitCount = hitCount;
    
}

@Override
  public String getHitCondition() {
    return condition;
  }

  @Override
  public void setHitCondition(String condition) {
      this.condition = condition;
  }

  @Override
  public int getHitCount() {
    return hitCount;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConditionsImpl)) return false;

    ConditionsImpl that = (ConditionsImpl) o;
    
    if (hitCount != that.hitCount) return false;
    return !(condition != null ? !condition.equals(that.condition) : that.condition != null);
  }

  @Override
  public int hashCode() {
    int result = 31 * hitCount;
    result = 31 * result + (condition != null ? condition.hashCode() : 0);
    return result;
  } 
}
