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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.factory.Factory;

/**
 * Convert legacy factory parameter to new the latest format
 *
 * @author Alexander Garagatyi
 */
public interface LegacyConverter {
  void convert(Factory factory) throws ApiException;
}
