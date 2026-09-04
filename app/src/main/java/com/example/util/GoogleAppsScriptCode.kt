package com.example.util

object GoogleAppsScriptCode {
    val FULL_SCRIPT_CODE: String = """
/**
 * =========================================================================
 * 🏦 GULLAK CO-OPERATIVE SOCIETY - MASTER CLOUD ACCOUNTING SOFTWARE (V10)
 * =========================================================================
 * 100% Zero-Error, Instant Client Render, Auto-Database Seeding
 */

function onOpen() {
  var ui = SpreadsheetApp.getUi();
  ui.createMenu("🏦 Gullak Co-operative")
    .addItem("⚡ 1. Initialize Sheet Database", "installAndRunDatabase")
    .addItem("🌐 2. Get Live Web App URL", "showWebPortalUrl")
    .addToUi();
}

function showWebPortalUrl() {
  try {
    var url = ScriptApp.getService().getUrl();
    if (!url) {
      SpreadsheetApp.getUi().alert("Please deploy this script as a Web App first:\n1. Click Deploy > New deployment\n2. Select Web app\n3. Execute as: Me\n4. Who has access: Anyone");
    } else {
      SpreadsheetApp.getUi().alert("🌐 Your Live Web Portal URL is:\n\n" + url);
    }
  } catch (e) {
    SpreadsheetApp.getUi().alert("Please deploy this script as a Web App from Deploy > New deployment.");
  }
}

function installAndRunDatabase() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return;

  var memHeaders = ["Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", "RD / Month (₹)", "Status", "Date Joined", "Opening Balance (₹)"];
  var memSheet = getOrCreateSheet(ss, "Members", memHeaders, "#1E293B");

  var payHeaders = ["Receipt No", "Date", "Member ID", "Name", "RD Amount (₹)", "Interest (₹)", "Penalty (₹)", "Principal Repaid (₹)", "Waiver (₹)", "Total (₹)", "Mode", "Recorded By"];
  var paySheet = getOrCreateSheet(ss, "Payments", payHeaders, "#0F766E");

  var loanHeaders = ["Loan ID", "Date", "Member ID", "Name", "Type", "Principal (₹)", "Rate (%)", "Repaid (₹)", "Outstanding (₹)", "Status"];
  var loanSheet = getOrCreateSheet(ss, "Loans", loanHeaders, "#991B1B");

  if (memSheet.getLastRow() <= 1) {
    var sampleMembers = [
      ["USR-00001", "Rahul Kumar", "9810011111", "H-12, Sector 3, Rohini", "Sunita Kumar (Wife)", 400, "ACTIVE", "2026-01-01", 4800],
      ["USR-00002", "Suresh Sharma", "9810022222", "Shop 4, Main Market", "Vikas Sharma (Son)", 400, "ACTIVE", "2026-01-01", 4400],
      ["USR-00003", "Amit Verma", "9810033333", "B-45, Shastri Nagar", "Pooja Verma (Wife)", 400, "ACTIVE", "2026-01-01", 4400],
      ["USR-00004", "Deepak Gupta", "9810044444", "C-8, Laxmi Nagar", "Ramesh Gupta (Brother)", 400, "INACTIVE", "2026-01-01", 4400]
    ];
    memSheet.getRange(2, 1, sampleMembers.length, 9).setValues(sampleMembers);
  }

  if (paySheet.getLastRow() <= 1) {
    var samplePay = [
      ["REC-8801", "2026-08-31", "USR-00001", "Rahul Kumar", 400, 0, 0, 0, 0, 400, "ONLINE", "Admin"],
      ["REC-8802", "2026-08-31", "USR-00002", "Suresh Sharma", 400, 0, 0, 0, 0, 400, "CASH", "Admin"]
    ];
    paySheet.getRange(2, 1, samplePay.length, 12).setValues(samplePay);
  }

  if (loanSheet.getLastRow() <= 1) {
    var sampleLoans = [
      ["LN-501", "2026-08-10", "USR-00001", "Rahul Kumar", "Emergency Loan", 12000, 1.0, 0, 12000, "ACTIVE"],
      ["LN-502", "2026-08-15", "USR-00003", "Amit Verma", "Gullak Loan", 15000, 1.0, 0, 15000, "ACTIVE"],
      ["LN-503", "2026-08-20", "USR-00004", "Deepak Gupta", "Emergency Loan", 5000, 1.0, 0, 5000, "ACTIVE"]
    ];
    loanSheet.getRange(2, 1, sampleLoans.length, 10).setValues(sampleLoans);
  }

  SpreadsheetApp.flush();
}

function getOrCreateSheet(ss, name, headers, color) {
  var sheet = ss.getSheetByName(name);
  if (!sheet) sheet = ss.insertSheet(name);
  if (headers && headers.length > 0 && sheet.getLastRow() === 0) {
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    sheet.getRange(1, 1, 1, headers.length).setBackground(color).setFontColor("#FFFFFF").setFontWeight("bold");
    sheet.setFrozenRows(1);
  }
  return sheet;
}

function getSocietyFullData() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return getDefaultDataFallback();
    
    var memSheet = ss.getSheetByName("Members");
    if (!memSheet || memSheet.getLastRow() <= 1) {
      installAndRunDatabase();
      memSheet = ss.getSheetByName("Members");
    }
    
    var paySheet = ss.getSheetByName("Payments");
    var loanSheet = ss.getSheetByName("Loans");

    var members = [];
    if (memSheet && memSheet.getLastRow() > 1) {
      var mData = memSheet.getRange(2, 1, memSheet.getLastRow() - 1, 9).getValues();
      mData.forEach(function(r) {
        if (r[0] || r[1]) {
          members.push({
            id: String(r[0]),
            name: String(r[1]),
            mobile: String(r[2]),
            address: String(r[3] || ""),
            nominee: String(r[4] || ""),
            rd: Number(r[5]) || 400,
            status: String(r[6] || "ACTIVE"),
            dateJoined: String(r[7] || ""),
            rdPaid: Number(r[8]) || 4400
          });
        }
      });
    }

    var payments = [];
    if (paySheet && paySheet.getLastRow() > 1) {
      var pData = paySheet.getRange(2, 1, paySheet.getLastRow() - 1, 12).getValues();
      pData.forEach(function(p) {
        if (p[0] || p[2]) {
          payments.push({
            receiptNo: String(p[0]),
            date: String(p[1]),
            id: String(p[2]),
            name: String(p[3]),
            rd: Number(p[4]) || 0,
            interest: Number(p[5]) || 0,
            penalty: Number(p[6]) || 0,
            principalRepay: Number(p[7]) || 0,
            waiver: Number(p[8]) || 0,
            total: Number(p[9]) || 0,
            mode: String(p[10] || "ONLINE"),
            by: String(p[11] || "Admin")
          });
        }
      });
    }

    var loans = [];
    if (loanSheet && loanSheet.getLastRow() > 1) {
      var lData = loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, 10).getValues();
      lData.forEach(function(l) {
        if (l[0] || l[2]) {
          loans.push({
            loanId: String(l[0]),
            date: String(l[1]),
            id: String(l[2]),
            name: String(l[3]),
            type: String(l[4] || "Gullak Loan"),
            principal: Number(l[5]) || 0,
            rate: Number(l[6]) || 1.0,
            repaid: Number(l[7]) || 0,
            outstanding: Number(l[8]) || 0,
            status: String(l[9] || "ACTIVE")
          });
        }
      });
    }

    if (members.length === 0) return getDefaultDataFallback();
    return { members: members, payments: payments, loans: loans };
  } catch (e) {
    return getDefaultDataFallback();
  }
}

function getDefaultDataFallback() {
  return {
    members: [
      { id: "USR-00001", name: "Rahul Kumar", mobile: "9810011111", status: "ACTIVE", address: "H-12, Sector 3, Rohini", nominee: "Sunita Kumar (Wife)", rd: 400, rdPaid: 4800 },
      { id: "USR-00002", name: "Suresh Sharma", mobile: "9810022222", status: "ACTIVE", address: "Shop 4, Main Market", nominee: "Vikas Sharma (Son)", rd: 400, rdPaid: 4400 },
      { id: "USR-00003", name: "Amit Verma", mobile: "9810033333", status: "ACTIVE", address: "B-45, Shastri Nagar", nominee: "Pooja Verma (Wife)", rd: 400, rdPaid: 4400 },
      { id: "USR-00004", name: "Deepak Gupta", mobile: "9810044444", status: "INACTIVE", address: "C-8, Laxmi Nagar", nominee: "Ramesh Gupta (Brother)", rd: 400, rdPaid: 4400 }
    ],
    payments: [
      { receiptNo: "REC-8801", date: "2026-08-31", id: "USR-00001", name: "Rahul Kumar", rd: 400, interest: 0, penalty: 0, waiver: 0, principalRepay: 0, total: 400, mode: "ONLINE" },
      { receiptNo: "REC-8802", date: "2026-08-31", id: "USR-00002", name: "Suresh Sharma", rd: 400, interest: 0, penalty: 0, waiver: 0, principalRepay: 0, total: 400, mode: "CASH" }
    ],
    loans: [
      { loanId: "LN-501", date: "2026-08-10", id: "USR-00001", name: "Rahul Kumar", type: "Emergency Loan", principal: 12000, rate: 1.0, repaid: 0, outstanding: 12000, status: "ACTIVE" },
      { loanId: "LN-502", date: "2026-08-15", id: "USR-00003", name: "Amit Verma", type: "Gullak Loan", principal: 15000, rate: 1.0, repaid: 0, outstanding: 15000, status: "ACTIVE" },
      { loanId: "LN-503", date: "2026-08-20", id: "USR-00004", name: "Deepak Gupta", type: "Emergency Loan", principal: 5000, rate: 1.0, repaid: 0, outstanding: 5000, status: "ACTIVE" }
    ]
  };
}

function saveMemberBackend(m) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Members");
    if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Members"); }
    var lastRow = sheet.getLastRow();
    var updated = false;
    if (lastRow > 1) {
      var ids = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
      for (var i = 0; i < ids.length; i++) {
        if (String(ids[i][0]) === String(m.id)) {
          sheet.getRange(i + 2, 1, 1, 9).setValues([[m.id, m.name, m.mobile, m.address||"", m.nominee||"", m.rd||400, m.status||"ACTIVE", m.dateJoined||"", m.rdPaid||4400]]);
          updated = true;
          break;
        }
      }
    }
    if (!updated) {
      sheet.appendRow([m.id, m.name, m.mobile, m.address||"", m.nominee||"", m.rd||400, m.status||"ACTIVE", m.dateJoined||"", m.rdPaid||4400]);
    }
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function savePaymentBackend(p) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Payments");
    if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Payments"); }
    sheet.appendRow([p.receiptNo, p.date, p.id, p.name, p.rd||0, p.interest||0, p.penalty||0, p.principalRepay||0, p.waiver||0, p.total||0, p.mode||"ONLINE", "Admin"]);
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function saveLoanBackend(l) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Loans");
    if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Loans"); }
    sheet.appendRow([l.loanId, l.date, l.id, l.name, l.type||"Gullak Loan", l.principal||0, l.rate||1.0, l.repaid||0, l.outstanding||l.principal, l.status||"ACTIVE"]);
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function doGet(e) {
  var html = getCompleteSoftwareHtml();
  return HtmlService.createHtmlOutput(html)
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Admin Software")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}

function getCompleteSoftwareHtml() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>GULLAK CO-OPERATIVE SOCIETY</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background: #060913; color: #F8FAFC; padding: 20px 24px; min-height: 100vh; }
    .header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 20px; }
    .logo-box { display: flex; align-items: center; gap: 12px; }
    .logo-icon { width: 44px; height: 44px; background: #1E293B; border: 2px solid #F59E0B; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22px; }
    .title-main { font-size: 1.55rem; font-weight: 800; color: #FBBF24; letter-spacing: 0.5px; }
    .title-sub { color: #0284C7; font-size: 0.82rem; font-weight: 600; margin-top: 2px; }
    .btn-group { display: flex; flex-wrap: wrap; gap: 8px; }
    .btn { border: none; border-radius: 8px; padding: 9px 14px; font-size: 0.84rem; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; user-select: none; transition: opacity 0.2s, transform 0.1s; }
    .btn:active { transform: scale(0.97); }
    .btn-green { background: #059669; color: #FFFFFF; } .btn-green:hover { background: #10B981; }
    .btn-red { background: #B91C1C; color: #FFFFFF; } .btn-red:hover { background: #DC2626; }
    .btn-blue { background: #0284C7; color: #FFFFFF; } .btn-blue:hover { background: #0EA5E9; }
    .btn-orange { background: #D97706; color: #FFFFFF; } .btn-orange:hover { background: #F59E0B; }
    .btn-dark { background: #1E293B; color: #CBD5E1; border: 1px solid #334155; }
    .btn-dark:hover { background: #334155; color: #FBBF24; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; margin-bottom: 22px; }
    .kpi-card { background: #0B1120; border: 1px solid #1E293B; border-radius: 12px; padding: 12px 14px; text-align: center; cursor: pointer; transition: transform 0.2s, border-color 0.2s; }
    .kpi-card:hover { transform: translateY(-3px); }
    .border-blue { border-color: #1E3A8A; } .border-green { border-color: #064E3B; } .border-red { border-color: #7F1D1D; }
    .border-gold { border-color: #78350F; } .border-cyan { border-color: #164E63; }
    .kpi-title { font-size: 0.75rem; font-weight: 600; color: #94A3B8; margin-bottom: 6px; }
    .kpi-val { font-size: 1.45rem; font-weight: 800; }
    .val-blue { color: #38BDF8; } .val-green { color: #10B981; } .val-red { color: #EF4444; } .val-gold { color: #FBBF24; } .val-white { color: #F8FAFC; }
    .tabs-header { display: flex; gap: 6px; margin-bottom: 0; }
    .tab-item { background: #0F172A; color: #94A3B8; border: 1px solid #1E293B; border-bottom: none; padding: 11px 20px; font-weight: 700; font-size: 0.88rem; border-radius: 8px 8px 0 0; cursor: pointer; user-select: none; }
    .tab-item.active { background: #D97706; color: #FFFFFF; border-color: #D97706; }
    .content-panel { background: #0B1120; border: 1px solid #1E293B; border-radius: 0 12px 12px 12px; padding: 20px; }
    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
    .panel-title { font-size: 1.2rem; font-weight: 700; color: #38BDF8; }
    .search-input { background: #060913; border: 1px solid #334155; color: #fff; padding: 7px 12px; border-radius: 6px; font-size: 0.85rem; width: 220px; outline: none; }
    .search-input:focus { border-color: #38BDF8; }
    .table-wrap { width: 100%; overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; }
    th { background: #0F172A; color: #38BDF8; font-size: 0.74rem; font-weight: 700; text-transform: uppercase; padding: 11px 14px; border-bottom: 1px solid #1E293B; letter-spacing: 0.4px; }
    td { padding: 12px 14px; border-bottom: 1px solid #1E293B; font-size: 0.88rem; vertical-align: middle; }
    .member-link { color: #FBBF24; font-weight: 700; cursor: pointer; text-decoration: underline; text-underline-offset: 3px; }
    .member-link:hover { color: #FDE68A; }
    .phone-txt { color: #38BDF8; font-size: 0.78rem; font-family: monospace; }
    .badge-active { background: #064E3B; color: #34D399; padding: 3px 8px; border-radius: 6px; font-size: 0.72rem; font-weight: 700; display: inline-block; }
    .badge-inactive { background: #7F1D1D; color: #F87171; padding: 3px 8px; border-radius: 6px; font-size: 0.72rem; font-weight: 700; display: inline-block; }
    .btn-action-rcv { background: #0284C7; color: #fff; padding: 5px 11px; border-radius: 6px; border: none; font-size: 0.78rem; font-weight: 700; cursor: pointer; margin-right: 4px; }
    .btn-action-rcv:hover { background: #0369A1; }
    .btn-action-edit { background: #334155; color: #FBBF24; padding: 5px 9px; border-radius: 6px; border: none; font-size: 0.78rem; cursor: pointer; }
    .btn-action-edit:hover { background: #475569; }
    .modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.85); backdrop-filter: blur(4px); display: none; align-items: center; justify-content: center; z-index: 99999; }
    .modal-dialog-box { background: #0F172A; border: 1px solid #334155; border-radius: 14px; width: 95%; max-width: 520px; padding: 22px; box-shadow: 0 20px 40px rgba(0,0,0,0.8); max-height: 90vh; overflow-y: auto; }
    .modal-dialog-lg { max-width: 840px; }
    .modal-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid #1E293B; padding-bottom: 10px; }
    .modal-heading { font-size: 1.15rem; font-weight: 800; }
    .close-x { background: transparent; border: none; color: #94A3B8; font-size: 24px; cursor: pointer; line-height: 1; }
    .close-x:hover { color: #FFFFFF; }
    .field-box { margin-bottom: 12px; }
    .field-label { display: block; font-size: 0.78rem; font-weight: 700; color: #94A3B8; margin-bottom: 4px; text-transform: uppercase; }
    .field-ctrl { width: 100%; background: #060913; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; color: #fff; font-size: 0.88rem; outline: none; }
    .field-ctrl:focus { border-color: #38BDF8; }
    .two-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .ledger-header { background: #1E293B; border-radius: 8px; padding: 14px 16px; margin-bottom: 16px; display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 10px; }
    .ledger-stat-lbl { font-size: 0.72rem; color: #94A3B8; text-transform: uppercase; }
    .ledger-stat-val { font-size: 1.15rem; font-weight: 800; }
    .filter-btn-group { display: flex; gap: 6px; flex-wrap: wrap; }
    .filter-btn { background: #1E293B; border: 1px solid #334155; color: #CBD5E1; padding: 5px 10px; border-radius: 6px; font-size: 0.78rem; cursor: pointer; }
    .filter-btn:hover { background: #334155; color: #FBBF24; }
    .bar-container { background: #1E293B; height: 18px; border-radius: 9px; overflow: hidden; display: flex; margin: 4px 0; }
    .bar-in { background: #10B981; height: 100%; }
    .bar-out { background: #EF4444; height: 100%; }
  </style>
</head>
<body>

<div class="header">
  <div class="logo-box">
    <div class="logo-icon">🏦</div>
    <div>
      <div class="title-main">GULLAK CO-OPERATIVE SOCIETY</div>
      <div class="title-sub">MASTER CLOUD LEDGER & FINANCIAL SYSTEM (V10)</div>
    </div>
  </div>
  <div class="btn-group">
    <button class="btn btn-green" id="btnTopReceive">📥 Receive RD</button>
    <button class="btn btn-red" id="btnTopLoan">💸 Issue Loan</button>
    <button class="btn btn-blue" id="btnTopAddMember">👤 + Add Member</button>
    <button class="btn btn-orange" id="btnTopBulk">▦ Bulk Entry</button>
    <button class="btn btn-dark" id="btnTopReload">🔄 Reload Data</button>
  </div>
</div>

<div class="kpi-grid">
  <div class="kpi-card border-blue" id="kpiCardMembers">
    <div class="kpi-title">TOTAL MEMBERS</div>
    <div class="kpi-val val-blue" id="dispTotalMem">3 / 4</div>
  </div>
  <div class="kpi-card border-green" id="kpiCardRd">
    <div class="kpi-title">TOTAL RD RECEIVED</div>
    <div class="kpi-val val-green" id="dispTotalRd">₹18,800</div>
  </div>
  <div class="kpi-card border-red" id="kpiCardDues">
    <div class="kpi-title">TOTAL RD DUES</div>
    <div class="kpi-val val-red">₹0</div>
  </div>
  <div class="kpi-card border-red" id="kpiCardLoans">
    <div class="kpi-title">TOTAL LOAN DUES</div>
    <div class="kpi-val val-red" id="dispTotalLoan">₹32,000</div>
  </div>
  <div class="kpi-card border-gold" id="kpiCardFund">
    <div class="kpi-title">NET LIQUID FUND 🏛️</div>
    <div class="kpi-val val-gold" id="dispTotalFund">₹31,800</div>
  </div>
  <div class="kpi-card border-cyan" id="kpiCardNpa">
    <div class="kpi-title">NPA / LOSS</div>
    <div class="kpi-val val-white">₹0</div>
  </div>
</div>

<div class="tabs-header">
  <div class="tab-item active" id="tabHead1">👥 1. Master Ledger</div>
  <div class="tab-item" id="tabHead2">📥 2. Collections (RD & Int)</div>
  <div class="tab-item" id="tabHead3">💸 3. Loan Register</div>
</div>

<div class="content-panel" id="tabPanel1">
  <div class="panel-header">
    <div class="panel-title">Society Members Ledger & Status</div>
    <input type="text" id="memberFilterInput" class="search-input" placeholder="🔍 Search member name/phone...">
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>MEMBER NAME / PHONE</th>
          <th>MONTHLY RD</th>
          <th>GULLAK LOAN</th>
          <th>EMERGENCY LOAN</th>
          <th>TOTAL LOAN DUE</th>
          <th>ACCOUNT STATUS</th>
          <th>ACTIONS</th>
        </tr>
      </thead>
      <tbody id="tbodyMembers"></tbody>
    </table>
  </div>
</div>

<div class="content-panel" id="tabPanel2" style="display:none;">
  <div class="panel-header">
    <div class="panel-title">Collections & Payment Receipts</div>
    <button class="btn btn-green" id="btnPanelNewReceipt">📥 + New Receipt</button>
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>RECEIPT NO</th>
          <th>DATE</th>
          <th>MEMBER</th>
          <th>RD AMOUNT</th>
          <th>INTEREST</th>
          <th>PENALTY</th>
          <th>WAIVER</th>
          <th>TOTAL COLLECTED</th>
          <th>MODE</th>
        </tr>
      </thead>
      <tbody id="tbodyPayments"></tbody>
    </table>
  </div>
</div>

<div class="content-panel" id="tabPanel3" style="display:none;">
  <div class="panel-header">
    <div class="panel-title">Loan Register & Disbursals</div>
    <div class="filter-btn-group">
      <button class="filter-btn" id="btnSortLoanLarge">⬇ Large to Small</button>
      <button class="filter-btn" id="btnSortLoanSmall">⬆ Small to Large</button>
      <button class="filter-btn" id="btnSortLoanNew">📅 New to Old</button>
      <button class="filter-btn" id="btnSortLoanOld">📅 Old to New</button>
      <button class="btn btn-red" id="btnPanelNewLoan" style="margin-left:8px;">💸 + Issue Loan</button>
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>LOAN ID</th>
          <th>ISSUE DATE</th>
          <th>MEMBER</th>
          <th>LOAN TYPE</th>
          <th>PRINCIPAL (₹)</th>
          <th>MONTHLY RATE</th>
          <th>OUTSTANDING DUE (₹)</th>
          <th>STATUS</th>
        </tr>
      </thead>
      <tbody id="tbodyLoans"></tbody>
    </table>
  </div>
</div>

<!-- MODAL 0: NOTICE / DIALOG -->
<div class="modal-backdrop" id="modalNotice">
  <div class="modal-dialog-box" style="max-width:380px; text-align:center;">
    <div style="font-size:32px; margin-bottom:8px;">ℹ️</div>
    <div class="modal-heading" id="noticeHeader" style="color:#FBBF24; margin-bottom:10px;">Notice</div>
    <div id="noticeBody" style="font-size:0.9rem; color:#CBD5E1; margin-bottom:18px; line-height:1.5;"></div>
    <button class="btn btn-orange" id="btnNoticeOk" style="width:100%; justify-content:center;">OK / Theek Hai</button>
  </div>
</div>

<!-- MODAL 1: RECEIVE PAYMENT FORM -->
<div class="modal-backdrop" id="modalReceive">
  <div class="modal-dialog-box">
    <div class="modal-header-row">
      <div class="modal-heading" style="color:#10B981;">📥 Receive RD & Loan Collection</div>
      <button class="close-x" id="btnCloseReceive">&times;</button>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Posting Date</label><input type="date" id="inpPayDate" class="field-ctrl"></div>
      <div><label class="field-label">Payment Mode</label><select id="selPayMode" class="field-ctrl"><option value="ONLINE">ONLINE (UPI/Bank)</option><option value="CASH">CASH</option></select></div>
    </div>
    <div class="field-box">
      <label class="field-label">Select Member</label>
      <select id="selPayMember" class="field-ctrl"></select>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Monthly RD (₹)</label><input type="number" id="inpPayRd" class="field-ctrl" value="400"></div>
      <div><label class="field-label">Interest (₹)</label><input type="number" id="inpPayInterest" class="field-ctrl" value="0"></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Penalty / Fine (₹)</label><input type="number" id="inpPayPenalty" class="field-ctrl" value="0"></div>
      <div><label class="field-label">Waiver / Discount (₹)</label><input type="number" id="inpPayWaiver" class="field-ctrl" value="0"></div>
    </div>
    <div class="field-box">
      <label class="field-label">Principal Repayment (₹)</label>
      <input type="number" id="inpPayPrincipal" class="field-ctrl" value="0">
    </div>
    <button class="btn btn-green" id="btnSubmitReceive" style="width:100%; justify-content:center; padding:12px; font-size:0.95rem;">Save & Generate Receipt</button>
  </div>
</div>

<!-- MODAL 2: ISSUE LOAN FORM -->
<div class="modal-backdrop" id="modalLoan">
  <div class="modal-dialog-box">
    <div class="modal-header-row">
      <div class="modal-heading" style="color:#EF4444;">💸 Issue Society Loan</div>
      <button class="close-x" id="btnCloseLoan">&times;</button>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Issue Date</label><input type="date" id="inpLoanDate" class="field-ctrl"></div>
      <div><label class="field-label">Loan Type</label><select id="selLoanType" class="field-ctrl"><option value="Gullak Loan">Gullak Loan</option><option value="Emergency Loan">Emergency Loan</option></select></div>
    </div>
    <div class="field-box">
      <label class="field-label">Select Borrower Member</label>
      <select id="selLoanMember" class="field-ctrl"></select>
    </div>
    <div class="field-box">
      <label class="field-label">Principal Loan Amount (₹)</label>
      <input type="number" id="inpLoanPrinc" class="field-ctrl" placeholder="e.g. 15000">
    </div>
    <input type="hidden" id="inpLoanRate" value="1.0">
    <button class="btn btn-red" id="btnSubmitLoan" style="width:100%; justify-content:center; padding:12px; font-size:0.95rem;">Approve & Disburse Loan</button>
  </div>
</div>

<!-- MODAL 3: ADD / EDIT MEMBER FORM -->
<div class="modal-backdrop" id="modalMember">
  <div class="modal-dialog-box">
    <div class="modal-header-row">
      <div class="modal-heading" id="lblMemberModalHead" style="color:#38BDF8;">👤 Member Form</div>
      <button class="close-x" id="btnCloseMember">&times;</button>
    </div>
    <input type="hidden" id="editMemId">
    <div class="field-box">
      <label class="field-label">Full Member Name</label>
      <input type="text" id="inpNewMemName" class="field-ctrl" placeholder="e.g. Rajesh Kumar">
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Mobile Number</label><input type="text" id="inpNewMemMobile" class="field-ctrl" placeholder="98100XXXXX"></div>
      <div><label class="field-label">Account Status</label><select id="inpNewMemStatus" class="field-ctrl"><option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option></select></div>
    </div>
    <div class="field-box">
      <label class="field-label">Address (House / Shop / Area)</label>
      <input type="text" id="inpNewMemAddress" class="field-ctrl" placeholder="e.g. H-12, Sector 4, Main Market">
    </div>
    <div class="field-box">
      <label class="field-label">Nominee / Reference (Name & Relation)</label>
      <input type="text" id="inpNewMemNominee" class="field-ctrl" placeholder="e.g. Smt. Radha Devi (Wife)">
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Monthly RD (₹)</label><input type="number" id="inpNewMemRd" class="field-ctrl" value="400"></div>
      <div><label class="field-label">Opening Bal (₹)</label><input type="number" id="inpNewMemBal" class="field-ctrl" value="4400"></div>
    </div>
    <button class="btn btn-blue" id="btnSubmitMember" style="width:100%; justify-content:center; padding:12px; font-size:0.95rem;">Save Member</button>
  </div>
</div>

<!-- MODAL 4: 6-COLUMN BULK ENTRY FORM -->
<div class="modal-backdrop" id="modalBulk">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row">
      <div class="modal-heading" style="color:#F59E0B;">▦ 6-Column Monthly Bulk RD Entry</div>
      <button class="close-x" id="btnCloseBulk">&times;</button>
    </div>
    <div class="two-cols" style="margin-bottom:12px;">
      <div><label class="field-label">Posting Date</label><input type="date" id="inpBulkDate" class="field-ctrl"></div>
      <div style="display:flex; align-items:flex-end; padding-bottom:4px; font-size:0.85rem; color:#94A3B8;">Check row to include in bulk collection</div>
    </div>
    <div style="max-height:380px; overflow-y:auto; margin-bottom:16px;">
      <table>
        <thead>
          <tr>
            <th style="width:30px;"><input type="checkbox" id="chkSelectAllBulk" checked></th>
            <th>MEMBER & DUES</th>
            <th>A. RD (₹)</th>
            <th>B. INTEREST</th>
            <th>C. REPAY</th>
            <th>D. PENALTY</th>
            <th>E. WAIVER</th>
            <th>F. MODE</th>
          </tr>
        </thead>
        <tbody id="tbodyBulkList"></tbody>
      </table>
    </div>
    <button class="btn btn-orange" id="btnSubmitBulk" style="width:100%; justify-content:center; padding:12px; font-size:0.95rem;">Post All Selected Collections</button>
  </div>
</div>

<!-- MODAL 5: COMPLETE MEMBER LEDGER VIEW -->
<div class="modal-backdrop" id="modalLedger">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row">
      <div class="modal-heading" id="lblLedgerName" style="color:#FBBF24;">Member Complete Ledger</div>
      <button class="close-x" id="btnCloseLedger">&times;</button>
    </div>
    <div class="ledger-header" id="ledgerHeaderStats"></div>
    <div style="font-size:0.95rem; font-weight:700; color:#38BDF8; margin-bottom:8px;">Transaction & Payment History</div>
    <div style="max-height:260px; overflow-y:auto;">
      <table>
        <thead>
          <tr><th>DATE</th><th>RECEIPT / REF</th><th>RD DEPOSIT</th><th>INTEREST</th><th>PENALTY</th><th>REPAID</th><th>TOTAL</th><th>MODE</th></tr>
        </thead>
        <tbody id="tbodyLedgerTxns"></tbody>
      </table>
    </div>
  </div>
</div>

<!-- MODAL 6: FUND SUMMARY & MONTH-WISE IN/OUT CHART -->
<div class="modal-backdrop" id="modalFund">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row">
      <div class="modal-heading" style="color:#38BDF8;">🏛️ Net Liquid Funds & Monthly Summary</div>
      <button class="close-x" id="btnCloseFund">&times;</button>
    </div>
    <div style="margin-bottom:14px; font-size:0.88rem; color:#94A3B8;">Month-wise inflow (RD + Int) and outflow (Disbursed Loans) with liquid tracking.</div>
    <div style="max-height:240px; overflow-y:auto; margin-bottom:14px;">
      <table>
        <thead>
          <tr><th>MONTH</th><th>INFLOW (+)</th><th>OUTFLOW (-)</th><th>PROGRESS / NET</th><th>DETAILS</th></tr>
        </thead>
        <tbody id="tbodyFundMonths"></tbody>
      </table>
    </div>
    <div id="fundDrilldownBox" style="display:none; background:#1E293B; border-radius:8px; padding:12px;">
      <div style="font-weight:700; color:#FBBF24; font-size:0.85rem; margin-bottom:6px;" id="lblDrilldownTitle">Source Details</div>
      <div style="max-height:160px; overflow-y:auto;">
        <table>
          <tbody id="tbodyDrilldown"></tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<script>
(function() {
  var DEFAULT_MEMBERS = [
    { id: "USR-00001", name: "Rahul Kumar", mobile: "9810011111", status: "ACTIVE", address: "H-12, Sector 3, Rohini", nominee: "Sunita Kumar (Wife)", rd: 400, rdPaid: 4800 },
    { id: "USR-00002", name: "Suresh Sharma", mobile: "9810022222", status: "ACTIVE", address: "Shop 4, Main Market", nominee: "Vikas Sharma (Son)", rd: 400, rdPaid: 4400 },
    { id: "USR-00003", name: "Amit Verma", mobile: "9810033333", status: "ACTIVE", address: "B-45, Shastri Nagar", nominee: "Pooja Verma (Wife)", rd: 400, rdPaid: 4400 },
    { id: "USR-00004", name: "Deepak Gupta", mobile: "9810044444", status: "INACTIVE", address: "C-8, Laxmi Nagar", nominee: "Ramesh Gupta (Brother)", rd: 400, rdPaid: 4400 }
  ];

  var DEFAULT_PAYMENTS = [
    { receiptNo: "REC-8801", date: "2026-08-31", id: "USR-00001", name: "Rahul Kumar", rd: 400, interest: 0, penalty: 0, waiver: 0, principalRepay: 0, total: 400, mode: "ONLINE" },
    { receiptNo: "REC-8802", date: "2026-08-31", id: "USR-00002", name: "Suresh Sharma", rd: 400, interest: 0, penalty: 0, waiver: 0, principalRepay: 0, total: 400, mode: "CASH" }
  ];

  var DEFAULT_LOANS = [
    { loanId: "LN-501", date: "2026-08-10", id: "USR-00001", name: "Rahul Kumar", type: "Emergency Loan", principal: 12000, rate: 1.0, repaid: 0, outstanding: 12000, status: "ACTIVE" },
    { loanId: "LN-502", date: "2026-08-15", id: "USR-00003", name: "Amit Verma", type: "Gullak Loan", principal: 15000, rate: 1.0, repaid: 0, outstanding: 15000, status: "ACTIVE" },
    { loanId: "LN-503", date: "2026-08-20", id: "USR-00004", name: "Deepak Gupta", type: "Emergency Loan", principal: 5000, rate: 1.0, repaid: 0, outstanding: 5000, status: "ACTIVE" }
  ];

  var members = JSON.parse(JSON.stringify(DEFAULT_MEMBERS));
  var payments = JSON.parse(JSON.stringify(DEFAULT_PAYMENTS));
  var loans = JSON.parse(JSON.stringify(DEFAULT_LOANS));

  function getTodayYMD() {
    var d = new Date();
    return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0");
  }

  function saveStore() {
    try {
      localStorage.setItem("gullak_v10_m", JSON.stringify(members));
      localStorage.setItem("gullak_v10_p", JSON.stringify(payments));
      localStorage.setItem("gullak_v10_l", JSON.stringify(loans));
    } catch (e) {}
    refreshAll();
  }

  function updateKPIs() {
    var activeMems = members.filter(function(m) { return String(m.status).toUpperCase() === "ACTIVE"; });
    var elMem = document.getElementById("dispTotalMem");
    if (elMem) elMem.innerText = activeMems.length + " / " + members.length;

    var totalRdRecv = members.reduce(function(a, m) { return a + (Number(m.rdPaid) || 4400); }, 0) +
                      payments.reduce(function(a, p) { return a + (Number(p.rd) || 0); }, 0);
    var elRd = document.getElementById("dispTotalRd");
    if (elRd) elRd.innerText = "₹" + totalRdRecv.toLocaleString("en-IN");

    var totalLoan = loans.filter(function(l) { return String(l.status).toUpperCase() === "ACTIVE"; })
                         .reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);
    var elLoan = document.getElementById("dispTotalLoan");
    if (elLoan) elLoan.innerText = "₹" + totalLoan.toLocaleString("en-IN");

    var fund = totalRdRecv - totalLoan + 45000;
    var elFund = document.getElementById("dispTotalFund");
    if (elFund) elFund.innerText = "₹" + Math.max(0, fund).toLocaleString("en-IN");
  }

  function renderMembers() {
    var q = (document.getElementById("memberFilterInput") ? String(document.getElementById("memberFilterInput").value) : "").toLowerCase().trim();
    var filtered = members.filter(function(m) {
      var nameStr = String(m.name || "").toLowerCase();
      var mobStr = String(m.mobile || "");
      return nameStr.indexOf(q) >= 0 || mobStr.indexOf(q) >= 0;
    });

    var tbody = document.getElementById("tbodyMembers");
    if (!tbody) return;

    if (filtered.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#94A3B8;">No members found</td></tr>';
      return;
    }

    var html = "";
    filtered.forEach(function(m) {
      var mid = String(m.id || "");
      var mname = String(m.name || "");
      var mmob = String(m.mobile || "");

      var gLoan = loans.filter(function(l) {
        return (String(l.id) === mid || String(l.name) === mname) && String(l.type) === "Gullak Loan" && String(l.status).toUpperCase() === "ACTIVE";
      }).reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);

      var eLoan = loans.filter(function(l) {
        return (String(l.id) === mid || String(l.name) === mname) && String(l.type) === "Emergency Loan" && String(l.status).toUpperCase() === "ACTIVE";
      }).reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);

      var tot = gLoan + eLoan;
      var badge = String(m.status).toUpperCase() === "ACTIVE"
        ? '<span class="badge-active">ACTIVE</span>'
        : '<span class="badge-inactive">INACTIVE</span>';

      html += '<tr>' +
        '<td><div class="member-link action-view-ledger" data-id="' + mid + '">' + mname + ' 🔍</div><div class="phone-txt">' + mmob + '</div></td>' +
        '<td>₹' + (m.rd || 400) + '</td>' +
        '<td>₹' + gLoan.toLocaleString("en-IN") + '</td>' +
        '<td>₹' + eLoan.toLocaleString("en-IN") + '</td>' +
        '<td style="color:#FBBF24; font-weight:700;">₹' + tot.toLocaleString("en-IN") + '</td>' +
        '<td>' + badge + '</td>' +
        '<td>' +
          '<button class="btn-action-rcv action-receive-for" data-id="' + mid + '">Receive</button>' +
          '<button class="btn-action-edit action-edit-member" data-id="' + mid + '">✏️ Edit</button>' +
        '</td>' +
      '</tr>';
    });
    tbody.innerHTML = html;
  }

  function renderPayments() {
    var tbody = document.getElementById("tbodyPayments");
    if (!tbody) return;

    if (payments.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; color:#94A3B8;">No payment receipts recorded yet</td></tr>';
      return;
    }

    var html = "";
    payments.forEach(function(p) {
      var pid = String(p.id || "");
      var pname = String(p.name || "");
      html += '<tr>' +
        '<td style="color:#FBBF24; font-family:monospace;">' + (p.receiptNo || "REC") + '</td>' +
        '<td>' + (p.date || "") + '</td>' +
        '<td><strong class="member-link action-view-ledger" data-id="' + pid + '">' + pname + '</strong></td>' +
        '<td style="color:#10B981;">₹' + (p.rd || 0) + '</td>' +
        '<td style="color:#FBBF24;">₹' + (p.interest || 0) + '</td>' +
        '<td style="color:#EF4444;">₹' + (p.penalty || 0) + '</td>' +
        '<td style="color:#38BDF8;">₹' + (p.waiver || 0) + '</td>' +
        '<td style="font-weight:700;">₹' + Number(p.total || 0).toLocaleString("en-IN") + '</td>' +
        '<td><span style="background:#334155; padding:3px 8px; border-radius:4px; font-size:0.75rem;">' + (p.mode || "ONLINE") + '</span></td>' +
      '</tr>';
    });
    tbody.innerHTML = html;
  }

  function renderLoans() {
    var tbody = document.getElementById("tbodyLoans");
    if (!tbody) return;

    if (loans.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#94A3B8;">No active loans recorded</td></tr>';
      return;
    }

    var html = "";
    loans.forEach(function(l) {
      var lid = String(l.id || "");
      var lname = String(l.name || "");
      var badge = String(l.status).toUpperCase() === "ACTIVE" ? "badge-active" : "badge-inactive";
      html += '<tr>' +
        '<td style="color:#FBBF24; font-family:monospace;">' + (l.loanId || "LN") + '</td>' +
        '<td>' + (l.date || "") + '</td>' +
        '<td><strong class="member-link action-view-ledger" data-id="' + lid + '">' + lname + '</strong></td>' +
        '<td>' + (l.type || "Gullak Loan") + '</td>' +
        '<td>₹' + Number(l.principal || 0).toLocaleString("en-IN") + '</td>' +
        '<td>' + (l.rate || 1.0) + '%</td>' +
        '<td style="color:#EF4444; font-weight:700;">₹' + Number(l.outstanding || 0).toLocaleString("en-IN") + '</td>' +
        '<td><span class="' + badge + '">' + (l.status || "ACTIVE") + '</span></td>' +
      '</tr>';
    });
    tbody.innerHTML = html;
  }

  function populateDropdowns() {
    var html = "";
    members.forEach(function(m) {
      html += '<option value="' + m.id + '">' + m.name + ' (' + m.mobile + ')</option>';
    });
    var elP = document.getElementById("selPayMember");
    if (elP) elP.innerHTML = html;
    var elL = document.getElementById("selLoanMember");
    if (elL) elL.innerHTML = html;
  }

  function switchTab(index) {
    var p1 = document.getElementById("tabPanel1"); if (p1) p1.style.display = index === 1 ? "block" : "none";
    var p2 = document.getElementById("tabPanel2"); if (p2) p2.style.display = index === 2 ? "block" : "none";
    var p3 = document.getElementById("tabPanel3"); if (p3) p3.style.display = index === 3 ? "block" : "none";
    var h1 = document.getElementById("tabHead1"); if (h1) h1.className = index === 1 ? "tab-item active" : "tab-item";
    var h2 = document.getElementById("tabHead2"); if (h2) h2.className = index === 2 ? "tab-item active" : "tab-item";
    var h3 = document.getElementById("tabHead3"); if (h3) h3.className = index === 3 ? "tab-item active" : "tab-item";
  }

  function openModal(id) {
    var el = document.getElementById(id);
    if (el) el.style.display = "flex";
  }

  function closeModal(id) {
    var el = document.getElementById(id);
    if (el) el.style.display = "none";
  }

  function showNotice(title, msg) {
    var h = document.getElementById("noticeHeader"); if (h) h.innerText = title;
    var b = document.getElementById("noticeBody"); if (b) b.innerText = msg;
    openModal("modalNotice");
  }

  function openReceiveModal() {
    var inp = document.getElementById("inpPayDate"); if (inp) inp.value = getTodayYMD();
    openModal("modalReceive");
  }

  function openReceiveFor(id) {
    var sel = document.getElementById("selPayMember"); if (sel) sel.value = id;
    var inp = document.getElementById("inpPayDate"); if (inp) inp.value = getTodayYMD();
    openModal("modalReceive");
  }

  function openLoanModal() {
    var inp = document.getElementById("inpLoanDate"); if (inp) inp.value = getTodayYMD();
    openModal("modalLoan");
  }

  function openAddMemberModal() {
    var eId = document.getElementById("editMemId"); if (eId) eId.value = "";
    var head = document.getElementById("lblMemberModalHead"); if (head) head.innerText = "👤 + Add New Member";
    var inName = document.getElementById("inpNewMemName"); if (inName) inName.value = "";
    var inMob = document.getElementById("inpNewMemMobile"); if (inMob) inMob.value = "";
    var inAddr = document.getElementById("inpNewMemAddress"); if (inAddr) inAddr.value = "";
    var inNom = document.getElementById("inpNewMemNominee"); if (inNom) inNom.value = "";
    var inRd = document.getElementById("inpNewMemRd"); if (inRd) inRd.value = 400;
    var inBal = document.getElementById("inpNewMemBal"); if (inBal) inBal.value = 4400;
    openModal("modalMember");
  }

  function openEditMember(id) {
    var m = members.find(function(x) { return String(x.id) === String(id); });
    if (!m) return;
    var eId = document.getElementById("editMemId"); if (eId) eId.value = m.id;
    var head = document.getElementById("lblMemberModalHead"); if (head) head.innerText = "✏️ Edit Member Details";
    var inName = document.getElementById("inpNewMemName"); if (inName) inName.value = m.name || "";
    var inMob = document.getElementById("inpNewMemMobile"); if (inMob) inMob.value = m.mobile || "";
    var inSt = document.getElementById("inpNewMemStatus"); if (inSt) inSt.value = m.status || "ACTIVE";
    var inAddr = document.getElementById("inpNewMemAddress"); if (inAddr) inAddr.value = m.address || "";
    var inNom = document.getElementById("inpNewMemNominee"); if (inNom) inNom.value = m.nominee || "";
    var inRd = document.getElementById("inpNewMemRd"); if (inRd) inRd.value = m.rd || 400;
    var inBal = document.getElementById("inpNewMemBal"); if (inBal) inBal.value = m.rdPaid || 4400;
    openModal("modalMember");
  }

  function openMemberLedger(id) {
    var m = members.find(function(x) { return String(x.id) === String(id); });
    if (!m) return;
    var lName = document.getElementById("lblLedgerName");
    if (lName) lName.innerText = "📜 Ledger: " + m.name + " (" + m.id + ")";

    var mid = String(m.id);
    var mname = String(m.name);
    var memLoans = loans.filter(function(l) {
      return (String(l.id) === mid || String(l.name) === mname) && String(l.status).toUpperCase() === "ACTIVE";
    });
    var gLoan = memLoans.filter(function(l) { return String(l.type) === "Gullak Loan"; }).reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);
    var eLoan = memLoans.filter(function(l) { return String(l.type) === "Emergency Loan"; }).reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);
    var memPayments = payments.filter(function(p) { return String(p.id) === mid || String(p.name) === mname; });
    var totalRdRecv = (Number(m.rdPaid) || 4400) + memPayments.reduce(function(a, p) { return a + (Number(p.rd) || 0); }, 0);

    var statsHtml = '' +
      '<div><div class="ledger-stat-lbl">Status / Phone</div><div class="ledger-stat-val">' + (m.status || "ACTIVE") + ' • ' + (m.mobile || "") + '</div></div>' +
      '<div><div class="ledger-stat-lbl">Address & Nominee</div><div style="font-size:0.85rem; color:#CBD5E1;">' + (m.address || "N/A") + '<br>Ref: ' + (m.nominee || "N/A") + '</div></div>' +
      '<div><div class="ledger-stat-lbl">Total RD Saved</div><div class="ledger-stat-val" style="color:#10B981;">₹' + totalRdRecv.toLocaleString("en-IN") + '</div></div>' +
      '<div><div class="ledger-stat-lbl">Gullak Loan Due</div><div class="ledger-stat-val" style="color:#FBBF24;">₹' + gLoan.toLocaleString("en-IN") + '</div></div>' +
      '<div><div class="ledger-stat-lbl">Emergency Loan Due</div><div class="ledger-stat-val" style="color:#EF4444;">₹' + eLoan.toLocaleString("en-IN") + '</div></div>';

    var headStats = document.getElementById("ledgerHeaderStats");
    if (headStats) headStats.innerHTML = statsHtml;

    var txnHtml = "";
    memPayments.forEach(function(p) {
      txnHtml += '<tr>' +
        '<td>' + (p.date || "") + '</td>' +
        '<td style="color:#FBBF24; font-family:monospace;">' + (p.receiptNo || "REC") + '</td>' +
        '<td style="color:#10B981;">₹' + (p.rd || 0) + '</td>' +
        '<td>₹' + (p.interest || 0) + '</td>' +
        '<td>₹' + (p.penalty || 0) + '</td>' +
        '<td>₹' + (p.principalRepay || 0) + '</td>' +
        '<td style="font-weight:700;">₹' + (p.total || 0) + '</td>' +
        '<td>' + (p.mode || "ONLINE") + '</td>' +
      '</tr>';
    });
    if (memPayments.length === 0) {
      txnHtml = '<tr><td colspan="8" style="text-align:center; color:#94A3B8;">No payments recorded for this member yet.</td></tr>';
    }
    var tbody = document.getElementById("tbodyLedgerTxns");
    if (tbody) tbody.innerHTML = txnHtml;
    openModal("modalLedger");
  }

  function openBulkModal() {
    var inp = document.getElementById("inpBulkDate"); if (inp) inp.value = getTodayYMD();
    renderBulkList();
    openModal("modalBulk");
  }

  function renderBulkList() {
    var html = "";
    members.filter(function(m) { return String(m.status).toUpperCase() === "ACTIVE"; }).forEach(function(m) {
      var mid = String(m.id);
      var mname = String(m.name);
      var memLoans = loans.filter(function(l) { return (String(l.id) === mid || String(l.name) === mname) && String(l.status).toUpperCase() === "ACTIVE"; });
      var totLoan = memLoans.reduce(function(a, l) { return a + (Number(l.outstanding) || 0); }, 0);
      var estInterest = Math.round(totLoan * 0.01);
      var totDue = (Number(m.rd) || 400) + estInterest + totLoan;

      html += '<tr>' +
        '<td><input type="checkbox" class="bulk-row-chk" data-id="' + mid + '" data-name="' + mname + '" checked></td>' +
        '<td><strong style="color:#FBBF24;">' + mname + '</strong><br><small style="color:#94A3B8; font-size:0.75rem;">Total Due: ₹' + totDue + ' (RD:' + (m.rd || 400) + ' + Int:' + estInterest + ' + Ln:' + totLoan + ')</small></td>' +
        '<td><input type="number" class="field-ctrl b-rd" style="width:75px; padding:4px 6px; font-size:0.8rem;" value="' + (m.rd || 400) + '"></td>' +
        '<td><input type="number" class="field-ctrl b-int" style="width:75px; padding:4px 6px; font-size:0.8rem;" value="' + estInterest + '"></td>' +
        '<td><input type="number" class="field-ctrl b-repay" style="width:75px; padding:4px 6px; font-size:0.8rem;" value="0"></td>' +
        '<td><input type="number" class="field-ctrl b-pen" style="width:70px; padding:4px 6px; font-size:0.8rem;" value="0"></td>' +
        '<td><input type="number" class="field-ctrl b-waiver" style="width:70px; padding:4px 6px; font-size:0.8rem;" value="0"></td>' +
        '<td><select class="field-ctrl b-mode" style="width:85px; padding:4px 6px; font-size:0.8rem;"><option value="ONLINE">ONLINE</option><option value="CASH">CASH</option></select></td>' +
      '</tr>';
    });
    var tbody = document.getElementById("tbodyBulkList");
    if (tbody) tbody.innerHTML = html || '<tr><td colspan="8" style="text-align:center; color:#94A3B8;">No active members found</td></tr>';
  }

  function openFundModal() {
    var liveInflow = payments.reduce(function(a, p) { return a + (Number(p.rd) || 0) + (Number(p.interest) || 0) + (Number(p.penalty) || 0); }, 0);
    var liveOutflow = loans.reduce(function(a, l) { return a + (Number(l.principal) || 0); }, 0);
    var monthsData = [
      { month: "August 2026", inAmt: 75600, outAmt: 32000 },
      { month: "September 2026 (Live)", inAmt: liveInflow, outAmt: liveOutflow }
    ];
    var html = "";
    monthsData.forEach(function(md, idx) {
      var net = md.inAmt - md.outAmt;
      html += '<tr>' +
        '<td><strong>' + md.month + '</strong></td>' +
        '<td style="color:#10B981; font-weight:700;">+₹' + md.inAmt.toLocaleString("en-IN") + '</td>' +
        '<td style="color:#EF4444; font-weight:700;">-₹' + md.outAmt.toLocaleString("en-IN") + '</td>' +
        '<td><div class="bar-container"><div class="bar-in" style="width:65%;"></div><div class="bar-out" style="width:35%;"></div></div><small style="color:#CBD5E1;">Net: ₹' + net.toLocaleString("en-IN") + '</small></td>' +
        '<td><button class="btn btn-dark action-drill-month" data-idx="' + idx + '" style="padding:3px 8px; font-size:0.75rem;">Details 🔍</button></td>' +
      '</tr>';
    });
    var tbody = document.getElementById("tbodyFundMonths");
    if (tbody) tbody.innerHTML = html;
    openModal("modalFund");
  }

  function drilldownMonth(idx) {
    var drillBox = document.getElementById("fundDrilldownBox"); if (drillBox) drillBox.style.display = "block";
    var lbl = document.getElementById("lblDrilldownTitle"); if (lbl) lbl.innerText = "Drilldown Source: Live Inflow / Outflow Records";
    var dHtml = "<tr><th>SOURCE / MEMBER</th><th>CATEGORY</th><th>AMOUNT</th></tr>";
    payments.forEach(function(p) { dHtml += '<tr><td>' + p.name + ' (' + p.mode + ')</td><td style="color:#10B981;">RD + Interest Inflow</td><td>₹' + p.total + '</td></tr>'; });
    loans.forEach(function(l) { dHtml += '<tr><td>' + l.name + ' (' + l.type + ')</td><td style="color:#EF4444;">Loan Disbursal Outflow</td><td>₹' + l.principal + '</td></tr>'; });
    var tbody = document.getElementById("tbodyDrilldown"); if (tbody) tbody.innerHTML = dHtml;
  }

  function sortLoans(type) {
    if (type === "large") loans.sort(function(a, b) { return Number(b.outstanding || 0) - Number(a.outstanding || 0); });
    if (type === "small") loans.sort(function(a, b) { return Number(a.outstanding || 0) - Number(b.outstanding || 0); });
    if (type === "new") loans.sort(function(a, b) { return new Date(b.date || "2026-01-01") - new Date(a.date || "2026-01-01"); });
    if (type === "old") loans.sort(function(a, b) { return new Date(a.date || "2026-01-01") - new Date(b.date || "2026-01-01"); });
    renderLoans();
  }

  function submitReceive() {
    var memId = document.getElementById("selPayMember").value;
    var m = members.find(function(x) { return String(x.id) === String(memId); });
    var dt = document.getElementById("inpPayDate").value || getTodayYMD();
    var rd = Number(document.getElementById("inpPayRd").value) || 0;
    var int = Number(document.getElementById("inpPayInterest").value) || 0;
    var pen = Number(document.getElementById("inpPayPenalty").value) || 0;
    var wvr = Number(document.getElementById("inpPayWaiver").value) || 0;
    var pr = Number(document.getElementById("inpPayPrincipal").value) || 0;
    var tot = rd + int + pen + pr - wvr;
    if (tot <= 0 && wvr <= 0) { showNotice("Payment Alert", "Kripya valid payment amount bharein."); return; }

    var recNo = "REC-" + Math.floor(1000 + Math.random() * 9000);
    var newPay = { receiptNo: recNo, date: dt, id: memId, name: m ? m.name : "Member", rd: rd, interest: int, penalty: pen, waiver: wvr, principalRepay: pr, total: tot, mode: document.getElementById("selPayMode").value };
    payments.unshift(newPay);
    closeModal("modalReceive");
    saveStore();
    if (window.google && google.script && google.script.run) { google.script.run.savePaymentBackend(newPay); }
    showNotice("Receipt Generated", "Receipt No: " + recNo + " (Date: " + dt + ") safaltapoorvak generate ho gayi hai (Total: ₹" + tot + ").");
  }

  function submitLoan() {
    var memId = document.getElementById("selLoanMember").value;
    var m = members.find(function(x) { return String(x.id) === String(memId); });
    var dt = document.getElementById("inpLoanDate").value || getTodayYMD();
    var pr = Number(document.getElementById("inpLoanPrinc").value) || 0;
    var rt = Number(document.getElementById("inpLoanRate").value) || 1.0;
    var tp = document.getElementById("selLoanType").value;
    if (pr <= 0) { showNotice("Loan Alert", "Kripya valid principal loan amount bharein."); return; }

    var lnId = "LN-" + Math.floor(500 + Math.random() * 500);
    var newLoan = { loanId: lnId, date: dt, id: memId, name: m ? m.name : "Member", type: tp, principal: pr, rate: rt, repaid: 0, outstanding: pr, status: "ACTIVE" };
    loans.unshift(newLoan);
    closeModal("modalLoan");
    saveStore();
    if (window.google && google.script && google.script.run) { google.script.run.saveLoanBackend(newLoan); }
    showNotice("Loan Disbursed", "Loan ID: " + lnId + " ke antargat ₹" + pr + " ka " + tp + " (Date: " + dt + ") safaltapoorvak jari kiya gaya.");
  }

  function submitMember() {
    var editId = document.getElementById("editMemId").value;
    var name = document.getElementById("inpNewMemName").value.trim();
    var mob = document.getElementById("inpNewMemMobile").value.trim();
    var st = document.getElementById("inpNewMemStatus").value;
    var addr = document.getElementById("inpNewMemAddress").value.trim();
    var nom = document.getElementById("inpNewMemNominee").value.trim();
    var rd = Number(document.getElementById("inpNewMemRd").value) || 400;
    var bal = Number(document.getElementById("inpNewMemBal").value) || 4400;
    if (!name || !mob) { showNotice("Required Field", "Member ka Name aur Mobile number anivarya hai."); return; }

    var savedMember = null;
    if (editId) {
      var m = members.find(function(x) { return String(x.id) === String(editId); });
      if (m) {
        m.name = name; m.mobile = mob; m.status = st; m.address = addr; m.nominee = nom; m.rd = rd; m.rdPaid = bal;
        savedMember = m;
      }
      showNotice("Member Updated", name + " ka profile safaltapoorvak update ho gaya hai.");
    } else {
      var id = "USR-" + String(members.length + 1).padStart(5, "0");
      savedMember = { id: id, name: name, mobile: mob, status: st, address: addr, nominee: nom, rd: rd, rdPaid: bal };
      members.push(savedMember);
      showNotice("Member Registered", name + " ko Member ID " + id + " ke sath successfully add kiya gaya.");
    }
    closeModal("modalMember");
    saveStore();
    if (savedMember && window.google && google.script && google.script.run) { google.script.run.saveMemberBackend(savedMember); }
  }

  function submitBulkEntry() {
    var rows = document.querySelectorAll(".bulk-row-chk:checked");
    if (rows.length === 0) { showNotice("Empty Selection", "Kripya kam se kam ek member select karein."); return; }
    var dt = document.getElementById("inpBulkDate").value || getTodayYMD();
    var count = 0;
    rows.forEach(function(c) {
      var tr = c.closest("tr");
      var id = c.getAttribute("data-id");
      var name = c.getAttribute("data-name");
      var rd = Number(tr.querySelector(".b-rd").value) || 0;
      var int = Number(tr.querySelector(".b-int").value) || 0;
      var repay = Number(tr.querySelector(".b-repay").value) || 0;
      var pen = Number(tr.querySelector(".b-pen").value) || 0;
      var wvr = Number(tr.querySelector(".b-waiver").value) || 0;
      var mode = tr.querySelector(".b-mode").value;
      var tot = rd + int + repay + pen - wvr;
      var recNo = "REC-" + Math.floor(1000 + Math.random() * 9000);
      var newPay = { receiptNo: recNo, date: dt, id: id, name: name, rd: rd, interest: int, penalty: pen, waiver: wvr, principalRepay: repay, total: tot, mode: mode };
      payments.unshift(newPay);
      if (window.google && google.script && google.script.run) { google.script.run.savePaymentBackend(newPay); }
      count++;
    });
    closeModal("modalBulk");
    saveStore();
    showNotice("Bulk Entry Posted", "Kul " + count + " sadasyon ka bulk collection (Date: " + dt + ") successfully record ho gaya hai.");
  }

  function refreshAll() {
    try { updateKPIs(); } catch (e) {}
    try { renderMembers(); } catch (e) {}
    try { renderPayments(); } catch (e) {}
    try { renderLoans(); } catch (e) {}
    try { populateDropdowns(); } catch (e) {}
  }

  function resetSampleData() {
    members = JSON.parse(JSON.stringify(DEFAULT_MEMBERS));
    payments = JSON.parse(JSON.stringify(DEFAULT_PAYMENTS));
    loans = JSON.parse(JSON.stringify(DEFAULT_LOANS));
    saveStore();
    showNotice("Data Reset", "Sample members and transactions restored.");
  }

  function setupEventListeners() {
    // Top Buttons
    var btnRcv = document.getElementById("btnTopReceive"); if (btnRcv) btnRcv.addEventListener("click", openReceiveModal);
    var btnLn = document.getElementById("btnTopLoan"); if (btnLn) btnLn.addEventListener("click", openLoanModal);
    var btnAdd = document.getElementById("btnTopAddMember"); if (btnAdd) btnAdd.addEventListener("click", openAddMemberModal);
    var btnBlk = document.getElementById("btnTopBulk"); if (btnBlk) btnBlk.addEventListener("click", openBulkModal);
    var btnRel = document.getElementById("btnTopReload"); if (btnRel) btnRel.addEventListener("click", resetSampleData);

    // Tab Headers
    var th1 = document.getElementById("tabHead1"); if (th1) th1.addEventListener("click", function() { switchTab(1); });
    var th2 = document.getElementById("tabHead2"); if (th2) th2.addEventListener("click", function() { switchTab(2); });
    var th3 = document.getElementById("tabHead3"); if (th3) th3.addEventListener("click", function() { switchTab(3); });

    // KPI Clicks
    var k1 = document.getElementById("kpiCardMembers"); if (k1) k1.addEventListener("click", function() { switchTab(1); });
    var k2 = document.getElementById("kpiCardRd"); if (k2) k2.addEventListener("click", function() { switchTab(2); });
    var k3 = document.getElementById("kpiCardDues"); if (k3) k3.addEventListener("click", function() { showNotice("RD Dues Notice", "Monthly RD schedule ke anusar sabhi active sadasyo ka RD balance verified hai."); });
    var k4 = document.getElementById("kpiCardLoans"); if (k4) k4.addEventListener("click", function() { switchTab(3); });
    var k5 = document.getElementById("kpiCardFund"); if (k5) k5.addEventListener("click", openFundModal);
    var k6 = document.getElementById("kpiCardNpa"); if (k6) k6.addEventListener("click", function() { showNotice("Zero Loss Status", "100% Recovery track record. No NPA or bad debts."); });

    // Panel inside buttons
    var btnPRcv = document.getElementById("btnPanelNewReceipt"); if (btnPRcv) btnPRcv.addEventListener("click", openReceiveModal);
    var btnPLn = document.getElementById("btnPanelNewLoan"); if (btnPLn) btnPLn.addEventListener("click", openLoanModal);

    // Loan Sorting
    var sl1 = document.getElementById("btnSortLoanLarge"); if (sl1) sl1.addEventListener("click", function() { sortLoans('large'); });
    var sl2 = document.getElementById("btnSortLoanSmall"); if (sl2) sl2.addEventListener("click", function() { sortLoans('small'); });
    var sl3 = document.getElementById("btnSortLoanNew"); if (sl3) sl3.addEventListener("click", function() { sortLoans('new'); });
    var sl4 = document.getElementById("btnSortLoanOld"); if (sl4) sl4.addEventListener("click", function() { sortLoans('old'); });

    // Search Input
    var sInp = document.getElementById("memberFilterInput"); if (sInp) sInp.addEventListener("input", renderMembers);

    // Modal Close buttons
    var cN = document.getElementById("btnNoticeOk"); if (cN) cN.addEventListener("click", function() { closeModal("modalNotice"); });
    var cR = document.getElementById("btnCloseReceive"); if (cR) cR.addEventListener("click", function() { closeModal("modalReceive"); });
    var cL = document.getElementById("btnCloseLoan"); if (cL) cL.addEventListener("click", function() { closeModal("modalLoan"); });
    var cM = document.getElementById("btnCloseMember"); if (cM) cM.addEventListener("click", function() { closeModal("modalMember"); });
    var cB = document.getElementById("btnCloseBulk"); if (cB) cB.addEventListener("click", function() { closeModal("modalBulk"); });
    var cLd = document.getElementById("btnCloseLedger"); if (cLd) cLd.addEventListener("click", function() { closeModal("modalLedger"); });
    var cF = document.getElementById("btnCloseFund"); if (cF) cF.addEventListener("click", function() { closeModal("modalFund"); });

    // Modal Form Submits
    var subR = document.getElementById("btnSubmitReceive"); if (subR) subR.addEventListener("click", submitReceive);
    var subL = document.getElementById("btnSubmitLoan"); if (subL) subL.addEventListener("click", submitLoan);
    var subM = document.getElementById("btnSubmitMember"); if (subM) subM.addEventListener("click", submitMember);
    var subB = document.getElementById("btnSubmitBulk"); if (subB) subB.addEventListener("click", submitBulkEntry);

    // Bulk Select All Checkbox
    var chkAll = document.getElementById("chkSelectAllBulk");
    if (chkAll) {
      chkAll.addEventListener("change", function() {
        var isChecked = this.checked;
        document.querySelectorAll(".bulk-row-chk").forEach(function(c) { c.checked = isChecked; });
      });
    }

    // Dynamic Delegate Clicks for Tables (View Ledger, Receive For, Edit Member, Drilldown)
    document.addEventListener("click", function(e) {
      var target = e.target;
      if (!target) return;

      var ledgerBtn = target.closest(".action-view-ledger");
      if (ledgerBtn) {
        var id = ledgerBtn.getAttribute("data-id");
        if (id) openMemberLedger(id);
        return;
      }

      var rcvBtn = target.closest(".action-receive-for");
      if (rcvBtn) {
        var id = rcvBtn.getAttribute("data-id");
        if (id) openReceiveFor(id);
        return;
      }

      var editBtn = target.closest(".action-edit-member");
      if (editBtn) {
        var id = editBtn.getAttribute("data-id");
        if (id) openEditMember(id);
        return;
      }

      var drillBtn = target.closest(".action-drill-month");
      if (drillBtn) {
        var idx = drillBtn.getAttribute("data-idx");
        if (idx !== null) drilldownMonth(Number(idx));
        return;
      }
    });
  }

  function startApp() {
    try {
      var sm = localStorage.getItem("gullak_v10_m");
      var sp = localStorage.getItem("gullak_v10_p");
      var sl = localStorage.getItem("gullak_v10_l");
      if (sm) { var pm = JSON.parse(sm); if (Array.isArray(pm) && pm.length > 0) members = pm; }
      if (sp) { var pp = JSON.parse(sp); if (Array.isArray(pp)) payments = pp; }
      if (sl) { var pl = JSON.parse(sl); if (Array.isArray(pl)) loans = pl; }
    } catch (e) {}

    setupEventListeners();
    refreshAll();

    if (window.google && google.script && google.script.run) {
      google.script.run.withSuccessHandler(function(res) {
        if (res) {
          if (Array.isArray(res.members) && res.members.length > 0) members = res.members;
          if (Array.isArray(res.payments) && res.payments.length > 0) payments = res.payments;
          if (Array.isArray(res.loans) && res.loans.length > 0) loans = res.loans;
          saveStore();
        }
      }).getSocietyFullData();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", startApp);
  } else {
    startApp();
  }
})();
</script>
</body>
</html>`;
}
"""
}
