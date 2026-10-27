# SplitMoney – Ứng dụng chia tiền nhóm (Java Swing)

## 1. Giới thiệu

**SplitMoney** là ứng dụng desktop viết bằng **Java Swing** giúp một nhóm bạn (đi chơi, ăn uống, ở trọ chung) ghi lại các khoản chi và biết ngay **ai nợ ai bao nhiêu**, cần **chuyển tiền cho ai** để mọi người về 0.

**Bài toán giải quyết:** một người thường ứng tiền trước cho cả nhóm; cuối buổi việc tính tay dễ nhầm và mất thời gian.

**Đặc điểm**
- Toàn bộ code bằng Java, cả giao diện lẫn tính toán.
- Không cần cơ sở dữ liệu, không cần cài thư viện ngoài. Chỉ cần JDK 17 trở lên.
- Dữ liệu tự lưu ra một file văn bản, mở lại ứng dụng vẫn còn.
- Mọi phép tính tiền dùng số nguyên (đồng), không bao giờ lệch 1 đồng.

## 2. Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 trở lên |
| Giao diện | Java Swing (có sẵn trong JDK) |
| Lưu dữ liệu | File văn bản `splitmoney-data.txt` |
| Kiểm thử | Chương trình test thuần Java (`CalculatorTests`), không cần JUnit |
| Build | `javac` / `java` (hoặc bấm Run trong IntelliJ, Eclipse, VS Code) |

## 3. Tính năng

Ứng dụng có một cửa sổ chính với 3 tab.

### Tab "Thành viên"
| Tính năng | Mô tả |
|---|---|
| Thêm thành viên | Nhập tên, bấm "Thêm". Tên không được rỗng và không trùng (không phân biệt hoa thường). |
| Xóa thành viên | Chỉ xóa được khi người đó chưa tham gia khoản chi hoặc thanh toán nào. |

### Tab "Khoản chi"
| Tính năng | Mô tả |
|---|---|
| Thêm khoản chi | Nhập mô tả, số tiền, chọn người trả và tick những người tham gia, bấm "Thêm". Tiền được **chia đều** cho những người tham gia. |
| Danh sách khoản chi | Bảng hiển thị mô tả, người trả, số tiền, những người tham gia. |
| Xóa khoản chi | Chọn một dòng rồi bấm "Xóa". Muốn sửa thì xóa và thêm lại. |

### Tab "Tổng kết"
| Tính năng | Mô tả |
|---|---|
| Bảng số dư | Mỗi người: đã trả, phải chịu, số dư. Số dư dương (xanh) là được nhận, âm (đỏ) là đang nợ. |
| Gợi ý thanh toán | Danh sách "A chuyển cho B: X đ" để cả nhóm về 0, với tối đa (n − 1) lần chuyển. |
| Ghi nhận "Đã thanh toán" | Chọn một gợi ý rồi bấm nút để ghi nhận khoản đã chuyển; số dư cập nhật ngay. |
| Lịch sử thanh toán | Danh sách các lần đã ghi nhận, có nút "Hủy" nếu ghi nhầm. |
| Sao chép tổng kết | Đưa nội dung tổng kết vào clipboard để dán vào Zalo/Messenger. |

### Thanh trên cùng
| Tính năng | Mô tả |
|---|---|
| Tên nhóm | Ô nhập tên nhóm, hiển thị trong nội dung tổng kết. |
| Nhóm mới | Xóa toàn bộ dữ liệu để bắt đầu nhóm khác (có hỏi xác nhận). |

## 4. Quy tắc và cách tính

### Quy tắc nhập liệu
1. Tên thành viên không rỗng, không trùng.
2. Số tiền là số nguyên dương (người dùng có thể gõ `100.000` hoặc `100000`).
3. Mỗi khoản chi có ít nhất 1 người tham gia.
4. Người trả phải là thành viên của nhóm.
5. Thanh toán: người chuyển khác người nhận, số tiền dương.

### Ba phép tính cốt lõi
1. **Chia đều:** mỗi người chịu `số tiền / số người` (chia nguyên). Phần dư `r = số tiền % số người` được chia thêm **1 đồng** cho `r` người đầu tiên trong danh sách tham gia. Tổng luôn đúng bằng số tiền gốc.
   Ví dụ: 100.000đ chia 3 người → 33.334 / 33.333 / 33.333.
