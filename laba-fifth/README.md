# Лабораторная работа № 5: Collections Framework и Generics — репозиторий финансовых операций

**Дисциплина:** Современные технологии объектно-ориентированного программирования  
**Студент:** [Фамилия И.О.]  
**Группа:** [Группа]

---

## 🎯 1. Цель работы
Научиться выбирать коллекцию Java по смыслу задачи и по алгоритмической сложности операций,
применять контракт `equals`/`hashCode`, сортировать объекты через `Comparable` и `Comparator`,
а также заменить конкретное хранилище обобщенным (`Generics`) и разобраться с `PECS` и стиранием типов.

## 📝 2. Задание
1. Хранить операции в подходящих коллекциях: `List<Transaction>`, `Set<Category>`, `Map<UUID, Transaction>`, `TreeMap<LocalDate, ...>`.
2. Решить задачи: найти операцию по UUID, получить уникальные категории, сгруппировать суммы по дате без Stream API,
   удалить операцию, отсортировать операции, найти повторяющиеся категории. Для каждой — письменно обосновать выбор структуры.
3. Эксперимент `ArrayList` vs `LinkedList` на четырех сценариях.
4. Преобразовать хранилище в обобщенный `Repository<T, ID>` + `Identifiable<ID>` + `InMemoryRepository<T extends Identifiable<ID>, ID>`, использовать `Optional`.
5. Написать generic-метод с `? extends T` / `? super T`, сформулировать PECS.
6. Эксперимент на type erasure.
7. Не менее трех `Comparator`; решить, нужен ли `Comparable` у `Transaction`.
8. Контрольная задача: 100 000 операций, многократный поиск по id — `List` или `Map`?
9. Автопроверка.

---

## 📁 3. Структура проекта

```
laba-fifth/src
├── Main.java                         — демонстрация всех пунктов по порядку
├── model/
│   ├── Identifiable.java             — интерфейс «у сущности есть id»
│   ├── Transaction.java              — операция (implements Identifiable<UUID>)
│   ├── Category.java                 — категория (equals/hashCode, Comparable)
│   ├── TransactionType.java          — INCOME / EXPENSE
│   ├── TransactionComparators.java   — BY_DATE, BY_AMOUNT, BY_CATEGORY_THEN_DATE
│   └── Wallet.java                   — кошелек из лаб. 4 (implements Identifiable<String>)
├── exception/InvalidTransactionException.java — из лаб. 4
├── repository/
│   ├── Repository.java               — обобщенный контракт Repository<T, ID>
│   └── InMemoryRepository.java       — реализация на LinkedHashMap
├── service/TransactionService.java   — задачи лабораторной
├── util/
│   ├── CollectionUtils.java          — generic-методы с PECS
│   ├── SampleData.java               — демонстрационные операции
│   └── TransactionGenerator.java      — генератор 100 000 операций
├── experiment/
│   ├── ListPerformanceExperiment.java   — ArrayList vs LinkedList
│   ├── SearchEfficiencyExperiment.java  — контрольная задача List vs Map
│   └── TypeErasureDemo.java             — стирание типов
└── check/SelfCheck.java              — автопроверка
```

---

## 🧠 4. Выбор структуры данных для каждой задачи

| Задача | Структура | Почему | Сложность |
|---|---|---|---|
| Хранение всех операций | `LinkedHashMap<UUID, Transaction>` внутри `InMemoryRepository` | Нужен быстрый доступ по id **и** предсказуемый порядок вывода (порядок добавления). `LinkedHashMap` дает и то и другое | `put/get/remove` — O(1) |
| Найти операцию по UUID | `Map<UUID, Transaction>` | Ключ → значение, поиск по хешу ключа без перебора всех операций | O(1) в среднем (`List` — O(n)) |
| Уникальные категории | `Set<Category>` (`LinkedHashSet`) | Множество само отбрасывает дубликаты по `equals/hashCode`. `LinkedHashSet` вместо `HashSet` — чтобы категории выводились в порядке первого появления | `add` — O(1) |
| Суммы по датам без Stream API | `TreeMap<LocalDate, BigDecimal>` | Ключи (даты) всегда отсортированы, отчет сразу выходит в хронологическом порядке | `get/put` — O(log n) |
| Удалить операцию | `Map.remove(id)` | Удаление по ключу без поиска по всему списку | O(1) (`ArrayList.remove` — O(n): поиск + сдвиг) |
| Отсортировать операции | копия в `ArrayList` + `List.sort(Comparator)` | Сортировка нужна только для вывода, хранилище не меняется. `ArrayList` — массив, сортируется быстро и без лишних ссылок | O(n log n) |
| Повторяющиеся категории | `HashSet` «уже видели» + `LinkedHashSet` результата | `Set.add()` возвращает `false`, если элемент уже был — значит категория повторилась. Один проход без вложенных циклов | O(n) (вложенные циклы по `List` — O(n²)) |

