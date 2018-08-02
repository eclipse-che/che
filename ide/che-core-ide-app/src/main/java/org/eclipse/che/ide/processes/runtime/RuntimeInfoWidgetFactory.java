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

import com.google.gwt.user.client.ui.Widget;
import java.util.List;

/**
 * Creates a widget to display information about given {@code machineName} and {@code runtimeList}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
public interface RuntimeInfoWidgetFactory {

  /**
   * Creates a widget to display runtime information.
   *
   * @param machineName machine name to display
   * @param runtimeList runtime information
   * @return instance of widget with displayed information about runtime
   */
  Widget create(String machineName, List<RuntimeInfo> runtimeList);
}
