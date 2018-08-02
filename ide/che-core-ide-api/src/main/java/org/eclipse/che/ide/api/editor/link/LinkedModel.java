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
package org.eclipse.che.ide.api.editor.link;

import java.util.List;

/**
 * The model for linked mode, umbrellas several {@link LinkedModelGroup}s. Once installed, the model
 * propagates any changes to a position to all its siblings in the same position group.
 *
 * @author Evgen Vidolob
 */
public interface LinkedModel {
  void setGroups(List<LinkedModelGroup> groups);

  void setEscapePosition(int offset);
}
