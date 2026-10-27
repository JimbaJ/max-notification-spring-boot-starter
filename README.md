# max-notification-spring-boot-starter

Spring Boot стартер для отправки уведомлений в мессенджер [MAX](https://max.ru) (`platform-api2.max.ru`).
Даёт один бин `MaxNotificationClient` с методом `send(chatId, text)` и настройки одной точкой.
Домена не знает: что и когда отправлять, решает приложение.

## Что это и зачем

- Автоконфигурация Spring Boot 3: ничего не нужно объявлять в `@Configuration` потребителя.
- Бин создаётся только при заданном токене; без токена бина нет, приложение стартует молча.
  Включенность проверяется тестом, а не «на глаз по логам».
- Таймауты заданы по умолчанию: 2000 мс на установку соединения, 5000 мс на ответ.
- Потребитель может подменить клиент своим бином: у точки создания стоит `@ConditionalOnMissingBean`.

## Требования

Java 21, Spring Boot 3.4.x (сборка тянет платформу `spring-boot-dependencies:3.4.2`).

## Сборка

    ./gradlew build

Артефакт — `build/libs/max-notification-spring-boot-starter-0.1.0.jar` (рядом `-sources` и `-javadoc`).

## Подключение

Публикация в JitPack — следующий шаг, поэтому пока подключение локальное:

    ./gradlew publishToMavenLocal

    repositories {
        mavenLocal()          // после настоящей публикации эта строка заменится на jitpack
    }

    dependencies {
        implementation("com.github.JimbaJ:max-notification-spring-boot-starter:0.1.0")
    }

После тега `0.1.0` координаты те же, а вместо `mavenLocal()` в репозитории добавляется
`maven { url = uri("https://jitpack.io") }`. Регистр в `com.github.JimbaJ` обязателен: JitPack
различает варианты написания логина, GitHub — нет. Тег называется `0.1.0`, без «v»: JitPack берёт
версию из имени тега, поэтому строка зависимости совпадает с версией в POM.

## Настройки

    max:
      notification:
        token: ${MAX_BOT_TOKEN}                 # пустой — клиента в контексте нет
        api-url: https://platform-api2.max.ru   # по умолчанию боевой адрес
        connect-timeout-ms: 2000
        read-timeout-ms: 5000

## Использование

    @Service
    class OrderNotifier {

        private final MaxNotificationClient client;

        OrderNotifier(MaxNotificationClient client) {
            this.client = client;
        }

        void orderPaid(String chatId, UUID orderId) {
            client.send(chatId, "Заказ " + orderId + " оплачен");
        }
    }

Адресат передаётся параметром запроса `chat_id`, текст — телом. Токен уходит только
в заголовке `Authorization`, без схемы Bearer. Бот не может написать первым: чат должен
быть открыт человеком заранее.

## Что пока не умеет

- разметку (`format`), клавиатуры `inline_keyboard`, ответ на нажатие и правку сообщения —
  план «отправка v1»;
- приём обновлений вебхуком (входящие сообщения и нажатия) — план «приём»;
- доверие корневому сертификату Минцифры — нужен при работе за прокси; будет добавлен
  вместе с интеграцией в сервис;
- метрики и health-индикатор.

## Лицензия

Apache License 2.0, см. [LICENSE](LICENSE).
