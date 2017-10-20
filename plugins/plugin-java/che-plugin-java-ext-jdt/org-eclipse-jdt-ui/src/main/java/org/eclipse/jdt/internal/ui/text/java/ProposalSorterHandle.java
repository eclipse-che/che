/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Marcel Bruch
 * <bruch@cs.tu-darmstadt.de> - [content assist] Allow to re-sort proposals -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=350991
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;

/**
 * The description of an extension to the <code>org.eclipse.jdt.ui.javaCompletionProposalSorters
 * </code> extension point. Instances are immutable.
 *
 * @since 3.2
 */
public final class ProposalSorterHandle {
  /** The extension schema name of the id attribute. */
  private static final String ID = "id"; // $NON-NLS-1$
  /** The extension schema name of the name attribute. */
  private static final String NAME = "name"; // $NON-NLS-1$
  /** The extension schema name of the class attribute. */
  private static final String CLASS = "class"; // $NON-NLS-1$
  /** The name of the performance event used to trace extensions. */
  private static final String PERFORMANCE_EVENT =
      JavaPlugin.getPluginId() + "/perf/content_assist_sorters/extensions"; // $NON-NLS-1$
  /**
   * If <code>true</code>, execution time of extensions is measured and extensions may be disabled
   * if execution takes too long.
   */
  private static final boolean MEASURE_PERFORMANCE = PerformanceStats.isEnabled(PERFORMANCE_EVENT);
  /** The one and only operation name. */
  private static final String SORT = "sort"; // $NON-NLS-1$

  /** The identifier of the extension. */
  private final String fId;
  /** The name of the extension. */
  private final String fName;
  /** The class name of the provided <code>AbstractProposalSorter</code>. */
  private final String fClass;
  /** The configuration element of this extension. */
  private final IConfigurationElement fElement;
  /** The computer, if instantiated, <code>null</code> otherwise. */
  private AbstractProposalSorter fSorter;

  /**
   * Creates a new descriptor.
   *
   * @param element the configuration element to read
   * @throws InvalidRegistryObjectException if the configuration element is not valid any longer
   * @throws org.eclipse.core.runtime.CoreException if the configuration does not contain mandatory
   *     attributes
   */
  ProposalSorterHandle(IConfigurationElement element)
      throws InvalidRegistryObjectException, CoreException {
    Assert.isLegal(element != null);

    fElement = element;
    fId = element.getAttribute(ID);
    checkNotNull(fId, ID);

    String name = element.getAttribute(NAME);
    if (name == null) fName = fId;
    else fName = name;

    fClass = element.getAttribute(CLASS);
    checkNotNull(fClass, CLASS);
  }

  /**
   * Checks that the given attribute value is not <code>null</code>.
   *
   * @param value the value to check if not null
   * @param attribute the attribute
   * @throws InvalidRegistryObjectException if the registry element is no longer valid
   * @throws org.eclipse.core.runtime.CoreException if <code>value</code> is <code>null</code>
   */
  private void checkNotNull(Object value, String attribute)
      throws InvalidRegistryObjectException, CoreException {
    if (value == null) {
      Object[] args = {getId(), fElement.getContributor().getName(), attribute};
      String message =
          Messages.format(
              JavaTextMessages.CompletionProposalComputerDescriptor_illegal_attribute_message,
              args);
      IStatus status =
          new Status(IStatus.WARNING, JavaPlugin.getPluginId(), IStatus.OK, message, null);
      throw new CoreException(status);
    }
  }

  /**
   * Returns the identifier of the described extension.
   *
   * @return Returns the id
   */
  public String getId() {
    return fId;
  }

  /**
   * Returns the name of the described extension.
   *
   * @return Returns the name
   */
  public String getName() {
    return fName;
  }

  /**
   * Returns a cached instance of the sorter as described in the extension's xml. The sorter is
   * {@link #createSorter() created} the first time that this method is called and then cached.
   *
   * @return a new instance of the proposal sorter as described by this descriptor
   * @throws org.eclipse.core.runtime.CoreException if the creation fails
   * @throws InvalidRegistryObjectException if the extension is not valid any longer (e.g. due to
   *     plug-in unloading)
   */
  synchronized AbstractProposalSorter getSorter()
      throws CoreException, InvalidRegistryObjectException {
    if (fSorter == null) fSorter = createSorter();
    return fSorter;
  }

