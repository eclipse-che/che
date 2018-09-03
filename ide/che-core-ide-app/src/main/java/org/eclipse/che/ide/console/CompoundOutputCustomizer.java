/*
 * Copyright (c) 2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Compound customizer allows to combine several different customizers. A text provided will be
 * treated as customize if at least one of nested customizers detects it as customizable
 *
 * @author Victor Rubezhny
 */
public class CompoundOutputCustomizer implements OutputCustomizer {
  private OutputCustomizer[] customizers = null;

  /**
   * Constructs the compound customizer object from a number of specified output customizers
   *
   * @param customizers
   */
  public CompoundOutputCustomizer(OutputCustomizer... customizers) {
    this.customizers = customizers;
  }

  @Override
  public boolean canCustomize(String text) {
    return Stream.of(customizers).anyMatch(customizer -> customizer.canCustomize(text));
  }

  @Override
  public String customize(String text) {
    Optional<OutputCustomizer> optional =
        Stream.of(customizers).filter(customizer -> customizer.canCustomize(text)).findFirst();
    return optional.isPresent() ? optional.get().customize(text) : text;
  }
}
