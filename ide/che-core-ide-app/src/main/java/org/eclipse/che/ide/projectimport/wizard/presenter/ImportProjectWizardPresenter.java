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
package org.eclipse.che.ide.projectimport.wizard.presenter;

import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.api.wizard.WizardPage;

import org.eclipse.che.ide.projectimport.wizard.ImportWizardFactory;
import org.eclipse.che.ide.projectimport.wizard.ImportWizard;
import org.eclipse.che.ide.projectimport.wizard.mainpage.MainPagePresenter;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presenter for import project wizard dialog.
 *
 * @author Ann Shumilova
 * @author Sergii Leschenko
 */
public class ImportProjectWizardPresenter implements Wizard.UpdateDelegate,
                                                     ImportProjectWizardView.ActionDelegate,
                                                     ImportProjectWizardView.EnterPressedDelegate,
                                                     MainPagePresenter.ImporterSelectionListener {

    private final DialogFactory                                dialogFactory;
    private final ImportWizardFactory                          importWizardFactory;
    private final ImportProjectWizardView                      view;
    private final Provider<MainPagePresenter>                  mainPageProvider;
    private final CoreLocalizationConstant                     locale;
    private final ImportWizardRegistry                         wizardRegistry;
    private final Map<ProjectImporterDescriptor, ImportWizard> wizardsCache;

    private MainPagePresenter mainPage;
    private WizardPage        currentPage;
    private ImportWizard      wizard;

    @Inject
    public ImportProjectWizardPresenter(ImportProjectWizardView view,
                                        MainPagePresenter mainPage,
                                        CoreLocalizationConstant locale,
                                        Provider<MainPagePresenter> mainPageProvider,
                                        ImportWizardRegistry wizardRegistry,
                                        DialogFactory dialogFactory,
                                        ImportWizardFactory importWizardFactory) {
        this.view = view;
        this.locale = locale;
        this.wizardRegistry = wizardRegistry;
        this.mainPage = mainPage;
        this.mainPageProvider = mainPageProvider;
        this.dialogFactory = dialogFactory;
        this.importWizardFactory = importWizardFactory;
        wizardsCache = new HashMap<>();
        view.setDelegate(this);
    }

    @Override
    public void onNextClicked() {
        final WizardPage nextPage = wizard.navigateToNext();
        if (nextPage != null) {
            showPage(nextPage);
        }
    }

    @Override
    public void onBackClicked() {
        final WizardPage prevPage = wizard.navigateToPrevious();
        if (prevPage != null) {
            showPage(prevPage);
        }
    }

    @Override
    public void onImportClicked() {
        view.setLoaderVisibility(true);
        wizard.complete(new Wizard.CompleteCallback() {
            @Override
            public void onCompleted() {
                view.close();
            }

            @Override
            public void onFailure(Throwable e) {
                view.setLoaderVisibility(false);
                dialogFactory.createMessageDialog(locale.importProjectViewTitle(), e.getMessage(), null).show();
            }
        });
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void updateControls() {
        view.setBackButtonEnabled(false);
        view.setNextButtonEnabled(wizard.hasNext() && currentPage.isCompleted());
        view.setImportButtonEnabled(wizard.canComplete());
    }

    public void show() {
        resetState();

        wizard = createDefaultWizard();
        final WizardPage<MutableProjectConfig> firstPage = wizard.navigateToFirst();
        if (firstPage != null) {
            showPage(firstPage);
            view.showDialog();
        }
    }

    private void resetState() {
        wizardsCache.clear();
        mainPage = mainPageProvider.get();
        mainPage.setUpdateDelegate(this);
        mainPage.setImporterSelectionListener(this);
        mainPage.setEnterPressedDelegate(this);
    }

    /** Creates or returns import wizard for the specified importer. */
    private ImportWizard getWizardForImporter(@NotNull ProjectImporterDescriptor importer) {
        if (wizardsCache.containsKey(importer)) {
            return wizardsCache.get(importer);
        }

        final ImportWizardRegistrar wizardRegistrar = wizardRegistry.getWizardRegistrar(importer.getId());
        if (wizardRegistrar == null) {
            // should never occur
            throw new IllegalStateException("WizardRegistrar for the importer " + importer.getId() + " isn't registered.");
        }

        List<Provider<? extends WizardPage<MutableProjectConfig>>> pageProviders = wizardRegistrar.getWizardPages();
        final ImportWizard importWizard = createDefaultWizard();
        for (Provider<? extends WizardPage<MutableProjectConfig>> provider : pageProviders) {
            importWizard.addPage(provider.get(), 1, false);
        }

        wizardsCache.put(importer, importWizard);
        return importWizard;
    }

    /** Creates and returns 'default' project wizard with pre-defined pages only. */
    private ImportWizard createDefaultWizard() {
        final MutableProjectConfig dataObject = new MutableProjectConfig();

        final ImportWizard importWizard = importWizardFactory.newWizard(dataObject);
        importWizard.setUpdateDelegate(this);
        // add pre-defined first page
        importWizard.addPage(mainPage);
        return importWizard;
    }

    private void showPage(@NotNull WizardPage wizardPage) {
        currentPage = wizardPage;
        updateControls();
        view.showPage(currentPage);
    }

    @Override
    public void onEnterKeyPressed() {
        if (wizard.hasNext() && currentPage.isCompleted()) {
            onNextClicked();
        } else if (wizard.canComplete()) {
            onImportClicked();
        }
    }

    @Override
    public void onImporterSelected(ProjectImporterDescriptor importer) {
        final MutableProjectConfig prevData = wizard.getDataObject();
        wizard = getWizardForImporter(importer);
        final MutableProjectConfig dataObject = wizard.getDataObject();

        dataObject.getSource().setType(importer.getId());

        // some values should be shared between wizards for different project types
        dataObject.setName(prevData.getName());
        dataObject.setDescription(prevData.getDescription());

        WizardPage<MutableProjectConfig> firstPage = wizard.navigateToFirst();
        if (firstPage != null) {
            firstPage.init(dataObject);
        }

        WizardPage<MutableProjectConfig> importerPage = wizard.navigateToNext();
        importerPage.go(mainPage.getImporterPanel());
    }
}
