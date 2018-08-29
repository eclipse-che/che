/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.codemanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.CodeGeneration;

/**
 * Utilities for code generation based on AST rewrite.
 *
 * @since 3.1
 */
public final class StubUtility2 {

  public static void addOverrideAnnotation(
      IJavaProject project, ASTRewrite rewrite, MethodDeclaration decl, IMethodBinding binding) {
    if (binding.getDeclaringClass().isInterface()) {
      String version = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
      if (JavaModelUtil.isVersionLessThan(version, JavaCore.VERSION_1_6))
        return; // not allowed in 1.5
      if (JavaCore.DISABLED.equals(
          project.getOption(
              JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION,
              true))) return; // user doesn't want to use 1.6 style
    }

    Annotation marker = rewrite.getAST().newMarkerAnnotation();
    marker.setTypeName(rewrite.getAST().newSimpleName("Override")); // $NON-NLS-1$
    rewrite.getListRewrite(decl, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(marker, null);
  }

  /* This method should work with all AST levels. */
  public static MethodDeclaration createConstructorStub(
      ICompilationUnit unit,
      ASTRewrite rewrite,
      ImportRewrite imports,
      ImportRewriteContext context,
      IMethodBinding binding,
      String type,
      int modifiers,
      boolean omitSuperForDefConst,
      boolean todo,
      CodeGenerationSettings settings)
      throws CoreException {
    AST ast = rewrite.getAST();
    MethodDeclaration decl = ast.newMethodDeclaration();
    decl.modifiers()
        .addAll(
            ASTNodeFactory.newModifiers(ast, modifiers & ~Modifier.ABSTRACT & ~Modifier.NATIVE));
    decl.setName(ast.newSimpleName(type));
    decl.setConstructor(true);

    createTypeParameters(imports, context, ast, binding, decl);

    List<SingleVariableDeclaration> parameters =
        createParameters(unit.getJavaProject(), imports, context, ast, binding, null, decl);

    createThrownExceptions(decl, binding, imports, context, ast);

    Block body = ast.newBlock();
    decl.setBody(body);

    String delimiter = StubUtility.getLineDelimiterUsed(unit);
    String bodyStatement = ""; // $NON-NLS-1$
    if (!omitSuperForDefConst || !parameters.isEmpty()) {
      SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();
      SingleVariableDeclaration varDecl = null;
      for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator();
          iterator.hasNext(); ) {
        varDecl = iterator.next();
        invocation.arguments().add(ast.newSimpleName(varDecl.getName().getIdentifier()));
      }
      bodyStatement =
          ASTNodes.asFormattedString(
              invocation, 0, delimiter, unit.getJavaProject().getOptions(true));
    }

    if (todo) {
      String placeHolder =
          CodeGeneration.getMethodBodyContent(
              unit, type, binding.getName(), true, bodyStatement, delimiter);
      if (placeHolder != null) {
        ReturnStatement todoNode =
            (ReturnStatement)
                rewrite.createStringPlaceholder(placeHolder, ASTNode.RETURN_STATEMENT);
        body.statements().add(todoNode);
      }
    } else {
      ReturnStatement statementNode =
          (ReturnStatement)
              rewrite.createStringPlaceholder(bodyStatement, ASTNode.RETURN_STATEMENT);
      body.statements().add(statementNode);
    }

    if (settings != null && settings.createComments) {
      String string = CodeGeneration.getMethodComment(unit, type, decl, binding, delimiter);
      if (string != null) {
        Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
        decl.setJavadoc(javadoc);
      }
    }
    return decl;
  }

  public static MethodDeclaration createConstructorStub(
      ICompilationUnit unit,
      ASTRewrite rewrite,
      ImportRewrite imports,
      ImportRewriteContext context,
      ITypeBinding typeBinding,
      IMethodBinding superConstructor,
      IVariableBinding[] variableBindings,
      int modifiers,
      CodeGenerationSettings settings)
      throws CoreException {
    AST ast = rewrite.getAST();

    MethodDeclaration decl = ast.newMethodDeclaration();
    decl.modifiers()
        .addAll(
            ASTNodeFactory.newModifiers(ast, modifiers & ~Modifier.ABSTRACT & ~Modifier.NATIVE));
    decl.setName(ast.newSimpleName(typeBinding.getName()));
    decl.setConstructor(true);

    List<SingleVariableDeclaration> parameters = decl.parameters();
    if (superConstructor != null) {
      createTypeParameters(imports, context, ast, superConstructor, decl);

      createParameters(unit.getJavaProject(), imports, context, ast, superConstructor, null, decl);

      createThrownExceptions(decl, superConstructor, imports, context, ast);
    }

    Block body = ast.newBlock();
    decl.setBody(body);

    String delimiter = StubUtility.getLineDelimiterUsed(unit);

    if (superConstructor != null) {
      SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();
      SingleVariableDeclaration varDecl = null;
      for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator();
          iterator.hasNext(); ) {
        varDecl = iterator.next();
        invocation.arguments().add(ast.newSimpleName(varDecl.getName().getIdentifier()));
      }
      body.statements().add(invocation);
    }

