/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.text.edits.TextEditGroup;

/** Rewrite helper for modifier lists. */
public class ModifierRewrite {

  public static final int VISIBILITY_MODIFIERS =
      Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

  private ListRewrite fModifierRewrite;
  private AST fAst;

  public static ModifierRewrite create(ASTRewrite rewrite, ASTNode declNode) {
    return new ModifierRewrite(rewrite, declNode);
  }

  private ModifierRewrite(ASTRewrite rewrite, ASTNode declNode) {
    fModifierRewrite = evaluateListRewrite(rewrite, declNode);
    fAst = declNode.getAST();
  }

  private ListRewrite evaluateListRewrite(ASTRewrite rewrite, ASTNode declNode) {
    switch (declNode.getNodeType()) {
      case ASTNode.METHOD_DECLARATION:
        return rewrite.getListRewrite(declNode, MethodDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.FIELD_DECLARATION:
        return rewrite.getListRewrite(declNode, FieldDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
        return rewrite.getListRewrite(declNode, VariableDeclarationExpression.MODIFIERS2_PROPERTY);
      case ASTNode.VARIABLE_DECLARATION_STATEMENT:
        return rewrite.getListRewrite(declNode, VariableDeclarationStatement.MODIFIERS2_PROPERTY);
      case ASTNode.SINGLE_VARIABLE_DECLARATION:
        return rewrite.getListRewrite(declNode, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.TYPE_DECLARATION:
        return rewrite.getListRewrite(declNode, TypeDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.ENUM_DECLARATION:
        return rewrite.getListRewrite(declNode, EnumDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.ANNOTATION_TYPE_DECLARATION:
        return rewrite.getListRewrite(declNode, AnnotationTypeDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.ENUM_CONSTANT_DECLARATION:
        return rewrite.getListRewrite(declNode, EnumConstantDeclaration.MODIFIERS2_PROPERTY);
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
        return rewrite.getListRewrite(
            declNode, AnnotationTypeMemberDeclaration.MODIFIERS2_PROPERTY);
      default:
        throw new IllegalArgumentException(
            "node has no modifiers: " + declNode.getClass().getName()); // $NON-NLS-1$
    }
  }

  public ListRewrite getModifierRewrite() {
    return fModifierRewrite;
  }

  /**
   * Sets the given modifiers. Removes all other flags, but leaves annotations in place.
   *
   * @param modifiers the modifiers to set
   * @param editGroup the edit group in which to collect the corresponding text edits, or <code>null
   *     </code> if ungrouped
   * @return a tracked position that contains the changed modifiers
   */
  public PositionInformation setModifiers(int modifiers, TextEditGroup editGroup) {
    return internalSetModifiers(modifiers, -1, editGroup);
  }

  /**
   * Sets the included modifiers and removes the excluded modifiers. Does not touch other flags and
   * leaves annotations in place.
   *
   * @param included the modifiers to set
   * @param excluded the modifiers to remove
   * @param editGroup the edit group in which to collect the corresponding text edits, or <code>null
   *     </code> if ungrouped
   * @return a tracked position that contains the changed modifiers
   */
  public PositionInformation setModifiers(int included, int excluded, TextEditGroup editGroup) {
    return internalSetModifiers(included, included | excluded, editGroup);
  }

  /**
   * Sets the included visibility modifiers and removes existing visibility modifiers. Does not
   * touch other flags and leaves annotations in place.
   *
   * @param visibilityFlags the new visibility modifiers
   * @param editGroup the edit group in which to collect the corresponding text edits, or <code>null
   *     </code> if ungrouped
   * @return a tracked position that contains the changed modifiers, or <code>null</code> iff <code>
   *     editGroup == null</code>
   */
  public PositionInformation setVisibility(int visibilityFlags, TextEditGroup editGroup) {
    return internalSetModifiers(visibilityFlags, VISIBILITY_MODIFIERS, editGroup);
  }

  public void copyAllModifiers(ASTNode otherDecl, TextEditGroup editGroup) {
    copyAllModifiers(otherDecl, editGroup, false);
  }

  public void copyAllModifiers(
      ASTNode otherDecl, TextEditGroup editGroup, boolean copyIndividually) {
    ListRewrite modifierList = evaluateListRewrite(fModifierRewrite.getASTRewrite(), otherDecl);
    List<IExtendedModifier> originalList = modifierList.getOriginalList();
    if (originalList.isEmpty()) {
      return;
    }

    if (copyIndividually) {
      for (Iterator<IExtendedModifier> iterator = originalList.iterator(); iterator.hasNext(); ) {
        ASTNode modifier = (ASTNode) iterator.next();
        ASTNode copy = fModifierRewrite.getASTRewrite().createCopyTarget(modifier);
        if (copy != null) { // paranoia check (only left here because we're in RC1)
          fModifierRewrite.insertLast(copy, editGroup);
        }
      }
    } else {
      ASTNode copy =
          modifierList.createCopyTarget(
              (ASTNode) originalList.get(0), (ASTNode) originalList.get(originalList.size() - 1));
      if (copy != null) { // paranoia check (only left here because we're in RC1)
        fModifierRewrite.insertLast(copy, editGroup);
      }
    }
  }

  public void copyAllAnnotations(ASTNode otherDecl, TextEditGroup editGroup) {
    ListRewrite modifierList = evaluateListRewrite(fModifierRewrite.getASTRewrite(), otherDecl);
    List<IExtendedModifier> originalList = modifierList.getOriginalList();

    for (Iterator<IExtendedModifier> iterator = originalList.iterator(); iterator.hasNext(); ) {
      IExtendedModifier modifier = iterator.next();
      if (modifier.isAnnotation()) {
        fModifierRewrite.insertLast(
            fModifierRewrite.getASTRewrite().createCopyTarget((Annotation) modifier), editGroup);
      }
    }
  }

  /**
   * Sets the given modifiers and removes all other modifiers that match the consideredFlags mask.
   * Does not touch other flags and leaves annotations in place.
   *
   * @param modifiers the modifiers to set
   * @param consideredFlags mask of modifiers to consider
   * @param editGroup the edit group in which to collect the corresponding text edits, or <code>null
   *     </code> if ungrouped
   * @return a tracked position that contains the changed modifiers
   */
  private PositionInformation internalSetModifiers(
      int modifiers, int consideredFlags, TextEditGroup editGroup) {
    int newModifiers = modifiers & consideredFlags;

    ITrackedNodePosition trackedFallback = null;
    List<ITrackedNodePosition> trackedNodes = new ArrayList<ITrackedNodePosition>();

    // remove modifiers
    List<IExtendedModifier> originalList = fModifierRewrite.getOriginalList();
    for (int i = 0; i < originalList.size(); i++) {
      ASTNode curr = (ASTNode) originalList.get(i);
      if (curr instanceof Modifier) {
        int flag = ((Modifier) curr).getKeyword().toFlagValue();
        if ((consideredFlags & flag) != 0) {
          if ((newModifiers & flag) == 0) {
            fModifierRewrite.remove(curr, editGroup);
            if (trackedFallback == null)
              trackedFallback = fModifierRewrite.getASTRewrite().track(curr);
          }
          newModifiers &= ~flag;
        }
      }
    }

    // find last annotation
    IExtendedModifier lastAnnotation = null;
    List<IExtendedModifier> extendedList = fModifierRewrite.getRewrittenList();
    for (int i = 0; i < extendedList.size(); i++) {
      IExtendedModifier curr = extendedList.get(i);
      if (curr.isAnnotation()) lastAnnotation = curr;
    }

    // add modifiers
    List<Modifier> newNodes = ASTNodeFactory.newModifiers(fAst, newModifiers);
    for (int i = 0; i < newNodes.size(); i++) {
      Modifier curr = newNodes.get(i);
      if ((curr.getKeyword().toFlagValue() & VISIBILITY_MODIFIERS) != 0) {
        if (lastAnnotation != null)
          fModifierRewrite.insertAfter(curr, (ASTNode) lastAnnotation, editGroup);
        else fModifierRewrite.insertFirst(curr, editGroup);
      } else {
        fModifierRewrite.insertLast(curr, editGroup);
      }
      trackedNodes.add(fModifierRewrite.getASTRewrite().track(curr));
    }

    if (trackedNodes.isEmpty()) {
      if (trackedFallback == null) {
        // out of tricks...
        trackedFallback = fModifierRewrite.getASTRewrite().track(fModifierRewrite.getParent());
      }
      return new LinkedProposalPositionGroup.StartPositionInformation(trackedFallback);
    } else {
      return new LinkedProposalPositionGroup.TrackedNodesPosition(trackedNodes);
    }
  }
}
