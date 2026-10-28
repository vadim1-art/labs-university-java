package repository;

import java.util.List;
import java.util.Optional;

/**
 * Обобщенный контракт хранилища.
 * Вместо отдельного TransactionRepository, WalletRepository ... — один интерфейс,
 * параметризованный типом сущности T и типом ее идентификатора ID.
 */
public interface Repository<T, ID> {

    /** Сохраняет сущность. Если сущность с таким id уже есть — заменяет ее. */
    void save(T entity);

    /** Optional вместо "магического null": вызывающий код обязан обработать случай "не найдено". */
    Optional<T> findById(ID id);

    List<T> findAll();

    /** @return true, если сущность была и удалена; false, если такого id не было */
    boolean deleteById(ID id);
}
