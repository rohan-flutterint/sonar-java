package checks.regex;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class PossessiveQuantifierContinuationCheck2 {

  public void f(Pattern pattern) {
    f(compile(".*+\\w+")); // Noncompliant
  }
}
