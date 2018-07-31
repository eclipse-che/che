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
package org.eclipse.che.ide.projectimport.wizard.mainpage;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.project.shared.dto.ProjectImporterData;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.projectimport.wizard.presenter.ImportProjectWizardView;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Presenter of the import project wizard's main page.
 *
 * @author Ann Shumilova
 */
public class MainPagePresenter extends AbstractWizardPage<MutableProjectConfig>
    implements MainPageView.ActionDelegate {

  private static final String DEFAULT_PROJECT_IMPORTER = "default-importer";

  private final MainPageView view;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant locale;
  private final ImportWizardRegistry importWizardRegistry;
  private final AppContext appContext;
  private final AsyncRequestFactory asyncRequestFactory;

  private ImporterSelectionListener importerSelectionListener;
  private ProjectImporterDescriptor selectedProjectImporter;
  private ImportProjectWizardView.EnterPressedDelegate enterPressedDelegate;

  @Inject
  public MainPagePresenter(
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      MainPageView view,
      ImportWizardRegistry importWizardRegistry,
      AppContext appContext,
      AsyncRequestFactory asyncRequestFactory) {
    super();
    this.view = view;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.importWizardRegistry = importWizardRegistry;
    this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;

    view.setDelegate(this);
  }

  @Override
  public void init(MutableProjectConfig dataObject) {
    super.init(dataObject);
  }

  public void setEnterPressedDelegate(
      ImportProjectWizardView.EnterPressedDelegate enterPressedDelegate) {
    this.enterPressedDelegate = enterPressedDelegate;
  }

  /** {@inheritDoc} */
  @Override
  public void projectImporterSelected(ProjectImporterDescriptor importer) {
    selectedProjectImporter = importer;
    view.setImporterDescription(importer.getDescription());

    if (importerSelectionListener != null) {
      importerSelectionListener.onImporterSelected(importer);
    }

    updateDelegate.updateControls();
  }

  public AcceptsOneWidget getImporterPanel() {
    return view.getImporterPanel();
  }

  @Override
  public boolean isCompleted() {
    final String projectName = dataObject.getName();
    return selectedProjectImporter != null
        && projectName != null
        && NameUtils.checkProjectName(projectName);
  }

  @Override
  public void go(final AcceptsOneWidget container) {
    selectedProjectImporter = null;

    view.reset();
    container.setWidget(view);

    loadImporters();
  }

  private void loadImporters() {
    final Map<String, Set<ProjectImporterDescriptor>> importersByCategory = new LinkedHashMap<>();

    final Unmarshallable<ProjectImporterData> unmarshaller =
        dtoUnmarshallerFactory.newUnmarshaller(ProjectImporterData.class);

    AsyncRequestCallback<ProjectImporterData> callback =
        new AsyncRequestCallback<ProjectImporterData>(unmarshaller) {
          @Override
          protected void onSuccess(ProjectImporterData data) {
            List<ProjectImporterDescriptor> result = data.getImporters();
            String defaultImporterId = data.getConfiguration().get(DEFAULT_PROJECT_IMPORTER);
            result.sort(getProjectImporterComparator(defaultImporterId));

            ProjectImporterDescriptor defaultImporter = null;
            for (ProjectImporterDescriptor importer : result) {
              if (importer.isInternal()
                  || importer.getCategory() == null
                  || !importWizardRegistry.getWizardRegistrar(importer.getId()).isPresent()) {
                continue;
              }

              if (importersByCategory.containsKey(importer.getCategory())) {
                importersByCategory.get(importer.getCategory()).add(importer);
              } else {
                Set<ProjectImporterDescriptor> importersSet = new LinkedHashSet<>();
                importersSet.add(importer);
                importersByCategory.put(importer.getCategory(), importersSet);
              }

              if (importer.getId().equals(defaultImporterId)) {
                defaultImporter = importer;
              }
            }

            setImporters(defaultImporter);
          }

          private void setImporters(final ProjectImporterDescriptor defaultImporter) {
            new Timer() {
              @Override
              public void run() {
                view.setImporters(importersByCategory);
                view.selectImporter(
                    defaultImporter != null
                        ? defaultImporter
                        : importersByCategory
                            .get(importersByCategory.keySet().iterator().next())
                            .iterator()
                            .next());
              }
            }.schedule(300);
          }

          @Override
          protected void onFailure(Throwable exception) {
            notificationManager.notify(locale.failedToImportProject(), FAIL, FLOAT_MODE);
          }
        };

    fetchProjectImporters(callback);
  }

  private Comparator<ProjectImporterDescriptor> getProjectImporterComparator(
      String defaultImporterId) {
    return (o1, o2) -> {
      if (o1.getId().equals(defaultImporterId)) {
        return -1;
      }

      if (o2.getId().equals(defaultImporterId)) {
        return 1;
      }

      return 0;
    };
  }

  /** {@inheritDoc} */
  @Override
  public void onEnterClicked() {
    if (enterPressedDelegate != null) {
      enterPressedDelegate.onEnterKeyPressed();
    }
  }

  public void setImporterSelectionListener(ImporterSelectionListener listener) {
    importerSelectionListener = listener;
  }

  /** Fetch project importers from the server. */
  private void fetchProjectImporters(AsyncRequestCallback<ProjectImporterData> callback) {
    asyncRequestFactory
        .createGetRequest(appContext.getWsAgentServerApiEndpoint() + "/project-importers")
        .header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON)
        .send(callback);
  }

  public interface ImporterSelectionListener {
    /** Called when importer selected. */
    void onImporterSelected(ProjectImporterDescriptor importer);
  }
}
