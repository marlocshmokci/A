# Ayurones VM — 350 команд терминала

Полный каталог команд, доступных в `>_`.

Формат: `команда [аргументы]`

Команды выполняются из каталога гостевой среды. Часть команд вызывает системную утилиту Android/Toybox, поэтому вывод зависит от версии системы и доступных утилит.

## FS

1. `fs.pwd` — Показать текущую гостевую папку
2. `fs.list` — Показать файлы и каталоги
3. `fs.tree` — Показать дерево файлов
4. `fs.stat` — Показать сведения о файлах
5. `fs.mkdir` — Создать каталог
6. `fs.touch` — Создать пустой файл
7. `fs.read` — Прочитать файл
8. `fs.head` — Показать начало файла
9. `fs.tail` — Показать конец файла
10. `fs.count` — Подсчитать строки/слова/байты
11. `fs.grep` — Найти текст в файлах
12. `fs.find` — Найти файлы
13. `fs.size` — Показать размеры
14. `fs.disk` — Показать место в файловой системе
15. `fs.copy` — Скопировать файл
16. `fs.move` — Переместить файл
17. `fs.remove` — Удалить файл или каталог
18. `fs.rmdir` — Удалить пустой каталог
19. `fs.basename` — Получить имя файла
20. `fs.dirname` — Получить каталог пути
21. `fs.link` — Прочитать ссылку
22. `fs.sort` — Сортировать строки
23. `fs.uniq` — Убрать повторяющиеся строки
24. `fs.cut` — Вырезать поля/символы
25. `fs.translate` — Заменить или удалить символы

## TEXT

26. `text.echo` — Вывести текст
27. `text.printf` — Форматированный вывод
28. `text.sed` — Потоковое редактирование текста
29. `text.awk` — Обработка текста по полям
30. `text.grep` — Поиск по тексту
31. `text.head` — Первые строки
32. `text.tail` — Последние строки
33. `text.lines` — Нумерованные строки
34. `text.fold` — Перенос длинных строк
35. `text.format` — Форматирование абзацев
36. `text.reverse` — Развернуть строки
37. `text.translate` — Преобразовать символы
38. `text.columns` — Выбрать колонки
39. `text.paste` — Объединить строки
40. `text.join` — Соединить таблицы по ключу
41. `text.diff` — Сравнить текст
42. `text.compare` — Сравнить байты
43. `text.strings` — Извлечь читаемые строки
44. `text.encode` — Кодировать Base64
45. `text.md5` — Посчитать MD5
46. `text.sha256` — Посчитать SHA-256
47. `text.args` — Передать аргументы пакетно
48. `text.repeat` — Повторить вывод
49. `text.sequence` — Создать последовательность
50. `text.calculate` — Простое выражение

## SYS

51. `sys.id` — Информация о UID/GID
52. `sys.uname` — Информация о ядре
53. `sys.props` — Системные свойства гостя
54. `sys.date` — Текущая дата и время
55. `sys.uptime` — Время работы процесса
56. `sys.processes` — Список процессов
57. `sys.whoami` — Текущий пользователь
58. `sys.env` — Переменные окружения
59. `sys.printenv` — Показать переменные
60. `sys.cwd` — Текущий путь
61. `sys.uid` — UID процесса
62. `sys.gid` — GID процесса
63. `sys.config` — Сведения о конфигурации
64. `sys.storage` — Сводка хранилища
65. `sys.mounts` — Список подключений
66. `sys.kernel` — Версия ядра
67. `sys.cpu` — Информация о CPU
68. `sys.memory` — Информация о памяти
69. `sys.timers` — Сведения о времени работы
70. `sys.load` — Средняя нагрузка
71. `sys.status` — Статус текущего процесса
72. `sys.devices` — Список устройств
73. `sys.sysfs` — Обзор sysfs
74. `sys.procfs` — Обзор procfs
75. `sys.toybox` — Версия встроенных утилит

## PROC

