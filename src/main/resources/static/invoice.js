 let stockData = [];

window.addEventListener('DOMContentLoaded', async () => {
  try {
    const [partyRes, stockRes] = await Promise.all([
      fetch('/parties'), fetch('/stock/summary')
    ]);
    const parties = (await partyRes.json()).data;
    stockData = (await stockRes.json()).data;

    const partyOptions = document.getElementById('partyOptions');
    const partyInput = document.getElementById('partyInput');

    function populatePartyOptions() {
      partyOptions.innerHTML = parties.map(p => `<option value="${p.name}">`).join('');
    }
    populatePartyOptions();

    partyInput.addEventListener('change', function () {
      const value = this.value.trim();
      if (value && !parties.some(p => p.name.toLowerCase() === value.toLowerCase())) {
        parties.push({ name: value });
        populatePartyOptions();
      }
    });

    const tbody = document.querySelector('#stockTable tbody');
    const addRowBtn = document.getElementById('addRowBtn');
    const stockOptions = document.getElementById('stockOptions');

    stockOptions.innerHTML = stockData.map(s =>
      `<option value="${s.itemName}" data-unit="${s.unit}" data-price="${s.price}" data-gst="${s.gst}">`
    ).join('');

    function createRow() {
      const row = document.createElement('tr');
       const rowCount = tbody.rows.length + 1;
       row.innerHTML = `
        <td>${rowCount}</td>
        <td><input type="text" list="stockOptions" class="stockInput" placeholder="Search stock..." required></td>
        <td><input type="number" min="1" value="1" class="qtyInput" required style="width:80px;"></td>
        <td><input type="text" class="unitInput" style="width:80px;"></td>
        <td><input type="number" min="0" step="1" class="priceInput" style="width:80px;"></td>
        <td><input type="number" min="0" step="1" class="gstInput" style="width:60px;"></td>
       <td><input type="number" class="totalInput" style="width:100px;" readonly></td>
       <td><button type="button" class="removeBtn" title="Remove row">✕</button></td>
        `;
      const qtyInput = row.querySelector('.qtyInput');
      const stockInput = row.querySelector('.stockInput');
      const unitInput = row.querySelector('.unitInput');
      const priceInput = row.querySelector('.priceInput');
      const gstInput = row.querySelector('.gstInput');
      const totalInput = row.querySelector('.totalInput');
      function updateTotal() {
          const price = parseFloat(priceInput.value) || 0;
          const qty = parseFloat(qtyInput.value) || 0;
          const gst = parseFloat(gstInput.value) || 0;
          const total = qty * price * (1 + gst / 100);
          totalInput.value = Math.round(total);
        }
        stockInput.addEventListener('change', function () {
        const stock = stockData.find(s => s.itemName.toLowerCase() === this.value.toLowerCase());
        if (stock) {
          unitInput.value = stock.unit;
          priceInput.value = stock.price;
          gstInput.value = stock.gst;
        } else {
          unitInput.value = '';
          priceInput.value = '';
          gstInput.value = '';
        }
        updateTotal();
        });

          priceInput.addEventListener('input', updateTotal);
          qtyInput.addEventListener('input', updateTotal);
          gstInput.addEventListener('input', updateTotal);

        row.querySelector('.removeBtn').addEventListener('click', () => row.remove());
        tbody.appendChild(row);
        updateTotal();
      }

      createRow();
      addRowBtn.addEventListener('click', createRow);
    } catch (err) {
      showNotification('Error loading data: ' + err.message, 'error');
    } finally{
      hideLoader()
    }
    });

