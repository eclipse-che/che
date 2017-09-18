/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'PHPUnitTestLocalizationConstant.properties'.
 *
 * @author Bartlomiej Laczkowski
 */
public interface PHPUnitTestLocalizationConstant extends Messages {

  @Key("action.runScript.title")
  String actionRunScriptTitle();

  @Key("action.runScript.description")
  String actionRunScriptDescription();
}