76. `proc.list` — Список процессов
77. `proc.top` — Сводка процессов
78. `proc.pidof` — Найти PID по имени
79. `proc.kill` — Отправить сигнал процессу
80. `proc.killall` — Остановить процессы по имени
81. `proc.jobs` — Список фоновых задач
82. `proc.pgrep` — Поиск PID
83. `proc.pkill` — Сигнал процессам по имени
84. `proc.wait` — Ожидание процесса
85. `proc.sleep` — Пауза
86. `proc.nice` — Запустить с приоритетом
87. `proc.renice` — Изменить приоритет
88. `proc.chrt` — Параметры планировщика
89. `proc.taskset` — Маска CPU
90. `proc.time` — Измерить время команды
91. `proc.timeout` — Ограничить время команды
92. `proc.nohup` — Запуск без терминала
93. `proc.setsid` — Новая сессия
94. `proc.shell` — Запустить POSIX shell
95. `proc.ash` — Запустить ash
96. `proc.mksh` — Запустить mksh
97. `proc.true` — Всегда успешный код
98. `proc.false` — Всегда код ошибки
99. `proc.test` — Проверка условия
100. `proc.expr` — Вычислить выражение

## NET

101. `net.interfaces` — Сетевые интерфейсы
102. `net.routes` — Таблица маршрутов
103. `net.sockets` — Список сокетов
104. `net.ping` — Проверка доступности узла
105. `net.dns` — Проверка DNS
106. `net.http` — HTTP-запрос
107. `net.download` — Загрузка по URL
108. `net.netstat` — Сетевая статистика
109. `net.ifconfig` — Сведения об интерфейсах
110. `net.hostname` — Имя узла
111. `net.route` — Маршруты
112. `net.arp` — ARP-таблица
113. `net.tcp` — TCP-соединения
114. `net.udp` — UDP-соединения
115. `net.dev` — Сетевые устройства
116. `net.wifi` — Свойства Wi‑Fi в системе
117. `net.dnsprops` — DNS-свойства
118. `net.connectivity` — Сведения о connectivity
119. `net.proxy` — Сведения о proxy
120. `net.gateway` — Поиск шлюза
121. `net.addresses` — Адреса хоста
122. `net.neighbors` — Соседи сети
123. `net.stats` — Статистика интерфейсов
124. `net.tcp6` — IPv6 TCP-сокеты
125. `net.udp6` — IPv6 UDP-сокеты

## ARCH

126. `arch.tar` — Работа с TAR-архивом
127. `arch.gzip` — Сжать gzip
128. `arch.gunzip` — Распаковать gzip
129. `arch.zip` — Создать ZIP
130. `arch.unzip` — Распаковать ZIP
131. `arch.cpio` — Работа с CPIO
132. `arch.xz` — Сжатие XZ
133. `arch.bzip2` — Сжатие bzip2
134. `arch.bzcat` — Читать bzip2
135. `arch.zcat` — Читать gzip
136. `arch.base64` — Base64 кодирование
137. `arch.md5` — MD5 контрольная сумма
138. `arch.sha256` — SHA-256 контрольная сумма
139. `arch.cmp` — Сравнить файлы
140. `arch.diff` — Сравнить каталоги/файлы
141. `arch.split` — Разделить файл
142. `arch.cat` — Склеить/прочитать байты
143. `arch.dd` — Копировать блоки данных
144. `arch.od` — Показать восьмеричный дамп
145. `arch.hexdump` — Показать hex-дамп
146. `arch.strings` — Извлечь строки
147. `arch.file` — Определить тип файла
148. `arch.stat` — Метаданные файла
149. `arch.du` — Размеры каталогов
150. `arch.packlist` — Список файлов для упаковки

## USER

151. `user.whoami` — Имя текущего пользователя
152. `user.id` — UID и группы
153. `user.groups` — Группы процесса
154. `user.env` — Переменные окружения
155. `user.printenv` — Вывести окружение
156. `user.umask` — Текущая маска прав
157. `user.home` — Домашний путь процесса
158. `user.files` — Содержимое guest/files
159. `user.cache` — Содержимое guest/cache
160. `user.tmp` — Содержимое guest/tmp
161. `user.config` — Содержимое guest/config
162. `user.data` — Содержимое guest/data
163. `user.logs` — Содержимое guest/logs
164. `user.shared` — Содержимое guest/shared
165. `user.permissions` — Права текущего каталога
166. `user.owner` — Владелец текущего каталога
167. `user.numericid` — Числовой UID
168. `user.numericgid` — Числовой GID
169. `user.groupsid` — Числовые группы
170. `user.shell` — Текущая оболочка
171. `user.path` — PATH
172. `user.lang` — Язык среды
173. `user.timezone` — Часовой пояс
174. `user.locale` — Locale
175. `user.limits` — Ограничения процесса

