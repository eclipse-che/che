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
package org.eclipse.che.commons.test.tck;

/**
 * This class is designed to clean up resources after tck test.
 *
 * <p>Implementation should be defined in {@link TckModule}. It can be defined common for all tests
 * or test specific by using @Named annotation. Cleaning of resources is invoked after finish of all
 * tests methods from one test suite that should contains one and only one tck test class.
 *
 * <p>The usage example:
 *
 * <pre>
 * class MyTckModule extends TckModule {
 *     public void configure() {
 *         bind(TckResourcesCleaner.class).to(...);
 *         bind(TckResourcesCleaner.class).annotatedWith(Names.named(SomeTest.class.getName())).to(...);
 *     }
 * }
 * </pre>
 *
 * @author Sergii Leschenko
 * @see TckListener
 */
public interface TckResourcesCleaner {
  /**
   * Clean up resources.
   *
   * <p>Note: it is invoked after finish of all methods from one test suite.
   */
  void clean();
}
