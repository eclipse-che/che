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
package org.eclipse.che.ide.ext.java.shared.dto.model;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a stand-alone instance or class (static) initializer in a type.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Initializer extends Member {
  // interface used as a marker: defines no member
}
