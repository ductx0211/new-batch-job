# Hướng dẫn xem Commit trên Git Browser

## Cách 1: Sử dụng Git Aliases (Khuyến nghị)

Đã cấu hình các alias sau trong git config:

### Mở Repository trên Browser
```bash
git view-repo
```
Mở repository trên GitHub: https://github.com/ductx0211/new-batch-job

### Mở Commit cụ thể
```bash
# Mở commit hiện tại (HEAD)
git view-commit

# Mở commit cụ thể
git view-commit bee5ce0
git view-commit b978ff1
```

### Mở danh sách Commit trên branch hiện tại
```bash
git view-commits
```
Mở trang commits của branch hiện tại trên GitHub

## Cách 2: Sử dụng Script

Sử dụng script `git-browser-commands.sh`:

```bash
# Mở repository
./git-browser-commands.sh repo

# Mở commit cụ thể
./git-browser-commands.sh commit bee5ce0

# Mở danh sách commit
./git-browser-commands.sh commits

# Mở file cụ thể
./git-browser-commands.sh file pom.xml

# So sánh 2 commit
./git-browser-commands.sh compare bee5ce0 b978ff1
```

## Cách 3: Mở trực tiếp trên Browser

### Xem commit trên GitHub:
1. Mở repository: https://github.com/ductx0211/new-batch-job
2. Click vào tab **Commits** để xem tất cả commit
3. Click vào commit hash để xem chi tiết

### Xem commit cụ thể:
- Commit hash: `bee5ce0` → https://github.com/ductx0211/new-batch-job/commit/bee5ce0
- Commit hash: `b978ff1` → https://github.com/ductx0211/new-batch-job/commit/b978ff1

### Xem commit trên branch:
- Branch `main`: https://github.com/ductx0211/new-batch-job/commits/main

## Cách 4: Sử dụng Git Log + Browser

### Xem commit history:
```bash
# Xem commit history
git log --oneline

# Xem commit với thông tin chi tiết
git log --graph --oneline --all

# Xem commit với author và date
git log --pretty=format:"%h - %an, %ar : %s" -10
```

### Mở commit từ log:
1. Chạy `git log --oneline` để xem danh sách commit
2. Copy commit hash
3. Chạy `git view-commit <hash>` hoặc mở trực tiếp trên browser

## Cách 5: Sử dụng Git Web (nếu có)

```bash
# Mở git web interface (nếu có cấu hình)
git instaweb

# Hoặc sử dụng gitweb
git web--browse
```

## Ví dụ thực tế:

### Xem commit mới nhất:
```bash
git view-commit
```

### Xem tất cả commit trên branch main:
```bash
git view-commits
```

### Xem commit "shedlock":
```bash
git view-commit bee5ce0
```

### Xem file pom.xml trên GitHub:
```bash
./git-browser-commands.sh file pom.xml
```

## Lưu ý:

1. **Aliases chỉ hoạt động trong repository này** (đã cấu hình local)
2. **Script cần quyền execute**: `chmod +x git-browser-commands.sh`
3. **Browser mặc định**: Script sử dụng `open` (macOS), có thể thay bằng `xdg-open` (Linux) hoặc `start` (Windows)
4. **Remote URL**: Đảm bảo remote URL đúng: `https://github.com/ductx0211/new-batch-job.git`

## Troubleshooting:

### Alias không hoạt động:
```bash
# Kiểm tra alias
git config --local --list | grep alias

# Xem alias cụ thể
git config --local alias.view-commit
```

### Script không chạy được:
```bash
# Kiểm tra quyền
ls -l git-browser-commands.sh

# Cấp quyền execute
chmod +x git-browser-commands.sh
```

### Remote URL không đúng:
```bash
# Kiểm tra remote URL
git remote -v

# Sửa remote URL nếu cần
git remote set-url origin https://github.com/ductx0211/new-batch-job.git
```


