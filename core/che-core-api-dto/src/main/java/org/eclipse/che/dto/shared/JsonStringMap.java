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
package org.eclipse.che.dto.shared;

import java.util.Map;
import org.eclipse.che.dto.server.JsonSerializable;

/** Abstraction for map of JSON values. */
public interface JsonStringMap<T> extends Map<String, T>, JsonSerializable {}
