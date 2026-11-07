# Rombak Total Sistem Pengelolaan Cabang, Isolasi Data & Pembersihan Permanen

Rencana arsitektur dan perombakan komprehensif untuk **Sistem Pengelolaan Cabang (Branch Management System)** pada aplikasi POS Waffleo. Sistem ini memecahkan akar masalah kemunculan data lama ("pesanan ghoib" / riwayat terhapus) ketika pengguna membuat cabang baru dengan nama yang sama dengan cabang yang pernah dihapus.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan konfirmasi dari pengguna pada Phase 1, dua keputusan utama arsitektur telah ditetapkan:

- **Keputusan 1 (Identitas Unik Cabang)**:
  - *Pilihan Terkonfirmasi*: Menggunakan **UUID v4 acak murni** (`branch_9f8a3c21-4d32-4e12-89ab-123456789abc`) untuk setiap cabang baru.
  - *Dampak*: Nama cabang (misal: `"Gabek"`) hanya berfungsi sebagai label tampilan visual (`name` / `displayName`), bukan kunci database utama. Cabang baru dengan nama sama akan memperoleh UUID yang 100% berbeda.
- **Keputusan 2 (Mekanisme Deletion Fence & Pembersihan)**:
  - *Pilihan Terkonfirmasi*: Terapkan **Deletion Fence Cloud & Lokal + Pembersihan Permanen Menyeluruh**.
  - *Dampak*: Saat cabang dihapus, ID UUID-nya dimasukkan ke daftar penanda `deletedBranchIds` permanen di Cloud dan lokal. Seluruh data bisnis dihapus total. Jika perangkat offline menyambung kembali nanti dan mencoba mengirim data dengan ID lama, Deletion Fence akan menolak dan membuang antrean tersebut secara otomatis.

---

## 1. Overview & Core Concept

### Akar Masalah Sebelumnya
Sebelumnya, sistem menggunakan slug nama cabang (misal: `"gabek"` atau `"cabang gabek"`) sebagai identifier di database lokal dan Firestore. Ketika cabang Gabek dihapus lalu dibuat kembali dengan nama yang sama:
1. Sistem menghapus penanda penghapusan `"gabek"` agar cabang baru bisa beroperasi.
2. Data sisa (seperti Order History, Erased Items, antrean sync lokal, atau cache) yang menggunakan `branchId = "Gabek"` mendadak dianggap valid kembali dan muncul di cabang baru.

### Solusi Arsitektur Baru
- Setiap pembuatan cabang baru menghasilkan **Branch ID UUID unik baru**.
- Nama cabang `"Gabek"` dapat digunakan berulang kali tanpa batasan, namun setiap pembuatan menghasilkan entitas baru yang sepenuhnya bersih dan independen.
- Seluruh relasi data (`OrderRecord`, `CatalogRecord`, `StaffAccount`, `MoneyTransaction`, `AuditLogRecord`, `MoneyBookmark`) terikat secara eksplisit pada UUID cabang tersebut.

---

## 2. User Experience & Visual Design

### 1. Navigasi & Pemilihan Cabang di Header
- Baris atas header POS & Dashboard menampilkan nama cabang aktif (contoh: `📍 Cabang Gabek`).
- Dropdown pemilih cabang menampilkan nama cabang beserta indikator status (Toko Buka / Tutup).
- Secara internal, pemilih cabang menyimpan dan mengoperasikan `branchId` (UUID), bukan string nama.

### 2. Dialog Pembuatan Cabang Baru
- Admin/Owner dapat memasukkan Nama Cabang (misal: `"Gabek"`), Lokasi, dan Akun Owner.
- Form tidak memblokir atau melarang penggunaan nama cabang yang pernah dihapus sebelumnya.
- Pembuatan cabang baru berjalan mulus dengan indikator progress dan langsung menginisialisasi kondisi bersih (0 transaksi, 0 pesanan lama, 0 riwayat terhapus).

