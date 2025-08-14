<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('barangs', function (Blueprint $table) {
            $table->id(); // PrimaryKey auto increment
            $table->string('nama');
            $table->string('kategori');
            $table->integer('stok');
            $table->double('harga', 15,2);  
            $table->double('modal', 15, 2);
            $table->string('barcode')->nullable();
            $table->string('gambar_path')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('barangs');
    }
};
