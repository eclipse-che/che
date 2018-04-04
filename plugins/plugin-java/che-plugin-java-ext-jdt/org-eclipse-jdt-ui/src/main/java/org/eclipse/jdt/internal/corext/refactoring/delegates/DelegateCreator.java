/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.delegates;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.CategorizedTextEditGroup;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.text.edits.TextEdit;

/**
 * This class implements functionality for creating delegates of renamed or moved members pointing
 * to the original element and containing a deprecation message. The delegate is always created in
 * the source file of the original element and is either added directly after the original element
 * or replaces it. Use this class as follows:
 *
 * <pre>
 *     DelegateCreator myCreator= new DelegateMethodCreator();
 *     myCreator.setRewrite(... cu-rewrite of modified file...);
 *     myCreator.setDeclaration(... declaration of moved/renamed member ...);
 *
 *     ... additional initialization methods, see below...
 *
 *     myCreator.prepareDelegate();
 *
 *     ... do something with the delegate before the edit is created ...
 *
 *     myCreator.createEdit();
 * </pre>
 *
 * <p>Before prepareDelegate(), depending on whether the member is moved or renamed, or both, as
 * well as other concerns, the following methods may be called:
 *
 * <pre>
 *     myCreator.setNewLocation(... new location where the member was moved ...);
 *     myCreator.setNewName(... new name of the member if renamed ...);
 *     myCreator.setDeclareDeprecated(... false or true ...);
 *     myCreator.setCopy(... false or true...);
 * </pre>
 *
 * <p>Note that removing or adding imports related to delegate creation is generally the
 * responsibility of the caller. As an exception, the import for a target type set via
 * setNewLocation() will be added by this class.
 *
 * <p>This class is intended to be subclassed by specialized creators for concrete element types
 * like methods or fields.
 *
 * @since 3.2
 */
public abstract class DelegateCreator {

  public static final GroupCategorySet CATEGORY_DELEGATE =
      new GroupCategorySet(
          new GroupCategory(
              "org.eclipse.jdt.internal.corext.refactoring.delegates.delegate",
              RefactoringCoreMessages.DelegateCreator_change_category_title,
              RefactoringCoreMessages.DelegateCreator_change_category_description)); // $NON-NLS-1$

  /*
   * We are dealing with two CURewrites here:
   *
   * 1) The original rewrite which is passed in from the outside. One import may
   * 	  be registered with this CURewrite in case setNewLocation() is called.
   * 	  On createEdit(), the complete delegate code will be added to the CURewrite's
   * 	  ASTRewrite (add or replace).
   *
   * 2) A new CuRewrite from which we'll only use the ASTRewrite to build the new delegate.
   *
   */
  private CompilationUnitRewrite fOriginalRewrite;
  private CompilationUnitRewrite fDelegateRewrite;

  private boolean fIsMoveToAnotherFile;
  private boolean fCopy;
  private boolean fDeclareDeprecated;
  private boolean fInsertBefore;

  private BodyDeclaration fDeclaration;
  private String fNewElementName;
  private ITypeBinding fDestinationTypeBinding;
  private Type fDestinationType;
  private ITrackedNodePosition fTrackedPosition;

  private CodeGenerationSettings fPreferences;

  public DelegateCreator() {
    fCopy = true;
    fDeclareDeprecated = true;
    fInsertBefore = false;
  }

  /**
   * Sets the compilation unit rewrite of the declaration to create a delegate for. Must always be
   * called prior to prepareDelegate(). Bindings need not be resolved.
   *
   * @param rewrite the CompilationUnitRewrite.
   */
  public void setSourceRewrite(CompilationUnitRewrite rewrite) {
    fOriginalRewrite = rewrite;
    fPreferences =
        JavaPreferencesSettings.getCodeGenerationSettings(rewrite.getCu().getJavaProject());

    fDelegateRewrite = new CompilationUnitRewrite(rewrite.getCu(), rewrite.getRoot());
    fDelegateRewrite
        .getASTRewrite()
        .setTargetSourceRangeComputer(rewrite.getASTRewrite().getExtendedSourceRangeComputer());
  }

  /**
   * Sets the old member declaration. Must always be called prior to prepareDelegate().
   *
   * @param declaration the BodyDeclaration
   */
  public void setDeclaration(BodyDeclaration declaration) {
    fDeclaration = declaration;
  }

  /**
   * Set the name of the new element. This is optional, but if set it must be called prior to
   * prepareDelegate().
   *
   * @param newName the String with the new name
   */
  public void setNewElementName(String newName) {
    fNewElementName = newName;
  }

