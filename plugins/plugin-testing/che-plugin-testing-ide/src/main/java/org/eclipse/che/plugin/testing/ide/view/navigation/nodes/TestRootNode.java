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