### Почему у `Transaction` нет `Comparable`
`Comparable` задает **один** естественный порядок. У финансовой операции его нет: в отчете за месяц
нужна сортировка по дате, при поиске крупных трат — по сумме, в отчете по категориям — по категории.
Если выбрать один из них «естественным», остальные все равно придется делать через `Comparator`, а выбор
окажется произвольным. Поэтому все порядки — отдельные компараторы в `TransactionComparators`:

```java
BY_DATE               = Comparator.comparing(Transaction::getDate);
BY_AMOUNT             = Comparator.comparing(Transaction::getAmount);
BY_CATEGORY_THEN_DATE = Comparator.comparing(Transaction::getCategory).thenComparing(Transaction::getDate);
```

А вот у `Category` естественный порядок есть — по алфавиту, поэтому она `implements Comparable<Category>`.
`compareTo` согласован с `equals`: оба сравнивают код категории.

### `equals` / `hashCode`
- `Transaction` равны, если совпадает `id` (это одна и та же операция, даже если ее сумму исправили).
- `Category` равны, если совпадает код (`"food"`, `" FOOD "` → `FOOD`).
- `hashCode` считается по тем же полям, что и `equals`. Иначе два «равных» объекта попадут в разные
  корзины `HashMap`/`HashSet`, и `Set` перестанет гарантировать уникальность.

---

## 🧩 5. Generics

```java
public interface Identifiable<ID> { ID getId(); }

public interface Repository<T, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
}

public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> { ... }
```

- Ограничение `T extends Identifiable<ID>` гарантирует, что у любого `T` есть `getId()`, поэтому
  репозиторий сам достает ключ из объекта.
- Один и тот же класс хранит и операции (`InMemoryRepository<Transaction, UUID>`), и кошельки
  (`InMemoryRepository<Wallet, String>`) — код хранилища больше не дублируется.
- `findById` возвращает `Optional<T>`: вызывающий код явно обрабатывает «не найдено» (`orElse`, `isPresent`),
  а не ловит `NullPointerException` от «магического null».

### PECS своими словами
**Producer Extends, Consumer Super.** Если коллекция *отдает* мне элементы (я из нее только читаю) —
пишу `? extends T`: туда подойдет список `T` или любых его наследников. Если коллекция *принимает*
элементы (я в нее только кладу) — пишу `? super T`: туда можно положить `T`, значит подойдет список `T`
или любого его предка.

```java
public static <T> void copyAll(Collection<? extends T> source, Collection<? super T> target)
```

Благодаря этому `copyAll(List<Transaction>, List<Object>)` и `copyAll(List<Transaction>, List<Identifiable<UUID>>)`
компилируются. С сигнатурой `copyAll(Collection<T>, Collection<T>)` — нет: `List<Transaction>` не является `List<Object>`.

### Стирание типов (type erasure)
```
strings.getClass() == integers.getClass(): true
Класс в runtime: java.util.ArrayList (без параметра типа)
```
- Параметры типа существуют только для компилятора. После компиляции `List<String>` и `List<Integer>` —
  один и тот же класс `ArrayList`, поэтому сравнение дает `true`.
- **Compile-time type safety:** `strings.add(42)` не скомпилируется — ошибка видна сразу, а не в runtime.
  При чтении компилятор сам вставляет каст `(String)`.
- **Почему нельзя `new T()`:** в runtime `T` стерт, JVM не знает, какой класс создавать и есть ли у него
  конструктор без параметров. Обходной путь — передать фабрику: `Supplier<T>` или `Class<T>`.
- **Почему нельзя raw types (`List list`):** они отключают проверку типов. В программе через raw type в
  `List<String>` удалось положить число — ошибка `ClassCastException` вылезла позже, при чтении,
  далеко от места, где был баг.
- Автопроверка показывает: даже через raw type объект `String` не попадает в `Repository<Transaction, UUID>` —
  срабатывает каст к `Identifiable` в мостовом методе `save(Object)`, который сгенерировал компилятор.

---

## 📊 6. Эксперимент ArrayList vs LinkedList

Каждый сценарий прогревается 2 раза, затем берется лучший из 3 замеров `System.nanoTime()`.
Результаты одного запуска (на другом компьютере числа будут другими):

| Сценарий | ArrayList | LinkedList | Теория (AL / LL) |
|---|---:|---:|---|
| 1. Последовательный проход 100 000 элементов | 0,28 мс | 1,18 мс | O(n) / O(n) |
| 2. 2 000 вызовов `get(i)` по случайным индексам | 0,21 мс | 106,53 мс | O(1) / O(n) на вызов |
| 3. 100 000 вызовов `add(e)` в конец | 1,77 мс | 1,96 мс | O(1) амортизированно / O(1) |
| 4. Вставка через `ListIterator.add()` после каждого из 20 000 элементов | 24,77 мс | 0,66 мс | O(n) / O(1) на вставку |

