Простенький бот, панинкек, в попытке закодить шото уникальное для плотва-чата.

Плюнул на все -- переписал на котлин.

Наговнил базовую архитектуру, воть:

есть PaninBot, который хавает через DI CommandsExecutor.
CommandsExecutor, в свою очередь, хавает список Actions.
Все Actions лежат в папке Actions.
Там же лежат Commands -- это когда бот потребляет команду через /
Там лежит WordTossAction, который просто рандомно переворачивает предложения.

Все на спрингбуте, чисто шоб из коробки шло тестирование и DI.

Для удобство наговнил асинхронную работу, через корутины и функциональщину.
Читать про это тут: https://kotlinlang.org/docs/reference/coroutines-overview.html
