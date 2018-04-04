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
package org.eclipse.che.plugin.testing.ide.view.navigation.factory;

import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestRootNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestStateNode;

/**
 * Factory for providing test navigation tree nodes.
 *
 * @author Mirage Abeysekara
 */
public interface TestResultNodeFactory {

  TestStateNode create(TestState testState);

  TestRootNode create(TestRootState testRootState);
}
