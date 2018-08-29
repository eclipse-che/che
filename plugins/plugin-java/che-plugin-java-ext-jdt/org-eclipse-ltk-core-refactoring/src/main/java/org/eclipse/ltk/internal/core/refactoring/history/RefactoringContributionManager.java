/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ltk.core.refactoring.CheRefactoringContributions;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Contribution manager for refactorings.
 *
 * @since 3.2
 */
public final class RefactoringContributionManager implements IRegistryChangeListener {

  /** The class attribute */
  private static final String ATTRIBUTE_CLASS = "class"; // $NON-NLS-1$

  /** The id attribute */
  private static final String ATTRIBUTE_ID = "id"; // $NON-NLS-1$
  /** The refactoring contributions extension point */
  private static final String REFACTORING_CONTRIBUTIONS_EXTENSION_POINT =
      "refactoringContributions"; // $NON-NLS-1$
  /** The singleton instance */
  private static RefactoringContributionManager fInstance = null;
  /**
   * The refactoring contribution cache (element type: &lt;String, <code>RefactoringContribution&gt;
   * </code>)
   */
  private Map fContributionCache = null;
  /**
   * The refactoring contribution cache (element type: &lt;RefactoringContribution, <code>String&gt;
   * </code>)
   *
   * @since 3.3
   */
  private Map fIdCache = null;

  /** Creates a new refactoring contribution manager. */
  private RefactoringContributionManager() {
    // Not instantiatable
  }

  /**
   * Returns the singleton instance of the refactoring contribution manager.
   *
   * @return the singleton instance
   */
  public static RefactoringContributionManager getInstance() {
    if (fInstance == null) fInstance = new RefactoringContributionManager();
    return fInstance;
  }

  /** Connects this manager to the platform's extension registry. */
  public void connect() {
    Platform.getExtensionRegistry().addRegistryChangeListener(this, RefactoringCore.ID_PLUGIN);
  }

  /**
   * Creates a new refactoring descriptor for the specified input data.
   *
   * @param id the unique id of the refactoring
   * @param project the project name, or <code>null</code>
   * @param description a description
   * @param comment the comment, or <code>null</code>
   * @param arguments the argument map
   * @param flags the flags
   * @return the refactoring descriptor
   * @throws IllegalArgumentException if the argument map contains invalid keys/values
   */
  public RefactoringDescriptor createDescriptor(
      final String id,
      final String project,
      final String description,
      final String comment,
      final Map arguments,
      final int flags)
      throws IllegalArgumentException {
    Assert.isNotNull(id);
    Assert.isNotNull(description);
    Assert.isNotNull(arguments);
    Assert.isLegal(flags >= RefactoringDescriptor.NONE);
    final RefactoringContribution contribution = getRefactoringContribution(id);
    if (contribution != null)
      return contribution.createDescriptor(id, project, description, comment, arguments, flags);
    return new DefaultRefactoringDescriptor(id, project, description, comment, arguments, flags);
  }

  /** Disconnects this manager from the platform's extension registry. */
  public void disconnect() {
    Platform.getExtensionRegistry().removeRegistryChangeListener(this);
  }

  /**
   * Returns the refactoring contribution for the refactoring with the specified id.
   *
   * @param id the unique id of the refactoring
   * @return the refactoring contribution, or <code>null</code> if no refactoring contribution has
   *     been registered with the specified id
   */
  public RefactoringContribution getRefactoringContribution(final String id) {
    Assert.isNotNull(id);
    Assert.isTrue(!"".equals(id)); // $NON-NLS-1$
    populateCache();
    return (RefactoringContribution) fContributionCache.get(id);
  }

  /**
   * Returns the refactoring id for the specified refactoring contribution.
   *
   * @param contribution the refactoring contribution
   * @return the corresonding refactoring id
   * @since 3.3
   */
  public String getRefactoringId(final RefactoringContribution contribution) {
    Assert.isNotNull(contribution);
    populateCache();
    return (String) fIdCache.get(contribution);
  }

  /**
   * Populates the refactoring contribution cache if necessary.
   *
   * @since 3.3
   */
  private void populateCache() {
    if (fContributionCache == null || fIdCache == null) {
      fContributionCache = new HashMap(32);
      fIdCache = new HashMap(32);
      Map<String, String> contributions = CheRefactoringContributions.getRefactoringContributions();
      contributions.forEach(
          (id, clazz) -> {
            try {
              final Object implementation =
                  CheRefactoringContributions.createExecutableExtension(clazz);
              if (implementation instanceof RefactoringContribution) {
                if (fContributionCache.get(id) != null)
                  RefactoringCorePlugin.logErrorMessage(
                      Messages.format(
                          RefactoringCoreMessages.RefactoringCorePlugin_duplicate_warning,
                          new String[] {id, clazz}));
                fContributionCache.put(id, implementation);
                fIdCache.put(implementation, id);
              } else
                RefactoringCorePlugin.logErrorMessage(
                    Messages.format(
                        RefactoringCoreMessages.RefactoringCorePlugin_creation_error,
                        new String[] {id, clazz}));
            } catch (CoreException exception) {
              RefactoringCorePlugin.log(exception);
            }
          });
      //			final IConfigurationElement[] elements=
      // Platform.getExtensionRegistry().getConfigurationElementsFor(RefactoringCore.ID_PLUGIN,
      // REFACTORING_CONTRIBUTIONS_EXTENSION_POINT);
      //			for (int index= 0; index < elements.length; index++) {
      //				final IConfigurationElement element= elements[index];
      //				final String attributeId= element.getAttribute(ATTRIBUTE_ID);
      //				final String point= RefactoringCore.ID_PLUGIN + "." +
      // REFACTORING_CONTRIBUTIONS_EXTENSION_POINT; //$NON-NLS-1$
      //				if (attributeId != null && !"".equals(attributeId)) { //$NON-NLS-1$
      //					final String className= element.getAttribute(ATTRIBUTE_CLASS);
      //					if (className != null && !"".equals(className)) { //$NON-NLS-1$
      //						try {
      //							final Object implementation= element.createExecutableExtension(ATTRIBUTE_CLASS);
      //							if (implementation instanceof RefactoringContribution) {
      //								if (fContributionCache.get(attributeId) != null)
      //									RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages
      // .RefactoringCorePlugin_duplicate_warning, new String[] { attributeId, point}));
      //								fContributionCache.put(attributeId, implementation);
      //								fIdCache.put(implementation, attributeId);
      //							} else
      //
      //	RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_creation_error, new String[] { point, attributeId}));
      //						} catch (CoreException exception) {
      //							RefactoringCorePlugin.log(exception);
      //						}
      //					} else
      //
      //	RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_missing_class_attribute, new String[] { point, attributeId, ATTRIBUTE_CLASS}));
      //				} else
      //
      //	RefactoringCorePlugin.logErrorMessage(Messages.format(RefactoringCoreMessages.RefactoringCorePlugin_missing_attribute, new String[] { point, ATTRIBUTE_ID}));
      //			}
    }
  }

  /** {@inheritDoc} */
  public void registryChanged(final IRegistryChangeEvent event) {
    fContributionCache = null;
    fIdCache = null;
  }
}
