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
import com.google.inject.Provider;
import java.lang.reflect.Field;

/**
 * Injector for custom annotation {@link InjectTestWorkspace}.
 *
 * @author Anatolii Bazko
 */
public class TestWorkspaceInjector<T> extends AbstractTestWorkspaceInjector<T> {

  public TestWorkspaceInjector(
      Field field, InjectTestWorkspace injectTestWorkspace, Provider<Injector> injectorProvider) {
    super(field, injectTestWorkspace, injectorProvider);
    testWorkspaceProvider = injector.getInstance(TestWorkspaceProvider.class);
  }
}
