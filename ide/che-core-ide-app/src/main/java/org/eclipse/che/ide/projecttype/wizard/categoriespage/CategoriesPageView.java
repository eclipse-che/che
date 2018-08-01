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
package org.eclipse.che.ide.projecttype.wizard.categoriespage;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

/** @author Evgen Vidolob */
@ImplementedBy(CategoriesPageViewImpl.class)
public interface CategoriesPageView extends View<CategoriesPageView.ActionDelegate> {

  void selectProjectType(String projectTypeId);

  void setCategories(
      Map<String, Set<ProjectTypeDto>> typesByCategory,
      Map<String, Set<ProjectTemplateDescriptor>> templatesByCategory);

  void updateCategories(boolean includeTemplates);

  void reset();

  void setConfigOptions(List<String> options);

  void setName(String name);

  void setDescription(String description);

  void removeNameError();

  void showNameError();

  /** Sets focus to the Select Path button. */
  void focusSelectPathButton();

  void focusName();

  void setProjectTypes(List<ProjectTypeDto> availableProjectTypes);

  void setNameFieldReadOnly(boolean readOnly);

  void setParentPath(Path path);

  interface ActionDelegate {
    void projectNameChanged(String name);

    void projectDescriptionChanged(String projectDescriptionValue);

    void projectTemplateSelected(ProjectTemplateDescriptor template);

    void projectTypeSelected(ProjectTypeDto typeDescriptor);

    void selectPathClicked();
  }
}
