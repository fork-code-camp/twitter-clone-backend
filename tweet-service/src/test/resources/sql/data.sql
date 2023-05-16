ALTER SEQUENCE tweets_id_seq RESTART WITH 1;
ALTER SEQUENCE likes_id_seq RESTART WITH 1;

INSERT INTO tweets (creation_date, profile_id, text)
VALUES (now(), 'dummy-id', 'some text');