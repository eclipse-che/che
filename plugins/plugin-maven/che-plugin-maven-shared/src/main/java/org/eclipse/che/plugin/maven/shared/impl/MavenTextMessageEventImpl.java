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

import org.eclipse.che.plugin.maven.shared.event.MavenTextMessageEvent;

/** Implementation of the {@link MavenTextMessageEvent}. */
public class MavenTextMessageEventImpl extends MavenOutputEventImpl
    implements MavenTextMessageEvent {
  private final String message;

  public MavenTextMessageEventImpl(String message, TYPE type) {
    super(type);
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MavenTextMessageEventImpl)) return false;
    if (!super.equals(o)) return false;

    MavenTextMessageEventImpl that = (MavenTextMessageEventImpl) o;

    return message.equals(that.message);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }
}
