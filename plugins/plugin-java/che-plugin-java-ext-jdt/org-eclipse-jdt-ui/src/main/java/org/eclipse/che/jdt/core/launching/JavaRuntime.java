/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

import org.eclipse.che.jdt.core.launching.environments.ExecutionEnvironment;
import org.eclipse.che.jdt.core.launching.environments.IExecutionEnvironment;

/** @author Evgen Vidolob */
public class JavaRuntime {

  private static final IExecutionEnvironment environment = new ExecutionEnvironment();

  public static IExecutionEnvironment getEnvironment(String environmentId) {
    return environment;
  }
}
