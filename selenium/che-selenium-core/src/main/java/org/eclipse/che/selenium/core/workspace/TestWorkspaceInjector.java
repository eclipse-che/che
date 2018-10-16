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
package org.eclipse.che.selenium.core.workspace;

import com.google.inject.Injector;
import java.lang.reflect.Field;

/**
 * Injector for custom annotation {@link InjectTestWorkspace}.
 *
 * @author Anatolii Bazko
 */
public class TestWorkspaceInjector<T> extends AbstractTestWorkspaceInjector<T> {

  public TestWorkspaceInjector(
      Field field, InjectTestWorkspace injectTestWorkspace, Injector injector) {
    super(field, injectTestWorkspace, injector);
  }

  @Override
  protected TestWorkspaceProvider getTestWorkspaceProvider() {
    return injector.getInstance(TestWorkspaceProvider.class);
  }
}