2. **Số dư mỗi người:** `đã trả − phải chịu + đã chuyển đi − đã nhận về`. Dương là được nhận, âm là đang nợ. Tổng số dư cả nhóm luôn bằng 0.
3. **Gợi ý ai chuyển cho ai:** lấy người nợ đầu tiên ghép với người được nhận đầu tiên, chuyển số tiền nhỏ hơn trong hai số, lặp lại đến hết.

Số dư **không lưu** vào file, luôn tính lại từ khoản chi và thanh toán, nên xóa hay thêm khoản chi không bao giờ làm lệch số liệu.

### Ví dụ kiểm chứng
Nhóm An, Bình, Chi, Dũng. An trả 400.000 tiền ăn chia đều 4 người; Bình trả 200.000 tiền cà phê chia đều 4 người.

| Người | Đã trả | Phải chịu | Số dư |
|---|---|---|---|
| An | 400.000 | 150.000 | +250.000 |
| Bình | 200.000 | 150.000 | +50.000 |
| Chi | 0 | 150.000 | −150.000 |
| Dũng | 0 | 150.000 | −150.000 |

Gợi ý: Chi → An 150.000đ; Dũng → An 100.000đ; Dũng → Bình 50.000đ.

## 5. Cấu trúc thư mục và vai trò từng file

```text
splitmoney/
├── README.md
├── PROJECT.md                        (tài liệu này)
├── .gitignore
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── Member.java
│   │   ├── Expense.java
│   │   ├── Settlement.java
│   │   ├── Transfer.java
│   │   └── GroupData.java
│   ├── logic/
│   │   ├── SplitCalculator.java
│   │   ├── BalanceCalculator.java
│   │   └── SettlementCalculator.java
│   ├── storage/
│   │   └── DataStore.java
│   └── ui/
│       ├── MainFrame.java
│       ├── MembersPanel.java
│       ├── ExpensesPanel.java
│       ├── SummaryPanel.java
│       └── Money.java
└── test/
    └── CalculatorTests.java
```

### 5.1. Gốc dự án
| File | Vai trò |
|---|---|
| `README.md` | Hướng dẫn ngắn: yêu cầu cài đặt, cách biên dịch và chạy. |
| `PROJECT.md` | Mô tả dự án (tài liệu này). |
| `.gitignore` | Loại khỏi Git các file sinh ra khi chạy: `out/`, `*.class`, `splitmoney-data.txt`, thư mục IDE. |

### 5.2. `Main.java` – điểm khởi động
Đặt giao diện theo hệ điều hành (`UIManager`), đọc dữ liệu đã lưu bằng `DataStore`, tạo `MainFrame` và hiển thị trên luồng giao diện (`SwingUtilities.invokeLater`). Không chứa logic nghiệp vụ.

### 5.3. `model/` – dữ liệu (chỉ chứa dữ liệu, không có giao diện)
| File | Nội dung | Phục vụ tính năng |
|---|---|---|
| `Member.java` | Thành viên: `id`, `name`. | Tab Thành viên |
| `Expense.java` | Khoản chi: `id`, `title`, `amount`, `paidBy`, danh sách `participantIds`. | Tab Khoản chi |
| `Settlement.java` | Một lần chuyển tiền **đã xảy ra**: `id`, `fromId`, `toId`, `amount`. | "Đã thanh toán", lịch sử thanh toán |
| `Transfer.java` | Một gợi ý chuyển tiền **chưa xảy ra**: `fromId`, `toId`, `amount`. | Gợi ý thanh toán |
| `GroupData.java` | Gói toàn bộ dữ liệu của nhóm: tên nhóm, danh sách thành viên, khoản chi, thanh toán, bộ đếm id. Có các hàm tiện ích: thêm/xóa, tìm thành viên theo id, kiểm tra thành viên đã được dùng chưa. | Mọi tab, lưu/đọc file |

