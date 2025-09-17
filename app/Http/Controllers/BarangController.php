<?php

namespace App\Http\Controllers;

use App\Models\barang;
use App\Services\RiwayatService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;

class BarangController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $barang = barang::all();
        return response()->json($barang);
    }

    /**
     * Search barang berdasarkan keyword
     */
    public function search(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'keyword' => 'required|string|min:2',
            'kategori' => 'nullable|string',
            'min_stok' => 'nullable|numeric|min:0',
            'max_stok' => 'nullable|numeric|min:0',
            'min_harga' => 'nullable|numeric|min:0',
            'max_harga' => 'nullable|numeric|min:0',
            'per_page' => 'nullable|integer|min:1|max:100'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $query = barang::query();

            // Search berdasarkan keyword (nama, barcode, kategori)
            $keyword = $request->keyword;
            $query->where(function ($q) use ($keyword) {
                $q->where('nama', 'LIKE', "%{$keyword}%")
                    ->orWhere('barcode', 'LIKE', "%{$keyword}%")
                    ->orWhere('kategori', 'LIKE', "%{$keyword}%");
            });

            // Filter berdasarkan kategori
            if ($request->has('kategori') && $request->kategori) {
                $query->where('kategori', $request->kategori);
            }

            // Filter berdasarkan range stok
            if ($request->has('min_stok') && $request->min_stok !== null) {
                $query->where('stok', '>=', $request->min_stok);
            }
            if ($request->has('max_stok') && $request->max_stok !== null) {
                $query->where('stok', '<=', $request->max_stok);
            }

            // Filter berdasarkan range harga
            if ($request->has('min_harga') && $request->min_harga !== null) {
                $query->where('harga', '>=', $request->min_harga);
            }
            if ($request->has('max_harga') && $request->max_harga !== null) {
                $query->where('harga', '<=', $request->max_harga);
            }

            // Pagination
            $perPage = $request->get('per_page', 15);
            $barang = $query->orderBy('nama', 'asc')->paginate($perPage);

            return response()->json([
                'success' => true,
                'message' => 'Pencarian berhasil',
                'data' => $barang,
                'filters' => [
                    'keyword' => $keyword,
                    'kategori' => $request->kategori,
                    'min_stok' => $request->min_stok,
                    'max_stok' => $request->max_stok,
                    'min_harga' => $request->min_harga,
                    'max_harga' => $request->max_harga
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat melakukan pencarian',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get semua kategori barang
     */
    public function getKategori()
    {
        try {
            $kategori = barang::select('kategori')
                ->distinct()
                ->whereNotNull('kategori')
                ->where('kategori', '!=', '')
                ->orderBy('kategori', 'asc')
                ->pluck('kategori');

            return response()->json([
                'success' => true,
                'data' => $kategori
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat mengambil kategori',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get barang berdasarkan kategori
     */
    public function getBarangByKategori(string $kategori, Request $request)
    {
        try {
            $query = barang::where('kategori', $kategori);

            // Filter berdasarkan range stok
            if ($request->has('min_stok') && $request->min_stok !== null) {
                $query->where('stok', '>=', $request->min_stok);
            }
            if ($request->has('max_stok') && $request->max_stok !== null) {
                $query->where('stok', '<=', $request->max_stok);
            }

            // Filter berdasarkan range harga
            if ($request->has('min_harga') && $request->min_harga !== null) {
                $query->where('harga', '>=', $request->min_harga);
            }
            if ($request->has('max_harga') && $request->max_harga !== null) {
                $query->where('harga', '<=', $request->max_harga);
            }

            // Pagination
            $perPage = $request->get('per_page', 15);
            $barang = $query->orderBy('nama', 'asc')->paginate($perPage);

            return response()->json([
                'success' => true,
                'data' => [
                    'kategori' => $kategori,
                    'barang' => $barang
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat mengambil barang berdasarkan kategori',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    public function storeKategori(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'kategori' => 'required|string|max:100',
        ]);
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }
        try {
            $kategori = barang::create([
                'kategori' => $request->kategori
            ]);
            return response()->json([
                'success' => true,
                'message' => 'Kategori berhasil disimpan',
                'data' => $kategori
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menyimpan kategori',
                'error' => $e->getMessage()
            ], 500);
        }
    }
    
    public function plustobasestock(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:barangs,id',
            'stok' => 'required|integer|min:1',
            'keterangan' => 'nullable|string|max:500',
            'user_id' => 'nullable|exists:users,id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'data' => ['errors' => $validator->errors()]
            ], 422);
        }

        try {
            $barang = Barang::findOrFail($request->id);
            $stokLama = (int) $barang->stok;
            $tambah = (int) $request->stok;
            $barang->stok = $stokLama + $tambah;

            if ($request->filled('keterangan')) {
                $barang->keterangan = $request->keterangan;
            }
            if ($request->filled('user_id')) {
                $barang->last_updated_by = $request->user_id;
            }

            $barang->save();

            // Catat riwayat; sesuaikan signature RiwayatService bila perlu
            RiwayatService::catatTambahStok($barang->id, $tambah, $request->user_id ?? null);

            return response()->json([
                'success' => true,
                'message' => 'Stok barang berhasil ditambahkan',
                'data' => [
                    'barang_id' => $barang->id,
                    'stok_lama' => $stokLama,
                    'stok_baru' => $barang->stok,
                    'penambahan' => $tambah
                ]
            ], 200);
        } catch (\Exception $e) {
            Log::error('Error plustobasestock: '.$e->getMessage(), ['trace' => $e->getTraceAsString()]);
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menambahkan stok barang',
                'data' => ['error' => config('app.debug') ? $e->getMessage() : 'Internal server error']
            ], 500);
        }
    }

    // Check stok by barcode (tetap), tapi format response diseragamkan
    public function checkstok($kode_barang)
    {
        $barang = Barang::where('barcode', $kode_barang)->first();
        if (!$barang) {
            return response()->json([
                'success' => false,
                'message' => 'Barang tidak ditemukan',
                'data' => ['kode_barang' => $kode_barang]
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Stok ditemukan',
            'data' => [
                'barang_id' => $barang->id,
                'stok' => (int) $barang->stok
            ]
        ], 200);
    }

    // Sekarang mstobasestock menerima 'id' (divalidasi) dan mengurangi stok
    public function mstobasestock(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:barangs,id',
            'stok' => 'required|integer|min:1',
            'keterangan' => 'nullable|string|max:500',
            'user_id' => 'nullable|exists:users,id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'data' => ['errors' => $validator->errors()]
            ], 422);
        }

        try {
            $barang = Barang::findOrFail($request->id);

            // cek status aktif jika ada kolom status
            if (isset($barang->status) && $barang->status !== 'active') {
                return response()->json([
                    'success' => false,
                    'message' => 'Barang tidak aktif atau sudah dihapus',
                    'data' => ['barang_id' => $barang->id]
                ], 400);
            }

            $stokLama = (int) $barang->stok;
            $kurangi = (int) $request->stok;
            $stokBaru = $stokLama - $kurangi;

            if ($stokBaru < 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'Stok tidak cukup',
                    'data' => [
                        'barang_id' => $barang->id,
                        'stok_tersedia' => $stokLama,
                        'stok_diminta' => $kurangi,
                        'kekurangan' => abs($stokBaru)
                    ]
                ], 400);
            }

            $barang->stok = $stokBaru;
            $barang->updated_at = now();

            if ($request->filled('keterangan')) {
                $barang->keterangan = $request->keterangan;
            }
            if ($request->filled('user_id')) {
                $barang->last_updated_by = $request->user_id;
            }

            $barang->save();

            // Catat riwayat pengurangan stok; sesuaikan signature RiwayatService bila perlu
            RiwayatService::catatKurangiStok($barang->id, $kurangi, $request->user_id ?? null);

            Log::info('Stok barang berhasil diupdate', [
                'barang_id' => $barang->id,
                'stok_lama' => $stokLama,
                'stok_baru' => $stokBaru,
                'pengurangan' => $kurangi,
                'user_id' => $request->user_id ?? 'system',
                'timestamp' => now()
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Stok barang berhasil dikurangi',
                'data' => [
                    'barang' => $barang,
                    'stok_lama' => $stokLama,
                    'stok_baru' => $stokBaru,
                    'pengurangan' => $kurangi
                ]
            ], 200);
        } catch (\Exception $e) {
            Log::error('Error mstobasestock: '.$e->getMessage(), [
                'id' => $request->id ?? null,
                'stok' => $request->stok ?? null,
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat mengurangi stok barang',
                'data' => ['error' => config('app.debug') ? $e->getMessage() : 'Internal server error']
            ], 500);
        }
    }
    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        // Method ini biasanya digunakan untuk form HTML, 
        // tapi karena ini API, kita bisa skip atau return view
        return response()->json([
            'message' => 'Gunakan POST /barang untuk membuat barang baru'
        ], 405);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validatedData = $request->validate([
            'nama' => 'required|string|max:255',
            'kategori' => 'required|string|max:100',
            'stok' => 'required|numeric|min:0',
            'harga' => 'required|numeric|min:0',
            'modal' => 'required|numeric|min:0',
            'barcode' => 'nullable|string|max:100|unique:barangs,barcode',
            'gambar_path' => 'nullable|string|max:255',
        ]);

        try {
            $barang = barang::create($validatedData);

            // Catat riwayat pembuatan barang
            RiwayatService::catatCreateBarang($barang->id);

            return response()->json([
                'success' => true,
                'message' => 'Barang berhasil disimpan',
                'data' => $barang
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menyimpan barang',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Display the specified resource.
     */
    public function show(string $kode_barang)
    {
        $barang = barang::where('barcode', $kode_barang)->first();
        if (!$barang) {
            return response()->json(['message' => 'Barang tidak ditemukan'], 404);
        }
        return response()->json($barang);
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(string $id)
    {
        // Method ini biasanya digunakan untuk form HTML edit, 
        // tapi karena ini API, kita bisa skip atau return view
        return response()->json([
            'message' => 'Gunakan PUT /barang/{id} untuk update barang'
        ], 405);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, string $id)
    {
        $validator = Validator::make($request->all(), [
            'nama' => 'sometimes|required|string|max:255',
            'kategori' => 'sometimes|required|string|max:100',
            'stok' => 'sometimes|required|numeric|min:0',
            'harga' => 'sometimes|required|numeric|min:0',
            'modal' => 'sometimes|required|numeric|min:0',
            'barcode' => 'sometimes|nullable|string|max:100|unique:barangs,barcode,' . $id,
            'gambar_path' => 'sometimes|nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $barang = barang::findOrFail($id);
            $dataLama = $barang->toArray();

            $barang->update($request->only([
                'nama',
                'kategori',
                'stok',
                'harga',
                'modal',
                'barcode',
                'gambar_path'
            ]));

            // Catat riwayat update barang
            RiwayatService::catatUpdateBarang($barang->id);

            return response()->json([
                'success' => true,
                'message' => 'Barang berhasil diupdate',
                'data' => $barang
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat mengupdate barang',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        try {
            $barang = barang::findOrFail($id);

            // Catat riwayat penghapusan barang sebelum dihapus
            RiwayatService::catatDeleteBarang($barang->id);

            $barang->delete();

            return response()->json([
                'success' => true,
                'message' => 'Barang berhasil dihapus'
            ], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menghapus barang',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
