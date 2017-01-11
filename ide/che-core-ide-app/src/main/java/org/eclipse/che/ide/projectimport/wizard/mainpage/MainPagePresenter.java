/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.projectimport.wizard.mainpage;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.ProjectImportersServiceClient;
import org.eclipse.che.api.project.shared.dto.ProjectImporterData;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.projectimport.wizard.presenter.ImportProjectWizardView;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.NameUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter of the import project wizard's main page.
 *
 * @author Ann Shumilova
 */
public class MainPagePresenter extends AbstractWizardPage<MutableProjectConfig> implements MainPageView.ActionDelegate {

    private static final String DEFAULT_PROJECT_IMPORTER = "default-importer";

    private final MainPageView                  view;
    private final DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    private final NotificationManager           notificationManager;
    private final CoreLocalizationConstant      locale;
    private final ImportWizardRegistry          importWizardRegistry;
    private final AppContext appContext;
    private final ProjectImportersServiceClient projectImportersService;

    private ImporterSelectionListener                    importerSelectionListener;
    private ProjectImporterDescriptor                    selectedProjectImporter;
    private ImportProjectWizardView.EnterPressedDelegate enterPressedDelegate;

    @Inject
    public MainPagePresenter(ProjectImportersServiceClient projectImportersService,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory,
                             NotificationManager notificationManager,
                             CoreLocalizationConstant locale,
                             MainPageView view,
                             ImportWizardRegistry importWizardRegistry,
                             AppContext appContext) {
        super();
        this.view = view;
        this.projectImportersService = projectImportersService;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.importWizardRegistry = importWizardRegistry;
        this.appContext = appContext;

        view.setDelegate(this);
    }

    @Override
    public void init(MutableProjectConfig dataObject) {
        super.init(dataObject);
    }

    public void setEnterPressedDelegate(ImportProjectWizardView.EnterPressedDelegate enterPressedDelegate) {
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
        return selectedProjectImporter != null && projectName != null && NameUtils.checkProjectName(projectName);
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
                        Iterator<ProjectImporterDescriptor> itr = result.iterator();
                        while (itr.hasNext()) {
                            ProjectImporterDescriptor importer = itr.next();
                            if (importer.getId().equals(defaultImporterId)) {
                                Set<ProjectImporterDescriptor> importersSet = new LinkedHashSet<>();
                                importersSet.add(importer);
                                importersByCategory.put(importer.getCategory(), importersSet);
                                itr.remove();
                            }
                        }

                        ProjectImporterDescriptor defaultImporter = null;
                        for (ProjectImporterDescriptor importer : result) {
                            if (importer.isInternal() || importer.getCategory() == null
                                || importWizardRegistry.getWizardRegistrar(importer.getId()) == null) {
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
                                view.selectImporter(defaultImporter != null ? defaultImporter
                                                                            : importersByCategory
                                                            .get(importersByCategory.keySet().iterator().next())
                                                            .iterator().next());
                            }
                        }.schedule(300);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(locale.failedToImportProject(), FAIL, FLOAT_MODE);
                    }
                };

        projectImportersService.getProjectImporters(appContext.getDevMachine(), callback);
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

    public interface ImporterSelectionListener {
        /** Called when importer selected. */
        void onImporterSelected(ProjectImporterDescriptor importer);
    }
}
