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
                'nama' => 'Laptop ASUS VivoBook',
                'kategori' => 'Elektronik',
                'stok' => 15,
                'harga' => 8500000.00,
                'modal' => 7200000.00,
                'barcode' => '1234567890123',
                'gambar_path' => 'images/laptop-asus.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Mouse Wireless Logitech',
                'kategori' => 'Aksesoris Komputer',
                'stok' => 50,
                'harga' => 250000.00,
                'modal' => 180000.00,
                'barcode' => '2345678901234',
                'gambar_path' => 'images/mouse-logitech.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Smartphone Samsung Galaxy',
                'kategori' => 'Elektronik',
                'stok' => 25,
                'harga' => 4500000.00,
                'modal' => 3800000.00,
                'barcode' => '3456789012345',
                'gambar_path' => 'images/samsung-galaxy.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Keyboard Mechanical RGB',
                'kategori' => 'Aksesoris Komputer',
                'stok' => 30,
                'harga' => 750000.00,
                'modal' => 550000.00,
                'barcode' => '4567890123456',
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Monitor LED 24 inch',
                'kategori' => 'Elektronik',
                'stok' => 12,
                'harga' => 2200000.00,
                'modal' => 1800000.00,
                'barcode' => null,
                'gambar_path' => 'images/monitor-24inch.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Flashdisk 32GB',
                'kategori' => 'Storage',
                'stok' => 100,
                'harga' => 85000.00,
                'modal' => 65000.00,
                'barcode' => '5678901234567',
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Headset Gaming',
                'kategori' => 'Aksesoris Gaming',
                'stok' => 20,
                'harga' => 450000.00,
                'modal' => 320000.00,
                'barcode' => '6789012345678',
                'gambar_path' => 'images/headset-gaming.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Webcam HD 1080p',
                'kategori' => 'Aksesoris Komputer',
                'stok' => 18,
                'harga' => 350000.00,
                'modal' => 280000.00,
                'barcode' => null,
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Power Bank 10000mAh',
                'kategori' => 'Aksesoris Mobile',
                'stok' => 75,
                'harga' => 180000.00,
                'modal' => 135000.00,
                'barcode' => '7890123456789',
                'gambar_path' => 'images/powerbank-10k.jpg',
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
            [
                'nama' => 'Charger Laptop Universal',
                'kategori' => 'Aksesoris Komputer',
                'stok' => 40,
                'harga' => 125000.00,
                'modal' => 95000.00,
                'barcode' => '8901234567890',
                'gambar_path' => null,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ],
        ];

        DB::table('barangs')->insert($barangs);
    }
}
