# SplitMoney – Ứng dụng chia tiền nhóm (Java Swing)

**SplitMoney** là ứng dụng desktop viết bằng **Java Swing**, giúp một nhóm bạn (đi chơi, ăn uống, ở trọ chung...) ghi lại các khoản chi và biết ngay **ai nợ ai bao nhiêu**, cần **chuyển tiền cho ai** để mọi người về 0.

> Bài toán: thường có một người ứng tiền trước cho cả nhóm, cuối buổi tính tay rất dễ nhầm và mất thời gian. SplitMoney tính tự động, chính xác tới từng đồng.

---

## Mục lục

1. [Điểm nổi bật](#1-điểm-nổi-bật)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Cài đặt](#3-cài-đặt)
4. [Biên dịch và chạy](#4-biên-dịch-và-chạy)
5. [Hướng dẫn sử dụng](#5-hướng-dẫn-sử-dụng)
6. [Tính năng chi tiết](#6-tính-năng-chi-tiết)
7. [Quy tắc và cách tính tiền](#7-quy-tắc-và-cách-tính-tiền)
8. [Cấu trúc thư mục](#8-cấu-trúc-thư-mục)
9. [Luồng hoạt động](#9-luồng-hoạt-động)
10. [Định dạng file dữ liệu](#10-định-dạng-file-dữ-liệu)
11. [Chạy kiểm thử](#11-chạy-kiểm-thử)
12. [Xử lý lỗi thường gặp](#12-xử-lý-lỗi-thường-gặp)
13. [Giới hạn đã biết](#13-giới-hạn-đã-biết)
14. [Hướng phát triển](#14-hướng-phát-triển)

---

## 1. Điểm nổi bật

- Toàn bộ code bằng Java, cả giao diện lẫn tính toán.
- **Không cần cơ sở dữ liệu, không cần cài thư viện ngoài, không dùng Maven/Spring.** Chỉ cần JDK 17 trở lên.
- Dữ liệu **tự lưu** ra file văn bản `splitmoney-data.txt`, mở lại ứng dụng vẫn còn.
- Mọi phép tính tiền dùng **số nguyên (đồng)**, không bao giờ lệch 1 đồng.
- Số dư **không lưu** vào file mà luôn được tính lại từ khoản chi và thanh toán, nên thêm/xóa khoản chi không bao giờ làm lệch số liệu.
- Có chương trình kiểm thử thuần Java cho phần tính toán.

## 2. Công nghệ sử dụng

| Thành phần   | Công nghệ                                                               |
| -------------- | ------------------------------------------------------------------------- |
| Ngôn ngữ     | Java 17 trở lên                                                         |
| Giao diện     | Java Swing (có sẵn trong JDK)                                           |
| Lưu dữ liệu | File văn bản `splitmoney-data.txt` (UTF-8)                            |
| Kiểm thử     | Chương trình test thuần Java (`CalculatorTests`), không cần JUnit |
| Build          | `javac` / `java` (hoặc bấm Run trong IDE)                           |

---

## 3. Cài đặt

### 3.1. Cài JDK 17 trở lên

Kiểm tra máy đã có JDK chưa, mở terminal và gõ:

```bash
java -version
javac -version
```

Cả hai lệnh đều phải in ra phiên bản **17 trở lên**. Nếu `javac` báo `'javac' is not recognized` hoặc `command not found`, nghĩa là máy mới có JRE mà chưa có JDK, hoặc JDK chưa nằm trong PATH.

Cách cài:

| Hệ điều hành        | Cách cài                                                                                                                                                      |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Windows / macOS / Linux | Tải **Temurin JDK 17 hoặc 21** tại https://adoptium.net và cài theo trình hướng dẫn. Trên Windows nên tick tùy chọn **"Add to PATH"**. |
| Ubuntu / Debian         | `sudo apt install openjdk-17-jdk`                                                                                                                             |
| macOS (Homebrew)        | `brew install openjdk@17`                                                                                                                                     |

Sau khi cài, **đóng và mở lại terminal** rồi kiểm tra lại bằng `java -version` và `javac -version`.

### 3.2. Lấy mã nguồn

- Giải nén file `splitmoney.zip` (hoặc `git clone` nếu có repository).
- **Lưu ý:** đôi khi giải nén tạo ra thư mục lồng nhau dạng `splitmoney\splitmoney\...`. Thư mục đúng để chạy lệnh là thư mục **chứa `src` và `test`** (xem [mục 8](#8-cấu-trúc-thư-mục)).
- Nên đặt dự án ở thư mục thường, ví dụ `C:\splitmoney`, tránh thư mục đồng bộ OneDrive nếu file chưa được tải về máy.

---

## 4. Biên dịch và chạy

> Hãy mở terminal và `cd` vào thư mục **chứa `src` và `test`**. Kiểm tra bằng lệnh `dir` (Windows) hoặc `ls` (Linux/macOS): phải thấy `src`, `test`, `README.md`.

Chỉ cần **biên dịch một lần** (hoặc mỗi khi sửa code), sau đó chạy bao nhiêu lần cũng được.

### 4.1. Windows – PowerShell (terminal mặc định của VS Code)

```powershell
cd đường\dẫn\tới\splitmoney
javac -encoding UTF-8 -d out src\Main.java src\model\*.java src\logic\*.java src\storage\*.java src\ui\*.java test\*.java
java -cp out Main
```

### 4.2. Windows – Command Prompt (cmd)

```bat
cd đường\dẫn\tới\splitmoney
dir /s /b src\*.java test\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -cp out Main
```

> Lệnh `dir /s /b` chỉ chạy trong **cmd**. Trong PowerShell nó sẽ báo lỗi `A positional parameter cannot be found`, hãy dùng cách 4.1.

### 4.3. Linux / macOS

```bash
cd đường/dẫn/tới/splitmoney
javac -encoding UTF-8 -d out $(find src test -name "*.java")
java -cp out Main
```

### 4.4. Dùng IDE

- **IntelliJ IDEA:** `File > Open` thư mục dự án → chuột phải thư mục `src` → *Mark Directory as* → *Sources Root* → mở `Main.java` → bấm Run.
- **VS Code:** cài *Extension Pack for Java* → mở thư mục dự án → mở `Main.java` → bấm **Run** phía trên hàm `main`.
- **Eclipse:** tạo *Java Project*, copy thư mục `src` vào, chuột phải `Main.java` → *Run As > Java Application*.

Dù dùng cách nào, lần chạy đầu sẽ mở cửa sổ ứng dụng SplitMoney. Từ các lần sau chỉ cần `java -cp out Main`.

> Lưu ý: file dữ liệu `splitmoney-data.txt` được tạo **trong thư mục bạn đang đứng khi chạy lệnh**. Muốn giữ dữ liệu cũ, hãy luôn chạy từ cùng một thư mục.

---

## 5. Hướng dẫn sử dụng

Ví dụ nhanh: nhóm An, Bình, Chi, Dũng đi chơi. An trả 400.000đ tiền ăn, Bình trả 200.000đ tiền cà phê, cả 4 người cùng chia.

1. **Đặt tên nhóm (tùy chọn):** gõ vào ô *Tên nhóm* ở thanh trên cùng, ví dụ `Đà Lạt 3N2Đ`.
2. **Tab "Thành viên":** nhập `An` → bấm **Thêm** (hoặc Enter). Làm tương tự với Bình, Chi, Dũng.
3. **Tab "Khoản chi":**
   - Mô tả: `Ăn tối`, Số tiền: `400.000`, Người trả: `An`.
   - Tick cả 4 người tham gia (người mới thêm mặc định được tick sẵn) → bấm **Thêm**.
   - Thêm tiếp khoản `Cà phê`, `200.000`, người trả `Bình`, 4 người tham gia.
4. **Tab "Tổng kết":** xem bảng số dư và danh sách gợi ý thanh toán:
   - Chi → An: 150.000đ
   - Dũng → An: 100.000đ
   - Dũng → Bình: 50.000đ
5. **Khi ai đó chuyển tiền thật:** chọn dòng gợi ý tương ứng → bấm **Đã thanh toán** → xác nhận. Số dư cập nhật ngay và dòng đó chuyển xuống *Lịch sử thanh toán*.
6. **Ghi nhầm?** chọn dòng trong *Lịch sử thanh toán* → bấm **Hủy**.
7. **Gửi kết quả cho nhóm:** bấm **Sao chép tổng kết** rồi dán vào Zalo/Messenger.
8. **Bắt đầu nhóm khác:** bấm **Nhóm mới** ở thanh trên cùng (có hỏi xác nhận, sẽ xóa toàn bộ dữ liệu).

---

## 6. Tính năng chi tiết

Ứng dụng có một cửa sổ chính với thanh trên cùng và 3 tab.

### Thanh trên cùng

| Tính năng | Mô tả                                                                       |
| ----------- | ----------------------------------------------------------------------------- |
| Tên nhóm  | Ô nhập tên nhóm, hiển thị trong nội dung tổng kết. Tự lưu khi gõ. |
| Nhóm mới  | Xóa toàn bộ dữ liệu để bắt đầu nhóm khác (có hỏi xác nhận).   |

### Tab "Thành viên"

| Tính năng        | Mô tả                                                                                                  |
| ------------------ | -------------------------------------------------------------------------------------------------------- |
| Thêm thành viên | Nhập tên, bấm "Thêm". Tên không được rỗng và không trùng (không phân biệt hoa thường). |
| Xóa thành viên  | Chỉ xóa được khi người đó chưa tham gia khoản chi hoặc thanh toán nào.                     |

### Tab "Khoản chi"

| Tính năng           | Mô tả                                                                                                                                                                                                    |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Thêm khoản chi      | Nhập mô tả, số tiền, chọn người trả và tick những người tham gia, bấm "Thêm". Tiền được **chia đều** cho những người tham gia. Có nút "Chọn tất cả" / "Bỏ chọn hết". |
| Danh sách khoản chi | Bảng hiển thị mô tả, người trả, số tiền, những người tham gia.                                                                                                                                |
| Xóa khoản chi       | Chọn một dòng rồi bấm "Xóa". Muốn sửa thì xóa và thêm lại.                                                                                                                                    |

### Tab "Tổng kết"

| Tính năng                  | Mô tả                                                                                                            |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Bảng số dư                | Mỗi người: đã trả, phải chịu, số dư. Số dư dương (xanh) là được nhận, âm (đỏ) là đang nợ. |
| Gợi ý thanh toán          | Danh sách "A chuyển cho B: X đ" để cả nhóm về 0, tối đa (n − 1) lần chuyển.                           |
| Ghi nhận "Đã thanh toán" | Chọn một gợi ý rồi bấm nút để ghi nhận khoản đã chuyển; số dư cập nhật ngay.                     |
| Lịch sử thanh toán        | Danh sách các lần đã ghi nhận, có nút "Hủy" nếu ghi nhầm.                                               |
| Tổng chi tiêu              | Hiển thị tổng số tiền cả nhóm đã chi.                                                                     |
| Sao chép tổng kết         | Đưa nội dung tổng kết vào clipboard để dán vào Zalo/Messenger.                                           |

---

## 7. Quy tắc và cách tính tiền

### Quy tắc nhập liệu

1. Tên thành viên không rỗng, không trùng.
2. Số tiền là số nguyên dương. Có thể gõ `100.000`, `100,000`, `100000` hoặc `100000đ`.
3. Mỗi khoản chi có ít nhất 1 người tham gia.
4. Người trả phải là thành viên của nhóm.
5. Thanh toán: người chuyển khác người nhận, số tiền dương.

### Ba phép tính cốt lõi

1. **Chia đều:** mỗi người chịu `số tiền / số người` (chia nguyên). Phần dư `r = số tiền % số người` được chia thêm **1 đồng** cho `r` người đầu tiên trong danh sách tham gia. Tổng luôn đúng bằng số tiền gốc.
   *Ví dụ:* 100.000đ chia 3 người → 33.334 / 33.333 / 33.333.
2. **Số dư mỗi người:** `đã trả − phải chịu + đã chuyển đi − đã nhận về`. Dương là được nhận, âm là đang nợ. Tổng số dư cả nhóm luôn bằng 0.
3. **Gợi ý ai chuyển cho ai:** lấy người nợ đầu tiên ghép với người được nhận đầu tiên, chuyển số tiền nhỏ hơn trong hai số, lặp lại đến hết.

### Ví dụ kiểm chứng

Nhóm An, Bình, Chi, Dũng. An trả 400.000 tiền ăn, Bình trả 200.000 tiền cà phê, mỗi khoản chia đều 4 người.

| Người | Đã trả | Phải chịu | Số dư   |
| ------- | --------- | ----------- | --------- |
| An      | 400.000   | 150.000     | +250.000  |
| Bình   | 200.000   | 150.000     | +50.000   |
| Chi     | 0         | 150.000     | −150.000 |
| Dũng   | 0         | 150.000     | −150.000 |

Gợi ý: **Chi → An 150.000đ; Dũng → An 100.000đ; Dũng → Bình 50.000đ.**

---

## 8. Cấu trúc thư mục

```text
splitmoney/
├── README.md                         (tài liệu này)
├── PROJECT.md                        (mô tả dự án)
├── .gitignore
├── src/
│   ├── Main.java                     điểm khởi động
│   ├── model/                        dữ liệu
│   │   ├── Member.java
│   │   ├── Expense.java
│   │   ├── Settlement.java
│   │   ├── Transfer.java
│   │   └── GroupData.java
│   ├── logic/                        tính toán (không Swing, không đọc/ghi file)
│   │   ├── SplitCalculator.java
│   │   ├── BalanceCalculator.java
│   │   └── SettlementCalculator.java
│   ├── storage/
│   │   └── DataStore.java            lưu/đọc file
│   └── ui/                           giao diện Swing
│       ├── MainFrame.java
│       ├── MembersPanel.java
│       ├── ExpensesPanel.java
│       ├── SummaryPanel.java
│       └── Money.java
└── test/
    └── CalculatorTests.java
```

### Vai trò từng file

| File                          | Vai trò                                                                                                                                        |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `Main.java`                 | Đặt giao diện theo hệ điều hành, đọc dữ liệu đã lưu, tạo và hiển thị `MainFrame`. Không chứa logic nghiệp vụ.           |
| `Member.java`               | Thành viên: `id`, `name`.                                                                                                                 |
| `Expense.java`              | Khoản chi: `id`, `title`, `amount`, `paidBy`, danh sách `participantIds`.                                                           |
| `Settlement.java`           | Một lần chuyển tiền **đã xảy ra**: `id`, `fromId`, `toId`, `amount`.                                                       |
| `Transfer.java`             | Một gợi ý chuyển tiền **chưa xảy ra**: `fromId`, `toId`, `amount`.                                                           |
| `GroupData.java`            | Gói toàn bộ dữ liệu nhóm (tên nhóm, thành viên, khoản chi, thanh toán, bộ đếm id) cùng các hàm thêm/xóa/kiểm tra hợp lệ. |
| `SplitCalculator.java`      | `splitEqually(amount, participantIds)`: chia đều, xử lý số dư lẻ.                                                                      |
| `BalanceCalculator.java`    | `paid`, `owed`, `calculate`: tính đã trả, phải chịu và số dư.                                                                    |
| `SettlementCalculator.java` | `suggest(balances)`: trả về danh sách `Transfer` gợi ý.                                                                                |
| `DataStore.java`            | `save(GroupData)` ghi ra `splitmoney-data.txt`; `load()` đọc lại khi mở ứng dụng.                                                   |
| `MainFrame.java`            | Cửa sổ chính, thanh trên cùng, 3 tab; có `refreshAll()` và hàm lưu dữ liệu.                                                        |
| `MembersPanel.java`         | Tab Thành viên.                                                                                                                               |
| `ExpensesPanel.java`        | Tab Khoản chi.                                                                                                                                 |
| `SummaryPanel.java`         | Tab Tổng kết (bảng số dư, gợi ý, lịch sử, sao chép).                                                                                  |
| `Money.java`                | `format(long)` → `150.000đ`; `signed(long)` → `+250.000đ` / `−150.000đ`; `parse(String)` → số tiền hoặc báo lỗi.        |
| `CalculatorTests.java`      | Kiểm thử phần tính toán, in `PASS`/`FAIL` cho từng ca.                                                                                |

### Quy tắc phân tầng

```text
ui/  ──gọi──>  logic/   (tính toán)
ui/  ──gọi──>  storage/ (lưu file)
ui/, logic/, storage/ ──dùng──>  model/ (dữ liệu)
```

- `logic/` không biết gì về Swing hay file, nên rất dễ test.
- `ui/` không tự tính tiền, chỉ gọi `logic/` rồi hiển thị.
- `model/` chỉ chứa dữ liệu.

---

## 9. Luồng hoạt động

### 9.1. Khi mở ứng dụng

```text
Main → DataStore.load() đọc file (nếu có) → tạo GroupData
     → mở MainFrame → các tab hiển thị dữ liệu
```

### 9.2. Khi người dùng thêm một khoản chi

1. `ExpensesPanel` đọc các ô nhập, kiểm tra hợp lệ (dùng `Money.parse`). Sai thì hiện hộp thoại báo lỗi và dừng.
2. Tạo đối tượng `Expense`, thêm vào `GroupData`.
3. `MainFrame` lưu dữ liệu bằng `DataStore.save(...)` rồi gọi `refreshAll()`.
4. `SummaryPanel` gọi `BalanceCalculator` và `SettlementCalculator` để vẽ lại bảng số dư và gợi ý.

### 9.3. Khi ghi nhận "Đã thanh toán"

```text
Chọn gợi ý → bấm "Đã thanh toán" → tạo Settlement trong GroupData
          → lưu file → refreshAll()
          → số dư tính lại (đã chuyển đi / đã nhận về thay đổi) → gợi ý mới
```

Mọi thao tác thêm/xóa khác (thành viên, khoản chi, hủy thanh toán, đổi tên nhóm) đều theo cùng mẫu: **sửa `GroupData` → lưu file → vẽ lại các tab**.

---

## 10. Định dạng file dữ liệu

File `splitmoney-data.txt` được tạo tự động trong thư mục chạy. Mỗi dòng là một bản ghi, các trường cách nhau bằng ký tự **Tab**, mã hóa UTF-8. Ký tự Tab và xuống dòng trong nội dung nhập được thay bằng khoảng trắng khi lưu.

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

| Mã   | Ý nghĩa                  | Các trường                                                                             |
| ----- | -------------------------- | ----------------------------------------------------------------------------------------- |
| `G` | Tên nhóm                 | tên                                                                                      |
| `N` | Id kế tiếp               | số                                                                                       |
| `M` | Thành viên               | id, tên                                                                                  |
| `E` | Khoản chi                 | id, mô tả, số tiền, id người trả, các id người tham gia (cách nhau dấu phẩy) |
| `S` | Thanh toán đã ghi nhận | id, id người chuyển, id người nhận, số tiền                                       |

Ghi chú:

- Muốn **xóa toàn bộ dữ liệu** có thể bấm "Nhóm mới" hoặc xóa file `splitmoney-data.txt`.
- Muốn **dùng nhiều nhóm**, chạy ứng dụng từ các thư mục khác nhau (mỗi thư mục có file dữ liệu riêng).
- Dòng nào trong file bị sai định dạng sẽ bị bỏ qua khi đọc.

---

## 11. Chạy kiểm thử

Sau khi đã biên dịch (mục 4), chạy:

```bash
java -cp out CalculatorTests
```

Chương trình in `PASS`/`FAIL` cho từng ca và dòng tổng kết ở cuối. Các ca kiểm tra gồm:

- Chia 100.000đ cho 3 người → 33.334 / 33.333 / 33.333.
- Chia 5đ cho 8 người → 5 người đầu 1đ, 3 người sau 0đ.
- Ví dụ An/Bình/Chi/Dũng: đúng số dư và đúng 3 gợi ý.
- Tổng số dư cả nhóm luôn bằng 0.
- Áp dụng hết gợi ý thì mọi số dư về 0.
- Mọi số dư bằng 0 thì không có gợi ý.
- Ghi nhận thanh toán thì số dư cập nhật đúng.

---


## 12. Giới hạn đã biết

- Chỉ có một nhóm tại một thời điểm (muốn nhóm khác thì "Nhóm mới" hoặc đổi thư mục chạy).
- Chỉ hỗ trợ chia đều; chưa có chia theo số tiền cụ thể hoặc phần trăm.
- Chưa có sửa khoản chi (xóa rồi thêm lại).
- Dữ liệu lưu trên máy một người, chưa chia sẻ được qua mạng.
- Gợi ý thanh toán cho ra tối đa (n − 1) giao dịch nhưng không đảm bảo là số giao dịch ít nhất tuyệt đối.

## 13. Hướng phát triển

- Chia theo số tiền cụ thể và theo phần trăm.
- Sửa khoản chi; tìm kiếm và lọc.
- Xuất tổng kết ra CSV hoặc PDF.
- Nhiều nhóm trong cùng ứng dụng.
- Chuyển sang giao diện JavaFX hoặc web nếu cần chia sẻ qua mạng.
