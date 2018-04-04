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
