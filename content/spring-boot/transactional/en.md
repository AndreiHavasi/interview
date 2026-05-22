# `@Transactional` and the proxy pattern

`@Transactional` is implemented by a **proxy** that wraps the bean. When you call an annotated method *through the proxy*, the proxy opens a transaction, calls the real method, then commits or rolls back.

## The classic gotcha: self-invocation

```java
@Service
public class OrderService {

    @Transactional
    public void place(Order o) { /* ... */ }

    public void placeMany(List<Order> os) {
        for (Order o : os) {
            place(o);   // <- bypasses the proxy. NO transaction per call.
        }
    }
}
```

Because `place()` is invoked via `this`, not via the proxy reference, the AOP advice never runs. Solutions:

1. Inject `OrderService` into itself (self-injection).
2. Move the inner method into a different bean.
3. Use `AopContext.currentProxy()` (requires `@EnableAspectJAutoProxy(exposeProxy = true)`).

## Why this is on every interview list

It's a clean test for whether a candidate understands **how Spring actually works** — IoC + dynamic proxies + bean lifecycle — rather than just having memorized the annotation.

## Romania overlay

Endava, Luxoft, and ING Hubs all routinely ask about self-invocation, propagation modes (`REQUIRES_NEW` vs `REQUIRED`), and rollback rules for checked exceptions.
