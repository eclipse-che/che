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
package org.eclipse.che.providers;

import com.google.common.annotations.Beta;
import com.google.inject.Provider;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Provider that can create instance of some object by class name.
 *
 * @author Evgen Vidolob
 */
@Beta
public interface DynaProvider {

  /**
   * Get provider for class name.
   *
   * @param className the class name: {@link Class#getName()}
   * @param <T> the type
   * @return the provider for class
   */
  @Nullable
  <T> Provider<T> getProvider(String className);
}
