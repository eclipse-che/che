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
