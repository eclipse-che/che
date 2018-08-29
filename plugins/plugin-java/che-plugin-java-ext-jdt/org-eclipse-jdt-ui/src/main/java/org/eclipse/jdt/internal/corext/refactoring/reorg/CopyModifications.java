/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.refactoring.participants.ResourceModifications;
import org.eclipse.jdt.internal.corext.util.JavaElementResourceMapping;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

public class CopyModifications extends RefactoringModifications {

  private List<Object> fCopies;
  private List<RefactoringArguments> fCopyArguments;
  private List<IParticipantDescriptorFilter> fParticipantDescriptorFilter;

  public CopyModifications() {
    fCopies = new ArrayList<Object>();
    fCopyArguments = new ArrayList<RefactoringArguments>();
    fParticipantDescriptorFilter = new ArrayList<IParticipantDescriptorFilter>();
  }

  public void copy(IResource resource, CopyArguments args) {
    add(resource, args, null);
  }

  public void copy(IJavaElement element, CopyArguments javaArgs, CopyArguments resourceArgs)
      throws CoreException {
    switch (element.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        copy((IPackageFragmentRoot) element, javaArgs, resourceArgs);
        break;
      case IJavaElement.PACKAGE_FRAGMENT:
        copy((IPackageFragment) element, javaArgs, resourceArgs);
        break;
      case IJavaElement.COMPILATION_UNIT:
        copy((ICompilationUnit) element, javaArgs, resourceArgs);
        break;
      default:
        add(element, javaArgs, null);
    }
  }

  public void copy(
      IPackageFragmentRoot sourceFolder, CopyArguments javaArgs, CopyArguments resourceArgs) {
    add(sourceFolder, javaArgs, null);
    ResourceMapping mapping = JavaElementResourceMapping.create(sourceFolder);
    if (mapping != null) {
      add(mapping, resourceArgs, null);
    }
    IResource sourceResource = sourceFolder.getResource();
    if (sourceResource != null) {
      getResourceModifications().addCopyDelta(sourceResource, resourceArgs);
      IFile classpath = getClasspathFile((IResource) resourceArgs.getDestination());
      if (classpath != null) {
        getResourceModifications().addChanged(classpath);
      }
    }
  }

  public void copy(IPackageFragment pack, CopyArguments javaArgs, CopyArguments resourceArgs)
      throws CoreException {
    add(pack, javaArgs, null);
    ResourceMapping mapping = JavaElementResourceMapping.create(pack);
    if (mapping != null) {
      add(mapping, resourceArgs, null);
    }
    IPackageFragmentRoot javaDestination = (IPackageFragmentRoot) javaArgs.getDestination();
    if (javaDestination.getResource() == null) return;
    IPackageFragment newPack = javaDestination.getPackageFragment(pack.getElementName());
    // Here we have a special case. When we copy a package into the same source
    // folder than the user will choose an "unused" name at the end which will
    // lead to the fact that we can copy the pack. Unfortunately we don't know
    // the new name yet, so we use the current package name.
    if (!pack.hasSubpackages() && (!newPack.exists() || pack.equals(newPack))) {
      // we can do a simple move
      IContainer resourceDestination = newPack.getResource().getParent();
      createIncludingParents(resourceDestination);
      getResourceModifications().addCopyDelta(pack.getResource(), resourceArgs);
    } else {
      IContainer resourceDestination = (IContainer) newPack.getResource();
      createIncludingParents(resourceDestination);
      CopyArguments arguments =
          new CopyArguments(resourceDestination, resourceArgs.getExecutionLog());
      IResource[] resourcesToCopy = collectResourcesOfInterest(pack);
      for (int i = 0; i < resourcesToCopy.length; i++) {
        IResource toCopy = resourcesToCopy[i];
        getResourceModifications().addCopyDelta(toCopy, arguments);
      }
    }
  }

  public void copy(ICompilationUnit unit, CopyArguments javaArgs, CopyArguments resourceArgs) {
    add(unit, javaArgs, null);
    ResourceMapping mapping = JavaElementResourceMapping.create(unit);
    if (mapping != null) {
      add(mapping, resourceArgs, null);
    }
    if (unit.getResource() != null) {
      getResourceModifications().addCopyDelta(unit.getResource(), resourceArgs);
    }
  }

  @Override
  public void buildDelta(IResourceChangeDescriptionFactory builder) {
    for (int i = 0; i < fCopies.size(); i++) {
      Object element = fCopies.get(i);
      if (element instanceof IResource) {
        ResourceModifications.buildCopyDelta(
            builder, (IResource) element, (CopyArguments) fCopyArguments.get(i));
      }
    }
    getResourceModifications().buildDelta(builder);
  }

  @Override
  public RefactoringParticipant[] loadParticipants(
      RefactoringStatus status,
      RefactoringProcessor owner,
      String[] natures,
      SharableParticipants shared) {
    List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>();
    for (int i = 0; i < fCopies.size(); i++) {
      result.addAll(
          Arrays.asList(
              ParticipantManager.loadCopyParticipants(
                  status,
                  owner,
                  fCopies.get(i),
                  (CopyArguments) fCopyArguments.get(i),
                  fParticipantDescriptorFilter.get(i),
                  natures,
                  shared)));
    }
    result.addAll(
        Arrays.asList(getResourceModifications().getParticipants(status, owner, natures, shared)));
    return result.toArray(new RefactoringParticipant[result.size()]);
  }

  private void add(Object element, RefactoringArguments args, IParticipantDescriptorFilter filter) {
    Assert.isNotNull(element);
    Assert.isNotNull(args);
    fCopies.add(element);
    fCopyArguments.add(args);
    fParticipantDescriptorFilter.add(filter);
  }
}
