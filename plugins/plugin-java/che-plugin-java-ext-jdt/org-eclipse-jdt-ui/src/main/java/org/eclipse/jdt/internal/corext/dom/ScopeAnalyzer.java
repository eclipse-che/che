/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

/**
 * Evaluates all fields, methods and types available (declared) at a given offset in a compilation
 * unit (Code assist that returns IBindings)
 */
public class ScopeAnalyzer {

  private static final IBinding[] NO_BINDING = new IBinding[0];

  /** Flag to specify that method should be reported. */
  public static final int METHODS = 1;

  /** Flag to specify that variables should be reported. */
  public static final int VARIABLES = 2;

  /** Flag to specify that types should be reported. */
  public static final int TYPES = 4;

  /** Flag to specify that only visible elements should be added. */
  public static final int CHECK_VISIBILITY = 16;

  private static interface IBindingRequestor {
    boolean acceptBinding(IBinding binding);
  }

  private static class DefaultBindingRequestor implements IBindingRequestor {

    private final List<IBinding> fResult;
    private final HashSet<String> fNamesAdded;
    private final int fFlags;
    private final ITypeBinding fParentTypeBinding;

    public DefaultBindingRequestor(ITypeBinding parentTypeBinding, int flags) {
      fParentTypeBinding = parentTypeBinding;
      fFlags = flags;
      fResult = new ArrayList<IBinding>();
      fNamesAdded = new HashSet<String>();
    }

    public DefaultBindingRequestor() {
      this(null, 0);
    }

    /** {@inheritDoc} */
    public boolean acceptBinding(IBinding binding) {
      if (binding == null) return false;

      String signature = getSignature(binding);
      if (signature != null
          && fNamesAdded.add(signature)) { // avoid duplicated results from inheritance
        fResult.add(binding);
      }
      return false;
    }

    public List<IBinding> getResult() {
      if (hasFlag(CHECK_VISIBILITY, fFlags)) {
        for (int i = fResult.size() - 1; i >= 0; i--) {
          IBinding binding = fResult.get(i);
          if (!isVisible(binding, fParentTypeBinding)) {
            fResult.remove(i);
          }
        }
      }
      return fResult;
    }
  }

  private final HashSet<ITypeBinding> fTypesVisited;

  private final CompilationUnit fRoot;

  public ScopeAnalyzer(CompilationUnit root) {
    fTypesVisited = new HashSet<ITypeBinding>();
    fRoot = root;
  }

  private void clearLists() {
    fTypesVisited.clear();
  }

