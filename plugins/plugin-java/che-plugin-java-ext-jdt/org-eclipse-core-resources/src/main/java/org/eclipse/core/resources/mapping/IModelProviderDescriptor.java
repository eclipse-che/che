/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A model provider descriptor contains information about a model provider obtained from the plug-in
 * manifest (<code>plugin.xml</code>) file.
 *
 * <p>Model provider descriptors are platform-defined objects that exist independent of whether that
 * model provider's plug-in has been started. In contrast, a model provider's runtime object (<code>
 * ModelProvider</code>) generally runs plug-in-defined code.
 *
 * @see org.eclipse.core.resources.mapping.ModelProvider
 * @since 3.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IModelProviderDescriptor {

  /**
   * Return the ids of model providers that this model provider extends.
   *
   * @return the ids of model providers that this model provider extends
   */
  public String[] getExtendedModels();

  /**
   * Returns the unique identifier of this model provider.
   *
   * <p>The model provider identifier is composed of the model provider's plug-in id and the simple
   * id of the provider extension. For example, if plug-in <code>"com.xyz"</code> defines a provider
   * extension with id <code>"myModelProvider"</code>, the unique model provider identifier will be
   * <code>"com.xyz.myModelProvider"</code>.
   *
   * @return the unique model provider identifier
   */
  public String getId();

  /**
   * Returns a displayable label for this model provider. Returns the empty string if no label for
   * this provider is specified in the plug-in manifest file.
   *
   * <p>Note that any translation specified in the plug-in manifest file is automatically applied.
   *
   * @return a displayable string label for this model provider, possibly the empty string
   */
  public String getLabel();

  /**
   * From the provides set of resources, return those that match the enablement rule specified for
   * the model provider descriptor. The resource mappings for the returned resources can then be
   * obtained by invoking {@link
   * org.eclipse.core.resources.mapping.ModelProvider#getMappings(IResource[],
   * ResourceMappingContext, IProgressMonitor)}
   *
   * @param resources the resources
   * @return the resources that match the descriptor's enablement rule
   */
  public IResource[] getMatchingResources(IResource[] resources) throws CoreException;

  /**
   * Return the set of traversals that overlap with the resources that this descriptor matches.
   *
   * @param traversals the traversals being tested
   * @return the subset of these traversals that overlap with the resources that match this
   *     descriptor
   * @throws CoreException
   */
  public ResourceTraversal[] getMatchingTraversals(ResourceTraversal[] traversals)
      throws CoreException;

  /**
   * Return the model provider for this descriptor, instantiating it if it is the first time the
   * method is called.
   *
   * @return the model provider for this descriptor
   * @exception CoreException if the model provider could not be instantiated for some reason
   */
  public ModelProvider getModelProvider() throws CoreException;
}
