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
package org.eclipse.che.api.debug.shared.model.impl;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.VariablePath;

/** @author Anatoliy Bazko */
public class VariablePathImpl implements VariablePath {
  private final List<String> path;

  public VariablePathImpl(List<String> path) {
    this.path = path;
  }

  public VariablePathImpl(String name) {
    this(Collections.singletonList(name));
  }

  @Override
  public List<String> getPath() {
    return Collections.unmodifiableList(path);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VariablePathImpl)) return false;

    VariablePathImpl that = (VariablePathImpl) o;

    return !(path != null ? !path.equals(that.path) : that.path != null);
  }

  @Override
  public int hashCode() {
    return path != null ? path.hashCode() : 0;
  }
}
