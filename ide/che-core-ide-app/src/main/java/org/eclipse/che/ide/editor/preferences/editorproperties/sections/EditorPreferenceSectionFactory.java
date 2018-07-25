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
import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;

/**
 * The factory which creates instances of {@link EditorPropertiesSection}.
 *
 * @author Roman Nikitenko
 */
public interface EditorPreferenceSectionFactory {

  /**
   * Creates one of implementations of {@link EditorPropertiesSection}.
   *
   * @param title title of editor's properties section
   * @param propertiesIds IDs of properties which will be added to the section
   * @return an instance of {@link EditorPropertiesSection}
   */
  EditorPreferenceSection create(String title, List<String> propertiesIds);
}
