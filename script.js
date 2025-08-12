const container = document.getElementById('produk-container');

fetch('data.json')
//fetch('https://temanmu.com/api/produk')  // Ambil data dari backend 

  .then(response => response.json())
  .then(dataProduk => {
    dataProduk.forEach((produk, index) => {
      const card = document.createElement('div');
      card.className = 'card';

      card.innerHTML = `
        <img src="${produk.gambar}" alt="${produk.nama}">
        <div><strong>${produk.nama}</strong></div>
        <div>Rp.${produk.harga.toLocaleString()}</div>
        <div class="controls">
          <button onclick="ubahKuantitas(${index}, -1)">-</button>
          <span id="kuantitas-${index}">${produk.kuantitas}</span>
          <button onclick="ubahKuantitas(${index}, 1)">+</button>
        </div>
        <button class="ok-button" onclick="konfirmasi(${index})">OK</button>
      `;

      container.appendChild(card);
    });

    // Simpan ke global untuk digunakan fungsi lainnya
    window.dataProduk = dataProduk;
  });

function ubahKuantitas(index, delta) {
  const produk = window.dataProduk[index];
  produk.kuantitas = Math.max(0, produk.kuantitas + delta);
  document.getElementById(`kuantitas-${index}`).innerText = produk.kuantitas;
}

function konfirmasi(index) {
  const produk = window.dataProduk[index];
  alert(`Produk: ${produk.nama}\nJumlah: ${produk.kuantitas}`);
}
