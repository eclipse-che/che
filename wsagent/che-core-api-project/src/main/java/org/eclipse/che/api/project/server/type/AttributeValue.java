/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.project.type.Value;

/** @author gazarenkov */
public class AttributeValue implements Value {

  private final List<String> values = new ArrayList<>();

  public AttributeValue(List<String> list) {
    if (list != null) {
      values.addAll(list);
    }
  }

  public AttributeValue(String str) {
    if (str != null) {
      values.add(str);
    }
  }

  @Override
  public String getString() {
    return values.isEmpty() ? null : values.get(0);
  }

  public void setString(String str) {
    values.clear();
    if (str != null) {
      values.add(str);
    }
  }

  @Override
  public List<String> getList() {
    return values;
  }

  public void setList(List<String> list) {
    values.clear();
    if (list != null) {
      values.addAll(list);
    }
  }

  @Override
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeValue) {
      return this.values.equals(((AttributeValue) obj).getList());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return values != null ? values.hashCode() : 0;
  }
}
