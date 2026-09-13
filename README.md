# MahaSigma 🎓🪄👟

<div align="center">

**Asisten Akademik Mahasiswa Modern, Cerdas, dan Offline-First**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge&logo=android)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Storage-Room%20SQLite-F4511E.svg?style=for-the-badge&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)

</div>

---

## 📌 Tentang MahaSigma

**MahaSigma** adalah aplikasi asisten akademik berbasis Android yang dirancang khusus untuk mahasiswa dalam mengelola rutinitas perkuliahan secara terstruktur, cepat, dan mandiri. 

Dibangun dengan prinsip **100% Offline-First**, seluruh data mata kuliah, jadwal, tugas, dan foto materi kuliah disimpan secara aman di perangkat lokal tanpa bergantung pada koneksi internet ataupun server pihak ketiga.

---

## ✨ Fitur Utama

### 1. 📅 Jadwal Kuliah Fleksibel & Dinamis
- Kelola daftar mata kuliah lengkap dengan kode matkul, nama dosen, ruang kelas, jumlah SKS, dan warna identitas khusus.
- Tampilan timeline mingguan yang rapi dan terstruktur.
- **Status Override Sesi Perkuliahan**: Fleksibel mencatat kondisi realita kelas seperti *Kelas Hadir*, *Kelas Pengganti*, *Diliburkan*, hingga *Kuliah Daring (Online)* tanpa merusak jadwal reguler.

### 2. 📄 Impor Tabel Jadwal Otomatis dari Berkas PDF (Background Processing)
- Ekstraksi jadwal kuliah langsung dari dokumen KRS / jadwal portal kampus (SIAKAD) dalam format **PDF**, **Excel (.xlsx)**, atau **CSV**.
- Pemrosesan dilakukan di latar belakang (*background thread*) menggunakan coroutines sehingga aplikasi tetap responsif.
- Pratinjau interaktif: periksa mata kuliah yang terdeteksi, pilih item yang diinginkan, dan simpan langsung ke database dengan sekali ketuk.

### 3. ✅ Manajemen Tugas Kuliah & Deadline
- Catat tugas kuliah dengan prioritas (*Rendah*, *Sedang*, *Mendesak*).
- Indikator hitung mundur tenggat waktu (*deadline badge*).
- Integrasi alarm dan notifikasi pengingat tepat waktu menggunakan Android `AlarmManager`.
- Sub-checklist untuk memecah tugas besar menjadi langkah-langkah kecil.

### 4. 📸 Catatan Materi & Penajam Foto Papan Tulis
- Simpan ringkasan materi dan catatan penting per mata kuliah.
- Lampirkan foto catatan atau papan tulis dari kelas.
- Dilengkapi fitur pengolahan citra untuk meningkatkan kontras teks pada foto papan tulis agar lebih mudah dibaca kembali.

### 5. 📊 Dashboard Akademik Cerdas
- Menampilkan mata kuliah hari ini secara instan.
- Daftar tugas mendesak yang mendekati tenggat waktu.
- Statistik ringkasan akademik dan progres perkuliahan.

### 6. 💾 Keamanan Data, Backup & Restore Mandiri
- Ekspor seluruh data akademik ke berkas JSON lokal untuk pencadangan (*backup*).
- Pulihkan (*restore*) data kapan saja dengan mudah.
- Opsi reset data dengan perlindungan dialog konfirmasi berlapis.

---

## 🛠️ Tech Stack & Arsitektur

MahaSigma dikembangkan menggunakan praktik dan standar modern Android terkini:

- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **Arsitektur**: Clean Architecture + MVVM (Model-View-ViewModel) + Repository Pattern
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) dengan **Material Design 3 (M3)**
- **Basis Data Lokal**: [Room Database (SQLite)](https://developer.android.com/training/data-storage/room) didukung Kotlin Coroutines & Flow
- **Pemrosesan Dokumen**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
- **Image Loading & Processing**: [Coil Compose](https://coil-kt.github.io/coil/) & Android Graphics APIs
- **Background Tasks & Alarms**: `AlarmManager` + `BroadcastReceiver`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) dengan Version Catalog (`libs.versions.toml`)

---

## 📂 Struktur Proyek

```
app/src/main/java/com/example/
├── data/
│   ├── dao/             # Room Data Access Objects (MahaSigmaDao)
│   ├── database/        # Room Database Configuration (MahaSigmaDatabase)
│   ├── entity/          # Entitas Tabel (Course, Schedule, Task, Note, Override)
│   └── repository/      # Abstraksi Data Layer (MahaSigmaRepository)
├── receiver/            # Broadcast Receiver untuk Notifikasi Alarm Tugas
├── ui/
│   ├── components/      # Komponen Compose Reusable (Dialogs, Cards, Badges)
│   ├── screens/         # Tampilan Layar Utama (Dashboard, Schedule, Tasks, Notes, Settings)
│   ├── theme/           # Tema, Tipografi, dan Skema Warna Dark/Light M3
│   ├── viewmodel/       # MahaSigmaViewModel (Manajemen State & Usecase)
│   └── MainAppContainer.kt # Navigasi & Shell Utama Aplikasi
└── util/                # Parser PDF/Tabel, Pengolah Citra, Date Formatting, & Backup Helper
```

---

## 🚀 Cara Menjalankan Proyek (Setup & Run)

### Prasyarat
1. **Android Studio** (Ladybug / Jellyfish atau versi yang lebih baru).
2. **JDK 11** atau **JDK 17**.
3. Perangkat fisik Android atau Emulator dengan **Android 7.0 (API Level 24)** ke atas.

### Langkah-langkah

1. **Clone Repositori**:
   ```bash
   git clone https://github.com/USERNAME-ANDA/MahaSigma.git
   cd MahaSigma
   ```

2. **Buka di Android Studio**:
   - Pilih menu **File** > **Open...**
   - Arahkan ke folder hasil clone `MahaSigma`.
   - Tunggu proses Gradle Sync hingga selesai.

3. **Konfigurasi Lingkungan (Opsional)**:
   - Buat salinan berkas `.env.example` menjadi `.env` jika diperlukan untuk variabel rahasia tambahan:
     ```bash
     cp .env.example .env
     ```

4. **Jalankan Aplikasi**:
   - Pilih target device (Emulator atau HP fisik).
   - Klik tombol **Run** (ikon segitiga hijau `▶`) atau tekan `Shift + F10`.

---

## 🧪 Menjalankan Pengujian (Testing)

Proyek ini telah dilengkapi dengan pengujian unit otomatis dan screenshot testing (Robolectric & Roborazzi):

```bash
# Menjalankan seluruh Unit Test lokal
gradle :app:testDebugUnitTest

# Memverifikasi tampilan UI dengan Roborazzi Screenshot Test
gradle :app:verifyRoborazziDebug
```

---

## 🤝 Kontribusi

Kontribusi selalu terbuka dan disambut baik! Jika Anda memiliki saran atau menemukan bug:

1. Fork repositori ini.
2. Buat branch fitur baru (`git checkout -b fitur/FiturKeren`).
3. Lakukan commit perubahan Anda (`git commit -m 'Menambahkan fitur keren'`).
4. Push ke branch Anda (`git push origin fitur/FiturKeren`).
5. Buat **Pull Request**.

---

## 📄 Lisensi

Didistribusikan di bawah lisensi Apache License 2.0. Lihat berkas `LICENSE` untuk informasi selengkapnya.

---

<div align="center">
Dibuat dengan ❤️ untuk seluruh mahasiswa Indonesia 🇲🇨
</div>
