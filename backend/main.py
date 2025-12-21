from fastapi import FastAPI
from pydantic import BaseModel
import mysql.connector
from mysql.connector import Error

app = FastAPI()

# --- CẤU HÌNH DATABASE ---
db_config = {
    'host': '127.0.0.1',
    'database': 'ktpmud',
    'user': 'root',
    'password': '123'  # Pass của bạn
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
    score: float
    correct_count: int
    total_questions: int

# [MỚI] Model cho việc kết nối phụ huynh - con
class LinkRequest(BaseModel):
    parent_id: int
    student_username: str

# --- CÁC API ---

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
        # Test ID = 1 là bài luyện tập tự do
        sql = """INSERT INTO test_results (test_id, student_id, score, correct_count, total_questions) 
                 VALUES (1, %s, %s, %s, %s)"""
        val = (item.user_id, item.score, item.correct_count, item.total_questions)
        cursor.execute(sql, val)
        conn.commit()
        return {"status": "success", "message": "Đã lưu kết quả"}
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

@app.get("/dashboard/{user_id}")
def get_dashboard(user_id: int):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        sql = """SELECT COUNT(*) as total_games, SUM(total_questions) as total_questions, SUM(correct_count) as total_correct
                 FROM test_results WHERE student_id = %s"""
        cursor.execute(sql, (user_id,))
        result = cursor.fetchone()
        
        if result['total_games'] == 0:
             return {"status": "success", "data": {"total": 0, "correct": 0}}
        return {
            "status": "success", 
            "data": {
                "total": int(result['total_questions'] or 0),
                "correct": int(result['total_correct'] or 0)
            }
        }
    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

# [MỚI] API KẾT NỐI PHỤ HUYNH VỚI CON
@app.post("/link_student")
def link_student(item: LinkRequest):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)

        # 1. Tìm học sinh theo tên đăng nhập
        cursor.execute("SELECT user_id FROM users WHERE username = %s AND role = 'student'", (item.student_username,))
        student = cursor.fetchone()

        if not student:
            return {"status": "fail", "message": "Không tìm thấy tài khoản học sinh này!"}

        student_id = student['user_id']

        # 2. Cập nhật parent_id vào bảng students
        sql = "UPDATE students SET parent_id = %s WHERE user_id = %s"
        cursor.execute(sql, (item.parent_id, student_id))
        conn.commit()

        return {"status": "success", "message": f"Đã kết nối thành công với {item.student_username}"}

    except Error as e:
        return {"status": "error", "message": str(e)}
    finally:
        if 'conn' in locals() and conn.is_connected(): conn.close()

# [MỚI] API LẤY ID CỦA CON (Để xem dashboard)
@app.get("/get_my_child/{parent_id}")
def get_my_child(parent_id: int):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        # Lấy user_id của đứa con đầu tiên
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