package checks;

import java.util.Locale;

public class ShouldBeACompactConstructorCheck {
  record TrivialAssignmentAtTheEnd(String name, int age) {
    TrivialAssignmentAtTheEnd(String name, int age) { // Noncompliant
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

  record NonTrivialAssignment(String name, int age) {
    NonTrivialAssignment(String name, int age) { // Compliant
      if (age < 0) {
        throw new IllegalArgumentException("Negative age");
      }
      this.name = name.trim().toLowerCase(Locale.ROOT);
      this.age = age;
    }

    public String name() {
      return this.name.toLowerCase(Locale.ROOT);
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
