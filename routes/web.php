<?php

use App\Http\Controllers\BarangController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json([
        'message' => 'are you lost?',
        'status' => 'success'
    ]);
});

// Route untuk testing API
Route::get('/test', function () {
    return response()->json([
        'message' => 'API Server is running!',
        'timestamp' => now(),
        'status' => 'success'
    ]);
});
