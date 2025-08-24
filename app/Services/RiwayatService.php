<?php

namespace App\Services;

use App\Models\Riwayat;

class RiwayatService
{
    /**
     * Catat riwayat aktivitas barang
     */
    public static function catatAktivitas($barangId, $tipe, $jumlah, $tanggal = null)
    {
        try {
            if (!$tanggal) {
                $tanggal = time() * 1000; // Convert ke milliseconds seperti Android
            }

            Riwayat::create([
                'barang_id' => $barangId,
                'tanggal' => $tanggal,
                'jumlah' => $jumlah,
                'tipe' => $tipe
            ]);

            return true;
        } catch (\Exception $e) {
            \Log::error('Error saat mencatat riwayat: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * Catat riwayat penambahan stok
     */
    public static function catatTambahStok($barangId, $jumlah, $tanggal = null)
    {
        return self::catatAktivitas($barangId, 'tambah_stok', $jumlah, $tanggal);
    }

    /**
     * Catat riwayat pengurangan stok
     */
    public static function catatKurangiStok($barangId, $jumlah, $tanggal = null)
    {
        return self::catatAktivitas($barangId, 'kurangi_stok', $jumlah, $tanggal);
    }

    /**
     * Catat riwayat pembuatan barang
     */
    public static function catatCreateBarang($barangId, $tanggal = null)
    {
        return self::catatAktivitas($barangId, 'create', 0, $tanggal);
    }

    /**
     * Catat riwayat update barang
     */
    public static function catatUpdateBarang($barangId, $tanggal = null)
    {
        return self::catatAktivitas($barangId, 'update', 0, $tanggal);
    }

    /**
     * Catat riwayat penghapusan barang
     */
    public static function catatDeleteBarang($barangId, $tanggal = null)
    {
        return self::catatAktivitas($barangId, 'delete', 0, $tanggal);
    }
}
