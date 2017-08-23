/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jface.viewers;

/** @author Evgen Vidolob */
public class DefaultStyler extends StyledString.Styler {

  private String className;

  /** @param className */
  public DefaultStyler(String className) {
    super();
    this.className = className;
  }

  @Override
  public String applyStyles(String text) {
    StringBuilder b = new StringBuilder();
    b.append("<span ").append("class=\"").append(className).append("\">");
    b.append(text).append("</span>");
    return b.toString();
  }
}
