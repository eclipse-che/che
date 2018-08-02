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

import com.google.gwt.dom.client.Element;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Node presentation which contains a useful builder to friendly build the presentation. Node which
 * should provide a new type of presentation should implement a {@link HasNewPresentation}
 * interface.
 *
 * @author Vlad Zhukovskyi
 * @since 5.19.0
 */
public class NewNodePresentation {

  private String nodeText;
  private StyleConfigurator nodeTextStyle;
  private String nodeInfoText;
  private StyleConfigurator nodeInfoTextStyle;
  private SVGResource icon;
  private Element userElement;

  private NewNodePresentation(Builder builder) {
    this.nodeText = builder.nodeText;
    this.nodeTextStyle = builder.nodeTextStyle;
    this.nodeInfoText = builder.nodeInfoText;
    this.nodeInfoTextStyle = builder.nodeInfoTextStyle;
    this.icon = builder.icon;
    this.userElement = builder.userElement;
  }

  public String getNodeText() {
    return nodeText;
  }

  public StyleConfigurator getNodeTextStyle() {
    return nodeTextStyle;
  }

  public String getNodeInfoText() {
    return nodeInfoText;
  }

  public StyleConfigurator getNodeInfoTextStyle() {
    return nodeInfoTextStyle;
  }

  public SVGResource getIcon() {
    return icon;
  }

  public Element getUserElement() {
    return userElement;
  }

  public static class Builder {
    private String nodeText;
    private StyleConfigurator nodeTextStyle;
    private String nodeInfoText;
    private StyleConfigurator nodeInfoTextStyle;
    private SVGResource icon;
    private Element userElement;

    public Builder() {}

    public Builder withNodeText(String nodeText) {
      this.nodeText = nodeText;
      return this;
    }

    public Builder withNodeTextStyle(StyleConfigurator nodeTextStyle) {
      this.nodeTextStyle = nodeTextStyle;
      return this;
    }

    public Builder withNodeInfoText(String nodeInfoText) {
      this.nodeInfoText = nodeInfoText;
      return this;
    }

    public Builder withNodeIntoTextStyle(StyleConfigurator nodeInfoTextStyle) {
      this.nodeInfoTextStyle = nodeInfoTextStyle;
      return this;
    }

    public Builder withIcon(SVGResource icon) {
      this.icon = icon;
      return this;
    }

    public Builder withUserElement(Element userElement) {
      this.userElement = userElement;
      return this;
    }

    public NewNodePresentation build() {
      return new NewNodePresentation(this);
    }
  }
}
