# Phazel Sound Mobile

Ứng dụng nghe nhạc mobile với giao diện hiện đại và tính năng xác thực người dùng đầy đủ.

## Tính năng Auth đã hoàn thành

- Đăng nhập
- Đăng ký tài khoản
- Xác thực OTP qua email
- Quên mật khẩu
- Đặt lại mật khẩu với OTP
- Gửi lại mã OTP

## Cài đặt

```bash
cd PhazelSoundMobile
npm install
```

## Cấu hình

Backend API mặc định: `http://localhost:8080/api`

Để thay đổi, chỉnh sửa file: `src/api/axiosClient.ts`

## Chạy ứng dụng

```bash
# Chạy trên iOS
npm run ios

# Chạy trên Android
npm run android

# Chạy trên web
npm run web
```

## Tone màu

Ứng dụng sử dụng tone màu xanh dương đậm (Deep Blue) kết hợp với Coral/Orange:

- Primary: #0A1628 (Dark Blue)
- Accent: #FF6B6B (Coral Red)
- Gradient: Coral to Orange (#FF6B6B → #FF8E53)

## Cấu trúc thư mục

```
PhazelSoundMobile/
├── app/
│   ├── (auth)/          # Màn hình xác thực
│   └── (tabs)/          # Màn hình chính
├── src/
│   ├── api/             # Config API
│   ├── components/      # Components tái sử dụng
│   ├── constants/       # Hằng số (colors, etc)
│   └── features/
│       └── auth/        # Logic auth (service, store, types)
└── ...
```

## Tech Stack

- React Native + Expo
- Expo Router (File-based routing)
- Zustand (State management)
- Axios (API calls)
- TypeScript
