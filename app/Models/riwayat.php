<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Riwayat extends Model
{
    use HasFactory;

    protected $table = 'riwayats';

    protected $fillable = [
        'barang_id',
        'tanggal',
        'jumlah',
        'tipe'
    ];

    protected $casts = [
        'tanggal' => 'integer',
        'jumlah' => 'integer'
    ];

    // Relasi dengan model Barang
    public function barang()
    {
        return $this->belongsTo(Barang::class);
    }

    // Scope untuk filter berdasarkan tipe
    public function scopeTipe($query, $tipe)
    {
        return $query->where('tipe', $tipe);
    }

    // Scope untuk filter berdasarkan barang
    public function scopeBarang($query, $barangId)
    {
        return $query->where('barang_id', $barangId);
    }

    // Scope untuk filter berdasarkan tanggal (dari timestamp)
    public function scopeTanggal($query, $tanggal)
    {
        $startOfDay = strtotime($tanggal . ' 00:00:00') * 1000; // Convert ke milliseconds
        $endOfDay = strtotime($tanggal . ' 23:59:59') * 1000;
        return $query->whereBetween('tanggal', [$startOfDay, $endOfDay]);
    }

    // Scope untuk filter berdasarkan range tanggal
    public function scopeRangeTanggal($query, $dari, $sampai)
    {
        $startTimestamp = strtotime($dari . ' 00:00:00') * 1000;
        $endTimestamp = strtotime($sampai . ' 23:59:59') * 1000;
        return $query->whereBetween('tanggal', [$startTimestamp, $endTimestamp]);
    }

    // Accessor untuk mendapatkan tanggal dalam format yang mudah dibaca
    public function getTanggalFormattedAttribute()
    {
        return date('Y-m-d H:i:s', $this->tanggal / 1000);
    }

    // Accessor untuk mendapatkan tanggal saja
    public function getTanggalOnlyAttribute()
    {
        return date('Y-m-d', $this->tanggal / 1000);
    }

    // Accessor untuk mendapatkan waktu saja
    public function getWaktuOnlyAttribute()
    {
        return date('H:i:s', $this->tanggal / 1000);
    }
}
