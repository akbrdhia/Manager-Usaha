<?php

use App\Http\Controllers\BarangController;
use App\Http\Controllers\RiwayatController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

//get
Route::get('/allbarang', [BarangController::class, 'index']);
Route::get('/barang', [BarangController::class, 'index']);
Route::get('/barang/{kode_barang}', [BarangController::class, 'show']);

// post
Route::post('/barang', [BarangController::class, 'store']);
Route::post('/minstobasestock', [BarangController::class, 'mstobasestock']);
Route::post('/plustobasestock', [BarangController::class, 'plustobasestock']);

// put/patch untuk update
Route::put('/barang/{id}', [BarangController::class, 'update']);
Route::patch('/barang/{id}', [BarangController::class, 'update']);

// delete
Route::delete('/barang/{id}', [BarangController::class, 'destroy']);

// Routes untuk Riwayat
Route::get('/riwayat', [RiwayatController::class, 'index']);
Route::get('/riwayat/{id}', [RiwayatController::class, 'show']);
Route::post('/riwayat', [RiwayatController::class, 'store']);
Route::get('/riwayat/barang/{barangId}', [RiwayatController::class, 'getRiwayatBarang']);
Route::get('/riwayat/tipe/{tipe}', [RiwayatController::class, 'getRiwayatByTipe']);
Route::get('/riwayat/stok', [RiwayatController::class, 'getRiwayatStok']);
Route::get('/riwayat/summary', [RiwayatController::class, 'getSummary']);



