/*
 * Copyright (c) 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;

/**
 * Compound renderer allows to combine several different renderers. A text provided will be treated
 * as 'can be rendered' if at least one of nested renderers detects it as 'can be rendered'.
 *
 * @author Victor Rubezhny
 */
public class CompoundOutputRenderer implements OutputConsoleRenderer {
  private String name;
  private OutputConsoleRenderer[] renderers = null;

  /**
   * Constructs the compound renderer object from a number of specified output renderers
   *
   * @param renderers
   */
  public CompoundOutputRenderer(OutputConsoleRenderer... renderers) {
    this(null, renderers);
  }

  /**
   * Constructs the compound renderer object from a number of specified output renderers
   *
   * @param name
   * @param renderers
   */
  public CompoundOutputRenderer(String name, OutputConsoleRenderer... renderers) {
    this.name = name;
    this.renderers = renderers;
  }

  @Override
  public boolean canRender(String text) {
    return Stream.of(renderers).anyMatch(renderer -> renderer.canRender(text));
  }

  @Override
  public String render(String text) {
    Optional<OutputConsoleRenderer> optional =
        Stream.of(renderers).filter(renderer -> renderer.canRender(text)).findFirst();
    return optional.isPresent() ? optional.get().render(text) : text;
  }

  @Override
  public String getName() {
    return name;
  }
}