## PACKAGE

176. `package.list` — Список гостевых пакетов (если доступен)
177. `package.path` — Путь пакета
178. `package.info` — Информация о пакете
179. `package.install` — Установка APK в гостевую среду
180. `package.uninstall` — Удаление гостевого пакета
181. `package.clear` — Очистка данных пакета
182. `package.enable` — Включение пакета
183. `package.disable` — Отключение пакета
184. `package.resolve` — Разрешение пакета
185. `package.features` — Функции Android package manager
186. `package.permissions` — Разрешения пакетов
187. `package.users` — Пользователи пакетного менеджера
188. `package.libraries` — Библиотеки
189. `package.instrumentation` — Инструментирование
190. `package.services` — Сервисы пакета
191. `package.receivers` — Broadcast receivers
192. `package.providers` — Content providers
193. `package.activities` — Activities
194. `package.apex` — APEX-модули
195. `package.system` — Системные пакеты
196. `package.thirdparty` — Сторонние пакеты
197. `package.uid` — Пакеты по UID
198. `package.installer` — Источник установки
199. `package.verify` — Состояние package manager
200. `package.help` — Справка package manager

## VM

201. `vm.info` — Показать информацию гостевой среды
202. `vm.version` — Показать выбранную версию Android
203. `vm.status` — Показать состояние подготовки
204. `vm.storage` — Размер гостевого каталога
205. `vm.files` — Список гостевой системы
206. `vm.reset` — Подготовить чистый каталог
207. `vm.home` — Открыть домашнюю структуру
208. `vm.system` — Показать системные файлы
209. `vm.markers` — Показать маркеры среды
210. `vm.uid` — Показать UID гостя
211. `vm.sandbox` — Показать корень sandbox
212. `vm.arch` — Архитектура гостя
213. `vm.api` — Показать Android API хоста
214. `vm.device` — Модель устройства
215. `vm.brand` — Бренд устройства
216. `vm.release` — Версия Android хоста
217. `vm.abis` — Поддерживаемые ABI
218. `vm.kernel` — Версия ядра
219. `vm.props` — Гостевые свойства
220. `vm.environment` — Переменные гостя
221. `vm.process` — PID текущей оболочки
222. `vm.cwd` — Текущая папка гостя
223. `vm.disk` — Место гостя
224. `vm.memory` — Память процесса
225. `vm.diagnose` — Быстрая диагностика

## DEV

226. `dev.debug` — Быстрый debug-вывод
227. `dev.trace` — Тест трассировки
228. `dev.env` — Окружение разработчика
229. `dev.props` — Android properties
230. `dev.logs` — Локальный logcat
231. `dev.logclear` — Очистить доступный log buffer
232. `dev.shell` — Проверить оболочку
233. `dev.commands` — Проверить каталог команд
234. `dev.path` — Пути утилит
235. `dev.version` — Версии основных утилит
236. `dev.java` — Версия Java внутри окружения
237. `dev.python` — Проверить Python
238. `dev.node` — Проверить Node.js
239. `dev.perl` — Проверить Perl
240. `dev.ruby` — Проверить Ruby
241. `dev.git` — Проверить Git
242. `dev.sqlite` — Проверить SQLite
243. `dev.openssl` — Проверить OpenSSL
244. `dev.busybox` — Проверить BusyBox
245. `dev.toybox` — Проверить Toybox
246. `dev.which` — Найти исполняемый файл
247. `dev.whereis` — Найти программу
248. `dev.command` — Проверить shell command
249. `dev.alias` — Список shell aliases
250. `dev.builtins` — Список shell builtins

## DATA

