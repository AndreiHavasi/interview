# The `static` keyword

`static` ties a member to the **class itself**, not to any instance of it.

## Where it shows up

- **Static fields** — one copy per class loader, shared across instances.
- **Static methods** — invoked on the class, no implicit `this`.
- **Static nested classes** — don't hold a reference to an enclosing instance.
- **Static initializer blocks** — run once when the class is initialized.

```java
public final class Counter {
    private static int total = 0;          // shared
    private final int id;

    public Counter() {
        this.id = ++total;                  // mutates shared state
    }

    public static int getTotal() {          // no `this`
        return total;
    }
}
```

## Common interview traps

1. **Static methods are not polymorphic.** They are resolved at compile time by the declared type. Subclass "overrides" of static methods are *method hiding*, not overriding.
2. **Static state is global state.** Initialization order is per-class-loader, and tests sharing a JVM can leak state into each other.
3. **Static nested vs inner class.** Drop `static` and the nested class silently captures the enclosing instance — a frequent memory-leak source.

## Romania overlay

Asked frequently in screening rounds at **Endava**, **Luxoft**, and **ING Hubs**, usually paired with a follow-up about **method hiding vs overriding**.
