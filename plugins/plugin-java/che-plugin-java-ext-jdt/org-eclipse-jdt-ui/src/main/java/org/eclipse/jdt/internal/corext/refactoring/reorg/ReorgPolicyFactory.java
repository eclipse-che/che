/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Carsten Pfeiffer
 * <carsten.pfeiffer@gebit.de> - [ccp] ReorgPolicies' canEnable() methods return true too often -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=303698
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.changes.CopyCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.CopyPackageChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.CopyPackageFragmentRootChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.CopyResourceChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.MovePackageChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.MovePackageFragmentRootChange;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.ICopyPolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory.JavaElementDestination;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory.ResourceDestination;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.ImportRewriteUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.Changes;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.QualifiedNameFinder;
import org.eclipse.jdt.internal.corext.refactoring.util.QualifiedNameSearchResult;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.JavaElementResourceMapping;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;

public final class ReorgPolicyFactory {

  private static final class ActualSelectionComputer {

    private final IJavaElement[] fJavaElements;

    private final IResource[] fResources;

    public ActualSelectionComputer(IJavaElement[] javaElements, IResource[] resources) {
      fJavaElements = javaElements;
      fResources = resources;
    }

    public IJavaElement[] getActualJavaElementsToReorg() throws JavaModelException {
      List<IJavaElement> result = new ArrayList<IJavaElement>();
      for (int i = 0; i < fJavaElements.length; i++) {
        IJavaElement element = fJavaElements[i];
        if (element == null) continue;
        if (element instanceof IType) {
          IType type = (IType) element;
          ICompilationUnit cu = type.getCompilationUnit();
          if (cu != null
              && type.getDeclaringType() == null
              && cu.exists()
              && cu.getTypes().length == 1
              && !result.contains(cu)) result.add(cu);
          else if (!result.contains(type)) result.add(type);
        } else if (!result.contains(element)) {
          result.add(element);
        }
      }
      return result.toArray(new IJavaElement[result.size()]);
    }

    public IResource[] getActualResourcesToReorg() {
      Set<IJavaElement> javaElementSet = new HashSet<IJavaElement>(Arrays.asList(fJavaElements));
      List<IResource> result = new ArrayList<IResource>();
      for (int i = 0; i < fResources.length; i++) {
        if (fResources[i] == null) continue;
        IJavaElement element = JavaCore.create(fResources[i]);
        if (element == null || !element.exists() || !javaElementSet.contains(element))
          if (!result.contains(fResources[i])) result.add(fResources[i]);
      }
      return result.toArray(new IResource[result.size()]);
    }
  }

