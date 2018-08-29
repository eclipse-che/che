/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class PotentialProgrammingProblemsFix extends CompilationUnitRewriteOperationsFix {

  /** Name of the serializable class */
  private static final String SERIALIZABLE_NAME = "java.io.Serializable"; // $NON-NLS-1$

  /** The name of the serial version field */
  private static final String NAME_FIELD = "serialVersionUID"; // $NON-NLS-1$

  private interface ISerialVersionFixContext {
    public RefactoringStatus initialize(IProgressMonitor monitor) throws CoreException;

    public Long getSerialVersionId(ITypeBinding binding);
  }

  private static class SerialVersionHashContext implements ISerialVersionFixContext {

    private final IJavaProject fProject;
    private final ICompilationUnit[] fCompilationUnits;
    private final Hashtable<String, Long> fIdsTable;

    public SerialVersionHashContext(IJavaProject project, ICompilationUnit[] compilationUnits) {
      fProject = project;
      fCompilationUnits = compilationUnits;
      fIdsTable = new Hashtable<String, Long>();
    }

    public RefactoringStatus initialize(IProgressMonitor monitor) throws CoreException {
      if (monitor == null) monitor = new NullProgressMonitor();

      RefactoringStatus result;
      try {
        monitor.beginTask("", 10); // $NON-NLS-1$

        IType[] types =
            findTypesWithMissingUID(
                fProject, fCompilationUnits, new SubProgressMonitor(monitor, 1));
        if (types.length == 0) return new RefactoringStatus();

        // TODO disable build
        //				fProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new
        // SubProgressMonitor(monitor, 60));
        if (monitor.isCanceled()) throw new OperationCanceledException();

        result = new RefactoringStatus();
        ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
        parser.setProject(fProject);
        IBinding[] bindings = parser.createBindings(types, new SubProgressMonitor(monitor, 1));
        for (int i = 0; i < bindings.length; i++) {
          IBinding curr = bindings[i];
          if (curr instanceof ITypeBinding) {
            ITypeBinding typeBinding = (ITypeBinding) curr;
            //						try {
            Long id = 0L; // SerialVersionHashOperation.calculateSerialVersionId(typeBinding, new
            // SubProgressMonitor(monitor, 1));
            if (id != null) {
              setSerialVersionId(typeBinding, id);
            } else {
              result.addWarning(
                  Messages.format(
                      FixMessages.PotentialProgrammingProblemsFix_calculatingUIDFailed_unknown,
                      BasicElementLabels.getJavaElementName(typeBinding.getName())));
            }
            //						} catch (IOException e) {
            //
            //	result.addWarning(Messages.format(FixMessages.PotentialProgrammingProblemsFix_calculatingUIDFailed_exception,
            //															  new
            // String[]{BasicElementLabels.getJavaElementName(typeBinding.getName()),
            //																		   e.getLocalizedMessage()}), JavaStatusContext.create(types[i]));
            //						} catch (CoreException e) {
            //
            //	result.addWarning(Messages.format(FixMessages.PotentialProgrammingProblemsFix_calculatingUIDFailed_exception,
            //															  new
            // String[]{BasicElementLabels.getJavaElementName(typeBinding.getName()),
            //																		   e.getLocalizedMessage()}), JavaStatusContext.create(types[i]));
            //						}
          }
        }
      } finally {
        monitor.done();
      }
      return result;
    }

    /** {@inheritDoc} */
    public Long getSerialVersionId(ITypeBinding binding) {
      return fIdsTable.get(binding.getKey());
    }

    protected void setSerialVersionId(ITypeBinding binding, Long id) {
      fIdsTable.put(binding.getKey(), id);
    }

    private IType[] findTypesWithMissingUID(
        IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
        throws CoreException {
      try {
        monitor.beginTask("", compilationUnits.length); // $NON-NLS-1$

        IType serializable = project.findType(SERIALIZABLE_NAME);

        List<IType> types = new ArrayList<IType>();

        if (compilationUnits.length > 500) {
          // 500 is a guess. Building the type hierarchy on serializable is very expensive
          // depending on how many subtypes exit in the project.

          HashSet<ICompilationUnit> cus = new HashSet<ICompilationUnit>();
          for (int i = 0; i < compilationUnits.length; i++) {
            cus.add(compilationUnits[i]);
          }

          monitor.subTask(
              Messages.format(
                  FixMessages.Java50Fix_SerialVersion_CalculateHierarchy_description,
                  SERIALIZABLE_NAME));
          ITypeHierarchy hierarchy1 =
              serializable.newTypeHierarchy(
                  project, new SubProgressMonitor(monitor, compilationUnits.length));
          IType[] allSubtypes1 = hierarchy1.getAllSubtypes(serializable);
          addTypes(allSubtypes1, cus, types);
        } else {
          monitor.subTask(FixMessages.Java50Fix_InitializeSerialVersionId_subtask_description);
          for (int i = 0; i < compilationUnits.length; i++) {
            collectChildrenWithMissingSerialVersionId(
                compilationUnits[i].getChildren(), serializable, types);
            if (monitor.isCanceled()) throw new OperationCanceledException();
            monitor.worked(1);
          }
        }

        return types.toArray(new IType[types.size()]);
      } finally {
        monitor.done();
      }
    }

    private void addTypes(IType[] allSubtypes, HashSet<ICompilationUnit> cus, List<IType> types)
        throws JavaModelException {
      for (int i = 0; i < allSubtypes.length; i++) {
        IType type = allSubtypes[i];

        IField field = type.getField(NAME_FIELD);
        if (!field.exists()) {
          if (type.isClass() && cus.contains(type.getCompilationUnit())) {
            types.add(type);
          }
        }
      }
    }

    private void collectChildrenWithMissingSerialVersionId(
        IJavaElement[] children, IType serializable, List<IType> result) throws JavaModelException {
      for (int i = 0; i < children.length; i++) {
        IJavaElement child = children[i];
        if (child instanceof IType) {
          IType type = (IType) child;

          if (type.isClass()) {
            IField field = type.getField(NAME_FIELD);
            if (!field.exists()) {
              ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
              IType[] interfaces = hierarchy.getAllSuperInterfaces(type);
              for (int j = 0; j < interfaces.length; j++) {
                if (interfaces[j].equals(serializable)) {
                  result.add(type);
                  break;
                }
              }
            }
          }

          collectChildrenWithMissingSerialVersionId(type.getChildren(), serializable, result);
        } else if (child instanceof IMethod) {
          collectChildrenWithMissingSerialVersionId(
              ((IMethod) child).getChildren(), serializable, result);
        } else if (child instanceof IField) {
          collectChildrenWithMissingSerialVersionId(
              ((IField) child).getChildren(), serializable, result);
        }
      }
    }
  }

  private static class SerialVersionHashBatchOperation extends AbstractSerialVersionOperation {

    private final ISerialVersionFixContext fContext;

    protected SerialVersionHashBatchOperation(
        ICompilationUnit unit, ASTNode[] node, ISerialVersionFixContext context) {
      super(unit, node);
      fContext = context;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean addInitializer(
        VariableDeclarationFragment fragment, ASTNode declarationNode) {
      ITypeBinding typeBinding = getTypeBinding(declarationNode);
      if (typeBinding == null) return false;

      Long id = fContext.getSerialVersionId(typeBinding);
      if (id == null) return false;

      fragment.setInitializer(fragment.getAST().newNumberLiteral(id.toString() + LONG_SUFFIX));
      return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void addLinkedPositions(
        ASTRewrite rewrite,
        VariableDeclarationFragment fragment,
        LinkedProposalModel positionGroups) {}
  }

  private static ISerialVersionFixContext fCurrentContext;

  public static IProposableFix[] createMissingSerialVersionFixes(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    if (problem.getProblemId() != IProblem.MissingSerialVersion) return null;

    final ICompilationUnit unit = (ICompilationUnit) compilationUnit.getJavaElement();
    if (unit == null) return null;

    final SimpleName simpleName = getSelectedName(compilationUnit, problem);
    if (simpleName == null) return null;

    ASTNode declaringNode = getDeclarationNode(simpleName);
    if (declaringNode == null) return null;

    SerialVersionDefaultOperation defop =
        new SerialVersionDefaultOperation(unit, new ASTNode[] {declaringNode});
    IProposableFix fix1 =
        new PotentialProgrammingProblemsFix(
            FixMessages.Java50Fix_SerialVersion_default_description,
            compilationUnit,
            new CompilationUnitRewriteOperation[] {defop});

    //		SerialVersionHashOperation hashop= new SerialVersionHashOperation(unit, new ASTNode[]
    // {declaringNode});
    //		IProposableFix
    //				fix2= new
    // PotentialProgrammingProblemsFix(FixMessages.Java50Fix_SerialVersion_hash_description,
    // compilationUnit, new CompilationUnitRewriteOperation[] {hashop});

    return new IProposableFix[] {fix1 /*, fix2*/};
  }

  public static RefactoringStatus checkPreConditions(
      IJavaProject project,
      ICompilationUnit[] compilationUnits,
      IProgressMonitor monitor,
      boolean calculatedId,
      boolean defaultId,
      boolean randomId)
      throws CoreException {

    if (defaultId) {
      fCurrentContext =
          new ISerialVersionFixContext() {
            public Long getSerialVersionId(ITypeBinding binding) {
              return new Long(1);
            }

            public RefactoringStatus initialize(IProgressMonitor pm) throws CoreException {
              return new RefactoringStatus();
            }
          };
      return fCurrentContext.initialize(monitor);
    } else if (randomId) {
      fCurrentContext =
          new ISerialVersionFixContext() {
            private Random rng;

            public Long getSerialVersionId(ITypeBinding binding) {
              return new Long(rng.nextLong());
            }

            public RefactoringStatus initialize(IProgressMonitor pm) throws CoreException {
              rng = new Random((new Date()).getTime());
              return new RefactoringStatus();
            }
          };
      return fCurrentContext.initialize(monitor);
    } else if (calculatedId) {
      fCurrentContext = new SerialVersionHashContext(project, compilationUnits);
      return fCurrentContext.initialize(monitor);
    } else {
      return new RefactoringStatus();
    }
  }

  public static RefactoringStatus checkPostConditions(IProgressMonitor monitor) {
    if (monitor != null) monitor.done();

    fCurrentContext = null;
    return new RefactoringStatus();
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit, boolean addSerialVersionIds) {

    IProblem[] problems = compilationUnit.getProblems();
    IProblemLocation[] locations = new IProblemLocation[problems.length];
    for (int i = 0; i < problems.length; i++) {
      locations[i] = new ProblemLocation(problems[i]);
    }
    return createCleanUp(compilationUnit, locations, addSerialVersionIds);
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit, IProblemLocation[] problems, boolean addSerialVersionIds) {
    if (addSerialVersionIds) {

      final ICompilationUnit unit = (ICompilationUnit) compilationUnit.getJavaElement();
      if (unit == null) return null;

      List<ASTNode> declarationNodes = new ArrayList<ASTNode>();
      for (int i = 0; i < problems.length; i++) {
        if (problems[i].getProblemId() == IProblem.MissingSerialVersion) {
          final SimpleName simpleName = getSelectedName(compilationUnit, problems[i]);
          if (simpleName != null) {
            ASTNode declarationNode = getDeclarationNode(simpleName);
            if (declarationNode != null) {
              declarationNodes.add(declarationNode);
            }
          }
        }
      }
      if (declarationNodes.size() == 0) return null;

      for (Iterator<ASTNode> iter = declarationNodes.iterator(); iter.hasNext(); ) {
        ASTNode declarationNode = iter.next();
        ITypeBinding binding = getTypeBinding(declarationNode);
        if (fCurrentContext.getSerialVersionId(binding) != null) {
          SerialVersionHashBatchOperation op =
              new SerialVersionHashBatchOperation(
                  unit,
                  declarationNodes.toArray(new ASTNode[declarationNodes.size()]),
                  fCurrentContext);
          return new PotentialProgrammingProblemsFix(
              FixMessages.PotentialProgrammingProblemsFix_add_id_change_name,
              compilationUnit,
              new CompilationUnitRewriteOperation[] {op});
        }
      }
    }
    return null;
  }

  private static SimpleName getSelectedName(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    final ASTNode selection = problem.getCoveredNode(compilationUnit);
    if (selection == null) return null;

    Name name = null;
    if (selection instanceof SimpleType) {
      name = ((SimpleType) selection).getName();
    } else if (selection instanceof NameQualifiedType) {
      name = ((NameQualifiedType) selection).getName();
    } else if (selection instanceof QualifiedType) {
      name = ((QualifiedType) selection).getName();
    } else if (selection instanceof ParameterizedType) {
      final ParameterizedType type = (ParameterizedType) selection;
      final Type raw = type.getType();
      if (raw instanceof SimpleType) name = ((SimpleType) raw).getName();
      else if (raw instanceof NameQualifiedType) name = ((NameQualifiedType) raw).getName();
      else if (raw instanceof QualifiedType) name = ((QualifiedType) raw).getName();
    } else if (selection instanceof Name) {
      name = (Name) selection;
    }
    if (name == null) return null;

    if (name.isSimpleName()) {
      return (SimpleName) name;
    } else {
      return ((QualifiedName) name).getName();
    }
  }

  /**
   * Returns the declaration node for the originally selected node.
   *
   * @param name the name of the node
   * @return the declaration node
   */
  private static ASTNode getDeclarationNode(SimpleName name) {
    ASTNode parent = name.getParent();
    if (!(parent instanceof AbstractTypeDeclaration)) {

      parent = parent.getParent();
      if (parent instanceof ParameterizedType || parent instanceof Type)
        parent = parent.getParent();
      if (parent instanceof ClassInstanceCreation) {

        final ClassInstanceCreation creation = (ClassInstanceCreation) parent;
        parent = creation.getAnonymousClassDeclaration();
      }
    }
    return parent;
  }

  /**
   * Returns the type binding of the class declaration node.
   *
   * @param parent the node to get the type for
   * @return the type binding
   */
  private static ITypeBinding getTypeBinding(final ASTNode parent) {
    if (parent instanceof AbstractTypeDeclaration) {
      final AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) parent;
      return declaration.resolveBinding();
    } else if (parent instanceof AnonymousClassDeclaration) {
      final AnonymousClassDeclaration declaration = (AnonymousClassDeclaration) parent;
      return declaration.resolveBinding();
    } else if (parent instanceof ParameterizedType) {
      final ParameterizedType type = (ParameterizedType) parent;
      return type.resolveBinding();
    }
    return null;
  }

  protected PotentialProgrammingProblemsFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations) {
    super(name, compilationUnit, fixRewriteOperations);
  }
}
