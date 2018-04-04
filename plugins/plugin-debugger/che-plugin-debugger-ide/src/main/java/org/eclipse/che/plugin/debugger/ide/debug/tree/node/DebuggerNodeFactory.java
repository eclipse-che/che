/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
