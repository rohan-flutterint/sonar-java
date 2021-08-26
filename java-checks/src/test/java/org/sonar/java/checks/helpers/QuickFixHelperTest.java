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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.MethodTree;

import static org.assertj.core.api.Assertions.assertThat;

class QuickFixHelperTest {

  @Test
  void nextToken() {
    CompilationUnitTree cut = JParserTestUtils.parse("class A { void foo() {} }");
    ClassTree a = (ClassTree) cut.types().get(0);
    MethodTree foo = (MethodTree) a.members().get(0);

    assertThat(QuickFixHelper.nextToken(foo.simpleName())).isEqualTo(foo.openParenToken());

    // through non-existing nodes (modifiers of method)
    assertThat(QuickFixHelper.nextToken(a.openBraceToken()))
      .isEqualTo(QuickFixHelper.nextToken(foo.modifiers()))
      .isEqualTo(foo.returnType().firstToken());

    // need to go through parent
    assertThat(QuickFixHelper.nextToken(foo.block().lastToken())).isEqualTo(a.closeBraceToken());

    // end of file
    assertThat(QuickFixHelper.nextToken(a.closeBraceToken()))
      .isEqualTo(QuickFixHelper.nextToken(cut))
      .isEqualTo(cut.lastToken());
    assertThat(((InternalSyntaxToken) cut.lastToken()).isEOF()).isTrue();
  }

  @Test
  void previousToken() {
    CompilationUnitTree cut = JParserTestUtils.parse("class A { void foo() {} }");
    ClassTree a = (ClassTree) cut.types().get(0);
    MethodTree foo = (MethodTree) a.members().get(0);

    assertThat(QuickFixHelper.previousToken(foo.simpleName())).isEqualTo(foo.returnType().lastToken());

    // through non-existing nodes (modifiers of method)
    assertThat(QuickFixHelper.previousToken(a.openBraceToken())).isEqualTo(a.simpleName().lastToken());

    // need to go through parent
    assertThat(QuickFixHelper.previousToken(foo.returnType())).isEqualTo(a.openBraceToken());

    // start of file
    assertThat(QuickFixHelper.previousToken(a.declarationKeyword())).isEqualTo(a.declarationKeyword());
  }

  @Nested
  class Imports {

    /**
     * Can only happen in a package-info file
     */
    @Test
    void no_imports() {
      String source = "package org.foo;";

      CompilationUnitTree cut = JParserTestUtils.parse(source);

      assertThat(QuickFixHelper.requiresImportOf("org.foo.A", cut)).isFalse();
      assertThat(QuickFixHelper.requiresImportOf("org.bar.A", cut)).isTrue();
    }

    @Test
    void imported_via_star_import() {
      String source = "package org.foo;\n"
        + "import java.util.*;\n"
        + "import org.bar.B;\n"
        + "import static java.util.function.Function.identity;\n"
        + "class A { }";

      CompilationUnitTree cut = JParserTestUtils.parse(source);

      assertThat(QuickFixHelper.requiresImportOf("org.foo.B", cut)).isFalse();
      assertThat(QuickFixHelper.requiresImportOf("java.util.List", cut)).isFalse();
      assertThat(QuickFixHelper.requiresImportOf("java.util.Collections", cut)).isFalse();

      // requires import
      assertThat(QuickFixHelper.requiresImportOf("org.bar.A", cut)).isTrue();
      assertThat(QuickFixHelper.requiresImportOf("java.util.function.Function", cut)).isTrue();
    }

    @Test
    void imported_via_explicit_import() {
      String source = "package org.foo;\n"
        + "import java.util.List;\n"
        + "import org.bar.B;\n"
        + "import static java.util.function.Function.identity;\n"
        + "class A { }";

      CompilationUnitTree cut = JParserTestUtils.parse(source);

      assertThat(QuickFixHelper.requiresImportOf("org.foo.B", cut)).isFalse();
      assertThat(QuickFixHelper.requiresImportOf("java.util.List", cut)).isFalse();

      // requires import
      assertThat(QuickFixHelper.requiresImportOf("org.bar.A", cut)).isTrue();
      assertThat(QuickFixHelper.requiresImportOf("java.util.Collections", cut)).isTrue();
    }

    @Test
    void default_package() {
      String source = "import java.util.List;\n"
        + "import org.bar.B;\n"
        + "class A { }";

      CompilationUnitTree cut = JParserTestUtils.parse(source);

      assertThat(QuickFixHelper.requiresImportOf("java.util.List", cut)).isFalse();
      assertThat(QuickFixHelper.requiresImportOf("org.bar.B", cut)).isFalse();

      // requires import
      assertThat(QuickFixHelper.requiresImportOf("org.foo.B", cut)).isTrue();
      assertThat(QuickFixHelper.requiresImportOf("org.bar.A", cut)).isTrue();
      assertThat(QuickFixHelper.requiresImportOf("java.util.Collections", cut)).isTrue();
    }
  }
}
