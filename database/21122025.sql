
SELECT 
    student_id,
    test_id, -- (Giả sử test_id = 1 là Đếm số, 2 là Cộng...)
    AVG(score) as diem_trung_binh_mon,
    SUM(correct_count) as tong_cau_dung
FROM test_results
GROUP BY student_id, test_id;
-- Lệnh này sẽ biến bảng dữ liệu thô thành bảng thành tích chi tiết như Android
SELECT 
    CASE 
        WHEN test_id = 1 THEN 'Đếm số (Counting)'
        WHEN test_id = 2 THEN 'Phép cộng (Addition)'
        WHEN test_id = 3 THEN 'Phép trừ (Subtraction)'
        WHEN test_id = 4 THEN 'Tập viết (Writing)'
        ELSE 'Khác'
    END AS mon_hoc,
    COUNT(*) as so_lan_choi,
    AVG(score) as diem_trung_binh,
    SUM(correct_count) as tong_cau_dung
FROM test_results
WHERE student_id = 5  -- Thay số 5 bằng ID học sinh bạn muốn xem
GROUP BY test_id;test_results