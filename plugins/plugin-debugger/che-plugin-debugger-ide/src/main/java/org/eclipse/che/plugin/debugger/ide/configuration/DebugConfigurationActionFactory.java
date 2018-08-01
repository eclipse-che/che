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
package org.eclipse.che.plugin.debugger.ide.configuration;

import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.debug.DebugConfiguration;

/**
 * Factory for creating instances of {@link DebugConfigurationAction}.
 *
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationActionFactory {
  BaseAction createAction(DebugConfiguration configuration);
}
