/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
