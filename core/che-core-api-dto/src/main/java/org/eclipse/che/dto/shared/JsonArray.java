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

import java.util.List;
import org.eclipse.che.dto.server.JsonSerializable;

/** Abstraction for list of JSON values. */
public interface JsonArray<T> extends List<T>, JsonSerializable {}
