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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.java.api.tree.Position;

import static org.assertj.core.api.Assertions.assertThat;

class InternalPositionTest {

  @Test
  void construction() {
    InternalPosition position = new InternalPosition(42, 12);
    assertThat(position.line()).isEqualTo(42);
    assertThat(position.lineOffset()).isEqualTo(41);
    assertThat(position.column()).isEqualTo(12);
    assertThat(position.columnOffset()).isEqualTo(11);
    assertThat(position)
      .hasToString("42:12")
      .isEqualTo(Position.at(42, 12));
  }

  @Test
  void comparison() {
    assertThat(Position.at(1,1)).isEqualTo(Position.at(1, 1));
    assertThat(Position.at(1,1)).isNotEqualTo(Position.at(1, 2));
    assertThat(Position.at(1,1)).isNotEqualTo(Position.at(2, 1));

    Position first = Position.at(42, 11);
    Position second = Position.at(42, 12);

    assertThat(first)
      .isNotEqualTo(second)
      .isLessThan(second)
      .isLessThanOrEqualTo(second);

    assertThat(first.isGreaterThan(second)).isFalse();
    assertThat(first.isGreaterThanOrEqualTo(second)).isFalse();
    assertThat(first.isLessThan(second)).isTrue();
    assertThat(first.isLessThanOrEqualTo(second)).isTrue();

    assertThat(second)
      .isNotEqualTo(first)
      .isGreaterThan(first)
      .isGreaterThanOrEqualTo(first);

    assertThat(second.isGreaterThan(first)).isTrue();
    assertThat(second.isGreaterThanOrEqualTo(first)).isTrue();
    assertThat(second.isLessThan(first)).isFalse();
    assertThat(second.isLessThanOrEqualTo(first)).isFalse();
  }

}
