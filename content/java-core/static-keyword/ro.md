# Cuvântul-cheie `static`

`static` leagă un membru de **clasă**, nu de o instanță a ei.

## Unde apare

- **Câmpuri statice** — o singură copie per class loader, partajată între instanțe.
- **Metode statice** — apelate pe clasă, fără `this` implicit.
- **Clase imbricate statice** — nu rețin o referință către instanța clasei externe.
- **Blocuri de inițializare statice** — rulează o singură dată, la inițializarea clasei.

```java
public final class Counter {
    private static int total = 0;
    private final int id;

    public Counter() {
        this.id = ++total;
    }

    public static int getTotal() {
        return total;
    }
}
```

## Capcane frecvente la interviu

1. **Metodele statice nu sunt polimorfice.** Sunt rezolvate la compilare după tipul declarat. „Suprascrierea" în subclasă este *method hiding*, nu overriding.
2. **Starea statică este stare globală.** Ordinea de inițializare este per class loader, iar testele care împart același JVM pot să-și scurgă starea reciproc.
3. **Static nested vs inner class.** Fără `static`, clasa imbricată captează tăcut instanța externă — sursă frecventă de memory leak.

## Context piața RO

Întrebare frecventă în rundele de screening la **Endava**, **Luxoft** și **ING Hubs**, de obicei urmată de **method hiding vs overriding**.
