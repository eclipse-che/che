/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;

public class JavaSearchScopeFactory {

  public static final int JRE = IJavaSearchScope.SYSTEM_LIBRARIES;
  public static final int LIBS = IJavaSearchScope.APPLICATION_LIBRARIES;
  public static final int PROJECTS = IJavaSearchScope.REFERENCED_PROJECTS;
  public static final int SOURCES = IJavaSearchScope.SOURCES;

  public static final int ALL = JRE | LIBS | PROJECTS | SOURCES;
  public static final int NO_PROJ = JRE | LIBS | SOURCES;
  public static final int NO_JRE = LIBS | PROJECTS | SOURCES;
  public static final int NO_JRE_NO_PROJ = LIBS | PROJECTS | SOURCES;

  private static JavaSearchScopeFactory fgInstance;
  private final IJavaSearchScope EMPTY_SCOPE =
      SearchEngine.createJavaSearchScope(new IJavaElement[] {});

  private JavaSearchScopeFactory() {}

  public static JavaSearchScopeFactory getInstance() {
    if (fgInstance == null) fgInstance = new JavaSearchScopeFactory();
    return fgInstance;
  }

  //	public IWorkingSet[] queryWorkingSets() throws InterruptedException {
  //		Shell shell = JavaPlugin.getActiveWorkbenchShell();
  //		if (shell == null)
  //			return null;
  //		IWorkingSetSelectionDialog dialog =
  // PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(shell, true);
  //		if (dialog.open() != Window.OK) {
  //			throw new InterruptedException();
  //		}
  //
  //		IWorkingSet[] workingSets = dialog.getSelection();
  //		if (workingSets.length > 0)
  //			return workingSets;
  //		return null; // 'no working set' selected
  //	}

  public IJavaSearchScope createJavaSearchScope(IWorkingSet[] workingSets, boolean includeJRE) {
    return createJavaSearchScope(workingSets, includeJRE ? ALL : NO_JRE);
  }

  public IJavaSearchScope createJavaSearchScope(IWorkingSet[] workingSets, int includeMask) {
    if (workingSets == null || workingSets.length < 1) return EMPTY_SCOPE;

    Set<IJavaElement> javaElements = new HashSet<IJavaElement>(workingSets.length * 10);
    for (int i = 0; i < workingSets.length; i++) {
      IWorkingSet workingSet = workingSets[i];
      if (workingSet.isEmpty() && workingSet.isAggregateWorkingSet()) {
        return createWorkspaceScope(includeMask);
      }
      addJavaElements(javaElements, workingSet);
    }
    return createJavaSearchScope(javaElements, includeMask);
  }

