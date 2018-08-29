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
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

public class ParentChecker {
  private IResource[] fResources;
  private IJavaElement[] fJavaElements;
  private IJarEntryResource[] fJarResources;

  public ParentChecker(IResource[] resources, IJavaElement[] javaElements) {
    this(resources, javaElements, new IJarEntryResource[0]);
  }

  public ParentChecker(
      IResource[] resources, IJavaElement[] javaElements, IJarEntryResource[] jarResources) {
    Assert.isNotNull(resources);
    Assert.isNotNull(javaElements);
    Assert.isNotNull(jarResources);
    fResources = resources;
    fJavaElements = javaElements;
    fJarResources = jarResources;
  }

  public boolean haveCommonParent() {
    return getCommonParent() != null;
  }

  public Object getCommonParent() {
    if (fJavaElements.length == 0 && fResources.length == 0 && fJarResources.length == 0)
      return null;
    if (!resourcesHaveCommonParent()
        || !javaElementsHaveCommonParent()
        || !jarResourcesHaveCommonParent()) return null;
    if (fJavaElements.length == 0 && fResources.length == 0) return getCommonJarResourceParent();
    if (fJavaElements.length == 0 && fJarResources.length == 0) {
      IResource commonResourceParent = getCommonResourceParent();
      Assert.isNotNull(commonResourceParent);
      IJavaElement convertedToJava = JavaCore.create(commonResourceParent);
      if (convertedToJava != null && convertedToJava.exists()) return convertedToJava;
      else return commonResourceParent;
    }
    if (fResources.length == 0 && fJarResources.length == 0) return getCommonJavaElementParent();

    IJavaElement convertedToJava = null;
    IJavaElement commonJavaElementParent = null;
    Object commonJarResourcesParent = null;
    Object commonParent = null;
    if (fResources.length != 0) {
      IResource commonResourceParent = getCommonResourceParent();
      Assert.isNotNull(commonResourceParent);
      convertedToJava = JavaCore.create(commonResourceParent);
      if (convertedToJava == null || !convertedToJava.exists()) return null;
    }
    if (fJavaElements.length != 0) {
      commonJavaElementParent = getCommonJavaElementParent();
      Assert.isNotNull(commonJavaElementParent);
      if (convertedToJava != null && !commonJavaElementParent.equals(convertedToJava)) return null;
    }
    commonParent = convertedToJava == null ? commonJavaElementParent : convertedToJava;
    if (fJarResources.length != 0) {
      commonJarResourcesParent = getCommonJarResourceParent();
      Assert.isNotNull(commonJarResourcesParent);
      if (!commonJarResourcesParent.equals(commonParent)) return null;
    }
    return commonParent;
  }

  /**
   * Return the common parent for the jar resources.
   *
   * @return the common parent for the jar resources
   * @since 3.6
   */
  private Object getCommonJarResourceParent() {
    Assert.isNotNull(fJarResources);
    Assert.isTrue(fJarResources.length > 0); // safe - checked before
    return fJarResources[0].getParent();
  }

  private IJavaElement getCommonJavaElementParent() {
    Assert.isNotNull(fJavaElements);
    Assert.isTrue(fJavaElements.length > 0); // safe - checked before
    return fJavaElements[0].getParent();
  }

  private IResource getCommonResourceParent() {
    Assert.isNotNull(fResources);
    Assert.isTrue(fResources.length > 0); // safe - checked before
    return fResources[0].getParent();
  }

  private boolean javaElementsHaveCommonParent() {
    if (fJavaElements.length == 0) return true;
    IJavaElement firstParent = fJavaElements[0].getParent();
    Assert.isNotNull(firstParent); // this should never happen
    for (int i = 1; i < fJavaElements.length; i++) {
      if (!firstParent.equals(fJavaElements[i].getParent())) return false;
    }
    return true;
  }

  private boolean resourcesHaveCommonParent() {
    if (fResources.length == 0) return true;
    IResource firstParent = fResources[0].getParent();
    Assert.isNotNull(firstParent);
    for (int i = 1; i < fResources.length; i++) {
      if (!firstParent.equals(fResources[i].getParent())) return false;
    }
    return true;
  }

