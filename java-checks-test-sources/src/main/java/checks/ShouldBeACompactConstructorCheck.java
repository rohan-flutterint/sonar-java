package checks;

import java.util.Locale;

public class ShouldBeACompactConstructorCheck {
  record OnlyTrivialAssignmentsAtTheEnd(String name, int age) {
    OnlyTrivialAssignmentsAtTheEnd(String name, int age) { // Noncompliant [[sc=5;ec=35]] {{Replace this usage of a 'canonical' constructor with a more concise 'compact' version.}}
      if (age < 0) {
        throw new IllegalArgumentException("Negative age");
      }
      this.name = name;
      this.age = age;
    }

    public String name() {
      return this.name.toLowerCase(Locale.ROOT);
    }
  }

  record TrivialAssignmentsSplit(String name, int age) {
    TrivialAssignmentsSplit(String name, int age) { // Compliant
      this.name = name;
      if (age < 0) {
        throw new IllegalArgumentException("Negative age");
      }
      this.age = age;
    }

    public String name() {
      return this.name.toLowerCase(Locale.ROOT);
    }
  }

  record NonTrivialAssignmentsAtheEnd(String name, int age) {
    NonTrivialAssignmentsAtheEnd(String name, int age) { // Compliant
      if (age < 0) {
        throw new IllegalArgumentException("Negative age");
      }
      this.name = name.trim().toLowerCase(Locale.ROOT);
      this.age = age;
    }
  }

  record OnlyTrivialAssignments(String name, int age) {
    OnlyTrivialAssignments(String name, int age) { // Compliant because already reported by S6207
      this.name = name;
      this.age = age;
    }
  }

  record Compliant(String name, int age) {
    Compliant { // Compliant
      if (age < 0) {
        throw new IllegalArgumentException("Negative age");
      }
    }
  }
}
