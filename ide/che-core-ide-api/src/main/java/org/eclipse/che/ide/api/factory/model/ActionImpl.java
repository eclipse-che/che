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
package org.eclipse.che.ide.api.factory.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Action;

/** Data object for {@link Action}. */
public class ActionImpl implements Action {

  private String id;
  private Map<String, String> properties;

  public ActionImpl(String id, Map<String, String> properties) {
    this.id = id;
    if (properties != null) {
      this.properties = new HashMap<>(properties);
    }
  }

  public ActionImpl(Action action) {
    this(action.getId(), action.getProperties());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Map<String, String> getProperties() {
    if (properties == null) {
      return new HashMap<>();
    }
    return properties;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ActionImpl)) {
      return false;
    }
    final ActionImpl that = (ActionImpl) obj;
    return Objects.equals(id, that.id) && getProperties().equals(that.getProperties());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + getProperties().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "ActionImpl{" + "id='" + id + '\'' + ", properties=" + properties + '}';
  }
}