    List<String> prohibited = new ArrayList<String>();
    for (final Iterator<SingleVariableDeclaration> iterator = parameters.iterator();
        iterator.hasNext(); ) prohibited.add(iterator.next().getName().getIdentifier());
    String param = null;
    List<String> list = new ArrayList<String>(prohibited);
    String[] excluded = null;
    for (int i = 0; i < variableBindings.length; i++) {
      SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
      var.setType(imports.addImport(variableBindings[i].getType(), ast, context));
      excluded = new String[list.size()];
      list.toArray(excluded);
      param = suggestParameterName(unit, variableBindings[i], excluded);
      list.add(param);
      var.setName(ast.newSimpleName(param));
      parameters.add(var);
    }

    list = new ArrayList<String>(prohibited);
    for (int i = 0; i < variableBindings.length; i++) {
      excluded = new String[list.size()];
      list.toArray(excluded);
      final String paramName = suggestParameterName(unit, variableBindings[i], excluded);
      list.add(paramName);
      final String fieldName = variableBindings[i].getName();
      Expression expression = null;
      if (paramName.equals(fieldName) || settings.useKeywordThis) {
        FieldAccess access = ast.newFieldAccess();
        access.setExpression(ast.newThisExpression());
        access.setName(ast.newSimpleName(fieldName));
        expression = access;
      } else expression = ast.newSimpleName(fieldName);
      Assignment assignment = ast.newAssignment();
      assignment.setLeftHandSide(expression);
      assignment.setRightHandSide(ast.newSimpleName(paramName));
      assignment.setOperator(Assignment.Operator.ASSIGN);
      body.statements().add(ast.newExpressionStatement(assignment));
    }

