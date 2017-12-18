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
