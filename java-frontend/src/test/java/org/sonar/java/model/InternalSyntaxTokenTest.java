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
package org.sonar.java.model;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.java.reporting.AnalyzerMessage.TextSpan;
import org.sonar.plugins.java.api.tree.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class InternalSyntaxTokenTest {

  @Test
  void line_column() {
    SyntaxToken token = token(1, 0, "");
    assertThat(token.line()).isEqualTo(1);
    assertThat(token.column()).isZero();
    assertThat(token.text()).isEmpty();

    token = token(42, 21, "foo");
    assertThat(token.line()).isEqualTo(42);
    assertThat(token.column()).isEqualTo(21);
    assertThat(token.text()).isEqualTo("foo");
  }

  @Test
  void text_span() {
    assertThat(token(1, 0, "").textSpan())
      .isEqualTo(new TextSpan(1,0,1,0));

    assertThat(token(42, 21, "foo").textSpan())
      .isEqualTo(new TextSpan(42,21,42, 24));

    assertThat(token(42, 21, "\"\"\"foo\"\"\"").textSpan())
      .isEqualTo(new TextSpan(42,21,42, 30));

    assertThat(token(10, 7, "\"\"\"foo\r\n  bar\n  \r  qix\"\"\"").textSpan())
      .isEqualTo(new TextSpan(10,7,13, 8));

    assertThat(token(10, 7, "\"\"\"\n\n\n\"\"\"").textSpan())
      .isEqualTo(new TextSpan(10,7,13, 3));
  }

  private static InternalSyntaxToken token(int line, int column, String value) {
    return new InternalSyntaxToken(line, column, value, Collections.emptyList(), false);
  }

}
