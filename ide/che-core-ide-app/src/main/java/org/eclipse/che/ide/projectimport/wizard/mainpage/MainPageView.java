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
package org.eclipse.che.ide.projectimport.wizard.mainpage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.ImplementedBy;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View of the import project wizard's main page.
 *
 * @author Ann Shumilova
 */
@ImplementedBy(MainPageViewImpl.class)
public interface MainPageView extends View<MainPageView.ActionDelegate> {

  /**
   * Set project's importers.
   *
   * @param categories
   */
  void setImporters(Map<String, Set<ProjectImporterDescriptor>> categories);

  AcceptsOneWidget getImporterPanel();

  /** Reset the page. */
  void reset();

  /**
   * Select importer in the list.
   *
   * @param importer importer to select
   */
  void selectImporter(ProjectImporterDescriptor importer);

  void setImporterDescription(@NotNull String text);

  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having selected the importer. */
    void projectImporterSelected(ProjectImporterDescriptor importer);

    /** Performs any actions appropriate in response to the user having clicked the Enter key. */
    void onEnterClicked();
  }
}
