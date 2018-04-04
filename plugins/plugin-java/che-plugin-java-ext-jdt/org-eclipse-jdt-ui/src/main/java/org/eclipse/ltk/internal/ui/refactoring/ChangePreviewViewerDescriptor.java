/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

public class ChangePreviewViewerDescriptor /*extends AbstractDescriptor*/ {

  //	private static final String EXT_ID= "changePreviewViewers"; //$NON-NLS-1$

  //	private static DescriptorManager fgDescriptions= new DescriptorManager(EXT_ID, "change") {
  // //$NON-NLS-1$
  //		protected AbstractDescriptor createDescriptor(IConfigurationElement element) {
  //			return new ChangePreviewViewerDescriptor(element);
  //		}
  //	};

  private final Class<? extends TextEditChangePreviewViewer> clazz;

  public ChangePreviewViewerDescriptor(Class<? extends TextEditChangePreviewViewer> clazz) {
    this.clazz = clazz;
  }

  private static ChangePreviewViewerDescriptor changePreviewViewerDescriptor =
      new ChangePreviewViewerDescriptor(TextEditChangePreviewViewer.class);

  public static ChangePreviewViewerDescriptor get(Object element) throws CoreException {
    if (element instanceof TextEditBasedChange) {
      return changePreviewViewerDescriptor;
    }
    return null;
    //        return fgDescriptions.get(element.getClass());
  }

  //	public ChangePreviewViewerDescriptor(IConfigurationElement element) {
  //		super(element);
  //	}

  public IChangePreviewViewer createViewer() throws CoreException {
    //        return (IChangePreviewViewer)fConfigurationElement.createExecutableExtension(CLASS);
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CoreException(new Status(IStatus.ERROR, "", e.getMessage(), e));
    }
  }
}
