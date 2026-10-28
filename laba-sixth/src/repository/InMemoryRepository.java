package repository;

import model.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Хранилище в памяти для любых сущностей с идентификатором.
 *
 * Ограничение T extends Identifiable<ID> говорит компилятору: у любого T есть getId(),
 * который возвращает именно ID. Поэтому save() может сам достать ключ из объекта.
 *
 * @param <T>  тип сущности (Transaction, Wallet ...)
 * @param <ID> тип идентификатора (UUID, String ...)
 */
public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    // LinkedHashMap: поиск, вставка и удаление по ключу за O(1), как у HashMap,
    // и вдобавок помнит порядок добавления — findAll() вернет операции в том порядке, в каком их сохранили.
    private final Map<ID, T> storage = new LinkedHashMap<>();

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "Нельзя сохранить null");
        ID id = Objects.requireNonNull(entity.getId(), "У сущности должен быть id");
        storage.put(id, entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        // Возвращаем копию: внешний код может сортировать или менять список, не ломая хранилище
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(ID id) {
        return storage.remove(id) != null;
    }
}