  private static final class CopyFilesFoldersAndCusPolicy extends FilesFoldersAndCusReorgPolicy
      implements ICopyPolicy {

    private static final String POLICY_COPY_RESOURCE =
        "org.eclipse.jdt.ui.copyResources"; // $NON-NLS-1$

    private static Change copyCuToPackage(
        ICompilationUnit cu,
        IPackageFragment dest,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      // XXX workaround for bug 31998 we will have to disable renaming of
      // linked packages (and cus)
      IResource res = ReorgUtils.getResource(cu);
      if (res != null && res.isLinked()) {
        if (ResourceUtil.getResource(dest) instanceof IContainer)
          return copyFileToContainer(
              cu, (IContainer) ResourceUtil.getResource(dest), nameProposer, copyQueries);
      }

      String newName = nameProposer.createNewName(cu, dest);
      Change simpleCopy =
          new CopyCompilationUnitChange(cu, dest, copyQueries.createStaticQuery(newName));
      if (newName == null || newName.equals(cu.getElementName())) return simpleCopy;

      try {
        IPath newPath =
            cu.getResource()
                .getParent()
                .getFullPath()
                .append(JavaModelUtil.getRenamedCUName(cu, newName));
        INewNameQuery nameQuery = copyQueries.createNewCompilationUnitNameQuery(cu, newName);
        return new CreateCopyOfCompilationUnitChange(newPath, cu.getSource(), cu, nameQuery);
      } catch (CoreException e) {
        // Using inferred change
        return simpleCopy;
      }
    }

    private static Change copyFileToContainer(
        ICompilationUnit cu,
        IContainer dest,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      IResource resource = ReorgUtils.getResource(cu);
      return createCopyResourceChange(resource, nameProposer, copyQueries, dest);
    }

    private static Change createCopyResourceChange(
        IResource resource,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries,
        IContainer destination) {
      if (resource == null || destination == null) return new NullChange();
      INewNameQuery nameQuery;
      String name = nameProposer.createNewName(resource, destination);
      if (name == null) nameQuery = copyQueries.createNullQuery();
      else nameQuery = copyQueries.createNewResourceNameQuery(resource, name);
      return new CopyResourceChange(resource, destination, nameQuery);
    }

    private CopyModifications fModifications;

    private ReorgExecutionLog fReorgExecutionLog;

    CopyFilesFoldersAndCusPolicy(IFile[] files, IFolder[] folders, ICompilationUnit[] cus) {
      super(files, folders, cus);
    }

    private Change createChange(
        ICompilationUnit unit, NewNameProposer nameProposer, INewNameQueries copyQueries) {
      IPackageFragment pack = getDestinationAsPackageFragment();
      if (pack != null) return copyCuToPackage(unit, pack, nameProposer, copyQueries);
      IContainer container = getDestinationAsContainer();
      return copyFileToContainer(unit, container, nameProposer, copyQueries);
    }

    public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries) {
      IFile[] file = getFiles();
      IFolder[] folders = getFolders();
      ICompilationUnit[] cus = getCus();
      pm.beginTask("", cus.length + file.length + folders.length); // $NON-NLS-1$
      NewNameProposer nameProposer = new NewNameProposer();
      CompositeChange composite =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_copy);
      composite.markAsSynthetic();
      for (int i = 0; i < cus.length; i++) {
        composite.add(createChange(cus[i], nameProposer, copyQueries));
        pm.worked(1);
      }
      if (pm.isCanceled()) throw new OperationCanceledException();
      for (int i = 0; i < file.length; i++) {
        composite.add(createChange(file[i], nameProposer, copyQueries));
        pm.worked(1);
      }
      if (pm.isCanceled()) throw new OperationCanceledException();
      for (int i = 0; i < folders.length; i++) {
        composite.add(createChange(folders[i], nameProposer, copyQueries));
        pm.worked(1);
      }
      pm.done();
      return composite;
    }

    private Change createChange(
        IResource resource, NewNameProposer nameProposer, INewNameQueries copyQueries) {
      IContainer dest = getDestinationAsContainer();
      return createCopyResourceChange(resource, nameProposer, copyQueries, dest);
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      ReorgExecutionLog log = getReorgExecutionLog();
      storeReorgExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createCopyDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    @Override
    protected String getDescriptionPlural() {
      final int kind = getContentKind();
      switch (kind) {
        case ONLY_FOLDERS:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_folders;
        case ONLY_FILES:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_files;
        case ONLY_CUS:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_compilation_units;
      }
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_description_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      final int kind = getContentKind();
      switch (kind) {
        case ONLY_FOLDERS:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_folder;
        case ONLY_FILES:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_file;
        case ONLY_CUS:
          return RefactoringCoreMessages.ReorgPolicyFactory_copy_compilation_unit;
      }
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_description_singular;
    }

    private Object getDestination() {
      Object result = getDestinationAsPackageFragment();
      if (result != null) return result;
      return getDestinationAsContainer();
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;
      fModifications = new CopyModifications();
      fReorgExecutionLog = new ReorgExecutionLog();
      CopyArguments jArgs = new CopyArguments(getDestination(), fReorgExecutionLog);
      CopyArguments rArgs = new CopyArguments(getDestinationAsContainer(), fReorgExecutionLog);
      ICompilationUnit[] cus = getCus();
      for (int i = 0; i < cus.length; i++) {
        fModifications.copy(cus[i], jArgs, rArgs);
      }
      IResource[] resources = ReorgUtils.union(getFiles(), getFolders());
      for (int i = 0; i < resources.length; i++) {
        fModifications.copy(resources[i], rArgs);
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_COPY_RESOURCE;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.COPY;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.COPY;
    }

    public ReorgExecutionLog getReorgExecutionLog() {
      return fReorgExecutionLog;
    }
  }

  private static final class CopyPackageFragmentRootsPolicy extends PackageFragmentRootsReorgPolicy
      implements ICopyPolicy {

    private static final String POLICY_COPY_ROOTS = "org.eclipse.jdt.ui.copyRoots"; // $NON-NLS-1$

    private CopyModifications fModifications;

    private ReorgExecutionLog fReorgExecutionLog;

    public CopyPackageFragmentRootsPolicy(IPackageFragmentRoot[] roots) {
      super(roots);
    }

    private Change createChange(
        IPackageFragmentRoot root,
        IJavaProject destination,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      IResource res = root.getResource();
      IProject destinationProject = destination.getProject();
      String newName = nameProposer.createNewName(res, destinationProject);
      INewNameQuery nameQuery;
      if (newName == null) nameQuery = copyQueries.createNullQuery();
      else nameQuery = copyQueries.createNewPackageFragmentRootNameQuery(root, newName);
      // TODO fix the query problem
      return new CopyPackageFragmentRootChange(root, destinationProject, nameQuery, null);
    }

    private Change createChange(
        IPackageFragmentRoot root,
        IContainer destination,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      IResource res = root.getResource();

      String newName = nameProposer.createNewName(res, destination);
      INewNameQuery nameQuery;
      if (newName == null) {
        nameQuery = copyQueries.createNullQuery();
      } else {
        nameQuery = copyQueries.createNewResourceNameQuery(res, newName);
      }

      return new CopyResourceChange(res, destination, nameQuery);
    }

    public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries) {
      NewNameProposer nameProposer = new NewNameProposer();
      IPackageFragmentRoot[] roots = getPackageFragmentRoots();
      pm.beginTask("", roots.length); // $NON-NLS-1$
      CompositeChange composite =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_copy_source_folder);
      composite.markAsSynthetic();
      IJavaProject destination = getDestinationJavaProject();
      for (int i = 0; i < roots.length; i++) {
        if (destination == null) {
          composite.add(
              createChange(
                  roots[i], (IContainer) getResourceDestination(), nameProposer, copyQueries));
        } else {
          composite.add(createChange(roots[i], destination, nameProposer, copyQueries));
        }
        pm.worked(1);
      }
      pm.done();
      return composite;
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      ReorgExecutionLog log = getReorgExecutionLog();
      storeReorgExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createCopyDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    @Override
    protected String getDescriptionPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_roots_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_roots_singular;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_roots_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_roots_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new CopyModifications();
      fReorgExecutionLog = new ReorgExecutionLog();
      IJavaProject destination = getDestinationJavaProject();
      if (destination == null) {
        IPackageFragmentRoot[] roots = getRoots();
        for (int i = 0; i < roots.length; i++) {
          fModifications.copy(
              roots[i].getResource(),
              new CopyArguments(getResourceDestination(), fReorgExecutionLog));
        }
      } else {
        CopyArguments javaArgs = new CopyArguments(destination, fReorgExecutionLog);
        CopyArguments resourceArgs =
            new CopyArguments(destination.getProject(), fReorgExecutionLog);
        IPackageFragmentRoot[] roots = getRoots();
        for (int i = 0; i < roots.length; i++) {
          fModifications.copy(roots[i], javaArgs, resourceArgs);
        }
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_COPY_ROOTS;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.COPY;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.COPY;
    }

    public ReorgExecutionLog getReorgExecutionLog() {
      return fReorgExecutionLog;
    }
  }

  private static final class CopyPackagesPolicy extends PackagesReorgPolicy implements ICopyPolicy {

    private static final String POLICY_COPY_PACKAGES =
        "org.eclipse.jdt.ui.copyPackages"; // $NON-NLS-1$

    private CopyModifications fModifications;

    private ReorgExecutionLog fReorgExecutionLog;

    public CopyPackagesPolicy(IPackageFragment[] packageFragments) {
      super(packageFragments);
    }

    private Change createChange(
        IPackageFragment pack,
        IPackageFragmentRoot destination,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      String newName = nameProposer.createNewName(pack, destination);
      if (newName == null
          || JavaConventionsUtil.validatePackageName(newName, destination).getSeverity()
              < IStatus.ERROR) {
        INewNameQuery nameQuery;
        if (newName == null) nameQuery = copyQueries.createNullQuery();
        else nameQuery = copyQueries.createNewPackageNameQuery(pack, newName);
        return new CopyPackageChange(pack, destination, nameQuery);
      } else {
        if (destination.getResource() instanceof IContainer) {
          IContainer dest = (IContainer) destination.getResource();
          IResource res = pack.getResource();
          INewNameQuery nameQuery = copyQueries.createNewResourceNameQuery(res, newName);
          return new CopyResourceChange(res, dest, nameQuery);
        } else {
          return new NullChange();
        }
      }
    }

    private Change createChange(
        IPackageFragment pack,
        IContainer container,
        NewNameProposer nameProposer,
        INewNameQueries copyQueries) {
      String newName = nameProposer.createNewName(pack.getResource(), container);
      if (newName == null) {
        return new CopyResourceChange(pack.getResource(), container, copyQueries.createNullQuery());
      } else {
        IResource res = pack.getResource();
        INewNameQuery nameQuery = copyQueries.createNewResourceNameQuery(res, newName);
        return new CopyResourceChange(res, container, nameQuery);
      }
    }

    public Change createChange(IProgressMonitor pm, INewNameQueries newNameQueries)
        throws JavaModelException {
      NewNameProposer nameProposer = new NewNameProposer();
      IPackageFragment[] fragments = getPackages();
      pm.beginTask("", fragments.length); // $NON-NLS-1$
      CompositeChange composite =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_copy_package);
      composite.markAsSynthetic();
      IPackageFragmentRoot root = getDestinationAsPackageFragmentRoot();
      for (int i = 0; i < fragments.length; i++) {
        if (root == null) {
          composite.add(
              createChange(
                  fragments[i],
                  (IContainer) getResourceDestination(),
                  nameProposer,
                  newNameQueries));
        } else {
          composite.add(createChange(fragments[i], root, nameProposer, newNameQueries));
        }
        pm.worked(1);
      }
      pm.done();
      return composite;
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      ReorgExecutionLog log = getReorgExecutionLog();
      storeReorgExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createCopyDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    @Override
    protected String getDescriptionPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_packages_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_package_singular;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_packages_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_packages_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new CopyModifications();
      fReorgExecutionLog = new ReorgExecutionLog();
      IPackageFragmentRoot destination = getDestinationAsPackageFragmentRoot();
      if (destination == null) {
        IPackageFragment[] packages = getPackages();
        for (int i = 0; i < packages.length; i++) {
          fModifications.copy(
              packages[i].getResource(),
              new CopyArguments(getResourceDestination(), fReorgExecutionLog));
        }
      } else {
        CopyArguments javaArgs = new CopyArguments(destination, fReorgExecutionLog);
        CopyArguments resourceArgs =
            new CopyArguments(destination.getResource(), fReorgExecutionLog);
        IPackageFragment[] packages = getPackages();
        for (int i = 0; i < packages.length; i++) {
          fModifications.copy(packages[i], javaArgs, resourceArgs);
        }
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_COPY_PACKAGES;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.COPY;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.COPY;
    }

    public ReorgExecutionLog getReorgExecutionLog() {
      return fReorgExecutionLog;
    }
  }

  private static final class CopySubCuElementsPolicy extends SubCuElementReorgPolicy
      implements ICopyPolicy {

    private static final String POLICY_COPY_MEMBERS =
        "org.eclipse.jdt.ui.copyMembers"; // $NON-NLS-1$

    private CopyModifications fModifications;

    private ReorgExecutionLog fReorgExecutionLog;

    CopySubCuElementsPolicy(IJavaElement[] javaElements) {
      super(javaElements);
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      return super.canEnable() && (getSourceCu() != null || getSourceClassFile() != null);
    }

    public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries)
        throws JavaModelException {
      try {
        CompilationUnit sourceCuNode = createSourceCuNode();
        ICompilationUnit targetCu = getEnclosingCompilationUnit(getJavaElementDestination());
        CompilationUnitRewrite targetRewriter = new CompilationUnitRewrite(targetCu);
        IJavaElement[] javaElements = getJavaElements();
        for (int i = 0; i < javaElements.length; i++) {
          copyToDestination(
              javaElements[i], targetRewriter, sourceCuNode, targetRewriter.getRoot());
        }
        return createCompilationUnitChange(targetRewriter);
      } catch (JavaModelException e) {
        throw e;
      } catch (CoreException e) {
        throw new JavaModelException(e);
      }
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      ReorgExecutionLog log = getReorgExecutionLog();
      storeReorgExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createCopyDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    private CompilationUnit createSourceCuNode() {
      Assert.isTrue(getSourceCu() != null || getSourceClassFile() != null);
      Assert.isTrue(getSourceCu() == null || getSourceClassFile() == null);
      ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
      parser.setBindingsRecovery(true);
      parser.setResolveBindings(true);
      if (getSourceCu() != null) parser.setSource(getSourceCu());
      else parser.setSource(getSourceClassFile());
      return (CompilationUnit) parser.createAST(null);
    }

    @Override
    public IFile[] getAllModifiedFiles() {
      return ReorgUtils.getFiles(
          new IResource[] {
            ReorgUtils.getResource(getEnclosingCompilationUnit(getJavaElementDestination()))
          });
    }

    @Override
    protected String getDescriptionPlural() {
      if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.TYPE)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_types;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.FIELD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_fields;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.METHOD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_methods;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.INITIALIZER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_initializers;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.PACKAGE_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_package_declarations;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_CONTAINER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_import_containers;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_imports;
      } else {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_elements_plural;
      }
    }

    @Override
    protected String getDescriptionSingular() {
      if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.TYPE)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_type;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.FIELD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_field;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.METHOD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_method;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.INITIALIZER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_initializer;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.PACKAGE_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_package;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_CONTAINER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_import_section;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_import;
      } else {
        return RefactoringCoreMessages.ReorgPolicyFactory_copy_elements_singular;
      }
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_elements_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_copy_elements_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new CopyModifications();
      fReorgExecutionLog = new ReorgExecutionLog();
      CopyArguments args = new CopyArguments(getJavaElementDestination(), fReorgExecutionLog);
      IJavaElement[] javaElements = getJavaElements();
      for (int i = 0; i < javaElements.length; i++) {
        fModifications.copy(javaElements[i], args, null);
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_COPY_MEMBERS;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.COPY;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.COPY;
    }

    public ReorgExecutionLog getReorgExecutionLog() {
      return fReorgExecutionLog;
    }

    private IClassFile getSourceClassFile() {
      // all have a common parent, so all must be in the same classfile
      // we checked before that the array in not null and not empty
      return (IClassFile) getJavaElements()[0].getAncestor(IJavaElement.CLASS_FILE);
    }
  }

  private abstract static class FilesFoldersAndCusReorgPolicy extends ReorgPolicy {

    protected static final int ONLY_CUS = 2;

    protected static final int ONLY_FILES = 1;

    protected static final int ONLY_FOLDERS = 0;

    private static IContainer getAsContainer(IResource resDest) {
      if (resDest instanceof IContainer) return (IContainer) resDest;
      if (resDest instanceof IFile) return ((IFile) resDest).getParent();
      return null;
    }

    private ICompilationUnit[] fCus;

    private IFile[] fFiles;

    private IFolder[] fFolders;

    public FilesFoldersAndCusReorgPolicy(IFile[] files, IFolder[] folders, ICompilationUnit[] cus) {
      fFiles = files;
      fFolders = folders;
      fCus = cus;
    }

    @Override
    public boolean canChildrenBeDestinations(IJavaElement javaElement) {
      switch (javaElement.getElementType()) {
        case IJavaElement.JAVA_MODEL:
        case IJavaElement.JAVA_PROJECT:
          return true;
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          IPackageFragmentRoot root = (IPackageFragmentRoot) javaElement;
          return !root.isArchive() && !root.isExternal();
        default:
          return false;
      }
    }

    @Override
    public boolean canChildrenBeDestinations(IResource resource) {
      return resource instanceof IContainer;
    }

    @Override
    public boolean canElementBeDestination(IJavaElement javaElement) {
      switch (javaElement.getElementType()) {
        case IJavaElement.PACKAGE_FRAGMENT:
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean canElementBeDestination(IResource resource) {
      return resource instanceof IProject || resource instanceof IFolder;
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      confirmOverwriting(reorgQueries);
      RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
      return status;
    }

    private void confirmOverwriting(IReorgQueries reorgQueries) {
      OverwriteHelper helper = new OverwriteHelper();
      helper.setFiles(fFiles);
      helper.setFolders(fFolders);
      helper.setCus(fCus);
      IPackageFragment destPack = getDestinationAsPackageFragment();
      if (destPack != null) {
        helper.confirmOverwriting(reorgQueries, destPack);
      } else {
        IContainer destinationAsContainer = getDestinationAsContainer();
        if (destinationAsContainer != null)
          helper.confirmOverwriting(reorgQueries, destinationAsContainer);
      }
      fFiles = helper.getFilesWithoutUnconfirmedOnes();
      fFolders = helper.getFoldersWithoutUnconfirmedOnes();
      fCus = helper.getCusWithoutUnconfirmedOnes();
    }

    protected boolean containsLinkedResources() {
      return ReorgUtils.containsLinkedResources(fFiles)
          || ReorgUtils.containsLinkedResources(fFolders)
          || ReorgUtils.containsLinkedResources(fCus);
    }

    protected abstract JavaRefactoringDescriptor createRefactoringDescriptor(
        final JDTRefactoringDescriptorComment comment,
        final Map<String, String> arguments,
        final String description,
        final String project,
        int flags);

    protected final int getContentKind() {
      final int length = fCus.length + fFiles.length + fFolders.length;
      if (length == fCus.length) return ONLY_CUS;
      else if (length == fFiles.length) return ONLY_FILES;
      else if (length == fFolders.length) return ONLY_FOLDERS;
      return -1;
    }

    protected final ICompilationUnit[] getCus() {
      return fCus;
    }

    private final String getSingleElementName() {
      switch (getContentKind()) {
        case ONLY_FOLDERS:
          return fFolders[0].getName();
        case ONLY_FILES:
          return fFiles[0].getName();
        case ONLY_CUS:
          return fCus[0].getElementName();
      }
      return null;
    }

    public final ChangeDescriptor getDescriptor() {
      final Map<String, String> arguments = new HashMap<String, String>();
      final int length = fFiles.length + fFolders.length + fCus.length;
      final String description = length == 1 ? getDescriptionSingular() : getDescriptionPlural();
      final IProject resource = getSingleProject();
      final String project = resource != null ? resource.getName() : null;
      final String header =
          length == 1
              ? Messages.format(
                  getHeaderPatternSingular(),
                  new String[] {getSingleElementName(), getDestinationLabel()})
              : Messages.format(
                  getHeaderPatternPlural(),
                  new String[] {String.valueOf(length), getDestinationLabel()});
      int flags =
          JavaRefactoringDescriptor.JAR_MIGRATION
              | JavaRefactoringDescriptor.JAR_REFACTORING
              | RefactoringDescriptor.STRUCTURAL_CHANGE
              | RefactoringDescriptor.MULTI_CHANGE;
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      arguments.put(ATTRIBUTE_POLICY, getPolicyId());
      arguments.put(ATTRIBUTE_FILES, new Integer(fFiles.length).toString());
      for (int offset = 0; offset < fFiles.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + 1),
            JavaRefactoringDescriptorUtil.resourceToHandle(project, fFiles[offset]));
      arguments.put(ATTRIBUTE_FOLDERS, new Integer(fFolders.length).toString());
      for (int offset = 0; offset < fFolders.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + fFiles.length + 1),
            JavaRefactoringDescriptorUtil.resourceToHandle(project, fFolders[offset]));
      arguments.put(ATTRIBUTE_UNITS, new Integer(fCus.length).toString());
      for (int offset = 0; offset < fCus.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT
                + (offset + fFolders.length + fFiles.length + 1),
            JavaRefactoringDescriptorUtil.elementToHandle(project, fCus[offset]));
      arguments.putAll(getRefactoringArguments(project));
      final JavaRefactoringDescriptor descriptor =
          createRefactoringDescriptor(comment, arguments, description, project, flags);
      return new RefactoringChangeDescriptor(descriptor);
    }

    protected final IContainer getDestinationAsContainer() {
      IResource resDest = getResourceDestination();
      if (resDest != null) return getAsContainer(resDest);
      IJavaElement jelDest = getJavaElementDestination();
      Assert.isNotNull(jelDest);
      return getAsContainer(ReorgUtils.getResource(jelDest));
    }

    protected IPackageFragment getDestinationAsPackageFragment() {
      IPackageFragment javaAsPackage =
          getJavaDestinationAsPackageFragment(getJavaElementDestination());
      if (javaAsPackage != null) return javaAsPackage;
      return getResourceDestinationAsPackageFragment(getResourceDestination());
    }

    protected final IJavaElement getDestinationContainerAsJavaElement() {
      if (getJavaElementDestination() != null) return getJavaElementDestination();
      IContainer destinationAsContainer = getDestinationAsContainer();
      if (destinationAsContainer == null) return null;
      IJavaElement je = JavaCore.create(destinationAsContainer);
      if (je != null && je.exists()) return je;
      return null;
    }

    protected final IFile[] getFiles() {
      return fFiles;
    }

    protected final IFolder[] getFolders() {
      return fFolders;
    }

    private IPackageFragment getJavaDestinationAsPackageFragment(IJavaElement javaDest) {
      if (javaDest == null || fCheckDestination && !javaDest.exists()) return null;
      if (javaDest instanceof IPackageFragment) return (IPackageFragment) javaDest;
      if (javaDest instanceof IPackageFragmentRoot)
        return ((IPackageFragmentRoot) javaDest).getPackageFragment(""); // $NON-NLS-1$
      if (javaDest instanceof IJavaProject) {
        try {
          IPackageFragmentRoot root =
              ReorgUtils.getCorrespondingPackageFragmentRoot((IJavaProject) javaDest);
          if (root != null) return root.getPackageFragment(""); // $NON-NLS-1$
        } catch (JavaModelException e) {
          // fall through
        }
      }
      return (IPackageFragment) javaDest.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    }

    public final IJavaElement[] getJavaElements() {
      return fCus;
    }

    private IPackageFragment getResourceDestinationAsPackageFragment(IResource resource) {
      if (resource instanceof IFile)
        return getJavaDestinationAsPackageFragment(JavaCore.create(resource.getParent()));
      return null;
    }

    public final IResource[] getResources() {
      return ReorgUtils.union(fFiles, fFolders);
    }

    private IProject getSingleProject() {
      IProject result = null;
      for (int index = 0; index < fFiles.length; index++) {
        if (result == null) result = fFiles[index].getProject();
        else if (!result.equals(fFiles[index].getProject())) return null;
      }
      for (int index = 0; index < fFolders.length; index++) {
        if (result == null) result = fFolders[index].getProject();
        else if (!result.equals(fFolders[index].getProject())) return null;
      }
      for (int index = 0; index < fCus.length; index++) {
        if (result == null) result = fCus[index].getJavaProject().getProject();
        else if (!result.equals(fCus[index].getJavaProject().getProject())) return null;
      }
      return result;
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      RefactoringStatus status = new RefactoringStatus();
      int fileCount = 0;
      int folderCount = 0;
      int unitCount = 0;
      String value = arguments.getAttribute(ATTRIBUTE_FILES);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          fileCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_FILES));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_FILES));
      value = arguments.getAttribute(ATTRIBUTE_FOLDERS);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          folderCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_FOLDERS));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_FOLDERS));
      value = arguments.getAttribute(ATTRIBUTE_UNITS);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          unitCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_UNITS));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_UNITS));
      String handle = null;
      List<IAdaptable> elements = new ArrayList<IAdaptable>();
      for (int index = 0; index < fileCount; index++) {
        final String attribute = JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IResource resource =
              JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), handle);
          if (resource == null || !resource.exists())
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    resource, getProcessorId(), getRefactoringId()));
          else elements.add(resource);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fFiles = elements.toArray(new IFile[elements.size()]);
      elements = new ArrayList<IAdaptable>();
      for (int index = 0; index < folderCount; index++) {
        final String attribute =
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (fileCount + index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IResource resource =
              JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), handle);
          if (resource == null || !resource.exists())
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    resource, getProcessorId(), getRefactoringId()));
          else elements.add(resource);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fFolders = elements.toArray(new IFolder[elements.size()]);
      elements = new ArrayList<IAdaptable>();
      for (int index = 0; index < unitCount; index++) {
        final String attribute =
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (folderCount + fileCount + index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IJavaElement element =
              JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
          if (element == null
              || !element.exists()
              || element.getElementType() != IJavaElement.COMPILATION_UNIT)
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    element, getProcessorId(), getRefactoringId()));
          else elements.add(element);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fCus = elements.toArray(new ICompilationUnit[elements.size()]);
      status.merge(super.initialize(arguments));
      return status;
    }

    private boolean isChildOfOrEqualToAnyFolder(IResource resource) {
      for (int i = 0; i < fFolders.length; i++) {
        IFolder folder = fFolders[i];
        if (folder.equals(resource) || ParentChecker.isDescendantOf(resource, folder)) return true;
      }
      return false;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      Assert.isNotNull(javaElement);
      if (!fCheckDestination) return new RefactoringStatus();
      if (!javaElement.exists())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_doesnotexist0);
      if (javaElement instanceof IJavaModel)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_jmodel);

      if (javaElement.isReadOnly())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_readonly);

      if (!javaElement.isStructureKnown())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_structure);

      if (javaElement instanceof IOpenable) {
        IOpenable openable = (IOpenable) javaElement;
        if (!openable.isConsistent())
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_inconsistent);
      }

      if (javaElement instanceof IPackageFragmentRoot) {
        IPackageFragmentRoot root = (IPackageFragmentRoot) javaElement;
        if (root.isArchive())
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_archive);
        if (root.isExternal())
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_external);
      }

      if (ReorgUtils.isInsideCompilationUnit(javaElement)) {
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot);
      }

      IContainer destinationAsContainer = getDestinationAsContainer();
      if (destinationAsContainer == null || isChildOfOrEqualToAnyFolder(destinationAsContainer))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (containsLinkedResources() && !ReorgUtils.canBeDestinationForLinkedResources(javaElement))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_linked);
      return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource resource) throws JavaModelException {
      Assert.isNotNull(resource);
      if (!resource.exists() || resource.isPhantom())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_phantom);
      if (!resource.isAccessible())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_inaccessible);

      if (resource.getType() == IResource.ROOT)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (isChildOfOrEqualToAnyFolder(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (containsLinkedResources() && !ReorgUtils.canBeDestinationForLinkedResources(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_linked);

      return new RefactoringStatus();
    }
  }

  private static final class MoveFilesFoldersAndCusPolicy extends FilesFoldersAndCusReorgPolicy
      implements IMovePolicy {

    private static final String POLICY_MOVE_RESOURCES =
        "org.eclipse.jdt.ui.moveResources"; // $NON-NLS-1$

    private static Change moveCuToPackage(ICompilationUnit cu, IPackageFragment dest) {
      // XXX workaround for bug 31998 we will have to disable renaming of
      // linked packages (and cus)
      IResource resource = cu.getResource();
      if (resource != null && resource.isLinked()) {
        if (ResourceUtil.getResource(dest) instanceof IContainer)
          return moveFileToContainer(cu, (IContainer) ResourceUtil.getResource(dest));
      }
      return new MoveCompilationUnitChange(cu, dest);
    }

    private static Change moveFileToContainer(ICompilationUnit cu, IContainer dest) {
      return new MoveResourceChange(cu.getResource(), dest);
    }

    private TextChangeManager fChangeManager;

    private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();

    private String fFilePatterns;

    private MoveModifications fModifications;

    private QualifiedNameSearchResult fQualifiedNameSearchResult;

    private boolean fUpdateQualifiedNames;

    private boolean fUpdateReferences = true;

    MoveFilesFoldersAndCusPolicy(IFile[] files, IFolder[] folders, ICompilationUnit[] cus) {
      super(files, folders, cus);
      fUpdateQualifiedNames = false;
      fQualifiedNameSearchResult = new QualifiedNameSearchResult();
    }

    public boolean canEnableQualifiedNameUpdating() {
      return getCus().length > 0 && !JavaElementUtil.isDefaultPackage(getCommonParent());
    }

    /** {@inheritDoc} */
    public boolean canUpdateJavaReferences() {
      return true;
    }

    public boolean canUpdateQualifiedNames() {
      if (!canEnableQualifiedNameUpdating()) return false;

      IPackageFragment pack = getDestinationAsPackageFragment();
      if (pack == null) return false;

      if (pack.isDefaultPackage()) return false;

      IJavaElement destination = getJavaElementDestination();
      if (destination instanceof IPackageFragmentRoot && getCus().length > 0) {
        return false;
      }

      return true;
    }

    /**
     * @return <code>true</code> if the user could expect that we update references, but we don't
     * @since 3.5
     */
    private boolean cannotUpdateReferencesForDestination() {
      if (getCus().length == 0) return false;
      IPackageFragment pack = getDestinationAsPackageFragment();
      if (pack == null || pack.isDefaultPackage()) return true;

      IJavaElement destination = getJavaElementDestination();
      if (destination instanceof IPackageFragmentRoot && getCus().length > 0) {
        return false;
      }

      Object commonParent = getCommonParent();
      return JavaElementUtil.isDefaultPackage(commonParent);
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      try {
        pm.beginTask("", fUpdateQualifiedNames ? 7 : 3); // $NON-NLS-1$
        RefactoringStatus result = new RefactoringStatus();
        confirmMovingReadOnly(reorgQueries);
        fChangeManager = createChangeManager(new SubProgressMonitor(pm, 2), result);
        if (fUpdateQualifiedNames) computeQualifiedNameMatches(new SubProgressMonitor(pm, 4));
        result.merge(
            super.checkFinalConditions(new SubProgressMonitor(pm, 1), context, reorgQueries));
        return result;
      } catch (JavaModelException e) {
        throw e;
      } catch (CoreException e) {
        throw new JavaModelException(e);
      } finally {
        pm.done();
      }
    }

    /** {@inheritDoc} */
    @Override
    protected IPackageFragment getDestinationAsPackageFragment() {
      IJavaElement destination = getJavaElementDestination();
      if (destination instanceof IPackageFragmentRoot && getCus().length > 0) {
        String packageName = ((IPackageFragment) getCus()[0].getParent()).getElementName();
        return ((IPackageFragmentRoot) destination).getPackageFragment(packageName);
      }

      return super.getDestinationAsPackageFragment();
    }

    private void computeQualifiedNameMatches(IProgressMonitor pm) throws JavaModelException {
      if (!fUpdateQualifiedNames) return;
      IPackageFragment destination = getDestinationAsPackageFragment();
      if (destination != null) {
        ICompilationUnit[] cus = getCus();
        pm.beginTask("", cus.length); // $NON-NLS-1$
        pm.subTask(RefactoringCoreMessages.MoveRefactoring_scanning_qualified_names);
        for (int i = 0; i < cus.length; i++) {
          ICompilationUnit cu = cus[i];
          IType[] types = cu.getTypes();
          IProgressMonitor typesMonitor = new SubProgressMonitor(pm, 1);
          typesMonitor.beginTask("", types.length); // $NON-NLS-1$
          for (int j = 0; j < types.length; j++) {
            handleType(types[j], destination, new SubProgressMonitor(typesMonitor, 1));
            if (typesMonitor.isCanceled()) throw new OperationCanceledException();
          }
          typesMonitor.done();
        }
      }
      pm.done();
    }

    private void confirmMovingReadOnly(IReorgQueries reorgQueries) throws CoreException {
      if (!ReadOnlyResourceFinder.confirmMoveOfReadOnlyElements(
          getJavaElements(), getResources(), reorgQueries)) throw new OperationCanceledException();
    }

    private Change createChange(ICompilationUnit cu) {
      IPackageFragment pack = getDestinationAsPackageFragment();
      if (pack != null) return moveCuToPackage(cu, pack);
      IContainer container = getDestinationAsContainer();
      if (container == null) return new NullChange();
      return moveFileToContainer(cu, container);
    }

    public Change createChange(IProgressMonitor pm) throws JavaModelException {
      if (!fUpdateReferences) {
        return createSimpleMoveChange(pm);
      } else {
        return createReferenceUpdatingMoveChange(pm);
      }
    }

    private Change createChange(IResource res) {
      IContainer destinationAsContainer = getDestinationAsContainer();
      if (destinationAsContainer == null) return new NullChange();
      return new MoveResourceChange(res, destinationAsContainer);
    }

    private TextChangeManager createChangeManager(IProgressMonitor pm, RefactoringStatus status)
        throws JavaModelException {
      pm.beginTask("", 1); // $NON-NLS-1$
      try {
        if (!fUpdateReferences) return new TextChangeManager();

        IPackageFragment packageDest = getDestinationAsPackageFragment();
        if (packageDest != null) {
          MoveCuUpdateCreator creator = new MoveCuUpdateCreator(getCus(), packageDest);
          return creator.createChangeManager(new SubProgressMonitor(pm, 1), status);
        } else return new TextChangeManager();
      } finally {
        pm.done();
      }
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      CreateTargetExecutionLog log = getCreateTargetExecutionLog();
      storeCreateTargetExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createMoveDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    private Change createReferenceUpdatingMoveChange(IProgressMonitor pm)
        throws JavaModelException {
      pm.beginTask("", 2 + (fUpdateQualifiedNames ? 1 : 0)); // $NON-NLS-1$
      try {
        CompositeChange composite =
            new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_move);
        composite.markAsSynthetic();
        // XX workaround for bug 13558
        // <workaround>
        if (fChangeManager == null) {
          fChangeManager =
              createChangeManager(new SubProgressMonitor(pm, 1), new RefactoringStatus());
          // TODO: non-CU matches silently dropped
          RefactoringStatus status = Checks.validateModifiesFiles(getAllModifiedFiles(), null);
          if (status.hasFatalError()) fChangeManager = new TextChangeManager();
        }
        // </workaround>

        composite.merge(
            new CompositeChange(
                RefactoringCoreMessages.MoveRefactoring_reorganize_elements,
                fChangeManager.getAllChanges()));

        Change fileMove = createSimpleMoveChange(new SubProgressMonitor(pm, 1));
        if (fileMove instanceof CompositeChange) {
          composite.merge(((CompositeChange) fileMove));
        } else {
          composite.add(fileMove);
        }
        return composite;
      } finally {
        pm.done();
      }
    }

    private Change createSimpleMoveChange(IProgressMonitor pm) {
      CompositeChange result =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_move);
      result.markAsSynthetic();
      IFile[] files = getFiles();
      IFolder[] folders = getFolders();
      ICompilationUnit[] cus = getCus();
      pm.beginTask("", files.length + folders.length + cus.length); // $NON-NLS-1$
      for (int i = 0; i < files.length; i++) {
        result.add(createChange(files[i]));
        pm.worked(1);
      }
      if (pm.isCanceled()) throw new OperationCanceledException();
      for (int i = 0; i < folders.length; i++) {
        result.add(createChange(folders[i]));
        pm.worked(1);
      }
      if (pm.isCanceled()) throw new OperationCanceledException();
      for (int i = 0; i < cus.length; i++) {
        result.add(createChange(cus[i]));
        pm.worked(1);
      }
      pm.done();
      return result;
    }

    @Override
    public IFile[] getAllModifiedFiles() {
      Set<IFile> result = new HashSet<IFile>();
      result.addAll(Arrays.asList(ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits())));
      result.addAll(Arrays.asList(fQualifiedNameSearchResult.getAllFiles()));
      if (!(getJavaElementDestination() instanceof IPackageFragmentRoot)
          && getDestinationAsPackageFragment() != null
          && getUpdateReferences()) result.addAll(Arrays.asList(ResourceUtil.getFiles(getCus())));
      return result.toArray(new IFile[result.size()]);
    }

    private Object getCommonParent() {
      return new ParentChecker(getResources(), getJavaElements()).getCommonParent();
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
      return fCreateTargetExecutionLog;
    }

    public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
      return createQueries.createNewPackageQuery();
    }

    @Override
    protected String getDescriptionPlural() {
      final int kind = getContentKind();
      switch (kind) {
        case ONLY_FOLDERS:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_folders;
        case ONLY_FILES:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_files;
        case ONLY_CUS:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_compilation_units;
      }
      return RefactoringCoreMessages.ReorgPolicyFactory_move_description_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      final int kind = getContentKind();
      switch (kind) {
        case ONLY_FOLDERS:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_folder;
        case ONLY_FILES:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_file;
        case ONLY_CUS:
          return RefactoringCoreMessages.ReorgPolicyFactory_move_compilation_unit;
      }
      return RefactoringCoreMessages.ReorgPolicyFactory_move_description_singular;
    }

    public String getFilePatterns() {
      return fFilePatterns;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new MoveModifications();
      IPackageFragment pack = getDestinationAsPackageFragment();
      IContainer container = getDestinationAsContainer();
      Object unitDestination = null;
      if (pack != null) unitDestination = pack;
      else unitDestination = container;

      boolean updateReferenes = getUpdateReferences();
      if (unitDestination != null) {
        ICompilationUnit[] units = getCus();
        for (int i = 0; i < units.length; i++) {
          fModifications.move(units[i], new MoveArguments(unitDestination, updateReferenes));
        }
      }
      if (container != null) {
        IFile[] files = getFiles();
        for (int i = 0; i < files.length; i++) {
          fModifications.move(files[i], new MoveArguments(container, updateReferenes));
        }
        IFolder[] folders = getFolders();
        for (int i = 0; i < folders.length; i++) {
          fModifications.move(folders[i], new MoveArguments(container, updateReferenes));
        }
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_MOVE_RESOURCES;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.MOVE;
    }

    @Override
    protected Map<String, String> getRefactoringArguments(String project) {
      final Map<String, String> arguments = new HashMap<String, String>();
      arguments.putAll(super.getRefactoringArguments(project));
      if (fFilePatterns != null && !"".equals(fFilePatterns)) // $NON-NLS-1$
      arguments.put(ATTRIBUTE_PATTERNS, fFilePatterns);
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES,
          Boolean.valueOf(fUpdateReferences).toString());
      arguments.put(ATTRIBUTE_QUALIFIED, Boolean.valueOf(fUpdateQualifiedNames).toString());
      return arguments;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.MOVE;
    }

    public boolean getUpdateQualifiedNames() {
      return fUpdateQualifiedNames;
    }

    public boolean getUpdateReferences() {
      return fUpdateReferences;
    }

    private void handleType(IType type, IPackageFragment destination, IProgressMonitor pm) {
      QualifiedNameFinder.process(
          fQualifiedNameSearchResult,
          type.getFullyQualifiedName(),
          destination.getElementName() + "." + type.getTypeQualifiedName(), // $NON-NLS-1$
          fFilePatterns,
          type.getJavaProject().getProject(),
          pm);
    }

    public boolean hasAllInputSet() {
      if (getResourceDestination() == null && getJavaElementDestination() == null) return false;
      if (canUpdateQualifiedNames()) return false;

      return true;
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      final String patterns = arguments.getAttribute(ATTRIBUTE_PATTERNS);
      if (patterns != null && !"".equals(patterns)) // $NON-NLS-1$
      fFilePatterns = patterns;
      else fFilePatterns = ""; // $NON-NLS-1$
      final String references =
          arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES);
      if (references != null) {
        fUpdateReferences = Boolean.valueOf(references).booleanValue();
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES));
      final String qualified = arguments.getAttribute(ATTRIBUTE_QUALIFIED);
      if (qualified != null) {
        fUpdateQualifiedNames = Boolean.valueOf(qualified).booleanValue();
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_QUALIFIED));
      return super.initialize(arguments);
    }

    public boolean isTextualMove() {
      return false;
    }

    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm)
        throws CoreException {
      if (fQualifiedNameSearchResult != null) {
        return fQualifiedNameSearchResult.getSingleChange(
            Changes.getModifiedFiles(participantChanges));
      } else {
        return null;
      }
    }

    public void setDestinationCheck(boolean check) {
      fCheckDestination = check;
    }

    public void setFilePatterns(String patterns) {
      Assert.isNotNull(patterns);
      fFilePatterns = patterns;
    }

    public void setUpdateQualifiedNames(boolean update) {
      fUpdateQualifiedNames = update;
    }

    public void setUpdateReferences(boolean update) {
      fUpdateReferences = update;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement destination)
        throws JavaModelException {
      RefactoringStatus superStatus = super.verifyDestination(destination);
      if (superStatus.hasFatalError()) return superStatus;

      Object commonParent = new ParentChecker(getResources(), getJavaElements()).getCommonParent();
      if (destination.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);
      IContainer destinationAsContainer = getDestinationAsContainer();
      if (destinationAsContainer != null
          && (destinationAsContainer.equals(commonParent)
              || commonParent instanceof IPackageFragmentRoot
                  && destinationAsContainer.equals(
                      ((IPackageFragmentRoot) commonParent).getResource())))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);
      IPackageFragment destinationAsPackage = getDestinationAsPackageFragment();
      if (destinationAsPackage != null && (destinationAsPackage.equals(commonParent)))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);

      if (cannotUpdateReferencesForDestination())
        superStatus.addInfo(RefactoringCoreMessages.ReorgPolicyFactory_noJavaUpdates);

      return superStatus;
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource destination) throws JavaModelException {
      RefactoringStatus superStatus = super.verifyDestination(destination);
      if (superStatus.hasFatalError()) return superStatus;

      Object commonParent = getCommonParent();
      if (destination.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);
      IContainer destinationAsContainer = getDestinationAsContainer();
      if (destinationAsContainer != null && destinationAsContainer.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);
      IJavaElement destinationContainerAsPackage = getDestinationContainerAsJavaElement();
      if (destinationContainerAsPackage != null
          && destinationContainerAsPackage.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_parent);

      if (cannotUpdateReferencesForDestination())
        superStatus.addInfo(RefactoringCoreMessages.ReorgPolicyFactory_noJavaUpdates);

      return superStatus;
    }
  }

  private static final class MovePackageFragmentRootsPolicy extends PackageFragmentRootsReorgPolicy
      implements IMovePolicy {

    private static final String POLICY_MOVE_ROOTS = "org.eclipse.jdt.ui.moveRoots"; // $NON-NLS-1$

    private static boolean isParentOfAny(IJavaProject javaProject, IPackageFragmentRoot[] roots) {
      for (int i = 0; i < roots.length; i++) {
        if (ReorgUtils.isParentInWorkspaceOrOnDisk(roots[i], javaProject)) return true;
      }
      return false;
    }

    private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();

    private MoveModifications fModifications;

    private boolean fUpdateReferences = true;

    MovePackageFragmentRootsPolicy(IPackageFragmentRoot[] roots) {
      super(roots);
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      if (!super.canEnable()) return false;
      IPackageFragmentRoot[] roots = getPackageFragmentRoots();
      for (int i = 0; i < roots.length; i++) {
        IPackageFragmentRoot root = roots[i];
        if (root.isReadOnly() && !root.isArchive() && !root.isExternal()) {
          final ResourceAttributes attributes = roots[i].getResource().getResourceAttributes();
          if (attributes == null || attributes.isReadOnly()) return false;
        }
      }
      return roots.length > 0;
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      try {
        RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
        confirmMovingReadOnly(reorgQueries);
        return status;
      } catch (JavaModelException e) {
        throw e;
      } catch (CoreException e) {
        throw new JavaModelException(e);
      }
    }

    private void confirmMovingReadOnly(IReorgQueries reorgQueries) throws CoreException {
      if (!ReadOnlyResourceFinder.confirmMoveOfReadOnlyElements(
          getJavaElements(), getResources(), reorgQueries)) throw new OperationCanceledException();
    }

    private Change createChange(IPackageFragmentRoot root, IJavaProject destination) {
      // /XXX fix the query
      return new MovePackageFragmentRootChange(root, destination.getProject(), null);
    }

    public Change createChange(IProgressMonitor pm) throws JavaModelException {
      IPackageFragmentRoot[] roots = getPackageFragmentRoots();
      pm.beginTask("", roots.length); // $NON-NLS-1$
      CompositeChange composite =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_move_source_folder);
      composite.markAsSynthetic();
      IJavaProject destination = getDestinationJavaProject();
      for (int i = 0; i < roots.length; i++) {
        if (destination == null) {
          composite.add(
              new MovePackageFragmentRootChange(
                  roots[i], (IContainer) getResourceDestination(), null));
        } else {
          composite.add(createChange(roots[i], destination));
        }
        pm.worked(1);
      }
      pm.done();
      return composite;
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {

      CreateTargetExecutionLog log = getCreateTargetExecutionLog();
      storeCreateTargetExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createMoveDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
      return fCreateTargetExecutionLog;
    }

    public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
      return null;
    }

    @Override
    protected String getDescriptionPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_roots_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_roots_singular;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_roots_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_roots_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new MoveModifications();
      IJavaProject destination = getDestinationJavaProject();
      boolean updateReferences = getUpdateReferences();
      if (destination != null) {
        IPackageFragmentRoot[] roots = getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
          fModifications.move(roots[i], new MoveArguments(destination, updateReferences));
        }
      } else {
        IPackageFragmentRoot[] roots = getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
          fModifications.move(
              roots[i], new MoveArguments(getResourceDestination(), updateReferences));
        }
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_MOVE_ROOTS;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.MOVE;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.MOVE;
    }

    public boolean isTextualMove() {
      return false;
    }

    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm)
        throws CoreException {
      return null;
    }

    public void setDestinationCheck(boolean check) {
      fCheckDestination = check;
    }

    /** {@inheritDoc} */
    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      RefactoringStatus superStatus = super.verifyDestination(javaElement);
      if (superStatus.hasFatalError()) return superStatus;
      IJavaProject javaProject = getDestinationJavaProject();
      if (isParentOfAny(javaProject, getPackageFragmentRoots()))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_element2parent);
      return superStatus;
    }

    /** {@inheritDoc} */
    @Override
    protected RefactoringStatus verifyDestination(IResource destination) {
      RefactoringStatus superStatus = super.verifyDestination(destination);
      if (superStatus.hasFatalError()) return superStatus;

      Object commonParent = new ParentChecker(getResources(), getJavaElements()).getCommonParent();
      if (destination.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot_move_source_to_parent);

      return superStatus;
    }

    /** {@inheritDoc} */
    public boolean canEnableQualifiedNameUpdating() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean canUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public void setUpdateQualifiedNames(boolean update) {}

    /** {@inheritDoc} */
    public void setFilePatterns(String patterns) {}

    /** {@inheritDoc} */
    public String getFilePatterns() {
      return null;
    }

    /** {@inheritDoc} */
    public boolean canUpdateJavaReferences() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateReferences() {
      return fUpdateReferences;
    }

    /** {@inheritDoc} */
    public void setUpdateReferences(boolean update) {
      fUpdateReferences = update;
    }

    /** {@inheritDoc} */
    public boolean hasAllInputSet() {
      return getJavaElementDestination() != null || getResourceDestination() != null;
    }
  }

  private static final class MovePackagesPolicy extends PackagesReorgPolicy implements IMovePolicy {

    private static final String POLICY_MOVE_PACKAGES =
        "org.eclipse.jdt.ui.movePackages"; // $NON-NLS-1$

    private static boolean isParentOfAny(IPackageFragmentRoot root, IPackageFragment[] fragments) {
      for (int i = 0; i < fragments.length; i++) {
        IPackageFragment fragment = fragments[i];
        if (ReorgUtils.isParentInWorkspaceOrOnDisk(fragment, root)) return true;
      }
      return false;
    }

    private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();

    private MoveModifications fModifications;

    private boolean fUpdateReferences = true;

    MovePackagesPolicy(IPackageFragment[] packageFragments) {
      super(packageFragments);
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      try {
        RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
        confirmMovingReadOnly(reorgQueries);
        return status;
      } catch (JavaModelException e) {
        throw e;
      } catch (CoreException e) {
        throw new JavaModelException(e);
      }
    }

    private void confirmMovingReadOnly(IReorgQueries reorgQueries) throws CoreException {
      if (!ReadOnlyResourceFinder.confirmMoveOfReadOnlyElements(
          getJavaElements(), getResources(), reorgQueries)) throw new OperationCanceledException();
    }

    private Change createChange(IPackageFragment pack, IPackageFragmentRoot destination) {
      return new MovePackageChange(pack, destination);
    }

    private Change createChange(IPackageFragment pack, IContainer destination) {
      return new MoveResourceChange(pack.getResource(), destination);
    }

    public Change createChange(IProgressMonitor pm) throws JavaModelException {
      IPackageFragment[] fragments = getPackages();
      pm.beginTask("", fragments.length); // $NON-NLS-1$
      CompositeChange result =
          new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_move_package);
      result.markAsSynthetic();
      IPackageFragmentRoot root = getDestinationAsPackageFragmentRoot();
      for (int i = 0; i < fragments.length; i++) {
        if (root == null) {
          result.add(createChange(fragments[i], (IContainer) getResourceDestination()));
        } else {
          result.add(createChange(fragments[i], root));
        }
        pm.worked(1);
        if (pm.isCanceled()) throw new OperationCanceledException();
      }
      pm.done();
      return result;
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      CreateTargetExecutionLog log = getCreateTargetExecutionLog();
      storeCreateTargetExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createMoveDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
      return fCreateTargetExecutionLog;
    }

    public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
      return null;
    }

    @Override
    protected String getDescriptionPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_packages_plural;
    }

    @Override
    protected String getDescriptionSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_packages_singular;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_packages_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_packages_header_plural;
    }

    @Override
    protected RefactoringModifications getModifications() throws CoreException {
      if (fModifications != null) return fModifications;

      fModifications = new MoveModifications();
      boolean updateReferences = getUpdateReferences();
      IPackageFragment[] packages = getPackages();
      IPackageFragmentRoot javaDestination = getDestinationAsPackageFragmentRoot();
      for (int i = 0; i < packages.length; i++) {
        if (javaDestination == null) {
          fModifications.move(
              packages[i].getResource(),
              new MoveArguments(getResourceDestination(), updateReferences));
        } else {
          fModifications.move(packages[i], new MoveArguments(javaDestination, updateReferences));
        }
      }
      return fModifications;
    }

    public String getPolicyId() {
      return POLICY_MOVE_PACKAGES;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.MOVE;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.MOVE;
    }

    public boolean isTextualMove() {
      return false;
    }

    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm)
        throws CoreException {
      return null;
    }

    public void setDestinationCheck(boolean check) {
      fCheckDestination = check;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      RefactoringStatus superStatus = super.verifyDestination(javaElement);
      if (superStatus.hasFatalError()) return superStatus;

      IPackageFragmentRoot root = getDestinationAsPackageFragmentRoot();
      if (isParentOfAny(root, getPackages()))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_package2parent);
      return superStatus;
    }

    /** {@inheritDoc} */
    @Override
    protected RefactoringStatus verifyDestination(IResource destination) {
      RefactoringStatus superStatus = super.verifyDestination(destination);
      if (superStatus.hasFatalError()) return superStatus;

      Object commonParent = new ParentChecker(getResources(), getJavaElements()).getCommonParent();
      if (destination.equals(commonParent))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot_move_package_to_parent);

      return superStatus;
    }

    /** {@inheritDoc} */
    public boolean canEnableQualifiedNameUpdating() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean canUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public void setUpdateQualifiedNames(boolean update) {}

    /** {@inheritDoc} */
    public void setFilePatterns(String patterns) {}

    /** {@inheritDoc} */
    public String getFilePatterns() {
      return null;
    }

    /** {@inheritDoc} */
    public boolean canUpdateJavaReferences() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateReferences() {
      return fUpdateReferences;
    }

    /** {@inheritDoc} */
    public void setUpdateReferences(boolean update) {
      fUpdateReferences = update;
    }

    /** {@inheritDoc} */
    public boolean hasAllInputSet() {
      return getJavaElementDestination() != null || getResourceDestination() != null;
    }
  }

  private abstract static class MoveSubCuElementsPolicy extends SubCuElementReorgPolicy
      implements IMovePolicy {

    private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();

    private boolean fUpdateReferences = true;

    MoveSubCuElementsPolicy(IJavaElement[] javaElements) {
      super(javaElements);
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      return super.canEnable() && getSourceCu() != null;
    }

    public Change createChange(IProgressMonitor pm) throws JavaModelException {
      pm.beginTask("", 3); // $NON-NLS-1$
      try {
        final ICompilationUnit sourceCu = getSourceCu();
        CompilationUnit sourceCuNode =
            RefactoringASTParser.parseWithASTProvider(
                sourceCu, true, new SubProgressMonitor(pm, 1));
        CompilationUnitRewrite sourceRewriter = new CompilationUnitRewrite(sourceCu, sourceCuNode);
        ICompilationUnit destinationCu = getEnclosingCompilationUnit(getJavaElementDestination());
        CompilationUnitRewrite targetRewriter;
        if (sourceCu.equals(destinationCu)) {
          targetRewriter = sourceRewriter;
          pm.worked(1);
        } else {
          CompilationUnit destinationCuNode =
              RefactoringASTParser.parseWithASTProvider(
                  destinationCu, true, new SubProgressMonitor(pm, 1));
          targetRewriter = new CompilationUnitRewrite(destinationCu, destinationCuNode);
        }
        IJavaElement[] javaElements = getJavaElements();
        for (int i = 0; i < javaElements.length; i++) {
          copyToDestination(
              javaElements[i], targetRewriter, sourceRewriter.getRoot(), targetRewriter.getRoot());
        }
        ASTNodeDeleteUtil.markAsDeleted(javaElements, sourceRewriter, null);
        Change targetCuChange = createCompilationUnitChange(targetRewriter);
        if (sourceCu.equals(destinationCu)) {
          return targetCuChange;
        } else {
          CompositeChange result =
              new DynamicValidationStateChange(RefactoringCoreMessages.ReorgPolicy_move_members);
          result.markAsSynthetic();
          result.add(targetCuChange);
          if (Arrays.asList(getJavaElements()).containsAll(Arrays.asList(sourceCu.getTypes()))) {
            result.add(
                DeleteChangeCreator.createDeleteChange(
                    null,
                    new IResource[0],
                    new ICompilationUnit[] {sourceCu},
                    RefactoringCoreMessages.ReorgPolicy_move,
                    Collections.<IResource>emptyList()));
          } else {
            result.add(createCompilationUnitChange(sourceRewriter));
          }
          return result;
        }
      } catch (JavaModelException e) {
        throw e;
      } catch (CoreException e) {
        throw new JavaModelException(e);
      } finally {
        pm.done();
      }
    }

    @Override
    protected JavaRefactoringDescriptor createRefactoringDescriptor(
        JDTRefactoringDescriptorComment comment,
        Map<String, String> arguments,
        String description,
        String project,
        int flags) {
      CreateTargetExecutionLog log = getCreateTargetExecutionLog();
      storeCreateTargetExecutionLog(project, arguments, log);
      return RefactoringSignatureDescriptorFactory.createMoveDescriptor(
          project, description, comment.asString(), arguments, flags);
    }

    @Override
    public IFile[] getAllModifiedFiles() {
      return ReorgUtils.getFiles(
          new IResource[] {
            ReorgUtils.getResource(getSourceCu()),
            ReorgUtils.getResource(getEnclosingCompilationUnit(getJavaElementDestination()))
          });
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
      return fCreateTargetExecutionLog;
    }

    public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
      return null;
    }

    @Override
    protected String getDescriptionPlural() {
      if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.TYPE)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_types;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.FIELD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_fields;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.METHOD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_methods;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.INITIALIZER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_initializers;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.PACKAGE_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_package_declarations;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_CONTAINER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_import_containers;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_import_declarations;
      } else {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_elements_plural;
      }
    }

    @Override
    protected String getDescriptionSingular() {
      if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.TYPE)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_type;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.FIELD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_field;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.METHOD)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_method;
      } else if (!ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.INITIALIZER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_initializer;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.PACKAGE_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_package_declaration;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_CONTAINER)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_import_section;
      } else if (!ReorgUtils.hasElementsNotOfType(
          getJavaElements(), IJavaElement.IMPORT_DECLARATION)) {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_import_declaration;
      } else {
        return RefactoringCoreMessages.ReorgPolicyFactory_move_elements_singular;
      }
    }

    @Override
    protected String getHeaderPatternSingular() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_elements_header_singular;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return RefactoringCoreMessages.ReorgPolicyFactory_move_elements_header_plural;
    }

    @Override
    protected String getProcessorId() {
      return IJavaRefactorings.MOVE;
    }

    @Override
    protected String getRefactoringId() {
      return IJavaRefactorings.MOVE;
    }

    public boolean isTextualMove() {
      return true;
    }

    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm)
        throws CoreException {
      return null;
    }

    public void setDestinationCheck(boolean check) {
      fCheckDestination = check;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement destination, int location)
        throws JavaModelException {
      IJavaElement[] elements = getJavaElements();
      for (int i = 0; i < elements.length; i++) {
        IJavaElement parent = destination.getParent();
        while (parent != null) {
          if (parent.equals(elements[i]))
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          parent = parent.getParent();
        }
      }

      RefactoringStatus superStatus = super.verifyDestination(destination, location);
      if (superStatus.hasFatalError()) return superStatus;

      if (location == IReorgDestination.LOCATION_ON) {
        Object commonParent =
            new ParentChecker(new IResource[0], getJavaElements()).getCommonParent();
        if (destination.equals(commonParent)
            || Arrays.asList(getJavaElements()).contains(destination))
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_element2parent);

        return superStatus;
      } else {
        if (contains(elements, destination))
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_cannot);

        IJavaElement parent = destination.getParent();
        if (!(parent instanceof IType)) return superStatus;

        if (!allInSameParent(elements, parent)) return superStatus;

        ArrayList<IJavaElement> sortedChildren = getSortedChildren((IType) parent);

        int destinationIndex = sortedChildren.indexOf(destination);

        for (int i = 0; i < elements.length; i++) {
          int elementIndex = sortedChildren.indexOf(elements[i]);
          if (location == IReorgDestination.LOCATION_AFTER && elementIndex == destinationIndex + 1)
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);

          if (location == IReorgDestination.LOCATION_BEFORE && elementIndex == destinationIndex - 1)
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);
        }

        return superStatus;
      }
    }

    private ArrayList<IJavaElement> getSortedChildren(IType parent) throws JavaModelException {
      IJavaElement[] children = parent.getChildren();
      ArrayList<IJavaElement> sortedChildren = new ArrayList<IJavaElement>(Arrays.asList(children));
      Collections.sort(
          sortedChildren,
          new Comparator<IJavaElement>() {
            public int compare(IJavaElement e1, IJavaElement e2) {
              if (!(e1 instanceof ISourceReference)) return 0;
              if (!(e2 instanceof ISourceReference)) return 0;

              try {
                ISourceRange sr1 = ((ISourceReference) e1).getSourceRange();
                ISourceRange sr2 = ((ISourceReference) e2).getSourceRange();
                if (sr1 == null || sr2 == null) return 0;

                return sr1.getOffset() - sr2.getOffset();

              } catch (JavaModelException e) {
                return 0;
              }
            }
          });
      return sortedChildren;
    }

    private boolean contains(IJavaElement[] elements, IJavaElement element) {
      for (int i = 0; i < elements.length; i++) {
        if (element.equals(elements[i])) return true;
      }

      return false;
    }

    private boolean allInSameParent(IJavaElement[] elements, IJavaElement parent) {
      for (int i = 0; i < elements.length; i++) {
        if (!elements[i].getParent().equals(parent)) return false;
      }

      return true;
    }

    /** {@inheritDoc} */
    public boolean canEnableQualifiedNameUpdating() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean canUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public void setUpdateQualifiedNames(boolean update) {}

    /** {@inheritDoc} */
    public void setFilePatterns(String patterns) {}

    /** {@inheritDoc} */
    public String getFilePatterns() {
      return null;
    }

    /** {@inheritDoc} */
    public boolean canUpdateJavaReferences() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateReferences() {
      return fUpdateReferences;
    }

    /** {@inheritDoc} */
    public void setUpdateReferences(boolean update) {
      fUpdateReferences = update;
    }

    /** {@inheritDoc} */
    public boolean hasAllInputSet() {
      return getJavaElementDestination() != null || getResourceDestination() != null;
    }
  }

  private static final class MoveMembersPolicy extends MoveSubCuElementsPolicy {

    MoveMembersPolicy(IMember[] members) {
      super(members);
    }

    private static final String POLICY_MOVE_MEMBERS =
        "org.eclipse.jdt.ui.moveMembers"; // $NON-NLS-1$

    /** {@inheritDoc} */
    public String getPolicyId() {
      return POLICY_MOVE_MEMBERS;
    }
  }

  private static final class MoveImportDeclarationsPolicy extends MoveSubCuElementsPolicy {

    MoveImportDeclarationsPolicy(IImportDeclaration[] importDeclarations) {
      super(importDeclarations);
    }

    private static final String POLICY_MOVE_IMPORT_DECLARATIONS =
        "org.eclipse.jdt.ui.moveImportDeclarations"; // $NON-NLS-1$

    /** {@inheritDoc} */
    public String getPolicyId() {
      return POLICY_MOVE_IMPORT_DECLARATIONS;
    }
  }

  private static final class NewNameProposer {

    private static boolean isNewNameOk(IContainer container, String newName) {
      return container.findMember(newName) == null;
    }

    private static boolean isNewNameOk(IPackageFragment dest, String newName) {
      return !dest.getCompilationUnit(newName).exists();
    }

    private static boolean isNewNameOk(IPackageFragmentRoot root, String newName) {
      return !root.getPackageFragment(newName).exists();
    }

    private final Set<String> fAutoGeneratedNewNames = new HashSet<String>(2);

    public String createNewName(ICompilationUnit cu, IPackageFragment destination) {
      if (isNewNameOk(destination, cu.getElementName())) return null;
      if (!ReorgUtils.isParentInWorkspaceOrOnDisk(cu, destination)) return null;
      int i = 1;
      while (true) {
        String newName;
        if (i == 1)
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_cu_copyOf1,
                  cu.getElementName()); // Don't use BasicElementLabels! No RTL!
        else
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_cu_copyOfMore,
                  new String[] {
                    String.valueOf(i), cu.getElementName()
                  }); // Don't use BasicElementLabels! No RTL!
        if (isNewNameOk(destination, newName) && !fAutoGeneratedNewNames.contains(newName)) {
          fAutoGeneratedNewNames.add(newName);
          return JavaCore.removeJavaLikeExtension(newName);
        }
        i++;
      }
    }

    public String createNewName(IPackageFragment pack, IPackageFragmentRoot destination) {
      if (isNewNameOk(destination, pack.getElementName())) return null;
      if (!ReorgUtils.isParentInWorkspaceOrOnDisk(pack, destination)) return null;
      int i = 1;
      while (true) {
        String newName;
        if (i == 1)
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_package_copyOf1,
                  pack.getElementName()); // Don't use
        // BasicElementLabels! No RTL!
        else
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_package_copyOfMore,
                  new String[] {
                    String.valueOf(i), pack.getElementName()
                  }); // Don't use BasicElementLabels! No RTL!
        if (isNewNameOk(destination, newName) && !fAutoGeneratedNewNames.contains(newName)) {
          fAutoGeneratedNewNames.add(newName);
          return newName;
        }
        i++;
      }
    }

    public String createNewName(IResource res, IContainer destination) {
      if (isNewNameOk(destination, res.getName())) return null;
      if (!ReorgUtils.isParentInWorkspaceOrOnDisk(res, destination)) return null;
      int i = 1;
      while (true) {
        String newName;
        if (i == 1)
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_resource_copyOf1,
                  res.getName()); // Don't use BasicElementLabels! No RTL!
        else
          newName =
              Messages.format(
                  RefactoringCoreMessages.CopyRefactoring_resource_copyOfMore,
                  new String[] {
                    String.valueOf(i), res.getName()
                  }); // Don't use BasicElementLabels! No RTL!
        if (isNewNameOk(destination, newName) && !fAutoGeneratedNewNames.contains(newName)) {
          fAutoGeneratedNewNames.add(newName);
          return newName;
        }
        i++;
      }
    }
  }

  private static final class NoCopyPolicy extends ReorgPolicy implements ICopyPolicy {

    @Override
    public boolean canEnable() throws JavaModelException {
      return false;
    }

    public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries) {
      return new NullChange();
    }

    @Override
    protected String getDescriptionPlural() {
      return UNUSED_STRING;
    }

    @Override
    protected String getDescriptionSingular() {
      return UNUSED_STRING;
    }

    public ChangeDescriptor getDescriptor() {
      return null;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return UNUSED_STRING;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return UNUSED_STRING;
    }

    public IJavaElement[] getJavaElements() {
      return new IJavaElement[0];
    }

    public String getPolicyId() {
      return NO_POLICY;
    }

    @Override
    protected String getProcessorId() {
      return NO_ID;
    }

    @Override
    protected String getRefactoringId() {
      return NO_ID;
    }

    public ReorgExecutionLog getReorgExecutionLog() {
      return null;
    }

    public IResource[] getResources() {
      return new IResource[0];
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_noCopying);
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource resource) throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_noCopying);
    }
  }

  private static final class NoMovePolicy extends ReorgPolicy implements IMovePolicy {

    @Override
    public boolean canEnable() throws JavaModelException {
      return false;
    }

    public Change createChange(IProgressMonitor pm) {
      return new NullChange();
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
      return new CreateTargetExecutionLog();
    }

    public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
      return null;
    }

    @Override
    protected String getDescriptionPlural() {
      return UNUSED_STRING;
    }

    @Override
    protected String getDescriptionSingular() {
      return UNUSED_STRING;
    }

    public ChangeDescriptor getDescriptor() {
      return null;
    }

    @Override
    protected String getHeaderPatternSingular() {
      return UNUSED_STRING;
    }

    @Override
    protected String getHeaderPatternPlural() {
      return UNUSED_STRING;
    }

    public IJavaElement[] getJavaElements() {
      return new IJavaElement[0];
    }

    public String getPolicyId() {
      return NO_POLICY;
    }

    @Override
    protected String getProcessorId() {
      return NO_ID;
    }

    @Override
    protected String getRefactoringId() {
      return NO_ID;
    }

    public IResource[] getResources() {
      return new IResource[0];
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      return new RefactoringStatus();
    }

    public boolean isTextualMove() {
      return true;
    }

    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm)
        throws CoreException {
      return null;
    }

    public void setDestinationCheck(boolean check) {
      fCheckDestination = check;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_noMoving);
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource resource) throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_noMoving);
    }

    /** {@inheritDoc} */
    public boolean canEnableQualifiedNameUpdating() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean canUpdateQualifiedNames() {
      return false;
    }

    /** {@inheritDoc} */
    public void setUpdateQualifiedNames(boolean update) {}

    /** {@inheritDoc} */
    public void setFilePatterns(String patterns) {}

    /** {@inheritDoc} */
    public String getFilePatterns() {
      return null;
    }

    /** {@inheritDoc} */
    public boolean canUpdateJavaReferences() {
      return false;
    }

    /** {@inheritDoc} */
    public boolean getUpdateReferences() {
      return false;
    }

    /** {@inheritDoc} */
    public void setUpdateReferences(boolean update) {}

    /** {@inheritDoc} */
    public boolean hasAllInputSet() {
      return getJavaElementDestination() != null || getResourceDestination() != null;
    }
  }

  private abstract static class PackageFragmentRootsReorgPolicy extends ReorgPolicy {

    private IPackageFragmentRoot[] fPackageFragmentRoots;

    public PackageFragmentRootsReorgPolicy(IPackageFragmentRoot[] roots) {
      fPackageFragmentRoots = roots;
    }

    @Override
    public boolean canChildrenBeDestinations(IJavaElement javaElement) {
      switch (javaElement.getElementType()) {
        case IJavaElement.JAVA_MODEL:
        case IJavaElement.JAVA_PROJECT:
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean canChildrenBeDestinations(IResource resource) {
      return resource instanceof IContainer;
    }

    @Override
    public boolean canElementBeDestination(IJavaElement javaElement) {
      return javaElement.getElementType() == IJavaElement.JAVA_PROJECT;
    }

    @Override
    public boolean canElementBeDestination(IResource resource) {
      return resource instanceof IContainer;
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      if (!super.canEnable() || fPackageFragmentRoots.length == 0) return false;
      for (int i = 0; i < fPackageFragmentRoots.length; i++) {
        IPackageFragmentRoot root = fPackageFragmentRoots[i];
        if (!(ReorgUtils.isSourceFolder(root) || root.isArchive() && !root.isExternal()))
          return false;
      }
      if (ReorgUtils.containsLinkedResources(fPackageFragmentRoots)) return false;
      return true;
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      confirmOverwriting(reorgQueries);
      RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
      return status;
    }

    private void confirmOverwriting(IReorgQueries reorgQueries) {
      OverwriteHelper oh = new OverwriteHelper();
      oh.setPackageFragmentRoots(fPackageFragmentRoots);
      IJavaProject javaProject = getDestinationJavaProject();
      if (javaProject == null) {
        oh.confirmOverwriting(reorgQueries, getResourceDestination());
      } else {
        oh.confirmOverwriting(reorgQueries, javaProject);
      }
      fPackageFragmentRoots = oh.getPackageFragmentRootsWithoutUnconfirmedOnes();
    }

    protected abstract JavaRefactoringDescriptor createRefactoringDescriptor(
        final JDTRefactoringDescriptorComment comment,
        final Map<String, String> arguments,
        final String description,
        final String project,
        int flags);

    public final ChangeDescriptor getDescriptor() {
      final Map<String, String> arguments = new HashMap<String, String>();
      final int length = fPackageFragmentRoots.length;
      final String description = length == 1 ? getDescriptionSingular() : getDescriptionPlural();
      final IProject resource = getSingleProject();
      final String project = resource != null ? resource.getName() : null;
      final String header =
          length == 1
              ? Messages.format(
                  getHeaderPatternSingular(),
                  new String[] {fPackageFragmentRoots[0].getElementName(), getDestinationLabel()})
              : Messages.format(
                  getHeaderPatternPlural(),
                  new String[] {String.valueOf(length), getDestinationLabel()});
      int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      arguments.put(ATTRIBUTE_POLICY, getPolicyId());
      arguments.put(ATTRIBUTE_ROOTS, new Integer(fPackageFragmentRoots.length).toString());
      for (int offset = 0; offset < fPackageFragmentRoots.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + 1),
            JavaRefactoringDescriptorUtil.elementToHandle(project, fPackageFragmentRoots[offset]));
      arguments.putAll(getRefactoringArguments(project));
      final JavaRefactoringDescriptor descriptor =
          createRefactoringDescriptor(comment, arguments, description, project, flags);
      return new RefactoringChangeDescriptor(descriptor);
    }

    private IJavaProject getDestinationAsJavaProject(IJavaElement javaElementDestination) {
      if (javaElementDestination == null) return null;
      else return javaElementDestination.getJavaProject();
    }

    protected IJavaProject getDestinationJavaProject() {
      return getDestinationAsJavaProject(getJavaElementDestination());
    }

    public IJavaElement[] getJavaElements() {
      return fPackageFragmentRoots;
    }

    protected IPackageFragmentRoot[] getPackageFragmentRoots() {
      return fPackageFragmentRoots;
    }

    public IResource[] getResources() {
      return new IResource[0];
    }

    public IPackageFragmentRoot[] getRoots() {
      return fPackageFragmentRoots;
    }

    private IProject getSingleProject() {
      IProject result = null;
      for (int index = 0; index < fPackageFragmentRoots.length; index++) {
        if (result == null) result = fPackageFragmentRoots[index].getJavaProject().getProject();
        else if (!result.equals(fPackageFragmentRoots[index].getJavaProject().getProject()))
          return null;
      }
      return result;
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      final RefactoringStatus status = new RefactoringStatus();
      int rootCount = 0;
      String value = arguments.getAttribute(ATTRIBUTE_ROOTS);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          rootCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_ROOTS));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_ROOTS));
      String handle = null;
      List<IJavaElement> elements = new ArrayList<IJavaElement>();
      for (int index = 0; index < rootCount; index++) {
        final String attribute = JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IJavaElement element =
              JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
          if (element == null
              || !element.exists()
              || element.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT)
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    element, getProcessorId(), getRefactoringId()));
          else elements.add(element);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fPackageFragmentRoots = elements.toArray(new IPackageFragmentRoot[elements.size()]);
      status.merge(super.initialize(arguments));
      return status;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      Assert.isNotNull(javaElement);
      if (!fCheckDestination) return new RefactoringStatus();
      if (!javaElement.exists())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot1);
      if (javaElement instanceof IJavaModel)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_jmodel);
      if (!(javaElement instanceof IJavaProject))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_src2proj);
      if (javaElement.isReadOnly())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_src2writable);
      if (ReorgUtils.isPackageFragmentRoot(javaElement.getJavaProject()))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_src2nosrc);
      return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource resource) {
      Assert.isNotNull(resource);
      if (!resource.exists() || resource.isPhantom())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_phantom);
      if (!resource.isAccessible())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_inaccessible);

      if (!(resource instanceof IContainer))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (resource.getType() == IResource.ROOT)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (isChildOfOrEqualToAnyFolder(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (containsLinkedResources() && !ReorgUtils.canBeDestinationForLinkedResources(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_linked);

      return new RefactoringStatus();
    }

    private boolean isChildOfOrEqualToAnyFolder(IResource resource) {
      for (int i = 0; i < fPackageFragmentRoots.length; i++) {
        IResource fragmentRootResource = fPackageFragmentRoots[i].getResource();
        if (fragmentRootResource.equals(resource)
            || ParentChecker.isDescendantOf(resource, fragmentRootResource)) return true;
      }
      return false;
    }

    protected boolean containsLinkedResources() {
      for (int i = 0; i < fPackageFragmentRoots.length; i++) {
        if (fPackageFragmentRoots[i].getResource().isLinked()) return true;
      }
      return false;
    }
  }

  private abstract static class PackagesReorgPolicy extends ReorgPolicy {

    private IPackageFragment[] fPackageFragments;

    public PackagesReorgPolicy(IPackageFragment[] packageFragments) {
      fPackageFragments = packageFragments;
    }

    @Override
    public boolean canChildrenBeDestinations(IJavaElement javaElement) {
      switch (javaElement.getElementType()) {
        case IJavaElement.JAVA_MODEL:
        case IJavaElement.JAVA_PROJECT:
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          // can be nested
          // (with exclusion
          // filters)
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean canChildrenBeDestinations(IResource resource) {
      return resource instanceof IContainer;
    }

    @Override
    public boolean canElementBeDestination(IJavaElement javaElement) {
      switch (javaElement.getElementType()) {
        case IJavaElement.JAVA_PROJECT:
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean canElementBeDestination(IResource resource) {
      return resource instanceof IContainer;
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      if (fPackageFragments.length == 0) return false;
      for (int i = 0; i < fPackageFragments.length; i++) {
        if (JavaElementUtil.isDefaultPackage(fPackageFragments[i])
            || fPackageFragments[i].isReadOnly()) return false;
      }
      if (ReorgUtils.containsLinkedResources(fPackageFragments)) return false;
      return true;
    }

    @Override
    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      confirmOverwriting(reorgQueries);
      RefactoringStatus refactoringStatus = super.checkFinalConditions(pm, context, reorgQueries);
      return refactoringStatus;
    }

    private void confirmOverwriting(IReorgQueries reorgQueries) throws JavaModelException {
      OverwriteHelper helper = new OverwriteHelper();
      helper.setPackages(fPackageFragments);
      IPackageFragmentRoot destRoot = getDestinationAsPackageFragmentRoot();
      if (destRoot == null) {
        helper.confirmOverwriting(reorgQueries, getResourceDestination());
      } else {
        helper.confirmOverwriting(reorgQueries, destRoot);
      }
      fPackageFragments = helper.getPackagesWithoutUnconfirmedOnes();
    }

    protected abstract JavaRefactoringDescriptor createRefactoringDescriptor(
        final JDTRefactoringDescriptorComment comment,
        final Map<String, String> arguments,
        final String description,
        final String project,
        int flags);

    public final ChangeDescriptor getDescriptor() {
      final Map<String, String> arguments = new HashMap<String, String>();
      final int length = fPackageFragments.length;
      final String description = length == 1 ? getDescriptionSingular() : getDescriptionPlural();
      final IProject resource = getSingleProject();
      final String project = resource != null ? resource.getName() : null;
      final String header =
          length == 1
              ? Messages.format(
                  getHeaderPatternSingular(),
                  new String[] {(fPackageFragments[0]).getElementName(), getDestinationLabel()})
              : Messages.format(
                  getHeaderPatternPlural(),
                  new String[] {String.valueOf(length), getDestinationLabel()});
      int flags =
          JavaRefactoringDescriptor.JAR_REFACTORING
              | JavaRefactoringDescriptor.JAR_MIGRATION
              | RefactoringDescriptor.STRUCTURAL_CHANGE
              | RefactoringDescriptor.MULTI_CHANGE;
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      arguments.put(ATTRIBUTE_POLICY, getPolicyId());
      arguments.put(ATTRIBUTE_FRAGMENTS, new Integer(fPackageFragments.length).toString());
      for (int offset = 0; offset < fPackageFragments.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + 1),
            JavaRefactoringDescriptorUtil.elementToHandle(project, fPackageFragments[offset]));
      arguments.putAll(getRefactoringArguments(project));
      final JavaRefactoringDescriptor descriptor =
          createRefactoringDescriptor(comment, arguments, description, project, flags);
      return new RefactoringChangeDescriptor(descriptor);
    }

    protected IPackageFragmentRoot getDestinationAsPackageFragmentRoot() throws JavaModelException {
      return getDestinationAsPackageFragmentRoot(getJavaElementDestination());
    }

    private IPackageFragmentRoot getDestinationAsPackageFragmentRoot(IJavaElement javaElement)
        throws JavaModelException {
      if (javaElement == null) return null;

      if (javaElement instanceof IPackageFragmentRoot) return (IPackageFragmentRoot) javaElement;

      if (javaElement instanceof IPackageFragment) {
        IPackageFragment pack = (IPackageFragment) javaElement;
        if (pack.getParent() instanceof IPackageFragmentRoot)
          return (IPackageFragmentRoot) pack.getParent();
      }

      if (javaElement instanceof IJavaProject)
        return ReorgUtils.getCorrespondingPackageFragmentRoot((IJavaProject) javaElement);
      return null;
    }

    public IJavaElement[] getJavaElements() {
      return fPackageFragments;
    }

    protected IPackageFragment[] getPackages() {
      return fPackageFragments;
    }

    public IResource[] getResources() {
      return new IResource[0];
    }

    private IProject getSingleProject() {
      IProject result = null;
      for (int index = 0; index < fPackageFragments.length; index++) {
        if (result == null) result = fPackageFragments[index].getJavaProject().getProject();
        else if (!result.equals(fPackageFragments[index].getJavaProject().getProject()))
          return null;
      }
      return result;
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      final RefactoringStatus status = new RefactoringStatus();
      int fragmentCount = 0;
      String value = arguments.getAttribute(ATTRIBUTE_FRAGMENTS);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          fragmentCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_FRAGMENTS));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_FRAGMENTS));
      String handle = null;
      List<IJavaElement> elements = new ArrayList<IJavaElement>();
      for (int index = 0; index < fragmentCount; index++) {
        final String attribute = JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IJavaElement element =
              JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
          if (element == null
              || !element.exists()
              || element.getElementType() != IJavaElement.PACKAGE_FRAGMENT)
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    element, getProcessorId(), getRefactoringId()));
          else elements.add(element);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fPackageFragments = elements.toArray(new IPackageFragment[elements.size()]);
      status.merge(super.initialize(arguments));
      return status;
    }

    @Override
    protected RefactoringStatus verifyDestination(IJavaElement javaElement)
        throws JavaModelException {
      Assert.isNotNull(javaElement);
      if (!fCheckDestination) return new RefactoringStatus();
      if (!javaElement.exists())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot1);
      if (javaElement instanceof IJavaModel)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_jmodel);
      IPackageFragmentRoot destRoot = getDestinationAsPackageFragmentRoot(javaElement);
      if (!ReorgUtils.isSourceFolder(destRoot))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_packages);
      return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus verifyDestination(IResource resource) {
      Assert.isNotNull(resource);
      if (!resource.exists() || resource.isPhantom())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_phantom);
      if (!resource.isAccessible())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_inaccessible);

      if (!(resource instanceof IContainer))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (resource.getType() == IResource.ROOT)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (isChildOfOrEqualToAnyFolder(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_not_this_resource);

      if (containsLinkedResources() && !ReorgUtils.canBeDestinationForLinkedResources(resource))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_linked);

      return new RefactoringStatus();
    }

    private boolean isChildOfOrEqualToAnyFolder(IResource resource) {
      for (int i = 0; i < fPackageFragments.length; i++) {
        IFolder folder = (IFolder) fPackageFragments[i].getResource();
        if (folder.equals(resource) || ParentChecker.isDescendantOf(resource, folder)) return true;
      }
      return false;
    }

    protected boolean containsLinkedResources() {
      for (int i = 0; i < fPackageFragments.length; i++) {
        if (fPackageFragments[i].getResource().isLinked()) return true;
      }
      return false;
    }
  }

  private abstract static class ReorgPolicy implements IReorgPolicy {

    private static final String ATTRIBUTE_DESTINATION = "destination"; // $NON-NLS-1$

    private static final String ATTRIBUTE_TARGET = "target"; // $NON-NLS-1$

    protected boolean fCheckDestination = true;

    private IReorgDestination fDestination;

    /** {@inheritDoc} */
    public boolean canChildrenBeDestinations(IReorgDestination destination) {
      if (destination instanceof JavaElementDestination) {
        return canChildrenBeDestinations(((JavaElementDestination) destination).getJavaElement());
      } else if (destination instanceof ResourceDestination) {
        return canChildrenBeDestinations(((ResourceDestination) destination).getResource());
      }

      return false;
    }

    /**
     * Is it possible, that resource contains valid destinations as children?
     *
     * @param resource the resource to verify
     * @return true if resource can have valid destinations
     */
    public boolean canChildrenBeDestinations(IResource resource) {
      return true;
    }

    /**
     * Is it possible, that resource contains valid destinations as children?
     *
     * @param javaElement the java element to verify
     * @return true if resource can have valid destinations
     */
    public boolean canChildrenBeDestinations(IJavaElement javaElement) {
      return true;
    }

    /** {@inheritDoc} */
    public boolean canElementBeDestination(IReorgDestination destination) {
      if (destination instanceof JavaElementDestination) {
        return canElementBeDestination(((JavaElementDestination) destination).getJavaElement());
      } else if (destination instanceof ResourceDestination) {
        return canElementBeDestination(((ResourceDestination) destination).getResource());
      }

      return false;
    }

    /**
     * Is it possible, that sources can be reorged to this kind of resource?
     *
     * <p>This is less strict then {@link #verifyDestination(IResource)} where the resource itself
     * is verified to be a valid destination.
     *
     * @param resource the resource to move to
     * @return true if possible
     */
    public boolean canElementBeDestination(IResource resource) {
      return true;
    }

    /**
     * Is it possible, that sources can be reorged to this kind of javaElement?
     *
     * <p>This is less strict then {@link #verifyDestination(IJavaElement)} where the java element
     * itself is verified to be a valid destination.
     *
     * @param javaElement the java element to move to
     * @return true if possible
     */
    public boolean canElementBeDestination(IJavaElement javaElement) {
      return true;
    }

    public boolean canEnable() throws JavaModelException {
      IResource[] resources = getResources();
      for (int i = 0; i < resources.length; i++) {
        IResource resource = resources[i];
        if (!resource.exists() || resource.isPhantom() || !resource.isAccessible()) return false;
      }

      IJavaElement[] javaElements = getJavaElements();
      for (int i = 0; i < javaElements.length; i++) {
        IJavaElement element = javaElements[i];
        if (!element.exists()) return false;
      }
      return resources.length > 0 || javaElements.length > 0;
    }

    /** {@inheritDoc} */
    public int getSaveMode() {
      // return RefactoringSaveHelper.SAVE_ALL;
      return 2;
    }

    public RefactoringStatus checkFinalConditions(
        IProgressMonitor pm, CheckConditionsContext context, IReorgQueries reorgQueries)
        throws CoreException {
      Assert.isNotNull(reorgQueries);
      ResourceChangeChecker checker =
          (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
      IFile[] allModifiedFiles = getAllModifiedFiles();
      RefactoringModifications modifications = getModifications();
      IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
      for (int i = 0; i < allModifiedFiles.length; i++) {
        deltaFactory.change(allModifiedFiles[i]);
      }
      if (modifications != null) {
        modifications.buildDelta(deltaFactory);
        modifications.buildValidateEdits(
            (ValidateEditChecker) context.getChecker(ValidateEditChecker.class));
      }
      return new RefactoringStatus();
    }

    protected IFile[] getAllModifiedFiles() {
      return new IFile[0];
    }

    protected abstract String getDescriptionPlural();

    protected abstract String getDescriptionSingular();

    protected String getDestinationLabel() {
      Object destination = getJavaElementDestination();
      if (destination == null) destination = getResourceDestination();
      return JavaElementLabels.getTextLabel(destination, JavaElementLabels.ALL_FULLY_QUALIFIED);
    }

    protected abstract String getHeaderPatternSingular();

    protected abstract String getHeaderPatternPlural();

    public final IJavaElement getJavaElementDestination() {
      if (!(fDestination instanceof JavaElementDestination)) return null;

      JavaElementDestination javaElementDestination = (JavaElementDestination) fDestination;
      return javaElementDestination.getJavaElement();
    }

    public final int getLocation() {
      return fDestination.getLocation();
    }

    /**
     * Returns the modifications
     *
     * @return the modifications
     * @throws CoreException if creating the modifications failed
     */
    protected RefactoringModifications getModifications() throws CoreException {
      return null;
    }

    protected abstract String getProcessorId();

    protected Map<String, String> getRefactoringArguments(String project) {
      final Map<String, String> arguments = new HashMap<String, String>();
      final IJavaElement element = getJavaElementDestination();
      if (element != null)
        arguments.put(
            ATTRIBUTE_DESTINATION, JavaRefactoringDescriptorUtil.elementToHandle(project, element));
      else {
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=157479
        final IResource resource = getResourceDestination();
        if (resource != null)
          arguments.put(
              ATTRIBUTE_TARGET, JavaRefactoringDescriptorUtil.resourceToHandle(null, resource));
      }
      return arguments;
    }

    protected abstract String getRefactoringId();

    public final IResource getResourceDestination() {
      if (!(fDestination instanceof ResourceDestination)) return null;

      ResourceDestination resourceDestination = (ResourceDestination) fDestination;
      return resourceDestination.getResource();
    }

    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      String handle = arguments.getAttribute(ATTRIBUTE_DESTINATION);
      if (handle != null) {
        final IJavaElement element =
            JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
        if (element != null) {
          if (fCheckDestination && !element.exists())
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(
                element, getProcessorId(), getRefactoringId());
          else {
            try {
              IReorgDestination destination = ReorgDestinationFactory.createDestination(element);
              setDestination(destination);
              return verifyDestination(destination);
            } catch (JavaModelException exception) {
              JavaPlugin.log(exception);
              return RefactoringStatus.createFatalErrorStatus(
                  Messages.format(
                      RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                      new String[] {handle, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT}));
            }
          }
        } else {
          // Leave for compatibility
          // https://bugs.eclipse.org/bugs/show_bug.cgi?id=157479
          final IResource resource =
              JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), handle);
          if (resource == null || fCheckDestination && !resource.exists())
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(
                resource, getProcessorId(), getRefactoringId());
          else {
            try {
              IReorgDestination destination = ReorgDestinationFactory.createDestination(resource);
              setDestination(destination);
              return verifyDestination(destination);
            } catch (JavaModelException exception) {
              JavaPlugin.log(exception);
              return RefactoringStatus.createFatalErrorStatus(
                  Messages.format(
                      RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                      new String[] {handle, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT}));
            }
          }
        }
      } else {
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=157479
        handle = arguments.getAttribute(ATTRIBUTE_TARGET);
        if (handle != null) {
          final IResource resource =
              JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), handle);
          if (resource == null || fCheckDestination && !resource.exists())
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(
                resource, getProcessorId(), getRefactoringId());
          else {
            try {
              IReorgDestination destination = ReorgDestinationFactory.createDestination(resource);
              setDestination(destination);
              return verifyDestination(destination);
            } catch (JavaModelException exception) {
              JavaPlugin.log(exception);
              return RefactoringStatus.createFatalErrorStatus(
                  Messages.format(
                      RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                      new String[] {handle, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT}));
            }
          }
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
      }
    }

    public final RefactoringParticipant[] loadParticipants(
        RefactoringStatus status,
        RefactoringProcessor processor,
        String[] natures,
        SharableParticipants shared)
        throws CoreException {
      RefactoringModifications modifications = getModifications();
      if (modifications != null) {
        return modifications.loadParticipants(status, processor, natures, shared);
      } else {
        return new RefactoringParticipant[0];
      }
    }

    /** {@inheritDoc} */
    public final void setDestination(IReorgDestination destination) {
      fDestination = destination;
    }

    /** {@inheritDoc} */
    public RefactoringStatus verifyDestination(IReorgDestination destination)
        throws JavaModelException {
      if (destination instanceof JavaElementDestination) {
        return verifyDestination(((JavaElementDestination) destination).getJavaElement());
      } else if (destination instanceof ResourceDestination) {
        return verifyDestination(((ResourceDestination) destination).getResource());
      }

      return RefactoringStatus.createErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_invalidDestinationKind);
    }

    /**
     * Can destination be a target for the given source elements?
     *
     * @param destination the destination to verify
     * @return OK status if valid destination
     * @throws JavaModelException should not happen
     */
    protected RefactoringStatus verifyDestination(IJavaElement destination)
        throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_no_java_element);
    }

    /**
     * Can destination be a target for the given source elements?
     *
     * @param destination the destination to verify
     * @return OK status if valid destination
     * @throws JavaModelException should not happen
     */
    protected RefactoringStatus verifyDestination(IResource destination) throws JavaModelException {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.ReorgPolicyFactory_no_resource);
    }
  }

  private abstract static class SubCuElementReorgPolicy extends ReorgPolicy {

    protected static CompilationUnitChange createCompilationUnitChange(
        CompilationUnitRewrite rewrite) throws CoreException {
      CompilationUnitChange change = rewrite.createChange(true);
      if (change != null) change.setSaveMode(TextFileChange.KEEP_SAVE_STATE);

      return change;
    }

    /*
     * The CU which does contain element
     */
    protected static final ICompilationUnit getEnclosingCompilationUnit(IJavaElement element) {
      if (element instanceof ICompilationUnit) return (ICompilationUnit) element;

      return (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
    }

    private static IType getEnclosingType(IJavaElement element) {
      if (element instanceof IType) return (IType) element;

      return (IType) element.getAncestor(IJavaElement.TYPE);
    }

    private static String getUnindentedSource(ISourceReference sourceReference)
        throws JavaModelException {

      String[] lines = Strings.convertIntoLines(sourceReference.getSource());
      final IJavaProject project = ((IJavaElement) sourceReference).getJavaProject();
      Strings.trimIndentation(lines, project, false);
      return Strings.concatenate(
          lines, StubUtility.getLineDelimiterUsed((IJavaElement) sourceReference));
    }

    private IJavaElement[] fJavaElements;

    SubCuElementReorgPolicy(IJavaElement[] javaElements) {
      fJavaElements = javaElements;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canChildrenBeDestinations(IResource resource) {
      return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canElementBeDestination(IJavaElement javaElement) {
      return true;
    }

    @Override
    public boolean canEnable() throws JavaModelException {
      if (!super.canEnable() || fJavaElements.length == 0) return false;

      for (int i = 0; i < fJavaElements.length; i++) {
        if (fJavaElements[i] instanceof IMember) {
          IMember member = (IMember) fJavaElements[i];
          // we can copy some binary members, but not all
          if (member.isBinary() && member.getSourceRange() == null) return false;
        }
      }

      return true;
    }

    /** {@inheritDoc} */
    @Override
    public int getSaveMode() {
      // return RefactoringSaveHelper.SAVE_REFACTORING;
      return 4;
    }

    private void copyImportsToDestination(
        IImportContainer container,
        ASTRewrite rewrite,
        CompilationUnit sourceCuNode,
        CompilationUnit destinationCuNode)
        throws JavaModelException {
      ListRewrite listRewrite =
          rewrite.getListRewrite(destinationCuNode, CompilationUnit.IMPORTS_PROPERTY);

      IJavaElement[] importDeclarations = container.getChildren();
      for (int i = 0; i < importDeclarations.length; i++) {
        IImportDeclaration declaration = (IImportDeclaration) importDeclarations[i];

        ImportDeclaration sourceNode =
            ASTNodeSearchUtil.getImportDeclarationNode(declaration, sourceCuNode);
        ImportDeclaration copiedNode =
            (ImportDeclaration) ASTNode.copySubtree(rewrite.getAST(), sourceNode);

        if (getLocation() == IReorgDestination.LOCATION_BEFORE) {
          listRewrite.insertFirst(copiedNode, null);
        } else {
          listRewrite.insertLast(copiedNode, null);
        }
      }
    }

    private void copyImportToDestination(
        IImportDeclaration declaration,
        ASTRewrite targetRewrite,
        CompilationUnit sourceCuNode,
        CompilationUnit destinationCuNode)
        throws JavaModelException {
      ImportDeclaration sourceNode =
          ASTNodeSearchUtil.getImportDeclarationNode(declaration, sourceCuNode);
      ImportDeclaration copiedNode =
          (ImportDeclaration) ASTNode.copySubtree(targetRewrite.getAST(), sourceNode);
      ListRewrite listRewrite =
          targetRewrite.getListRewrite(destinationCuNode, CompilationUnit.IMPORTS_PROPERTY);

      if (getJavaElementDestination() instanceof IImportDeclaration) {
        ImportDeclaration destinationNode =
            ASTNodeSearchUtil.getImportDeclarationNode(
                (IImportDeclaration) getJavaElementDestination(), destinationCuNode);
        insertRelative(copiedNode, destinationNode, listRewrite);
      } else {
        // dropped on container, we could be better here
        listRewrite.insertLast(copiedNode, null);
      }
    }

    private void copyInitializerToDestination(
        IInitializer initializer,
        CompilationUnitRewrite targetRewriter,
        CompilationUnit sourceCuNode,
        CompilationUnit targetCuNode)
        throws JavaModelException {
      BodyDeclaration newInitializer =
          (BodyDeclaration)
              targetRewriter
                  .getASTRewrite()
                  .createStringPlaceholder(getUnindentedSource(initializer), ASTNode.INITIALIZER);
      copyMemberToDestination(
          initializer, targetRewriter, sourceCuNode, targetCuNode, newInitializer);
    }

    private void copyMemberToDestination(
        IMember member,
        CompilationUnitRewrite targetRewriter,
        CompilationUnit sourceCuNode,
        CompilationUnit targetCuNode,
        BodyDeclaration newMember)
        throws JavaModelException {
      IJavaElement javaElementDestination = getJavaElementDestination();
      ASTNode nodeDestination = getDestinationNode(javaElementDestination, targetCuNode);
      ASTNode destinationContainer;
      if (getLocation() == IReorgDestination.LOCATION_ON
          && (javaElementDestination instanceof IType
              || javaElementDestination instanceof ICompilationUnit)) {
        destinationContainer = nodeDestination;
      } else {
        destinationContainer = nodeDestination.getParent();
      }

      ListRewrite listRewrite;
      if (destinationContainer instanceof AbstractTypeDeclaration) {
        if (newMember instanceof EnumConstantDeclaration
            && destinationContainer instanceof EnumDeclaration) {
          listRewrite =
              targetRewriter
                  .getASTRewrite()
                  .getListRewrite(destinationContainer, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
        } else {
          listRewrite =
              targetRewriter
                  .getASTRewrite()
                  .getListRewrite(
                      destinationContainer,
                      ((AbstractTypeDeclaration) destinationContainer)
                          .getBodyDeclarationsProperty());
        }
      } else if (destinationContainer instanceof CompilationUnit) {
        listRewrite =
            targetRewriter
                .getASTRewrite()
                .getListRewrite(destinationContainer, CompilationUnit.TYPES_PROPERTY);
      } else if (destinationContainer instanceof Block) {
        listRewrite =
            targetRewriter
                .getASTRewrite()
                .getListRewrite(destinationContainer, Block.STATEMENTS_PROPERTY);
      } else if (destinationContainer instanceof SwitchStatement) {
        listRewrite =
            targetRewriter
                .getASTRewrite()
                .getListRewrite(destinationContainer, SwitchStatement.STATEMENTS_PROPERTY);
      } else if (destinationContainer instanceof AnonymousClassDeclaration) {
        listRewrite =
            targetRewriter
                .getASTRewrite()
                .getListRewrite(
                    destinationContainer, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
      } else {
        listRewrite = null;
        Assert.isLegal(false);
      }

      if (getLocation() == IReorgDestination.LOCATION_ON) {
        listRewrite.insertAt(
            newMember, ASTNodes.getInsertionIndex(newMember, listRewrite.getRewrittenList()), null);
      } else {
        insertRelative(newMember, nodeDestination, listRewrite);
      }

      if (!(member instanceof IInitializer)) {
        BodyDeclaration decl = ASTNodeSearchUtil.getBodyDeclarationNode(member, sourceCuNode);
        if (decl != null) {
          ImportRewriteContext context =
              new ContextSensitiveImportRewriteContext(
                  destinationContainer, targetRewriter.getImportRewrite());
          ImportRewriteUtil.addImports(
              targetRewriter,
              context,
              decl,
              new HashMap<Name, String>(),
              new HashMap<Name, String>(),
              false);
        }
      }
    }

    /*
     * Get the 'destination' in 'target' as ASTNode or 'null'
     */
    private ASTNode getDestinationNode(IJavaElement destination, CompilationUnit target)
        throws JavaModelException {
      switch (destination.getElementType()) {
        case IJavaElement.INITIALIZER:
          return ASTNodeSearchUtil.getInitializerNode((IInitializer) destination, target);
        case IJavaElement.FIELD:
          return ASTNodeSearchUtil.getFieldOrEnumConstantDeclaration((IField) destination, target);
        case IJavaElement.METHOD:
          return ASTNodeSearchUtil.getMethodOrAnnotationTypeMemberDeclarationNode(
              (IMethod) destination, target);
        case IJavaElement.TYPE:
          IType typeDestination = (IType) destination;
          if (typeDestination.isAnonymous()) {
            return ASTNodeSearchUtil.getClassInstanceCreationNode(typeDestination, target)
                .getAnonymousClassDeclaration();
          } else {
            return ASTNodeSearchUtil.getAbstractTypeDeclarationNode(typeDestination, target);
          }
        case IJavaElement.COMPILATION_UNIT:
          IType mainType = JavaElementUtil.getMainType((ICompilationUnit) destination);
          if (mainType != null) {
            return ASTNodeSearchUtil.getAbstractTypeDeclarationNode(mainType, target);
          }
          // $FALL-THROUGH$
        default:
          return null;
      }
    }

    private void insertRelative(ASTNode newNode, ASTNode relativeNode, ListRewrite listRewrite) {
      final List<?> list = listRewrite.getOriginalList();
      final int index = list.indexOf(relativeNode);

      if (getLocation() == IReorgDestination.LOCATION_BEFORE) {
        listRewrite.insertBefore(newNode, (ASTNode) list.get(index), null);
      } else if (index + 1 < list.size()) {
        listRewrite.insertBefore(newNode, (ASTNode) list.get(index + 1), null);
      } else {
        listRewrite.insertLast(newNode, null);
      }
    }

    private void copyMethodToDestination(
        IMethod method,
        CompilationUnitRewrite targetRewriter,
        CompilationUnit sourceCuNode,
        CompilationUnit targetCuNode)
        throws JavaModelException {
      MethodDeclaration newMethod =
          (MethodDeclaration)
              targetRewriter
                  .getASTRewrite()
                  .createStringPlaceholder(getUnindentedSource(method), ASTNode.METHOD_DECLARATION);
      newMethod.setConstructor(method.isConstructor());
      copyMemberToDestination(method, targetRewriter, sourceCuNode, targetCuNode, newMethod);
    }

    private void copyPackageDeclarationToDestination(
        IPackageDeclaration declaration,
        ASTRewrite targetRewrite,
        CompilationUnit sourceCuNode,
        CompilationUnit destinationCuNode)
        throws JavaModelException {
      if (destinationCuNode.getPackage() != null) return;
      PackageDeclaration sourceNode =
          ASTNodeSearchUtil.getPackageDeclarationNode(declaration, sourceCuNode);
      PackageDeclaration copiedNode =
          (PackageDeclaration) ASTNode.copySubtree(targetRewrite.getAST(), sourceNode);
      targetRewrite.set(destinationCuNode, CompilationUnit.PACKAGE_PROPERTY, copiedNode, null);
    }

    protected void copyToDestination(
        IJavaElement element,
        CompilationUnitRewrite targetRewriter,
        CompilationUnit sourceCuNode,
        CompilationUnit targetCuNode)
        throws CoreException {
      final ASTRewrite rewrite = targetRewriter.getASTRewrite();
      switch (element.getElementType()) {
        case IJavaElement.FIELD:
          copyMemberToDestination(
              (IMember) element,
              targetRewriter,
              sourceCuNode,
              targetCuNode,
              createNewFieldDeclarationNode(((IField) element), rewrite, sourceCuNode));
          break;
        case IJavaElement.IMPORT_CONTAINER:
          copyImportsToDestination((IImportContainer) element, rewrite, sourceCuNode, targetCuNode);
          break;
        case IJavaElement.IMPORT_DECLARATION:
          copyImportToDestination(
              (IImportDeclaration) element, rewrite, sourceCuNode, targetCuNode);
          break;
        case IJavaElement.INITIALIZER:
          copyInitializerToDestination(
              (IInitializer) element, targetRewriter, sourceCuNode, targetCuNode);
          break;
        case IJavaElement.METHOD:
          copyMethodToDestination((IMethod) element, targetRewriter, sourceCuNode, targetCuNode);
          break;
        case IJavaElement.PACKAGE_DECLARATION:
          copyPackageDeclarationToDestination(
              (IPackageDeclaration) element, rewrite, sourceCuNode, targetCuNode);
          break;
        case IJavaElement.TYPE:
          copyTypeToDestination((IType) element, targetRewriter, sourceCuNode, targetCuNode);
          break;

        default:
          Assert.isTrue(false);
      }
    }

    private void copyTypeToDestination(
        IType type,
        CompilationUnitRewrite targetRewriter,
        CompilationUnit sourceCuNode,
        CompilationUnit targetCuNode)
        throws JavaModelException {
      AbstractTypeDeclaration newType =
          (AbstractTypeDeclaration)
              targetRewriter
                  .getASTRewrite()
                  .createStringPlaceholder(getUnindentedSource(type), ASTNode.TYPE_DECLARATION);
      IType enclosingType = getEnclosingType(getJavaElementDestination());
      if (enclosingType != null) {
        copyMemberToDestination(type, targetRewriter, sourceCuNode, targetCuNode, newType);
      } else {
        targetRewriter
            .getASTRewrite()
            .getListRewrite(targetCuNode, CompilationUnit.TYPES_PROPERTY)
            .insertLast(newType, null);
      }
    }

    private BodyDeclaration createNewFieldDeclarationNode(
        IField field, ASTRewrite rewrite, CompilationUnit sourceCuNode) throws CoreException {
      AST targetAst = rewrite.getAST();
      BodyDeclaration newDeclaration = null;

      BodyDeclaration bodyDeclaration =
          ASTNodeSearchUtil.getFieldOrEnumConstantDeclaration(field, sourceCuNode);
      if (bodyDeclaration instanceof FieldDeclaration) {
        FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
        if (fieldDeclaration.fragments().size() == 1)
          return (FieldDeclaration)
              rewrite.createStringPlaceholder(
                  getUnindentedSource(field), ASTNode.FIELD_DECLARATION);
        VariableDeclarationFragment originalFragment =
            ASTNodeSearchUtil.getFieldDeclarationFragmentNode(field, sourceCuNode);
        VariableDeclarationFragment copiedFragment =
            (VariableDeclarationFragment) ASTNode.copySubtree(targetAst, originalFragment);
        newDeclaration = targetAst.newFieldDeclaration(copiedFragment);
        ((FieldDeclaration) newDeclaration)
            .setType((Type) ASTNode.copySubtree(targetAst, fieldDeclaration.getType()));
      } else if (bodyDeclaration instanceof EnumConstantDeclaration) {
        EnumConstantDeclaration constantDeclaration = (EnumConstantDeclaration) bodyDeclaration;
        EnumConstantDeclaration newConstDeclaration = targetAst.newEnumConstantDeclaration();
        newConstDeclaration.setName(
            (SimpleName) ASTNode.copySubtree(targetAst, constantDeclaration.getName()));
        AnonymousClassDeclaration anonymousDeclaration =
            constantDeclaration.getAnonymousClassDeclaration();
        if (anonymousDeclaration != null) {
          String content = ASTNodes.getNodeSource(anonymousDeclaration, false, true);
          if (content != null) {
            newConstDeclaration.setAnonymousClassDeclaration(
                (AnonymousClassDeclaration)
                    rewrite.createStringPlaceholder(content, ASTNode.ANONYMOUS_CLASS_DECLARATION));
          }
        }
        newDeclaration = newConstDeclaration;
      } else Assert.isTrue(false);
      if (newDeclaration != null) {
        newDeclaration
            .modifiers()
            .addAll(ASTNodeFactory.newModifiers(targetAst, bodyDeclaration.getModifiers()));
        Javadoc javadoc = bodyDeclaration.getJavadoc();
        if (javadoc != null) {
          String content = ASTNodes.getNodeSource(javadoc, false, true);
          if (content != null) {
            newDeclaration.setJavadoc(
                (Javadoc) rewrite.createStringPlaceholder(content, ASTNode.JAVADOC));
          }
        }
      }

      return newDeclaration;
    }

    protected abstract JavaRefactoringDescriptor createRefactoringDescriptor(
        final JDTRefactoringDescriptorComment comment,
        final Map<String, String> arguments,
        final String description,
        final String project,
        int flags);

    public final ChangeDescriptor getDescriptor() {
      final Map<String, String> arguments = new HashMap<String, String>();
      final int length = fJavaElements.length;
      final String description = length == 1 ? getDescriptionSingular() : getDescriptionPlural();
      final IProject resource = getSingleProject();
      final String project = resource != null ? resource.getName() : null;
      final String header =
          length == 1
              ? Messages.format(
                  getHeaderPatternSingular(),
                  new String[] {
                    JavaElementLabels.getTextLabel(
                        fJavaElements[0].getElementName(), JavaElementLabels.ALL_FULLY_QUALIFIED),
                    getDestinationLabel()
                  })
              : Messages.format(
                  getHeaderPatternPlural(),
                  new String[] {String.valueOf(length), getDestinationLabel()});
      int flags =
          JavaRefactoringDescriptor.JAR_REFACTORING
              | JavaRefactoringDescriptor.JAR_MIGRATION
              | RefactoringDescriptor.STRUCTURAL_CHANGE
              | RefactoringDescriptor.MULTI_CHANGE;
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      arguments.put(ATTRIBUTE_POLICY, getPolicyId());
      arguments.put(ATTRIBUTE_MEMBERS, new Integer(fJavaElements.length).toString());
      for (int offset = 0; offset < fJavaElements.length; offset++)
        arguments.put(
            JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (offset + 1),
            JavaRefactoringDescriptorUtil.elementToHandle(project, fJavaElements[offset]));
      arguments.putAll(getRefactoringArguments(project));
      final JavaRefactoringDescriptor descriptor =
          createRefactoringDescriptor(comment, arguments, description, project, flags);
      return new RefactoringChangeDescriptor(descriptor);
    }

    public final IJavaElement[] getJavaElements() {
      return fJavaElements;
    }

    public final IResource[] getResources() {
      return new IResource[0];
    }

    private IProject getSingleProject() {
      IProject result = null;
      for (int index = 0; index < fJavaElements.length; index++) {
        if (result == null) result = fJavaElements[index].getJavaProject().getProject();
        else if (!result.equals(fJavaElements[index].getJavaProject().getProject())) return null;
      }
      return result;
    }

    protected final ICompilationUnit getSourceCu() {
      // all have a common parent, so all must be in the same cu
      // we checked before that the array in not null and not empty
      return getEnclosingCompilationUnit(fJavaElements[0]);
    }

    @Override
    public RefactoringStatus initialize(JavaRefactoringArguments arguments) {
      final RefactoringStatus status = new RefactoringStatus();
      int memberCount = 0;
      String value = arguments.getAttribute(ATTRIBUTE_MEMBERS);
      if (value != null && !"".equals(value)) { // $NON-NLS-1$
        try {
          memberCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_MEMBERS));
        }
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_MEMBERS));
      String handle = null;
      List<IJavaElement> elements = new ArrayList<IJavaElement>();
      for (int index = 0; index < memberCount; index++) {
        final String attribute = JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (index + 1);
        handle = arguments.getAttribute(attribute);
        if (handle != null && !"".equals(handle)) { // $NON-NLS-1$
          final IJavaElement element =
              JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
          if (element == null || !element.exists())
            status.merge(
                JavaRefactoringDescriptorUtil.createInputWarningStatus(
                    element, getProcessorId(), getRefactoringId()));
          else elements.add(element);
        } else
          return RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, attribute));
      }
      fJavaElements = elements.toArray(new IJavaElement[elements.size()]);
      status.merge(super.initialize(arguments));
      return status;
    }

    /**
     * Verifies the destination.
     *
     * @param destination the destination
     * @param location the location
     * @return returns the status
     * @throws JavaModelException should not happen
     */
    protected RefactoringStatus verifyDestination(IJavaElement destination, int location)
        throws JavaModelException {
      Assert.isNotNull(destination);
      if (!fCheckDestination) return new RefactoringStatus();
      if (!destination.exists())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_doesnotexist1);
      if (destination instanceof IJavaModel)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_jmodel);
      if (!(destination instanceof ICompilationUnit)
          && !ReorgUtils.isInsideCompilationUnit(destination))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot);

      ICompilationUnit destinationCu = getEnclosingCompilationUnit(destination);
      Assert.isNotNull(destinationCu);
      if (destinationCu.isReadOnly())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_cannot_modify);

      switch (destination.getElementType()) {
        case IJavaElement.COMPILATION_UNIT:
          if (location != IReorgDestination.LOCATION_ON)
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);

          int[] types0 =
              new int[] {
                IJavaElement.TYPE,
                IJavaElement.PACKAGE_DECLARATION,
                IJavaElement.IMPORT_CONTAINER,
                IJavaElement.IMPORT_DECLARATION
              };
          if (!ReorgUtils.hasOnlyElementsOfType(getJavaElements(), types0)) {
            if (JavaElementUtil.getMainType(destinationCu) == null
                || !ReorgUtils.hasOnlyElementsOfType(
                    getJavaElements(),
                    new int[] {
                      IJavaElement.FIELD,
                      IJavaElement.INITIALIZER,
                      IJavaElement.METHOD,
                      IJavaElement.TYPE
                    }))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          }

          break;
        case IJavaElement.PACKAGE_DECLARATION: // drop nothing
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ReorgPolicyFactory_package_decl);
        case IJavaElement.IMPORT_CONTAINER:
          if (location == IReorgDestination.LOCATION_ON) {
            if (ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.IMPORT_DECLARATION))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          } else {
            if (ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.IMPORT_CONTAINER))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          }

          break;
        case IJavaElement.IMPORT_DECLARATION: // drop import declarations before/after
          if (location == IReorgDestination.LOCATION_ON)
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);

          if (ReorgUtils.hasElementsNotOfType(getJavaElements(), IJavaElement.IMPORT_DECLARATION))
            return RefactoringStatus.createFatalErrorStatus(
                RefactoringCoreMessages.ReorgPolicyFactory_cannot);

          break;
        case IJavaElement.FIELD: // fall thru
        case IJavaElement.INITIALIZER: // fall thru
        case IJavaElement.METHOD: // fall thru
          if (location == IReorgDestination.LOCATION_ON) {
            // can paste on itself (drop on itself is disabled in the SelectionTransferDropAdapter)
            if (getJavaElements().length != 1)
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);

            if (!destination.equals(getJavaElements()[0]))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          } else {
            int[] types =
                new int[] {
                  IJavaElement.FIELD,
                  IJavaElement.INITIALIZER,
                  IJavaElement.METHOD,
                  IJavaElement.TYPE
                };
            if (!ReorgUtils.hasOnlyElementsOfType(getJavaElements(), types))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          }

          break;
        case IJavaElement.TYPE:
          if (location == IReorgDestination.LOCATION_ON) { // can drop type members
            int[] types1 =
                new int[] {
                  IJavaElement.FIELD,
                  IJavaElement.INITIALIZER,
                  IJavaElement.METHOD,
                  IJavaElement.TYPE
                };
            if (!ReorgUtils.hasOnlyElementsOfType(getJavaElements(), types1))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          } else { // can drop type before/after
            if (destination.getParent() instanceof IMethod)
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);

            int[] types =
                new int[] {
                  IJavaElement.FIELD,
                  IJavaElement.INITIALIZER,
                  IJavaElement.METHOD,
                  IJavaElement.TYPE
                };
            if (!ReorgUtils.hasOnlyElementsOfType(getJavaElements(), types))
              return RefactoringStatus.createFatalErrorStatus(
                  RefactoringCoreMessages.ReorgPolicyFactory_cannot);
          }

          break;
      }

      return new RefactoringStatus();
    }

    /** {@inheritDoc} */
    @Override
    public RefactoringStatus verifyDestination(IReorgDestination destination)
        throws JavaModelException {
      if (!(destination instanceof JavaElementDestination))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReorgPolicyFactory_invalidDestinationKind);

      JavaElementDestination javaElementDestination = (JavaElementDestination) destination;
      return verifyDestination(
          javaElementDestination.getJavaElement(), javaElementDestination.getLocation());
    }
  }

  private static final String ATTRIBUTE_FILES = "files"; // $NON-NLS-1$

  private static final String ATTRIBUTE_FOLDERS = "folders"; // $NON-NLS-1$

  private static final String ATTRIBUTE_FRAGMENTS = "fragments"; // $NON-NLS-1$

  private static final String ATTRIBUTE_LOG = "log"; // $NON-NLS-1$

  private static final String ATTRIBUTE_MEMBERS = "members"; // $NON-NLS-1$

  private static final String ATTRIBUTE_PATTERNS = "patterns"; // $NON-NLS-1$

  private static final String ATTRIBUTE_POLICY = "policy"; // $NON-NLS-1$

  private static final String ATTRIBUTE_QUALIFIED = "qualified"; // $NON-NLS-1$

  private static final String ATTRIBUTE_ROOTS = "roots"; // $NON-NLS-1$

  private static final String ATTRIBUTE_UNITS = "units"; // $NON-NLS-1$

  private static final String DELIMITER_ELEMENT = "\t"; // $NON-NLS-1$

  private static final String DELIMITER_RECORD = "\n"; // $NON-NLS-1$

  private static final String NO_ID = "no_id"; // $NON-NLS-1$

  private static final String NO_POLICY = "no_policy"; // $NON-NLS-1$

  private static final String UNUSED_STRING = "unused"; // $NON-NLS-1$

  private static boolean containsNull(Object[] objects) {
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] == null) return true;
    }
    return false;
  }

  public static ICopyPolicy createCopyPolicy(IResource[] resources, IJavaElement[] javaElements)
      throws JavaModelException {
    return (ICopyPolicy) createReorgPolicy(true, resources, javaElements);
  }

  public static ICopyPolicy createCopyPolicy(
      RefactoringStatus status, JavaRefactoringArguments arguments) {
    final String policy = arguments.getAttribute(ATTRIBUTE_POLICY);
    if (policy != null && !"".equals(policy)) { // $NON-NLS-1$
      if (CopyFilesFoldersAndCusPolicy.POLICY_COPY_RESOURCE.equals(policy)) {
        return new CopyFilesFoldersAndCusPolicy(null, null, null);
      } else if (CopyPackageFragmentRootsPolicy.POLICY_COPY_ROOTS.equals(policy)) {
        return new CopyPackageFragmentRootsPolicy(null);
      } else if (CopyPackagesPolicy.POLICY_COPY_PACKAGES.equals(policy)) {
        return new CopyPackagesPolicy(null);
      } else if (CopySubCuElementsPolicy.POLICY_COPY_MEMBERS.equals(policy)) {
        return new CopySubCuElementsPolicy(null);
      } else
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                    new String[] {policy, ATTRIBUTE_POLICY})));
    } else
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_POLICY)));
    return null;
  }

  public static IMovePolicy createMovePolicy(IResource[] resources, IJavaElement[] javaElements)
      throws JavaModelException {
    return (IMovePolicy) createReorgPolicy(false, resources, javaElements);
  }

  public static IMovePolicy createMovePolicy(
      RefactoringStatus status, JavaRefactoringArguments arguments) {
    final String policy = arguments.getAttribute(ATTRIBUTE_POLICY);
    if (policy != null && !"".equals(policy)) { // $NON-NLS-1$
      if (MoveFilesFoldersAndCusPolicy.POLICY_MOVE_RESOURCES.equals(policy)) {
        return new MoveFilesFoldersAndCusPolicy(null, null, null);
      } else if (MovePackageFragmentRootsPolicy.POLICY_MOVE_ROOTS.equals(policy)) {
        return new MovePackageFragmentRootsPolicy(null);
      } else if (MovePackagesPolicy.POLICY_MOVE_PACKAGES.equals(policy)) {
        return new MovePackagesPolicy(null);
      } else if (MoveMembersPolicy.POLICY_MOVE_MEMBERS.equals(policy)) {
        return new MoveMembersPolicy(null);
      } else if (MoveImportDeclarationsPolicy.POLICY_MOVE_IMPORT_DECLARATIONS.equals(policy)) {
        return new MoveImportDeclarationsPolicy(null);
      } else
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                    new String[] {policy, ATTRIBUTE_POLICY})));
    } else
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              Messages.format(
                  RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                  ATTRIBUTE_POLICY)));
    return null;
  }

  private static IReorgPolicy createReorgPolicy(
      boolean copy, IResource[] selectedResources, IJavaElement[] selectedJavaElements)
      throws JavaModelException {
    final IReorgPolicy NO;
    if (copy) NO = new NoCopyPolicy();
    else NO = new NoMovePolicy();

    ActualSelectionComputer selectionComputer =
        new ActualSelectionComputer(selectedJavaElements, selectedResources);
    IResource[] resources = selectionComputer.getActualResourcesToReorg();
    IJavaElement[] javaElements = selectionComputer.getActualJavaElementsToReorg();

    if (resources.length == 0 && javaElements.length == 0) return NO;

    if (containsNull(resources)) return NO;

    if (containsNull(javaElements)) return NO;

    if (ReorgUtils.isArchiveOrExternalMember(javaElements)
        && ReorgUtils.getElementsOfType(javaElements, IJavaElement.PACKAGE_FRAGMENT_ROOT).size()
            != javaElements.length) return NO;

    if (ReorgUtils.hasElementsOfType(javaElements, IJavaElement.JAVA_PROJECT)) return NO;

    if (ReorgUtils.hasElementsOfType(javaElements, IJavaElement.JAVA_MODEL)) return NO;

    if (ReorgUtils.hasElementsOfType(resources, IResource.PROJECT | IResource.ROOT)) return NO;

    if (!new ParentChecker(resources, javaElements).haveCommonParent()) return NO;

    if (ReorgUtils.hasElementsOfType(javaElements, IJavaElement.PACKAGE_FRAGMENT)) {
      if (resources.length != 0
          || ReorgUtils.hasElementsNotOfType(javaElements, IJavaElement.PACKAGE_FRAGMENT))
        return NO;
      if (copy) return new CopyPackagesPolicy(ArrayTypeConverter.toPackageArray(javaElements));
      else return new MovePackagesPolicy(ArrayTypeConverter.toPackageArray(javaElements));
    }

    if (ReorgUtils.hasElementsOfType(javaElements, IJavaElement.PACKAGE_FRAGMENT_ROOT)) {
      if (resources.length != 0
          || ReorgUtils.hasElementsNotOfType(javaElements, IJavaElement.PACKAGE_FRAGMENT_ROOT))
        return NO;
      if (copy)
        return new CopyPackageFragmentRootsPolicy(
            ArrayTypeConverter.toPackageFragmentRootArray(javaElements));
      else
        return new MovePackageFragmentRootsPolicy(
            ArrayTypeConverter.toPackageFragmentRootArray(javaElements));
    }

    if (ReorgUtils.hasElementsOfType(resources, IResource.FILE | IResource.FOLDER)
        || ReorgUtils.hasElementsOfType(javaElements, IJavaElement.COMPILATION_UNIT)) {
      if (ReorgUtils.hasElementsNotOfType(javaElements, IJavaElement.COMPILATION_UNIT)) return NO;
      if (ReorgUtils.hasElementsNotOfType(resources, IResource.FILE | IResource.FOLDER)) return NO;
      if (copy)
        return new CopyFilesFoldersAndCusPolicy(
            ReorgUtils.getFiles(resources),
            ReorgUtils.getFolders(resources),
            ArrayTypeConverter.toCuArray(javaElements));
      else
        return new MoveFilesFoldersAndCusPolicy(
            ReorgUtils.getFiles(resources),
            ReorgUtils.getFolders(resources),
            ArrayTypeConverter.toCuArray(javaElements));
    }

    if (hasOnlyMembers(javaElements)) {
      if (hasAnonymousClassDeclarations(javaElements)) return NO;

      if (copy) {
        return new CopySubCuElementsPolicy(javaElements);
      } else {
        List<IJavaElement> members = Arrays.asList(javaElements);
        return new MoveMembersPolicy(members.toArray(new IMember[members.size()]));
      }
    }

    if (hasOnlyImportDeclarations(javaElements)) {
      if (copy) {
        return new CopySubCuElementsPolicy(javaElements);
      } else {
        List<?> declarations =
            ReorgUtils.getElementsOfType(javaElements, IJavaElement.IMPORT_DECLARATION);
        return new MoveImportDeclarationsPolicy(
            declarations.toArray(new IImportDeclaration[declarations.size()]));
      }
    }

    if (copy && hasElementsSmallerThanCuOrClassFile(javaElements)) {
      if (ReorgUtils.hasElementsOfType(javaElements, IJavaElement.PACKAGE_DECLARATION)) return NO;

      if (hasAnonymousClassDeclarations(javaElements)) return NO;

      Assert.isTrue(resources.length == 0);
      return new CopySubCuElementsPolicy(javaElements);
    }

    return NO;
  }

  private static boolean hasAnonymousClassDeclarations(IJavaElement[] javaElements)
      throws JavaModelException {
    for (int i = 0; i < javaElements.length; i++) {
      if (javaElements[i] instanceof IType) {
        IType type = (IType) javaElements[i];
        if (type.isAnonymous()) return true;
      }
    }
    return false;
  }

  private static boolean hasElementsSmallerThanCuOrClassFile(IJavaElement[] javaElements) {
    for (int i = 0; i < javaElements.length; i++) {
      if (ReorgUtils.isInsideCompilationUnit(javaElements[i])) return true;
      if (ReorgUtils.isInsideClassFile(javaElements[i])) return true;
    }
    return false;
  }

  private static boolean hasOnlyImportDeclarations(IJavaElement[] javaElements) {
    for (int i = 0; i < javaElements.length; i++) {
      if (javaElements[i].getElementType() != IJavaElement.IMPORT_DECLARATION) return false;
    }
    return true;
  }

  private static boolean hasOnlyMembers(IJavaElement[] javaElements) {
    for (int i = 0; i < javaElements.length; i++) {
      if (!(javaElements[i] instanceof IMember)) return false;
    }
    return true;
  }

  public static CreateTargetExecutionLog loadCreateTargetExecutionLog(
      JavaRefactoringArguments arguments) {
    CreateTargetExecutionLog log = new CreateTargetExecutionLog();
    final String value = arguments.getAttribute(ATTRIBUTE_LOG);
    if (value != null) {
      final StringTokenizer tokenizer = new StringTokenizer(value, DELIMITER_RECORD, false);
      while (tokenizer.hasMoreTokens()) {
        final String token = tokenizer.nextToken();
        processCreateTargetExecutionRecord(log, arguments, token);
      }
    }
    return log;
  }

  public static ReorgExecutionLog loadReorgExecutionLog(JavaRefactoringArguments arguments) {
    ReorgExecutionLog log = new ReorgExecutionLog();
    final String value = arguments.getAttribute(ATTRIBUTE_LOG);
    if (value != null) {
      final StringTokenizer tokenizer = new StringTokenizer(value, DELIMITER_RECORD, false);
      while (tokenizer.hasMoreTokens()) {
        final String token = tokenizer.nextToken();
        processReorgExecutionRecord(log, arguments, token);
      }
    }
    return log;
  }

  private static void processCreateTargetExecutionRecord(
      CreateTargetExecutionLog log, JavaRefactoringArguments arguments, String token) {
    final StringTokenizer tokenizer = new StringTokenizer(token, DELIMITER_ELEMENT, false);
    String value = null;
    if (tokenizer.hasMoreTokens()) {
      value = tokenizer.nextToken();
      Object selection =
          JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), value, false);
      if (selection == null)
        selection = JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), value);
      if (selection != null && tokenizer.hasMoreTokens()) {
        value = tokenizer.nextToken();
        Object created =
            JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), value, false);
        if (created == null)
          created = JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), value);
        if (created != null) log.markAsCreated(selection, created);
      }
    }
  }

  private static void processReorgExecutionRecord(
      ReorgExecutionLog log, JavaRefactoringArguments arguments, String token) {
    final StringTokenizer tokenizer = new StringTokenizer(token, DELIMITER_ELEMENT, false);
    String value = null;
    if (tokenizer.hasMoreTokens()) {
      value = tokenizer.nextToken();
      Object element = JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), value);
      if (element == null)
        element = JavaRefactoringDescriptorUtil.handleToResource(arguments.getProject(), value);
      if (tokenizer.hasMoreTokens()) {
        final boolean processed = Boolean.valueOf(tokenizer.nextToken()).booleanValue();
        if (processed) {
          log.markAsProcessed(element);
          if (element instanceof IJavaElement)
            log.markAsProcessed(JavaElementResourceMapping.create((IJavaElement) element));
        }
        if (tokenizer.hasMoreTokens()) {
          final boolean renamed = Boolean.valueOf(tokenizer.nextToken()).booleanValue();
          if (renamed && tokenizer.hasMoreTokens()) {
            final String name = tokenizer.nextToken();
            log.setNewName(element, name);
            if (element instanceof IJavaElement)
              log.setNewName(JavaElementResourceMapping.create((IJavaElement) element), name);
          }
        }
      }
    }
  }

  public static void storeCreateTargetExecutionLog(
      String project, Map<String, String> arguments, CreateTargetExecutionLog log) {
    if (log != null) {
      final StringBuffer buffer = new StringBuffer(64);
      final Object[] selections = log.getSelectedElements();
      for (int index = 0; index < selections.length; index++) {
        final Object selection = selections[index];
        if (selection != null) {
          final Object created = log.getCreatedElement(selection);
          if (created != null) {
            storeLogElement(buffer, project, selection);
            buffer.append(DELIMITER_ELEMENT);
            storeLogElement(buffer, project, created);
            buffer.append(DELIMITER_RECORD);
          }
        }
      }
      final String value = new String(buffer.toString().trim());
      if (!"".equals(value)) // $NON-NLS-1$
      arguments.put(ATTRIBUTE_LOG, value);
    }
  }

  private static boolean storeLogElement(StringBuffer buffer, String project, Object object) {
    if (object instanceof IJavaElement) {
      final IJavaElement element = (IJavaElement) object;
      buffer.append(JavaRefactoringDescriptorUtil.elementToHandle(project, element));
      return true;
    } else if (object instanceof IResource) {
      final IResource resource = (IResource) object;
      buffer.append(JavaRefactoringDescriptorUtil.resourceToHandle(project, resource));
      return true;
    }
    return false;
  }

  public static void storeReorgExecutionLog(
      String project, Map<String, String> arguments, ReorgExecutionLog log) {
    if (log != null) {
      final Set<Object> set = new HashSet<Object>();
      set.addAll(Arrays.asList(log.getProcessedElements()));
      set.addAll(Arrays.asList(log.getRenamedElements()));
      final StringBuffer buffer = new StringBuffer(64);
      for (final Iterator<Object> iterator = set.iterator(); iterator.hasNext(); ) {
        final Object object = iterator.next();
        if (storeLogElement(buffer, project, object)) {
          buffer.append(DELIMITER_ELEMENT);
          buffer.append(log.isProcessed(object));
          buffer.append(DELIMITER_ELEMENT);
          final boolean renamed = log.isRenamed(object);
          buffer.append(renamed);
          if (renamed) {
            buffer.append(DELIMITER_ELEMENT);
            buffer.append(log.getNewName(object));
          }
          buffer.append(DELIMITER_RECORD);
        }
      }
      final String value = new String(buffer.toString().trim());
      if (!"".equals(value)) // $NON-NLS-1$
      arguments.put(ATTRIBUTE_LOG, value);
    }
  }

  private ReorgPolicyFactory() {
    // private
  }
}
