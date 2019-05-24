# EDS
## Модуль подписания документов PDF для платформы CUBA

Модуль состоит из сервиса **PdfSigningService**, имеющего в свою очередь единственный метод **sign**.
Модуль построен на базе патченной (для ГОСТ Р 34.10-2012) библиотеки [itextpdf](https://github.com/itext/itextpdf) (версии 5.5.5). 

Имеет следующие конфигурационные настройки:
- Пароль хранилища ключей (key store).
- Алиас контейнера (если не указан, то будет использоваться первый).
- Пароль контейнера.

Для подписания необходимо следующее ПО:
1. [КриптоПро JCP](https://www.cryptopro.ru/products/csp/jcp)

_Убедитесь в том, что JCP установлен именно в ту версию JRE, которую использует 
ваше приложение._
2. [КриптоПро CSP](https://www.cryptopro.ru/products/csp)

Контейнер, содержащий закрытый ключ, должен быть экспортирован с токена в файловую систему, с помощью *КриптоПро CSP* в каталог, 
указанный в настройках *КриптоПро JCP*. По умолчанию для Windows это: *${user.home}\Local Settings\Application Data\Crypto Pro*

Для проверки подписи следующее ПО:
1. [Acrobat Reader](https://acrobat.adobe.com/ru/ru/acrobat/pdf-reader.html)
2. [КриптоПро CSP](https://www.cryptopro.ru/products/csp)
3. [КриптоПро PDF](https://www.cryptopro.ru/products/other/pdf)
