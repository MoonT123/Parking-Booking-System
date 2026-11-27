# Cài đặt cơ sở dữ liệu Parking Booking System

## Tạo database demo mới

Dùng MySQL Server 8.4 và chạy các tệp theo thứ tự dưới đây trong MySQL Workbench, DBeaver hoặc MySQL CLI. Dừng khi có lỗi. Tệp `01` xóa toàn bộ database `ParkingBookingSystem` nếu database đó đã tồn tại. Tệp `02` cũng xóa dữ liệu trước khi nạp seed.

| Thứ tự | Tệp | Mục đích |
| --- | --- | --- |
| 1 | `01_create_database.sql` | Tạo 12 bảng nghiệp vụ gốc |
| 2 | `02_sample_data.sql` | Nạp dữ liệu mẫu gốc |
| 3 | `04_transactions.sql` | Chạy ví dụ COMMIT và ROLLBACK trên dữ liệu thử |
| 4 | `05_indexes.sql` | Tạo chỉ mục tra cứu |
| 5 | `06_upgrade_dsa.sql` | Nâng schema lên 15 bảng và thêm view quá giờ |
| 6 | `07_dsa_demo.sql` | Tạo 3 bãi, 60 ô, bản đồ, biểu giá và tình huống demo |
| 7 | `03_queries.sql` | Chạy các báo cáo, gồm Q04 dựa trên liên kết lượt gửi xe |

## Nâng cấp database đang có dữ liệu

1. Sao lưu database và kiểm tra xem bản sao lưu có thể khôi phục. Tạm dừng các ứng dụng ghi dữ liệu trong lúc nâng cấp.
2. Chạy câu SELECT đầu tiên trong `06_upgrade_dsa.sql`. Xử lý các booking có chủ booking khác chủ xe theo dữ liệu thực trước khi tiếp tục.
3. Chạy toàn bộ `06_upgrade_dsa.sql` một lần. MySQL tự commit từng lệnh DDL. Nếu gặp lỗi giữa chừng, kiểm tra các bước đã áp dụng trước khi tiếp tục.
4. Kiểm tra tài khoản quản lý và phân vai cho nhân viên thực tế. Script nhận diện quản lý từ các chủ bãi hiện có. Các tài khoản khác mặc định là `CUSTOMER`.
5. Điền tầng, khu vực, khoảng cách và điểm bản đồ theo sơ đồ thực tế. Các ô cũ giữ giá trị NULL cho những trường chưa có dữ liệu. Lượt gửi xe cũ chỉ được liên kết tự động khi đúng một booking phù hợp bao phủ thời điểm vào bãi. Lượt đi thẳng và lượt cần xác minh có thể giữ `BookingID = NULL`.
6. Gắn biểu giá có hiệu lực cho các bãi sẽ nhận đặt chỗ. Chỉ chạy `07` trên database demo. Script đó tạo tài khoản mô phỏng, dữ liệu đặt chỗ và một biểu giá dự phòng cho các bãi mẫu đã biết.

## Quy tắc dùng trong Java

- Booking dùng khoảng nửa mở `[StartTime, EndTime)`. Hai lịch sát giờ được chấp nhận. Hai lịch `PENDING` hoặc `CONFIRMED` giao nhau trên cùng ô bị từ chối.
- Thêm và sửa booking khóa dòng ô đỗ, kiểm tra lịch rồi ghi trong cùng transaction JDBC. Các chương trình ghi booking khác cần dùng cùng giao thức khóa. File seed nạp các tình huống mẫu bằng SQL trực tiếp.
- Hủy cập nhật booking và ghi lý do trong cùng transaction. Hủy không tự xử lý hoàn tiền. `bay_status` mô tả trạng thái vận hành. Tra cứu ô cho một khung giờ dùng lịch booking và loại trừ ô bảo trì.
- Xe phải thuộc người đặt. Ma trận `VEHICLE_BAY_SIZE` định nghĩa độ phù hợp. Xe máy ưu tiên ô nhỏ, ô tô ưu tiên ô vừa, SUV và VAN dùng ô lớn. `ParkingBayDAO.getAvailableBays` có phiên bản lọc theo xe và khung giờ.
- Giá được tính theo từng đoạn thời gian, kể cả qua nửa đêm. Quy tắc đúng ngày trong tuần ưu tiên hơn `ALL`, tiếp theo là ngày hiệu lực mới nhất và mã biểu giá lớn nhất. Thiếu giá cho bất kỳ đoạn nào sẽ khiến yêu cầu bị từ chối. Dùng `24:00:00` làm mốc kết thúc ngày.
- Mỗi cạnh bản đồ là một hướng. Lối hai chiều cần hai cạnh. Trình chỉ đường bỏ qua cạnh bị chặn. Dữ liệu demo dùng mét và chi phí di chuyển giữa tầng. Nếu đồ thị có cạnh ngắn hơn khoảng cách hình học, dùng heuristic bằng 0 hoặc một cận dưới phù hợp để giữ tính tối ưu của A*.
- Khi ghi lượt vào bãi theo booking, truyền `BookingID` vào `ParkingLog`. Khóa ngoại yêu cầu mã xe và bãi trùng với booking. Dữ liệu quyền tài khoản phục vụ tầng đăng nhập và phân quyền của ứng dụng.

## Demo và benchmark

- Bãi đầu tiên `DSA Riverside` kín chỗ từ 09:00 đến 10:00 vào ngày sau khi nạp seed. Ô `1-01` có lịch tiếp theo từ 10:00 đến 11:00 và một lịch hủy giao với cả hai.
- Thử đặt hai yêu cầu giao nhau đồng thời cho cùng ô còn trống. Kết quả mong đợi là một booking thành công. Thử tiếp hai lịch nối nhau, thao tác hủy lặp, ô bảo trì, xe sai chủ, xe quá cỡ và lượt gửi xe có sai mã bãi.
- Tạo CSV benchmark bằng Python 3. Ví dụ `python sql/generate_benchmark_requests.py requests.csv --count 100000 --seed 42`. Dùng cùng CSV cho baseline và thuật toán tối ưu. Mỗi dòng là một yêu cầu khoảng giờ, có thể trùng với yêu cầu khác để kiểm tra khả năng phát hiện xung đột.
