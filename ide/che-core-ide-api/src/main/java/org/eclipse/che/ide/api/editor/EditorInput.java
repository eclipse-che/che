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
package org.eclipse.che.ide.api.editor;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * <code>EditorInput</code> is a light weight descriptor of editor input, like a file name but more
 * abstract. It is not a model. It is a description of the model source for an <code>Editor</code>.
 *
 * <p>An editor input is passed to an editor via the <code>EditorPartPresenter.init</code> method.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 */
public interface EditorInput {

  /**
   * Returns the image descriptor for this input.
   *
   * @return the SVG image resource for this input.
   */
  @NotNull
  SVGResource getSVGResource();

  /**
   * Returns the name of this editor input for display purposes.
   *
   * <p>For instance, when the input is from a file, the return value would ordinarily be just the
   * file name.
   *
   * @return the name string; never <code>null</code>;
   */
  @NotNull
  String getName();

  /**
   * Returns the tool tip text for this editor input. This text is used to differentiate between two
   * input with the same name. For instance, MyClass.java in folder X and MyClass.java in folder Y.
   * The format of the text varies between input types.
   *
   * @return the tool tip text; never <code>null</code>.
   */
  @NotNull
  String getToolTipText();

  /**
   * Return the file of this input
   *
   * @return the File; never <code>null</code>
   */
  @NotNull
  VirtualFile getFile();

  /**
   * Sets file of this input.
   *
   * @param file
   */
  void setFile(@NotNull VirtualFile file);
}
