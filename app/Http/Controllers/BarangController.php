<?php

namespace App\Http\Controllers;

use App\Models\barang;
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

    public function plustobasestock(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:barangs,id',
            'stok' => 'required|numeric|min:0.01',
        ]);
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }
        try {
            $barang = barang::findOrFail($request->id);
            $barang->stok += $request->stok;
            $barang->save();
            return response()->json(['message' => 'Stok barang berhasil ditambahkan'], 200);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menambahkan stok barang',
                'error' => $e->getMessage()
            ], 500);
        }
    }
    
    public function checkstok($kode_barang)
    {
        $barang = barang::where('barcode', $kode_barang)->first();
        if (!$barang) {
            return response()->json(['message' => 'Barang tidak ditemukan'], 404);
        }
        return response()->json($barang->stok);
    }

    public function mstobasestock(Request $request)
    {
        // Validasi input
        $validator = Validator::make($request->all(), [
            'kode_barang' => 'required|string|max:255',
            'stok' => 'required|numeric|min:0.01',
            'keterangan' => 'nullable|string|max:500',
            'user_id' => 'nullable|exists:users,id'
        ], [
            'kode_barang.required' => 'Kode barang harus diisi',
            'kode_barang.string' => 'Kode barang harus berupa teks',
            'stok.required' => 'Jumlah stok harus diisi',
            'stok.numeric' => 'Jumlah stok harus berupa angka',
            'stok.min' => 'Jumlah stok minimal 0.01',
            'keterangan.string' => 'Keterangan harus berupa teks',
            'keterangan.max' => 'Keterangan maksimal 500 karakter',
            'user_id.exists' => 'User ID tidak valid'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // Cari barang berdasarkan barcode
            $barang = barang::where('barcode', $request->kode_barang)->first();
            
            if (!$barang) {
                return response()->json([
                    'success' => false,
                    'message' => 'Barang tidak ditemukan',
                    'data' => ['kode_barang' => $request->kode_barang]
                ], 404);
            }

            // Cek apakah barang aktif
            if (isset($barang->status) && $barang->status !== 'active') {
                return response()->json([
                    'success' => false,
                    'message' => 'Barang tidak aktif atau sudah dihapus',
                    'data' => ['kode_barang' => $request->kode_barang]
                ], 400);
            }

            $stokLama = $barang->stok;
            $stokBaru = $stokLama - $request->stok;

            // Validasi stok cukup
            if ($stokBaru < 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'Stok tidak cukup',
                    'data' => [
                        'stok_tersedia' => $stokLama,
                        'stok_diminta' => $request->stok,
                        'kekurangan' => abs($stokBaru)
                    ]
                ], 400);
            }

            // Update stok barang
            $barang->stok = $stokBaru;
            $barang->updated_at = now();
            
            // Tambahkan field tambahan jika ada
            if ($request->has('keterangan')) {
                $barang->keterangan = $request->keterangan;
            }
            
            if ($request->has('user_id')) {
                $barang->last_updated_by = $request->user_id;
            }

            $barang->save();

            Log::info('Stok barang berhasil diupdate', [
                'barang_id' => $barang->id,
                'kode_barang' => $barang->barcode,
                'stok_lama' => $stokLama,
                'stok_baru' => $stokBaru,
                'pengurangan' => $request->stok,
                'user_id' => $request->user_id ?? 'system',
                'timestamp' => now()
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Stok barang berhasil diupdate',
                'data' => [
                    'barang' => $barang,
                    'stok_lama' => $stokLama,
                    'stok_baru' => $stokBaru,
                    'pengurangan' => $request->stok
                ]
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error saat update stok barang: ' . $e->getMessage(), [
                'kode_barang' => $request->kode_barang,
                'stok' => $request->stok,
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan sistem',
                'error' => config('app.debug') ? $e->getMessage() : 'Internal server error'
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
            $barang->update($request->only([
                'nama', 'kategori', 'stok', 'harga', 'modal', 'barcode', 'gambar_path'
            ]));

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
