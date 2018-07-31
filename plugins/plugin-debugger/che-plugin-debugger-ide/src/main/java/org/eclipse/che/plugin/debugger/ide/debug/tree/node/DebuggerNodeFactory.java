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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;

/**
 * Factory for creation debugger tree node elements.
 *
 * @author Oleksandr Andriienko
 */
public interface DebuggerNodeFactory {

  VariableNode createVariableNode(@Assisted Variable variable);

  WatchExpressionNode createExpressionNode(@Assisted WatchExpression expression);
}
