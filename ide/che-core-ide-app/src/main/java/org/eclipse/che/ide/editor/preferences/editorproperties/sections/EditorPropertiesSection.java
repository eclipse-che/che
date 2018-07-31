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
package org.eclipse.che.ide.editor.preferences.editorproperties.sections;

import java.util.List;

/**
 * The interface provides methods to get info about editor's properties section.
 *
 * @author Roman Nikitenko
 */
public interface EditorPropertiesSection {

  /** Returns IDs of properties which the section contains */
  public List<String> getProperties();

  /** Returns the title of editor's properties section */
  public String getSectionTitle();
}
