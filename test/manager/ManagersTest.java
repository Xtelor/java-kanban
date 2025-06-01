package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test // Проверка утилитарного класса при возврате экземпляра менеджера
    public void shouldNotBeNullForTaskManager() {
        assertNotNull(Managers.getDefault());
    }

    @Test // Проверка утилитарного класса при возврате экземпляра менеджера истории
    public void shouldNotBeNullForHistoryManager() {
        assertNotNull(Managers.getDefaultHistory());
    }

}
