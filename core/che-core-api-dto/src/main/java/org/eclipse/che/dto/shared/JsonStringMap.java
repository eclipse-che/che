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
package org.eclipse.che.dto.shared;

import java.util.Map;
import org.eclipse.che.dto.server.JsonSerializable;

/** Abstraction for map of JSON values. */
public interface JsonStringMap<T> extends Map<String, T>, JsonSerializable {}
