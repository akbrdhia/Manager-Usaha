<?php

namespace App\Http\Controllers;

use App\Models\Riwayat;
use App\Models\Barang;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class RiwayatController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index(Request $request)
    {
        $query = Riwayat::with(['barang']);

        // Filter berdasarkan barang_id
        if ($request->has('barang_id')) {
            $query->barang($request->barang_id);
        }

        // Filter berdasarkan tipe
        if ($request->has('tipe')) {
            $query->tipe($request->tipe);
        }

        // Filter berdasarkan tanggal
        if ($request->has('tanggal')) {
            $query->tanggal($request->tanggal);
        }

        // Filter berdasarkan range tanggal
        if ($request->has('dari') && $request->has('sampai')) {
            $query->rangeTanggal($request->dari, $request->sampai);
        }

        // Pagination
        $perPage = $request->get('per_page', 15);
        $riwayat = $query->orderBy('tanggal', 'desc')->paginate($perPage);

        return response()->json([
            'success' => true,
            'data' => $riwayat
        ]);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'barang_id' => 'required|exists:barangs,id',
            'tanggal' => 'required|integer',
            'jumlah' => 'required|integer',
            'tipe' => 'required|string|in:tambah_stok,kurangi_stok,create,update,delete'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $riwayat = Riwayat::create($request->all());

            return response()->json([
                'success' => true,
                'message' => 'Riwayat berhasil disimpan',
                'data' => $riwayat
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat menyimpan riwayat',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Display the specified resource.
     */
    public function show(string $id)
    {
        $riwayat = Riwayat::with(['barang'])->find($id);
        
        if (!$riwayat) {
            return response()->json([
                'success' => false,
                'message' => 'Riwayat tidak ditemukan'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $riwayat
        ]);
    }

    /**
     * Get riwayat berdasarkan barang tertentu
     */
    public function getRiwayatBarang(string $barangId, Request $request)
    {
        $barang = Barang::find($barangId);
        
        if (!$barang) {
            return response()->json([
                'success' => false,
                'message' => 'Barang tidak ditemukan'
            ], 404);
        }

        $query = Riwayat::where('barang_id', $barangId)
            ->orderBy('tanggal', 'desc');

        // Filter berdasarkan tipe
        if ($request->has('tipe')) {
            $query->tipe($request->tipe);
        }

        // Filter berdasarkan range tanggal
        if ($request->has('dari') && $request->has('sampai')) {
            $query->rangeTanggal($request->dari, $request->sampai);
        }

        $perPage = $request->get('per_page', 15);
        $riwayat = $query->paginate($perPage);

        return response()->json([
            'success' => true,
            'data' => [
                'barang' => $barang,
                'riwayat' => $riwayat
            ]
        ]);
    }

    /**
     * Get riwayat berdasarkan tipe
     */
    public function getRiwayatByTipe(string $tipe, Request $request)
    {
        $query = Riwayat::with(['barang'])
            ->tipe($tipe)
            ->orderBy('tanggal', 'desc');

        // Filter berdasarkan range tanggal
        if ($request->has('dari') && $request->has('sampai')) {
            $query->rangeTanggal($request->dari, $request->sampai);
        }

        $perPage = $request->get('per_page', 15);
        $riwayat = $query->paginate($perPage);

        return response()->json([
            'success' => true,
            'data' => $riwayat
        ]);
    }

    /**
     * Get riwayat stok (tambah/kurang stok)
     */
    public function getRiwayatStok(Request $request)
    {
        $query = Riwayat::with(['barang'])
            ->whereIn('tipe', ['tambah_stok', 'kurangi_stok'])
            ->orderBy('tanggal', 'desc');

        // Filter berdasarkan barang
        if ($request->has('barang_id')) {
            $query->barang($request->barang_id);
        }

        // Filter berdasarkan range tanggal
        if ($request->has('dari') && $request->has('sampai')) {
            $query->rangeTanggal($request->dari, $request->sampai);
        }

        $perPage = $request->get('per_page', 15);
        $riwayat = $query->paginate($perPage);

        return response()->json([
            'success' => true,
            'data' => $riwayat
        ]);
    }

    /**
     * Get summary riwayat (statistik)
     */
    public function getSummary(Request $request)
    {
        $query = Riwayat::query();

        // Filter berdasarkan range tanggal
        if ($request->has('dari') && $request->has('sampai')) {
            $query->rangeTanggal($request->dari, $request->sampai);
        }

        $summary = [
            'total_aktivitas' => $query->count(),
            'aktivitas_per_tipe' => $query->selectRaw('tipe, COUNT(*) as total')
                ->groupBy('tipe')
                ->get(),
            'total_tambah_stok' => $query->where('tipe', 'tambah_stok')->sum('jumlah'),
            'total_kurangi_stok' => $query->where('tipe', 'kurangi_stok')->sum('jumlah')
        ];

        return response()->json([
            'success' => true,
            'data' => $summary
        ]);
    }
}
