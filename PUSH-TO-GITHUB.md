

# Hướng dẫn Push Project lên GitHub

## Đã hoàn thành

✅ Đã khởi tạo git repository  
✅ Đã tạo file `.gitignore`  
✅ Đã add tất cả files  
✅ Đã commit với message: "Initial commit: Spring Batch Job Framework with Java 21 and Spring Boot 3.2.2"  
✅ Đã set branch thành `main`  
✅ Đã add remote: `https://github.com/ductx0211/new-batch-job.git`

## Cần thực hiện: Push lên GitHub

Bạn cần xác thực với GitHub để push. Có 2 cách:

### Cách 1: Sử dụng Personal Access Token (Khuyến nghị)

1. **Tạo Personal Access Token trên GitHub:**
   - Vào GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Click "Generate new token (classic)"
   - Chọn scope: `repo` (full control of private repositories)
   - Copy token (chỉ hiển thị 1 lần)

2. **Push với token:**
   ```bash
   git push -u origin main
   ```
   - Username: `ductx0211`
   - Password: **Paste token vừa tạo** (không phải password GitHub)

### Cách 2: Sử dụng SSH (Nếu đã setup SSH key)

1. **Đổi remote sang SSH:**
   ```bash
   git remote set-url origin git@github.com:ductx0211/new-batch-job.git
   ```

2. **Push:**
   ```bash
   git push -u origin main
   ```

### Cách 3: Sử dụng GitHub CLI

```bash
# Cài đặt GitHub CLI (nếu chưa có)
# Mac: brew install gh
# Windows: winget install GitHub.cli

# Login
gh auth login

# Push
git push -u origin main
```

## Kiểm tra sau khi push

Sau khi push thành công, kiểm tra tại:
https://github.com/ductx0211/new-batch-job

## Lưu ý

- **Không commit** file `target/` (đã có trong `.gitignore`)
- **Không commit** file chứa credentials (username/password trong `application.yml`)
- Nếu `application.yml` có thông tin nhạy cảm, nên tạo `application.yml.example` và commit file đó thay vì `application.yml` thực tế

## Nếu gặp lỗi "repository is empty"

Nếu repository trên GitHub vẫn hiển thị "empty" sau khi push:
1. Refresh trang GitHub
2. Kiểm tra branch: Đảm bảo đang xem branch `main`
3. Kiểm tra lại remote URL: `git remote -v`