    if (settings != null && settings.createComments) {
      String string =
          CodeGeneration.getMethodComment(
              unit, typeBinding.getName(), decl, superConstructor, delimiter);
      if (string != null) {
        Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
        decl.setJavadoc(javadoc);
      }
    }
    return decl;
  }

  public static MethodDeclaration createDelegationStub(
      ICompilationUnit unit,
      ASTRewrite rewrite,
      ImportRewrite imports,
      ImportRewriteContext context,
      IMethodBinding delegate,
      IVariableBinding delegatingField,
      CodeGenerationSettings settings)
      throws CoreException {
    Assert.isNotNull(delegate);
    Assert.isNotNull(delegatingField);
    Assert.isNotNull(settings);

    AST ast = rewrite.getAST();

    MethodDeclaration decl = ast.newMethodDeclaration();
    decl.modifiers()
        .addAll(
            ASTNodeFactory.newModifiers(
                ast,
                delegate.getModifiers()
                    & ~Modifier.SYNCHRONIZED
                    & ~Modifier.ABSTRACT
                    & ~Modifier.NATIVE));

    decl.setName(ast.newSimpleName(delegate.getName()));
    decl.setConstructor(false);

    createTypeParameters(imports, context, ast, delegate, decl);

    decl.setReturnType2(imports.addImport(delegate.getReturnType(), ast, context));

    List<SingleVariableDeclaration> params =
        createParameters(unit.getJavaProject(), imports, context, ast, delegate, null, decl);

    createThrownExceptions(decl, delegate, imports, context, ast);

    Block body = ast.newBlock();
    decl.setBody(body);

    String delimiter = StubUtility.getLineDelimiterUsed(unit);

    Statement statement = null;
    MethodInvocation invocation = ast.newMethodInvocation();
    invocation.setName(ast.newSimpleName(delegate.getName()));
    List<Expression> arguments = invocation.arguments();
    for (int i = 0; i < params.size(); i++)
      arguments.add(ast.newSimpleName(params.get(i).getName().getIdentifier()));
    if (settings.useKeywordThis) {
      FieldAccess access = ast.newFieldAccess();
      access.setExpression(ast.newThisExpression());
      access.setName(ast.newSimpleName(delegatingField.getName()));
      invocation.setExpression(access);
    } else invocation.setExpression(ast.newSimpleName(delegatingField.getName()));
    if (delegate.getReturnType().isPrimitive()
        && delegate.getReturnType().getName().equals("void")) { // $NON-NLS-1$
      statement = ast.newExpressionStatement(invocation);
    } else {
      ReturnStatement returnStatement = ast.newReturnStatement();
      returnStatement.setExpression(invocation);
      statement = returnStatement;
    }
    body.statements().add(statement);

    ITypeBinding declaringType = delegatingField.getDeclaringClass();
    if (declaringType == null) { // can be null for
      return decl;
    }

    String qualifiedName = declaringType.getQualifiedName();
    IPackageBinding packageBinding = declaringType.getPackage();
    if (packageBinding != null) {
      if (packageBinding.getName().length() > 0
          && qualifiedName.startsWith(packageBinding.getName()))
        qualifiedName = qualifiedName.substring(packageBinding.getName().length());
    }

    if (settings.createComments) {
      /*
       * TODO: have API for delegate method comments This is an inlined
       * version of
       * {@link CodeGeneration#getMethodComment(ICompilationUnit, String, MethodDeclaration, IMethodBinding, String)}
       */
      delegate = delegate.getMethodDeclaration();
      String declaringClassQualifiedName = delegate.getDeclaringClass().getQualifiedName();
      String linkToMethodName = delegate.getName();
      String[] parameterTypesQualifiedNames = StubUtility.getParameterTypeNamesForSeeTag(delegate);
      String string =
          StubUtility.getMethodComment(
              unit,
              qualifiedName,
              decl,
              delegate.isDeprecated(),
              linkToMethodName,
              declaringClassQualifiedName,
              parameterTypesQualifiedNames,
              true,
              delimiter);
      if (string != null) {
        Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
        decl.setJavadoc(javadoc);
      }
    }
    return decl;
  }

  public static MethodDeclaration createImplementationStub(
      ICompilationUnit unit,
      ASTRewrite rewrite,
      ImportRewrite imports,
      ImportRewriteContext context,
      IMethodBinding binding,
      String type,
      CodeGenerationSettings settings,
      boolean inInterface)
      throws CoreException {
    return createImplementationStub(
        unit, rewrite, imports, context, binding, null, type, settings, inInterface);
  }

  public static MethodDeclaration createImplementationStub(
      ICompilationUnit unit,
      ASTRewrite rewrite,
      ImportRewrite imports,
      ImportRewriteContext context,
      IMethodBinding binding,
      String[] parameterNames,
      String type,
      CodeGenerationSettings settings,
      boolean inInterface)
      throws CoreException {
    Assert.isNotNull(imports);
    Assert.isNotNull(rewrite);

    AST ast = rewrite.getAST();

    MethodDeclaration decl = ast.newMethodDeclaration();
    decl.modifiers()
        .addAll(getImplementationModifiers(ast, binding, inInterface, imports, context));

    decl.setName(ast.newSimpleName(binding.getName()));
    decl.setConstructor(false);

    ITypeBinding bindingReturnType = binding.getReturnType();
    if (bindingReturnType.isWildcardType()) {
      ITypeBinding bound = bindingReturnType.getBound();
      bindingReturnType = (bound != null) ? bound : bindingReturnType.getErasure();
    }

    IJavaProject javaProject = unit.getJavaProject();
    if (JavaModelUtil.is50OrHigher(javaProject)) {
      createTypeParameters(imports, context, ast, binding, decl);

    } else {
      bindingReturnType = bindingReturnType.getErasure();
    }

    decl.setReturnType2(imports.addImport(bindingReturnType, ast, context));

    List<SingleVariableDeclaration> parameters =
        createParameters(javaProject, imports, context, ast, binding, parameterNames, decl);

    createThrownExceptions(decl, binding, imports, context, ast);

    String delimiter = unit.findRecommendedLineSeparator();
    int modifiers = binding.getModifiers();
    if (!(inInterface && Modifier.isAbstract(modifiers))) {
      // generate a method body

      Map<String, String> options = javaProject.getOptions(true);

      Block body = ast.newBlock();
      decl.setBody(body);

      String bodyStatement = ""; // $NON-NLS-1$
      if (Modifier.isAbstract(modifiers)) {
        Expression expression =
            ASTNodeFactory.newDefaultExpression(
                ast, decl.getReturnType2(), decl.getExtraDimensions());
        if (expression != null) {
          ReturnStatement returnStatement = ast.newReturnStatement();
          returnStatement.setExpression(expression);
          bodyStatement = ASTNodes.asFormattedString(returnStatement, 0, delimiter, options);
        }
      } else {
        SuperMethodInvocation invocation = ast.newSuperMethodInvocation();
        ITypeBinding declaringType = binding.getDeclaringClass();
        if (declaringType.isInterface()) {
          String qualifier = imports.addImport(declaringType.getErasure(), context);
          Name name = ASTNodeFactory.newName(ast, qualifier);
          invocation.setQualifier(name);
        }
        invocation.setName(ast.newSimpleName(binding.getName()));
        SingleVariableDeclaration varDecl = null;
        for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator();
            iterator.hasNext(); ) {
          varDecl = iterator.next();
          invocation.arguments().add(ast.newSimpleName(varDecl.getName().getIdentifier()));
        }
        Expression expression = invocation;
        Type returnType = decl.getReturnType2();
        if (returnType instanceof PrimitiveType
            && ((PrimitiveType) returnType).getPrimitiveTypeCode().equals(PrimitiveType.VOID)) {
          bodyStatement =
              ASTNodes.asFormattedString(
                  ast.newExpressionStatement(expression), 0, delimiter, options);
        } else {
          ReturnStatement returnStatement = ast.newReturnStatement();
          returnStatement.setExpression(expression);
          bodyStatement = ASTNodes.asFormattedString(returnStatement, 0, delimiter, options);
        }
      }

      String placeHolder =
          CodeGeneration.getMethodBodyContent(
              unit, type, binding.getName(), false, bodyStatement, delimiter);
      if (placeHolder != null) {
        ReturnStatement todoNode =
            (ReturnStatement)
                rewrite.createStringPlaceholder(placeHolder, ASTNode.RETURN_STATEMENT);
        body.statements().add(todoNode);
      }
    }

    if (settings != null && settings.createComments) {
      String string = CodeGeneration.getMethodComment(unit, type, decl, binding, delimiter);
      if (string != null) {
        Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
        decl.setJavadoc(javadoc);
      }
    }
    if (settings != null
        && settings.overrideAnnotation
        && JavaModelUtil.is50OrHigher(javaProject)) {
      addOverrideAnnotation(javaProject, rewrite, decl, binding);
    }

    return decl;
  }

  private static void createTypeParameters(
      ImportRewrite imports,
      ImportRewriteContext context,
      AST ast,
      IMethodBinding binding,
      MethodDeclaration decl) {
    ITypeBinding[] typeParams = binding.getTypeParameters();
    List<TypeParameter> typeParameters = decl.typeParameters();
    for (int i = 0; i < typeParams.length; i++) {
      ITypeBinding curr = typeParams[i];
      TypeParameter newTypeParam = ast.newTypeParameter();
      newTypeParam.setName(ast.newSimpleName(curr.getName()));
      ITypeBinding[] typeBounds = curr.getTypeBounds();
      if (typeBounds.length != 1
          || !"java.lang.Object".equals(typeBounds[0].getQualifiedName())) { // $NON-NLS-1$
        List<Type> newTypeBounds = newTypeParam.typeBounds();
        for (int k = 0; k < typeBounds.length; k++) {
          newTypeBounds.add(imports.addImport(typeBounds[k], ast, context));
        }
      }
      typeParameters.add(newTypeParam);
    }
  }

  private static List<SingleVariableDeclaration> createParameters(
      IJavaProject project,
      ImportRewrite imports,
      ImportRewriteContext context,
      AST ast,
      IMethodBinding binding,
      String[] paramNames,
      MethodDeclaration decl) {
    boolean is50OrHigher = JavaModelUtil.is50OrHigher(project);
    List<SingleVariableDeclaration> parameters = decl.parameters();
    ITypeBinding[] params = binding.getParameterTypes();
    if (paramNames == null || paramNames.length < params.length) {
      paramNames = StubUtility.suggestArgumentNames(project, binding);
    }
    for (int i = 0; i < params.length; i++) {
      SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
      ITypeBinding type = params[i];
      if (type.isWildcardType()) {
        ITypeBinding bound = type.getBound();
        type = (bound != null) ? bound : type.getErasure();
      }
      if (!is50OrHigher) {
        type = type.getErasure();
        var.setType(imports.addImport(type, ast, context));
      } else if (binding.isVarargs() && type.isArray() && i == params.length - 1) {
        var.setVarargs(true);
        /*
         * Varargs annotations are special.
         * Example:
         *     foo(@O Object @A [] @B ... arg)
         * => @B is not an annotation on the array dimension that constitutes the vararg.
         * It's the type annotation of the *innermost* array dimension.
         */
        int dimensions = type.getDimensions();
        @SuppressWarnings("unchecked")
        List<Annotation>[] dimensionAnnotations = (List<Annotation>[]) new List<?>[dimensions];
        for (int dim = 0; dim < dimensions; dim++) {
          dimensionAnnotations[dim] = new ArrayList<Annotation>();
          for (IAnnotationBinding annotation : type.getTypeAnnotations()) {
            dimensionAnnotations[dim].add(imports.addAnnotation(annotation, ast, context));
          }
          type = type.getComponentType();
        }

        Type elementType = imports.addImport(type, ast, context);
        if (dimensions == 1) {
          var.setType(elementType);
        } else {
          ArrayType arrayType = ast.newArrayType(elementType, dimensions - 1);
          List<Dimension> dimensionNodes = arrayType.dimensions();
          for (int dim = 0; dim < dimensions - 1; dim++) { // all except the innermost dimension
            Dimension dimension = dimensionNodes.get(dim);
            dimension.annotations().addAll(dimensionAnnotations[dim]);
          }
          var.setType(arrayType);
        }
        List<Annotation> varargTypeAnnotations = dimensionAnnotations[dimensions - 1];
        var.varargsAnnotations().addAll(varargTypeAnnotations);
      } else {
        var.setType(imports.addImport(type, ast, context));
      }
      var.setName(ast.newSimpleName(paramNames[i]));
      IAnnotationBinding[] annotations = binding.getParameterAnnotations(i);
      for (IAnnotationBinding annotation : annotations) {
        if (StubUtility2.isCopyOnInheritAnnotation(annotation.getAnnotationType(), project))
          var.modifiers().add(imports.addAnnotation(annotation, ast, context));
      }
      parameters.add(var);
    }
    return parameters;
  }

  private static void createThrownExceptions(
      MethodDeclaration decl,
      IMethodBinding method,
      ImportRewrite imports,
      ImportRewriteContext context,
      AST ast) {
    ITypeBinding[] excTypes = method.getExceptionTypes();
    if (ast.apiLevel() >= AST.JLS8) {
      List<Type> thrownExceptions = decl.thrownExceptionTypes();
      for (int i = 0; i < excTypes.length; i++) {
        Type excType = imports.addImport(excTypes[i], ast, context);
        thrownExceptions.add(excType);
      }
    } else {
      List<Name> thrownExceptions = getThrownExceptions(decl);
      for (int i = 0; i < excTypes.length; i++) {
        String excTypeName = imports.addImport(excTypes[i], context);
        thrownExceptions.add(ASTNodeFactory.newName(ast, excTypeName));
      }
    }
  }

  /**
   * @param decl method declaration
   * @return thrown exception names
   * @deprecated to avoid deprecation warnings
   */
  private static List<Name> getThrownExceptions(MethodDeclaration decl) {
    return decl.thrownExceptions();
  }

  private static IMethodBinding findMethodBinding(
      IMethodBinding method, List<IMethodBinding> allMethods) {
    for (int i = 0; i < allMethods.size(); i++) {
      IMethodBinding curr = allMethods.get(i);
      if (Bindings.isSubsignature(method, curr)) {
        return curr;
      }
    }
    return null;
  }

  private static IMethodBinding findOverridingMethod(
      IMethodBinding method, List<IMethodBinding> allMethods) {
    for (int i = 0; i < allMethods.size(); i++) {
      IMethodBinding curr = allMethods.get(i);
      if (Bindings.areOverriddenMethods(curr, method) || Bindings.isSubsignature(curr, method))
        return curr;
    }
    return null;
  }

  private static void findUnimplementedInterfaceMethods(
      ITypeBinding typeBinding,
      HashSet<ITypeBinding> visited,
      ArrayList<IMethodBinding> allMethods,
      IPackageBinding currPack,
      ArrayList<IMethodBinding> toImplement) {

    if (visited.add(typeBinding)) {
      IMethodBinding[] typeMethods = typeBinding.getDeclaredMethods();

      nextMethod:
      for (int i = 0; i < typeMethods.length; i++) {
        IMethodBinding curr = typeMethods[i];
        for (Iterator<IMethodBinding> allIter = allMethods.iterator(); allIter.hasNext(); ) {
          IMethodBinding oneMethod = allIter.next();
          if (Bindings.isSubsignature(oneMethod, curr)) {
            // We've already seen a method that is a subsignature of curr.
            if (!Bindings.isSubsignature(curr, oneMethod)) {
              // oneMethod is a true subsignature of curr; let's go with oneMethod
              continue nextMethod;
            }
            // Subsignatures are equivalent.
            // Check visibility and return types ('getErasure()' tries to achieve effect of "rename
            // type variables")
            if (Bindings.isVisibleInHierarchy(oneMethod, currPack)
                && oneMethod
                    .getReturnType()
                    .getErasure()
                    .isSubTypeCompatible(curr.getReturnType().getErasure())) {
              // oneMethod is visible and curr doesn't have a stricter return type; let's go with
              // oneMethod
              continue nextMethod;
            }
            // curr is stricter than oneMethod, so let's remove oneMethod
            allIter.remove();
            toImplement.remove(oneMethod);
          } else if (Bindings.isSubsignature(curr, oneMethod)) {
            // curr is a true subsignature of oneMethod. Let's remove oneMethod.
            allIter.remove();
            toImplement.remove(oneMethod);
          }
        }
        if (Modifier.isAbstract(curr.getModifiers())) {
          toImplement.add(curr);
          allMethods.add(curr);
        }
      }
      ITypeBinding[] superInterfaces = typeBinding.getInterfaces();
      for (int i = 0; i < superInterfaces.length; i++)
        findUnimplementedInterfaceMethods(
            superInterfaces[i], visited, allMethods, currPack, toImplement);
    }
  }

  //	public static DelegateEntry[] getDelegatableMethods(ITypeBinding binding) {
  //		final List<DelegateEntry> tuples= new ArrayList<DelegateEntry>();
  //		final List<IMethodBinding> declared= new ArrayList<IMethodBinding>();
  //		IMethodBinding[] typeMethods= binding.getDeclaredMethods();
  //		for (int index= 0; index < typeMethods.length; index++)
  //			declared.add(typeMethods[index]);
  //		IVariableBinding[] typeFields= binding.getDeclaredFields();
  //		for (int index= 0; index < typeFields.length; index++) {
  //			IVariableBinding fieldBinding= typeFields[index];
  //			if (fieldBinding.isField() && !fieldBinding.isEnumConstant() && !fieldBinding.isSynthetic())
  //				getDelegatableMethods(new ArrayList<IMethodBinding>(declared), fieldBinding,
  // fieldBinding.getType(), binding, tuples);
  //		}
  //		// list of tuple<IVariableBinding, IMethodBinding>
  //		return tuples.toArray(new DelegateEntry[tuples.size()]);
  //	}
  //
  //	private static void getDelegatableMethods(List<IMethodBinding> methods, IVariableBinding
  // fieldBinding, ITypeBinding typeBinding, ITypeBinding binding, List<DelegateEntry> result) {
  //		boolean match= false;
  //		if (typeBinding.isTypeVariable()) {
  //			ITypeBinding[] typeBounds= typeBinding.getTypeBounds();
  //			if (typeBounds.length > 0) {
  //				for (int i= 0; i < typeBounds.length; i++) {
  //					getDelegatableMethods(methods, fieldBinding, typeBounds[i], binding, result);
  //				}
  //			} else {
  //				ITypeBinding objectBinding= Bindings.findTypeInHierarchy(binding, "java.lang.Object");
  // //$NON-NLS-1$
  //				if (objectBinding != null) {
  //					getDelegatableMethods(methods, fieldBinding, objectBinding, binding, result);
  //				}
  //			}
  //		} else {
  //			IMethodBinding[] candidates= getDelegateCandidates(typeBinding, binding);
  //			for (int index= 0; index < candidates.length; index++) {
  //				match= false;
  //				final IMethodBinding methodBinding= candidates[index];
  //				for (int offset= 0; offset < methods.size() && !match; offset++) {
  //					if (Bindings.areOverriddenMethods(methods.get(offset), methodBinding))
  //						match= true;
  //				}
  //				if (!match) {
  //					result.add(new DelegateEntry(methodBinding, fieldBinding));
  //					methods.add(methodBinding);
  //				}
  //			}
  //			final ITypeBinding superclass= typeBinding.getSuperclass();
  //			if (superclass != null)
  //				getDelegatableMethods(methods, fieldBinding, superclass, binding, result);
  //			ITypeBinding[] superInterfaces= typeBinding.getInterfaces();
  //			for (int offset= 0; offset < superInterfaces.length; offset++)
  //				getDelegatableMethods(methods, fieldBinding, superInterfaces[offset], binding, result);
  //		}
  //	}

  private static IMethodBinding[] getDelegateCandidates(
      ITypeBinding binding, ITypeBinding hierarchy) {
    List<IMethodBinding> allMethods = new ArrayList<IMethodBinding>();
    boolean isInterface = binding.isInterface();
    IMethodBinding[] typeMethods = binding.getDeclaredMethods();
    for (int index = 0; index < typeMethods.length; index++) {
      final int modifiers = typeMethods[index].getModifiers();
      if (!typeMethods[index].isConstructor()
          && !Modifier.isStatic(modifiers)
          && (isInterface || Modifier.isPublic(modifiers))) {
        IMethodBinding result =
            Bindings.findOverriddenMethodInHierarchy(hierarchy, typeMethods[index]);
        if (result != null && Flags.isFinal(result.getModifiers())) continue;
        ITypeBinding[] parameterBindings = typeMethods[index].getParameterTypes();
        boolean upper = false;
        for (int offset = 0; offset < parameterBindings.length; offset++) {
          if (parameterBindings[offset].isWildcardType()
              && parameterBindings[offset].isUpperbound()) upper = true;
        }
        if (!upper) allMethods.add(typeMethods[index]);
      }
    }
    return allMethods.toArray(new IMethodBinding[allMethods.size()]);
  }

  private static List<IExtendedModifier> getImplementationModifiers(
      AST ast,
      IMethodBinding method,
      boolean inInterface,
      ImportRewrite importRewrite,
      ImportRewriteContext context)
      throws JavaModelException {
    IJavaProject javaProject = importRewrite.getCompilationUnit().getJavaProject();
    int modifiers =
        method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.PRIVATE;
    if (inInterface) {
      modifiers = modifiers & ~Modifier.PROTECTED;
      if (!method.getDeclaringClass().isInterface()) {
        modifiers = modifiers | Modifier.PUBLIC;
      }
    } else {
      modifiers = modifiers & ~Modifier.DEFAULT;
    }
    IAnnotationBinding[] annotations = method.getAnnotations();

    if (modifiers != Modifier.NONE && annotations.length > 0) {
      // need an AST of the source method to preserve order of modifiers
      IMethod iMethod = (IMethod) method.getJavaElement();
      if (iMethod != null && JavaElementUtil.isSourceAvailable(iMethod)) {
        CheASTParser parser = CheASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
        parser.setSource(iMethod.getTypeRoot());
        parser.setIgnoreMethodBodies(true);
        CompilationUnit otherCU = (CompilationUnit) parser.createAST(null);
        ASTNode otherMethod = NodeFinder.perform(otherCU, iMethod.getSourceRange());
        if (otherMethod instanceof MethodDeclaration) {
          MethodDeclaration otherMD = (MethodDeclaration) otherMethod;
          ArrayList<IExtendedModifier> result = new ArrayList<IExtendedModifier>();
          List<IExtendedModifier> otherModifiers = otherMD.modifiers();
          for (IExtendedModifier otherModifier : otherModifiers) {
            if (otherModifier instanceof Modifier) {
              int otherFlag = ((Modifier) otherModifier).getKeyword().toFlagValue();
              if ((otherFlag & modifiers) != 0) {
                modifiers = ~otherFlag & modifiers;
                result.addAll(ast.newModifiers(otherFlag));
              }
            } else {
              Annotation otherAnnotation = (Annotation) otherModifier;
              String n = otherAnnotation.getTypeName().getFullyQualifiedName();
              for (IAnnotationBinding annotation : annotations) {
                ITypeBinding otherAnnotationType = annotation.getAnnotationType();
                String qn = otherAnnotationType.getQualifiedName();
                if (qn.endsWith(n)
                    && (qn.length() == n.length()
                        || qn.charAt(qn.length() - n.length() - 1) == '.')) {
                  if (StubUtility2.isCopyOnInheritAnnotation(otherAnnotationType, javaProject))
                    result.add(importRewrite.addAnnotation(annotation, ast, context));
                  break;
                }
              }
            }
          }
          result.addAll(ASTNodeFactory.newModifiers(ast, modifiers));
          return result;
        }
      }
    }

    ArrayList<IExtendedModifier> result = new ArrayList<IExtendedModifier>();

    for (IAnnotationBinding annotation : annotations) {
      if (StubUtility2.isCopyOnInheritAnnotation(annotation.getAnnotationType(), javaProject))
        result.add(importRewrite.addAnnotation(annotation, ast, context));
    }

    result.addAll(ASTNodeFactory.newModifiers(ast, modifiers));

    return result;
  }

  public static IMethodBinding[] getOverridableMethods(
      AST ast, ITypeBinding typeBinding, boolean isSubType) {
    List<IMethodBinding> allMethods = new ArrayList<IMethodBinding>();
    IMethodBinding[] typeMethods = typeBinding.getDeclaredMethods();
    for (int index = 0; index < typeMethods.length; index++) {
      final int modifiers = typeMethods[index].getModifiers();
      if (!typeMethods[index].isConstructor()
          && !Modifier.isStatic(modifiers)
          && !Modifier.isPrivate(modifiers)) allMethods.add(typeMethods[index]);
    }
    ITypeBinding clazz = typeBinding.getSuperclass();
    while (clazz != null) {
      IMethodBinding[] methods = clazz.getDeclaredMethods();
      for (int offset = 0; offset < methods.length; offset++) {
        final int modifiers = methods[offset].getModifiers();
        if (!methods[offset].isConstructor()
            && !Modifier.isStatic(modifiers)
            && !Modifier.isPrivate(modifiers)) {
          if (findOverridingMethod(methods[offset], allMethods) == null)
            allMethods.add(methods[offset]);
        }
      }
      clazz = clazz.getSuperclass();
    }
    clazz = typeBinding;
    while (clazz != null) {
      ITypeBinding[] superInterfaces = clazz.getInterfaces();
      for (int index = 0; index < superInterfaces.length; index++) {
        getOverridableMethods(ast, superInterfaces[index], allMethods);
      }
      clazz = clazz.getSuperclass();
    }
    if (typeBinding.isInterface())
      getOverridableMethods(
          ast, ast.resolveWellKnownType("java.lang.Object"), allMethods); // $NON-NLS-1$
    if (!isSubType) allMethods.removeAll(Arrays.asList(typeMethods));
    int modifiers = 0;
    if (!typeBinding.isInterface()) {
      for (int index = allMethods.size() - 1; index >= 0; index--) {
        IMethodBinding method = allMethods.get(index);
        modifiers = method.getModifiers();
        if (Modifier.isFinal(modifiers)) allMethods.remove(index);
      }
    }
    return allMethods.toArray(new IMethodBinding[allMethods.size()]);
  }

  private static void getOverridableMethods(
      AST ast, ITypeBinding superBinding, List<IMethodBinding> allMethods) {
    IMethodBinding[] methods = superBinding.getDeclaredMethods();
    for (int offset = 0; offset < methods.length; offset++) {
      final int modifiers = methods[offset].getModifiers();
      if (!methods[offset].isConstructor()
          && !Modifier.isStatic(modifiers)
          && !Modifier.isPrivate(modifiers)) {
        if (findOverridingMethod(methods[offset], allMethods) == null
            && !Modifier.isStatic(modifiers)) allMethods.add(methods[offset]);
      }
    }
    ITypeBinding[] superInterfaces = superBinding.getInterfaces();
    for (int index = 0; index < superInterfaces.length; index++) {
      getOverridableMethods(ast, superInterfaces[index], allMethods);
    }
  }

  private static String suggestParameterName(
      ICompilationUnit unit, IVariableBinding binding, String[] excluded) {
    String name = StubUtility.getBaseName(binding, unit.getJavaProject());
    return StubUtility.suggestArgumentName(unit.getJavaProject(), name, excluded);
  }

  public static IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding) {
    return getUnimplementedMethods(typeBinding, false);
  }

  public static IMethodBinding[] getUnimplementedMethods(
      ITypeBinding typeBinding, boolean implementAbstractsOfInput) {
    ArrayList<IMethodBinding> allMethods = new ArrayList<IMethodBinding>();
    ArrayList<IMethodBinding> toImplement = new ArrayList<IMethodBinding>();

    IMethodBinding[] typeMethods = typeBinding.getDeclaredMethods();
    for (int i = 0; i < typeMethods.length; i++) {
      IMethodBinding curr = typeMethods[i];
      int modifiers = curr.getModifiers();
      if (!curr.isConstructor()
          && !Modifier.isStatic(modifiers)
          && !Modifier.isPrivate(modifiers)) {
        allMethods.add(curr);
      }
    }

    ITypeBinding superClass = typeBinding.getSuperclass();
    while (superClass != null) {
      typeMethods = superClass.getDeclaredMethods();
      for (int i = 0; i < typeMethods.length; i++) {
        IMethodBinding curr = typeMethods[i];
        int modifiers = curr.getModifiers();
        if (!curr.isConstructor()
            && !Modifier.isStatic(modifiers)
            && !Modifier.isPrivate(modifiers)) {
          if (findMethodBinding(curr, allMethods) == null) {
            allMethods.add(curr);
          }
        }
      }
      superClass = superClass.getSuperclass();
    }

    for (int i = 0; i < allMethods.size(); i++) {
      IMethodBinding curr = allMethods.get(i);
      int modifiers = curr.getModifiers();
      if ((Modifier.isAbstract(modifiers) || curr.getDeclaringClass().isInterface())
          && (implementAbstractsOfInput || typeBinding != curr.getDeclaringClass())) {
        // implement all abstract methods
        toImplement.add(curr);
      }
    }

    HashSet<ITypeBinding> visited = new HashSet<ITypeBinding>();
    ITypeBinding curr = typeBinding;
    while (curr != null) {
      ITypeBinding[] superInterfaces = curr.getInterfaces();
      for (int i = 0; i < superInterfaces.length; i++) {
        findUnimplementedInterfaceMethods(
            superInterfaces[i], visited, allMethods, typeBinding.getPackage(), toImplement);
      }
      curr = curr.getSuperclass();
    }

    return toImplement.toArray(new IMethodBinding[toImplement.size()]);
  }

  public static IMethodBinding[] getVisibleConstructors(
      ITypeBinding binding, boolean accountExisting, boolean proposeDefault) {
    List<IMethodBinding> constructorMethods = new ArrayList<IMethodBinding>();
    List<IMethodBinding> existingConstructors = null;
    ITypeBinding superType = binding.getSuperclass();
    if (superType == null) return new IMethodBinding[0];
    if (accountExisting) {
      IMethodBinding[] methods = binding.getDeclaredMethods();
      existingConstructors = new ArrayList<IMethodBinding>(methods.length);
      for (int index = 0; index < methods.length; index++) {
        IMethodBinding method = methods[index];
        if (method.isConstructor() && !method.isDefaultConstructor())
          existingConstructors.add(method);
      }
    }
    if (existingConstructors != null) constructorMethods.addAll(existingConstructors);
    IMethodBinding[] methods = binding.getDeclaredMethods();
    IMethodBinding[] superMethods = superType.getDeclaredMethods();
    for (int index = 0; index < superMethods.length; index++) {
      IMethodBinding method = superMethods[index];
      if (method.isConstructor()) {
        if (Bindings.isVisibleInHierarchy(method, binding.getPackage())
            && (!accountExisting
                || !Bindings.containsSignatureEquivalentConstructor(methods, method)))
          constructorMethods.add(method);
      }
    }
    if (existingConstructors != null) constructorMethods.removeAll(existingConstructors);
    if (constructorMethods.isEmpty()) {
      superType = binding;
      while (superType.getSuperclass() != null) superType = superType.getSuperclass();
      IMethodBinding method =
          Bindings.findMethodInType(superType, "Object", new ITypeBinding[0]); // $NON-NLS-1$
      if (method != null) {
        if ((proposeDefault
                || !accountExisting
                || existingConstructors == null
                || existingConstructors.isEmpty())
            && (!accountExisting
                || !Bindings.containsSignatureEquivalentConstructor(methods, method)))
          constructorMethods.add(method);
      }
    }
    return constructorMethods.toArray(new IMethodBinding[constructorMethods.size()]);
  }

  /**
   * Evaluates the insertion position of a new node.
   *
   * @param listRewrite The list rewriter to which the new node will be added
   * @param sibling The Java element before which the new element should be added.
   * @return the AST node of the list to insert before or null to insert as last.
   * @throws JavaModelException thrown if accessing the Java element failed
   */
  public static ASTNode getNodeToInsertBefore(ListRewrite listRewrite, IJavaElement sibling)
      throws JavaModelException {
    if (sibling instanceof IMember) {
      ISourceRange sourceRange = ((IMember) sibling).getSourceRange();
      if (sourceRange == null) {
        return null;
      }
      int insertPos = sourceRange.getOffset();

      List<? extends ASTNode> members = listRewrite.getOriginalList();
      for (int i = 0; i < members.size(); i++) {
        ASTNode curr = members.get(i);
        if (curr.getStartPosition() >= insertPos) {
          return curr;
        }
      }
    }
    return null;
  }

  /** Creates a new stub utility. */
  private StubUtility2() {
    // Not for instantiation
  }

  public static boolean isCopyOnInheritAnnotation(
      ITypeBinding annotationType, IJavaProject project) {
    if (JavaCore.ENABLED.equals(
        project.getOption(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, true))) return false;

    return Bindings.isNullAnnotation(annotationType, project);
  }
}
