/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;

/**
 * Viewer to present the preview for a {@link org.eclipse.ltk.core.refactoring.Change}.
 *
 * <p>Viewers are associated with a change object via the extension point <code>
 * org.eclipse.ltk.ui.refactoring.changePreviewViewers</code>. Implementors of this extension point
 * must therefore implement this interface.
 *
 * <p>To ensure visual consistency across all provided preview viewers the widget hierarchy provided
 * through the method {@link #createControl(Composite)} has to use a {@link swt.custom.ViewForm} as
 * its root widget.
 *
 * <p>Clients of this interface should call <code>createControl</code> before calling <code>setInput
 * </code>.
 *
 * @since 3.0
 */
public interface IChangePreviewViewer {

  //	/**
  //	 * Creates the preview viewer's widget hierarchy. This method
  //	 * is only called once. Method <code>getControl()</code>
  //	 * should be used to retrieve the widget hierarchy.
  //	 *
  //	 * @param parent the parent for the widget hierarchy
  //	 *
  //	 * @see #getControl()
  //	 */
  //	public void createControl(Composite parent);
  //
  //	/**
  //	 * Returns the preview viewer's SWT control.
  //	 *
  //	 * @return the preview viewer's SWT control or <code>null</code>
  //	 *  is the widget hierarchy hasn't been created yet
  //	 */
  //	public Control getControl();
  //

  /**
   * Sets the preview viewer's input element.
   *
   * @param input the input element
   */
  ChangePreview setInput(ChangePreviewViewerInput input);
}