  /**
   * Checks whether the jar resources have the same parent.
   *
   * @return <code>true</code> if the jar resources have the same parent, <code>false</code>
   *     otherwise
   * @since 3.6
   */
  private boolean jarResourcesHaveCommonParent() {
    if (fJarResources.length == 0) return true;
    Object firstParent = fJarResources[0].getParent();
    Assert.isNotNull(firstParent);
    for (int i = 1; i < fJarResources.length; i++) {
      if (!firstParent.equals(fJarResources[i].getParent())) return false;
    }
    return true;
  }

  public IResource[] getResources() {
    return fResources;
  }

  public IJavaElement[] getJavaElements() {
    return fJavaElements;
  }

  public void removeElementsWithAncestorsOnList(boolean removeOnlyJavaElements) {
    if (!removeOnlyJavaElements) {
      removeResourcesDescendantsOfResources();
      removeResourcesDescendantsOfJavaElements();
    }
    removeJavaElementsDescendantsOfJavaElements();
    //		removeJavaElementsChildrenOfResources(); //this case is covered by removeUnconfirmedArchives
  }

  private void removeResourcesDescendantsOfJavaElements() {
    List<IResource> subResources = new ArrayList<IResource>(3);
    for (int i = 0; i < fResources.length; i++) {
      IResource subResource = fResources[i];
      for (int j = 0; j < fJavaElements.length; j++) {
        IJavaElement superElements = fJavaElements[j];
        if (isDescendantOf(subResource, superElements)) subResources.add(subResource);
      }
    }
    removeFromSetToDelete(subResources.toArray(new IResource[subResources.size()]));
  }

  private void removeJavaElementsDescendantsOfJavaElements() {
    List<IJavaElement> subElements = new ArrayList<IJavaElement>(3);
    for (int i = 0; i < fJavaElements.length; i++) {
      IJavaElement subElement = fJavaElements[i];
      for (int j = 0; j < fJavaElements.length; j++) {
        IJavaElement superElement = fJavaElements[j];
        if (isDescendantOf(subElement, superElement)) subElements.add(subElement);
      }
    }
    removeFromSetToDelete(subElements.toArray(new IJavaElement[subElements.size()]));
  }

  private void removeResourcesDescendantsOfResources() {
    List<IResource> subResources = new ArrayList<IResource>(3);
    for (int i = 0; i < fResources.length; i++) {
      IResource subResource = fResources[i];
      for (int j = 0; j < fResources.length; j++) {
        IResource superResource = fResources[j];
        if (isDescendantOf(subResource, superResource)) subResources.add(subResource);
      }
    }
    removeFromSetToDelete(subResources.toArray(new IResource[subResources.size()]));
  }

  public static boolean isDescendantOf(IResource subResource, IJavaElement superElement) {
    IResource parent = subResource.getParent();
    while (parent != null) {
      IJavaElement el = JavaCore.create(parent);
      if (el != null && el.exists() && el.equals(superElement)) return true;
      parent = parent.getParent();
    }
    return false;
  }

  public static boolean isDescendantOf(IJavaElement subElement, IJavaElement superElement) {
    if (subElement.equals(superElement)) return false;
    IJavaElement parent = subElement.getParent();
    while (parent != null) {
      if (parent.equals(superElement)) return true;
      parent = parent.getParent();
    }
    return false;
  }

  public static boolean isDescendantOf(IResource subResource, IResource superResource) {
    return !subResource.equals(superResource)
        && superResource.getFullPath().isPrefixOf(subResource.getFullPath());
  }

  private void removeFromSetToDelete(IResource[] resourcesToNotDelete) {
    fResources = ReorgUtils.setMinus(fResources, resourcesToNotDelete);
  }

  private void removeFromSetToDelete(IJavaElement[] elementsToNotDelete) {
    fJavaElements = ReorgUtils.setMinus(fJavaElements, elementsToNotDelete);
  }
}