251. `data.cat` — Прочитать данные
252. `data.head` — Начало данных
253. `data.tail` — Конец данных
254. `data.wc` — Размер текста
255. `data.sort` — Сортировка
256. `data.uniq` — Уникальные строки
257. `data.cut` — Выбор полей
258. `data.tr` — Замена символов
259. `data.sed` — Редактирование потока
260. `data.awk` — Анализ полей
261. `data.grep` — Фильтрация
262. `data.paste` — Объединение колонок
263. `data.join` — Соединение таблиц
264. `data.split` — Разбиение файла
265. `data.base64` — Base64
266. `data.md5` — MD5
267. `data.sha256` — SHA-256
268. `data.od` — Дамп данных
269. `data.strings` — Строки из бинарных данных
270. `data.cmp` — Побайтовое сравнение
271. `data.diff` — Сравнение содержимого
272. `data.json` — Фильтр JSON через системные инструменты
273. `data.csv` — Просмотр CSV
274. `data.hex` — HEX-представление
275. `data.bytes` — Просмотр байтов

## TIME

276. `time.now` — Текущее время
277. `time.date` — Дата
278. `time.clock` — Часы с секундами
279. `time.iso` — ISO-время
280. `time.unix` — Unix timestamp
281. `time.zone` — Часовой пояс
282. `time.offset` — Смещение часового пояса
283. `time.year` — Текущий год
284. `time.month` — Текущий месяц
285. `time.day` — Текущий день
286. `time.weekday` — День недели
287. `time.week` — Номер недели
288. `time.uptime` — Uptime
289. `time.sleep1` — Пауза 1 секунда
290. `time.sleep5` — Пауза 5 секунд
291. `time.sleep10` — Пауза 10 секунд
292. `time.timer` — Секундомер shell
293. `time.epochms` — Epoch в миллисекундах
294. `time.calendar` — Календарь месяца
295. `time.measure` — Измерить команду
296. `time.deadline` — Ограничить время
297. `time.elapsed` — Показать uptime-счётчик
298. `time.monotonic` — Монотонный счётчик
299. `time.boot` — Время загрузки по uptime
300. `time.timezonefile` — Проверить timezone data

## MATH

301. `math.add` — Сложение
302. `math.subtract` — Вычитание
303. `math.multiply` — Умножение
304. `math.divide` — Деление
305. `math.mod` — Остаток деления
306. `math.compare` — Сравнение чисел
307. `math.max` — Максимум через awk
308. `math.min` — Минимум через awk
309. `math.sum` — Сумма через awk
310. `math.avg` — Среднее через awk
311. `math.abs` — Абсолютное значение
312. `math.round` — Округление через awk
313. `math.sqrt` — Квадратный корень
314. `math.pow` — Степень
315. `math.sin` — Синус
316. `math.cos` — Косинус
317. `math.tan` — Тангенс
318. `math.log` — Логарифм
319. `math.exp` — Экспонента
320. `math.floor` — Округление вниз
321. `math.ceil` — Округление вверх
322. `math.random` — Случайное число
323. `math.hex` — Число в hex
324. `math.octal` — Число в octal
325. `math.binary` — Двоичное представление

## DIAG

326. `diag.summary` — Общая диагностика
327. `diag.cpu` — Диагностика CPU
328. `diag.memory` — Диагностика памяти
329. `diag.process` — Диагностика процесса
330. `diag.fds` — Открытые дескрипторы
331. `diag.mounts` — Проверка mount
332. `diag.storage` — Проверка диска
333. `diag.guest` — Проверка гостевого дерева
334. `diag.system` — Проверка system tree
335. `diag.vendor` — Проверка vendor tree
336. `diag.dev` — Проверка устройств
337. `diag.proc` — Проверка procfs
338. `diag.sys` — Проверка sysfs
339. `diag.props` — Проверка properties
340. `diag.network` — Проверка сети
341. `diag.dns` — Проверка DNS props
342. `diag.battery` — Состояние батареи
343. `diag.display` — Сведения о дисплее
344. `diag.activity` — Сведения ActivityManager
345. `diag.package` — Сведения PackageManager
346. `diag.services` — Список сервисов
347. `diag.selinux` — Состояние SELinux
348. `diag.security` — Security properties
349. `diag.permissions` — Проверка доступа
350. `diag.health` — Быстрый health-check