  private static String getSignature(IBinding binding) {
    if (binding != null) {
      switch (binding.getKind()) {
        case IBinding.METHOD:
          StringBuffer buf = new StringBuffer();
          buf.append('M');
          buf.append(binding.getName()).append('(');
          ITypeBinding[] parameters = ((IMethodBinding) binding).getParameterTypes();
          for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
              buf.append(',');
            }
            ITypeBinding paramType = parameters[i].getErasure();
            buf.append(paramType.getQualifiedName());
          }
          buf.append(')');
          return buf.toString();
        case IBinding.VARIABLE:
          return 'V' + binding.getName();
        case IBinding.TYPE:
          return 'T' + binding.getName();
      }
    }
    return null;
  }

  static final boolean hasFlag(int property, int flags) {
    return (flags & property) != 0;
  }

  /**
   * Collects all elements available in a type and its hierarchy
   *
   * @param binding The type binding
   * @param flags Flags defining the elements to report
   * @param requestor the requestor to which all results are reported
   * @return return <code>true</code> if the requestor has reported the binding as found and no
   *     further results are required
   */
  private boolean addInherited(ITypeBinding binding, int flags, IBindingRequestor requestor) {
    if (!fTypesVisited.add(binding)) {
      return false;
    }
    if (hasFlag(VARIABLES, flags)) {
      IVariableBinding[] variableBindings = binding.getDeclaredFields();
      for (int i = 0; i < variableBindings.length; i++) {
        if (requestor.acceptBinding(variableBindings[i])) return true;
      }
    }

    if (hasFlag(METHODS, flags)) {
      IMethodBinding[] methodBindings = binding.getDeclaredMethods();
      for (int i = 0; i < methodBindings.length; i++) {
        IMethodBinding curr = methodBindings[i];
        if (!curr.isSynthetic() && !curr.isConstructor()) {
          if (requestor.acceptBinding(curr)) return true;
        }
      }
    }

    if (hasFlag(TYPES, flags)) {
      ITypeBinding[] typeBindings = binding.getDeclaredTypes();
      for (int i = 0; i < typeBindings.length; i++) {
        ITypeBinding curr = typeBindings[i];
        if (requestor.acceptBinding(curr)) return true;
      }
    }

    ITypeBinding superClass = binding.getSuperclass();
    if (superClass != null) {
      if (addInherited(superClass, flags, requestor)) // recursive
      return true;
    } else if (binding.isArray()) {
      if (addInherited(
          fRoot.getAST().resolveWellKnownType("java.lang.Object"), flags, requestor)) // $NON-NLS-1$
      return true;
    }

    ITypeBinding[] interfaces =
        binding.getInterfaces(); // includes looking for methods: abstract, unimplemented methods
    for (int i = 0; i < interfaces.length; i++) {
      if (addInherited(interfaces[i], flags, requestor)) // recursive
      return true;
    }
    return false;
  }

  /**
   * Collects all elements available in a type: its hierarchy and its outer scopes.
   *
   * @param binding The type binding
   * @param flags Flags defining the elements to report
   * @param requestor the requestor to which all results are reported
   * @return return <code>true</code> if the requestor has reported the binding as found and no
   *     further results are required
   */
  private boolean addTypeDeclarations(
      ITypeBinding binding, int flags, IBindingRequestor requestor) {
    if (hasFlag(TYPES, flags) && !binding.isAnonymous()) {
      if (requestor.acceptBinding(binding)) return true;

      ITypeBinding[] typeParameters = binding.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        if (requestor.acceptBinding(typeParameters[i])) return true;
      }
    }

    addInherited(binding, flags, requestor); // add inherited

    if (binding.isLocal()) {
      addOuterDeclarationsForLocalType(binding, flags, requestor);
    } else {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) {
        if (addTypeDeclarations(declaringClass, flags, requestor)) // Recursively add inherited
        return true;
      } else if (hasFlag(TYPES, flags)) {
        if (fRoot.findDeclaringNode(binding) != null) {
          List<AbstractTypeDeclaration> types = fRoot.types();
          for (int i = 0; i < types.size(); i++) {
            if (requestor.acceptBinding(types.get(i).resolveBinding())) return true;
          }
        }
      }
    }
    return false;
  }

  private boolean addOuterDeclarationsForLocalType(
      ITypeBinding localBinding, int flags, IBindingRequestor requestor) {
    ASTNode node = fRoot.findDeclaringNode(localBinding);
    if (node == null) {
      return false;
    }

    if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration) {
      if (addLocalDeclarations(node.getParent(), flags, requestor)) return true;

      ITypeBinding parentTypeBinding = Bindings.getBindingOfParentType(node.getParent());
      if (parentTypeBinding != null) {
        if (addTypeDeclarations(parentTypeBinding, flags, requestor)) return true;
      }
    }
    return false;
  }

  private static ITypeBinding getBinding(Expression node) {
    if (node != null) {
      return node.resolveTypeBinding();
    }
    return null;
  }

  private static ITypeBinding getQualifier(SimpleName selector) {
    ASTNode parent = selector.getParent();
    switch (parent.getNodeType()) {
      case ASTNode.METHOD_INVOCATION:
        MethodInvocation decl = (MethodInvocation) parent;
        if (selector == decl.getName()) {
          return getBinding(decl.getExpression());
        }
        return null;
      case ASTNode.QUALIFIED_NAME:
        QualifiedName qualifiedName = (QualifiedName) parent;
        if (selector == qualifiedName.getName()) {
          return getBinding(qualifiedName.getQualifier());
        }
        return null;
      case ASTNode.FIELD_ACCESS:
        FieldAccess fieldAccess = (FieldAccess) parent;
        if (selector == fieldAccess.getName()) {
          return getBinding(fieldAccess.getExpression());
        }
        return null;
      case ASTNode.SUPER_FIELD_ACCESS:
        {
          ITypeBinding curr = Bindings.getBindingOfParentType(parent);
          return curr.getSuperclass();
        }
      case ASTNode.SUPER_METHOD_INVOCATION:
        {
          SuperMethodInvocation superInv = (SuperMethodInvocation) parent;
          if (selector == superInv.getName()) {
            ITypeBinding curr = Bindings.getBindingOfParentType(parent);
            return curr.getSuperclass();
          }
          return null;
        }
      default:
        if (parent instanceof Type) {
          // bug 67644: in 'a.new X()', all member types of A are visible as location of X.
          ASTNode normalizedNode = ASTNodes.getNormalizedNode(parent);
          if (normalizedNode.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY) {
            ClassInstanceCreation creation = (ClassInstanceCreation) normalizedNode.getParent();
            return getBinding(creation.getExpression());
          }
        }
        return null;
    }
  }

  public IBinding[] getDeclarationsInScope(SimpleName selector, int flags) {
    try {
      // special case for switch on enum
      if (selector.getLocationInParent() == SwitchCase.EXPRESSION_PROPERTY) {
        ITypeBinding binding =
            ((SwitchStatement) selector.getParent().getParent())
                .getExpression()
                .resolveTypeBinding();
        if (binding != null && binding.isEnum()) {
          return getEnumContants(binding);
        }
      }

      ITypeBinding parentTypeBinding = Bindings.getBindingOfParentType(selector);
      if (parentTypeBinding != null) {
        ITypeBinding binding = getQualifier(selector);
        DefaultBindingRequestor requestor = new DefaultBindingRequestor(parentTypeBinding, flags);
        if (binding == null) {
          addLocalDeclarations(selector, flags, requestor);
          addTypeDeclarations(parentTypeBinding, flags, requestor);
        } else {
          addInherited(binding, flags, requestor);
        }

        List<IBinding> result = requestor.getResult();
        return result.toArray(new IBinding[result.size()]);
      }
      return NO_BINDING;
    } finally {
      clearLists();
    }
  }

  private static class SearchRequestor implements IBindingRequestor {

    private final int fFlags;
    private final ITypeBinding fParentTypeBinding;
    private final IBinding fToSearch;
    private boolean fFound;
    private boolean fIsVisible;

    public SearchRequestor(IBinding toSearch, ITypeBinding parentTypeBinding, int flag) {
      fFlags = flag;
      fToSearch = toSearch;
      fParentTypeBinding = parentTypeBinding;
      fFound = false;
      fIsVisible = true;
    }

    public boolean acceptBinding(IBinding binding) {
      if (fFound) return true;

      if (binding == null) return false;

      if (fToSearch.getKind() != binding.getKind()) {
        return false;
      }

      boolean checkVisibility = hasFlag(CHECK_VISIBILITY, fFlags);
      if (binding == fToSearch) {
        fFound = true;
      } else {
        IBinding bindingDeclaration = Bindings.getDeclaration(binding);
        if (bindingDeclaration == fToSearch) {
          fFound = true;
        } else if (bindingDeclaration.getName().equals(fToSearch.getName())) {
          String signature = getSignature(bindingDeclaration);
          if (signature != null && signature.equals(getSignature(fToSearch))) {
            if (checkVisibility) {
              fIsVisible = false;
            }
            return true; // found element that hides the binding to find
          }
        }
      }

      if (fFound && checkVisibility) {
        fIsVisible = ScopeAnalyzer.isVisible(binding, fParentTypeBinding);
      }
      return fFound;
    }

    public boolean found() {
      return fFound;
    }

    public boolean isVisible() {
      return fIsVisible;
    }
  }

  public boolean isDeclaredInScope(IBinding declaration, SimpleName selector, int flags) {
    try {
      // special case for switch on enum
      if (selector.getLocationInParent() == SwitchCase.EXPRESSION_PROPERTY) {
        ITypeBinding binding =
            ((SwitchStatement) selector.getParent().getParent())
                .getExpression()
                .resolveTypeBinding();
        if (binding != null && binding.isEnum()) {
          return hasEnumContants(declaration, binding.getTypeDeclaration());
        }
      }

      ITypeBinding parentTypeBinding = Bindings.getBindingOfParentTypeContext(selector);
      if (parentTypeBinding != null) {
        ITypeBinding binding = getQualifier(selector);
        SearchRequestor requestor = new SearchRequestor(declaration, parentTypeBinding, flags);
        if (binding == null) {
          addLocalDeclarations(selector, flags, requestor);
          if (requestor.found()) return requestor.isVisible();
          addTypeDeclarations(parentTypeBinding, flags, requestor);
          if (requestor.found()) return requestor.isVisible();
        } else {
          addInherited(binding, flags, requestor);
          if (requestor.found()) return requestor.isVisible();
        }
      }
      return false;
    } finally {
      clearLists();
    }
  }

  private IVariableBinding[] getEnumContants(ITypeBinding binding) {
    IVariableBinding[] declaredFields = binding.getDeclaredFields();
    ArrayList<IVariableBinding> res = new ArrayList<IVariableBinding>(declaredFields.length);
    for (int i = 0; i < declaredFields.length; i++) {
      IVariableBinding curr = declaredFields[i];
      if (curr.isEnumConstant()) {
        res.add(curr);
      }
    }
    return res.toArray(new IVariableBinding[res.size()]);
  }

  private boolean hasEnumContants(IBinding declaration, ITypeBinding binding) {
    IVariableBinding[] declaredFields = binding.getDeclaredFields();
    for (int i = 0; i < declaredFields.length; i++) {
      IVariableBinding curr = declaredFields[i];
      if (curr == declaration) return true;
    }
    return false;
  }

  public IBinding[] getDeclarationsInScope(int offset, int flags) {
    org.eclipse.jdt.core.dom.NodeFinder finder =
        new org.eclipse.jdt.core.dom.NodeFinder(fRoot, offset, 0);
    ASTNode node = finder.getCoveringNode();
    if (node == null) {
      return NO_BINDING;
    }

    if (node instanceof SimpleName) {
      return getDeclarationsInScope((SimpleName) node, flags);
    }

    try {
      ITypeBinding binding = Bindings.getBindingOfParentType(node);
      DefaultBindingRequestor requestor = new DefaultBindingRequestor(binding, flags);
      addLocalDeclarations(node, offset, flags, requestor);
      if (binding != null) {
        addTypeDeclarations(binding, flags, requestor);
      }
      List<IBinding> result = requestor.getResult();
      return result.toArray(new IBinding[result.size()]);
    } finally {
      clearLists();
    }
  }

  private static ITypeBinding getDeclaringType(IBinding binding) {
    switch (binding.getKind()) {
      case IBinding.VARIABLE:
        return ((IVariableBinding) binding).getDeclaringClass();
      case IBinding.METHOD:
        return ((IMethodBinding) binding).getDeclaringClass();
      case IBinding.TYPE:
        ITypeBinding typeBinding = (ITypeBinding) binding;
        if (typeBinding.getDeclaringClass() != null) {
          return typeBinding;
        }
        return typeBinding;
    }
    return null;
  }

  /**
   * Evaluates if the declaration is visible in a certain context.
   *
   * @param binding The binding of the declaration to examine
   * @param context The context to test in
   * @return Returns
   */
  public static boolean isVisible(IBinding binding, ITypeBinding context) {
    if (binding.getKind() == IBinding.VARIABLE && !((IVariableBinding) binding).isField()) {
      return true; // all local variables found are visible
    }
    ITypeBinding declaring = getDeclaringType(binding);
    if (declaring == null) {
      return false;
    }

    declaring = declaring.getTypeDeclaration();

    int modifiers = binding.getModifiers();
    if (Modifier.isPublic(modifiers) || declaring.isInterface()) {
      return true;
    } else if (Modifier.isProtected(modifiers) || !Modifier.isPrivate(modifiers)) {
      if (declaring.getPackage() == context.getPackage()) {
        return true;
      }
      return isTypeInScope(declaring, context, Modifier.isProtected(modifiers));
    }
    // private visibility
    return isTypeInScope(declaring, context, false);
  }

  private static boolean isTypeInScope(
      ITypeBinding declaring, ITypeBinding context, boolean includeHierarchy) {
    ITypeBinding curr = context.getTypeDeclaration();
    while (curr != null && curr != declaring) {
      if (includeHierarchy && isInSuperTypeHierarchy(declaring, curr)) {
        return true;
      }
      curr = curr.getDeclaringClass();
    }
    return curr == declaring;
  }

  /*
   * This method is different from Binding.isSuperType as type declarations are compared
   */
  private static boolean isInSuperTypeHierarchy(
      ITypeBinding possibleSuperTypeDecl, ITypeBinding type) {
    if (type == possibleSuperTypeDecl) {
      return true;
    }
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      if (isInSuperTypeHierarchy(possibleSuperTypeDecl, superClass.getTypeDeclaration())) {
        return true;
      }
    }
    if (possibleSuperTypeDecl.isInterface()) {
      ITypeBinding[] superInterfaces = type.getInterfaces();
      for (int i = 0; i < superInterfaces.length; i++) {
        if (isInSuperTypeHierarchy(
            possibleSuperTypeDecl, superInterfaces[i].getTypeDeclaration())) {
          return true;
        }
      }
    }
    return false;
  }

  public IBinding[] getDeclarationsAfter(int offset, int flags) {
    try {
      org.eclipse.jdt.core.dom.NodeFinder finder =
          new org.eclipse.jdt.core.dom.NodeFinder(fRoot, offset, 0);
      ASTNode node = finder.getCoveringNode();
      if (node == null) {
        return null;
      }

      ASTNode declaration = ASTResolving.findParentStatement(node);
      while (declaration instanceof Statement && declaration.getNodeType() != ASTNode.BLOCK) {
        declaration = declaration.getParent();
      }

      if (declaration instanceof Block) {
        DefaultBindingRequestor requestor = new DefaultBindingRequestor();
        DeclarationsAfterVisitor visitor =
            new DeclarationsAfterVisitor(node.getStartPosition(), flags, requestor);
        declaration.accept(visitor);
        List<IBinding> result = requestor.getResult();
        return result.toArray(new IBinding[result.size()]);
      }
      return NO_BINDING;
    } finally {
      clearLists();
    }
  }

  private class ScopeAnalyzerVisitor extends HierarchicalASTVisitor {

    private final int fPosition;
    private final int fFlags;
    private final IBindingRequestor fRequestor;
    private boolean fBreak;

    public ScopeAnalyzerVisitor(int position, int flags, IBindingRequestor requestor) {
      fPosition = position;
      fFlags = flags;
      fRequestor = requestor;
      fBreak = false;
    }

    private boolean isInside(ASTNode node) {
      int start = node.getStartPosition();
      int end = start + node.getLength();

      return start <= fPosition && fPosition < end;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
      if (isInside(node)) {
        Block body = node.getBody();
        if (body != null) {
          body.accept(this);
        }
        visitBackwards(node.parameters());
        visitBackwards(node.typeParameters());
      }
      return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.HierarchicalASTVisitor#visit(org.eclipse.jdt.core.dom.TypeParameter)
     */
    @Override
    public boolean visit(TypeParameter node) {
      if (hasFlag(TYPES, fFlags) && node.getStartPosition() < fPosition) {
        fBreak = fRequestor.acceptBinding(node.getName().resolveBinding());
      }
      return !fBreak;
    }

    @Override
    public boolean visit(SwitchCase node) {
      // switch on enum allows to use enum constants without qualification
      if (hasFlag(VARIABLES, fFlags) && !node.isDefault() && isInside(node.getExpression())) {
        SwitchStatement switchStatement = (SwitchStatement) node.getParent();
        ITypeBinding binding = switchStatement.getExpression().resolveTypeBinding();
        if (binding != null && binding.isEnum()) {
          IVariableBinding[] declaredFields = binding.getDeclaredFields();
          for (int i = 0; i < declaredFields.length; i++) {
            IVariableBinding curr = declaredFields[i];
            if (curr.isEnumConstant()) {
              fBreak = fRequestor.acceptBinding(curr);
              if (fBreak) return false;
            }
          }
        }
      }
      return false;
    }

    @Override
    public boolean visit(Initializer node) {
      return !fBreak && isInside(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
      return !fBreak && isInside(node);
    }

    @Override
    public boolean visit(Expression node) {
      return !fBreak && isInside(node);
    }

    @Override
    public boolean visit(Statement node) {
      return !fBreak && isInside(node);
    }

    @Override
    public boolean visit(ASTNode node) {
      return false;
    }

    @Override
    public boolean visit(Block node) {
      if (isInside(node)) {
        visitBackwards(node.statements());
      }
      return false;
    }

    @Override
    public boolean visit(VariableDeclaration node) {
      if (hasFlag(VARIABLES, fFlags) && node.getStartPosition() < fPosition) {
        fBreak = fRequestor.acceptBinding(node.resolveBinding());
      }
      return !fBreak;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
      visitBackwards(node.fragments());
      return false;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
      visitBackwards(node.fragments());
      return false;
    }

    @Override
    public boolean visit(CatchClause node) {
      if (isInside(node)) {
        node.getBody().accept(this);
        node.getException().accept(this);
      }
      return false;
    }

    @Override
    public boolean visit(ForStatement node) {
      if (isInside(node)) {
        node.getBody().accept(this);
        visitBackwards(node.initializers());
      }
      return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
      if (hasFlag(TYPES, fFlags) && node.getStartPosition() + node.getLength() < fPosition) {
        fBreak = fRequestor.acceptBinding(node.resolveBinding());
        return false;
      }
      return !fBreak && isInside(node);
    }

    private void visitBackwards(List<? extends ASTNode> list) {
      if (fBreak) return;

      for (int i = list.size() - 1; i >= 0; i--) {
        ASTNode curr = list.get(i);
        if (curr.getStartPosition() < fPosition) {
          curr.accept(this);
        }
      }
    }
  }

  private class DeclarationsAfterVisitor extends HierarchicalASTVisitor {
    private final int fPosition;
    private final int fFlags;
    private final IBindingRequestor fRequestor;
    private boolean fBreak;

    public DeclarationsAfterVisitor(int position, int flags, IBindingRequestor requestor) {
      fPosition = position;
      fFlags = flags;
      fRequestor = requestor;
      fBreak = false;
    }

    @Override
    public boolean visit(ASTNode node) {
      return !fBreak;
    }

    @Override
    public boolean visit(VariableDeclaration node) {
      if (hasFlag(VARIABLES, fFlags) && fPosition < node.getStartPosition()) {
        fBreak = fRequestor.acceptBinding(node.resolveBinding());
      }
      return false;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
      if (hasFlag(TYPES, fFlags) && fPosition < node.getStartPosition()) {
        fBreak = fRequestor.acceptBinding(node.resolveBinding());
      }
      return false;
    }
  }

  private boolean addLocalDeclarations(ASTNode node, int flags, IBindingRequestor requestor) {
    return addLocalDeclarations(node, node.getStartPosition(), flags, requestor);
  }

  private boolean addLocalDeclarations(
      ASTNode node, int offset, int flags, IBindingRequestor requestor) {
    if (hasFlag(VARIABLES, flags) || hasFlag(TYPES, flags)) {
      BodyDeclaration declaration = ASTResolving.findParentBodyDeclaration(node);
      if (declaration instanceof MethodDeclaration
          || declaration instanceof Initializer
          || declaration instanceof FieldDeclaration) {
        ScopeAnalyzerVisitor visitor = new ScopeAnalyzerVisitor(offset, flags, requestor);
        declaration.accept(visitor);
        return visitor.fBreak;
      }
    }
    return false;
  }

  public Collection<String> getUsedVariableNames(int offset, int length) {
    HashSet<String> result = new HashSet<String>();
    IBinding[] bindingsBefore = getDeclarationsInScope(offset, VARIABLES);
    for (int i = 0; i < bindingsBefore.length; i++) {
      result.add(bindingsBefore[i].getName());
    }
    IBinding[] bindingsAfter = getDeclarationsAfter(offset + length, VARIABLES);
    for (int i = 0; i < bindingsAfter.length; i++) {
      result.add(bindingsAfter[i].getName());
    }
    List<ImportDeclaration> imports = fRoot.imports();
    for (int i = 0; i < imports.size(); i++) {
      ImportDeclaration decl = imports.get(i);
      if (decl.isStatic() && !decl.isOnDemand()) {
        result.add(ASTNodes.getSimpleNameIdentifier(decl.getName()));
      }
    }
    return result;
  }
}
