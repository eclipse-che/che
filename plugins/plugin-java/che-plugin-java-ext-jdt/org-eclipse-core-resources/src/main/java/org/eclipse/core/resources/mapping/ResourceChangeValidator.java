/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation James Blackburn (Broadcom
 * Corp.) - ongoing development
 * *****************************************************************************
 */
package org.eclipse.core.resources.mapping;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.core.internal.resources.mapping.ResourceChangeDescriptionFactory;
import org.eclipse.che.core.internal.utils.Policy;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * The resource change validator is used to validate that changes made to resources will not
 * adversely affect the models stored in those resources.
 *
 * <p>The validator is used by first creating a resource delta describing the proposed changes. A
 * delta can be generated using a {@link
 * org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory}. The change is then
 * validated by calling the {@link #validateChange(IResourceDelta, IProgressMonitor)} method. This
 * example validates a change to a single file: <code>
 *    IFile file = ..;//some file that is going to be changed
 *    ResourceChangeValidator validator = ResourceChangeValidator.getValidator();
 *    IResourceChangeDescriptionFactory factory = validator.createDeltaFactory();
 *    factory.change(file);
 *    IResourceDelta delta = factory.getDelta();
 *    IStatus result = validator.validateChange(delta, null);
 * </code> If the result status does not have severity {@link IStatus#OK}, then the changes may
 * cause problems for models that are built on those resources. In this case the user should be
 * presented with the status message to determine if they want to proceed with the modification.
 *
 * @since 3.2
 */
public final class ResourceChangeValidator {

  private static org.eclipse.core.resources.mapping.ResourceChangeValidator instance;

  /**
   * Return the singleton change validator.
   *
   * @return the singleton change validator
   */
  public static org.eclipse.core.resources.mapping.ResourceChangeValidator getValidator() {
    if (instance == null)
      instance = new org.eclipse.core.resources.mapping.ResourceChangeValidator();
    return instance;
  }

  /**
   * Singleton accessor method should be used instead.
   *
   * @see #getValidator()
   */
  private ResourceChangeValidator() {
    super();
  }

  private IStatus combineResults(IStatus[] result) {
    List<IStatus> notOK = new ArrayList<IStatus>();
    for (int i = 0; i < result.length; i++) {
      IStatus status = result[i];
      if (!status.isOK()) {
        notOK.add(status);
      }
    }
    if (notOK.isEmpty()) {
      return Status.OK_STATUS;
    }
    if (notOK.size() == 1) {
      return notOK.get(0);
    }
    return new MultiStatus(
        ResourcesPlugin.PI_RESOURCES,
        0,
        notOK.toArray(new IStatus[notOK.size()]),
        Messages.mapping_multiProblems,
        null);
  }

  /**
   * Return an empty change description factory that can be used to build a proposed resource delta.
   *
   * @return an empty change description factory that can be used to build a proposed resource delta
   */
  public IResourceChangeDescriptionFactory createDeltaFactory() {
    return new ResourceChangeDescriptionFactory();
  }

  private ModelProvider[] getProviders(IResource[] resources) {
    IModelProviderDescriptor[] descriptors = ModelProvider.getModelProviderDescriptors();
    List<ModelProvider> result = new ArrayList<ModelProvider>();
    for (int i = 0; i < descriptors.length; i++) {
      IModelProviderDescriptor descriptor = descriptors[i];
      try {
        IResource[] matchingResources = descriptor.getMatchingResources(resources);
        if (matchingResources.length > 0) {
          result.add(descriptor.getModelProvider());
        }
      } catch (CoreException e) {
        Policy.log(
            e.getStatus().getSeverity(),
            NLS.bind("Could not instantiate provider {0}", descriptor.getId()),
            e); // $NON-NLS-1$
      }
    }
    return result.toArray(new ModelProvider[result.size()]);
  }

  /*
   * Get the roots of any changes.
   */
  private IResource[] getRootResources(IResourceDelta root) {
    final ChangeDescription changeDescription = new ChangeDescription();
    try {
      root.accept(
          new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta delta) {
              return changeDescription.recordChange(delta);
            }
          });
    } catch (CoreException e) {
      // Shouldn't happen since the ProposedResourceDelta accept doesn't throw an
      // exception and our visitor doesn't either
      Policy.log(IStatus.ERROR, "Internal error", e); // $NON-NLS-1$
    }
    return changeDescription.getRootResources();
  }

  /**
   * Validate the proposed changes contained in the given delta by consulting all model providers to
   * determine if the changes have any adverse side effects.
   *
   * <p>This method returns either a {@link org.eclipse.core.resources.mapping.ModelStatus}, or a
   * {@link MultiStatus} whose children are {@link org.eclipse.core.resources.mapping.ModelStatus}.
   * In either case, the severity of the status indicates the severity of the possible side-effects
   * of the operation. Any severity other than <code>OK</code> should be shown to the user. The
   * message should be a human readable message that will allow the user to make a decision on
   * whether to continue with the operation. The model provider id should indicate which model is
   * flagging the the possible side effects.
   *
   * @param delta a delta tree containing the proposed changes
   * @return a status indicating any potential side effects on models stored in the affected
   *     resources.
   */
  public IStatus validateChange(IResourceDelta delta, IProgressMonitor monitor) {
    monitor = Policy.monitorFor(monitor);
    try {
      //			IResource[] resources = getRootResources(delta);
      //			ModelProvider[] providers = getProviders(resources);
      //			if (providers.length == 0)
      return Status.OK_STATUS;
      //			monitor.beginTask(Messages.mapping_validate, providers.length);
      //			IStatus[] result = new IStatus[providers.length];
      //			for (int i = 0; i < providers.length; i++)
      //				result[i] = providers[i].validateChange(delta, Policy.subMonitorFor(monitor, 1));
      //			return combineResults(result);
    } finally {
      monitor.done();
    }
  }
}
