import os
import zipfile

# PART 1: SERVER SCRIPT
part1_server = '''/**
 * 🏦 GULLAK CO-OPERATIVE SOCIETY - BACKEND CONTROLLER (V21 PRO MASTER)
 * Standardized Sheets + Auto-Cleanup + Strict ID Formats + Sheet Protection ('Password') + Users Auth
 */

function onOpen() {
  SpreadsheetApp.getUi().createMenu("🏦 Gullak Co-operative")
    .addItem("⚡ 1. Initialize & Organize Sheet Database", "installAndRunDatabase")
    .addItem("🌐 2. Get Live Web App URL", "showWebPortalUrl")
    .addToUi();
}

function showWebPortalUrl() {
  try {
    var url = ScriptApp.getService().getUrl();
    if (!url) {
      SpreadsheetApp.getUi().alert("Please deploy Web App first:\\nDeploy > New deployment > Web app > Anyone");
    } else {
      SpreadsheetApp.getUi().alert("🌐 Your Live Web Portal URL:\\n\\n" + url);
    }
  } catch (e) {
    SpreadsheetApp.getUi().alert("Deploy Web App from Deploy > New deployment.");
  }
}

function installAndRunDatabase() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return;

  // Clean unusable sheets
  var unwanted = ["Sheet1", "Dashboard", "Fin"];
  unwanted.forEach(function(sName) {
    var s = ss.getSheetByName(sName);
    if (s && ss.getSheets().length > 1) {
      try { ss.deleteSheet(s); } catch (e) {}
    }
  });

  // Users Sheet for Multi-User Login & Roles
  var userH = ["Username", "Password", "Role", "Email", "Status", "CreatedAt"];
  var userSheet = getOrCreateSheet(ss, "Users", userH, "#0F172A");
  if (userSheet.getLastRow() <= 1) {
    userSheet.appendRow(["SANISH", "Password", "Super Admin", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
    userSheet.appendRow(["ADMIN", "Admin@123", "Manager", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
  }

  // Exactly 14 Columns Header (Fixed Order)
  var memH = ["Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", "RD / Month (₹)", "Status", "Date Joined", "Opening RD (₹)", "Due Day", "Custom Loan Limit (₹)", "Opening Loan (₹)", "Opening Int (₹)", "Opening Pen (₹)"];
  var memSheet = getOrCreateSheet(ss, "Members", memH, "#1E293B");

  // Exactly 14 Columns Header
  var payH = ["Receipt No", "Date", "Member ID", "Name", "RD Amount (₹)", "Interest (₹)", "Penalty (₹)", "Loan Repayment (₹)", "Waiver (₹)", "Total (₹)", "Mode", "Recorded By", "Type", "Narration"];
  var paySheet = getOrCreateSheet(ss, "Payments", payH, "#0F766E");

  var loanH = ["Loan ID", "Date", "Member ID", "Name", "Type", "Principal (₹)", "Rate (%)", "Repaid (₹)", "Outstanding (₹)", "Status", "Narration"];
  var loanSheet = getOrCreateSheet(ss, "Loans", loanH, "#991B1B");

  var exitH = ["Exit ID", "Date", "Member ID", "Name", "Total RD (₹)", "Loan Dues (₹)", "Bonus Adj (₹)", "NPA Loss (₹)", "Waiver (₹)", "Net Settlement (₹)", "Status"];
  getOrCreateSheet(ss, "ExitSettlements", exitH, "#7F1D1D");

  var bonusH = ["Settlement ID", "Date", "Member ID", "Name", "Total Bonus (₹)", "Adj Loan (₹)", "Adj Interest (₹)", "Adj RD (₹)", "Adj Penalty (₹)", "Net Paid (₹)", "Mode"];
  getOrCreateSheet(ss, "BonusSettlements", bonusH, "#D97706");

  // Format and align all sheets (Numbers center, text left)
  alignAndFormatSheet(memSheet, [0, 2, 5, 6, 7, 8, 9, 10, 11, 12, 13]);
  alignAndFormatSheet(paySheet, [0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12]);
  alignAndFormatSheet(loanSheet, [0, 1, 2, 5, 6, 7, 8, 9]);

  try {
    ss.getSheets().forEach(function(s) {
      var protections = s.getProtections(SpreadsheetApp.ProtectionType.SHEET);
      if (protections.length === 0) {
        var p = s.protect().setDescription("Gullak Master Protected (Password: Password)");
        p.setWarningOnly(true);
      }
    });
  } catch (e) {}

  if (memSheet.getLastRow() <= 1) {
    var sampleM = [
      ["MEM010120261", "Rahul Kumar", "9810011111", "H-12, Sector 3, Rohini", "Sunita Kumar (Wife)", 400, "ACTIVE", "2026-01-01", 4800, "15th of every month", 0, 0, 0, 0],
      ["MEM010120262", "Suresh Sharma", "9810022222", "Shop 4, Main Market", "Vikas Sharma (Son)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120263", "Amit Verma", "9810033333", "B-45, Shastri Nagar", "Pooja Verma (Wife)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120264", "SANISH", "9718174244", "ASD", "DFFF", 400, "ACTIVE", "2026-01-01", 1000, "15th of every month", 0, 0, 0, 0]
    ];
    memSheet.getRange(2, 1, sampleM.length, 14).setValues(sampleM);
  }
  SpreadsheetApp.flush();
}

function alignAndFormatSheet(sheet, centerCols) {
  if (!sheet || sheet.getLastRow() === 0) return;
  var lr = Math.max(sheet.getLastRow(), 2);
  var lc = sheet.getLastColumn();
  sheet.getRange(1, 1, lr, lc).setFontFamily("Segoe UI").setFontSize(10).setVerticalAlignment("middle");
  sheet.getRange(1, 1, lr, lc).setHorizontalAlignment("left");
  centerCols.forEach(function(cIdx) {
    if (cIdx + 1 <= lc) {
      sheet.getRange(2, cIdx + 1, lr - 1, 1).setHorizontalAlignment("center");
    }
  });
}

function getOrCreateSheet(ss, name, headers, color) {
  var s = ss.getSheetByName(name);
  if (!s) s = ss.insertSheet(name);
  if (headers) {
    s.getRange(1, 1, 1, headers.length).setValues([headers]);
    s.getRange(1, 1, 1, headers.length).setBackground(color).setFontColor("#FFF").setFontWeight("bold").setHorizontalAlignment("center");
    s.setFrozenRows(1);
  }
  return s;
}

function formatPureDate(d) {
  if (!d) return "2026-01-01";
  if (typeof d === "string") {
    if (d.indexOf("T") > 0) return d.split("T")[0];
    if (d.indexOf("GMT") > 0 || d.indexOf(":") > 0) {
      var p = new Date(d);
      if (!isNaN(p.getTime())) return p.getFullYear() + "-" + String(p.getMonth() + 1).padStart(2, "0") + "-" + String(p.getDate()).padStart(2, "0");
    }
    return d.split(" ")[0];
  }
  if (d instanceof Date) return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0");
  return String(d);
}

function getSocietyFullData() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return getDefaultDataFallback();
    var memSheet = ss.getSheetByName("Members");
    if (!memSheet || memSheet.getLastRow() <= 1) { installAndRunDatabase(); memSheet = ss.getSheetByName("Members"); }

    var members = [];
    if (memSheet && memSheet.getLastRow() > 1) {
      var mData = memSheet.getRange(2, 1, memSheet.getLastRow() - 1, 14).getValues();
      mData.forEach(function(r) {
        if (r[0] || r[1]) {
          var safeRd = Math.round(Number(r[5])) || 400;
          if (safeRd <= 0 || safeRd > 50000) safeRd = 400;
          var safeOpRd = Math.round(Number(r[8])) || 0;
          members.push({
            id: String(r[0]),
            name: String(r[1]),
            mobile: String(r[2]),
            address: String(r[3]||""),
            nominee: String(r[4]||""),
            rd: safeRd,
            status: String(r[6]||"ACTIVE").toUpperCase(),
            dateJoined: formatPureDate(r[7]||"2026-01-01"),
            rdPaid: safeOpRd,
            dueDay: String(r[9]||"15th of every month"),
            customLimit: Math.round(Number(r[10]))||0,
            opLoan: Math.round(Number(r[11]))||0,
            opInt: Math.round(Number(r[12]))||0,
            opPen: Math.round(Number(r[13]))||0
          });
        }
      });
    }

    var payments = [];
    var paySheet = ss.getSheetByName("Payments");
    if (paySheet && paySheet.getLastRow() > 1) {
      var pData = paySheet.getRange(2, 1, paySheet.getLastRow() - 1, 14).getValues();
      pData.forEach(function(p) {
        if (p[0] || p[2]) {
          var rawMode = String(p[10] || "CASH").toUpperCase().trim();
          var cleanMode = (rawMode.indexOf("ONLINE") >= 0 || rawMode.indexOf("UPI") >= 0 || rawMode.indexOf("BANK") >= 0) ? "ONLINE" : "CASH";
          payments.push({
            receiptNo: String(p[0]),
            date: formatPureDate(p[1]),
            id: String(p[2]),
            name: String(p[3]),
            rd: Math.round(Number(p[4]))||0,
            interest: Math.round(Number(p[5]))||0,
            penalty: Math.round(Number(p[6]))||0,
            loanRepay: Math.round(Number(p[7]))||0,
            waiver: Math.round(Number(p[8]))||0,
            total: Math.round(Number(p[9]))||0,
            mode: cleanMode,
            by: String(p[11]||"Admin"),
            type: String(p[12]||"REGULAR"),
            narration: String(p[13]||"")
          });
        }
      });
    }

    var loans = [];
    var loanSheet = ss.getSheetByName("Loans");
    if (loanSheet && loanSheet.getLastRow() > 1) {
      var lData = loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, Math.min(11, loanSheet.getLastColumn())).getValues();
      lData.forEach(function(l) {
        if (l[0] || l[2]) loans.push({
          loanId: String(l[0]),
          date: formatPureDate(l[1]),
          id: String(l[2]),
          name: String(l[3]),
          type: String(l[4]||"Gullak Loan"),
          principal: Math.round(Number(l[5]))||0,
          rate: Number(l[6])||1.0,
          repaid: Math.round(Number(l[7]))||0,
          outstanding: Math.round(Number(l[8]))||0,
          status: String(l[9]||"ACTIVE"),
          narration: String(l[10]||"")
        });
      });
    }

    var exitSettlements = [];
    var exitSheet = ss.getSheetByName("ExitSettlements");
    if (exitSheet && exitSheet.getLastRow() > 1) {
      var exData = exitSheet.getRange(2, 1, exitSheet.getLastRow() - 1, 11).getValues();
      exData.forEach(function(e) {
        if (e[0]) exitSettlements.push({
          exitId: String(e[0]),
          date: formatPureDate(e[1]),
          id: String(e[2]),
          name: String(e[3]),
          totalRd: Math.round(Number(e[4]))||0,
          loanDues: Math.round(Number(e[5]))||0,
          bonusAdj: Math.round(Number(e[6]))||0,
          npaLoss: Math.round(Number(e[7]))||0,
          waiver: Math.round(Number(e[8]))||0,
          netSettlement: Math.round(Number(e[9]))||0,
          status: String(e[10]||"INACTIVE")
        });
      });
    }

    var bonusSettlements = [];
    var bonusSheet = ss.getSheetByName("BonusSettlements");
    if (bonusSheet && bonusSheet.getLastRow() > 1) {
      var bData = bonusSheet.getRange(2, 1, bonusSheet.getLastRow() - 1, 11).getValues();
      bData.forEach(function(b) {
        if (b[0]) {
          var bMode = String(b[10] || "ONLINE").toUpperCase().indexOf("CASH") >= 0 ? "CASH" : "ONLINE";
          bonusSettlements.push({
            settlementId: String(b[0]),
            date: formatPureDate(b[1]),
            id: String(b[2]),
            name: String(b[3]),
            totalBonus: Math.round(Number(b[4]))||0,
            adjLoan: Math.round(Number(b[5]))||0,
            adjInterest: Math.round(Number(b[6]))||0,
            adjRd: Math.round(Number(b[7]))||0,
            adjPenalty: Math.round(Number(b[8]))||0,
            netPaid: Math.round(Number(b[9]))||0,
            mode: bMode
          });
        }
      });
    }

    var users = [];
    var userSheet = ss.getSheetByName("Users");
    if (userSheet && userSheet.getLastRow() > 1) {
      var uData = userSheet.getRange(2, 1, userSheet.getLastRow() - 1, 6).getValues();
      uData.forEach(function(u) {
        if (u[0]) {
          users.push({
            username: String(u[0]),
            password: String(u[1]),
            role: String(u[2] || "Manager"),
            email: String(u[3] || ""),
            status: String(u[4] || "ACTIVE")
          });
        }
      });
    }

    if (members.length === 0) return getDefaultDataFallback();
    return { members: members, payments: payments, loans: loans, exitSettlements: exitSettlements, bonusSettlements: bonusSettlements, users: users };
  } catch (e) {
    return getDefaultDataFallback();
  }
}

function getDefaultDataFallback() {
  return {
    members: [
      { id: "MEM010120261", name: "Rahul Kumar", mobile: "9810011111", status: "ACTIVE", address: "H-12, Sector 3, Rohini", nominee: "Sunita Kumar", rd: 400, dateJoined: "2026-01-01", rdPaid: 4800, dueDay: "15th of every month", customLimit: 0, opLoan: 0, opInt: 0, opPen: 0 },
      { id: "MEM010120262", name: "Suresh Sharma", mobile: "9810022222", status: "ACTIVE", address: "Shop 4, Market", nominee: "Vikas", rd: 400, dateJoined: "2026-01-01", rdPaid: 4400, dueDay: "15th of every month", customLimit: 0, opLoan: 0, opInt: 0, opPen: 0 },
      { id: "MEM010120263", name: "Amit Verma", mobile: "9810033333", status: "ACTIVE", address: "B-45, Shastri Nagar", nominee: "Pooja", rd: 400, dateJoined: "2026-01-01", rdPaid: 4400, dueDay: "15th of every month", customLimit: 0, opLoan: 0, opInt: 0, opPen: 0 },
      { id: "MEM010120264", name: "SANISH", mobile: "9718174244", status: "ACTIVE", address: "ASD", nominee: "DFFF", rd: 400, dateJoined: "2026-01-01", rdPaid: 1000, dueDay: "15th of every month", customLimit: 0, opLoan: 0, opInt: 0, opPen: 0 }
    ],
    payments: [],
    loans: [],
    exitSettlements: [],
    bonusSettlements: [],
    users: [
      { username: "SANISH", password: "Password", role: "Super Admin", email: "stfsolutionsdelhi@gmail.com" },
      { username: "ADMIN", password: "Admin@123", role: "Manager", email: "stfsolutionsdelhi@gmail.com" }
    ]
  };
}

function saveMemberBackend(m) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Members"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Members"); }
    var lastRow = sheet.getLastRow(); var updated = false;
    var safeRd = Math.round(Number(m.rd)) || 400;
    var safeOpRd = Math.round(Number(m.rdPaid)) || 0;

    var rowVals = [
      m.id,
      m.name,
      m.mobile,
      m.address||"",
      m.nominee||"",
      safeRd,
      m.status||"ACTIVE",
      formatPureDate(m.dateJoined||"2026-01-01"),
      safeOpRd,
      m.dueDay||"15th of every month",
      Math.round(Number(m.customLimit))||0,
      Math.round(Number(m.opLoan))||0,
      Math.round(Number(m.opInt))||0,
      Math.round(Number(m.opPen))||0
    ];

    if (lastRow > 1) {
      var ids = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
      for (var i = 0; i < ids.length; i++) {
        if (String(ids[i][0]) === String(m.id)) {
          sheet.getRange(i + 2, 1, 1, 14).setValues([rowVals]);
          updated = true; break;
        }
      }
    }
    if (!updated) sheet.appendRow(rowVals);
    SpreadsheetApp.flush();
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function savePaymentBackend(p) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Payments"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Payments"); }
    var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
    
    var lastRow = sheet.getLastRow();
    var updated = false;
    if (lastRow > 1) {
      var recNos = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
      for (var i = 0; i < recNos.length; i++) {
        if (String(recNos[i][0]) === String(p.receiptNo)) {
          sheet.getRange(i + 2, 1, 1, 14).setValues([[
            p.receiptNo, formatPureDate(p.date), p.id, p.name,
            Math.round(Number(p.rd))||0, Math.round(Number(p.interest))||0,
            Math.round(Number(p.penalty))||0, Math.round(Number(p.loanRepay))||0,
            Math.round(Number(p.waiver))||0, Math.round(Number(p.total))||0,
            safeMode, "Admin", p.type||"REGULAR", p.narration||""
          ]]);
          updated = true; break;
        }
      }
    }

    if (!updated) {
      sheet.appendRow([
        p.receiptNo, formatPureDate(p.date), p.id, p.name,
        Math.round(Number(p.rd))||0, Math.round(Number(p.interest))||0,
        Math.round(Number(p.penalty))||0, Math.round(Number(p.loanRepay))||0,
        Math.round(Number(p.waiver))||0, Math.round(Number(p.total))||0,
        safeMode, "Admin", p.type||"REGULAR", p.narration||""
      ]);
    }
    SpreadsheetApp.flush();
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function saveLoanBackend(l) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Loans"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Loans"); }
    
    var lastRow = sheet.getLastRow();
    var updated = false;
    if (lastRow > 1) {
      var lIds = sheet.getRange(2, 1, lastRow - 1, 1).getValues();
      for (var i = 0; i < lIds.length; i++) {
        if (String(lIds[i][0]) === String(l.loanId)) {
          sheet.getRange(i + 2, 1, 1, 11).setValues([[
            l.loanId, formatPureDate(l.date), l.id, l.name, l.type||"Gullak Loan",
            Math.round(Number(l.principal))||0, Number(l.rate)||1.0,
            Math.round(Number(l.repaid))||0, Math.round(Number(l.outstanding))||0, l.status||"ACTIVE", l.narration||""
          ]]);
          updated = true; break;
        }
      }
    }

    if (!updated) {
      sheet.appendRow([l.loanId, formatPureDate(l.date), l.id, l.name, l.type||"Gullak Loan", Math.round(Number(l.principal))||0, Number(l.rate)||1.0, Math.round(Number(l.repaid))||0, Math.round(Number(l.outstanding))||l.principal, l.status||"ACTIVE", l.narration||""]);
    }
    SpreadsheetApp.flush();
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function saveExitSettlementBackend(ex) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("ExitSettlements"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("ExitSettlements"); }
    sheet.appendRow([ex.exitId, formatPureDate(ex.date), ex.id, ex.name, Math.round(Number(ex.totalRd))||0, Math.round(Number(ex.loanDues))||0, Math.round(Number(ex.bonusAdj))||0, Math.round(Number(ex.npaLoss))||0, Math.round(Number(ex.waiver))||0, Math.round(Number(ex.netSettlement))||0, "INACTIVE"]);
    SpreadsheetApp.flush();
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function saveBonusSettlementBackend(b) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("BonusSettlements"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("BonusSettlements"); }
    var safeMode = String(b.mode||"ONLINE").toUpperCase().indexOf("CASH") >= 0 ? "CASH" : "ONLINE";
    sheet.appendRow([b.settlementId, formatPureDate(b.date), b.id, b.name, Math.round(Number(b.totalBonus))||0, Math.round(Number(b.adjLoan))||0, Math.round(Number(b.adjInterest))||0, Math.round(Number(b.adjRd))||0, Math.round(Number(b.adjPenalty))||0, Math.round(Number(b.netPaid))||0, safeMode]);
    SpreadsheetApp.flush();
    return { success: true };
  } catch (e) { return { success: false, error: e.toString() }; }
}

function doGet(e) {
  return HtmlService.createHtmlOutput(getCompleteSoftwareHtml())
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Accounting Platform (V21 PRO)")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}
'''

with open("Part1_Server.gs", "w", encoding="utf-8") as f:
    f.write(part1_server)
print("Part1_Server.gs created successfully.")