### 3. Dialog Penghapusan Cabang Permanen
- Dialog verifikasi penghapusan cabang membutuhkan konfirmasi kredensial Developer.
- Peringatan tegas: *"Penghapusan cabang bersifat permanen. Seluruh transaksi, pesanan, riwayat, dan akun staff cabang ini akan dihapus total dan tidak dapat dipulihkan."*
- Setelah konfirmasi, sistem menjalankan pembersihan bertahap (Local Store -> Cloud Firestore -> Sync Queue) dan otomatis memindahkan sesi pengguna ke cabang utama (Global).

---

## 3. Key Product Decisions & Trade-Offs

### 1. Struktur Model Data & Relasi
- **`BranchProfile`**:
  - `branchId: String` (UUID unik, contoh: `branch_e8a2b1c4-9012-4f3a-b890-123456789abc`)
  - `name: String` (Label nama cabang, contoh: `"Gabek"`)
  - `location`, `ownerName`, `ownerEmail`, `ownerPhone`, `createdAtText`
- **Tabel / Koleksi Terkait**:
  - Semua record (`OrderRecord`, `CatalogRecord`, `StaffAccount`, `MoneyTransaction`) menyimpan `branchId` berbasis UUID ini.

### 2. Deletion Fence (Pencegah Kebangkitan Data Offline)
- `deletedBranchIds` menyimpan array UUID cabang yang telah dihapus di `workspace_settings` Firestore dan `SharedPreferences` lokal.
- Setiap kali data di-load atau antrean offline di-sync, validator memeriksa:
  ```kotlin
  if (branchId in deletedBranchIds) {
      // Discard payload & purge local copy
  }
  ```
- Penanda `deletedBranchIds` bersifat permanen untuk UUID tersebut dan **TIDAK PERNAH** dibersihkan meskipun cabang bernama `"Gabek"` dibuat kembali dengan UUID baru.

---

## 4. Technical Architecture & Data Strategy

### Visual System Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           WAFFLEO POS CLIENT                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────┐    creates    ┌────────────────────────────┐  │
│  │ Create Branch Dialog │──────────────>│ BranchProfile              │  │
│  └──────────────────────┘               │ - branchId: "branch_UUID_B"│  │
│                                         │ - name: "Gabek"            │  │
│                                         └────────────────────────────┘  │
│                                                        │                │
│                                                        ▼                │
│                                         ┌────────────────────────────┐  │
│                                         │ New Clean Data Store       │  │
│                                         │ - Orders (0)               │  │
│                                         │ - Erased History (0)       │  │
│                                         │ - Transactions (0)         │  │
│                                         └────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │  Sync & Validate via Deletion Fence
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        CLOUD FIRESTORE BACKEND                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ deletedBranchIds: ["branch_UUID_A"] (Permanent Tombstone Fence)   │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌──────────────────────────────┐     ┌──────────────────────────────┐  │
│  │ branchProfiles/branch_UUID_A │     │ branchProfiles/branch_UUID_B │  │
│  │ (PURGED & BLOCKED)           │     │ (ACTIVE & INDEPENDENT)       │  │
│  └──────────────────────────────┘     └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Step-by-Step Implementation Roadmap

### Langkah 1: Refactoring Model & Helper ID Cabang (`WaffleoModels.kt` & `WaffleoRepository.kt`)
- Perbarui `BranchProfile` agar mendukung `name: String` secara terpisah dari `branchId: String`.
- Buat fungsi utilitas pembuat UUID cabang:
  ```kotlin
  fun generateUniqueBranchId(): String = "branch_" + UUID.randomUUID().toString()
  ```
- Perbarui pembentukan Document ID Firestore agar mengabaikan slug nama dan menggunakan `branchId` (UUID) murni.

