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
