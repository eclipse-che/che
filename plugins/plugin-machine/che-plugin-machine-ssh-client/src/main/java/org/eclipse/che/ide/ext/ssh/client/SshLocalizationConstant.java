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
package org.eclipse.che.ide.ext.ssh.client;

import com.google.gwt.i18n.client.Messages;

/** @author Evgen Vidolob */
public interface SshLocalizationConstant extends Messages {
  @Key("uploadButton")
  String uploadButton();

  @Key("fileNameFieldTitle")
  String fileNameFieldTitle();

  @Key("key.title")
  String sshKeyTitle();

  @Key("generate.sshKey.title")
  String generateSshKeyTitle();

  @Key("download.private.key.title")
  String downloadPrivateKeyTitle();

  @Key("download.private.key.message")
  String downloadPrivateKeyMessage();

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
  String deleteSshKeyQuestion(String host);

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
}