### Langkah 2: Rombak Logika Penambahan Cabang (`WaffleoViewModel.kt`)
- Dalam `addBranchWithProfile(...)`:
  1. Hasilkan `newBranchId = generateUniqueBranchId()`.
  2. Buat `BranchProfile(branchId = newBranchId, name = cleanBranch, ...)`.
  3. Buat akun owner dengan `branchId = newBranchId`.
  4. Jika menyalin menu dari cabang lain (misal Global), buat salinan `CatalogRecord` dengan `branchId = newBranchId` dan ID produk baru yang unik.
  5. Pastikan `newBranchId` TIDAK menghapus tombstone `deletedBranchIds` lama.

### Langkah 3: Rombak Logika Penghapusan Permanen (`WaffleoViewModel.kt` & `WaffleoRepository.kt`)
- Dalam `deleteBranchPermanently(targetBranchId)`:
  1. Masukkan `targetBranchId` (UUID) ke dalam `deletedBranchIds` lokal dan Firestore Cloud.
  2. Eksekusi pembersihan cascade di local store (`orders`, `catalog`, `staffAccounts`, `moneyTransactions`, `moneyBookmarks`, `pendingSyncKeys`).
  3. Eksekusi `deleteBranchCascadeInCloud` di Firestore yang menghapus seluruh dokumen dengan `branchId == targetBranchId` (termasuk pesanan aktif, selesai, dibatalkan, dan terhapus/erased).
  4. Hapus preferensi lokal status toko (`KEY_STORE_OPEN_PREFIX_$targetBranchId`).

### Langkah 4: Pengamanan Sync Offline-Online & Deletion Fence (`WaffleoLocalStore.kt` & `WaffleoRepository.kt`)
- Di `WaffleoLocalStore.kt` dan handler sync background:
  - Sebelum mengunggah atau memproses antrean `pendingSyncKeys`, periksa apakah `item.branchId in deletedBranchIds`.
  - Jika ya, hapus antrean lokal tersebut tanpa mengunggahnya ke server.

### Langkah 5: Audit Isolasi Data pada POS, Orders, Order History, dan Erased History
- Pastikan seluruh query dan perbandingan filter di:
  - `OrdersScreen.kt`
  - `OrderHistoryScreen.kt`
  - `ErasedOrdersDialog` / `ErasedHistory`
  - `DashboardAndSettingsScreen.kt`
  - `MoneyManagerScreen.kt`
  menggunakan perbandingan presisi: `order.branchId == activeSession.branchId` (UUID).

---

## 6. Verification & Test Scenarios

Sebelum pekerjaan dinyatakan selesai, 6 skenario wajib dari petunjuk pengguna akan diuji:

1. **Skenario 1 — Penghapusan Cabang Lama**:
   - Buat cabang Gabek (UUID-A), tambah transaksi & erased history, lalu hapus. Verifikasi bahwa seluruh data UUID-A terhapus bersih di Firestore dan lokal.
2. **Skenario 2 — Membuat Kembali Cabang Nama Identik**:
   - Buat cabang baru bernama "Gabek" (UUID-B). Verifikasi bahwa UUID-B != UUID-A, dan cabang baru dimulai 100% kosong (0 transaksi, 0 pesanan lama, 0 erased history).
3. **Skenario 3 — Perangkat Offline Kembali Online**:
   - Simulasikan antrean lokal untuk UUID-A saat offline, lalu sambungkan online setelah UUID-A dihapus. Verifikasi Deletion Fence membuang data UUID-A tanpa mengunggahnya.
4. **Skenario 4 — Non-Interferensi Cabang Lain**:
   - Siapkan cabang lain (misal Global, Cabang B). Hapus satu cabang dan pastikan cabang lain tidak mengalami perubahan/kehilangan data.
5. **Skenario 5 — Pencegahan Pemulihan dari Backup / Re-sync**:
   - Jalankan re-sync snapshot; verifikasi data dari UUID-A yang ada di tombstone ditolak dan dibersihkan.
6. **Skenario 6 — Pembuatan & Penghapusan Nama Sama Ulang Kali**:
   - Buat dan hapus cabang "Gabek" 3x berturut-turut. Verifikasi pengguna selalu dapat membuat cabang bernama Gabek dan setiap cabang memperoleh UUID baru yang terisolasi.
