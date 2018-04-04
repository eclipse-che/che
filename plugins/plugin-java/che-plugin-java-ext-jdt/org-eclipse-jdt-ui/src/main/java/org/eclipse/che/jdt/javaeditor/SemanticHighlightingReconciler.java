/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javaeditor;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;

/**
 * Semantic highlighting reconciler
 *
 * @author Evgen Vidolob
 */
@Singleton
public class SemanticHighlightingReconciler {

  /** Semantic highlightings */
  private SemanticHighlighting[] fJobSemanticHighlightings;

  /** Background job's added highlighted positions */
  private ThreadLocal<List<HighlightedPosition>> fAddedPositions = new ThreadLocal<>();

  private SemanticHighlighting fJobDeprecatedMemberHighlighting;

  public SemanticHighlightingReconciler() {
    fJobSemanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
    for (SemanticHighlighting highlighting : fJobSemanticHighlightings) {
      if (highlighting instanceof SemanticHighlightings.DeprecatedMemberHighlighting) {
        fJobDeprecatedMemberHighlighting = highlighting;
        break;
      }
    }
  }

  public List<HighlightedPosition> reconcileSemanticHighlight(CompilationUnit ast) {
    fAddedPositions.set(new ArrayList<HighlightedPosition>());
    PositionCollector collector = new PositionCollector();
    ast.accept(collector);
    return fAddedPositions.get();
  }

  /** Collects positions from the AST. */
  private class PositionCollector extends GenericVisitor {

    /** The semantic token */
    private SemanticToken fToken = new SemanticToken();

    /*
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visitNode(org.eclipse.jdt.core.dom.ASTNode)
     */
    @Override
    protected boolean visitNode(ASTNode node) {
      if ((node.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
        //                retainPositions(node.getStartPosition(), node.getLength());
        return false;
      }
      return true;
    }

    /*
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BooleanLiteral)
     */
    @Override
    public boolean visit(BooleanLiteral node) {
      return visitLiteral(node);
    }

    /*
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CharacterLiteral)
     */
    @Override
    public boolean visit(CharacterLiteral node) {
      return visitLiteral(node);
    }

    /*
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NumberLiteral)
     */
    @Override
    public boolean visit(NumberLiteral node) {
      return visitLiteral(node);
    }

    private boolean visitLiteral(Expression node) {
      fToken.update(node);
      for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
        SemanticHighlighting semanticHighlighting = fJobSemanticHighlightings[i];
        if (semanticHighlighting.consumesLiteral(fToken)) {
          int offset = node.getStartPosition();
          int length = node.getLength();
          if (offset > -1 && length > 0) addPosition(offset, length, semanticHighlighting);
          break;
        }
      }
      fToken.clear();
      return false;
    }

    /*
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
     * @since 3.5
     */
    @Override
    public boolean visit(ConstructorInvocation node) {
      // XXX Hack for performance reasons (should loop over fJobSemanticHighlightings can call
      // consumes(*))
      if (fJobDeprecatedMemberHighlighting != null) {
        IMethodBinding constructorBinding = node.resolveConstructorBinding();
        if (constructorBinding != null && constructorBinding.isDeprecated()) {
          int offset = node.getStartPosition();
          int length = 4;
          if (offset > -1 && length > 0)
            addPosition(offset, length, fJobDeprecatedMemberHighlighting);
        }
      }
      return true;
    }

    /*
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
     * @since 3.5
     */
    @Override
    public boolean visit(SuperConstructorInvocation node) {
      // XXX Hack for performance reasons (should loop over fJobSemanticHighlightings can call
      // consumes(*))
      if (fJobDeprecatedMemberHighlighting != null) {
        IMethodBinding constructorBinding = node.resolveConstructorBinding();
        if (constructorBinding != null && constructorBinding.isDeprecated()) {
          int offset = node.getStartPosition();
          int length = 5;
          if (offset > -1 && length > 0)
            addPosition(offset, length, fJobDeprecatedMemberHighlighting);
        }
      }
      return true;
    }

    /*
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
     */
    @Override
    public boolean visit(SimpleName node) {
      fToken.update(node);
      for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
        SemanticHighlighting semanticHighlighting = fJobSemanticHighlightings[i];
        if (semanticHighlighting.consumes(fToken)) {
          int offset = node.getStartPosition();
          int length = node.getLength();
          if (offset > -1 && length > 0) addPosition(offset, length, semanticHighlighting);
          break;
        }
      }
      fToken.clear();
      return false;
    }

    /**
     * Add a position with the given range and highlighting iff it does not exist already.
     *
     * @param offset The range offset
     * @param length The range length
     * @param highlighting The highlighting
     */
    private void addPosition(int offset, int length, SemanticHighlighting highlighting) {
      HighlightedPosition highlightedPosition =
          DtoFactory.getInstance().createDto(HighlightedPosition.class);
      highlightedPosition.setLength(length);
      highlightedPosition.setOffset(offset);
      highlightedPosition.setType(highlighting.getType());
      fAddedPositions.get().add(highlightedPosition);
    }
  }
}
