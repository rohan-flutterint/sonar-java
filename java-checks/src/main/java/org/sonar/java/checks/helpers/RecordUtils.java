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
package org.sonar.java.checks.helpers;

import java.util.List;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class RecordUtils {
  private RecordUtils() {
  }

  public static boolean isACanonicalConstructor(MethodTree method) {
    // Check if the method is a constructor
    if (!isARecordConstructor(method)) {
      return false;
    }
    // Check if constructor has a throw clause
    if (!method.throwsClauses().isEmpty()) {
      return false;
    }
    ClassTree theRecord = (ClassTree) method.symbol().owner().declaration();
    // Check if the number of parameters matches the number of components
    List<VariableTree> components = theRecord.recordComponents();
    List<VariableTree> parameters = method.parameters();
    if (components.size() != parameters.size()) {
      return false;
    }
    // Check if components and parameters are ordered int the same way
    for (int i = 0; i < components.size(); i++) {
      VariableTree component = components.get(i);
      VariableTree parameter = parameters.get(i);
      if (!component.simpleName().name().equals(parameter.simpleName().name()) ||
        !component.symbol().type().equals(parameter.symbol().type())) {
        return false;
      }
    }
    return true;
  }

  public static boolean isACompactConstructor(MethodTree method) {
    return isARecordConstructor(method) &&
      method.openParenToken() == method.closeParenToken() &&
      method.parameters().isEmpty();
  }

  public static boolean isARecordConstructor(MethodTree method) {
    // Check if the method is a constructor
    if (!"<init>".equals(method.symbol().name())) {
      return false;
    }
    // Check if the owner is a record
    return method.symbol().owner().type().isSubtypeOf("java.lang.Record");
  }
}
