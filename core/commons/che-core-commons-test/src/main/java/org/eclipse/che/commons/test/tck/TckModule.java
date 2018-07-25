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
package org.eclipse.che.commons.test.tck;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.ServiceLoader;
import org.testng.ITestContext;

/**
 * Abstract class for those Guice {@link Module modules} which provide TCK tests components, which
 * will be injected directly into the test class.
 *
 * <p>The {@link ServiceLoader} mechanism is used for loading such modules and for injecting them
 * later. So each module which is TCK module must provide the implementations list(as described by
 * {@code ServiceLoader} mechanism) in the file named
 * <i>org.eclipse.che.commons.test.tck.TckModule</i> usually under
 * <i>test/resources/META-INF/services</i> directory, then the {@link TckListener} will recognise
 * and load it.
 *
 * @author Yevhenii Voevodin
 * @see TckListener
 */
public abstract class TckModule extends AbstractModule {

  /**
   * It is guaranteed that this field is always present and can be reused by implementation, it will
   * be set by {@link TckListener} immediately after module implementation is loaded by {@link
   * ServiceLoader}.
   */
  private ITestContext testContext;

  /** Returns the {@link ITestContext context} of currently executing test suite. */
  protected ITestContext getTestContext() {
    return testContext;
  }

  /**
   * Sets the context of currently executing test suite. This method designed to be used by {@link
   * TckListener} for setting the context before installing modules.
   */
  void setTestContext(ITestContext testContext) {
    this.testContext = testContext;
  }
}
