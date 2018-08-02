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
package org.eclipse.che.ide.api.factory.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.ButtonAttributes;

/** Data object for {@link ButtonAttributes}. */
public class ButtonAttributesImpl implements ButtonAttributes {

  private String color;
  private String logo;
  private String style;
  private Boolean counter;

  public ButtonAttributesImpl(String color, String logo, String style, Boolean counter) {
    this.color = color;
    this.logo = logo;
    this.style = style;
    this.counter = counter;
  }

  public ButtonAttributesImpl(ButtonAttributes attributes) {
    this(
        attributes.getColor(),
        attributes.getLogo(),
        attributes.getStyle(),
        attributes.getCounter());
  }

  @Override
  public String getColor() {
    return color;
  }

  @Override
  public String getLogo() {
    return logo;
  }

  @Override
  public String getStyle() {
    return style;
  }

  @Override
  public Boolean getCounter() {
    return counter;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ButtonAttributesImpl)) return false;
    final ButtonAttributesImpl other = (ButtonAttributesImpl) obj;
    return Objects.equals(color, other.color)
        && Objects.equals(logo, other.logo)
        && Objects.equals(style, other.style)
        && Objects.equals(counter, other.counter);
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 31 * result + Objects.hashCode(color);
    result = 31 * result + Objects.hashCode(logo);
    result = 31 * result + Objects.hashCode(style);
    result = 31 * result + Objects.hashCode(counter);
    return result;
  }

  @Override
  public String toString() {
    return "ButtonAttributesImpl{"
        + "color='"
        + color
        + '\''
        + ", logo='"
        + logo
        + '\''
        + ", style='"
        + style
        + '\''
        + ", counter="
        + counter
        + '}';
  }
}
