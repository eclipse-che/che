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
package org.eclipse.che.plugin.factory.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * @author vzhukovskii@codenvy.com
 */
public interface FactoryLocalizationConstant extends Messages {

    /* ************************************************************************************************************
     *
     * Accept
     *
     * ************************************************************************************************************/

    @Key("projects.import.configuring.cloning")
    String cloningSource();

    @Key("project.import.configuring.cloning")
    String cloningSource(String projectName);

    @Key("project.import.configured.cloned")
    String clonedSource(String projectName);

    @Key("project.import.cloned.with.checkout")
    String clonedSourceWithCheckout(String projectName, String repoName, String ref, String branch);

    @Key("project.import.cloned.with.checkout.start.point")
    String clonedWithCheckoutOnStartPoint(String projectName, String repoName, String startPoint, String branch);

    @Key("project.import.cloning.failed.without.start.point")
    String cloningSourceWithCheckoutFailed(String branch, String repoName);

    @Key("project.import.cloning.failed.with.start.point")
    String cloningSourceCheckoutFailed(String project, String branch);

    @Key("project.import.cloning.failed.title")
    String cloningSourceFailedTitle(String projectName);

    @Key("project.import.configuring.failed")
    String configuringSourceFailed(String projectName);

    @Key("project.import.ssh.key.upload.failed.title")
    String cloningSourceSshKeyUploadFailedTitle();

    @Key("project.import.ssh.key.upload.failed.text")
    String cloningSourcesSshKeyUploadFailedText();

    @Key("project.already.imported")
    String projectAlreadyImported(String projectName);


    @Key("oauth.failed.to.get.authenticator.title")
    String oauthFailedToGetAuthenticatorTitle();

    @Key("oauth.failed.to.get.authenticator.text")
    String oauthFailedToGetAuthenticatorText();

    @Key("message.ssh.not.found.text")
    String acceptSshNotFoundText();

    /* ***************************************************************************************************************
     *
     * Importing from Config File
     *
     * **************************************************************************************************************/

    @Key("import.config.view.name")
    String importFromConfigurationName();

    @Key("import.config.view.description")
    String importFromConfigurationDescription();

    @Key("import.config.view.title")
    String importFromConfigurationTitle();

    @Key("import.config.form.prompt")
    String configFileTitle();

    @Key("import.config.form.button.cancel")
    String cancelButton();

    @Key("import.config.form.button.import")
    String importButton();

    /* ***************************************************************************************************************
     *
     * Exporting Config File
     *
     * **************************************************************************************************************/

    @Key("export.config.view.name")
    String exportConfigName();

    @Key("export.config.view.description")
    String exportConfigDescription();

    @Key("export.config.error.message")
    String exportConfigErrorMessage();

    @Key("export.config.dialog.not.under.vcs.title")
    String exportConfigDialogNotUnderVcsTitle();

    @Key("export.config.dialog.not.under.vcs.text")
    String exportConfigDialogNotUnderVcsText();

    /* ***************************************************************************************************************
     *
     * Welcome
     *
     * **************************************************************************************************************/

    @Key("welcome.preferences.title")
    String welcomePreferencesTitle();

    /* ***************************************************************************************************************
     *
     * Create factory
     *
     * **************************************************************************************************************/

    @Key("create.factory.form.title")
    String createFactoryTitle();

    @Key("create.factory.action.title")
    String createFactoryActionTitle();

    @Key("create.factory.label.name")
    String createFactoryName();

    @Key("create.factory.button.create")
    String createFactoryButton();

    @Key("create.factory.button.close")
    String createFactoryButtonClose();

    @Key("create.factory.label.link")
    String createFactoryLink();

    @Key("create.factory.already.exist")
    String createFactoryAlreadyExist();

    @Key("create.factory.unable.create.from.current.workspace")
    String createFactoryFromCurrentWorkspaceFailed();

    @Key("create.factory.configure.button.tooltip")
    String createFactoryConfigureTooltip();

    @Key("create.factory.launch.button.tooltip")
    String createFactoryLaunchTooltip();
}
