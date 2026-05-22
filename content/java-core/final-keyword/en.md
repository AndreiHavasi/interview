# The `final` keyword

`final` enforces immutability of a binding (not of the object behind it).

- **`final` variable** — assigned exactly once.
- **`final` method** — cannot be overridden.
- **`final` class** — cannot be subclassed.

```java
public final class ImmutablePoint {
    private final int x;
    private final int y;
    public ImmutablePoint(int x, int y) { this.x = x; this.y = y; }
}
```

## Trap

`final List<String> xs = new ArrayList<>();` — the *reference* is final, but `xs.add(...)` still works. Immutability of the binding ≠ immutability of the object.
