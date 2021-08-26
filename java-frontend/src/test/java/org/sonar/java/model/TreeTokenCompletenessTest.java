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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.java.TestUtils;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.ast.visitors.SubscriptionVisitor;
import org.sonar.plugins.java.api.location.Position;
import org.sonar.plugins.java.api.location.Range;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class TreeTokenCompletenessTest {

  private static final String EOL = System.getProperty("line.separator");

  @Test
  void test() {
    // test itself
    File file = new File("src/test/java/org/sonar/java/model/TreeTokenCompletenessTest.java");

    List<String> basedOnSyntaxTree = readFileFromSyntaxTree(TestUtils.inputFile(file));
    List<String> basedOnFileLine = readFile(file);
    Map<Integer, String> differences = getDifferences(basedOnSyntaxTree, basedOnFileLine);

    assertThat(basedOnSyntaxTree).isNotEmpty();
    assertThat(basedOnSyntaxTree.size()).isEqualTo(basedOnFileLine.size());

    // printListString(basedOnSyntaxTree);
    // printDifferences(differences);

    // the difference is on parsing on generic: "line 117 : 'Lists.<File>newArrayList(), null));'"
    assertThat(differences).isEmpty();
  }

  private static Map<Integer, String> getDifferences(List<String> basedOnSyntaxTree, List<String> basedOnFileLine) {
    Map<Integer, String> differences = new HashMap<>();
    for (int i = 0; i < basedOnSyntaxTree.size(); i++) {
      String lineFromSyntaxTree = basedOnSyntaxTree.get(i);
      String lineFromFile = basedOnFileLine.get(i);
      if (!StringUtils.isBlank(lineFromSyntaxTree) && !StringUtils.isBlank(lineFromFile)) {
        String difference = StringUtils.difference(lineFromSyntaxTree, lineFromFile);
        if (!difference.isEmpty()) {
          differences.put(i + 1, difference);
        }
      }
    }
    return differences;
  }

  private static void printDifferences(Map<Integer, String> differences) {
    List<Integer> keys = new ArrayList<>(differences.keySet());
    Collections.sort(keys);

    List<String> diffsWithLines = new LinkedList<>();
    for (Integer key : keys) {
      diffsWithLines.add("line " + String.format("%03d", key) + " : '" + differences.get(key) + "'");
    }

    printDiffHeader();
    printListString(diffsWithLines);
  }

  private static void printDiffHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append(EOL);
    builder.append(EOL);
    builder.append("----------------------- diff -------------------------------");
    builder.append(EOL);
    System.out.println(builder.toString());
  }

  private static void printListString(List<String> basedOnSyntaxTree) {
    for (String line : basedOnSyntaxTree) {
      System.out.println(line);
    }
  }

  private static List<String> readFile(File file) {
    try {
      return FileUtils.readLines(file);
    } catch (IOException e) {
      fail("can not read test file");
    }
    return Collections.emptyList();
  }

  private static List<String> readFileFromSyntaxTree(InputFile inputFile) {
    TokenPrinter tokenPrinter = new TokenPrinter();
    JavaAstScanner.scanSingleFileForTests(inputFile, new VisitorsBridge(Collections.singletonList(tokenPrinter), Collections.emptyList(), null));
    return tokenPrinter.getPrintedFile();
  }

  private static class TokenPrinter extends SubscriptionVisitor {
    private int lastLine = Position.FIRST_LINE;
    private int lastColumn = Position.FIRST_COLUMN;
    private StringBuilder resultBuilder = new StringBuilder();

    @Override
    public List<Tree.Kind> nodesToVisit() {
      return Collections.singletonList(Tree.Kind.TOKEN);
    }

    @Override
    public void visitToken(SyntaxToken syntaxToken) {
      for (SyntaxTrivia trivia : syntaxToken.trivias()) {
        print(trivia.comment(), trivia.range());
      }
      print(syntaxToken.text(), syntaxToken.range());
    }

    private void print(String text, Range range) {
      while (lastLine < range.start().line()) {
        newLine();
      }
      while (lastColumn < range.start().column()) {
        space();
      }
      resultBuilder.append(text);
      if (range.start().line() == range.end().line()) {
        lastColumn += range.end().column() - range.start().column();
      } else {
        lastLine += range.end().line() - range.start().line();
        lastColumn = range.end().column();
      }
    }

    private void newLine() {
      lastColumn = Position.FIRST_COLUMN;
      lastLine++;
      resultBuilder.append(EOL);
    }

    public void space() {
      lastColumn++;
      resultBuilder.append(" ");
    }

    public List<String> getPrintedFile() {
      return Arrays.asList(resultBuilder.toString().split(EOL));
    }
  }
}
