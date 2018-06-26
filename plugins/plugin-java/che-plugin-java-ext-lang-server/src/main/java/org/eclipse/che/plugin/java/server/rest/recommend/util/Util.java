package org.eclipse.che.plugin.java.server.rest.recommend.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.visitor.OverrideVisitor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class Util {

  public static IProject[] getIProjects() {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    return ResourcesPlugin.getWorkspace().getRoot().getProjects();
  }

  public static IJavaProject getIJavaProject(String projectName) {
    IJavaProject javaProject = null;
    IProject[] projects = getIProjects();
    for (IProject project : projects) {
      if (project.getName().equals(projectName)) {
        javaProject = JavaCore.create(project);
      }
    }
    return javaProject;
  }

  public static List<IPackageFragmentRoot> getSourceCodeFolder(String projectName) {
    List<IPackageFragmentRoot> roots = new ArrayList<IPackageFragmentRoot>();
    IJavaProject javaProject = getIJavaProject(projectName);

    if (javaProject != null && javaProject.exists()) {
      try {
        for (IPackageFragmentRoot pr : javaProject.getPackageFragmentRoots()) {
          if (pr.getKind() == IPackageFragmentRoot.K_SOURCE) {
            roots.add(pr);
          }
        }
      } catch (JavaModelException e) {
        e.printStackTrace();
      }
    }

    return roots;
  }

  public static List<IPackageFragment> getIPackages(String projectName) {
    List<IPackageFragment> iPackages = new ArrayList<IPackageFragment>();
    List<IPackageFragmentRoot> roots = getSourceCodeFolder(projectName);
    try {
      if (roots.size() == 0) return iPackages;
      for (IPackageFragmentRoot root : roots) {
        if (root.getChildren() == null) return iPackages;
        for (IJavaElement iPack : root.getChildren()) {
          if (iPack instanceof IPackageFragment) {
            iPackages.add((IPackageFragment) iPack);
          }
        }
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    return iPackages;
  }

  public static List<ICompilationUnit> getCompilationUnits(IPackageFragment iPackage) {
    List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();
    try {
      for (ICompilationUnit icu : iPackage.getCompilationUnits()) {
        compilationUnits.add(icu);
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    return compilationUnits;
  }

  public static CompilationUnit createCompilationUnit(ICompilationUnit cu) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(cu);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }

  public static ICompilationUnit getCompilationUnit(
      String projectName, String packageName, String typeName) {
    ICompilationUnit compilationUnit = null;

    List<IPackageFragment> packageFragments = getIPackages(projectName);
    for (IPackageFragment packageFragment : packageFragments) {
      if (packageFragment.getElementName().equals(packageName)) {
        List<ICompilationUnit> compilationUnits = getCompilationUnits(packageFragment);
        for (ICompilationUnit cU : compilationUnits) {
          if (cU.getElementName()
              .substring(0, cU.getElementName().lastIndexOf("."))
              .equals(typeName)) {
            compilationUnit = cU;
          }
        }
      }
    }
    return compilationUnit;
  }

  public static ICompilationUnit getCompilationUnit(DataModel renameData) {
    ICompilationUnit compilationUnit = null;

    String projectName = renameData.getProjectName();
    String packageName = renameData.getPackageName();
    String typeName = renameData.getTypeName();
    List<IPackageFragment> packageFragments = getIPackages(projectName);

    for (IPackageFragment packageFragment : packageFragments) {
      if (packageFragment.getElementName().equals(packageName)) {
        List<ICompilationUnit> compilationUnits = getCompilationUnits(packageFragment);

        for (ICompilationUnit cU : compilationUnits) {

          if (cU.getElementName()
              .substring(0, cU.getElementName().lastIndexOf("."))
              .equals(typeName)) {
            compilationUnit = cU;
          }
        }
      }
    }

    return compilationUnit;
  }

  public static ICompilationUnit getCompilationUnit1(DataModel renameData) {
    ICompilationUnit compilationUnit = null;
    String projectName = renameData.getProjectName();
    String packageName = renameData.getPackageName();
    String typeName = renameData.getTypeName();
    // String typeName = renameData.subsequentName;
    List<IPackageFragment> packageFragments = getIPackages(projectName);
    for (IPackageFragment packageFragment : packageFragments) {
      if (packageFragment.getElementName().equals(packageName)) {
        List<ICompilationUnit> compilationUnits = getCompilationUnits(packageFragment);
        for (ICompilationUnit cU : compilationUnits) {
          if (cU.getElementName()
              .substring(0, cU.getElementName().lastIndexOf("."))
              .equals(typeName)) {
            compilationUnit = cU;
          }
        }
      }
    }
    return compilationUnit;
  }

  public static List<ICompilationUnit> getAllCompilationUnit(DataModel renameData) {
    List<ICompilationUnit> iCompilationUnits = new ArrayList<ICompilationUnit>();
    String projectName = renameData.getProjectName();
    List<IPackageFragment> packageFragments = getIPackages(projectName);
    for (IPackageFragment packageFragment : packageFragments) {
      List<ICompilationUnit> compilationUnits = getCompilationUnits(packageFragment);
      iCompilationUnits.addAll(compilationUnits);
    }
    return iCompilationUnits;
  }

  public static IMethod getIMethod(DataModel renameData) {
    ICompilationUnit icu = getCompilationUnit(renameData);
    if (icu == null) return null;

    try {
      IType[] types = icu.getAllTypes();
      for (IType type : types) {
        if (type.getElementType() == IJavaElement.TYPE) {
          if (type != null) {
            IMethod[] methods = type.getMethods();
            for (IMethod method : methods) {
              if (renameData.getMethodName().equals(method.getElementName())) return method;
            }
          }
        }
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static boolean isContain(
      List<DataModel> renameDatas,
      String packageName,
      String typeName,
      String methodName,
      String candidateName) {
    for (DataModel renameData : renameDatas) {
      if (renameData.getOriginalName().equals(candidateName)
          && renameData.getPackageName().equals(packageName)
          && renameData.getTypeName().equals(typeName)
          && renameData.getMethodName().equals(methodName)) return true;
    }
    return false;
  }

  public static boolean isContain(ITypeBinding[] superInterfaces, ITypeBinding typeBinding) {
    if (superInterfaces == null || superInterfaces.length == 0) return false;
    for (int i = 0; i < superInterfaces.length; i++) {
      if (typeBinding.getQualifiedName().equals(superInterfaces[i].getQualifiedName())) return true;
    }
    return false;
  }

  public static boolean isContain(
      ITypeBinding[] superInterfaces1, ITypeBinding[] superInterfaces2) {
    if (superInterfaces1 == null || superInterfaces1.length == 0) return false;
    if (superInterfaces2 == null || superInterfaces2.length == 0) return false;
    for (int i = 0; i < superInterfaces1.length; i++) {
      for (int j = 0; j < superInterfaces2.length; j++) {
        if (superInterfaces2[j].getQualifiedName().equals(superInterfaces1[i].getQualifiedName()))
          return true;
      }
    }
    return false;
  }

  public static boolean isOverriding(IMethodBinding iMethod, DataModel ignoreRenameData) {

    if (ignoreRenameData.getRefactorType().equals("method")) {
      ICompilationUnit compilationUnit = getCompilationUnit(ignoreRenameData);
      if (compilationUnit == null) return false;
      CompilationUnit unit = createCompilationUnit(compilationUnit);
      if (unit == null) return false;
      OverrideVisitor visitor = new OverrideVisitor();
      visitor.setMethodName(ignoreRenameData.getMethodName());
      unit.accept(visitor);
      IMethodBinding iMethodbinding = visitor.getiMethodbinding();
      if (iMethodbinding == null) return false;

      ArrayList<IType> iMethodbindingSuperTypes = getAllSubTypes(ignoreRenameData, iMethodbinding);

      ArrayList<IType> iMethodSuperTypes = getAllSubTypes(ignoreRenameData, iMethod);

      String iMethodName =
          iMethod
              .getJavaElement()
              .toString()
              .substring(0, iMethod.getJavaElement().toString().indexOf("{") - 1);

      String iMethodbindingName =
          iMethodbinding
              .getJavaElement()
              .toString()
              .substring(0, iMethodbinding.getJavaElement().toString().indexOf("{") - 1);

      if (iMethodName.equals(iMethodbindingName)) {
        if (isSupOrSub(iMethodbinding, iMethodSuperTypes)
            || isSupOrSub(iMethod, iMethodbindingSuperTypes)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isSupOrSub(
      IMethodBinding iMethodbinding, ArrayList<IType> iMethodSuperTypes) {

    ITypeBinding iTypeBinding = iMethodbinding.getDeclaringClass();
    IType iType = (IType) iTypeBinding.getJavaElement();

    for (IType iMethodSuperType : iMethodSuperTypes) {
      if (iMethodSuperType.getFullyQualifiedName().equals(iType.getFullyQualifiedName()))
        return true;
    }

    return false;
  }

  public static ArrayList<IType> getAllSubTypes(
      DataModel ignoreRenameData, IMethodBinding iMethodbinding) {
    final ArrayList<IType> subTypes = new ArrayList<IType>();

    ArrayList<IType> subType = getSubType(ignoreRenameData, iMethodbinding);
    subTypes.addAll(subType);

    while (subType != null && subType.size() != 0) {
      subType = getSubTypes(ignoreRenameData, subType);
      subTypes.addAll(subType);
    }

    return subTypes;
  }

  public static ArrayList<IType> getSubTypes(DataModel ignoreRenameData, ArrayList<IType> subType) {
    final ArrayList<IType> subTypes = new ArrayList<IType>();

    String projectName = ignoreRenameData.getProjectName();
    IJavaProject javaProject = getIJavaProject(projectName);

    for (IType iType : subType) {
      SearchPattern pattern =
          SearchPattern.createPattern(iType, IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE);
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject});
      SearchRequestor requestor =
          new SearchRequestor() {
            public void acceptSearchMatch(SearchMatch match) {
              Object obj = match.getElement();
              if (obj instanceof IType) {
                subTypes.add((IType) obj);
              }
            }
          };

      SearchParticipant[] participants =
          new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
      SearchEngine searchEngine = new SearchEngine();
      try {
        searchEngine.search(pattern, participants, scope, requestor, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

    return subTypes;
  }

  public static ArrayList<IType> getSubType(
      DataModel ignoreRenameData, IMethodBinding iMethodbinding) {

    final ArrayList<IType> subType = new ArrayList<IType>();

    String projectName = ignoreRenameData.getProjectName();
    IJavaProject javaProject = getIJavaProject(projectName);

    ITypeBinding iTypeBinding = iMethodbinding.getDeclaringClass();
    IType iType = (IType) iTypeBinding.getJavaElement();

    SearchPattern pattern =
        SearchPattern.createPattern(iType, IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE);
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject});
    SearchRequestor requestor =
        new SearchRequestor() {
          public void acceptSearchMatch(SearchMatch match) {
            Object obj = match.getElement();
            if (obj instanceof IType) {
              subType.add((IType) obj);
            }
          }
        };

    SearchParticipant[] participants =
        new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
    SearchEngine searchEngine = new SearchEngine();
    try {
      searchEngine.search(pattern, participants, scope, requestor, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return subType;
  }
}
