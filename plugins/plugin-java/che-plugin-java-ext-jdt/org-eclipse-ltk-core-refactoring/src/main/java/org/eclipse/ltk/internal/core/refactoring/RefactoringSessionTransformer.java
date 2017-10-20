/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Transformer for XML-based refactoring histories.
 *
 * @since 3.2
 */
public final class RefactoringSessionTransformer {

  /** Comparator for attributes */
  private static final class AttributeComparator implements Comparator {

    /** {@inheritDoc} */
    public int compare(final Object first, final Object second) {
      final Attr predecessor = (Attr) first;
      final Attr successor = (Attr) second;
      return Collator.getInstance().compare(predecessor.getName(), successor.getName());
    }
  }

  /** The current document, or <code>null</code> */
  private Document fDocument = null;

  /** Should project information be included? */
  private final boolean fProjects;

  /** The current refactoring node, or <code>null</code> */
  private Node fRefactoring = null;

  /** The current refactoring arguments, or <code>null</code> */
  private List fRefactoringArguments = null;

  /** The current session node, or <code>null</code> */
  private Node fSession = null;

  /** The current session arguments, or <code>null</code> */
  private List fSessionArguments = null;

  /**
   * Creates a new refactoring session transformer.
   *
   * @param projects <code>true</code> to include project information, <code>false</code> otherwise
   */
  public RefactoringSessionTransformer(final boolean projects) {
    fProjects = projects;
  }

  /**
   * Adds the attributes specified in the list to the node, in ascending order of their names.
   *
   * @param node the node
   * @param list the list of attributes
   */
  private void addArguments(final Node node, final List list) {
    final NamedNodeMap map = node.getAttributes();
    if (map != null) {
      Collections.sort(list, new AttributeComparator());
      for (final Iterator iterator = list.iterator(); iterator.hasNext(); ) {
        final Attr attribute = (Attr) iterator.next();
        map.setNamedItem(attribute);
      }
    }
  }

