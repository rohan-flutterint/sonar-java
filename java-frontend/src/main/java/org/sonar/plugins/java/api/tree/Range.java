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
package org.sonar.plugins.java.api.tree;

import javax.annotation.Nonnull;
import org.sonar.java.model.InternalRange;

public interface Range {

  /**
   * @return the inclusive start position of the range. The character at this location is part of the range.
   */
  @Nonnull
  Position start();

  /**
   * @return the exclusive end position of the range. The character at this location is not part of the range.
   */
  @Nonnull
  Position end();

  /**
   * @param start is inclusive
   * @param end is exclusive
   */
  @Nonnull
  static Range at(Position start, Position end) {
    return new InternalRange(start, end);
  }

  /**
   * @param startColumn is inclusive
   * @param endColumn is exclusive
   */
  @Nonnull
  static Range at(int startLine, int startColumn, int endLine, int endColumn) {
    return new InternalRange(Position.at(startLine,startColumn), Position.at(endLine, endColumn));
  }

}
