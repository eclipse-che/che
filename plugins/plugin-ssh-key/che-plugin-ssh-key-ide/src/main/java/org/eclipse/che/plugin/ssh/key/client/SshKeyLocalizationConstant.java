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
package org.eclipse.che.plugin.ssh.key.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Evgen Vidolob
 */
public interface SshKeyLocalizationConstant extends Messages {
    @Key("cancelButton")
    String cancelButton();

    @Key("uploadButton")
    String uploadButton();

    @Key("hostFieldTitle")
    String hostFieldTitle();

    @Key("fileNameFieldTitle")
    String fileNameFieldTitle();

    @Key("generate.sshKey.title")
    String generateSshKeyTitle();

    @Key("generate.sshKey.hostname")
    String generateSshKeyHostname();

    @Key("uploadSshKeyViewTitle")
    String uploadSshKeyViewTitle();

    @Key("host.validation.error")
    String hostValidationError();

    @Key("key.manager.uploadButton")
    String managerUploadButton();

    @Key("key.manager.generateButton")
    String managerGenerateButton();

    @Key("key.manager.title")
    String sshManagerTitle();

    @Key("key.manager.category")
    String sshManagerCategory();

    @Key("public.sshkey.field")
    String publicSshKeyField();

    @Key("delete.sshkey.question")
    SafeHtml deleteSshKeyQuestion(String host);

    @Key("delete.sshkey.title")
    String deleteSshKeyTitle();

    @Key("delete.sshkey.failed")
    String deleteSshKeyFailed();

    @Key("sshkeys.provider.not.found")
    String sshKeysProviderNotFound(String host);

    @Key("failed.to.generate.ssh.key")
    String failedToGenerateSshKey();

    @Key("failed.to.load.ssh.keys")
    String failedToLoadSshKeys();

    @Key("failed.to.upload.ssh.key")
    String failedToUploadSshKey();

    @Key("invalid.hostname")
    String invalidHostName();
}
