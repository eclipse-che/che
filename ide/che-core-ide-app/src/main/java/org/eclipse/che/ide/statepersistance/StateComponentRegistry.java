/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.statepersistance;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.statepersistance.StateComponent;

@Singleton
class StateComponentRegistry {

  private final Set<StateComponent> persistenceComponents;

  @Inject
  StateComponentRegistry(Set<StateComponent> persistenceComponents) {
    this.persistenceComponents = persistenceComponents;
  }

  /** Returns list of persistence state components sorted by execution priority. */
  List<StateComponent> getComponents() {
    return persistenceComponents
        .stream()
        .sorted(comparingInt(StateComponent::getPriority).reversed())
        .collect(toList());
  }

  Optional<StateComponent> getComponentById(String id) {
    return persistenceComponents
        .stream()
        .filter(component -> component.getId().equals(id))
        .findAny();
  }
}
