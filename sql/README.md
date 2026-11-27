# HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY CƠ SỞ DỮ LIỆU – PARKING BOOKING SYSTEM

## 1. Yêu cầu môi trường (Prerequisites)
* **Hệ quản trị CSDL:** MySQL Server **8.0 trở lên** (khuyến nghị bản 8.x để hỗ trợ đầy đủ ràng buộc `CHECK` và `EXPLAIN FORMAT=TRADITIONAL`).
* **Công cụ quản trị (Client):** DBeaver (khuyến nghị bản 24.x–26.x), MySQL Workbench, hoặc MySQL CLI.
* **Bảng mã ký tự:** `utf8mb4` / `utf8mb4_unicode_ci`.

---

## 2. Cấu trúc thư mục & Thứ tự chạy bắt buộc (Execution Order)
Các script được thiết kế theo trình tự phụ thuộc khóa ngoại. Vui lòng chạy **đúng thứ tự từ 01 đến 05**:

1. **`01_create_database.sql`** *(Bắt buộc chạy đầu tiên)*
   * Xóa database cũ nếu đã tồn tại (`DROP DATABASE IF EXISTS ParkingBookingSystem`), tạo mới database `ParkingBookingSystem` và khởi tạo **12 bảng chuẩn BCNF** (`USER`, `USER_PHONENUMBER`, `VEHICLE`, `PARKINGLOT`, `PARKINGBAY`, `BOOKING`, `PARKINGLOGS`, `PENALTY`, `PAYMENT`, `CANCELLATION`, `TARIFF`, `APPLIESTARIF`) kèm đầy đủ khóa chính, khóa ngoại và ràng buộc toàn vẹn (`CHECK`, `UNIQUE`, `DEFAULT`).
2. **`02_sample_data.sql`** *(Chạy thứ 2)*
   * Nạp dữ liệu mẫu cho 12 bảng phục vụ kiểm thử và truy vấn.
3. **`03_queries.sql`** *(Chạy thứ 3)*
   * Thực thi các câu truy vấn nghiệp vụ, thống kê doanh thu, công suất bãi đỗ và kiểm tra lịch đặt chỗ.
4. **`04_transactions.sql`** *(Chạy thứ 4)*
   * Kiểm thử các thao tác DML (`INSERT`, `UPDATE`, `DELETE`) và 2 giao dịch ACID đa bảng (`COMMIT` đồng bộ `BOOKING` – `PAYMENT` – `PARKINGBAY`, và `ROLLBACK` hoàn tác trên `PARKINGBAY` – `PARKINGLOGS`).
   * *Lưu ý:* Script sử dụng bộ khóa thử nghiệm riêng (`tx_owner@parking.local`, `tx_driver@parking.local`, biển số `51G-999.99`) và tự động dọn dẹp dữ liệu test cũ ở đầu file nên **không xung đột** với `02_sample_data.sql`.
5. **`05_indexes.sql`** *(Chạy cuối cùng)*
   * Khởi tạo **8 Composite/Secondary Index** (`idx_...`) bằng thủ tục an toàn `CreateIndexIfNotExists` và kiểm chứng kế hoạch thực thi qua `EXPLAIN FORMAT=TRADITIONAL`.

---

## 3. Các bước thực thi trên DBeaver
1. Khởi động **MySQL Server** trên máy cá nhân.
2. Mở **DBeaver**, kết nối vào máy chủ MySQL cục bộ (`localhost` – đảm bảo hiển thị dấu tích xanh).
3. Mở lần lượt từng file `.sql` theo đúng thứ tự ở Mục 2 (**File** -> **Open File...**).
4. Trên thanh công cụ phía trên vùng soạn thảo SQL, kiểm tra kết nối đang chọn **`localhost`**.
5. Nhấn tổ hợp phím **`Option + X`** (trên macOS) hoặc **`Alt + X`** (trên Windows) để chạy toàn bộ script từ đầu đến cuối.
6. Kiểm tra tab **Statistics** ở khung kết quả phía dưới: nếu không hiện thông báo lỗi đỏ và toàn bộ các tab kết quả (`Grid`) hiển thị đầy đủ dữ liệu, bước đó đã hoàn tất.

---

## 4. Kiểm chứng tính tái lập (Idempotency) & Xử lý lỗi thường gặp
* **Chạy lại nhiều lần:** Toàn bộ các file `01_create_database.sql`, `04_transactions.sql` và `05_indexes.sql` đều đã được thiết kế để có thể nhấn **`Option + X` / `Alt + X` chạy lại từ đầu 2+ lần liên tiếp** mà không gặp lỗi trùng bảng, trùng dữ liệu test hay trùng chỉ mục (`SQL Error 1061` / `1553`).
* **Lỗi `No database selected (Error 1046)`:** Đảm bảo bạn chạy cả dòng `USE ParkingBookingSystem;` ở đầu mỗi file hoặc chọn sẵn `ParkingBookingSystem` trên thanh công cụ DBeaver trước khi chạy lẻ từng câu lệnh.
* **Lỗi `Cannot drop index: needed in a foreign key constraint (Error 1553)`:** Không tự ý chạy lệnh `DROP INDEX` thủ công trên các cột khóa ngoại; luôn dùng thủ tục kiểm tra có sẵn trong `05_indexes.sql`.
