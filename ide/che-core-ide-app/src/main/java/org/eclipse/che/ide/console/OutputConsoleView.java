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
package org.eclipse.che.ide.console;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for output console.
 *
 * @author Artem Zatsarynnyi
 */
public interface OutputConsoleView extends View<OutputConsoleView.ActionDelegate> {

  /**
   * Shows the command line to the console.
   *
   * @param commandLine command line
   */
  void showCommandLine(String commandLine);

  /**
   * Shows the command preview URL.
   *
   * @param previewUrl preview URL
   */
  void showPreviewUrl(String previewUrl);

  /**
   * Prints text.
   *
   * @param text text to print
   */
  void print(String text);

  /**
   * Prints colored text. Color of the text is defined by true color format with background, red,
   * blue and green components.
   *
   * @param text text to print
   * @param background background color component
   * @param red red color component
   * @param blue color component
   * @param green color component
   */
  void print(String text, int background, int red, int blue, int green);

  /**
   * Returns the console text.
   *
   * @return console text
   */
  String getText();

  /** Hides command title and command label. */
  void hideCommand();

  /** Hides preview title and preview label. */
  void hidePreview();

  /** Clears the console. */
  void clearConsole();

  /**
   * Sets visibility for Re-Run button.
   *
   * @param visible use <code>true</code> to show the button
   */
  void setReRunButtonVisible(boolean visible);

  /**
   * Sets visibility for Stop button.
   *
   * @param visible use <code>true</code> to show the button
   */
  void setStopButtonVisible(boolean visible);

  /**
   * Enables or disables Stop process button.
   *
   * @param enable new enabled state for the button
   */
  void enableStopButton(boolean enable);

  /** Action handler for the view actions/controls. */
  interface ActionDelegate {

    /** Handle click on `Run process` button. */
    void reRunProcessButtonClicked();

    /** Handle click on `Stop process` button. */
    void stopProcessButtonClicked();

    /** Handle click on `Clear console` button. */
    void clearOutputsButtonClicked();

    /** Handle click on `Download outputs` button. */
    void downloadOutputsButtonClicked();

    /** Returns the customizer for the console output */
    OutputCustomizer getCustomizer();
  }
}
