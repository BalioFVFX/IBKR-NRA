![Banner](/preview/banner.png)
![Demo](/preview/demo.webp)

### Въведение

IBKR-NRA (Interactive Brokers - НАП) е софтуер, който улеснява попълването на годишната данъчна декларация към НАП.

Софтуерът генерира екселски таблици готови за попълване в портала на НАП.

Поддържани приложения:

* Приложение 8 (Притежание на акции)

* Приложение 8 (Дивиденти)

## Инструкции за Приложение 8 (Притежание на акции, ETF)

Необходимо е генерирането на `Flex query` за отворени позиции и направени сделки през платформата на Interactive Brokers.

### Генериране на файл за отворени позиции

* От Activity Flex Queries се създава ново Query.

* От секциите се избира само `Open Positions`.

* От `Options` се избира само `LOT`

* Останалите необходими полета са:

*  `Account ID`

*  `Currency`

*  `Symbol`

*  `ISIN`

*  `Listing Exchange`

*  `Quantity`

*  `Open Price`

*  `Cost Basis Price`

*  `Cost Basis Money`

*  `Level of Detail`

*  `Open Date Time`

*  `Originating Transaction ID`

* От секцията General Configurations необходимите промени са:

* Date Format: `dd/MM/yyyy`

* Time Format: `HH:mm:ss`

* Date Time Separator: `; (semi-colon)`

При изпълнението на Flex Query за Format се избира `CSV`, а за период `Custom Date Range`, като периодът трябва да бъде годината за която попълвате ГДД.

Пример за 2024: From Date: `2024-01-01` To Date: `2024-12-31`.

**Важно:** Ако регистрацията ви има няколко акаунта, то преди генерирането на файла посочете акаунтите си от бутона `Select account(s)` намиращ се на страницата Flex Queries.

### Генериране на файл за направени сделки (Trades)

* От Activity Flex Queries се създава ново Query.

* От секциите се избира само `Trades`.

* От `Options` се избира само `Execution`

Останалите необходими полета са:

* `Account ID`

* `Symbol`

* `ISIN`

* `Listing Exchange`

* `Quantity`

* `Trade Price`

* `IB Commision`

* `IB Commision Currency`

* `Net Cash`

* `Buy/Sell`

* `Transaction ID`

* `Date/Time`

* `Currency`
  
От секцията General Configurations необходимите промени са:

* Date Format: `dd/MM/yyyy`

* Time Format: `HH:mm:ss`

* Date Time Separator: `; (semi-colon)`

При изпълнението на Flex Query за Format се избира `CSV`, а за период `Custom Date Range`, като периода трябва да цяла календарна година.

Пример за 2024: From Date: `2024-01-01` To Date: `2024-12-31`.

Небходимо е да генерирате файлове за периода от най-старата отворена позиция в `Open positions` до най-новата. Максималният позволен период за всеки генериран файл е 365 дни, което означава че ако имате отворени позиции от 2020 до 2023 ще трябва да генерирате 3 отделни файла.

**Важно:** Ако регистрацията ви има няколко акаунта то преди генерирането на файла посочете акаунтите си от бутона `Select account(s)` намиращ се на страницата Flex Queries.

### Генериране на екселска таблица през приложението
От екрана за `Отворени позиции (Приложение 8)` добавете генерираните файлове и изберете `Конвентирай`. При успешно конвентиране се създава файл с името `result.xls` в директорията `exports`

## Инструкции за приложение 8 (Дивиденти)
Необходимо е генерирането на `Flex query` за отворени позиции и направени сделки през платформата на Interactive Brokers.

### Генериране на файл за отворени позиции

* От Activity Flex Queries се създава ново Query.

* От секциите се избира само `Cash Transactions`.

От `Options` се избира:

* `Dividends`
* `Withholding tax`
* `Detail`


* Останалите необходими полета са:

*  `Account ID`

*  `Currency`

*  `Symbol`

*  `Description`

*  `ISIN`

*  `Issuer Country Code`

*  `Date/Time`

*  `Amount`

*  `Type`

*  `Action ID`

* От секцията General Configurations необходимите промени са:

* Date Format: `dd/MM/yyyy`

* Time Format: `HH:mm:ss`

* Date Time Separator: `; (semi-colon)`

При изпълнението на Flex Query за Format се избира `CSV`, а за период `Custom Date Range`, като периодът трябва да бъде годината за която попълвате ГДД.

Пример за 2024: From Date: `2024-01-01` To Date: `2024-12-31`.

**Важно:** Ако регистрацията ви има няколко акаунта, то преди генерирането на файла посочете акаунтите си от бутона `Select account(s)` намиращ се на страницата Flex Queries.

### Генериране на екселска таблица през приложението
От екрана за `Дивиденти (Приложение 8)` добавете генерирания файл и изберете `Конвентирай`. При успешно конвентиране се създава файл с името `result.xls` в директорията `exports`

## Начин на работа

###  Конвентиране на валута към лев
Конвентирането става според курса на БНБ. Ако за дадената дата не бъде намерен курс, то програмата търси курс за следващ ден. Текущо поддържаните валути са `USD` и `EUR`

Използван курс за евро: `1.95583`

### Определяне на цена на придобиване (Приложение 8)
За определянето на цената на придобиване се използва чистата цена на придобиване (без комисиона, такси и др.) За всяка отверна позиция се взима `TransactionID` и се търси в `csv` файловете с направени сделки.  

### Определяне на държава (Приложение 8)
За всеки инструмент програмата търси държавата във файла `ticker_countries.csv`. Добавянето на допълнителни държави за инструменти става чрез добавянето на нов ред във формат `ticker symbol,държава`, например: `AAPL,САЩ`.

**Важно:** Програмата пропуска попълването на държавата ако тя не бъде намерена.

### Определяне на име на компания
За всеки получен дивидент програмата търси името на компанията във файла `company_names.csv`. Търсенето става посредством `SYMBOL` колоната от CSV файла, изтеглен от Interactive Brokers.

**Важно:** Когато името на компанията не може да бъде намерено, програмата го замества със `SYMBOL` колоната.

## Забележка
Ползването на този софтуер е изцяло Ваша отговорност. Авторът не носи отговорност за грешни данни произлезли от използването на софтуера или грешно попълнена годишна данъчна декларация.
