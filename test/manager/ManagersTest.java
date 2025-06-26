package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test // Проверка утилитарного класса при возврате экземпляра менеджера
    void shouldNotBeNullForTaskManager() {
        assertNotNull(Managers.getDefault());
    }

    @Test // Проверка утилитарного класса при возврате экземпляра менеджера истории
    void shouldNotBeNullForHistoryManager() {
        assertNotNull(Managers.getDefaultHistory());
    }

}