**Анализ:**
1. Оба прохода линейные, но `ArrayList` быстрее в разы: элементы массива лежат в памяти подряд и хорошо
   попадают в кеш процессора, а узлы `LinkedList` разбросаны по куче, и каждый переход — это переход по ссылке.
2. Главное отличие. `ArrayList.get(i)` вычисляет адрес элемента сразу, `LinkedList.get(i)` идет по узлам от
   ближайшего конца, в среднем n/4 шагов. Разница — в сотни раз.
3. Почти одинаково. `ArrayList` изредка копирует массив в новый (в 1,5 раза больше), но в среднем это O(1).
   `LinkedList` на каждый элемент создает объект-узел.
4. Единственный сценарий, где `LinkedList` выигрывает: итератор уже стоит в нужном месте, и вставка — это
   перевешивание двух ссылок. `ArrayList` на каждую вставку сдвигает весь «хвост» массива.

**Вывод:** по умолчанию выбираем `ArrayList`. `LinkedList` оправдан только при частых вставках/удалениях
в середине через итератор. Это не профессиональный бенчмарк (на время влияют JIT, прогрев, сборка мусора;
для точных измерений есть JMH), но порядок величин совпадает с теорией.

---

## 🔎 7. Контрольная задача: поиск по id среди 100 000 операций

```
List, полный проход: 1 000 поисков за   838,20 мс (найдено 1000)
HashMap: построение индекса    48,11 мс, 1 000 поисков за    0,382 мс (найдено 1000)
```

Для многократного поиска выбираем `Map<UUID, Transaction>`. Поиск в `List` перебирает элементы — в среднем
50 000 сравнений на поиск, O(n). `HashMap` один раз строится за O(n), а потом каждый поиск — это вычисление
хеша и переход в нужную корзину, O(1). Уже при 1 000 поисках `Map` быстрее даже с учетом построения.

---

## ✅ 8. Автопроверка (`check/SelfCheck`)

Запускается в конце `Main` (или отдельно через `SelfCheck.main`). Результат: **26 из 26** проверок.

- CRUD обобщенного репозитория: `save`, `findById`, `findAll`, обновление по тому же id, `deleteById`; тот же репозиторий для `Wallet`.
- Поиск отсутствующего id возвращает `Optional.empty()`.
- Объект неправильного типа в репозиторий не попадает (компилятор не пропускает, raw type → `ClassCastException`).
- Уникальность `Set` (`"FOOD"`, `"food"`, `" Food "` → одна категория).
- Контракт `equals/hashCode` (симметричность, рефлексивность, `null`, работа в `HashSet`).
- Корректность трех сортировок, группировки по датам и поиска повторяющихся категорий.
- PECS-методы `copyAll` и `totalAmount`.

---

## ❓ 9. Ответы на вопросы

**Чем отличаются List, Set, Map и когда применять каждую структуру?**
`List` — упорядоченная последовательность с индексами, допускает дубликаты (история операций).
`Set` — множество без дубликатов (уникальные категории). `Map` — пары «уникальный ключ → значение»
(поиск операции по id, суммы по датам).

**ArrayList vs LinkedList.**
`ArrayList` — динамический массив: `get(i)` O(1), добавление в конец O(1) амортизированно, вставка/удаление
в середине O(n) из-за сдвига. `LinkedList` — двусвязный список: `get(i)` O(n), вставка/удаление через итератор O(1),
но больше памяти на узлы и хуже работа с кешем. На практике почти всегда `ArrayList`.

**HashMap vs TreeMap, HashSet vs TreeSet.**
`HashMap`/`HashSet` — хеш-таблица: O(1) в среднем, порядок не гарантирован, нужны `equals/hashCode`.
`TreeMap`/`TreeSet` — красно-черное дерево: O(log n), элементы всегда отсортированы, нужен `Comparable`
или `Comparator`. Сортированные структуры дают упорядоченный обход и навигацию: `firstKey()`, `headMap()`,
`ceilingKey()` и т.д. — например, отчет по датам за период.

**Зачем нужны Comparable и Comparator?**
`Comparable` — один естественный порядок, заложенный в сам класс (`Category` — по алфавиту).
`Comparator` — внешний порядок, их может быть сколько угодно (`Transaction` — по дате, сумме, категории).

**Для чего нужны generics и какие у них ограничения?**
Generics дают проверку типов при компиляции и переиспользование кода без приведений типов
(`InMemoryRepository` для любых сущностей). Ограничения из-за type erasure: нельзя `new T()`, `new T[]`,
`instanceof List<String>`, нельзя использовать примитивы как параметр (`List<int>`), в runtime
`List<String>` и `List<Integer>` — один класс.

---

## ▶️ 10. Как запустить
Открыть проект `labs-university` в IntelliJ IDEA → модуль `laba-fifth` → запустить `Main`.
Нужен JDK 21+. Эксперименты с 100 000 операций занимают несколько секунд.