  public IJavaSearchScope createJavaSearchScope(IWorkingSet workingSet, boolean includeJRE) {
    return createJavaSearchScope(workingSet, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaSearchScope(IWorkingSet workingSet, int includeMask) {
    Set<IJavaElement> javaElements = new HashSet<IJavaElement>(10);
    if (workingSet.isEmpty() && workingSet.isAggregateWorkingSet()) {
      return createWorkspaceScope(includeMask);
    }
    addJavaElements(javaElements, workingSet);
    return createJavaSearchScope(javaElements, includeMask);
  }

  public IJavaSearchScope createJavaSearchScope(IResource[] resources, boolean includeJRE) {
    return createJavaSearchScope(resources, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaSearchScope(IResource[] resources, int includeMask) {
    if (resources == null) return EMPTY_SCOPE;
    Set<IJavaElement> javaElements = new HashSet<IJavaElement>(resources.length);
    addJavaElements(javaElements, resources);
    return createJavaSearchScope(javaElements, includeMask);
  }

  public IJavaSearchScope createJavaSearchScope(ISelection selection, boolean includeJRE) {
    return createJavaSearchScope(selection, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaSearchScope(ISelection selection, int includeMask) {
    return createJavaSearchScope(getJavaElements(selection), includeMask);
  }

  public IJavaSearchScope createJavaProjectSearchScope(String[] projectNames, boolean includeJRE) {
    return createJavaProjectSearchScope(projectNames, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaProjectSearchScope(String[] projectNames, int includeMask) {
    ArrayList<IJavaElement> res = new ArrayList<IJavaElement>();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (int i = 0; i < projectNames.length; i++) {
      IJavaProject project = JavaCore.create(root.getProject(projectNames[i]));
      if (project.exists()) {
        res.add(project);
      }
    }
    return createJavaSearchScope(res, includeMask);
  }

  public IJavaSearchScope createJavaProjectSearchScope(IJavaProject project, boolean includeJRE) {
    return createJavaProjectSearchScope(project, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaProjectSearchScope(IJavaProject project, int includeMask) {
    return SearchEngine.createJavaSearchScope(
        new IJavaElement[] {project}, getSearchFlags(includeMask));
  }

  //	public IJavaSearchScope createJavaProjectSearchScope(IEditorInput editorInput, boolean
  // includeJRE) {
  //		return createJavaProjectSearchScope(editorInput, includeJRE ? ALL : NO_JRE);
  //	}
  //
  //	public IJavaSearchScope createJavaProjectSearchScope(IEditorInput editorInput, int includeMask)
  // {
  //		IJavaElement elem= JavaUI.getEditorInputJavaElement(editorInput);
  //		if (elem != null) {
  //			IJavaProject project= elem.getJavaProject();
  //			if (project != null) {
  //				return createJavaProjectSearchScope(project, includeMask);
  //			}
  //		}
  //		return EMPTY_SCOPE;
  //	}

  public String getWorkspaceScopeDescription(boolean includeJRE) {
    return includeJRE ? SearchMessages.WorkspaceScope : SearchMessages.WorkspaceScopeNoJRE;
  }

  public String getWorkspaceScopeDescription(int includeMask) {
    return getWorkspaceScopeDescription((includeMask & JRE) != 0);
  }

  public String getProjectScopeDescription(String[] projectNames, int includeMask) {
    if (projectNames.length == 0) {
      return SearchMessages.JavaSearchScopeFactory_undefined_projects;
    }
    boolean includeJRE = (includeMask & JRE) != 0;
    String scopeDescription;
    if (projectNames.length == 1) {
      String label =
          includeJRE
              ? SearchMessages.EnclosingProjectScope
              : SearchMessages.EnclosingProjectScopeNoJRE;
      scopeDescription =
          Messages.format(label, BasicElementLabels.getJavaElementName(projectNames[0]));
    } else if (projectNames.length == 2) {
      String label =
          includeJRE
              ? SearchMessages.EnclosingProjectsScope2
              : SearchMessages.EnclosingProjectsScope2NoJRE;
      scopeDescription =
          Messages.format(
              label,
              new String[] {
                BasicElementLabels.getJavaElementName(projectNames[0]),
                BasicElementLabels.getJavaElementName(projectNames[1])
              });
    } else {
      String label =
          includeJRE
              ? SearchMessages.EnclosingProjectsScope
              : SearchMessages.EnclosingProjectsScopeNoJRE;
      scopeDescription =
          Messages.format(
              label,
              new String[] {
                BasicElementLabels.getJavaElementName(projectNames[0]),
                BasicElementLabels.getJavaElementName(projectNames[1])
              });
    }
    return scopeDescription;
  }

  public String getProjectScopeDescription(IJavaProject project, boolean includeJRE) {
    if (includeJRE) {
      return Messages.format(
          SearchMessages.ProjectScope,
          BasicElementLabels.getJavaElementName(project.getElementName()));
    } else {
      return Messages.format(
          SearchMessages.ProjectScopeNoJRE,
          BasicElementLabels.getJavaElementName(project.getElementName()));
    }
  }

  //	public String getProjectScopeDescription(IEditorInput editorInput, boolean includeJRE) {
  //		IJavaElement elem= JavaUI.getEditorInputJavaElement(editorInput);
  //		if (elem != null) {
  //			IJavaProject project= elem.getJavaProject();
  //			if (project != null) {
  //				return getProjectScopeDescription(project, includeJRE);
  //			}
  //		}
  //		return Messages.format(SearchMessages.ProjectScope, "");  //$NON-NLS-1$
  //	}

  public String getHierarchyScopeDescription(IType type) {
    return Messages.format(
        SearchMessages.HierarchyScope,
        new String[] {JavaElementLabels.getElementLabel(type, JavaElementLabels.ALL_DEFAULT)});
  }

  public String getSelectionScopeDescription(IJavaElement[] javaElements, int includeMask) {
    return getSelectionScopeDescription(javaElements, (includeMask & JRE) != 0);
  }

  public String getSelectionScopeDescription(IJavaElement[] javaElements, boolean includeJRE) {
    if (javaElements.length == 0) {
      return SearchMessages.JavaSearchScopeFactory_undefined_selection;
    }
    String scopeDescription;
    if (javaElements.length == 1) {
      String label =
          includeJRE
              ? SearchMessages.SingleSelectionScope
              : SearchMessages.SingleSelectionScopeNoJRE;
      scopeDescription =
          Messages.format(
              label,
              JavaElementLabels.getElementLabel(javaElements[0], JavaElementLabels.ALL_DEFAULT));
    } else if (javaElements.length == 2) {
      String label =
          includeJRE
              ? SearchMessages.DoubleSelectionScope
              : SearchMessages.DoubleSelectionScopeNoJRE;
      scopeDescription =
          Messages.format(
              label,
              new String[] {
                JavaElementLabels.getElementLabel(javaElements[0], JavaElementLabels.ALL_DEFAULT),
                JavaElementLabels.getElementLabel(javaElements[1], JavaElementLabels.ALL_DEFAULT)
              });
    } else {
      String label =
          includeJRE ? SearchMessages.SelectionScope : SearchMessages.SelectionScopeNoJRE;
      scopeDescription =
          Messages.format(
              label,
              new String[] {
                JavaElementLabels.getElementLabel(javaElements[0], JavaElementLabels.ALL_DEFAULT),
                JavaElementLabels.getElementLabel(javaElements[1], JavaElementLabels.ALL_DEFAULT)
              });
    }
    return scopeDescription;
  }

  //	public String getWorkingSetScopeDescription(IWorkingSet[] workingSets, int includeMask) {
  //		return getWorkingSetScopeDescription(workingSets, (includeMask & JRE) != 0);
  //	}

  //	public String getWorkingSetScopeDescription(IWorkingSet[] workingSets, boolean includeJRE) {
  //		if (workingSets.length == 0) {
  //			return SearchMessages.JavaSearchScopeFactory_undefined_workingsets;
  //		}
  //		if (workingSets.length == 1) {
  //			String label= includeJRE ? SearchMessages.SingleWorkingSetScope :
  // SearchMessages.SingleWorkingSetScopeNoJRE;
  //			return Messages.format(label, BasicElementLabels.getWorkingSetLabel(workingSets[0]));
  //		}
  //		Arrays.sort(workingSets, new WorkingSetComparator());
  //		if (workingSets.length == 2) {
  //			String label= includeJRE ? SearchMessages.DoubleWorkingSetScope :
  // SearchMessages.DoubleWorkingSetScopeNoJRE;
  //			return Messages.format(label, new
  // String[]{BasicElementLabels.getWorkingSetLabel(workingSets[0]),
  //													   BasicElementLabels.getWorkingSetLabel(workingSets[1])});
  //		}
  //		String label= includeJRE ? SearchMessages.WorkingSetsScope :
  // SearchMessages.WorkingSetsScopeNoJRE;
  //		return Messages.format(label, new
  // String[]{BasicElementLabels.getWorkingSetLabel(workingSets[0]),
  //												   BasicElementLabels.getWorkingSetLabel(workingSets[1])});
  //	}

  public IProject[] getProjects(IJavaSearchScope scope) {
    IPath[] paths = scope.enclosingProjectsAndJars();
    HashSet<IResource> temp = new HashSet<IResource>();
    for (int i = 0; i < paths.length; i++) {
      IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(paths[i]);
      if (resource != null && resource.getType() == IResource.PROJECT) temp.add(resource);
    }
    return temp.toArray(new IProject[temp.size()]);
  }

  public IJavaElement[] getJavaElements(ISelection selection) {
    if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
      return getJavaElements(((IStructuredSelection) selection).toArray());
    } else {
      return new IJavaElement[0];
    }
  }

  private IJavaElement[] getJavaElements(Object[] elements) {
    if (elements.length == 0) return new IJavaElement[0];

    Set<IJavaElement> result = new HashSet<IJavaElement>(elements.length);
    for (int i = 0; i < elements.length; i++) {
      Object selectedElement = elements[i];
      if (selectedElement instanceof IJavaElement) {
        addJavaElements(result, (IJavaElement) selectedElement);
      } else if (selectedElement instanceof IResource) {
        addJavaElements(result, (IResource) selectedElement);
      } else if (selectedElement instanceof LogicalPackage) {
        addJavaElements(result, (LogicalPackage) selectedElement);
      } else if (selectedElement instanceof IWorkingSet) {
        IWorkingSet ws = (IWorkingSet) selectedElement;
        addJavaElements(result, ws);
      } else if (selectedElement instanceof IAdaptable) {
        IResource resource = (IResource) ((IAdaptable) selectedElement).getAdapter(IResource.class);
        if (resource != null) addJavaElements(result, resource);
      }
    }
    return result.toArray(new IJavaElement[result.size()]);
  }

  public IJavaSearchScope createJavaSearchScope(IJavaElement[] javaElements, boolean includeJRE) {
    return createJavaSearchScope(javaElements, includeJRE ? NO_PROJ : NO_JRE_NO_PROJ);
  }

  public IJavaSearchScope createJavaSearchScope(IJavaElement[] javaElements, int includeMask) {
    if (javaElements.length == 0) return EMPTY_SCOPE;
    return SearchEngine.createJavaSearchScope(javaElements, getSearchFlags(includeMask));
  }

  private IJavaSearchScope createJavaSearchScope(
      Collection<IJavaElement> javaElements, int includeMask) {
    if (javaElements.isEmpty()) return EMPTY_SCOPE;
    IJavaElement[] elementArray = javaElements.toArray(new IJavaElement[javaElements.size()]);
    return SearchEngine.createJavaSearchScope(elementArray, getSearchFlags(includeMask));
  }

  private static int getSearchFlags(int includeMask) {
    return includeMask;
  }

  private void addJavaElements(Set<IJavaElement> javaElements, IResource[] resources) {
    for (int i = 0; i < resources.length; i++) addJavaElements(javaElements, resources[i]);
  }

  private void addJavaElements(Set<IJavaElement> javaElements, IResource resource) {
    IJavaElement javaElement = (IJavaElement) resource.getAdapter(IJavaElement.class);
    if (javaElement == null)
      // not a Java resource
      return;

    if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
      // add other possible package fragments
      try {
        addJavaElements(javaElements, ((IFolder) resource).members());
      } catch (CoreException ex) {
        // don't add elements
      }
    }

    javaElements.add(javaElement);
  }

  private void addJavaElements(Set<IJavaElement> javaElements, IJavaElement javaElement) {
    javaElements.add(javaElement);
  }

  private void addJavaElements(Set<IJavaElement> javaElements, IWorkingSet workingSet) {
    if (workingSet == null) return;

    if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
      try {
        IJavaProject[] projects =
            JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
        javaElements.addAll(Arrays.asList(projects));
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
      return;
    }

    IAdaptable[] elements = workingSet.getElements();
    for (int i = 0; i < elements.length; i++) {
      IJavaElement javaElement = (IJavaElement) elements[i].getAdapter(IJavaElement.class);
      if (javaElement != null) {
        addJavaElements(javaElements, javaElement);
        continue;
      }
      IResource resource = (IResource) elements[i].getAdapter(IResource.class);
      if (resource != null) {
        addJavaElements(javaElements, resource);
      }

      // else we don't know what to do with it, ignore.
    }
  }

  private void addJavaElements(Set<IJavaElement> javaElements, LogicalPackage selectedElement) {
    IPackageFragment[] packages = selectedElement.getFragments();
    for (int i = 0; i < packages.length; i++) addJavaElements(javaElements, packages[i]);
  }

  public IJavaSearchScope createWorkspaceScope(boolean includeJRE) {
    return createWorkspaceScope(includeJRE ? ALL : NO_JRE);
  }

  public IJavaSearchScope createWorkspaceScope(int includeMask) {
    if ((includeMask & NO_PROJ) != NO_PROJ) {
      try {
        IJavaProject[] projects =
            JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
        return SearchEngine.createJavaSearchScope(projects, getSearchFlags(includeMask));
      } catch (JavaModelException e) {
        // ignore, use workspace scope instead
      }
    }
    return SearchEngine.createWorkspaceScope();
  }

  public boolean isInsideJRE(IJavaElement element) {
    IPackageFragmentRoot root =
        (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (root != null) {
      try {
        IClasspathEntry entry = root.getRawClasspathEntry();
        if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
          IClasspathContainer container =
              JavaCore.getClasspathContainer(entry.getPath(), root.getJavaProject());
          return container != null && container.getKind() == IClasspathContainer.K_DEFAULT_SYSTEM;
        }
        return false;
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return true; // include JRE in doubt
  }
}
