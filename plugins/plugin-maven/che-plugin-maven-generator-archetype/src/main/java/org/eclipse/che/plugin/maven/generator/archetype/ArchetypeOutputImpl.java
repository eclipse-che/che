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
package org.eclipse.che.plugin.maven.generator.archetype;

import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;

/**
 * Describes information about output object of maven archetype project generation.
 *
 * @author Vitalii Parfonov
 */
public class ArchetypeOutputImpl implements ArchetypeOutput {
  private String output;
  private State state;

  public ArchetypeOutputImpl(String output, State state) {
    this.output = output;
    this.state = state;
  }

  @Override
  public String getOutput() {
    return output;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void setOutput(String output) {
    this.output = output;
  }

  @Override
  public void setState(State state) {
    this.state = state;
  }
}
