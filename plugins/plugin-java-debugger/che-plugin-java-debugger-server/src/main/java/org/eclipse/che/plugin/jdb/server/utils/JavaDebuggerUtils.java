/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server.utils;

import static java.lang.String.format;
import static org.eclipse.jdt.core.search.SearchEngine.createWorkspaceScope;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;

/**
 * Class uses for find and handle important information from the Java Model.
 *
 * @author Alexander Andrienko
 */
public class JavaDebuggerUtils {

  private static final JavaModel MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

  /**
   * Returns Location for current debugger resource.
   *
   * @param location location type from JVM
   * @throws DebuggerException in case {@link org.eclipse.jdt.core.JavaModelException} or if Java
   *     {@link org.eclipse.jdt.core.IType} was not find
   */
  public Location getLocation(com.sun.jdi.Location location) throws DebuggerException {
    String fqn = location.declaringType().name();

    List<IType> types;
    try {
      Pair<char[][], char[][]> fqnPair = prepareFqnToSearch(fqn);

      types = findTypeByFqn(fqnPair.first, fqnPair.second, createWorkspaceScope());
    } catch (JavaModelException e) {
      throw new DebuggerException("Can't find class models by fqn: " + fqn, e);
    }

    if (types.isEmpty()) {
      throw new DebuggerException("Type with fully qualified name: " + fqn + " was not found");
    }

    IType type = types.get(0); // TODO we need handle few result! It's temporary solution.
    String typeProjectPath = type.getJavaProject().getPath().toOSString();
    if (type.isBinary()) {
      IClassFile classFile = type.getClassFile();
      int libId = classFile.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode();
      return new LocationImpl(fqn, location.lineNumber(), true, libId, typeProjectPath, null, -1);
    } else {
      ICompilationUnit compilationUnit = type.getCompilationUnit();
      typeProjectPath = type.getJavaProject().getPath().toOSString();
      String resourcePath = compilationUnit.getPath().toOSString();
      return new LocationImpl(
          resourcePath, location.lineNumber(), false, -1, typeProjectPath, null, -1);
    }
  }

  private Pair<char[][], char[][]> prepareFqnToSearch(@NotNull String fqn) {
    String outerClassFqn = extractOuterClassFqn(fqn);
    int lastDotIndex = outerClassFqn.trim().lastIndexOf('.');

    char[][] packages;
    char[][] names;
    if (lastDotIndex == -1) {
      packages = new char[0][];
      names = new char[][] {outerClassFqn.toCharArray()};
    } else {
      String packageLine = fqn.substring(0, lastDotIndex);
      packages = new char[][] {packageLine.toCharArray()};

      String nameLine = fqn.substring(lastDotIndex + 1, outerClassFqn.length());
      names = new char[][] {nameLine.toCharArray()};
    }
    return new Pair<>(packages, names);
  }

  private String extractOuterClassFqn(String fqn) {
    // handle fqn in case nested classes
    if (fqn.contains("$")) {
      return fqn.substring(0, fqn.indexOf("$"));
    }
    // handle fqn in case lambda expressions
    if (fqn.contains("$$")) {
      return fqn.substring(0, fqn.indexOf("$$"));
    }
    return fqn;
  }

  private List<IType> findTypeByFqn(char[][] packages, char[][] names, IJavaSearchScope scope)
      throws JavaModelException {
    List<IType> result = new ArrayList<>();

    SearchEngine searchEngine = new SearchEngine();
    searchEngine.searchAllTypeNames(
        packages,
        names,
        scope,
        new TypeNameMatchRequestor() {
          @Override
          public void acceptTypeNameMatch(TypeNameMatch typeNameMatch) {
            result.add(typeNameMatch.getType());
          }
        },
        IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
        new NullProgressMonitor());
    return result;
  }

