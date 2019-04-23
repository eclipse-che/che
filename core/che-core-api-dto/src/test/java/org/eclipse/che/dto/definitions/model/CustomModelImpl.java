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
package org.eclipse.che.dto.definitions.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomModelImpl implements Model {

  @Override
  public List<? extends ModelComponent> getComponents() {
    return Collections.singletonList(() -> "custom");
  }

  @Override
  public Map<String, ? extends ModelComponent> getComponentMap() {
    return Collections.emptyMap();
  }

  @Override
  public ModelComponent getPrimary() {
    return null;
  }
}
