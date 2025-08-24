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
     * Search riwayat berdasarkan berbagai kriteria
     */
    public function search(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'keyword' => 'nullable|string|min:2',
            'barang_id' => 'nullable|exists:barangs,id',
            'tipe' => 'nullable|string|in:tambah_stok,kurangi_stok,create,update,delete',
            'dari' => 'nullable|date',
            'sampai' => 'nullable|date|after_or_equal:dari',
            'min_jumlah' => 'nullable|numeric',
            'max_jumlah' => 'nullable|numeric',
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
            $query = Riwayat::with(['barang']);

            // Search berdasarkan keyword (nama barang, barcode)
            if ($request->has('keyword') && $request->keyword) {
                $keyword = $request->keyword;
                $query->whereHas('barang', function($q) use ($keyword) {
                    $q->where('nama', 'LIKE', "%{$keyword}%")
                      ->orWhere('barcode', 'LIKE', "%{$keyword}%");
                });
            }

            // Filter berdasarkan barang_id
            if ($request->has('barang_id') && $request->barang_id) {
                $query->barang($request->barang_id);
            }

            // Filter berdasarkan tipe
            if ($request->has('tipe') && $request->tipe) {
                $query->tipe($request->tipe);
            }

            // Filter berdasarkan range tanggal
            if ($request->has('dari') && $request->has('sampai')) {
                $query->rangeTanggal($request->dari, $request->sampai);
            }

            // Filter berdasarkan range jumlah
            if ($request->has('min_jumlah') && $request->min_jumlah !== null) {
                $query->where('jumlah', '>=', $request->min_jumlah);
            }
            if ($request->has('max_jumlah') && $request->max_jumlah !== null) {
                $query->where('jumlah', '<=', $request->max_jumlah);
            }

            // Pagination
            $perPage = $request->get('per_page', 15);
            $riwayat = $query->orderBy('tanggal', 'desc')->paginate($perPage);

            return response()->json([
                'success' => true,
                'message' => 'Pencarian riwayat berhasil',
                'data' => $riwayat,
                'filters' => [
                    'keyword' => $request->keyword,
                    'barang_id' => $request->barang_id,
                    'tipe' => $request->tipe,
                    'dari' => $request->dari,
                    'sampai' => $request->sampai,
                    'min_jumlah' => $request->min_jumlah,
                    'max_jumlah' => $request->max_jumlah
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat melakukan pencarian riwayat',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get riwayat berdasarkan periode waktu
     */
    public function getRiwayatByPeriod(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'period' => 'required|string|in:today,yesterday,this_week,last_week,this_month,last_month,this_year,last_year',
            'tipe' => 'nullable|string|in:tambah_stok,kurangi_stok,create,update,delete',
            'barang_id' => 'nullable|exists:barangs,id',
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
            $query = Riwayat::with(['barang']);

            // Filter berdasarkan periode
            $period = $request->period;
            switch ($period) {
                case 'today':
                    $query->tanggal(date('Y-m-d'));
                    break;
                case 'yesterday':
                    $query->tanggal(date('Y-m-d', strtotime('-1 day')));
                    break;
                case 'this_week':
                    $query->rangeTanggal(
                        date('Y-m-d', strtotime('monday this week')),
                        date('Y-m-d', strtotime('sunday this week'))
                    );
                    break;
                case 'last_week':
                    $query->rangeTanggal(
                        date('Y-m-d', strtotime('monday last week')),
                        date('Y-m-d', strtotime('sunday last week'))
                    );
                    break;
                case 'this_month':
                    $query->rangeTanggal(
                        date('Y-m-01'),
                        date('Y-m-t')
                    );
                    break;
                case 'last_month':
                    $query->rangeTanggal(
                        date('Y-m-01', strtotime('last month')),
                        date('Y-m-t', strtotime('last month'))
                    );
                    break;
                case 'this_year':
                    $query->rangeTanggal(
                        date('Y-01-01'),
                        date('Y-12-31')
                    );
                    break;
                case 'last_year':
                    $query->rangeTanggal(
                        date('Y-01-01', strtotime('last year')),
                        date('Y-12-31', strtotime('last year'))
                    );
                    break;
            }

            // Filter berdasarkan tipe
            if ($request->has('tipe') && $request->tipe) {
                $query->tipe($request->tipe);
            }

            // Filter berdasarkan barang_id
            if ($request->has('barang_id') && $request->barang_id) {
                $query->barang($request->barang_id);
            }

            // Pagination
            $perPage = $request->get('per_page', 15);
            $riwayat = $query->orderBy('tanggal', 'desc')->paginate($perPage);

            return response()->json([
                'success' => true,
                'message' => "Riwayat periode {$period} berhasil diambil",
                'data' => $riwayat,
                'period' => $period
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Terjadi kesalahan saat mengambil riwayat berdasarkan periode',
                'error' => $e->getMessage()
            ], 500);
        }
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
