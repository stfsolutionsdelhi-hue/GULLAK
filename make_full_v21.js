const fs = require('fs');

// Extract the exact HTML, CSS, JS from the user attached snippet:
// The user prompt contains the exact string starting at `function getCompleteSoftwareHtml() {`
// Let's create the final Code.gs with:
// 1. Server logic with Users sheet (from Part1_Server.gs)
// 2. getCompleteSoftwareHtml() with the Windows Login overlay (no quick login button) and all V21 tabs intact.

const p1 = fs.readFileSync("Part1_Server.gs", "utf8");

const p2_html = `
function getCompleteSoftwareHtml() {
  return \`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>GULLAK CO-OPERATIVE SOCIETY</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background: #060913; color: #F8FAFC; padding: 16px 20px; min-height: 100vh; }
    
    /* WINDOWS TERMINAL LOGIN OVERLAY (WITHOUT QUICK LOGIN BUTTON) */
    #windowsLoginOverlay {
      position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
      background: rgba(4, 7, 18, 0.96); backdrop-filter: blur(12px);
      z-index: 2147483645; display: flex; align-items: center; justify-content: center;
    }
    .win-login-card {
      position: relative; background: #0F172A; border: 1.5px solid #F59E0B;
      border-radius: 16px; padding: 34px 28px; width: 92%; max-width: 420px;
      box-shadow: 0 20px 50px rgba(0,0,0,0.85); text-align: center;
    }
    .login-fullscreen-toggle {
      position: absolute; top: 14px; right: 14px; background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(245, 158, 11, 0.4); color: #FBBF24; font-size: 0.75rem;
      font-weight: 700; padding: 5px 10px; border-radius: 6px; cursor: pointer;
    }
    .win-avatar-circle {
      width: 64px; height: 64px; background: rgba(245, 158, 11, 0.15);
      border: 2px solid #F59E0B; border-radius: 50%; display: flex;
      align-items: center; justify-content: center; font-size: 28px; margin: 0 auto 14px;
    }
    .win-title { color: #FBBF24; font-weight: 800; font-size: 1.25rem; letter-spacing: 0.5px; }
    .win-sub { color: #94A3B8; font-size: 0.76rem; font-weight: 700; margin-bottom: 22px; letter-spacing: 1px; }
    .win-field-group { text-align: left; margin-bottom: 14px; }
    .win-label { display: block; font-size: 0.78rem; font-weight: 700; color: #CBD5E1; margin-bottom: 5px; }
    .win-input {
      width: 100%; background: #1E293B; border: 1px solid #334155;
      border-radius: 8px; padding: 10px 14px; color: #FFFFFF; font-size: 0.95rem; outline: none;
    }
    .win-input:focus { border-color: #F59E0B; }
    .password-wrapper { position: relative; width: 100%; }
    .password-wrapper .win-input { padding-right: 44px; }
    .password-toggle-btn {
      position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
      background: transparent; border: none; font-size: 1.15rem; cursor: pointer;
      color: #94A3B8; padding: 4px; display: flex; align-items: center; justify-content: center;
    }
    .password-toggle-btn:hover { color: #FBBF24; }
    .win-btn-login {
      width: 100%; background: #F59E0B; color: #0F172A; border: none;
      border-radius: 8px; font-weight: 800; font-size: 1rem; padding: 11px;
      cursor: pointer; margin-top: 10px;
    }
    .win-btn-login:hover { opacity: 0.92; }
    .win-forgot-link { color: #64748B; font-size: 0.78rem; font-weight: 600; margin-top: 16px; cursor: pointer; }
    .win-forgot-link:hover { color: #FBBF24; text-decoration: underline; }

    /* V21 STYLES */
    .header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 18px; }
    .logo-box { display: flex; align-items: center; gap: 10px; }
    .logo-icon { width: 42px; height: 42px; background: #1E293B; border: 2px solid #F59E0B; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; }
    .title-main { font-size: 1.45rem; font-weight: 800; color: #FBBF24; letter-spacing: 0.5px; }
    .title-sub { color: #0284C7; font-size: 0.8rem; font-weight: 600; }
    .btn-group { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
    .btn { border: none; border-radius: 8px; padding: 8px 12px; font-size: 0.82rem; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 5px; user-select: none; }
    .btn-green { background: #059669; color: #FFF; } .btn-red { background: #B91C1C; color: #FFF; }
    .btn-blue { background: #0284C7; color: #FFF; } .btn-orange { background: #D97706; color: #FFF; }
    .btn-purple { background: #7C3AED; color: #FFF; } .btn-dark { background: #1E293B; color: #CBD5E1; border: 1px solid #334155; }
    .year-badge { background: #1E293B; border: 1px solid #F59E0B; color: #FBBF24; padding: 6px 10px; border-radius: 8px; font-weight: 800; font-size: 0.82rem; cursor: pointer; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(135px, 1fr)); gap: 10px; margin-bottom: 18px; }
    .kpi-card { background: #0B1120; border: 1px solid #1E293B; border-radius: 10px; padding: 10px 12px; text-align: center; cursor: pointer; transition: 0.15s; }
    .kpi-card:hover { border-color: #38BDF8; transform: translateY(-2px); }
    .kpi-title { font-size: 0.7rem; font-weight: 600; color: #94A3B8; margin-bottom: 4px; }
    .kpi-val { font-size: 1.35rem; font-weight: 800; }
    .val-blue { color: #38BDF8; } .val-green { color: #10B981; } .val-red { color: #EF4444; } .val-gold { color: #FBBF24; } .val-purple { color: #C084FC; } .val-white { color: #F8FAFC; }
    .tabs-header { display: flex; gap: 4px; margin-bottom: 0; flex-wrap: wrap; }
    .tab-item { background: #0F172A; color: #94A3B8; border: 1px solid #1E293B; border-bottom: none; padding: 10px 16px; font-weight: 700; font-size: 0.84rem; border-radius: 8px 8px 0 0; cursor: pointer; }
    .tab-item.active { background: #D97706; color: #FFF; border-color: #D97706; }
    .content-panel { background: #0B1120; border: 1px solid #1E293B; border-radius: 0 10px 10px 10px; padding: 18px; }
    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
    .panel-title { font-size: 1.15rem; font-weight: 700; color: #38BDF8; }
    .search-input, .filter-ctrl { background: #060913; border: 1px solid #334155; color: #fff; padding: 6px 10px; border-radius: 6px; font-size: 0.82rem; outline: none; }
    
    input[type="date"]::-webkit-calendar-picker-indicator { filter: invert(1); cursor: pointer; opacity: 0.9; }

    .table-wrap { width: 100%; overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; }
    th { background: #0F172A; color: #38BDF8; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; padding: 10px; border-bottom: 1px solid #1E293B; white-space: nowrap; }
    td { padding: 10px; border-bottom: 1px solid #1E293B; font-size: 0.84rem; vertical-align: middle; }
    .tfoot-total-row td { background: #0F172A; font-weight: 800; border-top: 2px solid #F59E0B; padding: 12px 10px; }
    .member-link { color: #FBBF24; font-weight: 700; cursor: pointer; text-decoration: underline; text-underline-offset: 2px; }
    .badge-active { background: #064E3B; color: #34D399; padding: 2px 6px; border-radius: 4px; font-size: 0.7rem; font-weight: 700; }
    .badge-inactive { background: #7F1D1D; color: #F87171; padding: 2px 6px; border-radius: 4px; font-size: 0.7rem; font-weight: 700; }
    .badge-pending { background: #78350F; color: #FBBF24; padding: 2px 6px; border-radius: 4px; font-size: 0.7rem; font-weight: 700; }
    .btn-action-rcv { background: #0284C7; color: #fff; padding: 4px 8px; border-radius: 5px; border: none; font-size: 0.75rem; font-weight: 700; cursor: pointer; margin-right: 4px; }
    .btn-action-edit { background: #334155; color: #FBBF24; padding: 4px 8px; border-radius: 5px; border: none; font-size: 0.75rem; cursor: pointer; margin-right: 4px; }
    
    .modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.85); backdrop-filter: blur(3px); display: none; align-items: center; justify-content: center; z-index: 100000; }
    .modal-dialog-box { background: #0F172A; border: 1px solid #334155; border-radius: 12px; width: 95%; max-width: 540px; padding: 18px; max-height: 90vh; overflow-y: auto; position: relative; }
    .modal-dialog-lg { max-width: 980px; }
    .modal-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid #1E293B; padding-bottom: 8px; }
    .close-x { background: transparent; border: none; color: #94A3B8; font-size: 24px; cursor: pointer; line-height: 1; padding: 0 4px; }
    .close-x:hover { color: #EF4444; }
    .field-box { margin-bottom: 10px; }
    .field-label { display: block; font-size: 0.74rem; font-weight: 700; color: #94A3B8; margin-bottom: 3px; text-transform: uppercase; }
    .field-ctrl { width: 100%; background: #060913; border: 1px solid #334155; border-radius: 6px; padding: 7px 10px; color: #fff; font-size: 0.85rem; outline: none; }
    .two-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
    .four-cols { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 6px; }
    .ledger-header { background: #1E293B; border-radius: 8px; padding: 10px 12px; margin-bottom: 14px; display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 8px; }
    .ledger-stat-lbl { font-size: 0.7rem; color: #94A3B8; text-transform: uppercase; }
    .ledger-stat-val { font-size: 1.1rem; font-weight: 800; }
    .narration-badge { font-size: 0.72rem; color: #94A3B8; display: block; margin-top: 2px; font-style: italic; }
    .bonus-clickable { color: #C084FC; font-weight: 800; font-size: 1.05rem; cursor: pointer; text-decoration: underline; text-underline-offset: 3px; }
    .bonus-clickable:hover { color: #E9D5FF; }

    body.simulated-fullscreen {
      position: fixed !important; top: 0 !important; left: 0 !important;
      width: 100vw !important; height: 100vh !important; max-width: 100vw !important;
      margin: 0 !important; padding: 12px 16px !important; z-index: 2147483640 !important;
      overflow: auto !important; background: #060913 !important;
    }
  </style>
</head>
<body>

<!-- WINDOWS TERMINAL LOGIN (NO QUICK LOGIN BUTTON) -->
<div id="windowsLoginOverlay">
  <div class="win-login-card">
    <button type="button" class="login-fullscreen-toggle" id="btnLoginFullscreen" onclick="safeToggleFullscreen(event)">⛶ Full Screen</button>
    <div class="win-avatar-circle">🏦</div>
    <div class="win-title">GULLAK SUVIDHA SOCIETY</div>
    <div class="win-sub">AUTHORIZED CLOUD TERMINAL (V21 PRO)</div>
    
    <form id="formWinLogin" onsubmit="executeDirectLogin(event); return false;" style="width:100%; margin:0; padding:0;">
      <div class="win-field-group">
        <label class="win-label">User ID / Username</label>
        <input type="text" id="inpWinUsername" class="win-input" value="SANISH" placeholder="Enter Username" autocomplete="username" onkeydown="handleLoginKeyPress(event)">
      </div>
      
      <div class="win-field-group">
        <label class="win-label">Security Password</label>
        <div class="password-wrapper">
          <input type="password" id="inpWinPassword" class="win-input" value="Password" placeholder="Enter Password" autocomplete="current-password" autofocus onkeydown="handleLoginKeyPress(event)">
          <button type="button" class="password-toggle-btn" id="btnToggleEye" onclick="togglePasswordEye(event)" onmousedown="togglePasswordEye(event)" title="Show/Hide Password">👁️</button>
        </div>
      </div>
      
      <button type="submit" class="win-btn-login" id="btnWinLogin" onclick="executeDirectLogin(event)">Sign In / Unlock Portal ➔</button>
    </form>

    <div id="winLoginError" style="color:#EF4444; font-size:0.85rem; font-weight:700; margin-top:10px; display:none; background:rgba(239,68,68,0.15); border:1px solid #EF4444; border-radius:6px; padding:8px; line-height:1.4;"></div>
    
    <div class="win-forgot-link" onclick="handleForgotCredentials()">Forgot Username / Password?</div>
  </div>
</div>

<div class="header">
  <div class="logo-box">
    <div class="logo-icon">🏦</div>
    <div>
      <div class="title-main">GULLAK CO-OPERATIVE SOCIETY</div>
      <div class="title-sub">MASTER CLOUD ACCOUNTING SYSTEM (V21 PRO)</div>
    </div>
  </div>
  <div class="btn-group">
    <select id="selFinancialYear" class="year-badge"></select>
    <button class="btn btn-green" id="btnTopReceive">📥 Receive Amount</button>
    <button class="btn btn-red" id="btnTopLoan">💸 Issue Loan</button>
    <button class="btn btn-blue" id="btnTopAddMember">👤 + Add Member</button>
    <button class="btn btn-orange" id="btnTopBulk">▦ Bulk Entry</button>
    <button class="btn btn-purple" id="btnTopExit">🚪 Member Exit</button>
    <button class="btn btn-dark" id="btnTopSettings">⚙️ Settings</button>
    <button class="btn btn-dark" id="btnTopReload" title="Sync fresh verified data from Google Sheet">🔄 Fix/Reload</button>
    <button class="btn btn-dark" onclick="safeToggleFullscreen(event)">⛶ Fullscreen</button>
    <button class="btn btn-red" onclick="logoutSession()">Lock 🔒</button>
  </div>
</div>

<div class="kpi-grid">
  <div class="kpi-card" id="kpiCardMembers"><div class="kpi-title">TOTAL MEMBERS</div><div class="kpi-val val-blue" id="dispTotalMem">0 / 0</div></div>
  <div class="kpi-card" id="kpiCardRd"><div class="kpi-title">TOTAL RECEIPT / COLLECTION</div><div class="kpi-val val-green" id="dispTotalRd">₹0</div></div>
  <div class="kpi-card" id="kpiCardLoans"><div class="kpi-title">TOTAL LOAN DUES</div><div class="kpi-val val-red" id="dispTotalLoan">₹0</div></div>
  <div class="kpi-card" id="kpiCardBonus"><div class="kpi-title">EST. ANNUAL BONUS</div><div class="kpi-val val-purple" id="dispTotalBonus">₹0</div></div>
  <div class="kpi-card" id="kpiCardFund"><div class="kpi-title">CASH / BANK REGISTER 🏛️</div><div class="kpi-val val-green" id="dispTotalFund">+₹0</div></div>
  <div class="kpi-card" id="kpiCardNpa"><div class="kpi-title">NPA / LOSS ⚠️</div><div class="kpi-val val-white" id="dispTotalNpa">₹0</div></div>
</div>

<div class="tabs-header">
  <div class="tab-item active" id="tabHead1">👥 1. Master Ledger</div>
  <div class="tab-item" id="tabHead2">📥 2. Collections & Receipts</div>
  <div class="tab-item" id="tabHead3">💸 3. Loan Register</div>
  <div class="tab-item" id="tabHead4">🎁 4. Annual Bonus & Set-off</div>
</div>

<!-- TAB 1: MEMBERS -->
<div class="content-panel" id="tabPanel1">
  <div class="panel-header">
    <div class="panel-title">Society Members Master Ledger</div>
    <div style="display:flex; gap:6px; flex-wrap:wrap;">
      <select id="selFilterStatus" class="filter-ctrl">
        <option value="ALL">All Status</option>
        <option value="ACTIVE" selected>Active Only</option>
        <option value="INACTIVE">Inactive Only</option>
      </select>
      <select id="selSortMembers" class="filter-ctrl">
        <option value="name_az">🔤 Name (A-Z)</option>
        <option value="rd_high">⬆ RD: High</option>
        <option value="rd_low">⬇ RD: Low</option>
        <option value="loan_high">⬆ Loan: High</option>
      </select>
      <input type="text" id="memberFilterInput" class="search-input" placeholder="🔍 Search name/phone...">
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>MEMBER NAME / PHONE</th>
          <th>TOTAL RD DEPOSITED</th>
          <th>TOTAL LOAN (G+E)</th>
          <th>INTEREST DUES</th>
          <th>PENALTY DUES</th>
          <th>LOAN LIMIT</th>
          <th>STATUS</th>
          <th>ACTIONS</th>
        </tr>
      </thead>
      <tbody id="tbodyMembers"></tbody>
      <tfoot id="tfootMembersTotal"></tfoot>
    </table>
  </div>
</div>

<!-- TAB 2: PAYMENTS -->
<div class="content-panel" id="tabPanel2" style="display:none;">
  <div class="panel-header">
    <div class="panel-title">Collections & Payment Receipts</div>
    <div style="display:flex; gap:6px; flex-wrap:wrap; align-items:center;">
      <label class="field-label" style="margin:0;">Date:</label>
      <input type="date" id="inpPayFilterFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
      <input type="date" id="inpPayFilterTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
      <select id="selFilterPayMode" class="filter-ctrl">
        <option value="ALL">All Modes</option>
        <option value="CASH">Cash Only</option>
        <option value="ONLINE">Online (UPI/Bank)</option>
      </select>
      <select id="selSortPayDate" class="filter-ctrl">
        <option value="new">📅 New First</option>
        <option value="old">📅 Old First</option>
        <option value="amt_high">⬆ Amount: High</option>
      </select>
      <input type="text" id="searchPayInput" class="search-input" placeholder="🔍 Search receipts / member...">
      <button class="btn btn-green" id="btnPanelNewReceipt">📥 + New Receipt</button>
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>RECEIPT NO</th>
          <th>DATE</th>
          <th>MEMBER & NARRATION</th>
          <th>RD AMOUNT</th>
          <th>INTEREST</th>
          <th>PENALTY</th>
          <th>LOAN REPAYMENT</th>
          <th>WAIVER</th>
          <th>TOTAL (₹)</th>
          <th>MODE</th>
          <th>ACTION</th>
        </tr>
      </thead>
      <tbody id="tbodyPayments"></tbody>
      <tfoot id="tfootPaymentsTotal"></tfoot>
    </table>
  </div>
</div>

<!-- TAB 3: LOANS -->
<div class="content-panel" id="tabPanel3" style="display:none;">
  <div class="panel-header">
    <div class="panel-title">Loan Register & Disbursals</div>
    <div style="display:flex; gap:6px; flex-wrap:wrap; align-items:center;">
      <label class="field-label" style="margin:0;">Date:</label>
      <input type="date" id="inpLoanFilterFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
      <input type="date" id="inpLoanFilterTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
      <select id="selFilterLoanType" class="filter-ctrl">
        <option value="ALL">All Loan Types</option>
        <option value="Gullak Loan">Gullak Loan</option>
        <option value="Emergency Loan">Emergency Loan</option>
      </select>
      <select id="selFilterLoanStatus" class="filter-ctrl">
        <option value="ALL">All Status</option>
        <option value="ACTIVE" selected>Active Loans</option>
        <option value="CLOSED">Closed Loans</option>
      </select>
      <input type="text" id="searchLoanInput" class="search-input" placeholder="🔍 Search borrower / loan ID...">
      <button class="btn btn-red" id="btnPanelNewLoan">💸 + Issue Loan</button>
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead><tr><th>LOAN ID</th><th>DATE</th><th>MEMBER</th><th>TYPE</th><th>PRINCIPAL (₹)</th><th>RATE</th><th>OUTSTANDING DUE (₹)</th><th>STATUS</th><th>ACTION</th></tr></thead>
      <tbody id="tbodyLoans"></tbody>
      <tfoot id="tfootLoansTotal"></tfoot>
    </table>
  </div>
</div>

<!-- TAB 4: BONUS -->
<div class="content-panel" id="tabPanel4" style="display:none;">
  <div class="panel-header">
    <div>
      <div class="panel-title">🎁 Annual Bonus Calculation & Set-off (1% Per Month on Cumulative RD)</div>
      <small style="color:#94A3B8;">Click on any calculated bonus (Purple) to view month-by-month breakup for the active date range.</small>
    </div>
    <div style="display:flex; gap:6px; flex-wrap:wrap; align-items:center;">
      <label class="field-label" style="margin:0;">Date:</label>
      <input type="date" id="inpBonusFilterFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
      <input type="date" id="inpBonusFilterTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
      <select id="selFilterBonusStatus" class="filter-ctrl">
        <option value="ALL">All Bonus Status</option>
        <option value="PENDING">Pending Only</option>
        <option value="PAID">Set-off / Paid Only</option>
      </select>
      <select id="selSortBonus" class="filter-ctrl">
        <option value="bonus_high">⬆ Bonus: High</option>
        <option value="bonus_low">⬇ Bonus: Low</option>
        <option value="name_az">🔤 Name (A-Z)</option>
      </select>
      <input type="text" id="searchBonusInput" class="search-input" placeholder="🔍 Search member...">
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead><tr><th>MEMBER</th><th>TOTAL RD SAVED</th><th>CALCULATED BONUS (1% P.M.)</th><th>ACTIVE LOAN DUES</th><th>PENALTY DUES</th><th>STATUS</th><th>ACTIONS</th></tr></thead>
      <tbody id="tbodyBonusList"></tbody>
      <tfoot id="tfootBonusTotal"></tfoot>
    </table>
  </div>
</div>

<!-- ALL MODALS -->
<div class="modal-backdrop" id="modalNotice" style="z-index: 100010;">
  <div class="modal-dialog-box" style="max-width:380px; text-align:center;">
    <div style="font-size:32px; margin-bottom:6px;">ℹ️</div>
    <div id="noticeHeader" style="font-size:1.15rem; font-weight:800; color:#FBBF24; margin-bottom:8px;">Notice</div>
    <div id="noticeBody" style="font-size:0.88rem; color:#CBD5E1; margin-bottom:18px; line-height:1.4;"></div>
    <button class="btn btn-orange" id="btnNoticeOk" style="width:100%; justify-content:center; padding:10px;">OK / Theek Hai</button>
  </div>
</div>

<div class="modal-backdrop" id="modalConfirm" style="z-index: 100010;">
  <div class="modal-dialog-box" style="max-width:400px; text-align:center;">
    <div style="font-size:32px; margin-bottom:6px;">❓</div>
    <div id="confirmHeader" style="font-size:1.15rem; font-weight:800; color:#F59E0B; margin-bottom:8px;">Confirm Action</div>
    <div id="confirmBody" style="font-size:0.9rem; color:#CBD5E1; margin-bottom:16px;"></div>
    <div style="display:flex; gap:8px;">
      <button class="btn btn-dark action-close-modal" style="flex:1; justify-content:center;">Cancel</button>
      <button class="btn btn-orange" id="btnConfirmProceed" style="flex:1; justify-content:center;">Yes, Proceed</button>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalBonusOverview">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row"><div style="color:#C084FC; font-weight:800;">🎁 Annual Bonus & Interest Overview</div><button class="close-x action-close-modal">&times;</button></div>
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px; margin-bottom:12px;">
      <div style="display:flex; gap:6px;">
        <button class="tab-item active" id="btnSubTabInterest" style="border-radius:6px;">📈 Total Interest Received</button>
        <button class="tab-item" id="btnSubTabBonus" style="border-radius:6px;">🎁 Total Bonus Paid / Settled</button>
      </div>
      <div style="display:flex; gap:6px; align-items:center;">
        <label class="field-label" style="margin:0;">Date:</label>
        <input type="date" id="inpSubFilterFrom" class="filter-ctrl" style="width:125px;" value="2026-01-01">
        <input type="date" id="inpSubFilterTo" class="filter-ctrl" style="width:125px;" value="2026-12-31">
        <button class="btn btn-dark" id="btnApplySubFilter" style="padding:4px 8px; font-size:0.75rem;">Filter</button>
      </div>
    </div>
    <div id="boxSubInterest" style="max-height:260px; overflow-y:auto;">
      <table><thead><tr><th>DATE</th><th>MEMBER</th><th>RECEIPT</th><th>INTEREST RECEIVED (₹)</th><th>MODE</th></tr></thead><tbody id="tbodySubInterest"></tbody></table>
    </div>
    <div id="boxSubBonus" style="max-height:260px; overflow-y:auto; display:none;">
      <table><thead><tr><th>DATE</th><th>MEMBER</th><th>SETTLEMENT ID</th><th>BONUS ADJUSTED / PAID (₹)</th><th>MODE</th></tr></thead><tbody id="tbodySubBonus"></tbody></table>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalReceive">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div id="lblReceiveModalHead" style="color:#10B981; font-weight:800;">📥 Receive Amount</div><button class="close-x action-close-modal">&times;</button></div>
    <input type="hidden" id="editReceiptNo">
    <div class="field-box">
      <label class="field-label">Search / Select Member</label>
      <input type="text" id="inpSearchReceiveMember" class="field-ctrl" placeholder="🔍 Type member name to search..." style="margin-bottom:5px;">
      <select id="selPayMember" class="field-ctrl"></select>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Posting Date</label><input type="date" id="inpPayDate" class="field-ctrl"></div>
      <div><label class="field-label">Payment Mode</label><select id="selPayMode" class="field-ctrl"><option value="CASH">CASH (By Default)</option><option value="ONLINE">ONLINE (UPI/Bank)</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Monthly RD (₹)</label><input type="number" step="1" id="inpPayRd" class="field-ctrl" value="400"></div>
      <div><label class="field-label">Interest (₹)</label><input type="number" step="1" id="inpPayInterest" class="field-ctrl" value="0"></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Penalty (₹)</label><input type="number" step="1" id="inpPayPenalty" class="field-ctrl" value="0"></div>
      <div><label class="field-label">Waiver (₹)</label><input type="number" step="1" id="inpPayWaiver" class="field-ctrl" value="0"></div>
    </div>
    <div class="field-box"><label class="field-label">Loan Repayment (₹)</label><input type="number" step="1" id="inpPayPrincipal" class="field-ctrl" value="0"></div>
    <div class="field-box"><label class="field-label">Narration / Remarks (Optional)</label><input type="text" id="inpPayNarration" class="field-ctrl" placeholder="e.g. Monthly RD + Regular Fine"></div>
    <button class="btn btn-green" id="btnSubmitReceive" style="width:100%; justify-content:center; padding:11px;">Save & Generate Receipt</button>
  </div>
</div>

<div class="modal-backdrop" id="modalLoan">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div id="lblLoanModalHead" style="color:#EF4444; font-weight:800;">💸 Issue Society Loan</div><button class="close-x action-close-modal">&times;</button></div>
    <input type="hidden" id="editLoanId">
    <div class="field-box">
      <label class="field-label">Search / Select Borrower</label>
      <input type="text" id="inpSearchLoanMember" class="field-ctrl" placeholder="🔍 Type borrower name to search..." style="margin-bottom:5px;">
      <select id="selLoanMember" class="field-ctrl"></select>
      <small id="lblMemberLoanLimit" style="color:#FBBF24; display:block; margin-top:4px; font-weight:700;"></small>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Issue Date</label><input type="date" id="inpLoanDate" class="field-ctrl"></div>
      <div><label class="field-label">Loan Type</label><select id="selLoanType" class="field-ctrl"><option value="Gullak Loan">Gullak Loan</option><option value="Emergency Loan">Emergency Loan</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Principal Loan Amount (₹)</label><input type="number" step="1" id="inpLoanPrinc" class="field-ctrl" placeholder="e.g. 15000"></div>
      <div><label class="field-label">Interest Rate (% p.m.)</label><input type="number" id="inpLoanRate" class="field-ctrl" value="1.0" step="0.1"></div>
    </div>
    <button class="btn btn-red" id="btnSubmitLoan" style="width:100%; justify-content:center; padding:11px;">Approve & Disburse Loan</button>
  </div>
</div>

<div class="modal-backdrop" id="modalMember">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div id="lblMemberModalHead" style="color:#38BDF8; font-weight:800;">👤 Member Profile Form</div><button class="close-x action-close-modal">&times;</button></div>
    <input type="hidden" id="editMemId">
    <div class="field-box"><label class="field-label">Full Name *</label><input type="text" id="inpNewMemName" class="field-ctrl" placeholder="Enter Full Name"></div>
    <div class="two-cols field-box">
      <div><label class="field-label">Mobile (Strict 10 Digits) *</label><input type="text" id="inpNewMemMobile" class="field-ctrl" maxlength="10" placeholder="e.g. 9810011111"></div>
      <div><label class="field-label">Status</label><select id="inpNewMemStatus" class="field-ctrl"><option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Joining Date</label><input type="date" id="inpNewMemJoinDate" class="field-ctrl" value="2026-01-01"></div>
      <div><label class="field-label">Monthly RD (₹) *</label><input type="number" step="1" id="inpNewMemRd" class="field-ctrl" value="400"></div>
    </div>
    <div class="field-box">
      <label class="field-label">Address *</label>
      <input type="text" id="inpNewMemAddress" class="field-ctrl" placeholder="Full Postal Address" style="margin-bottom:6px;">
      <label class="field-label">Nominee / Reference *</label>
      <input type="text" id="inpNewMemNominee" class="field-ctrl" placeholder="Nominee Name & Relation">
    </div>
    <div style="background:#1E293B; border-radius:6px; padding:8px; margin-bottom:10px;">
      <div style="font-size:0.72rem; font-weight:700; color:#FBBF24; margin-bottom:4px;">OPENING BALANCES (As on 31 Dec 2025)</div>
      <div class="four-cols">
        <div><label class="field-label" style="font-size:0.65rem;">RD (₹)</label><input type="number" step="1" id="inpNewMemBal" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Loan (₹)</label><input type="number" step="1" id="inpNewMemOpLoan" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Int (₹)</label><input type="number" step="1" id="inpNewMemOpInt" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Pen (₹)</label><input type="number" step="1" id="inpNewMemOpPen" class="field-ctrl" value="0"></div>
      </div>
    </div>
    <div class="field-box"><label class="field-label">Custom Loan Limit Override (₹)</label><input type="number" step="1" id="inpNewMemCustomLimit" class="field-ctrl" value="0"></div>
    <button class="btn btn-blue" id="btnSubmitMember" style="width:100%; justify-content:center; padding:11px;">Save Member Profile</button>
  </div>
</div>

<div class="modal-backdrop" id="modalBulk">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row"><div style="color:#F59E0B; font-weight:800;">▦ Bulk Entry Register</div><button class="close-x action-close-modal">&times;</button></div>
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; flex-wrap:wrap; gap:8px;">
      <div style="display:flex; align-items:center; gap:8px;">
        <label class="field-label" style="margin:0;">Posting Date:</label>
        <input type="date" id="inpBulkDate" class="field-ctrl" style="width:150px;">
        <span id="dispBulkDateFormatted" style="color:#38BDF8; font-weight:700; font-size:0.85rem;"></span>
      </div>
      <div style="display:flex; gap:6px;">
        <button class="btn btn-dark" id="btnBulkSetAllCash" style="font-size:0.75rem; padding:4px 8px;">💵 All Cash</button>
        <button class="btn btn-dark" id="btnBulkSetAllOnline" style="font-size:0.75rem; padding:4px 8px;">🌐 All Online</button>
        <button class="btn btn-blue" id="btnAddBulkRow">➕ Add Row</button>
      </div>
    </div>
    <div style="max-height:360px; overflow-y:auto; margin-bottom:14px;">
      <table>
        <thead>
          <tr>
            <th><input type="checkbox" id="chkSelectAllBulk" checked></th>
            <th>MEMBER & LIVE DUES</th>
            <th style="text-align:center;">RD</th>
            <th style="text-align:center;">INTEREST</th>
            <th style="text-align:center;">LOAN REPAYMENT</th>
            <th style="text-align:center;">PENALTY</th>
            <th style="text-align:center;">WAIVER</th>
            <th style="text-align:center; color:#10B981;">TOTAL (₹)</th>
            <th style="text-align:center;">MODE</th>
          </tr>
        </thead>
        <tbody id="tbodyBulkList"></tbody>
      </table>
    </div>
    <div style="display:flex; justify-content:space-between; align-items:center; background:#1E293B; border-radius:8px; padding:10px 14px; margin-bottom:10px;">
      <div style="font-size:0.85rem; font-weight:700; color:#94A3B8;">Selected: <span id="lblBulkSelectedCount" style="color:#FBBF24;">0</span> Members</div>
      <div style="font-size:1.1rem; font-weight:800; color:#10B981;">Grand Total: <span id="lblBulkGrandTotal">₹0</span></div>
    </div>
    <button class="btn btn-orange" id="btnSubmitBulk" style="width:100%; justify-content:center; padding:11px;">Post All Selected Collections</button>
  </div>
</div>

<div class="modal-backdrop" id="modalLedger">
  <div class="modal-dialog-box modal-dialog-lg" id="printableLedgerArea">
    <div class="modal-header-row">
      <div id="lblLedgerName" style="color:#FBBF24; font-weight:800;">Member Complete Ledger</div>
      <div style="display:flex; gap:6px; align-items:center;">
        <input type="date" id="inpLedgerFilterFrom" class="filter-ctrl" style="width:125px;" value="2026-01-01">
        <input type="date" id="inpLedgerFilterTo" class="filter-ctrl" style="width:125px;" value="2026-12-31">
        <button class="btn btn-green" id="btnPrintLedgerPdf">🖨️ Print (PDF)</button>
        <button class="close-x action-close-modal">&times;</button>
      </div>
    </div>
    <div class="ledger-header" id="ledgerHeaderStats"></div>
    <div style="font-size:0.9rem; font-weight:700; color:#38BDF8; margin-bottom:6px;">Transaction History (Debit - Disbursal / Credit - Payment)</div>
    <div style="max-height:260px; overflow-y:auto;">
      <table>
        <thead><tr><th>DATE</th><th>REF ID</th><th>TRANSACTION PARTICULARS</th><th>DEBIT (-)</th><th>CREDIT (+)</th><th>BALANCE</th><th>MODE</th></tr></thead>
        <tbody id="tbodyLedgerTxns"></tbody>
      </table>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalBonusStatement">
  <div class="modal-dialog-box modal-dialog-lg" id="printableBonusArea">
    <div class="modal-header-row">
      <div id="lblBonusStmtTitle" style="color:#C084FC; font-weight:800;">Annual Bonus Statement (1% P.M.)</div>
      <div style="display:flex; gap:6px;">
        <button class="btn btn-green" id="btnPrintBonusPdf">🖨️ Print Statement (PDF)</button>
        <button class="close-x action-close-modal">&times;</button>
      </div>
    </div>
    <div class="ledger-header" id="bonusStmtHeaderStats"></div>
    <div style="font-size:0.9rem; font-weight:700; color:#FBBF24; margin-bottom:6px;">Month-by-Month RD Base & 1% Compounding Schedule</div>
    <div style="max-height:260px; overflow-y:auto;">
      <table>
        <thead><tr><th>MONTH</th><th>MONTH START BASE (₹)</th><th>RD DEPOSITED (₹)</th><th>MONTH END BASE (₹)</th><th>BONUS EARNED (1%)</th><th>PROGRESSIVE TOTAL</th></tr></thead>
        <tbody id="tbodyBonusSchedule"></tbody>
      </table>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalBonusSetoff">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div style="color:#C084FC; font-weight:800;">🎁 Manual Bonus Set-off</div><button class="close-x action-close-modal">&times;</button></div>
    <input type="hidden" id="bonusMemId">
    <div style="background:#1E293B; border-radius:6px; padding:10px; margin-bottom:12px;">
      <div style="font-weight:700; color:#FBBF24;" id="lblBonusTargetMember">Member Name</div>
      <div style="font-size:0.85rem; color:#94A3B8;">Calculated Bonus: <strong style="color:#34D399;" id="lblBonusAmount">₹0</strong></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Set-off Date</label><input type="date" id="inpBonusDate" class="field-ctrl"></div>
      <div><label class="field-label">Payout Mode</label><select id="selBonusMode" class="field-ctrl"><option value="ONLINE">ONLINE</option><option value="CASH">CASH</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">1. Adj. Loan Principal (₹)</label><input type="number" step="1" id="inpBonusAdjLoan" class="field-ctrl" value="0" oninput="calcBonusNet()"></div>
      <div><label class="field-label">2. Adj. Loan Interest (₹)</label><input type="number" step="1" id="inpBonusAdjInt" class="field-ctrl" value="0" oninput="calcBonusNet()"></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">3. Adj. RD Dues (₹)</label><input type="number" step="1" id="inpBonusAdjRd" class="field-ctrl" value="0" oninput="calcBonusNet()"></div>
      <div><label class="field-label">4. Adj. Penalty (₹)</label><input type="number" step="1" id="inpBonusAdjPen" class="field-ctrl" value="0" oninput="calcBonusNet()"></div>
    </div>
    <div class="field-box" style="background:#060913; border:1px solid #334155; border-radius:6px; padding:8px;">
      <label class="field-label">Net Cash/Bank Payout (₹)</label>
      <input type="number" id="inpBonusNetPaid" class="field-ctrl" value="0" readonly style="font-weight:800; color:#34D399;">
    </div>
    <button class="btn btn-purple" id="btnSubmitBonusSetoff" style="width:100%; justify-content:center; padding:11px;">Confirm & Post Bonus Set-off</button>
  </div>
</div>

<div class="modal-backdrop" id="modalExit">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div style="color:#EF4444; font-weight:800;">🚪 Member Exit Settlement</div><button class="close-x action-close-modal">&times;</button></div>
    <div class="field-box"><label class="field-label">Select Member</label><select id="selExitMember" class="field-ctrl" onchange="loadExitDetails()"></select></div>
    <div style="background:#1E293B; border-radius:6px; padding:10px; margin-bottom:12px;">
      <div class="two-cols" style="font-size:0.85rem;"><div>Total RD: <strong style="color:#10B981;" id="lblExitRd">₹0</strong></div><div>Loan Due: <strong style="color:#EF4444;" id="lblExitLoan">₹0</strong></div></div>
      <div class="two-cols" style="font-size:0.85rem; margin-top:4px;"><div>Bonus: <strong style="color:#C084FC;" id="lblExitBonus">₹0</strong></div><div>Penalty: <strong style="color:#FBBF24;" id="lblExitPen">₹0</strong></div></div>
    </div>
    <div class="field-box" style="display:flex; align-items:center; gap:6px;">
      <input type="checkbox" id="chkExitIncludeBonus" checked onchange="calcExitNet()">
      <label for="chkExitIncludeBonus" style="font-size:0.82rem; font-weight:700; color:#CBD5E1;">Adjust Bonus in Settlement?</label>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">NPA / Bad Debt Write-off (₹)</label><input type="number" step="1" id="inpExitNpa" class="field-ctrl" value="0" oninput="calcExitNet()"></div>
      <div><label class="field-label">Waiver / Discount (₹)</label><input type="number" step="1" id="inpExitWaiver" class="field-ctrl" value="0" oninput="calcExitNet()"></div>
    </div>
    <div class="field-box" style="background:#060913; border:1px solid #334155; border-radius:6px; padding:10px; text-align:center;">
      <div class="field-label" id="lblExitResultType">FINAL SETTLEMENT AMOUNT</div>
      <div id="dispExitNetResult" style="font-size:1.3rem; font-weight:800; color:#10B981;">₹0</div>
      <input type="hidden" id="inpExitNetRefund" value="0">
    </div>
    <button class="btn btn-red" id="btnSubmitExit" style="width:100%; justify-content:center; padding:11px;">Execute Settlement & Inactivate</button>
  </div>
</div>

<div class="modal-backdrop" id="modalFund">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row"><div style="color:#38BDF8; font-weight:800;">🏛️ Net Liquid Fund & Cash / Bank Register</div><button class="close-x action-close-modal">&times;</button></div>
    <div class="ledger-header" style="grid-template-columns:repeat(3,1fr); margin-bottom:12px;">
      <div><div class="ledger-stat-lbl">CASH IN HAND</div><div class="ledger-stat-val" style="color:#10B981;" id="lblRegCashBal">₹0</div></div>
      <div><div class="ledger-stat-lbl">BANK / ONLINE BALANCE</div><div class="ledger-stat-val" style="color:#38BDF8;" id="lblRegBankBal">₹0</div></div>
      <div><div class="ledger-stat-lbl">NET LIQUID FUND AVAILABLE</div><div class="ledger-stat-val" style="color:#FBBF24;" id="lblRegTotalFund">₹0</div></div>
    </div>
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; flex-wrap:wrap; gap:8px;">
      <div style="font-size:0.85rem; font-weight:700; color:#FBBF24;">📊 Month-wise Fund Summary & Breakdown</div>
      <div style="display:flex; gap:6px; align-items:center;">
        <label class="field-label" style="margin:0;">Filter Date:</label>
        <input type="date" id="inpFundFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
        <input type="date" id="inpFundTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
        <button class="btn btn-dark" id="btnApplyFundDate" style="padding:5px 8px; font-size:0.75rem;">Apply</button>
      </div>
    </div>
    <div style="max-height:180px; overflow-y:auto; margin-bottom:12px;">
      <table><thead><tr><th>MONTH</th><th>INFLOW (+)</th><th>OUTFLOW (-)</th><th>NET STATUS</th><th>DRILLDOWN</th></tr></thead><tbody id="tbodyFundMonths"></tbody></table>
    </div>
    <div id="fundDrilldownBox" style="display:none; background:#1E293B; border-radius:6px; padding:10px; border:1px solid #38BDF8;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
        <div style="font-weight:700; color:#FBBF24; font-size:0.85rem;" id="lblDrilldownTitle">Source Details</div>
        <button class="btn btn-dark" id="btnCloseDrilldown" style="padding:2px 6px; font-size:0.7rem;">Close ✖</button>
      </div>
      <div style="max-height:150px; overflow-y:auto;">
        <table>
          <thead><tr><th>DATE</th><th>MEMBER NAME</th><th>CATEGORY</th><th>AMOUNT</th><th>MODE</th></tr></thead>
          <tbody id="tbodyDrilldown"></tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalNpa">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row"><div style="color:#EF4444; font-weight:800;">⚠️ NPA & Bad Debts Write-off Register</div><button class="close-x action-close-modal">&times;</button></div>
    <div style="max-height:260px; overflow-y:auto;">
      <table><thead><tr><th>DATE</th><th>EXIT ID</th><th>MEMBER NAME / ID</th><th>WRITE-OFF NPA (₹)</th><th>STATUS</th></tr></thead><tbody id="tbodyNpaList"></tbody></table>
    </div>
  </div>
</div>

<div class="modal-backdrop" id="modalSettings">
  <div class="modal-dialog-box">
    <div class="modal-header-row"><div style="color:#FBBF24; font-weight:800;">⚙️ Society Global Settings</div><button class="close-x action-close-modal">&times;</button></div>
    <div class="field-box"><label class="field-label">Global Default Due Date</label><input type="text" id="inpGlobalDueDay" class="field-ctrl" value="15th of every month"></div>
    <div class="field-box"><label class="field-label">Global Default Interest Rate (% p.m. for NEW loans)</label><input type="number" id="inpGlobalRate" class="field-ctrl" value="1.0" step="0.1"></div>
    <small style="color:#94A3B8; display:block; margin-bottom:12px;">Note: Changes apply to default forms and members without custom settings. Issued historical loans remain safe.</small>
    <button class="btn btn-blue" id="btnApplyGlobalSettings" style="width:100%; justify-content:center; padding:11px;">Apply Global Settings</button>
  </div>
</div>
\`;
}
`;

fs.writeFileSync("Part2_Html.gs", p2_html, "utf8");
console.log("Part2_Html.gs written:", p2_html.length);
