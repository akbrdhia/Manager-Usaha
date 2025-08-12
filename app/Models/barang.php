<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class barang extends Model
{
    protected $table = 'barangs';
    protected $fillable = ['nama', 'kategori', 'stok', 'harga', 'modal', 'barcode', 'gambar_path'];
}
