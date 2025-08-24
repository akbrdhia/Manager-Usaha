# API Routes Documentation

## Barang Endpoints

### Basic CRUD
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| GET | `/api/barang` | BarangController@index | Get all barang |
| GET | `/api/allbarang` | BarangController@index | Get all barang (alias) |
| POST | `/api/barang` | BarangController@store | Create new barang |
| GET | `/api/barang/{kode_barang}` | BarangController@show | Get barang by code |
| PUT/PATCH | `/api/barang/{id}` | BarangController@update | Update barang |
| DELETE | `/api/barang/{id}` | BarangController@destroy | Delete barang |

### Kategori & Search
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| GET | `/api/barang/kategori` | BarangController@getKategori | Get all categories |
| GET | `/api/barang/kategori/{kategori}` | BarangController@getBarangByKategori | Get barang by category |
| GET | `/api/barang/search` | BarangController@search | Search barang |

### Stock Management
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| POST | `/api/plustobasestock` | BarangController@plustobasestock | Add stock |
| POST | `/api/minstobasestock` | BarangController@mstobasestock | Reduce stock |

---

## Riwayat Endpoints

### Basic CRUD
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| GET | `/api/riwayat` | RiwayatController@index | Get all riwayat |
| POST | `/api/riwayat` | RiwayatController@store | Create new riwayat |
| GET | `/api/riwayat/{id}` | RiwayatController@show | Get riwayat by ID |

### Filtering & Search
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| GET | `/api/riwayat/barang/{barangId}` | RiwayatController@getRiwayatBarang | Get riwayat by barang ID |
| GET | `/api/riwayat/tipe/{tipe}` | RiwayatController@getRiwayatByTipe | Get riwayat by type |
| GET | `/api/riwayat/period` | RiwayatController@getRiwayatByPeriod | Get riwayat by date range |
| GET | `/api/riwayat/search` | RiwayatController@search | Search riwayat |

### Reports & Summary
| Method | Endpoint | Controller | Description |
|--------|----------|------------|-------------|
| GET | `/api/riwayat/stok` | RiwayatController@getRiwayatStok | Get stock history |
| GET | `/api/riwayat/summary` | RiwayatController@getSummary | Get summary report |

---

## Quick Reference

**Base URL:** `/api`

**Authentication:** [Add if required]

**Response Format:** JSON

**Error Handling:** Standard HTTP status codes