### 5.4. `logic/` – tính toán (Java thuần, không dùng Swing, không đọc/ghi file)
| File | Nhiệm vụ | Phục vụ tính năng |
|---|---|---|
| `SplitCalculator.java` | `splitEqually(amount, participantIds)`: chia đều, xử lý số dư lẻ, trả về số tiền từng người phải chịu. | Thêm khoản chi, bảng số dư |
| `BalanceCalculator.java` | `calculate(GroupData)`: tính số dư từng người từ khoản chi (dùng `SplitCalculator`) và thanh toán đã ghi nhận. Cũng cung cấp "đã trả" và "phải chịu" để hiển thị. | Bảng số dư |
| `SettlementCalculator.java` | `suggest(balances)`: từ số dư, trả về danh sách `Transfer` gợi ý ai chuyển cho ai. | Gợi ý thanh toán |

### 5.5. `storage/` – lưu trữ
| File | Nhiệm vụ |
|---|---|
| `DataStore.java` | `save(GroupData)` ghi toàn bộ dữ liệu ra file `splitmoney-data.txt`; `load()` đọc lại khi mở ứng dụng (file chưa có thì trả về dữ liệu rỗng). Được gọi tự động sau mỗi thay đổi. |

### 5.6. `ui/` – giao diện Swing
| File | Nhiệm vụ | Phục vụ tính năng |
|---|---|---|
| `MainFrame.java` | Cửa sổ chính: giữ `GroupData` và `DataStore`, chứa thanh trên cùng (tên nhóm, "Nhóm mới") và `JTabbedPane` 3 tab. Có hàm `refreshAll()` để các tab cập nhật lại sau mỗi thay đổi và hàm lưu dữ liệu. | Thanh trên cùng, điều phối |
| `MembersPanel.java` | Tab Thành viên: danh sách, ô nhập tên, nút Thêm/Xóa; kiểm tra tên rỗng, trùng, thành viên đang được dùng. | Thêm/xóa thành viên |
| `ExpensesPanel.java` | Tab Khoản chi: form nhập (mô tả, số tiền, người trả, các ô tick người tham gia), bảng danh sách, nút Xóa; kiểm tra số tiền và người tham gia. | Thêm/xóa khoản chi |
| `SummaryPanel.java` | Tab Tổng kết: bảng số dư (tô màu), bảng gợi ý thanh toán + nút "Đã thanh toán", lịch sử thanh toán + nút "Hủy", tổng chi tiêu, nút "Sao chép tổng kết". | Toàn bộ tab Tổng kết |
| `Money.java` | Tiện ích: `format(long)` → `150.000đ`; `signed(long)` → `+250.000đ` / `−150.000đ`; `parse(String)` → số tiền hoặc báo không hợp lệ. | Hiển thị và nhập số tiền ở mọi tab |

### 5.7. `test/` – kiểm thử
| File | Nhiệm vụ |
|---|---|
| `CalculatorTests.java` | Chương trình chạy độc lập, kiểm tra 3 lớp trong `logic/`: chia 100.000 cho 3 người; chia 5đ cho 8 người; ví dụ An/Bình/Chi/Dũng (số dư và đúng 3 gợi ý); tổng số dư luôn bằng 0; áp dụng hết gợi ý thì mọi số dư về 0; mọi số dư bằng 0 thì không có gợi ý. In `PASS`/`FAIL` cho từng ca. |

## 6. Bảng "tính năng nào do file nào code"

| Tính năng | File chính | File hỗ trợ |
|---|---|---|
| Khởi động ứng dụng | `Main.java` | `MainFrame.java`, `DataStore.java` |
| Thêm/xóa thành viên | `MembersPanel.java` | `GroupData.java`, `Member.java` |
| Thêm/xóa khoản chi | `ExpensesPanel.java` | `GroupData.java`, `Expense.java`, `Money.java` |
| Chia đều, xử lý số dư lẻ | `SplitCalculator.java` | – |
| Tính số dư từng người | `BalanceCalculator.java` | `SplitCalculator.java` |
| Gợi ý ai chuyển cho ai | `SettlementCalculator.java` | `Transfer.java` |
| Ghi nhận / hủy thanh toán | `SummaryPanel.java` | `Settlement.java`, `GroupData.java` |
| Hiển thị số dư, màu xanh/đỏ | `SummaryPanel.java` | `Money.java`, `BalanceCalculator.java` |
| Sao chép tổng kết | `SummaryPanel.java` | `Money.java` |
| Lưu và mở lại dữ liệu | `DataStore.java` | `GroupData.java` |
| Đổi tên nhóm, nhóm mới | `MainFrame.java` | `GroupData.java`, `DataStore.java` |
| Định dạng và đọc số tiền | `Money.java` | – |
| Kiểm thử phần tính toán | `CalculatorTests.java` | 3 file trong `logic/` |

