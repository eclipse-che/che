/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server;

import java.util.List;
import java.util.Set;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.project.shared.dto.ItemReference;

/** Converts file system items to DTOs */
public interface FsDtoConverter {

  ItemReference asDto(String wsPath) throws NotFoundException;

  List<ItemReference> asDto(List<String> wsPaths) throws NotFoundException;

  Set<ItemReference> asDto(Set<String> wsPaths) throws NotFoundException;
}
