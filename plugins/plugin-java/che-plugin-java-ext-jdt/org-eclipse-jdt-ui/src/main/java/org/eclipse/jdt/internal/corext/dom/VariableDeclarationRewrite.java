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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

/** Rewrite helper for variable declarations. */
public class VariableDeclarationRewrite {

  public static void rewriteModifiers(
      final SingleVariableDeclaration declarationNode,
      final int includedModifiers,
      final int excludedModifiers,
      final ASTRewrite rewrite,
      final TextEditGroup group) {
    ModifierRewrite listRewrite = ModifierRewrite.create(rewrite, declarationNode);
    listRewrite.setModifiers(includedModifiers, excludedModifiers, group);
  }

  public static void rewriteModifiers(
      final VariableDeclarationExpression declarationNode,
      final int includedModifiers,
      final int excludedModifiers,
      final ASTRewrite rewrite,
      final TextEditGroup group) {
    ModifierRewrite listRewrite = ModifierRewrite.create(rewrite, declarationNode);
    listRewrite.setModifiers(includedModifiers, excludedModifiers, group);
  }

  public static void rewriteModifiers(
      final FieldDeclaration declarationNode,
      final VariableDeclarationFragment[] toChange,
      final int includedModifiers,
      final int excludedModifiers,
      final ASTRewrite rewrite,
      final TextEditGroup group) {
    final List<VariableDeclarationFragment> fragmentsToChange = Arrays.asList(toChange);
    AST ast = declarationNode.getAST();
    /*
     * Problem: Same declarationNode can be the subject of multiple calls to this method.
     * For the 2nd++ calls, the original declarationNode has already been rewritten, and this has to be taken into account.
     *
     * Assumption:
     * - Modifiers for each VariableDeclarationFragment are modified at most once.
     *
     * Solution:
     * - Maintain a map from original VariableDeclarationFragments to their new FieldDeclaration.
     * - Original modifiers in declarationNode belong to the first fragment.
     * - When a later fragment needs different modifiers, we create a new FieldDeclaration and move all successive fragments into that declaration
     * - When a fragment has been moved to a new declaration, make sure we don't create a new move target again, but instead use the already created one
     */
    List<VariableDeclarationFragment> fragments = declarationNode.fragments();
    Iterator<VariableDeclarationFragment> iter = fragments.iterator();

    ListRewrite blockRewrite;
    if (declarationNode.getParent() instanceof AbstractTypeDeclaration) {
      blockRewrite =
          rewrite.getListRewrite(
              declarationNode.getParent(),
              ((AbstractTypeDeclaration) declarationNode.getParent())
                  .getBodyDeclarationsProperty());
    } else {
      blockRewrite =
          rewrite.getListRewrite(
              declarationNode.getParent(), AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
    }

    VariableDeclarationFragment lastFragment = iter.next();
    ASTNode lastStatement = declarationNode;

    if (fragmentsToChange.contains(lastFragment)) {
      ModifierRewrite modifierRewrite = ModifierRewrite.create(rewrite, declarationNode);
      modifierRewrite.setModifiers(includedModifiers, excludedModifiers, group);
    }

    ListRewrite fragmentsRewrite = null;
    while (iter.hasNext()) {
      VariableDeclarationFragment currentFragment = iter.next();

      @SuppressWarnings("unchecked")
      Map<VariableDeclarationFragment, MovedFragment> lookup =
          (Map<VariableDeclarationFragment, MovedFragment>)
              rewrite.getProperty(MovedFragment.class.getName());
      if (lookup == null) {
        lookup = new HashMap<VariableDeclarationFragment, MovedFragment>();
        rewrite.setProperty(MovedFragment.class.getName(), lookup);
      }
      MovedFragment currentMovedFragment = lookup.get(currentFragment);

      boolean changeLast = fragmentsToChange.contains(lastFragment);
      boolean changeCurrent = fragmentsToChange.contains(currentFragment);
      if (changeLast != changeCurrent || lookup.containsKey(lastFragment)) {
        ModifierRewrite modifierRewrite = null;
        if (currentMovedFragment != null) {
          // Current fragment has already been moved.

          if (currentMovedFragment.fUsesOriginalModifiers) {
            // Need to put in the right modifiers (removing any existing ones).
            modifierRewrite = ModifierRewrite.create(rewrite, currentMovedFragment.fDeclaration);
            ListRewrite listRewrite =
                rewrite.getListRewrite(
                    currentMovedFragment.fDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
            List<IExtendedModifier> extendedList = listRewrite.getRewrittenList();
            for (int i = 0; i < extendedList.size(); i++) {
              ASTNode curr = (ASTNode) extendedList.get(i);
              if (curr instanceof Modifier) rewrite.remove(curr, group);
            }
          }
          // otherwise, don't need to touch the modifiers, so leave modifierRewrite null

        } else { // need to split an existing field declaration
          VariableDeclarationFragment moveTarget;
          moveTarget = (VariableDeclarationFragment) rewrite.createMoveTarget(currentFragment);

          FieldDeclaration newStatement =
              (FieldDeclaration) ast.createInstance(FieldDeclaration.class);
          rewrite
              .getListRewrite(newStatement, FieldDeclaration.FRAGMENTS_PROPERTY)
              .insertLast(moveTarget, group);
          lookup.put(currentFragment, new MovedFragment(moveTarget, newStatement, !changeCurrent));
          rewrite.set(
              newStatement,
              FieldDeclaration.TYPE_PROPERTY,
              rewrite.createCopyTarget(declarationNode.getType()),
              group);

          modifierRewrite = ModifierRewrite.create(rewrite, newStatement);
          modifierRewrite.copyAllAnnotations(declarationNode, group);
          blockRewrite.insertAfter(newStatement, lastStatement, group);

          fragmentsRewrite =
              rewrite.getListRewrite(newStatement, FieldDeclaration.FRAGMENTS_PROPERTY);
          lastStatement = newStatement;
        }

        if (modifierRewrite != null) {
          if (changeCurrent) {
            int newModifiers =
                (declarationNode.getModifiers() & ~excludedModifiers) | includedModifiers;
            modifierRewrite.setModifiers(newModifiers, excludedModifiers, group);
          } else {
            int newModifiers = declarationNode.getModifiers();
            modifierRewrite.setModifiers(newModifiers, Modifier.NONE, group);
          }
        }

      } else if (fragmentsRewrite != null) {
        VariableDeclarationFragment fragment0;
        boolean usesOriginalModifiers = true;
        if (currentMovedFragment != null) {
          fragment0 = currentMovedFragment.fMoveTarget;
          usesOriginalModifiers = currentMovedFragment.fUsesOriginalModifiers;
          rewrite
              .getListRewrite(
                  currentMovedFragment.fDeclaration, FieldDeclaration.FRAGMENTS_PROPERTY)
              .remove(fragment0, group);
        } else {
          fragment0 = (VariableDeclarationFragment) rewrite.createMoveTarget(currentFragment);
        }
        lookup.put(
            currentFragment, new MovedFragment(fragment0, lastStatement, usesOriginalModifiers));
        fragmentsRewrite.insertLast(fragment0, group);
      }
      lastFragment = currentFragment;
    }
  }

  private static class MovedFragment {
    final VariableDeclarationFragment fMoveTarget;
    final ASTNode fDeclaration;
    boolean fUsesOriginalModifiers;

    public MovedFragment(
        VariableDeclarationFragment moveTarget,
        ASTNode declaration,
        boolean usesOriginalModifiers) {
      fMoveTarget = moveTarget;
      fDeclaration = declaration;
      fUsesOriginalModifiers = usesOriginalModifiers;
    }
  }

  public static void rewriteModifiers(
      final VariableDeclarationStatement declarationNode,
      final VariableDeclarationFragment[] toChange,
      final int includedModifiers,
      final int excludedModifiers,
      ASTRewrite rewrite,
      final TextEditGroup group) {
    final List<VariableDeclarationFragment> fragmentsToChange = Arrays.asList(toChange);
    AST ast = declarationNode.getAST();

    List<VariableDeclarationFragment> fragments = declarationNode.fragments();
    Iterator<VariableDeclarationFragment> iter = fragments.iterator();

    ListRewrite blockRewrite = null;
    ASTNode parentStatement = declarationNode.getParent();
    if (parentStatement instanceof SwitchStatement) {
      blockRewrite = rewrite.getListRewrite(parentStatement, SwitchStatement.STATEMENTS_PROPERTY);
    } else if (parentStatement instanceof Block) {
      blockRewrite = rewrite.getListRewrite(parentStatement, Block.STATEMENTS_PROPERTY);
    } else {
      // should not happen. VariableDeclaration's can not be in a control statement body
      Assert.isTrue(false);
    }

    VariableDeclarationFragment lastFragment = iter.next();
    ASTNode lastStatement = declarationNode;

    boolean modifiersModified = false;
    if (fragmentsToChange.contains(lastFragment)) {
      ModifierRewrite modifierRewrite = ModifierRewrite.create(rewrite, declarationNode);
      modifierRewrite.setModifiers(includedModifiers, excludedModifiers, group);
      modifiersModified = true;
    }

    ListRewrite fragmentsRewrite = null;
    while (iter.hasNext()) {
      VariableDeclarationFragment currentFragment = iter.next();

      if (fragmentsToChange.contains(lastFragment) != fragmentsToChange.contains(currentFragment)) {

        VariableDeclarationStatement newStatement =
            ast.newVariableDeclarationStatement(
                (VariableDeclarationFragment) rewrite.createMoveTarget(currentFragment));
        newStatement.setType((Type) rewrite.createCopyTarget(declarationNode.getType()));

        ModifierRewrite modifierRewrite = ModifierRewrite.create(rewrite, newStatement);
        if (fragmentsToChange.contains(currentFragment)) {
          modifierRewrite.copyAllAnnotations(declarationNode, group);
          int newModifiers =
              (declarationNode.getModifiers() & ~excludedModifiers) | includedModifiers;
          modifierRewrite.setModifiers(newModifiers, excludedModifiers, group);
        } else {
          modifierRewrite.copyAllModifiers(declarationNode, group, modifiersModified);
        }
        blockRewrite.insertAfter(newStatement, lastStatement, group);

        fragmentsRewrite =
            rewrite.getListRewrite(newStatement, VariableDeclarationStatement.FRAGMENTS_PROPERTY);
        lastStatement = newStatement;
      } else if (fragmentsRewrite != null) {
        ASTNode fragment0 = rewrite.createMoveTarget(currentFragment);
        fragmentsRewrite.insertLast(fragment0, group);
      }
      lastFragment = currentFragment;
    }
  }
}
