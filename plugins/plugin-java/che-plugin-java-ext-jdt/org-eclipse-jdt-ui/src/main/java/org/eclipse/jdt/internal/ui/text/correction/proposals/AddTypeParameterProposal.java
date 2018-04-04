/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.JavadocTagsSubProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;

public class AddTypeParameterProposal extends LinkedCorrectionProposal {

  private IBinding fBinding;
  private CompilationUnit fAstRoot;

  private final String fTypeParamName;
  private final ITypeBinding[] fBounds;

  public AddTypeParameterProposal(
      ICompilationUnit targetCU,
      IBinding binding,
      CompilationUnit astRoot,
      String name,
      ITypeBinding[] bounds,
      int relevance) {
    super(
        "",
        targetCU,
        null,
        relevance,
        JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC)); // $NON-NLS-1$

    Assert.isTrue(binding != null && Bindings.isDeclarationBinding(binding));
    Assert.isTrue(binding instanceof IMethodBinding || binding instanceof ITypeBinding);

    fBinding = binding;
    fAstRoot = astRoot;
    fTypeParamName = name;
    fBounds = bounds;

    if (binding instanceof IMethodBinding) {
      String[] args = {
        BasicElementLabels.getJavaElementName(fTypeParamName),
        ASTResolving.getMethodSignature((IMethodBinding) binding)
      };
      setDisplayName(
          Messages.format(CorrectionMessages.AddTypeParameterProposal_method_label, args));
    } else {
      String[] args = {
        BasicElementLabels.getJavaElementName(fTypeParamName),
        ASTResolving.getTypeSignature((ITypeBinding) binding)
      };
      setDisplayName(Messages.format(CorrectionMessages.AddTypeParameterProposal_type_label, args));
    }
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    ASTNode boundNode = fAstRoot.findDeclaringNode(fBinding);
    ASTNode declNode = null;

    if (boundNode != null) {
      declNode = boundNode; // is same CU
      createImportRewrite(fAstRoot);
    } else {
      CompilationUnit newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
      declNode = newRoot.findDeclaringNode(fBinding.getKey());
      createImportRewrite(newRoot);
    }
    AST ast = declNode.getAST();
    TypeParameter newTypeParam = ast.newTypeParameter();
    newTypeParam.setName(ast.newSimpleName(fTypeParamName));
    if (fBounds != null && fBounds.length > 0) {
      List<Type> typeBounds = newTypeParam.typeBounds();
      ImportRewriteContext importRewriteContext =
          new ContextSensitiveImportRewriteContext(declNode, getImportRewrite());
      for (int i = 0; i < fBounds.length; i++) {
        Type newBound = getImportRewrite().addImport(fBounds[i], ast, importRewriteContext);
        typeBounds.add(newBound);
      }
    }
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ListRewrite listRewrite;
    Javadoc javadoc;
    List<TypeParameter> otherTypeParams;
    if (declNode instanceof TypeDeclaration) {
      TypeDeclaration declaration = (TypeDeclaration) declNode;
      listRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
      otherTypeParams = declaration.typeParameters();
      javadoc = declaration.getJavadoc();
    } else {
      MethodDeclaration declaration = (MethodDeclaration) declNode;
      listRewrite = rewrite.getListRewrite(declNode, MethodDeclaration.TYPE_PARAMETERS_PROPERTY);
      otherTypeParams = declaration.typeParameters();
      javadoc = declaration.getJavadoc();
    }
    listRewrite.insertLast(newTypeParam, null);

    if (javadoc != null && otherTypeParams != null) {
      ListRewrite tagsRewriter = rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY);
      Set<String> previousNames =
          JavadocTagsSubProcessor.getPreviousTypeParamNames(otherTypeParams, null);

      String name = '<' + fTypeParamName + '>';
      TagElement newTag = ast.newTagElement();
      newTag.setTagName(TagElement.TAG_PARAM);
      TextElement text = ast.newTextElement();
      text.setText(name);
      newTag.fragments().add(text);

      JavadocTagsSubProcessor.insertTag(tagsRewriter, newTag, previousNames);
    }
    return rewrite;
  }
}
