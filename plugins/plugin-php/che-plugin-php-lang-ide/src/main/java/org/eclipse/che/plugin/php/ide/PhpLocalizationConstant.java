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
package org.eclipse.che.plugin.php.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'PhpLocalizationConstant.properties'.
 *
 * @author Kaloyan Raev
 */
public interface PhpLocalizationConstant extends Messages {

  @Key("php.action.create.php.file.title")
  @DefaultMessage("PHP File")
  String createPhpFileActionTitle();

  @Key("php.action.create.php.file.description")
  @DefaultMessage("Create PHP File")
  String createPhpFileActionDescription();
}
