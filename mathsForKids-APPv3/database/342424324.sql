CREATE TABLE game_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    game_id INT NOT NULL,  -- 1: Đếm số, 2: Cộng, 3: Trừ, 4: Tập viết
    current_level INT DEFAULT 1,
    FOREIGN KEY (student_id) REFERENCES students(user_id)
);