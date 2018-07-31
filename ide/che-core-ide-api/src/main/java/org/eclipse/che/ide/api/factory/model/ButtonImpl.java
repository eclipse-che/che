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
package org.eclipse.che.ide.api.factory.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.ButtonAttributes;

/** Data object for {@link Button}. */
public class ButtonImpl implements Button {

  private ButtonAttributesImpl attributes;
  private Type type;

  public ButtonImpl(ButtonAttributes attributes, Type type) {
    this.attributes = new ButtonAttributesImpl(attributes);
    this.type = type;
  }

  public ButtonImpl(Button button) {
    this(button.getAttributes(), button.getType());
  }

  @Override
  public ButtonAttributesImpl getAttributes() {
    return attributes;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ButtonImpl)) {
      return false;
    }
    final ButtonImpl that = (ButtonImpl) obj;
    return Objects.equals(attributes, that.attributes) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(attributes);
    hash = 31 * hash + Objects.hashCode(type);
    return hash;
  }

  @Override
  public String toString() {
    return "ButtonImpl{" + "attributes=" + attributes + ", type=" + type + '}';
  }
}
