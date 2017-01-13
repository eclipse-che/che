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
package org.eclipse.che.plugin.svn.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionClientServiceImpl;
import org.eclipse.che.plugin.svn.ide.commit.CommitView;
import org.eclipse.che.plugin.svn.ide.commit.CommitViewImpl;
import org.eclipse.che.plugin.svn.ide.commit.diff.DiffViewerView;
import org.eclipse.che.plugin.svn.ide.commit.diff.DiffViewerViewImpl;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsole;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsolePresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleView;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleViewImpl;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialog;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogFactory;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogPresenter;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogView;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogViewImpl;
import org.eclipse.che.plugin.svn.ide.credentialsdialog.SubversionCredentialsDialogImpl;
import org.eclipse.che.plugin.svn.ide.export.ExportView;
import org.eclipse.che.plugin.svn.ide.export.ExportViewImpl;
import org.eclipse.che.plugin.svn.ide.importer.SubversionImportWizardRegistrar;
import org.eclipse.che.plugin.svn.ide.importer.SubversionProjectImporterView;
import org.eclipse.che.plugin.svn.ide.importer.SubversionProjectImporterViewImpl;
import org.eclipse.che.plugin.svn.ide.log.ShowLogsView;
import org.eclipse.che.plugin.svn.ide.log.ShowLogsViewImpl;
import org.eclipse.che.plugin.svn.ide.property.PropertyEditorView;
import org.eclipse.che.plugin.svn.ide.property.PropertyEditorViewImpl;
import org.eclipse.che.plugin.svn.ide.resolve.ResolveView;
import org.eclipse.che.plugin.svn.ide.resolve.ResolveViewImpl;
import org.eclipse.che.plugin.svn.ide.sw.LocationSelectorView;
import org.eclipse.che.plugin.svn.ide.sw.LocationSelectorViewImpl;
import org.eclipse.che.plugin.svn.ide.sw.SwitchView;
import org.eclipse.che.plugin.svn.ide.sw.SwitchViewImpl;
import org.eclipse.che.plugin.svn.ide.update.UpdateToRevisionView;
import org.eclipse.che.plugin.svn.ide.update.UpdateToRevisionViewImpl;

/**
 * Subversion Gin module.
 *
 * @author Jeremy Whitlock
 */
@ExtensionGinModule
public class SubversionGinModule extends AbstractGinModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(SubversionClientService.class).to(SubversionClientServiceImpl.class).in(Singleton.class);

        GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class).addBinding()
                      .to(SubversionImportWizardRegistrar.class);

        bind(SubversionProjectImporterView.class).to(SubversionProjectImporterViewImpl.class).in(Singleton.class);

        bind(SubversionOutputConsoleView.class).to(SubversionOutputConsoleViewImpl.class);
        bind(UpdateToRevisionView.class).to(UpdateToRevisionViewImpl.class).in(Singleton.class);
        bind(SwitchView.class).to(SwitchViewImpl.class).in(Singleton.class);
        bind(LocationSelectorView.class).to(LocationSelectorViewImpl.class).in(Singleton.class);
        bind(ResolveView.class).to(ResolveViewImpl.class).in(Singleton.class);
        bind(ExportView.class).to(ExportViewImpl.class).in(Singleton.class);
        bind(ShowLogsView.class).to(ShowLogsViewImpl.class).in(Singleton.class);
        bind(PropertyEditorView.class).to(PropertyEditorViewImpl.class).in(Singleton.class);

        bind(CommitView.class).to(CommitViewImpl.class).in(Singleton.class);
        bind(DiffViewerView.class).to(DiffViewerViewImpl.class).in(Singleton.class);

        bind(SubversionCredentialsDialog.class).to(SubversionCredentialsDialogImpl.class);

        install(new GinFactoryModuleBuilder().implement(ChoiceDialog.class, ChoiceDialogPresenter.class)
                                             .build(ChoiceDialogFactory.class));
        bind(ChoiceDialogView.class).to(ChoiceDialogViewImpl.class);

        install(new GinFactoryModuleBuilder().implement(SubversionOutputConsole.class, SubversionOutputConsolePresenter.class)
                                             .build(SubversionOutputConsoleFactory.class));
    }

}
