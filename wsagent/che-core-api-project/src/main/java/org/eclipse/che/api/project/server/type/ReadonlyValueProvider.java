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
package org.eclipse.che.api.project.server.type;

import java.util.List;

/**
 * Value provider to read the values. Initializing
 *
 * @author gazarenkov
 */
public abstract class ReadonlyValueProvider implements ValueProvider {

  @Override
  public final void setValues(String attributeName, List<String> values)
      throws ValueStorageException {
    throw new ValueStorageException("Value Provider is read only");
  }

  @Override
  public final boolean isSettable() {
    return false;
  }
}