  /**
   * Returns a new instance of the sorter as described in the extension's xml.
   *
   * @return a new instance of the completion proposal computer as described by this descriptor
   * @throws org.eclipse.core.runtime.CoreException if the creation fails
   * @throws InvalidRegistryObjectException if the extension is not valid any longer (e.g. due to
   *     plug-in unloading)
   */
  private AbstractProposalSorter createSorter()
      throws CoreException, InvalidRegistryObjectException {
    return (AbstractProposalSorter) fElement.createExecutableExtension(CLASS);
  }

  /**
   * Safely computes completion proposals through the described extension. If the extension throws
   * an exception or otherwise does not adhere to the contract described in {@link
   * AbstractProposalSorter}, the list is returned as is.
   *
   * @param context the invocation context passed on to the extension
   * @param proposals the list of computed completion proposals to be sorted (element type: {@link
   *     ICompletionProposal}), must be writable
   */
  public void sortProposals(
      ContentAssistInvocationContext context, List<ICompletionProposal> proposals) {
    IStatus status;
    try {
      AbstractProposalSorter sorter = getSorter();

      PerformanceStats stats = startMeter(SORT, sorter);

      sorter.beginSorting(context);
      Collections.sort(proposals, sorter);
      sorter.endSorting();

      status = stopMeter(stats, SORT);

      // valid result
      if (status == null) return;

      status = createAPIViolationStatus(SORT);

    } catch (InvalidRegistryObjectException x) {
      status = createExceptionStatus(x);
    } catch (CoreException x) {
      status = createExceptionStatus(x);
    } catch (RuntimeException x) {
      status = createExceptionStatus(x);
    }

    JavaPlugin.log(status);
    return;
  }

  private IStatus stopMeter(final PerformanceStats stats, String operation) {
    if (MEASURE_PERFORMANCE) {
      stats.endRun();
      if (stats.isFailure()) return createPerformanceStatus(operation);
    }
    return null;
  }

  private PerformanceStats startMeter(String context, AbstractProposalSorter sorter) {
    final PerformanceStats stats;
    if (MEASURE_PERFORMANCE) {
      stats = PerformanceStats.getStats(PERFORMANCE_EVENT, sorter);
      stats.startRun(context);
    } else {
      stats = null;
    }
    return stats;
  }

  Status createExceptionStatus(InvalidRegistryObjectException x) {
    // extension has become invalid - log & disable
    String disable = createBlameMessage();
    String reason = JavaTextMessages.CompletionProposalComputerDescriptor_reason_invalid;
    return new Status(
        IStatus.INFO,
        JavaPlugin.getPluginId(),
        IStatus.OK,
        disable + " " + reason,
        x); // $NON-NLS-1$
  }

  Status createExceptionStatus(CoreException x) {
    // unable to instantiate the extension - log & disable
    String disable = createBlameMessage();
    String reason = JavaTextMessages.CompletionProposalComputerDescriptor_reason_instantiation;
    return new Status(
        IStatus.ERROR,
        JavaPlugin.getPluginId(),
        IStatus.OK,
        disable + " " + reason,
        x); // $NON-NLS-1$
  }

  Status createExceptionStatus(RuntimeException x) {
    // misbehaving extension - log & disable
    String disable = createBlameMessage();
    String reason = JavaTextMessages.CompletionProposalComputerDescriptor_reason_runtime_ex;
    return new Status(
        IStatus.WARNING,
        JavaPlugin.getPluginId(),
        IStatus.OK,
        disable + " " + reason,
        x); // $NON-NLS-1$
  }

  private Status createAPIViolationStatus(String operation) {
    String disable = createBlameMessage();
    Object[] args = {operation};
    String reason =
        Messages.format(JavaTextMessages.CompletionProposalComputerDescriptor_reason_API, args);
    return new Status(
        IStatus.WARNING,
        JavaPlugin.getPluginId(),
        IStatus.OK,
        disable + " " + reason,
        null); // $NON-NLS-1$
  }

  private Status createPerformanceStatus(String operation) {
    String disable = createBlameMessage();
    Object[] args = {operation};
    String reason =
        Messages.format(
            JavaTextMessages.CompletionProposalComputerDescriptor_reason_performance, args);
    return new Status(
        IStatus.WARNING,
        JavaPlugin.getPluginId(),
        IStatus.OK,
        disable + " " + reason,
        null); // $NON-NLS-1$
  }

  private String createBlameMessage() {
    Object[] args = {getName(), getId()};
    String disable = Messages.format(JavaTextMessages.ProposalSorterHandle_blame, args);
    return disable;
  }

  /**
   * Returns the error message from the described extension, <code>null</code> for no error.
   *
   * @return the error message from the described extension, <code>null</code> for no error
   */
  public String getErrorMessage() {
    return null;
  }
}
