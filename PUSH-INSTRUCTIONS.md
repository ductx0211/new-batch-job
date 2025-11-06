# Hướng dẫn Push lên GitHub

## Đã hoàn thành
✅ Remote đã được đổi về HTTPS: `https://github.com/ductx0211/new-batch-job.git`

## Cách Push

### Bước 1: Tạo Personal Access Token trên GitHub

1. Vào: https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Đặt tên token: `new-batch-job-push`
4. Chọn scope: ✅ **repo** (full control of private repositories)
5. Click **"Generate token"**
6. **Copy token ngay** (chỉ hiển thị 1 lần!)

### Bước 2: Push code

Chạy lệnh:
```bash
git push -u origin main
```

Khi được hỏi:
- **Username**: `ductx0211`
- **Password**: **Paste Personal Access Token** (không phải password GitHub!)

### Hoặc: Sử dụng token trực tiếp trong URL

```bash
# Thay YOUR_TOKEN bằng token vừa tạo
git remote set-url origin https://YOUR_TOKEN@github.com/ductx0211/new-batch-job.git
git push -u origin main
```

### Hoặc: Sử dụng GitHub CLI (nếu đã cài)

```bash
gh auth login
git push -u origin main
```

## Lưu ý

- GitHub **không còn chấp nhận password** khi push qua HTTPS
- **Bắt buộc** phải dùng Personal Access Token
- Token có quyền **repo** để có thể push code

## Sau khi push thành công

Kiểm tra tại: https://github.com/ductx0211/new-batch-job

