/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.ext.client;

import com.google.gwt.i18n.client.Messages;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public interface DockerLocalizationConstant extends Messages {
    @Key("docker.preferences.title")
    String dockerPreferencesTitle();

    @Key("docker.preferences.category")
    String dockerPreferencesCategory();


    @Key("docker.add.private.registry.text")
    String dockerAddPrivateRegistryText();

    @Key("docker.add.dockerhub.account.text")
    String dockerAddDockerhubAccountText();



    @Key("docker.input.credentials.server.address.label")
    String inputCredentialsServerAddressLabel();

    @Key("docker.input.credentials.username.label")
    String inputCredentialsUsernameLabel();

    @Key("docker.input.credentials.password.label")
    String inputCredentialsPasswordLabel();

    @Key("docker.input.credentials.email.label")
    String inputCredentialsEmailLabel();

    @Key("docker.input.missed.value.of.field")
    String inputMissedValueOfField(String invalidField);


    @Key("docker.input.credentials.cancel.button.text")
    String inputCredentialsCancelButtonText();

    @Key("docker.input.credentials.save.button.text")
    String inputCredentialsSaveButtonText();

    @Key("docker.input.credentials.edit.button.text")
    String inputCredentialsEditButtonText();


    @Key("docker.add.private.registry.title")
    String addPrivateRegitryTitle();

    @Key("docker.edit.private.registry.title")
    String editPrivateRegistryTitle();

    @Key("docker.add.dockerhub.account.title")
    String addDockerhubAccountTitle();

    @Key("docker.edit.dockerhub.account.title")
    String editDockerhubAccountTitle();


    @Key("docker.remove.credentials.confirm.title")
    String removeCredentialsConfirmTitle();

    @Key("docker.remove.credentials.confirm.text.regexp")
    String removeCredentialsConfirmText(String serverAddress);
}