## 7. Luồng hoạt động

### 7.1. Khi mở ứng dụng
`Main` → `DataStore.load()` đọc file (nếu có) → tạo `GroupData` → mở `MainFrame` → các tab hiển thị dữ liệu.

### 7.2. Khi người dùng thêm một khoản chi
1. `ExpensesPanel` đọc các ô nhập, kiểm tra hợp lệ (dùng `Money.parse`); sai thì hiện hộp thoại báo lỗi và dừng.
2. Tạo đối tượng `Expense`, thêm vào `GroupData`.
3. `MainFrame` lưu dữ liệu bằng `DataStore.save(...)` rồi gọi `refreshAll()`.
4. `SummaryPanel` gọi `BalanceCalculator` và `SettlementCalculator` để vẽ lại bảng số dư và gợi ý.

### 7.3. Quy tắc phân tầng
```text
ui/  ──gọi──>  logic/   (tính toán)
ui/  ──gọi──>  storage/ (lưu file)
ui/, logic/, storage/ ──dùng──>  model/ (dữ liệu)
```
- `logic/` không biết gì về Swing hay file, nên dễ test.
- `ui/` không tự tính tiền, chỉ gọi `logic/` rồi hiển thị.
- `model/` chỉ chứa dữ liệu.

## 8. Định dạng file dữ liệu `splitmoney-data.txt`

Mỗi dòng là một bản ghi, các trường cách nhau bằng ký tự Tab, mã hóa UTF-8. Ký tự Tab và xuống dòng trong nội dung nhập được thay bằng khoảng trắng khi lưu.

```text
G	Đà Lạt 3N2Đ
N	7
M	1	An
M	2	Bình
M	3	Chi
M	4	Dũng
E	5	Ăn tối	400000	1	1,2,3,4
E	6	Cà phê	200000	2	1,2,3,4
S	7	3	1	150000
```

| Mã | Ý nghĩa | Các trường |
|---|---|---|
| `G` | Tên nhóm | tên |
| `N` | Id kế tiếp | số |
| `M` | Thành viên | id, tên |
| `E` | Khoản chi | id, mô tả, số tiền, id người trả, các id người tham gia (cách nhau dấu phẩy) |
| `S` | Thanh toán đã ghi nhận | id, id người chuyển, id người nhận, số tiền |

## 9. Cách biên dịch và chạy

Yêu cầu: JDK 17 trở lên (kiểm tra bằng `java -version` và `javac -version`).

**Linux / macOS**
```bash
cd splitmoney
javac -d out $(find src test -name "*.java")
java -cp out Main
```

**Windows (Command Prompt)**
```bat
cd splitmoney
dir /s /b src\*.java test\*.java > sources.txt
javac -d out @sources.txt
java -cp out Main
```

**Chạy kiểm thử**
```bash
java -cp out CalculatorTests
```

**Dùng IDE:** mở thư mục `splitmoney`, đặt `src` làm thư mục nguồn (Sources Root), mở `Main.java` rồi bấm Run.

## 10. Giới hạn đã biết
- Chỉ có một nhóm tại một thời điểm (muốn nhóm khác thì "Nhóm mới" hoặc đổi thư mục chạy).
- Chỉ hỗ trợ chia đều; chưa có chia theo số tiền hoặc phần trăm.
- Chưa có sửa khoản chi (xóa rồi thêm lại).
- Dữ liệu lưu trên máy một người, chưa chia sẻ được qua mạng.
- Gợi ý thanh toán cho ra tối đa (n − 1) giao dịch nhưng không đảm bảo là số giao dịch ít nhất tuyệt đối.

## 11. Hướng phát triển
- Chia theo số tiền cụ thể và theo phần trăm.
- Sửa khoản chi; tìm kiếm và lọc.
- Xuất tổng kết ra CSV hoặc PDF.
- Nhiều nhóm trong cùng ứng dụng.
- Chuyển sang giao diện JavaFX hoặc web nếu cần chia sẻ qua mạng.
