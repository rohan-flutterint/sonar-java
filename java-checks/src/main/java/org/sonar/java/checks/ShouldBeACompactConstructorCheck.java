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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.java.checks.helpers.RecordUtils;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S6210")
public class ShouldBeACompactConstructorCheck extends AbstractRecordChecker {
  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.RECORD);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassTree someRecord = (ClassTree) tree;
    Optional<MethodTree> canonicalConstructor = someRecord.members().stream()
      .filter(member -> member.is(Tree.Kind.CONSTRUCTOR))
      .map(MethodTree.class::cast)
      .filter(RecordUtils::isACanonicalConstructor)
      .findFirst();
    if (!canonicalConstructor.isPresent()) {
      return;
    }
    List<AssignmentExpressionTree> assignments = extractLastAssignments(canonicalConstructor.get());
    List<Symbol.VariableSymbol> components = someRecord.recordComponents().stream()
      .map(component -> (Symbol.VariableSymbol) component.symbol())
      .collect(Collectors.toList());
    List<Symbol.VariableSymbol> parameters = canonicalConstructor.get().parameters().stream()
      .map(parameter -> (Symbol.VariableSymbol) parameter.symbol())
      .collect(Collectors.toList());
    Set<Symbol.VariableSymbol> componentsInTrivialAssignment = new HashSet<>();
    for (AssignmentExpressionTree assignment : assignments) {
      isTrivialAssignment(assignment, components, parameters).ifPresent(componentsInTrivialAssignment::add);
    }
    if (componentsInTrivialAssignment.containsAll(components)) {
      reportIssue(canonicalConstructor.get(), "BOOM!");
    }
  }

  private static List<AssignmentExpressionTree> extractLastAssignments(MethodTree method) {
    List<StatementTree> statements = method.block().body();
    List<AssignmentExpressionTree> assignments = new ArrayList<>();
    for (int i = statements.size() - 1; i >= 0; i--) {
      StatementTree statement = statements.get(i);
      Optional<AssignmentExpressionTree> assignment = extractAssignment(statement);
      if (!assignment.isPresent()) {
        break;
      }
      assignments.add(assignment.get());
    }
    return assignments;
  }
}
