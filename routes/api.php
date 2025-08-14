<?php

use App\Http\Controllers\BarangController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::get('/allbarang', [BarangController::class, 'index']);
Route::get('/barang', [BarangController::class, 'index']);
Route::get('/barang/{kode_barang}', [BarangController::class, 'show']);

Route::post('/barang', [BarangController::class, 'store']);
Route::post('/minstobasestock', [BarangController::class, 'mstobasestock']);
Route::post('/plustobasestock', [BarangController::class, 'plustobasestock']);



