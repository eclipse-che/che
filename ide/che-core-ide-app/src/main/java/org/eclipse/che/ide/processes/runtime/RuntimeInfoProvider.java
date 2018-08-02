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
package org.eclipse.che.ide.processes.runtime;

import java.util.List;

/**
 * Provides a runtime list for the specific machine name.
 *
 * @author Vlad Zhukovskyi
 * @see ContextBasedRuntimeInfoProvider
 * @since 5.18.0
 */
public interface RuntimeInfoProvider {

  /**
   * Returns the runtime info list for the given {@code machineName}.
   *
   * @return returns list with server info or empty list. Never returns a null.
   * @throws NullPointerException when {@code machineName} is null.
   */
  List<RuntimeInfo> get(String machineName);
}