async function calculateCost() {
  const tbody = document.querySelector('#stockTable tbody');
  const additionalDiscount = document.getElementById("additionalDiscount");

  const rows = Array.from(tbody.querySelectorAll('tr'));
  const stockBills = rows.map(row => {
  const stockName = row.querySelector('.stockInput').value.trim();
  const qty = Number(row.querySelector('.qtyInput').value || 0);
  const unit = row.querySelector('.unitInput').value.trim();
  const price = Number(row.querySelector('.priceInput').value || 0);
  const gst = Number(row.querySelector('.gstInput').value || 0);
  if (stockName && qty > 0) return { itemName: stockName, quantity: qty, unit, price, gst };
  return null;
  }).filter(Boolean);

  if (stockBills.length === 0) {
    showNotification('Please enter at least one stock with quantity > 0', 'error');
    return;
  }
  const btn = document.getElementById('calculateBtn');
    const spinner = btn.querySelector('.spinner');
    const text = btn.querySelector('.btn-text');

    // Show spinner
    spinner.style.display = 'inline-block';
    text.style.display = 'none';
    btn.disabled = true;

  try {
    const res = await fetch('/calculate/cost', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ stockBills, additionalDiscount:Number(additionalDiscount.value, 0) })
    });
    if (!res.ok) throw new Error('Failed to calculate cost');
    const data = await res.json();
    document.getElementById('totalCost').value = data.data.Cost;
    document.getElementById('gst').value = data.data.Tax;
  } catch (err) {
    showNotification('Error: ' + err.message, 'error');
  } finally{
    spinner.style.display = 'none';
    text.style.display = 'inline';
    btn.disabled = false;
  }
}

document.getElementById('invoiceForm').addEventListener('submit', async e => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const tbody = document.querySelector('#stockTable tbody');
  const rows = Array.from(tbody.querySelectorAll('tr'));

  const stockBills = rows.map(row => {
  const stockName = row.querySelector('.stockInput').value.trim();
  const qty = Number(row.querySelector('.qtyInput').value || 0);
  const unit = row.querySelector('.unitInput').value.trim();
  const price = Number(row.querySelector('.priceInput').value || 0);
  const gst = Number(row.querySelector('.gstInput').value || 0);
  if (stockName && qty > 0) return { itemName: stockName, quantity: qty, unit, price, gst };
  return null;
  }).filter(Boolean);

const invoice = {
  partyName: fd.get('partyName'),
  transactionType: fd.get('transactionType'),
  stockBills,
  additionalDiscount: Number(fd.get('additionalDiscount') || 0),
  paidAmount: Number(fd.get('paidAmount') || 0)
};
if(!invoice.partyName) {
  showNotification("Enter Party Name", 'error');
  return;
}
const btn = document.getElementById('generateBtn');
    const spinner = btn.querySelector('.spinner');
    const text = btn.querySelector('.btn-text');

    spinner.style.display = 'inline-block';
    text.style.display = 'none';
    btn.disabled = true;
  try {
    const res = await fetch('/generateInvoice', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(invoice)
    });
    spinner.style.display = 'inline-block';
    text.style.display = 'none';
    btn.disabled = true;
    if (!res.ok) throw new Error('Failed to generate invoice');
    const saved = await res.json();
    showNotification('Invoice generated with ID: ' + saved.data, 'success');
    window.location.href = "/viewSingleInvoice.html?id=" + saved.data;
  } catch (err) {
    showNotification('Error: ' + err.message, 'error');
  }
});

function goHome() {
window.location.href = '/index.html';
}

function showLoader() {
    document.getElementById('loaderOverlay').classList.remove('hide');
  }
  function hideLoader() {
    document.getElementById('loaderOverlay').classList.add('hide');
  }
  showLoader(); // Show by default at page load

// Show notification
function showNotification(message, type = 'success', duration = 3000) {
  const banner = document.getElementById('notification');
  const messageSpan = document.getElementById('notification-message');

  banner.className = `notification ${type}`; // success or error
  messageSpan.innerText = message;
  banner.style.display = 'flex';

  // Auto hide after duration
  setTimeout(() => {
    banner.style.display = 'none';
  }, duration);
}

// Close button
function closeNotification() {
  document.getElementById('notification').style.display = 'none';
}
