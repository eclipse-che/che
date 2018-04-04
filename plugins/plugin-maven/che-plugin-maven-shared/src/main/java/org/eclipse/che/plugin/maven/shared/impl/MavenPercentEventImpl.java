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
package org.eclipse.che.plugin.maven.shared.impl;

import org.eclipse.che.plugin.maven.shared.event.MavenPercentMessageEvent;

/** Implementation of the event which describes percent of the project resolving. */
public class MavenPercentEventImpl extends MavenOutputEventImpl
    implements MavenPercentMessageEvent {
  private final double percent;

  public MavenPercentEventImpl(double percent, TYPE type) {
    super(type);
    this.percent = percent;
  }

  @Override
  public double getPercent() {
    return percent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MavenPercentEventImpl)) return false;
    if (!super.equals(o)) return false;

    MavenPercentEventImpl that = (MavenPercentEventImpl) o;

    return percent == that.percent;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Double.hashCode(percent);
    return result;
  }
}
