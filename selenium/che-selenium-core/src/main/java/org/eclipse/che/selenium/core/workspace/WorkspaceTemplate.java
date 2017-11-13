/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

/**
 * Templates file names to create workspaces based upon.
 *
 * @author Dmytro Nochevnov
 */
public class WorkspaceTemplate {
  public static final String ECLIPSE_CPP_GCC = "eclipse_cpp_gcc.json";
  public static final String UBUNTU_LSP = "ubuntu_with_c_sharp_lsp.json";
  public static final String ECLIPSE_NODEJS = "eclipse_nodejs.json";
  public static final String ECLIPSE_PHP = "eclipse_php.json";
  public static final String CODENVY_UBUNTU_JDK8 = "codenvy_ubuntu_jdk8.json";
  public static final String UBUNTU = "ubuntu.json";
  public static final String DEFAULT = "default.json";

  private WorkspaceTemplate() {}
}
