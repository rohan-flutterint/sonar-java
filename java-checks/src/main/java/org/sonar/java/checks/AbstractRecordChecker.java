/*
 * SonarQube Java
 * Copyright (C) 2012-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree;

public abstract class AbstractRecordChecker extends IssuableSubscriptionVisitor {
  /**
   * Returns the symbol of a component used as the receiving end of the assignment if the matching parameter is used as the value.
   * @param assignment The assignment statement
   * @param components The components' symbols
   * @param parameters The parameters' symbols
   * @return Optional of the component's symbol receiving the matching parameter. Optional.empty() otherwise.
   */
  protected static Optional<Symbol.VariableSymbol> isTrivialAssignment(
    AssignmentExpressionTree assignment,
    List<Symbol.VariableSymbol> components,
    List<Symbol.VariableSymbol> parameters) {
    ExpressionTree leftHandSide = assignment.variable();
    if (!leftHandSide.is(Tree.Kind.MEMBER_SELECT)) {
      return Optional.empty();
    }
    Symbol variableSymbol = ((MemberSelectExpressionTree) leftHandSide).identifier().symbol();
    Optional<Symbol.VariableSymbol> component = components.stream()
      .filter(variableSymbol::equals)
      .findFirst();
    if (!component.isPresent()) {
      return Optional.empty();
    }
    ExpressionTree rightHandSide = assignment.expression();
    if (!rightHandSide.is(Tree.Kind.IDENTIFIER)) {
      return Optional.empty();
    }
    Symbol valueSymbol = ((IdentifierTree) rightHandSide).symbol();
    if (parameters.stream().anyMatch(valueSymbol::equals) && variableSymbol.name().equals(valueSymbol.name())) {
      return component;
    }
    return Optional.empty();
  }

  protected static Optional<AssignmentExpressionTree> extractAssignment(StatementTree statement) {
    if (!statement.is(Tree.Kind.EXPRESSION_STATEMENT)) {
      return Optional.empty();
    }
    ExpressionStatementTree initialStatement = (ExpressionStatementTree) statement;
    if (!initialStatement.expression().is(Tree.Kind.ASSIGNMENT)) {
      return Optional.empty();
    }
    return Optional.of((AssignmentExpressionTree) initialStatement.expression());
  }
}
