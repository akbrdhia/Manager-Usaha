# 📌 Manager Usaha

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-2024.2.2.13-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![API Integration](https://img.shields.io/badge/Backend_API-Integrated-00D9FF?style=for-the-badge&logo=api&logoColor=white)
![API Level](https://img.shields.io/badge/Min_SDK-29-orange?style=for-the-badge)

*Aplikasi manajemen usaha yang powerful dan user-friendly untuk UMKM Indonesia*

[📱 Download APK](#) | [📖 Documentation](#) | [🐛 Report Bug](#) | [💡 Request Feature](#)

</div>

---

## 📖 Tentang Project

**Manager Usaha** adalah aplikasi Android native yang dikembangkan khusus untuk membantu pemilik UMKM (Usaha Mikro, Kecil, dan Menengah) di Indonesia dalam mengelola bisnis mereka secara digital. Aplikasi ini menyediakan solusi terintegrasi untuk pencatatan transaksi, manajemen inventori, dan analisis keuangan dengan interface yang intuitif dan mudah digunakan.

### 🎯 Tujuan
- Digitalisasi pencatatan usaha tradisional
- Meningkatkan efisiensi operasional UMKM
- Menyediakan insights bisnis melalui laporan otomatis
- Membantu pengambilan keputusan berbasis data

---

## ✨ Fitur Lengkap

### 💰 **Manajemen Transaksi**
- ➕ Pencatatan penjualan dan pembelian
- 🔍 Pencarian transaksi berdasarkan tanggal/produk
- 📊 Kategori transaksi yang dapat dikustomisasi

### 📦 **Manajemen Stok**
- 📋 Daftar produk dengan detail lengkap
- ⚠️ Notifikasi stok menipis (low stock alert)
- 📈 Tracking pergerakan barang masuk/keluar
- 🏷️ Barcode scanner untuk input cepat
- 📊 Analisis produk terlaris

### 📈 **Laporan & Analytics**
- 💹 Dashboard dengan ringkasan bisnis harian
- 📊 Grafik penjualan harian, mingguan, bulanan
- 💰 Laporan laba rugi otomatis
- 📋 Export laporan ke PDF/Excel
- 🎯 Target penjualan dan tracking pencapaian

### 🔧 **Fitur Tambahan**
- 🔒 Backup & restore data otomatis ke server
- 👥 Multi-user access dengan role management (coming soon)
- 🌐 Real-time data synchronization
- 📱 Widget untuk akses cepat
- 🔄 Offline mode dengan sync otomatis

---

## 🛠 Tech Stack

<div align="left">
  <img src="https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-icon.svg" alt="Kotlin" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/android/android-original.svg" alt="Android" width="60" height="60"/>
  <img src="https://www.vectorlogo.zone/logos/nodejs/nodejs-icon.svg" alt="Backend API" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/Templarian/MaterialDesign/master/svg/material-design.svg" alt="Material Design" width="60" height="60"/>
</div>

### Core Technologies
- **Language**: Kotlin 100%
- **IDE**: Android Studio Ladybug 2024.2.2.13
- **Backend**: RESTful API Integration
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Material Design 3
- **Network**: Retrofit2 + OkHttp
- **Local Storage**: Room Database (offline cache)
- **Charts**: MPAndroidChart
- **PDF Generation**: iText7

### System Requirements
- **Min SDK**: Android 10.0 (API Level 29)
- **Target SDK**: Android 14 (API Level 34)
- **RAM**: Minimum 2GB
- **Storage**: 40MB

---

## 📸 Screenshots

<div align="center">

### 🏠 Dashboard
<img src="screenshots/dashboard.png" alt="Dashboard" width="200"/>

### 💰 Transaksi
<img src="screenshots/transactions.png" alt="Transactions" width="200"/>

### 📦 Stok Management
<img src="screenshots/inventory.png" alt="Inventory" width="200"/>

### 📊 Laporan
<img src="screenshots/reports.png" alt="Reports" width="200"/>

*Screenshots akan segera diupdate*

</div>

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug 2024.2.2.13+
- JDK 17+
- Android SDK 34+
- Git

### Installation

1. **Clone repository**
   ```bash
   git clone https://github.com/akbrdhia/ManagerUsaha.git
   cd ManagerUsaha
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to cloned directory

3. **Setup Dependencies**
   ```bash
   # Sync project with Gradle files
   ./gradlew build
   ```

4. **Run Application**
   - Connect Android device or start emulator
   - Click Run ▶️ button
   - Select target device

### Build APK
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

---

## 📁 Project Structure

```
Coming Soon(I havent finish the project yet)
```

---

## 📋 Roadmap

- [x] ~~Manajemen transaksi dasar~~
- [x] ~~Manajemen stok~~
- [x] ~~Laporan keuangan~~
- [x] ~~Backend API integration~~
- [x] ~~Real-time data sync~~
- [ ] 🔄 Advanced analytics dashboard
- [ ] 🔄 Mobile POS integration
- [ ] 🔄 Customer relationship management

---

## 📄 License

Project ini dilisensikan under MIT License - lihat file [LICENSE](LICENSE) untuk detail.

---

## 📞 Support & Contact

<div align="center">

**Butuh bantuan atau punya saran?**

[![GitHub](https://img.shields.io/badge/GitHub-akbrdhia-black?style=for-the-badge&logo=github)](https://github.com/akbrdhia)
[![Email](https://img.shields.io/badge/Email-Contact-red?style=for-the-badge&logo=gmail)](mailto:akbadhia19@gmail.com)

</div>

### 🐛 Found a Bug?
Silakan buat [issue baru](https://github.com/akbrdhia/ManagerUsaha/issues) dengan detail:
- Deskripsi bug
- Langkah reproduksi
- Expected vs actual behavior
- Screenshots (jika ada)
- Device info & OS version

### 💡 Feature Request?
Punya ide fitur baru? [Diskusikan di sini](https://github.com/akbrdhia/ManagerUsaha/discussions)

---

## ⭐ Show Your Support

Jika project ini membantu, berikan ⭐ untuk mendukung development selanjutnya!

<div align="center">

**Made with by Kelompok 11(Akbar, Dhafin, Rie)**

*Bye*

</div>