  /**
   * Set the location of the new element. This is optional, but if set it must be called prior to
   * prepareDelegate().
   *
   * @param binding the ITypeBinding of the old type
   */
  public void setNewLocation(ITypeBinding binding) {
    fDestinationTypeBinding = binding;
  }

  /**
   * Set whether the existing element should be copied and the copy made delegate (true), or the
   * original element should be changed to become the delegate (false). This is optional, but if set
   * it must be called prior to prepareDelegate().
   *
   * <p>The default is true (create a copy).
   *
   * @param isCopy true if a copy should be created
   */
  public void setCopy(boolean isCopy) {
    fCopy = isCopy;
  }

  /**
   * Sets whether a deprecation message including a link to the new element should be added to the
   * javadoc of the delegate. This is optional, but if set it must be called prior to
   * prepareDelegate().
   *
   * <p>The default is true (create deprecation message).
   *
   * @param declareDeprecated true if the member should be deprecated
   */
  public void setDeclareDeprecated(boolean declareDeprecated) {
    fDeclareDeprecated = declareDeprecated;
  }

  /**
   * When in copy mode, use this method to control the insertion point of the delegate. If the
   * parameter is true, the delegate gets inserted before the original declaration. If false, the
   * delegate gets inserted after the original declaration.
   *
   * <p>The default is false (do not insert before).
   *
   * @param insertBefore insertion point
   */
  public void setInsertBefore(boolean insertBefore) {
    fInsertBefore = insertBefore;
  }

  // Methods to be overridden by subclasses

  /** Initializes the creator. Must set the "new" name of the element if not already set. */
  protected abstract void initialize();

  /**
   * Creates the body of the delegate.
   *
   * @param declaration the member declaration
   * @return the body of the delegate
   * @throws JavaModelException
   */
  protected abstract ASTNode createBody(BodyDeclaration declaration) throws JavaModelException;

  /**
   * Creates the javadoc reference to the old member to be put inside the javadoc comment.
   *
   * <p>This method is only called if isDeclareDeprecated() == true.
   *
   * @param declaration the member declaration
   * @return the javadoc link node
   * @throws JavaModelException
   */
  protected abstract ASTNode createDocReference(BodyDeclaration declaration)
      throws JavaModelException;

  /**
   * Returns the node of the declaration on which to add the body.
   *
   * @param declaration the member declaration
   * @return the body head
   */
  protected abstract ASTNode getBodyHead(BodyDeclaration declaration);

  /**
   * Returns the javadoc property descriptor. The javadoc will be added using this descriptor.
   *
   * @return property descriptor
   */
  protected abstract ChildPropertyDescriptor getJavaDocProperty();

  /**
   * Returns the body property descriptor. The body of the delegate will be added using this
   * descriptor.
   *
   * @return property descriptor
   */
  protected abstract ChildPropertyDescriptor getBodyProperty();

  // Getters for subclasses

  protected boolean isMoveToAnotherFile() {
    return fIsMoveToAnotherFile;
  }

  protected AST getAst() {
    return fDelegateRewrite.getAST();
  }

  protected BodyDeclaration getDeclaration() {
    return fDeclaration;
  }

  protected String getNewElementName() {
    return fNewElementName;
  }

  /**
   * Prepares the delegate member. The delegate member will have the same signature as the old
   * member and contain a call to the new member and a javadoc reference with a reference to the new
   * member.
   *
   * <p>All references to the new member will contain the new name of the member and/or new
   * declaring type, if any.
   */
  public void prepareDelegate() throws JavaModelException {
    Assert.isNotNull(fDelegateRewrite);
    Assert.isNotNull(fDeclaration);

    initialize();

    // Moving to a new type?
    if (fDestinationTypeBinding != null) {
      fDestinationType =
          fOriginalRewrite.getImportRewrite().addImport(fDestinationTypeBinding, getAst());
      fIsMoveToAnotherFile = true;
    } else fIsMoveToAnotherFile = false;

    fTrackedPosition = fDelegateRewrite.getASTRewrite().track(fDeclaration);

    ASTNode delegateBody = createBody(fDeclaration);
    if (delegateBody != null) {
      // is null for interface and abstract methods
      fDelegateRewrite
          .getASTRewrite()
          .set(getBodyHead(fDeclaration), getBodyProperty(), delegateBody, null);
    }

    if (fDeclareDeprecated) {
      createJavadoc();
    }
  }

  /**
   * Creates the javadoc for the delegate.
   *
   * @throws JavaModelException
   */
  private void createJavadoc() throws JavaModelException {
    TagElement tag = getDelegateJavadocTag(fDeclaration);

    Javadoc comment = fDeclaration.getJavadoc();
    if (comment == null) {
      comment = getAst().newJavadoc();
      comment.tags().add(tag);
      fDelegateRewrite.getASTRewrite().set(fDeclaration, getJavaDocProperty(), comment, null);
    } else
      fDelegateRewrite
          .getASTRewrite()
          .getListRewrite(comment, Javadoc.TAGS_PROPERTY)
          .insertLast(tag, null);
  }

