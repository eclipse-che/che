/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.search.IQueryParticipant;

/** */
public class SearchParticipantDescriptor {
  private static final String CLASS = "class"; // $NON-NLS-1$
  private static final String NATURE = "nature"; // $NON-NLS-1$
  private static final String ID = "id"; // $NON-NLS-1$

  private IConfigurationElement fConfigurationElement;
  private boolean fEnabled; //

  protected SearchParticipantDescriptor(IConfigurationElement configElement) {
    fConfigurationElement = configElement;
    fEnabled = true;
  }

  /**
   * checks whether a participant has all the proper attributes.
   *
   * @return returns a status describing the result of the validation
   */
  protected IStatus checkSyntax() {
    if (fConfigurationElement.getAttribute(ID) == null) {
      String format = SearchMessages.SearchParticipant_error_noID;
      String message =
          Messages.format(
              format,
              new String[] {fConfigurationElement.getDeclaringExtension().getUniqueIdentifier()});
      return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), 0, message, null);
    }
    if (fConfigurationElement.getAttribute(NATURE) == null) {
      String format = SearchMessages.SearchParticipant_error_noNature;
      String message =
          Messages.format(format, new String[] {fConfigurationElement.getAttribute(ID)});
      return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), 0, message, null);
    }

    if (fConfigurationElement.getAttribute(CLASS) == null) {
      String format = SearchMessages.SearchParticipant_error_noClass;
      String message =
          Messages.format(format, new String[] {fConfigurationElement.getAttribute(ID)});
      return new Status(IStatus.ERROR, JavaPlugin.getPluginId(), 0, message, null);
    }
    return Status.OK_STATUS;
  }

  public String getID() {
    return fConfigurationElement.getAttribute(ID);
  }

  public void disable() {
    fEnabled = false;
  }

  public boolean isEnabled() {
    return fEnabled;
  }

  protected IQueryParticipant create() throws CoreException {
    try {
      return (IQueryParticipant) fConfigurationElement.createExecutableExtension(CLASS);
    } catch (ClassCastException e) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              JavaPlugin.getPluginId(),
              0,
              SearchMessages.SearchParticipant_error_classCast,
              e));
    }
  }

  protected String getNature() {
    return fConfigurationElement.getAttribute(NATURE);
  }
}
