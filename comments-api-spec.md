# Комментарии к событиям

Функциональность комментариев позволяет пользователям делиться своими впечатлениями о событиях, в которых они
участвовали. Комментарии модерируются автором и администратором.

## Модель комментария

```json
{
  "id": "long",
  "text": "string",
  "authorId": "long",
  "eventId": "long",
  "createdOn": "LocalDateTime",
  "updatedOn": "LocalDateTime"
}
```

### Создание комментария (`POST users/{userId}/comments?eventId={eventId}`)

- Пользователь может оставить комментарий **только если**:
  - Событие уже прошло
  - Пользователь **принял участие** в событии (его заявка была одобрена автором)
  - Нельзя комментировать предстоящие или чужие события без участия
  - Длина комментария не более 1000 символов

**Параметры (body):**

```json
{
  "text": "string"
}
```

**Ответ: `201 Created`**

```json
{
  "id": "long",
  "text": "string",
  "authorDto": {
    "id": "long",
    "name": "string"
  },
  "eventDto": {
    "id": "long",
    "name": "string"
  },
  "createdOn": "LocalDateTime"
}
```

### Изменение комментария (`PATCH users/{userId}/comments/{commentId}`)

- Только автор комментария может его редактировать
- Можно редактировать только текст
- Длина комментария не более 1000 символов

**Параметры (body):**

```json
{
  "text": "string"
}
```

**Ответ: `200 OK`**

```json
{
  "id": "long",
  "text": "string",
  "authorDto": {
    "id": "long",
    "name": "string"
  },
  "eventDto": {
    "id": "long",
    "name": "string"
  },
  "updatedOn": "LocalDateTime"
}
```

### Удаление комментария пользователем (`DELETE users/{userId}/comments/{commentId}`)

- Автор комментария может удалить свой комментарий

**Ответ:** `204 No Content`

### Удаление комментария администратором (`DELETE admin/comments/{commentId}`)

- Администратор может удалить любой комментарий

**Ответ:** `204 No Content`

### Получение комментария по ID (`GET admin/comments/{commentId}`)

- Доступно только администратору

**Ответ: `200 OK`**

```json
{
  "id": "long",
  "text": "string",
  "authorDto": {
    "id": "long",
    "name": "string"
  },
  "eventDto": {
    "id": "long",
    "name": "string"
  },
  "createdOn": "LocalDateTime"
}
```

### Получение комментариев по событию (`GET /events/{eventId}/comments`)

- Доступно любому пользователю
- Комментарии отсортированы по `createdOn DESC`

**Ответ: `200 Created`**

```json
[
  {
    "id": "long",
    "text": "string",
    "authorDto": {
      "id": "long",
      "name": "string"
    },
    "eventDto": {
      "id": "long",
      "name": "string"
    },
    "createdOn": "LocalDateTime"
  }
]
```

### Получение комментариев по пользователю (`GET user/{userId}/comments`)

- Доступно только автору комментариев
- Комментарии отсортированы по `createdOn DESC`

**Ответ: `200 Created`**

```json
[
  {
    "id": "long",
    "text": "string",
    "eventDto": {
      "id": "long",
      "name": "string"
    },
    "createdOn": "LocalDateTime"
  }
]
```

### Комментарии добавлены в `EventFullDto`