  /**
   * Performs the actual rewriting and adds an edit to the ASTRewrite set with {@link
   * #setSourceRewrite(CompilationUnitRewrite)}.
   *
   * @throws JavaModelException
   */
  public void createEdit() throws JavaModelException {
    try {
      IDocument document = new Document(fDelegateRewrite.getCu().getBuffer().getContents());
      TextEdit edit =
          fDelegateRewrite
              .getASTRewrite()
              .rewriteAST(document, fDelegateRewrite.getCu().getJavaProject().getOptions(true));
      edit.apply(document, TextEdit.UPDATE_REGIONS);

      String newSource =
          Strings.trimIndentation(
              document.get(fTrackedPosition.getStartPosition(), fTrackedPosition.getLength()),
              fPreferences.tabWidth,
              fPreferences.indentWidth,
              false);

      ASTNode placeholder =
          fOriginalRewrite
              .getASTRewrite()
              .createStringPlaceholder(newSource, fDeclaration.getNodeType());

      CategorizedTextEditGroup groupDescription =
          fOriginalRewrite.createCategorizedGroupDescription(
              getTextEditGroupLabel(), CATEGORY_DELEGATE);
      ListRewrite bodyDeclarationsListRewrite =
          fOriginalRewrite
              .getASTRewrite()
              .getListRewrite(fDeclaration.getParent(), getTypeBodyDeclarationsProperty());
      if (fCopy)
        if (fInsertBefore)
          bodyDeclarationsListRewrite.insertBefore(placeholder, fDeclaration, groupDescription);
        else bodyDeclarationsListRewrite.insertAfter(placeholder, fDeclaration, groupDescription);
      else bodyDeclarationsListRewrite.replace(fDeclaration, placeholder, groupDescription);

    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
  }

  protected abstract String getTextEditGroupLabel();

  /**
   * Returns the binding of the declaration.
   *
   * @return the binding of the declaration
   */
  protected abstract IBinding getDeclarationBinding();

  /**
   * Returns a new rewrite with the delegate changes registered. This rewrite can be used in-between
   * calls to prepareDelegate() and createEdit() to add additional changes to the delegate.
   *
   * @return CompilationUnitRewrite the new rewrite
   */
  public CompilationUnitRewrite getDelegateRewrite() {
    return fDelegateRewrite;
  }

  // ******************* INTERNAL HELPERS ***************************

  private TagElement getDelegateJavadocTag(BodyDeclaration declaration) throws JavaModelException {
    Assert.isNotNull(declaration);

    String msg = RefactoringCoreMessages.DelegateCreator_use_member_instead;
    int firstParam = msg.indexOf("{0}"); // $NON-NLS-1$
    Assert.isTrue(firstParam != -1);

    List<ASTNode> fragments = new ArrayList<ASTNode>();
    TextElement text = getAst().newTextElement();
    text.setText(msg.substring(0, firstParam).trim());
    fragments.add(text);

    fragments.add(createJavadocMemberReferenceTag(declaration, getAst()));

    text = getAst().newTextElement();
    text.setText(msg.substring(firstParam + 3).trim());
    fragments.add(text);

    final TagElement tag = getAst().newTagElement();
    tag.setTagName(TagElement.TAG_DEPRECATED);
    tag.fragments().addAll(fragments);
    return tag;
  }

  private TagElement createJavadocMemberReferenceTag(BodyDeclaration declaration, final AST ast)
      throws JavaModelException {
    Assert.isNotNull(ast);
    Assert.isNotNull(declaration);
    ASTNode javadocReference = createDocReference(declaration);
    final TagElement element = ast.newTagElement();
    element.setTagName(TagElement.TAG_LINK);
    element.fragments().add(javadocReference);
    return element;
  }

  protected Expression getAccess() {
    return isMoveToAnotherFile() ? createDestinationTypeName() : null;
  }

  protected Name createDestinationTypeName() {
    return ASTNodeFactory.newName(getAst(), ASTNodes.asString(fDestinationType));
  }

  private ChildListPropertyDescriptor getTypeBodyDeclarationsProperty() {
    ASTNode parent = fDeclaration.getParent();

    if (parent instanceof AbstractTypeDeclaration)
      return ((AbstractTypeDeclaration) parent).getBodyDeclarationsProperty();
    else if (parent instanceof AnonymousClassDeclaration)
      return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;

    Assert.isTrue(false);
    return null;
  }
}