  /**
   * Return nested class fqn if line with number {@code lineNumber} contains such element, otherwise
   * return outer class fqn.
   *
   * @throws DebuggerException
   */
  public String findFqnByPosition(Location location) throws DebuggerException {
    IPath path = Path.fromOSString(location.getTarget());
    IJavaProject project = getJavaProject(path);
    if (project == null) {
      if (location.getResourceProjectPath() != null) {
        project = MODEL.getJavaProject(location.getResourceProjectPath());
      } else {
        return location.getTarget();
      }
    }

    String fqn = null;
    for (int i = path.segmentCount(); i > 0; i--) {
      try {
        IClasspathEntry classpathEntry =
            ((JavaProject) project).getClasspathEntryFor(path.removeLastSegments(i));

        if (classpathEntry != null) {
          fqn =
              path.removeFirstSegments(path.segmentCount() - i)
                  .removeFileExtension()
                  .toString()
                  .replace("/", ".");
          break;
        }
      } catch (JavaModelException e) {
        return location.getTarget();
      }
    }

    if (fqn == null) {
      return location.getTarget();
    }

    IType outerClass;
    IJavaElement iMember;
    try {
      outerClass = project.findType(fqn);

      if (outerClass == null) {
        return location.getTarget();
      }

      String source;
      if (outerClass.isBinary()) {
        IClassFile classFile = outerClass.getClassFile();
        source = classFile.getSource();
      } else {
        ICompilationUnit unit = outerClass.getCompilationUnit();
        source = unit.getSource();
      }

      Document document = new Document(source);
      IRegion region = document.getLineInformation(location.getLineNumber());
      int start = region.getOffset();
      int end = start + region.getLength();

      iMember = binSearch(outerClass, start, end);
    } catch (JavaModelException e) {
      throw new DebuggerException(
          format(
              "Unable to find source for class with fqn '%s' in the project '%s'",
              location.getTarget(), project),
          e);
    } catch (BadLocationException e) {
      throw new DebuggerException("Unable to calculate breakpoint location", e);
    }

    if (iMember instanceof IType) {
      return ((IType) iMember).getFullyQualifiedName();
    }

    if (iMember != null) {
      fqn = ((IMember) iMember).getDeclaringType().getFullyQualifiedName();
      while (!(iMember instanceof CompilationUnit)) {
        if (iMember instanceof SourceType
            && !((SourceType) iMember).isAnonymous()
            && iMember.getParent() instanceof SourceMethod) {

          fqn = fqn.replace("$" + iMember.getElementName(), "$1" + iMember.getElementName());
        }

        iMember = iMember.getParent();
      }

      return fqn;
    }

    throw new DebuggerException("Unable to calculate breakpoint location");
  }

  private IJavaProject getJavaProject(IPath path) throws DebuggerException {
    IJavaProject project = null;
    outer:
    for (int i = 1; i < path.segmentCount(); i++) {
      IPath projectPath = path.removeLastSegments(i);
      try {
        for (IJavaProject p : MODEL.getJavaProjects()) {
          if (p.getPath().equals(projectPath)) {
            project = p;
            break outer;
          }
        }
      } catch (JavaModelException e) {
        throw new DebuggerException(e.getMessage(), e);
      }
    }
    return project;
  }

  /**
   * Searches the given source range of the container for a member that is not the same as the given
   * type.
   *
   * @param type the {@link IType}
   * @param start the starting position
   * @param end the ending position
   * @return the {@link IMember} from the given start-end range
   * @throws JavaModelException if there is a problem with the backing Java model
   */
  @Nullable
  private IMember binSearch(IType type, int start, int end) throws JavaModelException {
    IJavaElement je = getElementAt(type, start);
    if (je != null && !je.equals(type)) {
      return asMember(je);
    }
    if (end > start) {
      je = getElementAt(type, end);
      if (je != null && !je.equals(type)) {
        return asMember(je);
      }
      int mid = ((end - start) / 2) + start;
      if (mid > start) {
        je = binSearch(type, start + 1, mid);
        if (je == null) {
          je = binSearch(type, mid + 1, end - 1);
        }
        return asMember(je);
      }
    }
    return null;
  }

  /**
   * Returns the given Java element if it is an <code>IMember</code>, otherwise <code>null</code>.
   *
   * @param element Java element
   * @return the given element if it is a type member, otherwise <code>null</code>
   */
  @Nullable
  private static IMember asMember(IJavaElement element) {
    if (element instanceof IMember) {
      return (IMember) element;
    }
    return null;
  }

  /**
   * Returns the element at the given position in the given type
   *
   * @param type the {@link IType}
   * @param pos the position
   * @return the {@link IJavaElement} at the given position
   * @throws JavaModelException if there is a problem with the backing Java model
   */
  private static IJavaElement getElementAt(IType type, int pos) throws JavaModelException {
    if (type.isBinary()) {
      return type.getClassFile().getElementAt(pos);
    }
    return type.getCompilationUnit().getElementAt(pos);
  }
}