  /**
   * Begins the transformation of a refactoring specified by the given arguments.
   *
   * <p>Calls to {@link RefactoringSessionTransformer#beginRefactoring(String, long, String, String,
   * String, int)} must be balanced with calls to {@link
   * RefactoringSessionTransformer#endRefactoring()}. If the transformer is already processing a
   * refactoring, nothing happens.
   *
   * @param id the unique identifier of the refactoring
   * @param stamp the time stamp of the refactoring, or <code>-1</code>
   * @param project the non-empty name of the project this refactoring is associated with, or <code>
   *     null</code>
   * @param description a human-readable description of the refactoring
   * @param comment the comment associated with the refactoring, or <code>null</code>
   * @param flags the flags associated with refactoring
   * @throws CoreException if an error occurs while creating a new refactoring
   */
  public void beginRefactoring(
      final String id,
      long stamp,
      final String project,
      final String description,
      final String comment,
      final int flags)
      throws CoreException {
    Assert.isNotNull(id);
    Assert.isNotNull(description);
    Assert.isTrue(flags >= RefactoringDescriptor.NONE);
    try {
      if (fDocument == null)
        fDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException exception) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              RefactoringCorePlugin.getPluginId(),
              IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR,
              exception.getLocalizedMessage(),
              null));
    } catch (FactoryConfigurationError exception) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              RefactoringCorePlugin.getPluginId(),
              IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR,
              exception.getLocalizedMessage(),
              null));
    }
    if (fRefactoring == null) {
      try {
        fRefactoringArguments = new ArrayList(16);
        fRefactoring =
            fDocument.createElement(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
        Attr attribute = fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_ID);
        attribute.setValue(id);
        fRefactoringArguments.add(attribute);
        if (stamp >= 0) {
          attribute = fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_STAMP);
          attribute.setValue(new Long(stamp).toString());
          fRefactoringArguments.add(attribute);
        }
        if (flags != RefactoringDescriptor.NONE) {
          attribute = fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_FLAGS);
          attribute.setValue(String.valueOf(flags));
          fRefactoringArguments.add(attribute);
        }
        attribute =
            fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_DESCRIPTION);
        attribute.setValue(description);
        fRefactoringArguments.add(attribute);
        if (comment != null && !"".equals(comment)) { // $NON-NLS-1$
          attribute =
              fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT);
          attribute.setValue(comment);
          fRefactoringArguments.add(attribute);
        }
        if (project != null && fProjects) {
          attribute =
              fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_PROJECT);
          attribute.setValue(project);
          fRefactoringArguments.add(attribute);
        }
        if (fSession == null) fDocument.appendChild(fRefactoring);
        else fSession.appendChild(fRefactoring);
      } catch (DOMException exception) {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                RefactoringCorePlugin.getPluginId(),
                IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR,
                exception.getLocalizedMessage(),
                null));
      }
    }
  }

  /**
   * Begins the transformation of a refactoring session.
   *
   * <p>Calls to {@link RefactoringSessionTransformer#beginSession(String, String)} must be balanced
   * with calls to {@link RefactoringSessionTransformer#endSession()}. If the transformer is already
   * processing a session, nothing happens.
   *
   * @param comment the comment associated with the refactoring session, or <code>null</code>
   * @param version the non-empty version tag
   * @throws CoreException if an error occurs while creating a new session
   */
  public void beginSession(final String comment, final String version) throws CoreException {
    if (fDocument == null) {
      try {
        fDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        fSession = fDocument.createElement(IRefactoringSerializationConstants.ELEMENT_SESSION);
        fSessionArguments = new ArrayList(2);
        Attr attribute =
            fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_VERSION);
        attribute.setValue(version);
        fSessionArguments.add(attribute);
        if (comment != null && !"".equals(comment)) { // $NON-NLS-1$
          attribute =
              fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT);
          attribute.setValue(comment);
          fSessionArguments.add(attribute);
        }
        fDocument.appendChild(fSession);
      } catch (DOMException exception) {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                RefactoringCorePlugin.getPluginId(),
                IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR,
                exception.getLocalizedMessage(),
                null));
      } catch (ParserConfigurationException exception) {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                RefactoringCorePlugin.getPluginId(),
                IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR,
                exception.getLocalizedMessage(),
                null));
      }
    }
  }

  /**
   * Creates a refactoring argument with the specified name and value.
   *
   * <p>If no refactoring is currently processed, this call has no effect.
   *
   * @param name the non-empty name of the argument
   * @param value the value of the argument
   * @throws CoreException if an error occurs while creating a new argument
   */
  public void createArgument(final String name, final String value) throws CoreException {
    Assert.isNotNull(name);
    Assert.isTrue(!"".equals(name)); // $NON-NLS-1$
    Assert.isNotNull(value);
    if (fDocument != null && fRefactoringArguments != null && value != null) {
      try {
        final Attr attribute = fDocument.createAttribute(name);
        attribute.setValue(value);
        fRefactoringArguments.add(attribute);
      } catch (DOMException exception) {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                RefactoringCorePlugin.getPluginId(),
                IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR,
                exception.getLocalizedMessage(),
                null));
      }
    }
  }

  /**
   * Ends the transformation of the current refactoring.
   *
   * <p>If no refactoring is currently processed, this call has no effect.
   */
  public void endRefactoring() {
    if (fRefactoring != null && fRefactoringArguments != null)
      addArguments(fRefactoring, fRefactoringArguments);
    fRefactoringArguments = null;
    fRefactoring = null;
  }

  /**
   * Ends the transformation of the current refactoring session.
   *
   * <p>If no refactoring session is currently processed, this call has no effect.
   */
  public void endSession() {
    if (fSession != null && fSessionArguments != null) addArguments(fSession, fSessionArguments);
    fSessionArguments = null;
    fSession = null;
  }

  /**
   * Returns the result of the transformation process.
   *
   * <p>This method must only be called once during the life time of a transformer.
   *
   * @return the object representing the refactoring session, or <code>null</code> if no session has
   *     been transformed
   */
  public Document getResult() {
    final Document document = fDocument;
    fDocument = null;
    return document;
  }
}
