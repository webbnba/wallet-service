# Wallet Service

## Описание
Wallet Service - это микросервис для управления кошельками пользователей, включая операции по пополнению и снятию средств. Он использует Spring Boot, Spring Data R2DBC для работы с PostgreSQL, Spring Data Redis для кэширования и Nginx для балансировки нагрузки.

## Функционал
- Создание кошельков
- Получение баланса кошелька
- Пополнение кошелька
- Снятие средств с кошелька

## Стек технологий
- Java 17
- Spring Boot
- Spring Webflux
- Spring Data R2DBC
- PostgreSQL
- Redis
- Docker
- Nginx

## Требования
- Docker
- Docker Compose

## API

### Получение баланса кошелька
- **URL:** `/api/v1/wallets/{walletId}`
- **Метод:** `GET`
- **Пример запроса:**
  ```sh
  curl -X GET http://localhost/api/v1/wallets/91767858-603d-42de-a4c4-cd2e69298c19

### Пополнение/Снятие денег с кошелька
- **URL:** `/api/v1/wallets`
- **Метод:** `PUT`
- **Тело запроса (JSON):**
{
  "id": "91767858-603d-42de-a4c4-cd2e69298c19",
  "operationType": "DEPOSIT",
  "amount": 1000
}
- **Пример запроса:**
  ```sh
  curl -X PUT http://localhost/api/v1/wallets -H "Content-Type: application/json" -d '{"id": "91767858-603d-42de-a4c4-cd2e69298c19", "operationType": "DEPOSIT", "amount": 1000}'




