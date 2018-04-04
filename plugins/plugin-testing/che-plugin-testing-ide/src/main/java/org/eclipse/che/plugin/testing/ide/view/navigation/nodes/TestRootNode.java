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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestState;

/** */
public class TestRootNode extends TestStateNode {

  private final TestRootState testState;

  @Inject
  public TestRootNode(
      PromiseProvider promiseProvider,
      TestResources testResources,
      @Assisted TestRootState testState) {
    super(promiseProvider, testResources, testState);
    this.testState = testState;
  }

  @Override
  public String getName() {
    return testState.getPresentation() == null ? testState.getName() : testState.getPresentation();
  }

  @Override
  public TestState getTestState() {
    return testState;
  }
}
