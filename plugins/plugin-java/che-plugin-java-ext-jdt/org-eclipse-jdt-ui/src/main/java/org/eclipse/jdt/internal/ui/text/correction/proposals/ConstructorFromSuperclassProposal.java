/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.swt.graphics.Image;

public class ConstructorFromSuperclassProposal extends LinkedCorrectionProposal {

  private TypeDeclaration fTypeNode;
  private IMethodBinding fSuperConstructor;

  public ConstructorFromSuperclassProposal(
      ICompilationUnit cu,
      TypeDeclaration typeNode,
      IMethodBinding superConstructor,
      int relevance) {
    super("", cu, null, relevance, null); // $NON-NLS-1$
    fTypeNode = typeNode;
    fSuperConstructor = superConstructor;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
   */
  @Override
  public Image getImage() {
    //		return JavaPlugin.getImageDescriptorRegistry().get(
    //				new JavaElementImageDescriptor(JavaPluginImages.DESC_MISC_PUBLIC,
    // JavaElementImageDescriptor.CONSTRUCTOR,
    //											   JavaElementImageProvider.SMALL_SIZE)
    //														  );
    // TODO add icon for constructor
    return JavaPluginImages.get(JavaPluginImages.DESC_MISC_PUBLIC);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeCorrectionProposal#getName()
   */
  @Override
  public String getName() {
    StringBuffer buf = new StringBuffer();
    buf.append(fTypeNode.getName().getIdentifier());
    buf.append('(');
    if (fSuperConstructor != null) {
      ITypeBinding[] paramTypes = fSuperConstructor.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++) {
        if (i > 0) {
          buf.append(',');
        }
        buf.append(paramTypes[i].getName());
      }
    }
    buf.append(')');
    return Messages.format(
        CorrectionMessages.ConstructorFromSuperclassProposal_description,
        BasicElementLabels.getJavaElementName(buf.toString()));
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    AST ast = fTypeNode.getAST();

    ASTRewrite rewrite = ASTRewrite.create(ast);

    createImportRewrite((CompilationUnit) fTypeNode.getRoot());

    CodeGenerationSettings settings =
        JavaPreferencesSettings.getCodeGenerationSettings(getCompilationUnit().getJavaProject());
    if (!settings.createComments) {
      settings = null;
    }
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(fTypeNode, getImportRewrite());

    MethodDeclaration newMethodDecl =
        createNewMethodDeclaration(ast, fSuperConstructor, rewrite, importRewriteContext, settings);
    rewrite
        .getListRewrite(fTypeNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY)
        .insertFirst(newMethodDecl, null);

    addLinkedRanges(rewrite, newMethodDecl);

    return rewrite;
  }

  private void addLinkedRanges(ASTRewrite rewrite, MethodDeclaration newStub) {
    List<SingleVariableDeclaration> parameters = newStub.parameters();
    for (int i = 0; i < parameters.size(); i++) {
      SingleVariableDeclaration curr = parameters.get(i);
      String name = curr.getName().getIdentifier();
      addLinkedPosition(rewrite.track(curr.getType()), false, "arg_type_" + name); // $NON-NLS-1$
      addLinkedPosition(rewrite.track(curr.getName()), false, "arg_name_" + name); // $NON-NLS-1$
    }
  }

  private MethodDeclaration createNewMethodDeclaration(
      AST ast,
      IMethodBinding binding,
      ASTRewrite rewrite,
      ImportRewriteContext importRewriteContext,
      CodeGenerationSettings commentSettings)
      throws CoreException {
    String name = fTypeNode.getName().getIdentifier();
    MethodDeclaration decl = ast.newMethodDeclaration();
    decl.setConstructor(true);
    decl.setName(ast.newSimpleName(name));
    Block body = ast.newBlock();
    decl.setBody(body);

    SuperConstructorInvocation invocation = null;

    List<SingleVariableDeclaration> parameters = decl.parameters();
    String[] paramNames = getArgumentNames(binding);

    ITypeBinding enclosingInstance = getEnclosingInstance();
    if (enclosingInstance != null) {
      invocation =
          addEnclosingInstanceAccess(
              rewrite, importRewriteContext, parameters, paramNames, enclosingInstance);
    }

    if (binding == null) {
      decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    } else {
      decl.modifiers().addAll(ASTNodeFactory.newModifiers(ast, binding.getModifiers()));

      ITypeBinding[] params = binding.getParameterTypes();
      for (int i = 0; i < params.length; i++) {
        SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
        var.setType(getImportRewrite().addImport(params[i], ast, importRewriteContext));
        var.setName(ast.newSimpleName(paramNames[i]));
        parameters.add(var);
      }

      List<Type> thrownExceptions = decl.thrownExceptionTypes();
      ITypeBinding[] excTypes = binding.getExceptionTypes();
      for (int i = 0; i < excTypes.length; i++) {
        Type excType = getImportRewrite().addImport(excTypes[i], ast, importRewriteContext);
        thrownExceptions.add(excType);
      }

      if (invocation == null) {
        invocation = ast.newSuperConstructorInvocation();
      }

      List<Expression> arguments = invocation.arguments();
      for (int i = 0; i < paramNames.length; i++) {
        Name argument = ast.newSimpleName(paramNames[i]);
        arguments.add(argument);
        addLinkedPosition(
            rewrite.track(argument), false, "arg_name_" + paramNames[i]); // $NON-NLS-1$
      }
    }

    String bodyStatement =
        (invocation == null)
            ? ""
            : ASTNodes.asFormattedString(
                invocation,
                0,
                String.valueOf('\n'),
                getCompilationUnit().getJavaProject().getOptions(true));
    // $NON-NLS-1$
    String placeHolder =
        CodeGeneration.getMethodBodyContent(
            getCompilationUnit(), name, name, true, bodyStatement, String.valueOf('\n'));
    if (placeHolder != null) {
      ASTNode todoNode = rewrite.createStringPlaceholder(placeHolder, ASTNode.RETURN_STATEMENT);
      body.statements().add(todoNode);
    }
    if (commentSettings != null) {
      String string =
          CodeGeneration.getMethodComment(
              getCompilationUnit(), name, decl, null, String.valueOf('\n'));
      if (string != null) {
        Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
        decl.setJavadoc(javadoc);
      }
    }
    return decl;
  }

  private SuperConstructorInvocation addEnclosingInstanceAccess(
      ASTRewrite rewrite,
      ImportRewriteContext importRewriteContext,
      List<SingleVariableDeclaration> parameters,
      String[] paramNames,
      ITypeBinding enclosingInstance) {
    AST ast = rewrite.getAST();
    SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();

    SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
    var.setType(getImportRewrite().addImport(enclosingInstance, ast, importRewriteContext));
    String[] enclosingArgNames =
        StubUtility.getArgumentNameSuggestions(
            getCompilationUnit().getJavaProject(),
            enclosingInstance.getTypeDeclaration().getName(),
            0,
            paramNames);
    String firstName = enclosingArgNames[0];
    var.setName(ast.newSimpleName(firstName));
    parameters.add(var);

    Name enclosing = ast.newSimpleName(firstName);
    invocation.setExpression(enclosing);

    String key = "arg_name_" + firstName; // $NON-NLS-1$
    addLinkedPosition(rewrite.track(enclosing), false, key);
    for (int i = 0; i < enclosingArgNames.length; i++) {
      addLinkedPositionProposal(key, enclosingArgNames[i], null); // alternative names
    }
    return invocation;
  }

  private ITypeBinding getEnclosingInstance() {
    ITypeBinding currBinding = fTypeNode.resolveBinding();
    if (currBinding == null || Modifier.isStatic(currBinding.getModifiers())) {
      return null;
    }
    ITypeBinding superBinding = currBinding.getSuperclass();
    if (superBinding == null
        || superBinding.getDeclaringClass() == null
        || Modifier.isStatic(superBinding.getModifiers())) {
      return null;
    }
    ITypeBinding enclosing = superBinding.getDeclaringClass();

    while (currBinding != null) {
      if (Bindings.isSuperType(enclosing, currBinding)) {
        return null; // enclosing in scope
      }
      if (Modifier.isStatic(currBinding.getModifiers())) {
        return null; // no more enclosing instances
      }
      currBinding = currBinding.getDeclaringClass();
    }
    return enclosing;
  }

  private String[] getArgumentNames(IMethodBinding binding) {
    if (binding == null) {
      return new String[0];
    }
    return StubUtility.suggestArgumentNames(getCompilationUnit().getJavaProject(), binding);
  }
}
