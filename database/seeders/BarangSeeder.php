<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class BarangSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        $barangs = [
            [
                'nama' => 'Keripik Singkong Balado',
                'kategori' => 'Makanan Ringan',
                'stok' => 200,
                'harga' => 15000.00,
                'modal' => 10000.00,
                'barcode' => '1112223334445',
                'gambar_path' => 'images/keripik-singkong.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Kopi Bubuk Arabica Lokal',
                'kategori' => 'Minuman',
                'stok' => 120,
                'harga' => 35000.00,
                'modal' => 25000.00,
                'barcode' => '2223334445556',
                'gambar_path' => 'images/kopi-arabica.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Tas Anyaman Rotan',
                'kategori' => 'Kerajinan',
                'stok' => 30,
                'harga' => 120000.00,
                'modal' => 85000.00,
                'barcode' => '3334445556667',
                'gambar_path' => 'images/tas-rotan.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Batik Tulis Pekalongan',
                'kategori' => 'Fashion',
                'stok' => 15,
                'harga' => 250000.00,
                'modal' => 180000.00,
                'barcode' => '4445556667778',
                'gambar_path' => 'images/batik-tulis.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Sambal Botol Homemade',
                'kategori' => 'Makanan',
                'stok' => 60,
                'harga' => 20000.00,
                'modal' => 12000.00,
                'barcode' => '5556667778889',
                'gambar_path' => 'images/sambal-botol.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Kerajinan Kayu Miniatur',
                'kategori' => 'Kerajinan',
                'stok' => 25,
                'harga' => 175000.00,
                'modal' => 120000.00,
                'barcode' => '6667778889990',
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Brownies Kukus Cokelat',
                'kategori' => 'Makanan Ringan',
                'stok' => 40,
                'harga' => 30000.00,
                'modal' => 20000.00,
                'barcode' => '7778889990001',
                'gambar_path' => 'images/brownies.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Minuman Jahe Instan',
                'kategori' => 'Minuman',
                'stok' => 70,
                'harga' => 18000.00,
                'modal' => 12000.00,
                'barcode' => '8889990001112',
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Dompet Kulit Handmade',
                'kategori' => 'Fashion',
                'stok' => 20,
                'harga' => 95000.00,
                'modal' => 65000.00,
                'barcode' => '9990001112223',
                'gambar_path' => 'images/dompet-kulit.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Kerupuk Ikan Asli',
                'kategori' => 'Makanan Ringan',
                'stok' => 90,
                'harga' => 12000.00,
                'modal' => 8000.00,
                'barcode' => null,
                'gambar_path' => 'images/kerupuk-ikan.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
        ];

        DB::table('barangs')->insert($barangs);
    }
}
