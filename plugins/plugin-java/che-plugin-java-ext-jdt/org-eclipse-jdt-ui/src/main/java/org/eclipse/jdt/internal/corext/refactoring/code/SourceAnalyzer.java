/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Dmitry Stalnov
 * (dstalnov@fusionone.com) - contributed fixes for: o bug "Inline refactoring showed bogus error"
 * (see bugzilla https://bugs.eclipse.org/bugs/show_bug.cgi?id=42753) o Allow 'this' constructor to
 * be inlined (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38093)
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportReferencesCollector;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.LocalVariableIndex;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.code.flow.FlowContext;
import org.eclipse.jdt.internal.corext.refactoring.code.flow.FlowInfo;
import org.eclipse.jdt.internal.corext.refactoring.code.flow.InOutFlowAnalyzer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

class SourceAnalyzer {

  public static class NameData {
    private String fName;
    private List<SimpleName> fReferences;

    public NameData(String n) {
      fName = n;
      fReferences = new ArrayList<SimpleName>(2);
    }

    public String getName() {
      return fName;
    }

    public void addReference(SimpleName ref) {
      fReferences.add(ref);
    }

    public List<SimpleName> references() {
      return fReferences;
    }
  }

  private class ActivationAnalyzer extends ASTVisitor {
    public RefactoringStatus status = new RefactoringStatus();
    private ASTNode fLastNode = getLastNode();
    private IMethodBinding fBinding = getBinding();

    @Override
    public boolean visit(ReturnStatement node) {
      if (node != fLastNode) {
        fInterruptedExecutionFlow = true;
      }
      return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(LambdaExpression node) {
      return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      IMethodBinding methodBinding = node.resolveMethodBinding();
      if (methodBinding != null) methodBinding.getMethodDeclaration();
      if (fBinding != null
          && methodBinding != null
          && fBinding.isEqualTo(methodBinding)
          && !status.hasFatalError()) {
        status.addFatalError(
            RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_recursive_call);
        return false;
      }
      return true;
    }

    @Override
    public boolean visit(SimpleName node) {
      IBinding binding = node.resolveBinding();
      if (binding == null && !status.hasFatalError()) {
        // fixes bug #42753
        if (!ASTNodes.isLabel(node)) {
          status.addFatalError(
              RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_declaration_has_errors,
              JavaStatusContext.create(fTypeRoot, fDeclaration));
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean visit(ThisExpression node) {
      if (node.getQualifier() != null) {
        status.addFatalError(
            RefactoringCoreMessages
                .InlineMethodRefactoring_SourceAnalyzer_qualified_this_expressions,
            JavaStatusContext.create(fTypeRoot, node));
        return false;
      }
      return true;
    }

    private ASTNode getLastNode() {
      List<Statement> statements = fDeclaration.getBody().statements();
      if (statements.size() == 0) return null;
      return statements.get(statements.size() - 1);
    }

    private IMethodBinding getBinding() {
      IMethodBinding result = fDeclaration.resolveBinding();
      if (result != null) return result.getMethodDeclaration();
      return result;
    }
  }

  private class UpdateCollector extends ASTVisitor {
    private int fTypeCounter;

    @Override
    public boolean visit(TypeDeclaration node) {
      return visitType(node);
    }

    @Override
    public void endVisit(TypeDeclaration node) {
      fTypeCounter--;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
      return visitType(node);
    }

    @Override
    public void endVisit(EnumDeclaration node) {
      fTypeCounter--;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
      return visitType(node);
    }

    @Override
    public void endVisit(AnnotationTypeDeclaration node) {
      fTypeCounter--;
    }

    private boolean visitType(AbstractTypeDeclaration node) {
      if (fTypeCounter++ == 0) {
        addNameReference(node.getName());
      }
      return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      fTypeCounter++;
      return true;
    }

    @Override
    public void endVisit(AnonymousClassDeclaration node) {
      fTypeCounter--;
    }

    @Override
    public boolean visit(FieldAccess node) {
      // only visit the expression and not the simple name
      node.getExpression().accept(this);
      addReferencesToName(node.getName());
      return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
      if (node.isConstructor()) {
        AbstractTypeDeclaration decl =
            (AbstractTypeDeclaration) ASTNodes.getParent(node, AbstractTypeDeclaration.class);
        NameData name = fNames.get(decl.getName().resolveBinding());
        if (name != null) {
          name.addReference(node.getName());
        }
      }
      return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      if (fTypeCounter == 0) {
        Expression receiver = node.getExpression();
        if (receiver == null && !isStaticallyImported(node.getName())) {
          fImplicitReceivers.add(node);
        }
      }
      return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
      if (fTypeCounter == 0) {
        fHasSuperMethodInvocation = true;
      }
      return true;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
      if (fTypeCounter == 0) {
        fHasSuperMethodInvocation = true;
      }
      return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
      if (fTypeCounter == 0) {
        Expression receiver = node.getExpression();
        if (receiver == null) {
          if (node.resolveTypeBinding().isLocal()) fImplicitReceivers.add(node);
        }
      }
      return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
      if (fTypeCounter == 0) addNameReference(node.getName());
      return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
      if (fTypeCounter == 0) addNameReference(node.getName());
      return true;
    }

    @Override
    public boolean visit(SimpleName node) {
      addReferencesToName(node);
      IBinding binding = node.resolveBinding();
      if (binding instanceof ITypeBinding) {
        ITypeBinding type = (ITypeBinding) binding;
        if (type.isTypeVariable()) {
          addTypeVariableReference(type, node);
        }
      } else if (binding instanceof IVariableBinding) {
        IVariableBinding vb = (IVariableBinding) binding;
        if (vb.isField() && !isStaticallyImported(node)) {
          Name topName = ASTNodes.getTopMostName(node);
          if (node == topName || node == ASTNodes.getLeftMostSimpleName(topName)) {
            StructuralPropertyDescriptor location = node.getLocationInParent();
            if (location != SingleVariableDeclaration.NAME_PROPERTY
                && location != VariableDeclarationFragment.NAME_PROPERTY) {
              fImplicitReceivers.add(node);
            }
          }
        } else if (!vb.isField()) {
          // we have a local. Check if it is a parameter.
          ParameterData data = fParameters.get(binding);
          if (data != null) {
            ASTNode parent = node.getParent();
            if (parent instanceof Expression) {
              int precedence = OperatorPrecedence.getExpressionPrecedence((Expression) parent);
              if (precedence != Integer.MAX_VALUE) {
                data.setOperatorPrecedence(precedence);
              }
            }
          }
        }
      }
      return true;
    }

    @Override
    public boolean visit(ThisExpression node) {
      if (fTypeCounter == 0) {
        fImplicitReceivers.add(node);
      }
      return true;
    }

    private void addReferencesToName(SimpleName node) {
      IBinding binding = node.resolveBinding();
      ParameterData data = fParameters.get(binding);
      if (data != null) data.addReference(node);

      NameData name = fNames.get(binding);
      if (name != null) name.addReference(node);
    }

    private void addNameReference(SimpleName name) {
      fNames.put(name.resolveBinding(), new NameData(name.getIdentifier()));
    }

    private void addTypeVariableReference(ITypeBinding variable, SimpleName name) {
      NameData data = fTypeParameterMapping.get(variable);
      if (data == null) {
        data = fMethodTypeParameterMapping.get(variable);
      }
      data.addReference(name);
    }

    private boolean isStaticallyImported(Name name) {
      return fStaticsToImport.contains(name);
    }
  }

  private class VarargAnalyzer extends ASTVisitor {
    private IBinding fParameter;

    public VarargAnalyzer(IBinding parameter) {
      fParameter = parameter;
    }

    @Override
    public boolean visit(ArrayAccess node) {
      Expression array = node.getArray();
      if (array instanceof SimpleName
          && fParameter.isEqualTo(((SimpleName) array).resolveBinding())) {
        fArrayAccess = true;
      }
      return true;
    }
  }

  private ITypeRoot fTypeRoot;
  private MethodDeclaration fDeclaration;
  private Map<IVariableBinding, ParameterData> fParameters;
  private Map<IBinding, NameData> fNames;
  private List<Expression> fImplicitReceivers;

  private boolean fArrayAccess;
  private boolean fHasSuperMethodInvocation;

  private List<SimpleName> fTypesToImport;
  private List<SimpleName> fStaticsToImport;

  private List<NameData> fTypeParameterReferences;
  private Map<ITypeBinding, NameData> fTypeParameterMapping;

  private List<NameData> fMethodTypeParameterReferences;
  private Map<ITypeBinding, NameData> fMethodTypeParameterMapping;

  private boolean fInterruptedExecutionFlow;

  public SourceAnalyzer(ITypeRoot typeRoot, MethodDeclaration declaration) {
    super();
    fTypeRoot = typeRoot;
    fDeclaration = declaration;
  }

  public boolean isExecutionFlowInterrupted() {
    return fInterruptedExecutionFlow;
  }

  public RefactoringStatus checkActivation() throws JavaModelException {
    RefactoringStatus result = new RefactoringStatus();
    if (!fTypeRoot.isStructureKnown()) {
      result.addFatalError(
          RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_syntax_errors,
          JavaStatusContext.create(fTypeRoot));
      return result;
    }
    IProblem[] problems = ASTNodes.getProblems(fDeclaration, ASTNodes.NODE_ONLY, ASTNodes.ERROR);
    if (problems.length > 0) {
      result.addFatalError(
          RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_declaration_has_errors,
          JavaStatusContext.create(fTypeRoot, fDeclaration));
      return result;
    }
    final IMethodBinding declarationBinding = fDeclaration.resolveBinding();
    if (declarationBinding != null) {
      final int modifiers = declarationBinding.getModifiers();
      if (Modifier.isAbstract(modifiers)) {
        result.addFatalError(
            RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_abstract_methods,
            JavaStatusContext.create(fTypeRoot, fDeclaration));
        return result;
      } else if (Modifier.isNative(modifiers)) {
        result.addFatalError(
            RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_native_methods,
            JavaStatusContext.create(fTypeRoot, fDeclaration));
        return result;
      }
    } else {
      result.addFatalError(
          RefactoringCoreMessages
              .InlineMethodRefactoring_SourceAnalyzer_methoddeclaration_has_errors,
          JavaStatusContext.create(fTypeRoot));
      return result;
    }
    ActivationAnalyzer analyzer = new ActivationAnalyzer();
    fDeclaration.accept(analyzer);
    result.merge(analyzer.status);
    if (!result.hasFatalError()) {
      List<SingleVariableDeclaration> parameters = fDeclaration.parameters();
      fParameters = new HashMap<IVariableBinding, ParameterData>(parameters.size() * 2);
      for (Iterator<SingleVariableDeclaration> iter = parameters.iterator(); iter.hasNext(); ) {
        SingleVariableDeclaration element = iter.next();
        IVariableBinding binding = element.resolveBinding();
        if (binding == null) {
          result.addFatalError(
              RefactoringCoreMessages.InlineMethodRefactoring_SourceAnalyzer_declaration_has_errors,
              JavaStatusContext.create(fTypeRoot, fDeclaration));
          return result;
        }
        fParameters.put(binding, (ParameterData) element.getProperty(ParameterData.PROPERTY));
      }
      fNames = new HashMap<IBinding, NameData>();
      fImplicitReceivers = new ArrayList<Expression>(2);

      fTypeParameterReferences = new ArrayList<NameData>(0);
      fTypeParameterMapping = new HashMap<ITypeBinding, NameData>();
      ITypeBinding declaringType = declarationBinding.getDeclaringClass();
      if (declaringType == null) {
        result.addFatalError(
            RefactoringCoreMessages
                .InlineMethodRefactoring_SourceAnalyzer_typedeclaration_has_errors,
            JavaStatusContext.create(fTypeRoot));
        return result;
      }
      ITypeBinding[] typeParameters = declaringType.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        NameData data = new NameData(typeParameters[i].getName());
        fTypeParameterReferences.add(data);
        fTypeParameterMapping.put(typeParameters[i], data);
      }

      fMethodTypeParameterReferences = new ArrayList<NameData>(0);
      fMethodTypeParameterMapping = new HashMap<ITypeBinding, NameData>();
      IMethodBinding method = declarationBinding;
      typeParameters = method.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        NameData data = new NameData(typeParameters[i].getName());
        fMethodTypeParameterReferences.add(data);
        fMethodTypeParameterMapping.put(typeParameters[i], data);
      }
    }
    if (fDeclaration.isVarargs()) {
      List<SingleVariableDeclaration> parameters = fDeclaration.parameters();
      VarargAnalyzer vAnalyzer =
          new VarargAnalyzer(parameters.get(parameters.size() - 1).getName().resolveBinding());
      fDeclaration.getBody().accept(vAnalyzer);
    }
    return result;
  }

  public void initialize() {
    Block body = fDeclaration.getBody();
    // first collect the static imports. This is necessary to not mark
    // static imported fields and methods as implicit visible.
    fTypesToImport = new ArrayList<SimpleName>();
    fStaticsToImport = new ArrayList<SimpleName>();
    ImportReferencesCollector.collect(
        body, fTypeRoot.getJavaProject(), null, fTypesToImport, fStaticsToImport);

    // Now collect implicit references and name references
    body.accept(new UpdateCollector());

    int numberOfLocals = LocalVariableIndex.perform(fDeclaration);
    FlowContext context = new FlowContext(0, numberOfLocals + 1);
    context.setConsiderAccessMode(true);
    context.setComputeMode(FlowContext.MERGE);
    InOutFlowAnalyzer flowAnalyzer = new InOutFlowAnalyzer(context);
    FlowInfo info = flowAnalyzer.perform(getStatements());

    for (Iterator<SingleVariableDeclaration> iter = fDeclaration.parameters().iterator();
        iter.hasNext(); ) {
      SingleVariableDeclaration element = iter.next();
      IVariableBinding binding = element.resolveBinding();
      ParameterData data = (ParameterData) element.getProperty(ParameterData.PROPERTY);
      data.setAccessMode(info.getAccessMode(context, binding));
    }
  }

  public Collection<NameData> getUsedNames() {
    return fNames.values();
  }

  public List<Expression> getImplicitReceivers() {
    return fImplicitReceivers;
  }

  public List<SimpleName> getTypesToImport() {
    return fTypesToImport;
  }

  public List<SimpleName> getStaticsToImport() {
    return fStaticsToImport;
  }

  public List<NameData> getTypeParameterReferences() {
    return fTypeParameterReferences;
  }

  public List<NameData> getMethodTypeParameterReferences() {
    return fMethodTypeParameterReferences;
  }

  public boolean hasArrayAccess() {
    return fArrayAccess;
  }

  public boolean hasSuperMethodInvocation() {
    return fHasSuperMethodInvocation;
  }

  private ASTNode[] getStatements() {
    List<Statement> statements = fDeclaration.getBody().statements();
    return statements.toArray(new ASTNode[statements.size()]);
  }
}
