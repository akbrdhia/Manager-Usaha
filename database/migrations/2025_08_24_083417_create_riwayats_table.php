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
        Schema::create('riwayats', function (Blueprint $table) {
            $table->id(); // PrimaryKey auto increment
            $table->unsignedBigInteger('barang_id'); // barangId dari Android
            $table->bigInteger('tanggal'); // timestamp dalam format Long dari Android
            $table->integer('jumlah'); // jumlah dari Android
            $table->string('tipe'); // tipe dari Android (tambah_stok, kurangi_stok, dll)
            $table->timestamps(); // created_at dan updated_at
            
            // Foreign key constraints
            $table->foreign('barang_id')->references('id')->on('barangs')->onDelete('cascade');
            
            // Indexes untuk performa query
            $table->index(['barang_id', 'tanggal']);
            $table->index(['tipe', 'tanggal']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('riwayats');
    }
};
