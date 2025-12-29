CREATE TABLE IF NOT EXISTS completed_levels (
    user_id INT,
    level_id INT,
    PRIMARY KEY (user_id, level_id)
);