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
package org.eclipse.che.plugin.jdb.server.model;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;

/**
 * @author andrew00x
 * @author Anatolii Bazko
 */
public final class JdbNullValue implements SimpleValue {
  @Override
  public String getString() {
    return "null";
  }

  @Override
  public List<Variable> getVariables() {
    return Collections.emptyList();
  }
}
