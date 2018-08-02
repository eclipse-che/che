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
