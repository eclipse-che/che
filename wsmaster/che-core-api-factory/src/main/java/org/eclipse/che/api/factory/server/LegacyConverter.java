/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
