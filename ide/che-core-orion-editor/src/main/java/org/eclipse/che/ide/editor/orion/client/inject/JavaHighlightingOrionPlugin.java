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
package org.eclipse.che.ide.editor.orion.client.inject;

/**
 * Plugin that fixes some Orion's highlighting issues for Java files.
 *
 * @author Artem Zatsarynnyi
 */
public class JavaHighlightingOrionPlugin implements OrionPlugin {

  @Override
  public String getRelPath() {
    return "cheJavaHighlightingPlugin/plugin.html";
  }
}
