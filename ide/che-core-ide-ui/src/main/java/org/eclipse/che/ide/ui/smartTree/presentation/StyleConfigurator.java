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
package org.eclipse.che.ide.ui.smartTree.presentation;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.unmodifiableMap;

import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import java.util.HashMap;
import java.util.Map;

/**
 * Css style configurator with builder. Consists of basic style configuration and a map with
 * additional css properties.
 *
 * @author Vlad Zhukovskyi
 * @since 5.19.0
 */
public class StyleConfigurator {

  private static final String STYLE_COLOR = "color";
  private static final String STYLE_FONT_STYLE = "fontStyle";
  private static final String STYLE_FONT_WEIGHT = "fontWeight";

  private final Map<String, String> cssConfiguration;

  private StyleConfigurator(Builder builder) {
    cssConfiguration = new HashMap<>();

    if (!isNullOrEmpty(builder.color)) {
      cssConfiguration.put(STYLE_COLOR, builder.color);
    }

    if (builder.fontStyle != null) {
      cssConfiguration.put(STYLE_FONT_STYLE, builder.fontStyle.getCssName());
    }

    if (builder.fontWeight != null) {
      cssConfiguration.put(STYLE_FONT_WEIGHT, builder.fontWeight.getCssName());
    }

    if (builder.properties != null) {
      cssConfiguration.putAll(builder.properties);
    }
  }

  public Map<String, String> getCssConfiguration() {
    return unmodifiableMap(cssConfiguration);
  }

  public static class Builder {
    private String color;
    private FontStyle fontStyle;
    private FontWeight fontWeight;
    private Map<String, String> properties;

    public Builder() {}

    public Builder withColor(String color) {
      this.color = color;
      return this;
    }

    public Builder withFontStyle(FontStyle fontStyle) {
      this.fontStyle = fontStyle;
      return this;
    }

    public Builder withFontWeight(FontWeight fontWeight) {
      this.fontWeight = fontWeight;
      return this;
    }

    public Builder withProperty(String property, String value) {
      if (properties == null) {
        properties = new HashMap<>();
      }

      properties.put(property, value);
      return this;
    }

    public StyleConfigurator build() {
      return new StyleConfigurator(this);
    }
  }
}
