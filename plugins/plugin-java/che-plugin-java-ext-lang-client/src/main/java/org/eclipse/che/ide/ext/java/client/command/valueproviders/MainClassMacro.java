/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandModel;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandPagePresenter;

/**
 * Provides a path to the Main class.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MainClassMacro implements Macro {

  private static final String KEY = "${java.main.class}";

  private final JavaCommandPagePresenter javaCommandPagePresenter;
  private final JavaLocalizationConstant localizationConstants;

  @Inject
  public MainClassMacro(
      JavaCommandPagePresenter javaCommandPagePresenter,
      JavaLocalizationConstant localizationConstants) {
    this.javaCommandPagePresenter = javaCommandPagePresenter;
    this.localizationConstants = localizationConstants;
  }

  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroJavaMainClassDescription();
  }

  @Override
  public Promise<String> expand() {
    JavaCommandModel editedJavaCommandModel = javaCommandPagePresenter.getEditedCommandModel();

    return editedJavaCommandModel == null
        ? Promises.resolve("")
        : Promises.resolve(editedJavaCommandModel.getMainClass());
  }
}
