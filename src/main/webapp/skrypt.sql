-- Tworzenie tabel
CREATE TABLE "user"
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    surname       VARCHAR(100)        NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    is_blocked    BOOLEAN             NOT NULL DEFAULT false,
    role          VARCHAR(20)                  DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE bike
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100)   NOT NULL,
    type          VARCHAR(50)    NOT NULL,
    size          VARCHAR(10)    NOT NULL,
    available     BOOLEAN DEFAULT true,
    price_per_day NUMERIC(10, 2) NOT NULL,
    description   TEXT,
    image_url     VARCHAR(255)
);

CREATE TABLE favourite
(
    id       SERIAL PRIMARY KEY,
    user_id  INTEGER REFERENCES "user" (id) ON DELETE CASCADE,
    bike_id  INTEGER REFERENCES bike (id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, bike_id)
);

CREATE TABLE reservation
(
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES "user" (id) ON DELETE CASCADE,
    bike_id     INTEGER REFERENCES bike (id) ON DELETE CASCADE,
    start_date  DATE           NOT NULL,
    end_date    DATE           NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT valid_dates CHECK (end_date >= start_date)
);

CREATE TABLE notice_board
(
    id         SERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Użytkownicy (hasło to "password123" zahashowane)
INSERT INTO "user" (name, surname, email, password_hash, role)
VALUES ('Jan', 'Kowalski', 'jan@example.com', '$2a$10$xVV7tZR8RzK/Bj0lCJZK2O5v8QbZ0yxX9Q9L5X9Q9L5X9Q9L5X9Q9', 'USER'),
       ('Anna', 'Nowak', 'anna@example.com', '$2a$10$xVV7tZR8RzK/Bj0lCJZK2O5v8QbZ0yxX9Q9L5X9Q9L5X9Q9L5X9Q9', 'ADMIN');

-- Rowery
INSERT INTO bike (name, type, size, available, price_per_day, description, image_url)
VALUES ('Trek Marlin 7', 'MTB', 'M', true, 50.00, 'Świetny rower górski do jazdy w terenie', 'trek_marlin.jpg'),
       ('Specialized Sirrus', 'City', 'L', true, 35.00, 'Wygodny rower miejski', 'specialized_sirrus.jpg'),
       ('Cannondale Synapse', 'Road', 'S', true, 75.00, 'Szosowy rower dla wymagających', 'cannondale_synapse.jpg'),
       ('Scott Scale', 'MTB', 'XL', true, 60.00, 'Hardtail do cross-country', 'scott_scale.jpg');

-- Ulubione
INSERT INTO favourite (user_id, bike_id)
VALUES (1, 1),
       (1, 3),
       (2, 2);

-- Rezerwacje
INSERT INTO reservation (user_id, bike_id, start_date, end_date, total_price)
VALUES (1, 1, '2024-03-01', '2024-03-03', 100.00),
       (2, 3, '2024-03-05', '2024-03-07', 150.00);
-- Ogłoszenia
INSERT INTO notice_board (title, content)
VALUES ('Nowe rowery w ofercie!', 'Zapraszamy do wypożyczenia nowych rowerów górskich Trek Marlin 7'),
       ('Promocja weekendowa', 'W ten weekend 20% zniżki na wszystkie rowery szosowe'),
       ('Serwis rowerowy', 'Przypominamy o możliwości skorzystania z naszego serwisu rowerowego');