import os
from urllib.parse import urlparse
from fastapi import FastAPI
from pydantic import BaseModel
import mysql.connector
from mysql.connector import Error
from typing import List, Optional

app = FastAPI()

# --- CẤU HÌNH DATABASE (ĐÃ NÂNG CẤP ĐỂ CHẠY CLOUD & LOCAL) ---
# Tự động kiểm tra: Nếu có biến môi trường DB_URL (trên Render) thì dùng, không thì dùng localhost
DATABASE_URL = os.getenv("DB_URL")

if DATABASE_URL:
    # Cấu hình cho Render + Aiven (Chạy trên mạng)
    url = urlparse(DATABASE_URL)
    db_config = {
        'host': url.hostname,
        'port': url.port,
        'user': url.username,
        'password': url.password,
        'database': url.path[1:],  # Bỏ dấu / ở đầu tên db
        # 'ssl_disabled': False    # Bỏ comment dòng này nếu gặp lỗi SSL trên cloud
    }
else:
    # Cấu hình Localhost (Chạy ở nhà trên máy tính của bạn)
    db_config = {
        'host': '127.0.0.1',
        'database': 'ktpmud', # Tên database dưới máy bạn
        'user': 'root',
        'password': '123'     # Pass của bạn
    }

# --- CÁC MODEL DỮ LIỆU ---
class LoginRequest(BaseModel):
    username: str
    password: str

class RegisterRequest(BaseModel):
    username: str
    password: str
    full_name: str
    role: str = "student"

class GameResultRequest(BaseModel):
    user_id: int
    game_type: str      # VD: "ADDITION"
    level_id: int       # VD: 2001
    score: float
    correct_count: int
    total_questions: int

class LinkRequest(BaseModel):
    parent_id: int
    student_username: str

# --- CÁC API ---

@app.get("/")
def read_root():
    return {"message": "Math App API is running!"}

@app.post("/login")
def login(item: LoginRequest):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM users WHERE username = %s AND password = %s", (item.username, item.password))
        user = cursor.fetchone()
        
        if user:
            return {"status": "success", "data": user}
        else:
            return {"status": "fail", "message": "Sai tài khoản/mật khẩu"}
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.post("/register")
def register(item: RegisterRequest):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        # Check tồn tại
        cursor.execute("SELECT * FROM users WHERE username = %s", (item.username,))
        if cursor.fetchone():
            return {"status": "fail", "message": "Tên đăng nhập đã tồn tại"}
            
        # Insert User
        cursor.execute("INSERT INTO users (username, password, full_name, role) VALUES (%s, %s, %s, %s)", 
                       (item.username, item.password, item.full_name, item.role))
        conn.commit()
        new_id = cursor.lastrowid
        
        # Insert vào bảng con tương ứng
        if item.role == 'student': cursor.execute("INSERT INTO students (user_id) VALUES (%s)", (new_id,))
        elif item.role == 'parent': cursor.execute("INSERT INTO parents (user_id) VALUES (%s)", (new_id,))
        elif item.role == 'teacher': cursor.execute("INSERT INTO teachers (user_id) VALUES (%s)", (new_id,))
        
        conn.commit()
        return {"status": "success", "message": "Đăng ký thành công"}
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.post("/save_result")
def save_result(item: GameResultRequest):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        # 1. Lưu lịch sử kết quả
        sql_history = """INSERT INTO test_results (test_id, student_id, score, correct_count, total_questions) 
                         VALUES (1, %s, %s, %s, %s)"""
        val_history = (item.user_id, item.score, item.correct_count, item.total_questions)
        cursor.execute(sql_history, val_history)
        
        # 2. Mở khoá Map nếu điểm cao
        if item.score >= 5.0:
            sql_progress = "INSERT IGNORE INTO completed_levels (user_id, level_id) VALUES (%s, %s)"
            cursor.execute(sql_progress, (item.user_id, item.level_id))

        conn.commit()
        return {"status": "success", "message": "Đã lưu kết quả"}
    except Error as e:
        print(f"Error: {e}") 
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.get("/dashboard/{user_id}")
def get_dashboard(user_id: int):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        # 1. Lấy thống kê tổng quát
        sql_stats = """SELECT SUM(total_questions) as total_questions, SUM(correct_count) as total_correct
                       FROM test_results WHERE student_id = %s"""
        cursor.execute(sql_stats, (user_id,))
        stats = cursor.fetchone()
        
        # 2. Lấy danh sách Level đã hoàn thành
        cursor.execute("SELECT level_id FROM completed_levels WHERE user_id = %s", (user_id,))
        level_rows = cursor.fetchall()
        completed_list = [row['level_id'] for row in level_rows]

        total_q = int(stats['total_questions']) if stats and stats['total_questions'] else 0
        total_c = int(stats['total_correct']) if stats and stats['total_correct'] else 0

        return {
            "status": "success", 
            "data": {
                "total": total_q,
                "correct": total_c,
                "completed_levels": completed_list
            }
        }
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.post("/link_student")
def link_student(item: LinkRequest):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)

        # 1. Tìm học sinh
        cursor.execute("SELECT user_id FROM users WHERE username = %s AND role = 'student'", (item.student_username,))
        student = cursor.fetchone()

        if not student:
            return {"status": "fail", "message": "Không tìm thấy tài khoản học sinh này!"}

        student_id = student['user_id']

        # 2. Cập nhật parent_id
        sql = "UPDATE students SET parent_id = %s WHERE user_id = %s"
        cursor.execute(sql, (item.parent_id, student_id))
        conn.commit()

        return {"status": "success", "message": f"Đã kết nối thành công với {item.student_username}"}

    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.get("/get_my_child/{parent_id}")
def get_my_child(parent_id: int):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        sql = "SELECT user_id FROM students WHERE parent_id = %s LIMIT 1"
        cursor.execute(sql, (parent_id,))
        child = cursor.fetchone()
        
        if child:
            return {"status": "success", "data": child['user_id']}
        else:
            return {"status": "fail", "message": "Chưa kết nối với con nào"}
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
         if 'conn' in locals() and conn.is_connected(): conn.close()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

#uvicorn main:app --host 0.0.0.0 --port 8000 --reload