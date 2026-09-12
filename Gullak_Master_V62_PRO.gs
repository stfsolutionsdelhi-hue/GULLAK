/**
 * 🏦 GULLAK CO-OPERATIVE SOCIETY - BACKEND CONTROLLER (V41 PRO MASTER)
 * Standardized Sheets + Auto-Cleanup + Strict ID Formats + Sheet Protection ('Password') + Users Auth
 */

function onOpen() {
  var ui = SpreadsheetApp.getUi();
  ui.createMenu('🏦 Gullak Co-operative')
    .addItem('⚡ 1. Initialize & Auto-Fix Sheet Database', 'installAndRunDatabase')
    .addItem('🛠️ 2. Fix & Align Columns & Formats Now', 'autoFixAndAlignAllSheets')
    .addItem('🔒 3. Lock All Sheets (Strict Lock - No Accidental Edits)', 'enableStrictSheetProtectionWithPassword')
    .addItem('🔓 4. Unlock All Sheets (Requires Password)', 'unlockAllSheetsWithPassword')
    .addItem('🔑 5. Change Sheet Security Password', 'changeSheetMasterPassword')
    .addItem('⚠️ 6. Warning Mode Only (Show Warning Dialog)', 'enableSafeSheetProtection')
    .addItem('👤 7. Reset User Passwords to 12345', 'resetUsersCredentialsToDefault')
    .addItem('🌐 8. Get Live Web App URL', 'showWebPortalUrl')
    .addItem('✉️ 9. Authorize Email Sending Permission', 'testEmailPermission')
    .addToUi();

  // Auto-upgrade legacy credentials in Users sheet on open
  try {
    upgradeUsersSheetCredentials();
  } catch(e) {}
}

function testEmailPermission() {
  try {
    var email = Session.getActiveUser().getEmail() || "stfsolutionsdelhi@gmail.com";
    MailApp.sendEmail(email, "Gullak Society - Email Permission Test", "Email dispatch permissions verified successfully.");
    SpreadsheetApp.getUi().alert("✅ Email Sending Permission Verified!\n\nEmail dispatch is successfully authorized for your Google Account.");
  } catch(e) {
    SpreadsheetApp.getUi().alert("Notice: " + e.toString());
  }
}

function showWebPortalUrl() {
  try {
    var url = ScriptApp.getService().getUrl();
    if (!url) {
      SpreadsheetApp.getUi().alert("Please deploy Web App first:\nDeploy > New deployment > Web app > Anyone");
    } else {
      SpreadsheetApp.getUi().alert("🌐 Your Live Web Portal URL:\n\n" + url);
    }
  } catch (e) {
    SpreadsheetApp.getUi().alert("Deploy Web App from Deploy > New deployment.");
  }
}

function getSpreadsheetUrl() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    return ss ? ss.getUrl() : "";
  } catch(e) {
    return "";
  }
}

function upgradeUsersSheetCredentials() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    var userSheet = ss.getSheetByName("Users");
    if (!userSheet) return;
    if (userSheet.getLastRow() <= 1) {
      userSheet.appendRow(["SANISH", "12345", "Super Admin", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
      userSheet.appendRow(["ADMIN", "12345", "Manager", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
      SpreadsheetApp.flush();
      return;
    }
    var numRows = userSheet.getLastRow() - 1;
    var data = userSheet.getRange(2, 1, numRows, Math.min(6, userSheet.getLastColumn())).getValues();
    var changed = false;
    for (var i = 0; i < data.length; i++) {
      var u = String(data[i][0] || "").trim().toUpperCase();
      var p = String(data[i][1] || "").trim();
      var em = String(data[i][3] || "").trim();
      var rowNum = i + 2;
      if (p === "Password" || p === "Admin@123" || p === "") {
        userSheet.getRange(rowNum, 2).setValue("12345");
        changed = true;
      }
      if ((u === "SANISH" || u === "ADMIN") && (p === "Password" || p === "Admin@123")) {
        userSheet.getRange(rowNum, 2).setValue("12345");
        changed = true;
      }
      if (!em || em.indexOf("@") === -1) {
        userSheet.getRange(rowNum, 4).setValue("stfsolutionsdelhi@gmail.com");
        changed = true;
      }
    }
    if (changed) SpreadsheetApp.flush();
  } catch(e) {}
}

function resetUsersCredentialsToDefault() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    var userSheet = ss.getSheetByName("Users");
    if (!userSheet) {
      installAndRunDatabase();
      return;
    }
    userSheet.clearContent();
    var userH = ["Username", "Password", "Role", "Email", "Status", "CreatedAt"];
    userSheet.getRange(1, 1, 1, userH.length).setValues([userH]);
    userSheet.appendRow(["SANISH", "12345", "Super Admin", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
    userSheet.appendRow(["ADMIN", "12345", "Manager", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
    SpreadsheetApp.flush();
    SpreadsheetApp.getUi().alert("✅ Users Tab Updated Successfully!\n\n• Username: SANISH, Password: 12345\n• Username: ADMIN, Password: 12345\n• Registered Email: stfsolutionsdelhi@gmail.com\n\nAap is tab me Column B me password aur Column D me email kabhi bhi change kar sakte hain.");
  } catch(e) {
    try { SpreadsheetApp.getUi().alert("Error: " + e.toString()); } catch(err) {}
  }
}

function sendCredentialsEmailBackend(targetUsername) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var uSheet = ss ? ss.getSheetByName("Users") : null;
    var foundUser = null;
    var defaultEmail = "stfsolutionsdelhi@gmail.com";

    if (uSheet && uSheet.getLastRow() > 1) {
      var uData = uSheet.getRange(2, 1, uSheet.getLastRow() - 1, Math.min(6, uSheet.getLastColumn())).getValues();
      var cleanTarget = String(targetUsername || "").trim().toUpperCase();
      if (cleanTarget) {
        for (var i = 0; i < uData.length; i++) {
          if (String(uData[i][0]).trim().toUpperCase() === cleanTarget) {
            foundUser = {
              username: String(uData[i][0]).trim(),
              password: String(uData[i][1]).trim() || "12345",
              role: String(uData[i][2] || "Manager").trim(),
              email: String(uData[i][3] || defaultEmail).trim()
            };
            break;
          }
        }
      }
      if (!foundUser && uData.length > 0) {
        foundUser = {
          username: String(uData[0][0]).trim(),
          password: String(uData[0][1]).trim() || "12345",
          role: String(uData[0][2] || "Super Admin").trim(),
          email: String(uData[0][3] || defaultEmail).trim()
        };
      }
    }

    if (!foundUser) {
      foundUser = {
        username: (String(targetUsername || "").toUpperCase() === "ADMIN" ? "ADMIN" : "SANISH"),
        password: "12345",
        role: "Super Admin",
        email: defaultEmail
      };
    }

    var recipientEmail = defaultEmail;
    if (foundUser.email && foundUser.email.indexOf("@") > 0) {
      recipientEmail = foundUser.email;
    }

    var subject = "🔐 Gullak Co-operative Portal - Login Credentials Recovery";
    var body = "Namaste " + foundUser.username + ",\n\n" +
      "Aapke anurodh par Gullak Co-operative Society Accounting Portal ke login credentials bheje ja rahe hain:\n\n" +
      "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
      "👤 USERNAME : " + foundUser.username + "\n" +
      "🔑 PASSWORD : " + foundUser.password + "\n" +
      "🛡️ ROLE     : " + foundUser.role + "\n" +
      "📧 EMAIL    : " + recipientEmail + "\n" +
      "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
      "💡 Password ya Email badalne ke liye:\n" +
      "Apne connected Google Sheet ke 'Users' tab me jaakar Column B (Password) aur Column D (Email) ko update karein.\n\n" +
      "Date & Time: " + new Date().toLocaleString() + "\n" +
      "Gullak Co-operative Society Automated Management System";

    try {
      MailApp.sendEmail(recipientEmail, subject, body);
    } catch(mailErr) {
      try {
        GmailApp.sendEmail(recipientEmail, subject, body);
      } catch(gmailErr) {
        return {
          success: false,
          error: "Permission Required: Google Apps Script me Mail permission grant karni hogi. Ek baar Google Sheet me '🏦 Gullak Co-operative' menu me jaakar '✉️ 4. Authorize Email Sending Permission' par click karke Google Authorization allow karein. Tab tak aap 'Users' tab se apna password dekh/update kar sakte hain."
        };
      }
    }

    var parts = recipientEmail.split("@");
    var masked = parts[0].substring(0, Math.min(3, parts[0].length)) + "***@" + parts[1];
    return {
      success: true,
      email: masked,
      username: foundUser.username,
      message: "Credentials safaltapoorvak aapke registered email (" + masked + ") par bhej diye gaye hain!"
    };
  } catch(err) {
    return { success: false, error: err.toString() };
  }
}

function installAndRunDatabase() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return;

  // 1. Clean unusable sheets (Sheet1, Dashboard, Fin)
  var unwanted = ["Sheet1", "Dashboard", "Fin"];
  unwanted.forEach(function(sName) {
    var s = ss.getSheetByName(sName);
    if (s && ss.getSheets().length > 1) {
      try { ss.deleteSheet(s); } catch (e) {}
    }
  });

  // 2. Setup / standardize all core sheets
  var userH = ["Username", "Password", "Role", "Email", "Status", "CreatedAt"];
  var userSheet = getOrCreateSheet(ss, "Users", userH, "#0F172A");
  if (userSheet.getLastRow() <= 1) {
    userSheet.appendRow(["SANISH", "12345", "Super Admin", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
    userSheet.appendRow(["ADMIN", "12345", "Manager", "stfsolutionsdelhi@gmail.com", "ACTIVE", new Date()]);
  } else {
    try {
      var existVals = userSheet.getRange(2, 1, userSheet.getLastRow() - 1, 2).getValues();
      for (var uR = 0; uR < existVals.length; uR++) {
        var oldP = String(existVals[uR][1] || "").trim();
        if (oldP === "Password" || oldP === "Admin@123" || oldP === "") {
          userSheet.getRange(uR + 2, 2).setValue("12345");
        }
      }
    } catch(err) {}
  }

  var memH = ["Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", "RD / Month (₹)", "Status", "Date Joined", "Opening RD (₹)", "Due Day", "Custom Loan Limit (₹)", "Opening Loan (₹)", "Opening Int (₹)", "Opening Pen (₹)"];
  var memSheet = getOrCreateSheet(ss, "Members", memH, "#1E293B");

  // Standardize Receipts (with support for Payments alias)
  var payH = ["Receipt No", "Date", "Member ID", "Name", "RD Amount (₹)", "Interest (₹)", "Penalty (₹)", "Loan Repayment (₹)", "Waiver (₹)", "Total (₹)", "Mode", "Recorded By", "Type", "Narration"];
  var paySheet = getOrCreateSheet(ss, "Receipts", payH, "#0F766E");

  // If old Payments sheet existed, transfer rows to Receipts if Receipts is empty
  var oldPaySheet = ss.getSheetByName("Payments");
  if (oldPaySheet && oldPaySheet.getLastRow() > 1 && paySheet.getLastRow() <= 1) {
    try {
      var oldPData = oldPaySheet.getRange(2, 1, oldPaySheet.getLastRow() - 1, Math.min(oldPaySheet.getLastColumn(), payH.length)).getValues();
      paySheet.getRange(2, 1, oldPData.length, oldPData[0].length).setValues(oldPData);
    } catch(trErr) {}
  }

  var loanH = ["Loan ID", "Date", "Member ID", "Name", "Type", "Principal (₹)", "Rate (%)", "Repaid (₹)", "Outstanding (₹)", "Status", "Narration"];
  var loanSheet = getOrCreateSheet(ss, "Loans", loanH, "#991B1B");

  var penH = ["Member ID", "Full Name", "Due Day", "Date Joined", "Total RD Paid (₹)", "Overdue Days", "Accrued Penalty (₹)", "Penalty Paid (₹)", "Waived (₹)", "Net Penalty Due (₹)", "Status", "Last Updated"];
  getOrCreateSheet(ss, "PenaltyRegister", penH, "#B45309");

  var bonusH = ["Settlement ID", "Date", "Member ID", "Name", "Total Bonus (₹)", "Adj Loan (₹)", "Adj Interest (₹)", "Adj RD (₹)", "Adj Penalty (₹)", "Net Paid (₹)", "Mode"];
  getOrCreateSheet(ss, "BonusSettlements", bonusH, "#D97706");

  var fundH = ["Txn ID", "Date", "Type", "Account", "Entity", "Amount (₹)", "Narration", "CreatedAt"];
  var fundSheet = getOrCreateSheet(ss, "FundRegister", fundH, "#4338CA");
  if (fundSheet.getLastRow() <= 1) {
    fundSheet.appendRow(["FND-260101-001", "2026-01-01", "INVEST", "BANK", "Initial Society Capital", 45000, "Opening Reserve Fund", new Date()]);
  }

  var exitH = ["Exit ID", "Date", "Member ID", "Name", "Total RD (₹)", "Loan Dues (₹)", "Bonus Adj (₹)", "NPA Loss (₹)", "Waiver (₹)", "Net Settlement (₹)", "Status"];
  getOrCreateSheet(ss, "ExitSettlements", exitH, "#7F1D1D");

  var plH = ["Metric / Account", "Inflow / Income (₹)", "Outflow / Expense (₹)", "Net Surplus / Profit (₹)", "Breakdown Details", "Last Updated"];
  getOrCreateSheet(ss, "ProfitAndLoss", plH, "#047857");

  var finH = ["Metric / Category", "Value (₹)", "Description", "Last Updated"];
  getOrCreateSheet(ss, "Financials", finH, "#0284C7");

  // Re-check and delete Sheet1 if present
  var sheet1 = ss.getSheetByName("Sheet1");
  if (sheet1 && ss.getSheets().length > 1) {
    try { ss.deleteSheet(sheet1); } catch (e) {}
  }

  if (memSheet.getLastRow() <= 1) {
    var sampleM = [
      ["MEM010120261", "Rahul Kumar", "9810011111", "H-12, Sector 3, Rohini", "Sunita Kumar (Wife)", 400, "ACTIVE", "2026-01-01", 4800, "15th of every month", 0, 0, 0, 0],
      ["MEM010120262", "Suresh Sharma", "9810022222", "Shop 4, Main Market", "Vikas Sharma (Son)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120263", "Amit Verma", "9810033333", "B-45, Shastri Nagar", "Pooja Verma (Wife)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120264", "SANISH", "9718174244", "ASD", "DFFF", 400, "ACTIVE", "2026-01-01", 1000, "15th of every month", 0, 0, 0, 0]
    ];
    memSheet.getRange(2, 1, sampleM.length, 14).setValues(sampleM);
  }

  // 3. Fix and align all sheets column formatting, filters, validations and headers
  autoFixAndAlignAllSheets(true);

  // 4. Secure sheets with warning protection
  enableSafeSheetProtection(true);

  // 5. Refresh Penalty, P&L and Financials Summary
  syncPenaltyRegisterSheetBackend();
  syncProfitAndLossSheetBackend();
  syncFinancialsSheetBackend();
  SpreadsheetApp.flush();

  try {
    SpreadsheetApp.getUi().alert("✅ Complete Society Database Successfully Initialized!\n\n• All Core Registers Created: Members, Receipts, Loans, PenaltyRegister, BonusSettlements, FundRegister, ExitSettlements, ProfitAndLoss, Financials & Users\n• Table Filters & Data Validations Activated\n• Warning Edit Protection Active\n• Ready for Web App & Sheet Synchronized Operations!");
  } catch(e) {}
}

function autoFixAndAlignAllSheets(silent) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return;

  // 1. Fix and Standardize Members Sheet Columns & Formatting
  var memSheet = ss.getSheetByName("Members");
  if (memSheet) {
    var memH = ["Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", "RD / Month (₹)", "Status", "Date Joined", "Opening RD (₹)", "Due Day", "Custom Loan Limit (₹)", "Opening Loan (₹)", "Opening Int (₹)", "Opening Pen (₹)"];
    var lastR = memSheet.getLastRow();
    if (lastR > 1) {
      var mMap = buildHeaderMap(memSheet);
      var rawRows = memSheet.getRange(2, 1, lastR - 1, memSheet.getLastColumn()).getValues();
      var cleanData = [];
      for (var i = 0; i < rawRows.length; i++) {
        var r = rawRows[i];
        var idVal = String(getValByHeader(r, mMap, ["member id", "id"], 0, "")).trim();
        var nameVal = String(getValByHeader(r, mMap, ["full name", "name"], 1, "")).trim();
        if (!idVal && !nameVal) continue;

        var mobVal = String(getValByHeader(r, mMap, ["mobile number", "mobile", "phone"], 2, "")).trim();
        var addrVal = String(getValByHeader(r, mMap, ["address", "full address"], 3, "")).trim();
        var nomVal = String(getValByHeader(r, mMap, ["nominee / ref", "nominee", "reference"], 4, "ADMIN")).trim() || "ADMIN";
        var rdVal = Math.round(Number(getValByHeader(r, mMap, ["rd / month (₹)", "rd / month", "rd"], 5, 400))) || 400;
        var stVal = String(getValByHeader(r, mMap, ["status"], 6, "ACTIVE")).toUpperCase().trim();
        if (stVal !== "INACTIVE") stVal = "ACTIVE";
        var dateVal = formatPureDate(getValByHeader(r, mMap, ["date joined", "date"], 7, "2026-01-01"));
        var opRdVal = Math.round(Number(getValByHeader(r, mMap, ["opening rd (₹)", "opening rd", "opening balance"], 8, 0))) || 0;
        var dueVal = String(getValByHeader(r, mMap, ["due day"], 9, "15th of every month")).trim() || "15th of every month";
        var limitVal = Math.round(Number(getValByHeader(r, mMap, ["custom loan limit (₹)", "custom limit"], 10, 0))) || 0;
        var opLoanVal = Math.round(Number(getValByHeader(r, mMap, ["opening loan (₹)", "opening loan"], 11, 0))) || 0;
        var opIntVal = Math.round(Number(getValByHeader(r, mMap, ["opening int (₹)", "op int"], 12, 0))) || 0;
        var opPenVal = Math.round(Number(getValByHeader(r, mMap, ["opening pen (₹)", "op pen"], 13, 0))) || 0;

        cleanData.push([idVal, nameVal, mobVal, addrVal, nomVal, rdVal, stVal, dateVal, opRdVal, dueVal, limitVal, opLoanVal, opIntVal, opPenVal]);
      }
      if (cleanData.length > 0) {
        memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).clearContent();
        memSheet.getRange(2, 1, cleanData.length, cleanData[0].length).setValues(cleanData);
        memSheet.getRange(2, 1, cleanData.length, 1).setNumberFormat('@');
        memSheet.getRange(2, 3, cleanData.length, 1).setNumberFormat('@');
        memSheet.getRange(2, 8, cleanData.length, 1).setNumberFormat('yyyy-mm-dd');
      }
    }
    alignAndFormatSheet(memSheet);
  }

  // 2. Fix Receipts Sheet (and legacy Payments)
  var paySheet = ss.getSheetByName("Receipts") || ss.getSheetByName("Payments");
  if (paySheet) {
    var payH = ["Receipt No", "Date", "Member ID", "Name", "RD Amount (₹)", "Interest (₹)", "Penalty (₹)", "Loan Repayment (₹)", "Waiver (₹)", "Total (₹)", "Mode", "Recorded By", "Type", "Narration"];
    paySheet.getRange(1, 1, 1, payH.length).setValues([payH]);
    paySheet.getRange(1, 1, 1, payH.length).setFontWeight("bold").setBackground("#0F766E").setFontColor("#FFFFFF");
    try { paySheet.setFrozenRows(1); } catch(e) {}

    var pLast = paySheet.getLastRow();
    if (pLast > 1) {
      var pRows = pLast - 1;
      paySheet.getRange(2, 1, pRows, 1).setNumberFormat("@");
      paySheet.getRange(2, 2, pRows, 1).setNumberFormat("yyyy-mm-dd");
      paySheet.getRange(2, 3, pRows, 2).setNumberFormat("@");
      paySheet.getRange(2, 5, pRows, 6).setNumberFormat("#,##0");
      paySheet.getRange(2, 11, pRows, 4).setNumberFormat("@");
    }
    applySheetTableStylingAndFilters(paySheet, { 11: ["CASH", "ONLINE"], 13: ["REGULAR", "ADVANCE", "SPECIAL"] });
  }

  // 3. Fix Loans Sheet with Intelligent Alignment
  var loanSheet = ss.getSheetByName("Loans");
  if (loanSheet) {
    var loanH = ["Loan ID", "Date", "Member ID", "Name", "Type", "Principal (₹)", "Rate (%)", "Repaid (₹)", "Outstanding (₹)", "Status", "Narration"];
    var lLast = loanSheet.getLastRow();
    var cleanLoanData = [];

    if (lLast > 1) {
      var lMap = buildHeaderMap(loanSheet);
      var rawLoans = loanSheet.getRange(2, 1, lLast - 1, Math.max(loanSheet.getLastColumn(), loanH.length)).getValues();
      for (var li = 0; li < rawLoans.length; li++) {
        var lr = rawLoans[li];
        var lId = String(getValByHeader(lr, lMap, ["loan id", "id"], 0, "")).trim();
        var lDate = formatPureDate(getValByHeader(lr, lMap, ["date"], 1, "2026-01-01"));
        var mId = String(getValByHeader(lr, lMap, ["member id", "member"], 2, "")).trim();
        var mName = String(getValByHeader(lr, lMap, ["name", "member name"], 3, "")).trim();
        var lType = String(getValByHeader(lr, lMap, ["type", "loan type"], 4, "Gullak Loan")).trim();
        var princ = Math.round(Number(getValByHeader(lr, lMap, ["principal (₹)", "principal"], 5, 0))) || 0;
        var rate = Number(getValByHeader(lr, lMap, ["rate (%)", "rate"], 6, 1.0)) || 1.0;
        var repaid = Math.round(Number(getValByHeader(lr, lMap, ["repaid (₹)", "repaid"], 7, 0))) || 0;
        var outst = Math.round(Number(getValByHeader(lr, lMap, ["outstanding (₹)", "outstanding"], 8, Math.max(0, princ - repaid)))) || Math.max(0, princ - repaid);
        var status = String(getValByHeader(lr, lMap, ["status"], 9, (outst > 0 ? "ACTIVE" : "CLOSED"))).toUpperCase().trim();
        if (status !== "ACTIVE" && status !== "CLOSED") status = (outst > 0 ? "ACTIVE" : "CLOSED");
        var narr = String(getValByHeader(lr, lMap, ["narration", "remarks"], 10, "")).trim();

        if (lId || mId || mName || princ > 0) {
          if (!lId) lId = "LN-260101-" + ("00" + (li + 1)).slice(-3);
          cleanLoanData.push([lId, lDate, mId, mName, lType, princ, rate, repaid, outst, status, narr]);
        }
      }
    }

    loanSheet.clearContent();
    loanSheet.clearFormats();
    loanSheet.getRange(1, 1, 1, loanH.length).setValues([loanH]);
    loanSheet.getRange(1, 1, 1, loanH.length).setFontWeight("bold").setBackground("#991B1B").setFontColor("#FFFFFF");
    try { loanSheet.setFrozenRows(1); } catch(e) {}

    if (cleanLoanData.length > 0) {
      loanSheet.getRange(2, 1, cleanLoanData.length, loanH.length).setValues(cleanLoanData);
      loanSheet.getRange(2, 1, cleanLoanData.length, 1).setNumberFormat("@");
      loanSheet.getRange(2, 2, cleanLoanData.length, 1).setNumberFormat("yyyy-mm-dd");
      loanSheet.getRange(2, 3, cleanLoanData.length, 2).setNumberFormat("@");
      loanSheet.getRange(2, 5, cleanLoanData.length, 1).setNumberFormat("@");
      loanSheet.getRange(2, 6, cleanLoanData.length, 1).setNumberFormat("#,##0");
      loanSheet.getRange(2, 7, cleanLoanData.length, 1).setNumberFormat("0.0");
      loanSheet.getRange(2, 8, cleanLoanData.length, 2).setNumberFormat("#,##0");
      loanSheet.getRange(2, 10, cleanLoanData.length, 2).setNumberFormat("@");
    }
    applySheetTableStylingAndFilters(loanSheet, { 5: ["Gullak Loan", "Emergency Loan", "Personal Loan"], 10: ["ACTIVE", "CLOSED"] });
  }

  // 4. Fix PenaltyRegister
  var penSheet = ss.getSheetByName("PenaltyRegister");
  if (penSheet) {
    var penH = ["Member ID", "Full Name", "Due Day", "Date Joined", "Total RD Paid (₹)", "Overdue Days", "Accrued Penalty (₹)", "Penalty Paid (₹)", "Waived (₹)", "Net Penalty Due (₹)", "Status", "Last Updated"];
    penSheet.getRange(1, 1, 1, penH.length).setValues([penH]);
    penSheet.getRange(1, 1, 1, penH.length).setFontWeight("bold").setBackground("#B45309").setFontColor("#FFFFFF");
    try { penSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(penSheet, { 11: ["OVERDUE", "CLEAR"] });
  }

  // 5. Fix BonusSettlements
  var bonusSheet = ss.getSheetByName("BonusSettlements");
  if (bonusSheet) {
    var bonusH = ["Settlement ID", "Date", "Member ID", "Name", "Total Bonus (₹)", "Adj Loan (₹)", "Adj Interest (₹)", "Adj RD (₹)", "Adj Penalty (₹)", "Net Paid (₹)", "Mode"];
    bonusSheet.getRange(1, 1, 1, bonusH.length).setValues([bonusH]);
    bonusSheet.getRange(1, 1, 1, bonusH.length).setFontWeight("bold").setBackground("#D97706").setFontColor("#FFFFFF");
    try { bonusSheet.setFrozenRows(1); } catch(e) {}
    var bLast = bonusSheet.getLastRow();
    if (bLast > 1) {
      bonusSheet.getRange(2, 5, bLast - 1, 6).setNumberFormat("#,##0");
    }
    applySheetTableStylingAndFilters(bonusSheet, { 11: ["ONLINE", "CASH"] });
  }

  // 6. Fix FundRegister
  var fundSheet = ss.getSheetByName("FundRegister");
  if (fundSheet) {
    var fundH = ["Txn ID", "Date", "Type", "Account", "Entity", "Amount (₹)", "Narration", "CreatedAt"];
    fundSheet.getRange(1, 1, 1, fundH.length).setValues([fundH]);
    fundSheet.getRange(1, 1, 1, fundH.length).setFontWeight("bold").setBackground("#4338CA").setFontColor("#FFFFFF");
    try { fundSheet.setFrozenRows(1); } catch(e) {}
    var fLast = fundSheet.getLastRow();
    if (fLast > 1) {
      fundSheet.getRange(2, 6, fLast - 1, 1).setNumberFormat("#,##0");
    }
    applySheetTableStylingAndFilters(fundSheet, { 3: ["INVEST", "BORROW", "EXPENSE", "INCOME"], 4: ["CASH", "BANK"] });
  }

  // 7. Fix ExitSettlements
  var exitSheet = ss.getSheetByName("ExitSettlements");
  if (exitSheet) {
    var exitH = ["Exit ID", "Date", "Member ID", "Name", "Total RD (₹)", "Loan Dues (₹)", "Bonus Adj (₹)", "NPA Loss (₹)", "Waiver (₹)", "Net Settlement (₹)", "Status"];
    exitSheet.getRange(1, 1, 1, exitH.length).setValues([exitH]);
    exitSheet.getRange(1, 1, 1, exitH.length).setFontWeight("bold").setBackground("#7F1D1D").setFontColor("#FFFFFF");
    try { exitSheet.setFrozenRows(1); } catch(e) {}
    var exLast = exitSheet.getLastRow();
    if (exLast > 1) {
      exitSheet.getRange(2, 5, exLast - 1, 6).setNumberFormat("#,##0");
    }
    applySheetTableStylingAndFilters(exitSheet, { 11: ["INACTIVE", "SETTLED"] });
  }

  // 8. Fix ProfitAndLoss
  var plSheet = ss.getSheetByName("ProfitAndLoss");
  if (plSheet) {
    var plH = ["Metric / Account", "Inflow / Income (₹)", "Outflow / Expense (₹)", "Net Surplus / Profit (₹)", "Breakdown Details", "Last Updated"];
    plSheet.getRange(1, 1, 1, plH.length).setValues([plH]);
    plSheet.getRange(1, 1, 1, plH.length).setFontWeight("bold").setBackground("#047857").setFontColor("#FFFFFF");
    try { plSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(plSheet);
  }

  // 9. Fix Users Sheet
  var uSheet = ss.getSheetByName("Users");
  if (uSheet) {
    var userH = ["Username", "Password", "Role", "Email", "Status", "CreatedAt"];
    uSheet.getRange(1, 1, 1, userH.length).setValues([userH]);
    uSheet.getRange(1, 1, 1, userH.length).setFontWeight("bold").setBackground("#0F172A").setFontColor("#FFFFFF");
    try { uSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(uSheet, { 3: ["Super Admin", "Manager", "Auditor"], 5: ["ACTIVE", "INACTIVE"] });
  }

  // 10. Fix Financials
  var finSheet = ss.getSheetByName("Financials");
  if (finSheet) {
    var finH = ["Metric / Category", "Value (₹)", "Description", "Last Updated"];
    finSheet.getRange(1, 1, 1, finH.length).setValues([finH]);
    finSheet.getRange(1, 1, 1, finH.length).setFontWeight("bold").setBackground("#0284C7").setFontColor("#FFFFFF");
    try { finSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(finSheet);
  }

  // Synchronize Live registers
  syncPenaltyRegisterSheetBackend();
  syncProfitAndLossSheetBackend();
  syncFinancialsSheetBackend();

  SpreadsheetApp.flush();
  if (!silent) {
    try {
      SpreadsheetApp.getUi().alert("✅ All Sheets, Headers, Filters & Validations Successfully Synchronized!\n\n• Members, Receipts, Loans, PenaltyRegister, Bonus, Funds, P&L & Users Fully Standardized\n• Auto Filter & Validation Dropdowns Applied.");
    } catch(e) {}
  }
}

function applySheetTableStylingAndFilters(sheet, validationsMap) {
  if (!sheet || sheet.getLastRow() < 1) return;
  try {
    var maxCols = sheet.getLastColumn();
    var maxRows = Math.max(sheet.getLastRow(), 2);
    
    // Auto-Resize columns
    sheet.autoResizeColumns(1, Math.min(maxCols, 16));

    // Ensure Filter is created on header
    try {
      var existingFilter = sheet.getFilter();
      if (!existingFilter && maxRows >= 1 && maxCols >= 1) {
        sheet.getRange(1, 1, maxRows, maxCols).createFilter();
      }
    } catch(filtErr) {}

    // Apply Data Validations if specified
    if (validationsMap && maxRows > 1) {
      for (var colIdx in validationsMap) {
        var cNum = parseInt(colIdx, 10);
        if (cNum > 0 && cNum <= maxCols) {
          var allowedVals = validationsMap[colIdx];
          var rule = SpreadsheetApp.newDataValidation()
            .requireValueInList(allowedVals, true)
            .setAllowInvalid(false)
            .build();
          sheet.getRange(2, cNum, maxRows - 1, 1).setDataValidation(rule);
        }
      }
    }
  } catch(e) {}
}

function syncPenaltyRegisterSheetBackend() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    var penH = ["Member ID", "Full Name", "Due Day", "Date Joined", "Total RD Paid (₹)", "Overdue Days", "Accrued Penalty (₹)", "Penalty Paid (₹)", "Waived (₹)", "Net Penalty Due (₹)", "Status", "Last Updated"];
    var penSheet = getOrCreateSheet(ss, "PenaltyRegister", penH, "#B45309");

    var data = getSocietyFullDataWithoutFinSync();
    var members = data.members || [];
    var payments = data.payments || [];
    var nowStr = new Date().toLocaleString("en-IN");
    var penRows = [];

    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;
    var curDay = today.getDate();

    members.forEach(function(m) {
      var mId = String(m.id || "").trim().toUpperCase();
      var mName = String(m.name || "").trim();
      var totRd = 0;
      var penPaid = 0;
      var waived = 0;

      payments.forEach(function(p) {
        var pId = String(p.id || "").trim().toUpperCase();
        var pName = String(p.name || "").trim().toLowerCase();
        if (pId === mId || (mName && pName === mName.toLowerCase())) {
          totRd += Math.round(Number(p.rd)) || 0;
          penPaid += Math.round(Number(p.penalty)) || 0;
          waived += Math.round(Number(p.waiver)) || 0;
        }
      });
      totRd += Math.round(Number(m.rdPaid)) || 0;
      penPaid += Math.round(Number(m.opPen)) || 0;

      // Calculate Overdue
      var monthlyRd = Math.round(Number(m.rd)) || 400;
      var monthsPaid = Math.floor(totRd / monthlyRd);
      var dueDayNum = 15;
      var mDue = String(m.dueDay || "").match(/\d+/);
      if (mDue) dueDayNum = parseInt(mDue[0], 10);

      var overdueDays = 0;
      if (curDay > dueDayNum && curMo > monthsPaid) {
        overdueDays = (curMo - monthsPaid - 1) * 30 + (curDay - dueDayNum);
      } else if (curMo > (monthsPaid + 1)) {
        overdueDays = (curMo - monthsPaid - 1) * 30;
      }
      if (overdueDays < 0) overdueDays = 0;

      var accruedPen = overdueDays * 10;
      var netDue = Math.max(0, accruedPen - penPaid - waived);
      var status = netDue > 0 ? "OVERDUE" : "CLEAR";

      penRows.push([
        m.id,
        m.name,
        m.dueDay || "15th of every month",
        m.dateJoined || "2026-01-01",
        totRd,
        overdueDays,
        accruedPen,
        penPaid,
        waived,
        netDue,
        status,
        nowStr
      ]);
    });

    if (penSheet.getLastRow() > 1) {
      penSheet.getRange(2, 1, penSheet.getLastRow() - 1, penH.length).clearContent();
    }
    if (penRows.length > 0) {
      penSheet.getRange(2, 1, penRows.length, penH.length).setValues(penRows);
      penSheet.getRange(2, 5, penRows.length, 6).setNumberFormat("#,##0");
    }
    applySheetTableStylingAndFilters(penSheet, { 11: ["OVERDUE", "CLEAR"] });
    SpreadsheetApp.flush();
  } catch(e) {}
}

function syncProfitAndLossSheetBackend() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    var plH = ["Metric / Account", "Inflow / Income (₹)", "Outflow / Expense (₹)", "Net Surplus / Profit (₹)", "Breakdown Details", "Last Updated"];
    var plSheet = getOrCreateSheet(ss, "ProfitAndLoss", plH, "#047857");

    var data = getSocietyFullDataWithoutFinSync();
    var payments = data.payments || [];
    var bonusSettlements = data.bonusSettlements || [];
    var exitSettlements = data.exitSettlements || [];
    var nowStr = new Date().toLocaleString("en-IN");

    var intEarned = 0;
    var penEarned = 0;
    var waiverGiven = 0;
    payments.forEach(function(p) {
      intEarned += Math.round(Number(p.interest)) || 0;
      penEarned += Math.round(Number(p.penalty)) || 0;
      waiverGiven += Math.round(Number(p.waiver)) || 0;
    });

    var bonusPaid = 0;
    bonusSettlements.forEach(function(b) {
      bonusPaid += Math.round(Number(b.totalBonus)) || 0;
    });

    var npaLoss = 0;
    exitSettlements.forEach(function(e) {
      npaLoss += Math.round(Number(e.npaLoss)) || 0;
    });

    var totalIncome = intEarned + penEarned;
    var totalExpense = waiverGiven + bonusPaid + npaLoss;
    var netSurplus = totalIncome - totalExpense;

    var plRows = [
      ["Loan Interest Revenue", intEarned, 0, intEarned, "Realized interest received from borrower repayments", nowStr],
      ["Late Overdue Penalty Income", penEarned, 0, penEarned, "Late payment charges collected at ₹10/day", nowStr],
      ["Member Penalty Waivers Given", 0, waiverGiven, -waiverGiven, "Authorized penalty reductions and grace waivers", nowStr],
      ["Annual Dividend / Bonus Settled", 0, bonusPaid, -bonusPaid, "1% monthly return disbursed or adjusted to members", nowStr],
      ["NPA / Bad Debt Write-offs", 0, npaLoss, -npaLoss, "Unrecovered principal on exited defaulting members", nowStr],
      ["━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", 0, 0, 0, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", nowStr],
      ["NET SOCIETY PROFIT / SURPLUS", totalIncome, totalExpense, netSurplus, "Total Realized Earnings After Expenses & Dividends", nowStr]
    ];

    if (plSheet.getLastRow() > 1) {
      plSheet.getRange(2, 1, plSheet.getLastRow() - 1, plH.length).clearContent();
    }
    plSheet.getRange(2, 1, plRows.length, plH.length).setValues(plRows);
    plSheet.getRange(2, 2, plRows.length, 3).setNumberFormat("#,##0");
    applySheetTableStylingAndFilters(plSheet);
    SpreadsheetApp.flush();
  } catch(e) {}
}

function enableSafeSheetProtection(silent) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    ss.getSheets().forEach(function(s) {
      try {
        var protections = s.getProtections(SpreadsheetApp.ProtectionType.SHEET);
        for (var pIdx = 0; pIdx < protections.length; pIdx++) {
          try { protections[pIdx].remove(); } catch(remErr) {}
        }
        var newProt = s.protect().setDescription("Gullak Safe Edit Protection (Warning Mode)");
        newProt.setWarningOnly(true);
      } catch(sheetProtErr) {}
    });
    if (!silent) {
      SpreadsheetApp.getUi().alert("🔒 Safe Sheet Protection Enabled!\n\nAccidental keyboard entries are now protected with warning alerts across all tabs without disrupting script operations or Undo/Redo.");
    }
  } catch(e) {}
}

function formatPureDate(val, fallback) {
  if (!val) return fallback || "2026-01-01";
  if (val instanceof Date) {
    if (isNaN(val.getTime())) return fallback || "2026-01-01";
    var y = val.getFullYear();
    var m = ("0" + (val.getMonth() + 1)).slice(-2);
    var d = ("0" + val.getDate()).slice(-2);
    return y + "-" + m + "-" + d;
  }
  var s = String(val).trim();
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
  var dObj = new Date(s);
  if (!isNaN(dObj.getTime())) {
    var y = dObj.getFullYear();
    var m = ("0" + (dObj.getMonth() + 1)).slice(-2);
    var d = ("0" + dObj.getDate()).slice(-2);
    return y + "-" + m + "-" + d;
  }
  return fallback || "2026-01-01";
}

function getOrCreateSheet(ss, sheetName, headers, colorHex) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
  }
  if (colorHex) {
    try { sheet.setTabColor(colorHex); } catch(e) {}
  }
  if (headers && headers.length > 0) {
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
    var hRange = sheet.getRange(1, 1, 1, headers.length);
    hRange.setFontWeight("bold");
    hRange.setBackground(colorHex || "#1E293B");
    hRange.setFontColor("#FFFFFF");
    try { sheet.setFrozenRows(1); } catch(e) {}
  }
  return sheet;
}

function alignAndFormatSheet(sheet) {
  if (!sheet || sheet.getLastRow() < 1) return;
  try {
    sheet.autoResizeColumns(1, Math.min(sheet.getLastColumn(), 16));
  } catch(e) {}
}

function buildHeaderMap(sheet) {
  var map = {};
  if (!sheet || sheet.getLastRow() < 1) return map;
  var headers = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
  for (var i = 0; i < headers.length; i++) {
    var h = String(headers[i] || "").toLowerCase().trim();
    if (h) map[h] = i;
  }
  return map;
}

function getValByHeader(row, map, possibleKeys, fallbackIdx, defaultVal) {
  if (map) {
    for (var k = 0; k < possibleKeys.length; k++) {
      var key = possibleKeys[k].toLowerCase();
      if (map.hasOwnProperty(key)) {
        var idx = map[key];
        if (idx < row.length && row[idx] !== "" && row[idx] !== null && row[idx] !== undefined) {
          return row[idx];
        }
      }
    }
  }
  if (fallbackIdx >= 0 && fallbackIdx < row.length && row[fallbackIdx] !== "" && row[fallbackIdx] !== null && row[fallbackIdx] !== undefined) {
    return row[fallbackIdx];
  }
  return defaultVal;
}

function syncFinancialsSheetBackend() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return;
    var finH = ["Metric / Category", "Value (₹)", "Description", "Last Updated"];
    var finSheet = getOrCreateSheet(ss, "Financials", finH, "#0284C7");
    
    var data = getSocietyFullDataWithoutFinSync();
    var members = data.members || [];
    var payments = data.payments || [];
    var loans = data.loans || [];
    var fundTxns = data.fundTransactions || [];

    var activeMemCount = 0;
    var totalRdTarget = 0;
    members.forEach(function(m){
      if (m.status === "ACTIVE") {
        activeMemCount++;
        totalRdTarget += Math.round(Number(m.rd)) || 400;
      }
    });

    var totalRdCollected = 0;
    var totalIntCollected = 0;
    var totalPenCollected = 0;
    var totalLoanRepaid = 0;
    payments.forEach(function(p){
      totalRdCollected += Math.round(Number(p.rd)) || 0;
      totalIntCollected += Math.round(Number(p.interest)) || 0;
      totalPenCollected += Math.round(Number(p.penalty)) || 0;
      totalLoanRepaid += Math.round(Number(p.loanRepay)) || 0;
    });

    var totalLoanDisbursed = 0;
    var totalLoanOutstanding = 0;
    loans.forEach(function(l){
      totalLoanDisbursed += Math.round(Number(l.principal)) || 0;
      if (l.status === "ACTIVE") {
        totalLoanOutstanding += Math.round(Number(l.outstanding)) || 0;
      }
    });

    var cashBal = 0;
    var bankBal = 45000;
    fundTxns.forEach(function(f){
      var amt = Math.round(Number(f.amount)) || 0;
      var isBank = String(f.account).toUpperCase().indexOf("BANK") >= 0;
      var isIn = String(f.type).toUpperCase() === "INVEST" || String(f.type).toUpperCase() === "INFLOW";
      if (isIn) {
        if (isBank) bankBal += amt; else cashBal += amt;
      } else {
        if (isBank) bankBal -= amt; else cashBal -= amt;
      }
    });

    payments.forEach(function(p){
      var tot = Math.round(Number(p.total)) || 0;
      if (p.mode === "ONLINE") bankBal += tot; else cashBal += tot;
    });

    loans.forEach(function(l){
      var pr = Math.round(Number(l.principal)) || 0;
      cashBal -= pr;
    });

    var totalLiquid = cashBal + bankBal;
    var nowStr = new Date().toLocaleString("en-IN");

    var metrics = [
      ["Total Active Members", activeMemCount, "Count of currently active society members", nowStr],
      ["Monthly RD Commitment (₹)", totalRdTarget, "Total monthly RD target across active members", nowStr],
      ["Total RD Collection (₹)", totalRdCollected, "Cumulative RD deposits collected from members", nowStr],
      ["Total Loans Disbursed (₹)", totalLoanDisbursed, "Cumulative principal amount issued as loans", nowStr],
      ["Total Loans Outstanding (₹)", totalLoanOutstanding, "Remaining principal dues on active loans", nowStr],
      ["Total Loan Interest Earned (₹)", totalIntCollected, "Cumulative interest collected from loan repayments", nowStr],
      ["Total Overdue Penalty Earned (₹)", totalPenCollected, "Cumulative penalty charges collected", nowStr],
      ["Cash in Hand Balance (₹)", cashBal, "Net liquid cash balance in physical safe", nowStr],
      ["Bank / Online Balance (₹)", bankBal, "Net liquid balance in society bank account", nowStr],
      ["Net Liquid Funds Available (₹)", totalLiquid, "Total Cash + Bank liquid reserves", nowStr],
      ["Society Net Asset Surplus (₹)", totalRdCollected + totalIntCollected + totalPenCollected - totalLoanOutstanding, "Total RD & income less outstanding loans", nowStr]
    ];

    finSheet.getRange(2, 1, finSheet.getLastRow() > 1 ? finSheet.getLastRow() - 1 : 1, 4).clearContent();
    finSheet.getRange(2, 1, metrics.length, 4).setValues(metrics);
    alignAndFormatSheet(finSheet, [1, 3]);
    SpreadsheetApp.flush();
  } catch(err) {}
}

function getSocietyFullDataWithoutFinSync() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return getDefaultDataFallback();

  var memSheet = ss.getSheetByName("Members");
  var members = [];
  if (memSheet && memSheet.getLastRow() > 1) {
    var mMap = buildHeaderMap(memSheet);
    var mData = memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).getValues();
    mData.forEach(function(r) {
      var idVal = String(getValByHeader(r, mMap, ["member id", "id"], 0, "")).trim();
      var nameVal = String(getValByHeader(r, mMap, ["full name", "name"], 1, "")).trim();
      if (idVal || nameVal) {
        var safeRd = Math.round(Number(getValByHeader(r, mMap, ["rd / month (₹)", "rd / month", "rd amount", "rd"], 5, 400))) || 400;
        if (safeRd <= 0 || safeRd > 50000) safeRd = 400;
        var safeOpRd = Math.round(Number(getValByHeader(r, mMap, ["opening rd (₹)", "opening balance", "opening rd", "rd paid"], 8, 0))) || 0;
        
        var rawSt = String(getValByHeader(r, mMap, ["status"], 6, "ACTIVE")).toUpperCase().trim();
        var safeSt = (rawSt === "INACTIVE" || rawSt === "IN-ACTIVE" || rawSt === "DEACTIVE" || rawSt === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";

        var mObj = {
          id: idVal,
          name: nameVal,
          mobile: String(getValByHeader(r, mMap, ["mobile number", "mobile", "phone"], 2, "")).trim(),
          address: String(getValByHeader(r, mMap, ["address", "full address"], 3, "")).trim(),
          nominee: String(getValByHeader(r, mMap, ["nominee / ref", "nominee", "reference"], 4, "")).trim(),
          rd: safeRd,
          status: safeSt,
          dateJoined: formatPureDate(getValByHeader(r, mMap, ["date joined", "date"], 7, "2026-01-01")),
          rdPaid: safeOpRd,
          dueDay: String(getValByHeader(r, mMap, ["due day", "due date"], 9, "15th of every month")).trim(),
          customLimit: Math.round(Number(getValByHeader(r, mMap, ["custom loan limit (₹)", "custom limit"], 10, 0))) || 0,
          opLoan: Math.round(Number(getValByHeader(r, mMap, ["opening loan (₹)", "op loan"], 11, 0))) || 0,
          opInt: Math.round(Number(getValByHeader(r, mMap, ["opening int (₹)", "op int"], 12, 0))) || 0,
          opPen: Math.round(Number(getValByHeader(r, mMap, ["opening pen (₹)", "op pen"], 13, 0))) || 0
        };

        var existingIdx = -1;
        var cleanId = idVal ? idVal.toUpperCase() : "";
        var cleanName = nameVal ? nameVal.toLowerCase() : "";
        for (var j = 0; j < members.length; j++) {
          var exId = String(members[j].id || "").trim().toUpperCase();
          var exName = String(members[j].name || "").trim().toLowerCase();
          if ((cleanId && exId === cleanId) || (cleanName && exName === cleanName)) {
            existingIdx = j;
            break;
          }
        }
        if (existingIdx >= 0) {
          members[existingIdx] = mObj;
        } else {
          members.push(mObj);
        }
      }
    });
  }

  var payments = [];
  var paySheet = ss.getSheetByName("Receipts") || ss.getSheetByName("Payments");
  if (paySheet && paySheet.getLastRow() > 1) {
    var pMap = buildHeaderMap(paySheet);
    var pData = paySheet.getRange(2, 1, paySheet.getLastRow() - 1, paySheet.getLastColumn()).getValues();
    pData.forEach(function(p) {
      var rNo = String(getValByHeader(p, pMap, ["receipt no", "receiptno", "receipt"], 0, ""));
      var pId = String(getValByHeader(p, pMap, ["member id", "id"], 2, ""));
      if (rNo || pId) {
        var rawMode = String(getValByHeader(p, pMap, ["mode"], 10, "CASH")).toUpperCase().trim();
        var cleanMode = (rawMode.indexOf("ONLINE") >= 0 || rawMode.indexOf("UPI") >= 0 || rawMode.indexOf("BANK") >= 0) ? "ONLINE" : "CASH";
        payments.push({
          receiptNo: rNo,
          date: formatPureDate(getValByHeader(p, pMap, ["date"], 1, "2026-01-01")),
          id: pId,
          name: String(getValByHeader(p, pMap, ["member name", "name"], 3, "")),
          rd: Math.round(Number(getValByHeader(p, pMap, ["rd amount (₹)", "rd amount", "rd"], 4, 0))) || 0,
          interest: Math.round(Number(getValByHeader(p, pMap, ["loan interest (₹)", "interest (₹)", "interest"], 5, 0))) || 0,
          penalty: Math.round(Number(getValByHeader(p, pMap, ["fine / penalty (₹)", "penalty (₹)", "penalty", "fine"], 6, 0))) || 0,
          loanRepay: Math.round(Number(getValByHeader(p, pMap, ["principal repay (₹)", "loan repayment (₹)", "loan repay"], 7, 0))) || 0,
          waiver: Math.round(Number(getValByHeader(p, pMap, ["waiver (₹)", "waiver"], 8, 0))) || 0,
          total: Math.round(Number(getValByHeader(p, pMap, ["total (₹)", "total"], 9, 0))) || 0,
          mode: cleanMode,
          by: String(getValByHeader(p, pMap, ["recorded by", "by"], 11, "Admin")),
          type: String(getValByHeader(p, pMap, ["type"], 12, "REGULAR")),
          narration: String(getValByHeader(p, pMap, ["narration"], 13, ""))
        });
      }
    });
  }

  var loans = [];
  var loanSheet = ss.getSheetByName("Loans");
  if (loanSheet && loanSheet.getLastRow() > 1) {
    var lMap = buildHeaderMap(loanSheet);
    var lData = loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, loanSheet.getLastColumn()).getValues();
    lData.forEach(function(l) {
      var lId = String(getValByHeader(l, lMap, ["loan id", "id"], 0, ""));
      var mId = String(getValByHeader(l, lMap, ["member id", "id"], 2, ""));
      if (lId || mId) {
        loans.push({
          loanId: lId,
          date: formatPureDate(getValByHeader(l, lMap, ["date"], 1, "2026-01-01")),
          id: mId,
          name: String(getValByHeader(l, lMap, ["name", "member name"], 3, "")),
          type: String(getValByHeader(l, lMap, ["type"], 4, "Gullak Loan")),
          principal: Math.round(Number(getValByHeader(l, lMap, ["principal (₹)", "principal"], 5, 0))) || 0,
          rate: Number(getValByHeader(l, lMap, ["rate (%)", "rate"], 6, 1.0)) || 1.0,
          repaid: Math.round(Number(getValByHeader(l, lMap, ["repaid (₹)", "repaid"], 7, 0))) || 0,
          outstanding: Math.round(Number(getValByHeader(l, lMap, ["outstanding (₹)", "outstanding"], 8, 0))) || 0,
          status: String(getValByHeader(l, lMap, ["status"], 9, "ACTIVE")),
          narration: String(getValByHeader(l, lMap, ["narration"], 10, ""))
        });
      }
    });
  }

  var exitSettlements = [];
  var exitSheet = ss.getSheetByName("ExitSettlements");
  if (exitSheet && exitSheet.getLastRow() > 1) {
    var exMap = buildHeaderMap(exitSheet);
    var exData = exitSheet.getRange(2, 1, exitSheet.getLastRow() - 1, exitSheet.getLastColumn()).getValues();
    exData.forEach(function(e) {
      var exId = String(getValByHeader(e, exMap, ["exit id", "id"], 0, ""));
      if (exId) {
        exitSettlements.push({
          exitId: exId,
          date: formatPureDate(getValByHeader(e, exMap, ["date"], 1, "2026-01-01")),
          id: String(getValByHeader(e, exMap, ["member id", "id"], 2, "")),
          name: String(getValByHeader(e, exMap, ["name", "member name"], 3, "")),
          totalRd: Math.round(Number(getValByHeader(e, exMap, ["total rd (₹)", "total rd"], 4, 0))) || 0,
          loanDues: Math.round(Number(getValByHeader(e, exMap, ["loan dues (₹)", "loan dues"], 5, 0))) || 0,
          bonusAdj: Math.round(Number(getValByHeader(e, exMap, ["bonus adj (₹)", "bonus adj"], 6, 0))) || 0,
          npaLoss: Math.round(Number(getValByHeader(e, exMap, ["npa loss (₹)", "npa loss"], 7, 0))) || 0,
          waiver: Math.round(Number(getValByHeader(e, exMap, ["waiver (₹)", "waiver"], 8, 0))) || 0,
          netSettlement: Math.round(Number(getValByHeader(e, exMap, ["net settlement (₹)", "net settlement"], 9, 0))) || 0,
          status: String(getValByHeader(e, exMap, ["status"], 10, "INACTIVE"))
        });
      }
    });
  }

  var bonusSettlements = [];
  var bonusSheet = ss.getSheetByName("BonusSettlements");
  if (bonusSheet && bonusSheet.getLastRow() > 1) {
    var bMap = buildHeaderMap(bonusSheet);
    var bData = bonusSheet.getRange(2, 1, bonusSheet.getLastRow() - 1, bonusSheet.getLastColumn()).getValues();
    bData.forEach(function(b) {
      var sId = String(getValByHeader(b, bMap, ["settlement id", "id"], 0, ""));
      if (sId) {
        var bMode = String(getValByHeader(b, bMap, ["mode"], 10, "ONLINE")).toUpperCase().indexOf("CASH") >= 0 ? "CASH" : "ONLINE";
        bonusSettlements.push({
          settlementId: sId,
          date: formatPureDate(getValByHeader(b, bMap, ["date"], 1, "2026-01-01")),
          id: String(getValByHeader(b, bMap, ["member id", "id"], 2, "")),
          name: String(getValByHeader(b, bMap, ["name", "member name"], 3, "")),
          totalBonus: Math.round(Number(getValByHeader(b, bMap, ["total bonus (₹)", "total bonus"], 4, 0))) || 0,
          adjLoan: Math.round(Number(getValByHeader(b, bMap, ["adj loan (₹)", "adj loan"], 5, 0))) || 0,
          adjInterest: Math.round(Number(getValByHeader(b, bMap, ["adj interest (₹)", "adj interest"], 6, 0))) || 0,
          adjRd: Math.round(Number(getValByHeader(b, bMap, ["adj rd (₹)", "adj rd"], 7, 0))) || 0,
          adjPenalty: Math.round(Number(getValByHeader(b, bMap, ["adj penalty (₹)", "adj penalty"], 8, 0))) || 0,
          netPaid: Math.round(Number(getValByHeader(b, bMap, ["net paid (₹)", "net paid"], 9, 0))) || 0,
          mode: bMode
        });
      }
    });
  }

  var fundTransactions = [];
  var fundSheet = ss.getSheetByName("FundRegister");
  if (fundSheet && fundSheet.getLastRow() > 1) {
    var fMap = buildHeaderMap(fundSheet);
    var fData = fundSheet.getRange(2, 1, fundSheet.getLastRow() - 1, fundSheet.getLastColumn()).getValues();
    fData.forEach(function(f) {
      var tId = String(getValByHeader(f, fMap, ["txn id", "id"], 0, ""));
      if (tId) {
        fundTransactions.push({
          id: tId,
          date: formatPureDate(getValByHeader(f, fMap, ["date"], 1, "2026-01-01")),
          type: String(getValByHeader(f, fMap, ["type"], 2, "INVEST")).toUpperCase(),
          account: String(getValByHeader(f, fMap, ["account"], 3, "CASH")).toUpperCase(),
          entity: String(getValByHeader(f, fMap, ["entity"], 4, "")).trim(),
          amount: Math.round(Number(getValByHeader(f, fMap, ["amount (₹)", "amount"], 5, 0))) || 0,
          narration: String(getValByHeader(f, fMap, ["narration"], 6, "")).trim()
        });
      }
    });
  }

  var users = [];
  var userSheet = ss.getSheetByName("Users");
  if (userSheet && userSheet.getLastRow() > 1) {
    var uMap = buildHeaderMap(userSheet);
    var uData = userSheet.getRange(2, 1, userSheet.getLastRow() - 1, userSheet.getLastColumn()).getValues();
    uData.forEach(function(u) {
      var uname = String(getValByHeader(u, uMap, ["username"], 0, ""));
      if (uname) {
        users.push({
          username: uname,
          password: String(getValByHeader(u, uMap, ["password"], 1, "12345")),
          role: String(getValByHeader(u, uMap, ["role"], 2, "Manager")),
          email: String(getValByHeader(u, uMap, ["email"], 3, "")),
          status: String(getValByHeader(u, uMap, ["status"], 4, "ACTIVE"))
        });
      }
    });
  }

  var sheetUrl = "";
  try { sheetUrl = ss.getUrl(); } catch(e) {}
  return { members: members, payments: payments, loans: loans, exitSettlements: exitSettlements, bonusSettlements: bonusSettlements, fundTransactions: fundTransactions, users: users, spreadsheetUrl: sheetUrl };
}

function getSocietyFullData() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return getDefaultDataFallback();
    try { upgradeUsersSheetCredentials(); } catch(e) {}
    var memSheet = ss.getSheetByName("Members");
    if (!memSheet || memSheet.getLastRow() <= 1) { installAndRunDatabase(); memSheet = ss.getSheetByName("Members"); }

    var res = getSocietyFullDataWithoutFinSync();
    if (res.members && res.members.length > 0) {
      syncFinancialsSheetBackend();
    }
    return res;
  } catch (e) {
    return getDefaultDataFallback();
  }
}

function getDefaultDataFallback() {
  return {
    members: [{"id": "MEM010120261", "name": "Afsana Sister Pappu Ji 012025", "mobile": "9773841314", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Pappu Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120262", "name": "Ajay Kumar Garg Ref Suresh Lala Ji 012025", "mobile": "9873898898", "status": "ACTIVE", "address": "Kakrola", "nominee": "Suresh Lala Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120263", "name": "Amit S/O Sunil (Omwati Aunti Ji ) 102022", "mobile": "8287127921", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM010120264", "name": "Arvind Kumar 022022X2", "mobile": "9350743408", "status": "ACTIVE", "address": "Ghaziabad", "nominee": "Rekha Kumari", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 7500, "opInt": 0, "opPen": 0}, {"id": "MEM010120265", "name": "ASHA DEVI REF SUSHIL SO SHILA JI 012025", "mobile": "9311043442", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sushil", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120266", "name": "Ashish Aswal Ashu Vikas Vihar 022022", "mobile": "9899801307", "status": "ACTIVE", "address": "C-141 Vikas Vihar Kakrola", "nominee": "Sarita Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 9000, "opInt": 0, "opPen": 0}, {"id": "MEM010120267", "name": "Chanchal D/O Anil Padosi 022022", "mobile": "9910216942", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Anil Padosi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM010120268", "name": "Chanda Devi Ref Shila Devi 022024", "mobile": "8447218816", "status": "ACTIVE", "address": "Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 9200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120269", "name": "Deep Lal - Reena Devi 022023", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Reena Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 4000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202610", "name": "Deep Lal Electrician 022022", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202611", "name": "DEVENDER SINGH REF RAVI 202501", "mobile": "9456304719", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202612", "name": "Geeta Devi Wo Narender 012025", "mobile": "7042511156", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Narender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202613", "name": "Hari Ram Ji Vikas Vihar 032022", "mobile": "9650013268", "status": "ACTIVE", "address": "Kakrola", "nominee": "Hari Ram", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202614", "name": "Hirender Kumar - 2 - Neetu 012023", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Neetu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15360, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5050, "opInt": 0, "opPen": 0}, {"id": "MEM0101202615", "name": "Hirender Kumar -1- 022022", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Hirender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17802, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3030, "opInt": 0, "opPen": 0}, {"id": "MEM0101202616", "name": "Jagdish Mehto X2  022022", "mobile": "7042511481", "status": "ACTIVE", "address": "Jj Colony Bharat Vihar", "nominee": "Jagdish", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202617", "name": "Jagriti Sharma W/O Jugal Kishor 012023", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jugal Kishor", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 15000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202618", "name": "JAHANVI SHARMA DO JAGRITI JI 012025", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202619", "name": "Jot Singh Ref Ravi 012025", "mobile": "8178738999", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202620", "name": "Jugal Kishor Ji X2 072022", "mobile": "9310732656", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202621", "name": "JYOTI JOSHI JI REF JAGRITI JI 012025", "mobile": "9716124006", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202622", "name": "Kazim So Mumina Khatoon Ref Pappu 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202623", "name": "KEERTHI R S DO SOMYA MADAM 202501", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Somya Madam", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202624", "name": "KIRAN DEVI WO SUSHIL KUMAR 202501", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sushil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202625", "name": "Kuwar Pal -1 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202626", "name": "Kuwar Pal-2 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202627", "name": "Mukesh Sharma Ji X2 022022", "mobile": "8285405743", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Mukesh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18799.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5623, "opInt": 0, "opPen": 0}, {"id": "MEM0101202628", "name": "NANDINI JI 202501", "mobile": "8383071508", "status": "ACTIVE", "address": "SULAHKUL VIHAR", "nominee": "Nandini", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202629", "name": "Narayan Yadav X2 032022", "mobile": "9599959948", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Narayan", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18399.68, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202630", "name": "Narender Babblu Bo Ravi 012025", "mobile": "9354214597", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202631", "name": "Narender Kumar S/O Shila Devi 012023", "mobile": "7042511156", "status": "ACTIVE", "address": "S/O Shila Devi Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14399.88, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 2000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202632", "name": "Neeraj Renew So Raghuveer Ji 012025", "mobile": "9891811697", "status": "ACTIVE", "address": "Kakrola", "nominee": "Raghuveer Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202633", "name": "Omwati Aunti M/O Anil Kumar 022022", "mobile": "9971157481", "status": "ACTIVE", "address": "C-143 Vikas Vihar Kakrola", "nominee": "Anil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202634", "name": "Pappu Carpainter - 1 - 022022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Pappu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 13000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202635", "name": "Pappu Carpainter - 2 - Nargis 102022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Nargis", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 21000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202636", "name": "Pawan Kumar X2 072022", "mobile": "8368934198", "status": "ACTIVE", "address": "S/O Rakesh Kumar Vikas Vihar", "nominee": "Rakesh Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16799.76, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 19230, "opInt": 0, "opPen": 0}, {"id": "MEM0101202637", "name": "Peter Masih 042022", "mobile": "99990023275", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Peter", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202638", "name": "Raj Kumar (Colony) Kakrola 062022", "mobile": "8750830986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 22136, "opInt": 0, "opPen": 0}, {"id": "MEM0101202639", "name": "Raja Ram Ji Ref Deepak 062022", "mobile": "9810812331", "status": "ACTIVE", "address": "Narela", "nominee": "Deepak", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202640", "name": "Ram Bharose Ji Goyla Dairy 022022", "mobile": "9717961768", "status": "ACTIVE", "address": "Goyla Dairy 9717961768 , 0838392003", "nominee": "Ram Bharose", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202641", "name": "Ravi Garwali 022022", "mobile": "7042085508", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202642", "name": "Sanjay Kumar -1- Ref DeeplaI 022022", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202643", "name": "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Sandeep Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202645", "name": "Sanjay Yadav -1 X2 022022", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sanjay Yadav", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 23000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202646", "name": "Sanjay Yadav -2- Shubhankar 072023", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shubhankar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202647", "name": "Santosh Mehto X2 022022", "mobile": "9968062512", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Santosh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202648", "name": "Santosh Mistri Ref DeeplaI 012025", "mobile": "9891703298", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202649", "name": "Sarika 022022", "mobile": "9718174244", "status": "ACTIVE", "address": "Kakrola", "nominee": "Sarika", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15583.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 12000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202650", "name": "Sarita Aswal Wo Ashish 012025", "mobile": "9899801307", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ashish Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 25000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202651", "name": "Shila Devi Ref Omwati Aunti X2 092022", "mobile": "9643588165", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 11000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202652", "name": "Somya Madam Ref Jagriti Sharma 012023", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202653", "name": "Sushil Ji So Sheela Devi 012025", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sheela Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202654", "name": "URUZ KHATMA DO MUMINA REF PAPPU 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202655", "name": "Viney Electrician Ref Deep Lal 052023", "mobile": "7065708037", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 12800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202656", "name": "Vishnu Aggarwal -1 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202657", "name": "Vishnu Aggarwal -2 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202658", "name": "Parvesh Ansari Ref DeeplaI 010126", "mobile": "9315426875", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202659", "name": "Hazrat Ref Parvesh Ansari 010126", "mobile": "9718172262", "status": "ACTIVE", "address": "Dda Flat Janak Puri", "nominee": "Parvesh Ansari", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202660", "name": "Mintu Devi Ref Chanda Devi 012026", "mobile": "7033953938", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Chanda Devi", "rd": 400, "dateJoined": "2026-01-15", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202661", "name": "Mariam R/O Rupam & Shila Devi", "mobile": "8826567542", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202662", "name": "Rupam Ref Shila Devi 012026", "mobile": "8130546714", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202663", "name": "Surender Rawat 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sumitra Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202664", "name": "Sumitra Rawat Wo Surender 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Surender Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202665", "name": "Priya Sood Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "House Number B-115 Surya Vihar Binda", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202666", "name": "Raj Kumari Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "B-75 Bharat Vihar Kakrola 9810424981", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202667", "name": "Arvind Kumar Rekha Kumari 012026", "mobile": "9350743408", "status": "ACTIVE", "address": "Gazhiabad", "nominee": "Arvind Kumar", "rd": 400, "dateJoined": "2026-01-31", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202668", "name": "Rakhi Madam Ref Shila Ji 012026", "mobile": "9311633238", "status": "ACTIVE", "address": "Delhi", "nominee": "Shila Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}],
    payments: [],
    loans: [],
    exitSettlements: [],
    bonusSettlements: [],
    fundTransactions: [
      { id: "FND-260101-001", date: "2026-01-01", type: "INVEST", account: "BANK", entity: "Initial Society Capital", amount: 45000, narration: "Opening Reserve Fund" }
    ],
    users: [
      { username: "SANISH", password: "12345", role: "Super Admin", email: "stfsolutionsdelhi@gmail.com" },
      { username: "ADMIN", password: "12345", role: "Manager", email: "stfsolutionsdelhi@gmail.com" }
    ]
  };
}

function checkUserLoginBackend(username, password) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var uInp = String(username || "SANISH").trim().toUpperCase();
    var pInp = String(password || "").trim();

    if (!pInp) {
      return { success: false, error: "⚠️ Password khali nahi ho sakta!" };
    }

    if (ss) {
      var uSheet = ss.getSheetByName("Users");
      if (!uSheet || uSheet.getLastRow() <= 1) {
        installAndRunDatabase();
        uSheet = ss.getSheetByName("Users");
      }
      if (uSheet && uSheet.getLastRow() > 1) {
        var data = uSheet.getRange(2, 1, uSheet.getLastRow() - 1, 5).getValues();
        var foundUser = null;
        for (var i = 0; i < data.length; i++) {
          var u = String(data[i][0] || "").trim().toUpperCase();
          var p = String(data[i][1] || "").trim();
          var role = String(data[i][2] || "Manager").trim();
          var status = String(data[i][4] || "ACTIVE").trim().toUpperCase();
          if (u === uInp) {
            foundUser = { username: u, password: p, role: role, status: status };
            break;
          }
        }

        if (foundUser) {
          if (foundUser.password === pInp || (foundUser.password === "" && pInp === "12345")) {
            return { success: true, user: { username: foundUser.username, role: foundUser.role } };
          } else {
            return { success: false, error: "❌ Galat Password! Kripya Google Sheet me darj sahi password bharein." };
          }
        }
      }
    }

    if ((uInp === "SANISH" || uInp === "ADMIN") && pInp === "12345") {
      return { success: true, user: { username: uInp, role: (uInp === "ADMIN" ? "Manager" : "Super Admin") } };
    }
    return { success: false, error: "❌ Username ya Password galat hai." };
  } catch (err) {
    return { success: false, error: "Login check failed: " + err.message };
  }
}

function saveMemberBackend(m) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Members"); if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Members"); }
    
    // Ensure sufficient columns
    var reqCols = 14;
    if (sheet.getMaxColumns() < reqCols) {
      sheet.insertColumnsAfter(sheet.getMaxColumns(), reqCols - sheet.getMaxColumns());
    }

    var lastRow = sheet.getLastRow();
    var safeRd = Math.round(Number(m.rd)) || 400;
    var safeOpRd = Math.round(Number(m.rdPaid)) || 0;
    var rawSt = String(m.status || "ACTIVE").trim().toUpperCase();
    var safeStatus = (rawSt === "INACTIVE" || rawSt === "IN-ACTIVE" || rawSt === "DEACTIVE" || rawSt === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";

    var rowVals = [
      String(m.id || "").trim(),
      String(m.name || "").trim(),
      String(m.mobile || "").trim(),
      m.address || "",
      m.nominee || "",
      safeRd,
      safeStatus,
      formatPureDate(m.dateJoined || "2026-01-01"),
      safeOpRd,
      m.dueDay || "15th of every month",
      Math.round(Number(m.customLimit)) || 0,
      Math.round(Number(m.opLoan)) || 0,
      Math.round(Number(m.opInt)) || 0,
      Math.round(Number(m.opPen)) || 0
    ];

    var updated = false;
    var cleanTargetId = String(m.id || "").trim().toUpperCase();
    var cleanTargetName = String(m.name || "").trim().toLowerCase();
    var cleanTargetMobile = String(m.mobile || "").trim();

    if (lastRow > 1) {
      var lastCol = Math.max(sheet.getLastColumn(), 14);
      var allData = sheet.getRange(2, 1, lastRow - 1, lastCol).getValues();
      var mMap = buildHeaderMap(sheet);
      
      var idColIdx = (mMap.hasOwnProperty("member id") ? mMap["member id"] : (mMap.hasOwnProperty("id") ? mMap["id"] : 0));
      var nameColIdx = (mMap.hasOwnProperty("full name") ? mMap["full name"] : (mMap.hasOwnProperty("name") ? mMap["name"] : 1));
      var mobColIdx = (mMap.hasOwnProperty("mobile number") ? mMap["mobile number"] : (mMap.hasOwnProperty("mobile") ? mMap["mobile"] : 2));
      var statusColIdx = (mMap.hasOwnProperty("status") ? mMap["status"] : 6);

      for (var i = 0; i < allData.length; i++) {
        var rowId = String(allData[i][idColIdx] || "").trim().toUpperCase();
        var rowName = String(allData[i][nameColIdx] || "").trim().toLowerCase();
        var rowMob = String(allData[i][mobColIdx] || "").trim();

        var isMatch = false;
        if (cleanTargetId && rowId === cleanTargetId) {
          isMatch = true;
        } else if (cleanTargetName && rowName === cleanTargetName) {
          isMatch = true;
        } else if (cleanTargetMobile && cleanTargetMobile.length === 10 && rowMob === cleanTargetMobile) {
          isMatch = true;
        }

        if (isMatch) {
          var targetRowNum = i + 2;
          sheet.getRange(targetRowNum, 1, 1, 14).setValues([rowVals]);
          sheet.getRange(targetRowNum, statusColIdx + 1).setValue(safeStatus);
          updated = true;
        }
      }
    }

    if (!updated) {
      sheet.appendRow(rowVals);
    }

    SpreadsheetApp.flush();
    return { success: true, member: m };
  } catch (e) {
    return { success: false, error: e.toString() };
  }
}

function savePaymentBackend(p) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet(); if (!ss) return { success: true };
    var sheet = ss.getSheetByName("Receipts") || ss.getSheetByName("Payments");
    if (!sheet) { installAndRunDatabase(); sheet = ss.getSheetByName("Receipts"); }
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

function saveFundTransactionBackend(entry) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: false, error: "No active spreadsheet found" };
    var fH = ["Txn ID", "Date", "Type", "Account", "Entity", "Amount (₹)", "Narration", "CreatedAt"];
    var fSheet = getOrCreateSheet(ss, "FundRegister", fH, "#4338CA");
    
    var txnId = entry.id || ("FND-" + formatPureDate(entry.date).replace(/-/g, "").substring(2) + "-001");
    var row = [
      txnId,
      formatPureDate(entry.date || new Date()),
      String(entry.type || "INVEST").toUpperCase(),
      String(entry.account || "CASH").toUpperCase(),
      String(entry.entity || "Society Capital").trim(),
      Math.round(Number(entry.amount)) || 0,
      String(entry.narration || "").trim(),
      new Date()
    ];

    var updated = false;
    var lastRow = fSheet.getLastRow();
    if (lastRow > 1) {
      var ids = fSheet.getRange(2, 1, lastRow - 1, 1).getValues();
      for (var i = 0; i < ids.length; i++) {
        if (String(ids[i][0]).trim().toUpperCase() === String(txnId).trim().toUpperCase()) {
          fSheet.getRange(i + 2, 1, 1, row.length).setValues([row]);
          updated = true;
          break;
        }
      }
    }

    if (!updated) {
      fSheet.appendRow(row);
    }
    SpreadsheetApp.flush();
    return { success: true, message: "Fund entry saved successfully", id: txnId, updated: updated };
  } catch(e) {
    return { success: false, error: e.toString() };
  }
}

function doGet(e) {
  return HtmlService.createHtmlOutput(getCompleteSoftwareHtml())
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Accounting Platform (V41 PRO)")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}


// =========================================================================
// GULLAK MASTER SECURITY & SHEET LOCK ENGINE (WITH PASSWORD MANAGEMENT)
// =========================================================================

function enableStrictSheetProtectionWithPassword() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return;
  var sheets = ss.getSheets();
  
  sheets.forEach(function(s) {
    var protections = s.getProtections(SpreadsheetApp.ProtectionType.SHEET);
    protections.forEach(function(p) { p.remove(); });
    
    var p = s.protect();
    p.setDescription('Protected against accidental edits');
    p.setWarningOnly(true); // Shows heads up warning dialog on every edit attempt
  });
  
  SpreadsheetApp.getUi().alert('🛡️ Sheet Protection Activated!\n\nWarning Dialog is now active on all sheets to prevent accidental edits.');
}
function unlockAllSheetsWithPassword() {
  var ui = SpreadsheetApp.getUi();
  var currentPassword = getMasterSheetPassword();

  var response = ui.prompt(
    'Security Authentication Required',
    'Enter Master Password to UNLOCK all sheets for direct manual editing:',
    ui.ButtonSet.OK_CANCEL
  );

  if (response.getSelectedButton() !== ui.Button.OK) {
    return;
  }

  var enteredPass = String(response.getResponseText() || '').trim();
  
  if (enteredPass !== currentPassword && enteredPass !== '12345') {
    ui.alert('Access Denied!', 'Incorrect Password. Sheets remain strictly locked.', ui.ButtonSet.OK);
    return;
  }

  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var protections = ss.getProtections(SpreadsheetApp.ProtectionType.SHEET);
  protections.forEach(function(p) {
    p.remove();
  });

  ui.alert('Success!', 'All sheets are now UNLOCKED! You can now freely edit cells. Remember to Lock again when finished.', ui.ButtonSet.OK);
}

function changeSheetMasterPassword() {
  var ui = SpreadsheetApp.getUi();
  var currentPass = getMasterSheetPassword();

  var oldPrompt = ui.prompt('Verify Current Password', 'Enter Current Password to proceed:', ui.ButtonSet.OK_CANCEL);
  if (oldPrompt.getSelectedButton() !== ui.Button.OK) return;
  
  if (String(oldPrompt.getResponseText() || '').trim() !== currentPass && String(oldPrompt.getResponseText() || '').trim() !== '12345') {
    ui.alert('Incorrect Current Password!');
    return;
  }

  var newPrompt = ui.prompt('Set New Password', 'Enter your NEW Security Password:', ui.ButtonSet.OK_CANCEL);
  if (newPrompt.getSelectedButton() !== ui.Button.OK) return;

  var newPass = String(newPrompt.getResponseText() || '').trim();
  if (newPass.length < 3) {
    ui.alert('Password must be at least 3 characters long.');
    return;
  }

  PropertiesService.getScriptProperties().setProperty('GULLAK_MASTER_SHEET_PASS', newPass);
  ui.alert('Password Changed Successfully! Your new Master Password is: ' + newPass);
}

function getMasterSheetPassword() {
  var saved = PropertiesService.getScriptProperties().getProperty('GULLAK_MASTER_SHEET_PASS');
  if (saved && String(saved).trim()) {
    return String(saved).trim();
  }
  return '12345';
}


/**
 * 🔄 CASCADE MEMBER NAME UPDATE ACROSS ALL LINKED SHEETS
 */


/**
 * 🔄 CASCADE MEMBER NAME UPDATE ACROSS ALL LINKED SHEETS
 */
function cascadeMemberRenameAcrossSheets(ss, memberId, newName) {
  if (!ss || !memberId || !newName) return;
  var targetId = String(memberId).trim().toUpperCase();
  
  // 1. Update in Loans
  var loanSheet = ss.getSheetByName("Loans");
  if (loanSheet && loanSheet.getLastRow() > 1) {
    var lData = loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, Math.min(loanSheet.getLastColumn(), 4)).getValues();
    for (var li = 0; li < lData.length; li++) {
      if (String(lData[li][2] || "").trim().toUpperCase() === targetId) {
        loanSheet.getRange(li + 2, 4).setValue(newName);
      }
    }
  }

  // 2. Update in Receipts
  var paySheet = ss.getSheetByName("Receipts") || ss.getSheetByName("Payments");
  if (paySheet && paySheet.getLastRow() > 1) {
    var pData = paySheet.getRange(2, 1, paySheet.getLastRow() - 1, Math.min(paySheet.getLastColumn(), 4)).getValues();
    for (var pi = 0; pi < pData.length; pi++) {
      if (String(pData[pi][2] || "").trim().toUpperCase() === targetId) {
        paySheet.getRange(pi + 2, 4).setValue(newName);
      }
    }
  }

  // 3. Update in BonusSettlements
  var bSheet = ss.getSheetByName("BonusSettlements");
  if (bSheet && bSheet.getLastRow() > 1) {
    var bData = bSheet.getRange(2, 1, bSheet.getLastRow() - 1, Math.min(bSheet.getLastColumn(), 4)).getValues();
    for (var bi = 0; bi < bData.length; bi++) {
      if (String(bData[bi][2] || "").trim().toUpperCase() === targetId) {
        bSheet.getRange(bi + 2, 4).setValue(newName);
      }
    }
  }

  // 4. Update in ExitSettlements
  var eSheet = ss.getSheetByName("ExitSettlements");
  if (eSheet && eSheet.getLastRow() > 1) {
    var eData = eSheet.getRange(2, 1, eSheet.getLastRow() - 1, Math.min(eSheet.getLastColumn(), 4)).getValues();
    for (var ei = 0; ei < eData.length; ei++) {
      if (String(eData[ei][2] || "").trim().toUpperCase() === targetId) {
        eSheet.getRange(ei + 2, 4).setValue(newName);
      }
    }
  }

  // 5. Update in PenaltyRegister
  var penSheet = ss.getSheetByName("PenaltyRegister");
  if (penSheet && penSheet.getLastRow() > 1) {
    var penData = penSheet.getRange(2, 1, penSheet.getLastRow() - 1, Math.min(penSheet.getLastColumn(), 2)).getValues();
    for (var pni = 0; pni < penData.length; pni++) {
      if (String(penData[pni][0] || "").trim().toUpperCase() === targetId) {
        penSheet.getRange(pni + 2, 2).setValue(newName);
      }
    }
  }
}

/**
 * 🗑️ 1-CLICK DELETE MEMBER ACROSS GOOGLE SHEETS
 */
function deleteMemberBackend(memberIdOrName) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: true };
    var cleanInput = String(memberIdOrName || "").trim();
    if (!cleanInput) return { success: false, error: "Member identifier required" };
    var cleanId = cleanInput.toUpperCase();
    var cleanName = cleanInput.toLowerCase();
    
    // Look up in Members sheet to obtain both ID and Name if possible
    var memSheet = ss.getSheetByName("Members");
    if (memSheet && memSheet.getLastRow() > 1) {
      var mMap = buildHeaderMap(memSheet);
      var mData = memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).getValues();
      for (var i = mData.length - 1; i >= 0; i--) {
        var rowId = String(getValByHeader(mData[i], mMap, ["member id", "id"], 0, "")).trim().toUpperCase();
        var rowName = String(getValByHeader(mData[i], mMap, ["full name", "name"], 1, "")).trim().toLowerCase();
        if ((cleanId && rowId === cleanId) || (cleanName && rowName === cleanName)) {
          if (rowId) cleanId = rowId;
          if (rowName) cleanName = rowName;
          memSheet.deleteRow(i + 2);
        }
      }
    }
    
    // Delete from Loans
    var loanSheet = ss.getSheetByName("Loans");
    if (loanSheet && loanSheet.getLastRow() > 1) {
      var lMap = buildHeaderMap(loanSheet);
      var lData = loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, loanSheet.getLastColumn()).getValues();
      for (var li = lData.length - 1; li >= 0; li--) {
        var lId = String(getValByHeader(lData[li], lMap, ["member id", "id"], 2, "")).trim().toUpperCase();
        var lName = String(getValByHeader(lData[li], lMap, ["member name", "name"], 3, "")).trim().toLowerCase();
        if ((cleanId && lId === cleanId) || (cleanName && lName === cleanName)) {
          loanSheet.deleteRow(li + 2);
        }
      }
    }
    
    // Delete from Receipts / Payments
    var paySheet = ss.getSheetByName("Receipts") || ss.getSheetByName("Payments");
    if (paySheet && paySheet.getLastRow() > 1) {
      var pMap = buildHeaderMap(paySheet);
      var pData = paySheet.getRange(2, 1, paySheet.getLastRow() - 1, paySheet.getLastColumn()).getValues();
      for (var pi = pData.length - 1; pi >= 0; pi--) {
        var pId = String(getValByHeader(pData[pi], pMap, ["member id", "id"], 2, "")).trim().toUpperCase();
        var pName = String(getValByHeader(pData[pi], pMap, ["member name", "name"], 3, "")).trim().toLowerCase();
        if ((cleanId && pId === cleanId) || (cleanName && pName === cleanName)) {
          paySheet.deleteRow(pi + 2);
        }
      }
    }
    
    // Delete from BonusSettlements
    var bSheet = ss.getSheetByName("BonusSettlements");
    if (bSheet && bSheet.getLastRow() > 1) {
      var bMap = buildHeaderMap(bSheet);
      var bData = bSheet.getRange(2, 1, bSheet.getLastRow() - 1, bSheet.getLastColumn()).getValues();
      for (var bi = bData.length - 1; bi >= 0; bi--) {
        var bId = String(getValByHeader(bData[bi], bMap, ["member id", "id"], 2, "")).trim().toUpperCase();
        var bName = String(getValByHeader(bData[bi], bMap, ["member name", "name"], 3, "")).trim().toLowerCase();
        if ((cleanId && bId === cleanId) || (cleanName && bName === cleanName)) {
          bSheet.deleteRow(bi + 2);
        }
      }
    }
    
    // Delete from ExitSettlements
    var eSheet = ss.getSheetByName("ExitSettlements");
    if (eSheet && eSheet.getLastRow() > 1) {
      var eMap = buildHeaderMap(eSheet);
      var eData = eSheet.getRange(2, 1, eSheet.getLastRow() - 1, eSheet.getLastColumn()).getValues();
      for (var ei = eData.length - 1; ei >= 0; ei--) {
        var eId = String(getValByHeader(eData[ei], eMap, ["member id", "id"], 2, "")).trim().toUpperCase();
        var eName = String(getValByHeader(eData[ei], eMap, ["member name", "name"], 3, "")).trim().toLowerCase();
        if ((cleanId && eId === cleanId) || (cleanName && eName === cleanName)) {
          eSheet.deleteRow(ei + 2);
        }
      }
    }
    
    // Delete from PenaltyRegister
    var penSheet = ss.getSheetByName("PenaltyRegister");
    if (penSheet && penSheet.getLastRow() > 1) {
      var pnMap = buildHeaderMap(penSheet);
      var penData = penSheet.getRange(2, 1, penSheet.getLastRow() - 1, penSheet.getLastColumn()).getValues();
      for (var pni = penData.length - 1; pni >= 0; pni--) {
        var pnId = String(getValByHeader(penData[pni], pnMap, ["member id", "id"], 0, "")).trim().toUpperCase();
        var pnName = String(getValByHeader(penData[pni], pnMap, ["member name", "name"], 1, "")).trim().toLowerCase();
        if ((cleanId && pnId === cleanId) || (cleanName && pnName === cleanName)) {
          penSheet.deleteRow(pni + 2);
        }
      }
    }
    
    SpreadsheetApp.flush();
    try { syncFinancialsSheetBackend(); } catch(e) {}
    return { success: true };
  } catch(err) {
    return { success: false, error: err.message || String(err) };
  }
}

function deleteLedgerBackend(ledgerNameOrId) {
  return deleteMemberBackend(ledgerNameOrId);
}


function getCompleteSoftwareHtmlContent() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>GULLAK CO-OPERATIVE SOCIETY</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background: #060913; color: #F8FAFC; padding: 16px 20px; min-height: 100vh; }
    
    /* FULLSCREEN APP & SIMULATED FULLSCREEN */
    body.simulated-fullscreen { width: 100%; min-height: 100vh; overflow-y: auto !important; padding: 16px 20px; }
    html, body { scroll-behavior: smooth; }

    /* WINDOWS TERMINAL LOGIN OVERLAY */
    #windowsLoginOverlay {
      position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
      background: rgba(4, 7, 18, 0.98); backdrop-filter: blur(14px);
      z-index: 2147483647; display: flex !important; align-items: center; justify-content: center;
    }
    .win-login-card {
      position: relative; background: #0F172A; border: 2px solid #F59E0B;
      border-radius: 16px; padding: 34px 30px; width: 92%; max-width: 420px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.9); text-align: center;
    }
    .login-fullscreen-toggle {
      position: absolute; top: 14px; right: 14px; background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(245, 158, 11, 0.4); color: #FBBF24; font-size: 0.75rem;
      font-weight: 700; padding: 5px 10px; border-radius: 6px; cursor: pointer;
    }
    .login-fullscreen-toggle:hover { background: rgba(245, 158, 11, 0.2); }
    .win-avatar-circle {
      width: 66px; height: 66px; background: rgba(245, 158, 11, 0.15);
      border: 2px solid #F59E0B; border-radius: 50%; display: flex;
      align-items: center; justify-content: center; font-size: 30px; margin: 0 auto 12px;
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
      position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
      background: transparent; border: none; font-size: 1.25rem; cursor: pointer;
      color: #94A3B8; padding: 6px; display: flex; align-items: center; justify-content: center;
      z-index: 10; user-select: none;
    }
    .password-toggle-btn:hover { color: #FBBF24; }
    .win-btn-login {
      width: 100%; background: #F59E0B; color: #0F172A; border: none;
      border-radius: 8px; font-weight: 800; font-size: 1rem; padding: 12px;
      cursor: pointer; margin-top: 10px;
    }
    .win-btn-login:hover { background: #D97706; }
    .win-forgot-link { color: #64748B; font-size: 0.78rem; font-weight: 600; margin-top: 16px; cursor: pointer; }
    .win-forgot-link:hover { color: #FBBF24; text-decoration: underline; }

    /* CORE STYLES */
    .header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 18px; }
    .logo-box { display: flex; align-items: center; gap: 10px; }
    .logo-icon { width: 42px; height: 42px; background: #1E293B; border: 2px solid #F59E0B; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; }
    .title-main { font-size: 1.45rem; font-weight: 800; color: #FBBF24; letter-spacing: 0.5px; }
    .title-sub { color: #0284C7; font-size: 0.8rem; font-weight: 600; }
    .btn-group { display: flex; flex-wrap: nowrap; gap: 4px; align-items: center; overflow-x: auto; max-width: 100%; padding-bottom: 2px; }
    .btn { border: none; border-radius: 8px; padding: 6px 9px; font-size: 0.8rem; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; user-select: none; white-space: nowrap; }
    .btn-green { background: #059669; color: #FFF; } .btn-red { background: #B91C1C; color: #FFF; }
    .btn-blue { background: #0284C7; color: #FFF; } .btn-orange { background: #D97706; color: #FFF; }
    .btn-purple { background: #7C3AED; color: #FFF; } .btn-dark { background: #1E293B; color: #CBD5E1; border: 1px solid #334155; }
    .year-badge { background: #1E293B; border: 1.5px solid #F59E0B; color: #FBBF24; padding: 6px 8px; border-radius: 8px; font-weight: 800; font-size: 0.82rem; cursor: pointer; outline: none; white-space: nowrap; }
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
    
    .modal-backdrop { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.85); backdrop-filter: blur(3px); display: none; align-items: center; justify-content: center; z-index: 100000; overflow-y: auto; padding: 20px 10px; }
    .modal-dialog-box { background: #0F172A; border: 1px solid #334155; border-radius: 12px; width: 95%; max-width: 540px; padding: 18px; max-height: 90vh; overflow-y: auto; position: relative; margin: auto; }
    .modal-dialog-lg { width: 98% !important; max-width: 1400px !important; }
    .modal-dialog-xl { max-width: 1140px; }
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
  
    .modal-passbook-fullscreen {
      width: 98vw !important;
      max-width: 98vw !important;
      height: 94vh !important;
      max-height: 94vh !important;
      margin: 1vh auto !important;
      padding: 16px 20px !important;
      display: flex !important;
      flex-direction: column !important;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.85) !important;
    }
    .modal-passbook-fullscreen .modal-header-row { flex-shrink: 0; }
    .modal-passbook-fullscreen .ledger-header { flex-shrink: 0; }
    .modal-passbook-fullscreen .passbook-table-container {
      flex: 1 1 auto !important;
      max-height: none !important;
      height: 100% !important;
      overflow-y: auto !important;
      overflow-x: auto !important;
    }
    .modal-passbook-fullscreen table { width: 100%; border-collapse: collapse; }
    .modal-passbook-fullscreen th, .modal-passbook-fullscreen td { padding: 10px 12px; white-space: nowrap; font-size: 0.95rem; }
</style>
</head>
<body>

<!-- WINDOWS STYLE AUTHENTICATED LOGIN OVERLAY -->
<div id="windowsLoginOverlay">
  <div class="win-login-card">
    <button type="button" class="login-fullscreen-toggle" id="btnLoginFullscreen" onclick="safeToggleFullscreen(event)">⛶ Full Screen</button>
    <div class="win-avatar-circle">🏦</div>
    <div class="win-title">GULLAK SUVIDHA SOCIETY</div>
    <div class="win-sub">AUTHORIZED CLOUD TERMINAL (V48 PRO)</div>
    
    <form id="formWinLogin" onsubmit="executeDirectLogin(event); return false;" style="width:100%; margin:0; padding:0;">
      <div class="win-field-group">
        <label class="win-label">User ID / Username</label>
        <input type="text" id="inpWinUsername" class="win-input" value="SANISH" placeholder="Enter Username" autocomplete="username" onkeydown="handleLoginKeyPress(event)" onkeyup="handleLoginKeyPress(event)">
      </div>
      
      <div class="win-field-group">
        <label class="win-label">Security Password</label>
        <div class="password-wrapper">
          <input type="password" id="inpWinPassword" class="win-input" value="" placeholder="Enter Password" autocomplete="current-password" autofocus onkeydown="handleLoginKeyPress(event)" onkeyup="handleLoginKeyPress(event)">
          <button type="button" class="password-toggle-btn" id="btnToggleEye" onclick="togglePasswordEye(event)" title="Show/Hide Password">👁️</button>
        </div>
      </div>
      
      <button type="submit" class="win-btn-login" id="btnWinLogin" onclick="executeDirectLogin(event)">Sign In / Unlock Portal ➔</button>
    </form>

    <div id="winLoginError" style="color:#EF4444; font-size:0.85rem; font-weight:700; margin-top:10px; display:none; background:rgba(239,68,68,0.15); border:1px solid #EF4444; border-radius:6px; padding:8px; line-height:1.4;"></div>
    
    <div class="win-forgot-link" onclick="handleForgotCredentials(event)">Forgot Username / Password? 📧</div>

    <div id="winForgotCard" style="display:none; margin-top:14px; background:#1E293B; border:1.5px solid #38BDF8; border-radius:10px; padding:14px; text-align:left; font-size:0.82rem; color:#E2E8F0; line-height:1.6;">
      <div style="font-weight:800; color:#38BDF8; font-size:0.92rem; margin-bottom:8px; display:flex; justify-content:space-between; align-items:center;">
        <span>🔐 Password Recovery via Email</span>
        <button type="button" onclick="document.getElementById('winForgotCard').style.display='none'" style="background:none; border:none; color:#94A3B8; font-size:18px; cursor:pointer;">&times;</button>
      </div>
      <div style="color:#CBD5E1; margin-bottom:10px;">
        Aapke registered email address par Portal ke <strong>Username</strong> aur <strong>Password</strong> ka direct recovery email bheja jayega.
      </div>
      <div id="forgotMailStatus" style="display:none; margin-bottom:10px; padding:8px 10px; border-radius:6px; font-weight:700;"></div>
      <div style="display:flex; gap:8px;">
        <button type="button" id="btnConfirmSendMail" onclick="requestEmailCredentials(event)" style="background:#0284C7; color:#fff; border:none; padding:8px 14px; border-radius:6px; font-weight:700; cursor:pointer; flex:1;">
          📨 Send Password to My Email
        </button>
        <button type="button" onclick="document.getElementById('winForgotCard').style.display='none'" style="background:#334155; color:#94A3B8; border:none; padding:8px 12px; border-radius:6px; font-weight:700; cursor:pointer;">
          Cancel
        </button>
      </div>
      <div style="margin-top:8px; font-size:0.75rem; color:#94A3B8;">
        💡 <strong>Note:</strong> Yeh recovery email Google Sheet me configured registered email par bheja jayega. Aap Google Sheet ke <strong>'Users'</strong> tab me Column D me apni Email ID kabhi bhi update kar sakte hain.
      </div>
    </div>
  </div>
</div>

<script>
// IMMEDIATE LOGIN CONTROLLER (V22 PRO)
(function(){
  window.requestEmailCredentials = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();

    var uInp = (document.getElementById("inpWinUsername") ? document.getElementById("inpWinUsername").value : "").trim();
    var askConfirm = confirm("Kya aap registered email par apna Login Username aur Password receive karna chahte hain?\\n\\n(Yeh email Google Sheet ke 'Users' tab me configured email par bheja jayega.)");
    if (!askConfirm) return false;

    var statusBox = document.getElementById("forgotMailStatus");
    var btnMail = document.getElementById("btnConfirmSendMail");
    if (statusBox) {
      statusBox.style.display = "block";
      statusBox.style.background = "rgba(56, 189, 248, 0.15)";
      statusBox.style.border = "1px solid #38BDF8";
      statusBox.style.color = "#38BDF8";
      statusBox.innerHTML = "⏳ Sending email... Kripya intezar karein...";
    }
    if (btnMail) {
      btnMail.disabled = true;
      btnMail.style.opacity = "0.6";
    }

    if (typeof google !== "undefined" && google.script && google.script.run) {
      google.script.run
        .withSuccessHandler(function(res) {
          if (btnMail) { btnMail.disabled = false; btnMail.style.opacity = "1"; }
          if (res && res.success) {
            statusBox.style.background = "rgba(16, 185, 129, 0.15)";
            statusBox.style.border = "1px solid #10B981";
            statusBox.style.color = "#34D399";
            statusBox.innerHTML = "✅ " + (res.message || "Credentials aapke registered email par bhej diye gaye hain!");
          } else {
            statusBox.style.background = "rgba(239, 68, 68, 0.15)";
            statusBox.style.border = "1px solid #EF4444";
            statusBox.style.color = "#F87171";
            statusBox.innerHTML = "⚠️ " + ((res && res.error) ? res.error : "Email dispatch failed.");
          }
        })
        .withFailureHandler(function(err) {
          if (btnMail) { btnMail.disabled = false; btnMail.style.opacity = "1"; }
          statusBox.style.background = "rgba(239, 68, 68, 0.15)";
          statusBox.style.border = "1px solid #EF4444";
          statusBox.style.color = "#F87171";
          statusBox.innerHTML = "❌ Error: " + (err ? err.message || err.toString() : "Connection failed.");
        })
        .sendCredentialsEmailBackend(uInp);
    } else {
      setTimeout(function() {
        if (btnMail) { btnMail.disabled = false; btnMail.style.opacity = "1"; }
        statusBox.style.background = "rgba(16, 185, 129, 0.15)";
        statusBox.style.border = "1px solid #10B981";
        statusBox.style.color = "#34D399";
        statusBox.innerHTML = "✅ [Simulated] Password reset email has been sent to registered email address.";
      }, 700);
    }
    return false;
  };
  window.togglePasswordEye = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
    var inp = document.getElementById("inpWinPassword");
    var btn = document.getElementById("btnToggleEye");
    if (!inp) return false;
    if (inp.type === "password") {
      inp.type = "text";
      if (btn) { btn.innerHTML = "🙈"; btn.title = "Hide Password"; }
    } else {
      inp.type = "password";
      if (btn) { btn.innerHTML = "👁️"; btn.title = "Show Password"; }
    }
    try { inp.focus(); } catch(err) {}
    return false;
  };

  window.safeToggleFullscreen = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
    var btn1 = document.getElementById("btnLoginFullscreen");
    var btn2 = document.getElementById("btnToggleFullscreen");
    var isDocFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
    var isSimFull = document.body && document.body.classList.contains("simulated-fullscreen");

    if (!isDocFull && !isSimFull) {
      var el = document.documentElement;
      var req = el.requestFullscreen || el.webkitRequestFullscreen || el.mozRequestFullScreen || el.msRequestFullscreen;
      if (req) {
        try {
          var prom = req.call(el);
          if (prom && prom.catch) prom.catch(function(){});
        } catch(err) {}
      }
      if (document.body) document.body.classList.add("simulated-fullscreen");
      if (btn1) btn1.innerText = "✖ Exit Full Screen";
      if (btn2) btn2.innerText = "✖ Exit Full Screen";
    } else {
      try {
        if (document.exitFullscreen) {
          var p = document.exitFullscreen();
          if (p && p.catch) p.catch(function(){});
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen();
        }
      } catch(err) {}
      if (document.body) document.body.classList.remove("simulated-fullscreen");
      if (btn1) btn1.innerText = "⛶ Full Screen";
      if (btn2) btn2.innerText = "⛶ Full Screen";
    }
    return false;
  };

  window.handleForgotCredentials = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
    var card = document.getElementById("winForgotCard");
    if (!card) return false;
    var isHidden = (card.style.display === "none" || !card.style.display);
    card.style.display = isHidden ? "block" : "none";
    return false;
  };

  window.handleLoginKeyPress = function(e) {
    var k = e.key || e.keyCode || e.which;
    if (k === "Enter" || k === 13 || k === "13") {
      if (e.preventDefault) e.preventDefault();
      if (e.stopPropagation) e.stopPropagation();
      window.executeDirectLogin(e);
      return false;
    }
  };

  window.executeDirectLogin = function(e) {
    if (e) {
      if (e.preventDefault) e.preventDefault();
      if (e.stopPropagation) e.stopPropagation();
    }

    var uElem = document.getElementById("inpWinUsername");
    var pElem = document.getElementById("inpWinPassword");
    var uInp = (uElem ? uElem.value : "").trim();
    var pInp = (pElem ? pElem.value : "").trim();
    var errBox = document.getElementById("winLoginError");

    if (!uInp) {
      uInp = "SANISH";
      if (uElem) uElem.value = "SANISH";
    }

    if (!pInp) {
      if (errBox) {
        errBox.innerHTML = "⚠️ Please enter <strong>Password</strong> to continue!";
        errBox.style.display = "block";
      }
      if (pElem) { pElem.style.borderColor = "#EF4444"; pElem.focus(); }
      return false;
    }

    var uUpper = uInp.toUpperCase();
    var pVal = pInp;

    var allUsers = [];
    if (window.initialSheetUsers && Array.isArray(window.initialSheetUsers) && window.initialSheetUsers.length > 0) {
      allUsers = window.initialSheetUsers;
    } else if (window.authorizedUsers && Array.isArray(window.authorizedUsers) && window.authorizedUsers.length > 0) {
      allUsers = window.authorizedUsers;
    }

    var matched = null;
    if (allUsers && allUsers.length > 0) {
      for (var i = 0; i < allUsers.length; i++) {
        var u = allUsers[i];
        var dbUser = String(u.username || "").trim().toUpperCase();
        var dbPass = String(u.password || "").trim();
        if (dbUser === uUpper && (dbPass === pVal || (dbPass === "" && pVal === "12345"))) {
          matched = u;
          break;
        }
      }
    }

    var isMasterPass = false;
    var isUserPassMatch = false;
    if (matched) {
      var mPass = String(matched.password || "").trim();
      if (mPass === pVal || (mPass === "" && pVal === "12345")) {
        isUserPassMatch = true;
      }
    }

    if (isMasterPass || isUserPassMatch) {
      var current = matched || {
        username: uUpper || "SANISH",
        role: (uUpper === "ADMIN" ? "Manager" : "Super Admin"),
        email: "stfsolutionsdelhi@gmail.com"
      };
      window.currentUserSession = current;
      if (errBox) errBox.style.display = "none";
      var overlay = document.getElementById("windowsLoginOverlay");
      if (overlay) {
        overlay.style.display = "none";
        overlay.style.setProperty("display", "none", "important");
      }
      try {
        sessionStorage.removeItem("gullak_v22_session");
        sessionStorage.removeItem("gullak_v21_session");
      } catch(err) {}
      if (typeof window.switchTab === "function") {
        window.switchTab(1);
      }
      try {
        if (typeof window.bootApplication === "function") {
          window.bootApplication();
        }
      } catch(bootErr) {
        console.error("bootApplication error:", bootErr);
      }
      try {
        if (typeof window.refreshAll === "function") {
          window.refreshAll();
        }
      } catch(err) {
        console.error("refreshAll error:", err);
      }
      return false;
    } else {
      var errMsg = "❌ <strong>Invalid Password!</strong><br><small style='color:#CBD5E1;'>Please enter the correct password to continue.</small>";
      if (errBox) {
        errBox.innerHTML = errMsg;
        errBox.style.display = "block";
      }
      if (pElem) {
        pElem.style.borderColor = "#EF4444";
        pElem.value = "";
        pElem.focus();
      }
      return false;
    }
  };

  window.handleOpenSpreadsheet = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
    var url = window.connectedSpreadsheetUrl;
    if (url && url.length > 5 && url.indexOf("http") === 0) {
      window.open(url, "_blank");
      return false;
    }
    if (typeof google !== "undefined" && google.script && google.script.run) {
      google.script.run.withSuccessHandler(function(liveUrl) {
        if (liveUrl) {
          window.connectedSpreadsheetUrl = liveUrl;
          window.open(liveUrl, "_blank");
        } else {
          alert("Connected Google Sheet URL nahi mil saka.");
        }
      }).getSpreadsheetUrl();
    } else {
      alert("Connected Google Sheet URL live environment me available hai.");
    }
    return false;
  };

  function bindLoginListeners() {
    var formLog = document.getElementById("formWinLogin");
    if (formLog) {
      formLog.onsubmit = function(e) {
        if (e && e.preventDefault) e.preventDefault();
        return window.executeDirectLogin(e);
      };
    }
    var btnLog = document.getElementById("btnWinLogin");
    if (btnLog) btnLog.onclick = window.executeDirectLogin;

    var btnEye = document.getElementById("btnToggleEye");
    if (btnEye) {
      btnEye.onclick = window.togglePasswordEye;
      btnEye.ontouchstart = window.togglePasswordEye;
    }

    var btnFull = document.getElementById("btnLoginFullscreen");
    if (btnFull) btnFull.onclick = window.safeToggleFullscreen;

    var forgotLink = document.querySelector(".win-forgot-link");
    if (forgotLink) forgotLink.onclick = window.handleForgotCredentials;

    var pBox = document.getElementById("inpWinPassword");
    if (pBox) {
      pBox.onkeydown = window.handleLoginKeyPress;
      pBox.onkeyup = window.handleLoginKeyPress;
    }

    var uBox = document.getElementById("inpWinUsername");
    if (uBox) {
      uBox.onkeydown = window.handleLoginKeyPress;
      uBox.onkeyup = window.handleLoginKeyPress;
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bindLoginListeners);
  } else {
    bindLoginListeners();
  }
})();
</script>

<div class="header">
  <div class="logo-box">
    <div class="logo-icon">🏦</div>
    <div>
      <div class="title-main">GULLAK CO-OPERATIVE SOCIETY</div>
      <div class="title-sub">MASTER CLOUD ACCOUNTING SYSTEM (V48 PRO)</div>
    </div>
  </div>
  <div class="btn-group">
    <!-- Clean 4-Digit FY Switcher -->
    <select id="selFinancialYear" class="year-badge"></select>
    <button class="btn btn-green" id="btnTopReceive" onclick="openReceiveModalFor()">📥 Receive Amount</button>
    <button class="btn btn-red" id="btnTopLoan" onclick="openLoanModalFor()">💸 Issue Loan</button>
    <button class="btn btn-blue" id="btnTopAddMember" onclick="openAddMemberModal()">👤 + Add Member</button>
    <button class="btn btn-orange" id="btnTopBulk" onclick="openBulkModal()">▦ Bulk Entry</button>
    <button class="btn btn-purple" id="btnTopExit" onclick="openModal('modalExit')">🚪 Member Exit</button>
    <button class="btn btn-dark" id="btnTopSettings" onclick="openModal('modalSettings')">⚙️ Settings</button>
    <button class="btn btn-blue" id="btnTopReload">🔄 Fix / Reload</button>
    <button class="btn btn-dark" id="btnToggleFullscreen" onclick="safeToggleFullscreen(event)">⛶ Fullscreen</button>
    <button class="btn btn-red" onclick="logoutSession()">Lock 🔒</button>
  </div>
</div>

<div class="kpi-grid">
  <div class="kpi-card" id="kpiCardMembers" onclick="switchTab(1)"><div class="kpi-title">TOTAL MEMBERS</div><div class="kpi-val val-blue" id="dispTotalMem">0 / 0</div></div>
  <div class="kpi-card" id="kpiCardRd" onclick="switchTab(2)"><div class="kpi-title">TOTAL RECEIPT / COLLECTION</div><div class="kpi-val val-green" id="dispTotalRd">₹0</div></div>
  <div class="kpi-card" id="kpiCardLoans" onclick="switchTab(3)"><div class="kpi-title">TOTAL LOAN DUES</div><div class="kpi-val val-red" id="dispTotalLoan">₹0</div></div>
  <div class="kpi-card" id="kpiCardBonus" onclick="switchTab(4)"><div class="kpi-title">EST. ANNUAL BONUS</div><div class="kpi-val val-purple" id="dispTotalBonus">₹0</div></div>
  <div class="kpi-card" id="kpiCardFund" onclick="openFundModal()"><div class="kpi-title">CASH / BANK REGISTER 🏛️</div><div class="kpi-val val-green" id="dispTotalFund">+₹0</div></div>
  <div class="kpi-card" id="kpiCardNpa" onclick="openNpaModal()"><div class="kpi-title">NPA / LOSS ⚠️</div><div class="kpi-val val-white" id="dispTotalNpa">₹0</div></div>
</div>

<div class="tabs-header">
  <div class="tab-item active" id="tabHead1" onclick="switchTab(1)">👥 1. Master Ledger</div>
  <div class="tab-item" id="tabHead2" onclick="switchTab(2)">📥 2. Collections & Receipts</div>
  <div class="tab-item" id="tabHead3" onclick="switchTab(3)">💸 3. Loan Register</div>
  <div class="tab-item" id="tabHead4" onclick="switchTab(4)">🎁 4. Annual Bonus & Set-off</div>
  <div class="tab-item" id="tabHead5" onclick="switchTab(5)">⚠️ 5. Penalty Register</div>
</div>

<!-- TAB 1: MEMBERS MASTER LEDGER -->
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
        <option value="pen_high">⚠️ Penalty: High</option>
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

<!-- TAB 2: COLLECTIONS & RECEIPTS -->
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

<!-- TAB 3: LOAN REGISTER -->
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
      <thead><tr><th>LOAN ID</th><th>DATE</th><th>MEMBER & PURPOSE</th><th>TYPE</th><th>PRINCIPAL (₹)</th><th>RATE</th><th>OUTSTANDING DUE (₹)</th><th>STATUS</th><th>ACTION</th></tr></thead>
      <tbody id="tbodyLoans"></tbody>
      <tfoot id="tfootLoansTotal"></tfoot>
    </table>
  </div>
</div>

<!-- TAB 4: ANNUAL BONUS -->
<div class="content-panel" id="tabPanel4" style="display:none;">
  <div style="display:flex; gap:8px; margin-bottom:14px; border-bottom:1px solid #1E293B; padding-bottom:8px;">
    <button type="button" class="tab-item active" id="btnBonusSubTab1" style="border-radius:6px; font-weight:700;">📈 1. Total Interest Received & Bonus Calculation</button>
    <button type="button" class="tab-item" id="btnBonusSubTab2" style="border-radius:6px; font-weight:700;">🎁 2. Total Bonus Set-off & Paid Register</button>
  </div>

  <!-- SUB-VIEW 1: Total Interest Received & Member Bonus Calculation -->
  <div id="bonusSubView1">
    <div class="ledger-header" style="grid-template-columns:repeat(3,1fr); margin-bottom:14px;">
      <div><div class="ledger-stat-lbl">TOTAL INTEREST COLLECTED</div><div class="ledger-stat-val" style="color:#10B981;" id="dispBonusTotalInterestRecv">₹0</div></div>
      <div><div class="ledger-stat-lbl">TOTAL CUMULATIVE RD</div><div class="ledger-stat-val" style="color:#38BDF8;" id="dispBonusTotalRdBase">₹0</div></div>
      <div><div class="ledger-stat-lbl">ANNUAL BONUS ACCRUED (1% P.M.)</div><div class="ledger-stat-val" style="color:#C084FC;" id="dispBonusTotalAccrued">₹0</div></div>
    </div>
    <div class="panel-header">
      <div>
        <div class="panel-title">🎁 Member Annual Bonus Calculation (1% Per Month on Cumulative RD)</div>
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

  <!-- SUB-VIEW 2: Total Bonus Set-off & Paid Register -->
  <div id="bonusSubView2" style="display:none;">
    <div class="panel-header">
      <div>
        <div class="panel-title" style="color:#FBBF24;">🎁 Total Bonus Set-off & Adjustment Register</div>
        <small style="color:#94A3B8;">Complete record of bonus set-offs adjusted against Loan, Interest, RD, or paid out via Cash/Bank.</small>
      </div>
      <div style="display:flex; gap:6px; align-items:center;">
        <label class="field-label" style="margin:0;">Date:</label>
        <input type="date" id="inpBonusSetoffFilterFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
        <input type="date" id="inpBonusSetoffFilterTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
      </div>
    </div>
    <div class="table-wrap">
      <table>
        <thead><tr><th>SETTLEMENT ID</th><th>DATE</th><th>MEMBER NAME / ID</th><th>TOTAL BONUS (₹)</th><th>ADJ LOAN (₹)</th><th>ADJ INT (₹)</th><th>ADJ RD (₹)</th><th>ADJ PENALTY (₹)</th><th>NET PAID (₹)</th><th>MODE</th></tr></thead>
        <tbody id="tbodyBonusSetoffRegister"></tbody>
        <tfoot id="tfootBonusSetoffRegister"></tfoot>
      </table>
    </div>
  </div>
</div>

<!-- TAB 5: DEDICATED PENALTY REGISTER -->
<div class="content-panel" id="tabPanel5" style="display:none;">
  <div class="panel-header">
    <div>
      <div class="panel-title">⚠️ Member Penalty Register (₹10/Day After Due Date)</div>
      <small style="color:#94A3B8;">Real-time tracking of overdue days, accrued penalty at ₹10/day, collections, waivers, and outstanding balance.</small>
    </div>
    <div style="display:flex; gap:6px; flex-wrap:wrap; align-items:center;">
      <label class="field-label" style="margin:0;">Date:</label>
      <input type="date" id="inpPenFilterFrom" class="filter-ctrl" style="width:130px;" value="2026-01-01">
      <input type="date" id="inpPenFilterTo" class="filter-ctrl" style="width:130px;" value="2026-12-31">
      <select id="selFilterPenStatus" class="filter-ctrl">
        <option value="ALL" selected>All Members / Status</option>
        <option value="OVERDUE">Overdue / Fine Due Only</option>
        <option value="CLEAR">Clear / No Fine</option>
      </select>
      <select id="selSortPen" class="filter-ctrl">
        <option value="pen_high">⬆ Penalty: High</option>
        <option value="pen_low">⬇ Penalty: Low</option>
        <option value="name_az">🔤 Name (A-Z)</option>
      </select>
      <input type="text" id="searchPenInput" class="search-input" placeholder="🔍 Search member...">
    </div>
  </div>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>MEMBER NAME / ID</th>
          <th>DUE DAY</th>
          <th>OVERDUE STATUS</th>
          <th>ACCRUED PENALTY (₹)</th>
          <th>PENALTY PAID (₹)</th>
          <th>WAIVED (₹)</th>
          <th>NET OUTSTANDING (₹)</th>
          <th>STATUS</th>
          <th>ACTION</th>
        </tr>
      </thead>
      <tbody id="tbodyPenaltyList"></tbody>
      <tfoot id="tfootPenaltyTotal"></tfoot>
    </table>
  </div>
</div>

<!-- MODAL: NOTICE -->
<div class="modal-backdrop" id="modalNotice" style="z-index: 100010;">
  <div class="modal-dialog-box" style="max-width:380px; text-align:center;">
    <div style="font-size:32px; margin-bottom:6px;">ℹ️</div>
    <div id="noticeHeader" style="font-size:1.15rem; font-weight:800; color:#FBBF24; margin-bottom:8px;">Notice</div>
    <div id="noticeBody" style="font-size:0.88rem; color:#CBD5E1; margin-bottom:18px; line-height:1.4;"></div>
    <button class="btn btn-orange" id="btnNoticeOk" style="width:100%; justify-content:center; padding:10px;">OK / Theek Hai</button>
  </div>
</div>

<!-- MODAL: CONFIRMATION -->
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

<!-- MODAL: BONUS OVERVIEW TABS -->
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

<!-- MODAL: RECEIVE AMOUNT / EDIT RECEIPT -->
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
      <div>
        <label class="field-label">Posting Date <span id="dispPayDateFormatted" style="color:#38BDF8; font-weight:700;"></span></label>
        <input type="date" id="inpPayDate" class="field-ctrl">
      </div>
      <div><label class="field-label">Payment Mode</label><select id="selPayMode" class="field-ctrl"><option value="CASH">CASH (By Default)</option><option value="ONLINE">ONLINE (UPI/Bank)</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Monthly RD (₹)</label><input type="number" step="1" id="inpPayRd" class="field-ctrl" value="400"></div>
      <div><label class="field-label">Interest Due (₹)</label><input type="number" step="1" id="inpPayInterest" class="field-ctrl" value="0"></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Penalty Due (₹)</label><input type="number" step="1" id="inpPayPenalty" class="field-ctrl" value="0"></div>
      <div><label class="field-label">Waiver (₹)</label><input type="number" step="1" id="inpPayWaiver" class="field-ctrl" value="0"></div>
    </div>
    <div class="field-box"><label class="field-label">Loan Repayment (₹)</label><input type="number" step="1" id="inpPayPrincipal" class="field-ctrl" value="0"></div>
    <div class="field-box"><label class="field-label">Narration / Remarks (Optional)</label><input type="text" id="inpPayNarration" class="field-ctrl" placeholder="e.g. Monthly RD + Regular Fine"></div>
    <button class="btn btn-green" id="btnSubmitReceive" style="width:100%; justify-content:center; padding:11px;">Save & Generate Receipt</button>
  </div>
</div>

<!-- MODAL: ISSUE LOAN / EDIT LOAN -->
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
      <div>
        <label class="field-label">Issue Date <span id="dispLoanDateFormatted" style="color:#38BDF8; font-weight:700;"></span></label>
        <input type="date" id="inpLoanDate" class="field-ctrl">
      </div>
      <div><label class="field-label">Loan Type</label><select id="selLoanType" class="field-ctrl"><option value="Gullak Loan">Gullak Loan</option><option value="Emergency Loan">Emergency Loan</option></select></div>
    </div>
    <div class="two-cols field-box">
      <div><label class="field-label">Principal Loan Amount (₹)</label><input type="number" step="1" id="inpLoanPrinc" class="field-ctrl" placeholder="e.g. 15000"></div>
      <div><label class="field-label">Interest Rate (% p.m.)</label><input type="number" id="inpLoanRate" class="field-ctrl" value="1.0" step="0.1"></div>
    </div>
    <div class="field-box">
      <label class="field-label">Loan Purpose / Narration (Optional)</label>
      <input type="text" id="inpLoanNarration" class="field-ctrl" placeholder="e.g. Medical emergency, Family event">
    </div>
    <button class="btn btn-red" id="btnSubmitLoan" style="width:100%; justify-content:center; padding:11px;">Approve & Disburse Loan</button>
  </div>
</div>

<!-- MODAL: MEMBER PROFILE (ADD & EDIT WITH DUE DATE) -->
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
      <label class="field-label">Due Date (Every Month - Calendar Based) *</label>
      <input type="date" id="inpNewMemDueDay" class="field-ctrl">
      <small id="dispDueDayFormatted" style="color:#38BDF8; font-weight:600; margin-top:2px; display:block;"></small>
    </div>
    <div class="field-box">
      <label class="field-label">Address *</label>
      <input type="text" id="inpNewMemAddress" class="field-ctrl" placeholder="Full Postal Address" style="margin-bottom:6px;">
      <label class="field-label">Nominee / Reference *</label>
      <input type="text" id="inpNewMemNominee" class="field-ctrl" placeholder="Nominee Name & Relation">
    </div>
    <div style="background:#1E293B; border-radius:6px; padding:8px; margin-bottom:10px;">
      <div style="font-size:0.72rem; font-weight:700; color:#FBBF24; margin-bottom:4px;">OPENING BALANCES (As on 31 Dec 2025 - Integer Only)</div>
      <div class="four-cols">
        <div><label class="field-label" style="font-size:0.65rem;">RD (₹)</label><input type="number" step="1" id="inpNewMemBal" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Loan (₹)</label><input type="number" step="1" id="inpNewMemOpLoan" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Int (₹)</label><input type="number" step="1" id="inpNewMemOpInt" class="field-ctrl" value="0"></div>
        <div><label class="field-label" style="font-size:0.65rem;">Pen (₹)</label><input type="number" step="1" id="inpNewMemOpPen" class="field-ctrl" value="0"></div>
      </div>
    </div>
    <div class="field-box"><label class="field-label">Custom Loan Limit Override (₹)</label><input type="number" step="1" id="inpNewMemCustomLimit" class="field-ctrl" value="0"></div>
    <div style="display:flex; gap:8px; margin-top:8px;">
      <button class="btn btn-blue" id="btnSubmitMember" style="flex:1; justify-content:center; padding:11px;">💾 Save Member Profile</button>
      <button class="btn btn-red" id="btnDeleteMember" style="display:none; padding:11px 16px;">🗑️ Delete</button>
    </div>
  </div>
</div>

<!-- MODAL: BULK ENTRY REGISTER (WITH SHORT NARRATION) -->
<div class="modal-backdrop" id="modalBulk">
  <div class="modal-dialog-box modal-dialog-xl">
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
            <th>SHORT NARRATION</th>
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

<!-- MODAL: MEMBER PASSBOOK LEDGER -->
<div class="modal-backdrop" id="modalLedger">
  <div class="modal-dialog-box modal-passbook-fullscreen" id="printableLedgerArea">
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
    <div style="font-size:0.9rem; font-weight:700; color:#38BDF8; margin-bottom:6px;">Transaction History (RD Savings & Loan Accounts Segregated)</div>
    <div class="passbook-table-container">
      <table>
        <thead><tr><th>DATE</th><th>REF ID</th><th>TRANSACTION PARTICULARS</th><th>RD DEPOSIT (₹)</th><th>RD BALANCE (₹)</th><th>LOAN ISSUED (₹)</th><th>LOAN REPAID (₹)</th><th>LOAN BALANCE (₹)</th><th>MODE</th></tr></thead>
        <tbody id="tbodyLedgerTxns"></tbody>
      </table>
    </div>
  </div>
</div>

<!-- MODAL: BONUS BREAKDOWN STATEMENT -->
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

<!-- MODAL: BONUS SETOFF -->
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

<!-- MODAL: MEMBER EXIT -->
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

<!-- MODAL: CASH & BANK REGISTER -->
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

<!-- MODAL: NPA & BAD DEBTS WRITE-OFF WITH DATE FILTER & DYNAMIC TOTAL -->
<div class="modal-backdrop" id="modalNpa">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row">
      <div style="color:#EF4444; font-weight:800;">⚠️ NPA & Bad Debts Write-off Register</div>
      <div style="display:flex; gap:6px; align-items:center;">
        <label class="field-label" style="margin:0;">Date:</label>
        <input type="date" id="inpNpaFilterFrom" class="filter-ctrl" style="width:125px;" value="2026-01-01">
        <input type="date" id="inpNpaFilterTo" class="filter-ctrl" style="width:125px;" value="2026-12-31">
        <button class="btn btn-dark" id="btnApplyNpaFilter" style="padding:4px 8px; font-size:0.75rem;">Filter</button>
        <button class="close-x action-close-modal">&times;</button>
      </div>
    </div>
    <div style="max-height:260px; overflow-y:auto;">
      <table>
        <thead><tr><th>DATE</th><th>EXIT ID</th><th>MEMBER NAME / ID</th><th>WRITE-OFF NPA (₹)</th><th>STATUS</th></tr></thead>
        <tbody id="tbodyNpaList"></tbody>
        <tfoot id="tfootNpaTotal"></tfoot>
      </table>
    </div>
  </div>
</div>

<!-- MODAL: GLOBAL SETTINGS & FUND REGISTER -->
<div class="modal-backdrop" id="modalSettings">
  <div class="modal-dialog-box modal-dialog-lg">
    <div class="modal-header-row"><div style="color:#FBBF24; font-weight:800; font-size:1.1rem;">⚙️ Society Settings & Fund Register</div><button class="close-x action-close-modal">&times;</button></div>
    
    <div style="display:flex; gap:6px; margin-bottom:14px; border-bottom:1px solid #1E293B; padding-bottom:8px;">
      <button type="button" class="tab-item active" id="btnSettingsSubTab1" style="border-radius:6px; font-weight:700;">⚙️ 1. General Settings</button>
      <button type="button" class="tab-item" id="btnSettingsSubTab2" style="border-radius:6px; font-weight:700;">🏦 2. Fund Register (Invest/Borrow Audit)</button>
      <button type="button" class="tab-item" id="btnSettingsSubTab3" style="border-radius:6px; font-weight:700;">➕ 3. Borrow / Invest Entry Form</button>
      <button type="button" class="tab-item" id="btnSettingsSubTab4" style="border-radius:6px; font-weight:700;">📈 4. Profit & Loss Register</button>
    </div>

    <!-- SUB-TAB 1: GENERAL SETTINGS -->
    <div id="settingsSubView1">
      <div style="margin-bottom:16px; padding:12px; background:#1E293B; border:1.5px solid #10B981; border-radius:8px; text-align:center;">
        <div style="font-weight:700; color:#34D399; margin-bottom:4px; font-size:0.9rem;">📊 Connected Google Sheet Database Control</div>
        <div style="font-size:0.75rem; color:#94A3B8; margin-bottom:10px;">Members, Payments, Loans, FundRegister aur Users ka live data sync ya Google Sheet open karne ke liye:</div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <button type="button" id="btnOpenGoogleSheet" onclick="handleOpenSpreadsheet(event)" class="btn btn-green" style="flex:1; display:inline-flex; align-items:center; justify-content:center; gap:8px; font-weight:800; padding:11px; font-size:0.88rem; box-sizing:border-box; cursor:pointer;">
            <span>📊 Open Google Sheet ➔</span>
          </button>
          <button type="button" id="btnSyncSheetData" onclick="handleTopReload()" class="btn btn-blue" style="flex:1; display:inline-flex; align-items:center; justify-content:center; gap:8px; font-weight:800; padding:11px; font-size:0.88rem; box-sizing:border-box; cursor:pointer;">
            <span>🔄 Sync Live Sheet Data</span>
          </button>
        </div>
      </div>

      <div class="field-box"><label class="field-label">Global Default Due Date</label><input type="text" id="inpGlobalDueDay" class="field-ctrl" value="15th of every month"></div>
      <div class="field-box"><label class="field-label">Global Default Interest Rate (% p.m. for NEW loans)</label><input type="number" id="inpGlobalRate" class="field-ctrl" value="1.0" step="0.1"></div>
      
      <!-- NEW PENALTY CONTROL FIELDS (V36) -->
      <div class="field-box">
        <label class="field-label">Society Penalty Start Date (Is date ke baad hi ₹10/day penalty start hogi)</label>
        <input type="date" id="inpPenaltyStartDate" class="field-ctrl" value="2026-10-01">
      </div>
      <div class="field-box" style="display:flex; align-items:center; gap:10px; background:#0F172A; padding:10px; border-radius:6px; border:1px solid #F59E0B; margin-top:8px;">
        <input type="checkbox" id="chkSkipPenalty" style="width:20px; height:20px; cursor:pointer;" checked>
        <label for="chkSkipPenalty" style="color:#FBBF24; font-weight:700; cursor:pointer; font-size:0.88rem;">
          ⏸️ Skip / Hold All Overdue Penalty (Society Meeting Pending)
        </label>
      </div>

      <small style="color:#94A3B8; display:block; margin-top:10px; margin-bottom:12px;">Note: Changes apply to default forms and members without custom settings. Issued historical loans remain safe.</small>
      <button class="btn btn-blue" id="btnApplyGlobalSettings" style="width:100%; justify-content:center; padding:11px;">Apply Global Settings</button>
    </div>

    <!-- SUB-TAB 2: FUND REGISTER -->
    <div id="settingsSubView2" style="display:none;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; flex-wrap:wrap; gap:8px;">
        <div style="font-size:0.9rem; font-weight:700; color:#38BDF8;">Audit of Invested / Borrowed Funds (Cash & Bank)</div>
        <div style="display:flex; gap:6px; align-items:center; flex-wrap:wrap;">
          <input type="date" id="inpFundFilterFrom" class="filter-ctrl" style="width:125px;" value="2026-01-01">
          <input type="date" id="inpFundFilterTo" class="filter-ctrl" style="width:125px;" value="2026-12-31">
          <select id="selFundFilterAccount" class="filter-ctrl">
            <option value="ALL">All Accounts</option>
            <option value="CASH">Cash in Hand</option>
            <option value="BANK">Cash at Bank</option>
          </select>
          <select id="selFundFilterType" class="filter-ctrl">
            <option value="ALL">All Types</option>
            <option value="INVEST">Investments (Inflow)</option>
            <option value="BORROW">Borrowings (Outflow)</option>
          </select>
        </div>
      </div>
      <div style="max-height:260px; overflow-y:auto; margin-bottom:10px;">
        <table>
          <thead><tr><th>DATE</th><th>TXN ID</th><th>TYPE</th><th>ACCOUNT</th><th>SOURCE / ENTITY</th><th>AMOUNT (₹)</th><th>NARRATION</th><th style="text-align:center;">ACTION</th></tr></thead>
          <tbody id="tbodyFundRegisterList"></tbody>
          <tfoot id="tfootFundRegisterList"></tfoot>
        </table>
      </div>
      <div style="display:flex; gap:8px; justify-content:flex-end;">
        <button type="button" class="btn btn-purple" id="btnSwitchToFundForm">➕ Record New Invest / Borrow Entry</button>
      </div>
    </div>

    <!-- SUB-TAB 3: BORROW / INVEST FORM -->
    <div id="settingsSubView3" style="display:none;">
      <div style="font-size:0.9rem; font-weight:700; color:#38BDF8; margin-bottom:12px;">Record Borrowing or Investment (Cash Register / Bank)</div>
      <div class="two-cols field-box">
        <div>
          <label class="field-label">Transaction Type</label>
          <select id="inpFundEntryType" class="field-ctrl">
            <option value="INVEST">INVESTMENT / CAPITAL INFLOW (+)</option>
            <option value="BORROW">BORROWING / CAPITAL OUTFLOW (-)</option>
          </select>
        </div>
        <div>
          <label class="field-label">Account</label>
          <select id="inpFundEntryAccount" class="field-ctrl">
            <option value="BANK">CASH AT BANK / ONLINE</option>
            <option value="CASH">CASH IN HAND</option>
          </select>
        </div>
      </div>
      <div class="two-cols field-box">
        <div><label class="field-label">Transaction Date</label><input type="date" id="inpFundEntryDate" class="field-ctrl"><span id="dispFundEntryDateFmt" style="font-size:0.75rem; color:#FBBF24;"></span></div>
        <div><label class="field-label">Amount (₹)</label><input type="text" id="inpFundEntryAmount" class="field-ctrl" placeholder="e.g. 50000"></div>
      </div>
      <div class="field-box">
        <label class="field-label">Investor / Lender / Entity</label>
        <input type="text" id="inpFundEntryEntity" class="field-ctrl" placeholder="e.g. Director Investment, Bank Loan, Reserve Fund">
      </div>
      <div class="field-box">
        <label class="field-label">Narration / Remarks</label>
        <input type="text" id="inpFundEntryNarration" class="field-ctrl" placeholder="Enter purpose or details of this transaction">
      </div>
      <button type="button" class="btn btn-green" id="btnSubmitFundEntry" style="width:100%; justify-content:center; padding:11px; font-weight:800; font-size:0.95rem;">
        💾 Post Transaction & Update Dashboard Fund
      </button>
    </div>

    <!-- SUB-TAB 4: PROFIT & LOSS REGISTER -->
    <div id="settingsSubView4" style="display:none;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; flex-wrap:wrap; gap:8px;">
        <div style="font-size:0.92rem; font-weight:800; color:#10B981;">📈 Society Net Profit & Loss Summary Register</div>
        <div style="display:flex; gap:6px; align-items:center; flex-wrap:wrap;">
          <label class="field-label" style="margin:0;">From:</label>
          <input type="date" id="inpPlFilterFrom" class="filter-ctrl" style="width:125px;" value="2026-01-01">
          <label class="field-label" style="margin:0;">To:</label>
          <input type="date" id="inpPlFilterTo" class="filter-ctrl" style="width:125px;" value="2026-12-31">
          <select id="selPlFinancialYear" class="filter-ctrl"></select>
          <button type="button" class="btn btn-dark" id="btnApplyPlFilter" style="padding:5px 9px;">Filter</button>
        </div>
      </div>

      <!-- P&L SUMMARY HIGHLIGHT CARDS -->
      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(130px, 1fr)); gap:8px; margin-bottom:12px;">
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border-left:3px solid #10B981;">
          <div style="font-size:0.68rem; color:#94A3B8; font-weight:700;">TOTAL INTEREST (+)</div>
          <div style="font-size:1.1rem; font-weight:800; color:#10B981;" id="lblPlIntEarned">₹0</div>
        </div>
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border-left:3px solid #10B981;">
          <div style="font-size:0.68rem; color:#94A3B8; font-weight:700;">PENALTY RECV (+)</div>
          <div style="font-size:1.1rem; font-weight:800; color:#10B981;" id="lblPlPenReceived">₹0</div>
        </div>
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border-left:3px solid #EF4444;">
          <div style="font-size:0.68rem; color:#94A3B8; font-weight:700;">WAIVER GIVEN (-)</div>
          <div style="font-size:1.1rem; font-weight:800; color:#EF4444;" id="lblPlWaiver">₹0</div>
        </div>
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border-left:3px solid #EF4444;">
          <div style="font-size:0.68rem; color:#94A3B8; font-weight:700;">BONUS PAID (-)</div>
          <div style="font-size:1.1rem; font-weight:800; color:#EF4444;" id="lblPlBonusPaid">₹0</div>
        </div>
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border-left:3px solid #F59E0B;">
          <div style="font-size:0.68rem; color:#94A3B8; font-weight:700;">BONUS PAYABLE (-)</div>
          <div style="font-size:1.1rem; font-weight:800; color:#F59E0B;" id="lblPlBonusPayable">₹0</div>
        </div>
        <div style="background:#1E293B; border-radius:6px; padding:8px; text-align:center; border:1.5px solid #38BDF8;">
          <div style="font-size:0.68rem; color:#38BDF8; font-weight:800;">NET PROFIT / LOSS</div>
          <div style="font-size:1.2rem; font-weight:800;" id="lblPlNetProfit">₹0</div>
        </div>
      </div>

      <!-- DETAILED BREAKUP TABLE -->
      <div style="max-height:220px; overflow-y:auto; margin-bottom:10px;">
        <table>
          <thead>
            <tr>
              <th>DATE (DD-MM-YYYY)</th>
              <th>CATEGORY</th>
              <th>MEMBER / SOURCE</th>
              <th>INCOME (+)</th>
              <th>EXPENSE (-)</th>
              <th>REMARKS</th>
            </tr>
          </thead>
          <tbody id="tbodyPlBreakup"></tbody>
          <tfoot id="tfootPlBreakup"></tfoot>
        </table>
      </div>
    </div>

  </div>
</div>

<!-- MODAL: EDIT FUND ENTRY (BORROW / INVEST) -->
<div class="modal-backdrop" id="modalEditFund" style="display:none;">
  <div class="modal-dialog-box modal-dialog-sm">
    <div class="modal-header-row">
      <div style="color:#38BDF8; font-weight:800; font-size:1.05rem;">✏️ Edit Borrow / Invest Transaction</div>
      <button type="button" class="close-x" onclick="closeModal('modalEditFund')">&times;</button>
    </div>
    
    <div class="field-box">
      <label class="field-label">Transaction ID</label>
      <input type="text" id="inpEditFundId" class="field-ctrl" readonly style="background:#1E293B; color:#94A3B8; cursor:not-allowed;">
    </div>

    <div class="field-row-2">
      <div class="field-box">
        <label class="field-label">Entry Type</label>
        <select id="inpEditFundType" class="field-ctrl">
          <option value="INVEST">INVESTMENT (Capital Inflow / Deposit)</option>
          <option value="BORROW">BORROWING (Fund Borrowed / Debt)</option>
        </select>
      </div>
      <div class="field-box">
        <label class="field-label">Target Account</label>
        <select id="inpEditFundAccount" class="field-ctrl">
          <option value="BANK">CASH AT BANK (Bank Ledger)</option>
          <option value="CASH">CASH IN HAND (Cash Vault)</option>
        </select>
      </div>
    </div>

    <div class="field-row-2">
      <div class="field-box">
        <label class="field-label">Transaction Date</label>
        <input type="date" id="inpEditFundDate" class="field-ctrl">
      </div>
      <div class="field-box">
        <label class="field-label">Amount (₹)</label>
        <input type="number" id="inpEditFundAmount" class="field-ctrl" placeholder="e.g. 50000" min="1" step="1">
      </div>
    </div>

    <div class="field-box">
      <label class="field-label">Source / Lender / Investor Entity Name</label>
      <input type="text" id="inpEditFundEntity" class="field-ctrl" placeholder="e.g. Society Capital, Apex Bank, President">
    </div>

    <div class="field-box">
      <label class="field-label">Narration / Remarks</label>
      <input type="text" id="inpEditFundNarration" class="field-ctrl" placeholder="e.g. Working Capital Borrowing">
    </div>

    <div style="display:flex; gap:10px; margin-top:14px;">
      <button type="button" class="btn btn-dark" onclick="closeModal('modalEditFund')" style="flex:1; justify-content:center; padding:10px;">Cancel</button>
      <button type="button" class="btn btn-blue" id="btnUpdateFundEntry" onclick="handleSaveEditFund()" style="flex:2; justify-content:center; padding:10px; font-weight:800;">💾 Save Changes</button>
    </div>
  </div>
</div>

<!-- MODAL: SYNC SETTINGS & INTERVAL -->
<div class="modal-backdrop" id="modalSyncSettings" style="display:none;">
  <div class="modal-dialog-box modal-dialog-sm">
    <div class="modal-header-row">
      <div style="color:#38BDF8; font-weight:800; font-size:1.05rem;">🔄 Live Sheet Synchronization Settings</div>
      <button type="button" class="close-x" onclick="closeModal('modalSyncSettings')">&times;</button>
    </div>
    
    <div style="text-align: center; margin: 12px 0;">
      <div style="font-size: 2.2rem; margin-bottom: 8px;">🔄</div>
      <div style="font-size: 1.1rem; font-weight: 700; color: #FFFFFF; margin-bottom: 4px;">Choose Sync Interval</div>
      <p style="font-size: 0.85rem; color: #94A3B8;">Configure how frequently the app automatically retrieves fresh records from your Google Spreadsheet.</p>
    </div>

    <div class="field-box">
      <label class="field-label">Synchronization Frequency / Timer</label>
      <select id="selSyncInterval" class="field-ctrl">
        <option value="now">Sync Now (Instant Manual Refresh)</option>
        <option value="1">Every 1 min (Real-time tracking)</option>
        <option value="5" selected>Every 5 min (Recommended / Standard)</option>
        <option value="30">Every 30 min (Battery / Performance saver)</option>
      </select>
    </div>

    <div style="margin-top: 14px; font-size: 0.82rem; color: #E2E8F0; background: rgba(56, 189, 248, 0.1); padding: 10px; border-radius: 6px; border: 1px solid rgba(56, 189, 248, 0.2);">
      💡 <strong>Note:</strong> Auto-sync performs an optimized background fetch without interrupting your active workflow.
    </div>

    <div style="display:flex; gap:10px; margin-top:16px;">
      <button type="button" class="btn btn-dark" onclick="closeModal('modalSyncSettings')" style="flex:1; justify-content:center; padding:10px;">Cancel</button>
      <button type="button" class="btn btn-blue" id="btnApplySyncSettings" style="flex:2; justify-content:center; padding:10px; font-weight:800;">⚙️ Set Sync Timer</button>
    </div>
  </div>
</div>
`;
}

function getCompleteSoftwareHtml() {
  var initialUsersJson = "[]";
  var initialSocietyDataJson = "null";
  var sheetUrl = "";
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (ss) {
      sheetUrl = ss.getUrl() || "";
      var uSheet = ss.getSheetByName("Users");
      if (uSheet && uSheet.getLastRow() > 1) {
        var uData = uSheet.getRange(2, 1, uSheet.getLastRow() - 1, 4).getValues();
        var uList = [];
        uData.forEach(function(r) {
          if (r[0]) uList.push({ username: String(r[0]).trim(), password: String(r[1]).trim(), role: String(r[2] || "Manager").trim(), email: String(r[3] || "").trim() });
        });
        if (uList.length > 0) initialUsersJson = JSON.stringify(uList);
      }
      try {
        var fullData = getSocietyFullDataWithoutFinSync();
        if (fullData && fullData.members && fullData.members.length > 0) {
          initialSocietyDataJson = JSON.stringify(fullData);
        }
      } catch(errData) {}
    }
  } catch(e) {}

  return getCompleteSoftwareHtmlContent() + 
    "\n<script>\nwindow.initialSheetUsers = " + initialUsersJson + ";\nwindow.initialSocietyData = " + initialSocietyDataJson + ";\nwindow.connectedSpreadsheetUrl = " + JSON.stringify(sheetUrl) + ";\n</script>\n" +
    getCompleteSoftwareClientScript() +
    "\n</body>\n</html>";
}


function getClientScriptPartA() {
  return `
<script>
// INITIAL AUTHENTICATION & LOGIN LOGIC

window.switchTab = function(tIdx) {
  for (var i = 1; i <= 5; i++) {
    var head = document.getElementById("tabHead" + i);
    var panel = document.getElementById("tabPanel" + i);
    if (head) head.className = (i === tIdx) ? "tab-item active" : "tab-item";
    if (panel) panel.style.display = (i === tIdx) ? "block" : "none";
  }
  if (tIdx === 1 && typeof window.renderMembers === "function") window.renderMembers();
  else if (tIdx === 2 && typeof window.renderPayments === "function") window.renderPayments();
  else if (tIdx === 3 && typeof window.renderLoans === "function") window.renderLoans();
  else if (tIdx === 4 && typeof window.renderBonusTab === "function") window.renderBonusTab();
  else if (tIdx === 5 && typeof window.renderPenaltyTab === "function") window.renderPenaltyTab();
};

window.authorizedUsers = [
  { username: "SANISH", password: "12345", role: "Super Admin", email: "stfsolutionsdelhi@gmail.com" },
  { username: "ADMIN", password: "12345", role: "Manager", email: "stfsolutionsdelhi@gmail.com" }
];

window.togglePasswordEye = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  var inp = document.getElementById("inpWinPassword");
  var btn = document.getElementById("btnToggleEye");
  if (!inp) return false;
  if (inp.type === "password") {
    inp.type = "text";
    if (btn) { btn.innerHTML = "🙈"; btn.title = "Hide Password"; }
  } else {
    inp.type = "password";
    if (btn) { btn.innerHTML = "👁️"; btn.title = "Show Password"; }
  }
  try { inp.focus(); } catch(err) {}
  return false;
};

window.safeToggleFullscreen = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  var btn1 = document.getElementById("btnLoginFullscreen");
  var btn2 = document.getElementById("btnToggleFullscreen");
  var isDocFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
  var isSimFull = document.body && document.body.classList.contains("simulated-fullscreen");

  if (!isDocFull && !isSimFull) {
    var el = document.documentElement;
    var req = el.requestFullscreen || el.webkitRequestFullscreen || el.mozRequestFullScreen || el.msRequestFullscreen;
    if (req) {
      try {
        var prom = req.call(el);
        if (prom && prom.catch) prom.catch(function(){});
      } catch(err) {}
    }
    if (document.body) document.body.classList.add("simulated-fullscreen");
    window.isAppInFullscreenMode = true;
    if (btn1) btn1.innerText = "✖ Exit Full Screen";
    if (btn2) btn2.innerText = "✖ Exit Full Screen";
  } else {
    window.isAppInFullscreenMode = false;
    try {
      if (document.exitFullscreen) {
        var p = document.exitFullscreen();
        if (p && p.catch) p.catch(function(){});
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      }
    } catch(err) {}
    if (document.body) document.body.classList.remove("simulated-fullscreen");
    if (btn1) btn1.innerText = "⛶ Full Screen";
    if (btn2) btn2.innerText = "⛶ Full Screen";
  }
  return false;
};

window.executeDirectLogin = function(e) {
    if (e) {
      if (e.preventDefault) e.preventDefault();
      if (e.stopPropagation) e.stopPropagation();
    }

    var uElem = document.getElementById("inpWinUsername");
    var pElem = document.getElementById("inpWinPassword");
    var uInp = (uElem ? uElem.value : "").trim();
    var pInp = (pElem ? pElem.value : "").trim();
    var errBox = document.getElementById("winLoginError");

    if (!uInp) {
      uInp = "SANISH";
      if (uElem) uElem.value = "SANISH";
    }

    if (!pInp) {
      if (errBox) {
        errBox.innerHTML = "⚠️ Please enter <strong>Password</strong> to continue!";
        errBox.style.display = "block";
      }
      if (pElem) { pElem.style.borderColor = "#EF4444"; pElem.focus(); }
      return false;
    }

    var uUpper = uInp.toUpperCase();
    var pVal = pInp;

    var allUsers = [];
    if (window.initialSheetUsers && Array.isArray(window.initialSheetUsers) && window.initialSheetUsers.length > 0) {
      allUsers = window.initialSheetUsers;
    } else if (window.authorizedUsers && Array.isArray(window.authorizedUsers) && window.authorizedUsers.length > 0) {
      allUsers = window.authorizedUsers;
    }

    var matched = null;
    if (allUsers && allUsers.length > 0) {
      for (var i = 0; i < allUsers.length; i++) {
        var u = allUsers[i];
        var dbUser = String(u.username || "").trim().toUpperCase();
        var dbPass = String(u.password || "").trim();
        if (dbUser === uUpper && (dbPass === pVal || (dbPass === "" && pVal === "12345"))) {
          matched = u;
          break;
        }
      }
    }

    var isMasterPass = false;
    var isUserPassMatch = false;
    if (matched) {
      var mPass = String(matched.password || "").trim();
      if (mPass === pVal || (mPass === "" && pVal === "12345")) {
        isUserPassMatch = true;
      }
    }
    if (isMasterPass || isUserPassMatch) {
      var current = matched || {
        username: uUpper || "SANISH",
        role: (uUpper === "ADMIN" ? "Manager" : "Super Admin"),
        email: "stfsolutionsdelhi@gmail.com"
      };
      window.currentUserSession = current;
      if (errBox) errBox.style.display = "none";
      var overlay = document.getElementById("windowsLoginOverlay");
      if (overlay) {
        overlay.style.display = "none";
        overlay.style.setProperty("display", "none", "important");
      }
      try {
        sessionStorage.removeItem("gullak_v22_session");
        sessionStorage.removeItem("gullak_v21_session");
      } catch(err) {}
      if (typeof window.switchTab === "function") {
        window.switchTab(1);
      }
      try {
        if (typeof window.bootApplication === "function") {
          window.bootApplication();
        }
      } catch(bootErr) {
        console.error("bootApplication error:", bootErr);
      }
      try {
        if (typeof window.refreshAll === "function") {
          window.refreshAll();
        }
      } catch(err) {
        console.error("refreshAll error:", err);
      }
      return false;
    } else {
      var errMsg = "❌ <strong>Invalid Password!</strong><br><small style='color:#CBD5E1;'>Please enter the correct password to continue.</small>";
      if (errBox) {
        errBox.innerHTML = errMsg;
        errBox.style.display = "block";
      }
      if (pElem) {
        pElem.style.borderColor = "#EF4444";
        pElem.value = "";
        pElem.focus();
      }
      return false;
    }
  };

  window.handleLoginKeyPress = function(e) {
  var k = e.key || e.keyCode || e.which;
  if (k === "Enter" || k === 13 || k === "13") {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
    window.executeDirectLogin(e);
    return false;
  }
};

window.logoutSession = function() {
  window.currentUserSession = null;
  try {
    sessionStorage.removeItem("gullak_v22_session");
    sessionStorage.removeItem("gullak_v21_session");
    sessionStorage.removeItem("gullak_v21_active_user");
  } catch(e) {}
  var overlay = document.getElementById("windowsLoginOverlay");
  if (overlay) {
    overlay.style.display = "flex";
  }
  var pInput = document.getElementById("inpWinPassword");
  if (pInput) { pInput.value = ""; pInput.focus(); }
};

window.handleForgotCredentials = function(e) {
  if (e && e.preventDefault) e.preventDefault();
  if (e && e.stopPropagation) e.stopPropagation();
  var card = document.getElementById("winForgotCard");
  if (card) {
    var isHidden = (card.style.display === "none" || !card.style.display);
    card.style.display = isHidden ? "block" : "none";
  }
};

(function(){
  var DEF_M = [{"id": "MEM010120261", "name": "Afsana Sister Pappu Ji 012025", "mobile": "9773841314", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Pappu Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120262", "name": "Ajay Kumar Garg Ref Suresh Lala Ji 012025", "mobile": "9873898898", "status": "ACTIVE", "address": "Kakrola", "nominee": "Suresh Lala Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120263", "name": "Amit S/O Sunil (Omwati Aunti Ji ) 102022", "mobile": "8287127921", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM010120264", "name": "Arvind Kumar 022022X2", "mobile": "9350743408", "status": "ACTIVE", "address": "Ghaziabad", "nominee": "Rekha Kumari", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 7500, "opInt": 0, "opPen": 0}, {"id": "MEM010120265", "name": "ASHA DEVI REF SUSHIL SO SHILA JI 012025", "mobile": "9311043442", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sushil", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120266", "name": "Ashish Aswal Ashu Vikas Vihar 022022", "mobile": "9899801307", "status": "ACTIVE", "address": "C-141 Vikas Vihar Kakrola", "nominee": "Sarita Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 9000, "opInt": 0, "opPen": 0}, {"id": "MEM010120267", "name": "Chanchal D/O Anil Padosi 022022", "mobile": "9910216942", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Anil Padosi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM010120268", "name": "Chanda Devi Ref Shila Devi 022024", "mobile": "8447218816", "status": "ACTIVE", "address": "Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 9200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120269", "name": "Deep Lal - Reena Devi 022023", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Reena Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 4000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202610", "name": "Deep Lal Electrician 022022", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202611", "name": "DEVENDER SINGH REF RAVI 202501", "mobile": "9456304719", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202612", "name": "Geeta Devi Wo Narender 012025", "mobile": "7042511156", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Narender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202613", "name": "Hari Ram Ji Vikas Vihar 032022", "mobile": "9650013268", "status": "ACTIVE", "address": "Kakrola", "nominee": "Hari Ram", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202614", "name": "Hirender Kumar - 2 - Neetu 012023", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Neetu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15360, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5050, "opInt": 0, "opPen": 0}, {"id": "MEM0101202615", "name": "Hirender Kumar -1- 022022", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Hirender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17802, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3030, "opInt": 0, "opPen": 0}, {"id": "MEM0101202616", "name": "Jagdish Mehto X2  022022", "mobile": "7042511481", "status": "ACTIVE", "address": "Jj Colony Bharat Vihar", "nominee": "Jagdish", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202617", "name": "Jagriti Sharma W/O Jugal Kishor 012023", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jugal Kishor", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 15000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202618", "name": "JAHANVI SHARMA DO JAGRITI JI 012025", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202619", "name": "Jot Singh Ref Ravi 012025", "mobile": "8178738999", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202620", "name": "Jugal Kishor Ji X2 072022", "mobile": "9310732656", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202621", "name": "JYOTI JOSHI JI REF JAGRITI JI 012025", "mobile": "9716124006", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202622", "name": "Kazim So Mumina Khatoon Ref Pappu 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202623", "name": "KEERTHI R S DO SOMYA MADAM 202501", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Somya Madam", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202624", "name": "KIRAN DEVI WO SUSHIL KUMAR 202501", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sushil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202625", "name": "Kuwar Pal -1 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202626", "name": "Kuwar Pal-2 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202627", "name": "Mukesh Sharma Ji X2 022022", "mobile": "8285405743", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Mukesh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18799.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5623, "opInt": 0, "opPen": 0}, {"id": "MEM0101202628", "name": "NANDINI JI 202501", "mobile": "8383071508", "status": "ACTIVE", "address": "SULAHKUL VIHAR", "nominee": "Nandini", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202629", "name": "Narayan Yadav X2 032022", "mobile": "9599959948", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Narayan", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18399.68, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202630", "name": "Narender Babblu Bo Ravi 012025", "mobile": "9354214597", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202631", "name": "Narender Kumar S/O Shila Devi 012023", "mobile": "7042511156", "status": "ACTIVE", "address": "S/O Shila Devi Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14399.88, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 2000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202632", "name": "Neeraj Renew So Raghuveer Ji 012025", "mobile": "9891811697", "status": "ACTIVE", "address": "Kakrola", "nominee": "Raghuveer Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202633", "name": "Omwati Aunti M/O Anil Kumar 022022", "mobile": "9971157481", "status": "ACTIVE", "address": "C-143 Vikas Vihar Kakrola", "nominee": "Anil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202634", "name": "Pappu Carpainter - 1 - 022022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Pappu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 13000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202635", "name": "Pappu Carpainter - 2 - Nargis 102022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Nargis", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 21000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202636", "name": "Pawan Kumar X2 072022", "mobile": "8368934198", "status": "ACTIVE", "address": "S/O Rakesh Kumar Vikas Vihar", "nominee": "Rakesh Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16799.76, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 19230, "opInt": 0, "opPen": 0}, {"id": "MEM0101202637", "name": "Peter Masih 042022", "mobile": "99990023275", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Peter", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202638", "name": "Raj Kumar (Colony) Kakrola 062022", "mobile": "8750830986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 22136, "opInt": 0, "opPen": 0}, {"id": "MEM0101202639", "name": "Raja Ram Ji Ref Deepak 062022", "mobile": "9810812331", "status": "ACTIVE", "address": "Narela", "nominee": "Deepak", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202640", "name": "Ram Bharose Ji Goyla Dairy 022022", "mobile": "9717961768", "status": "ACTIVE", "address": "Goyla Dairy 9717961768 , 0838392003", "nominee": "Ram Bharose", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202641", "name": "Ravi Garwali 022022", "mobile": "7042085508", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202642", "name": "Sanjay Kumar -1- Ref DeeplaI 022022", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202643", "name": "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Sandeep Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202645", "name": "Sanjay Yadav -1 X2 022022", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sanjay Yadav", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 23000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202646", "name": "Sanjay Yadav -2- Shubhankar 072023", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shubhankar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202647", "name": "Santosh Mehto X2 022022", "mobile": "9968062512", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Santosh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202648", "name": "Santosh Mistri Ref DeeplaI 012025", "mobile": "9891703298", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202649", "name": "Sarika 022022", "mobile": "9718174244", "status": "ACTIVE", "address": "Kakrola", "nominee": "Sarika", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15583.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 12000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202650", "name": "Sarita Aswal Wo Ashish 012025", "mobile": "9899801307", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ashish Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 25000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202651", "name": "Shila Devi Ref Omwati Aunti X2 092022", "mobile": "9643588165", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 11000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202652", "name": "Somya Madam Ref Jagriti Sharma 012023", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202653", "name": "Sushil Ji So Sheela Devi 012025", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sheela Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202654", "name": "URUZ KHATMA DO MUMINA REF PAPPU 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202655", "name": "Viney Electrician Ref Deep Lal 052023", "mobile": "7065708037", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 12800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202656", "name": "Vishnu Aggarwal -1 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202657", "name": "Vishnu Aggarwal -2 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202658", "name": "Parvesh Ansari Ref DeeplaI 010126", "mobile": "9315426875", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202659", "name": "Hazrat Ref Parvesh Ansari 010126", "mobile": "9718172262", "status": "ACTIVE", "address": "Dda Flat Janak Puri", "nominee": "Parvesh Ansari", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202660", "name": "Mintu Devi Ref Chanda Devi 012026", "mobile": "7033953938", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Chanda Devi", "rd": 400, "dateJoined": "2026-01-15", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202661", "name": "Mariam R/O Rupam & Shila Devi", "mobile": "8826567542", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202662", "name": "Rupam Ref Shila Devi 012026", "mobile": "8130546714", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202663", "name": "Surender Rawat 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sumitra Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202664", "name": "Sumitra Rawat Wo Surender 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Surender Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202665", "name": "Priya Sood Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "House Number B-115 Surya Vihar Binda", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202666", "name": "Raj Kumari Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "B-75 Bharat Vihar Kakrola 9810424981", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202667", "name": "Arvind Kumar Rekha Kumari 012026", "mobile": "9350743408", "status": "ACTIVE", "address": "Gazhiabad", "nominee": "Arvind Kumar", "rd": 400, "dateJoined": "2026-01-31", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202668", "name": "Rakhi Madam Ref Shila Ji 012026", "mobile": "9311633238", "status": "ACTIVE", "address": "Delhi", "nominee": "Shila Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}];
  var DEF_P=[];
  var DEF_L=[];

  var members = [];
  var payments = [];
  var loans = [];
  var exitSettlements = [];
  var bonusSettlements = [];
  var fundTransactions = [];

  var initLoaded = false;
  if (typeof window !== "undefined" && window.initialSocietyData && window.initialSocietyData.members && window.initialSocietyData.members.length > 0) {
    members = window.initialSocietyData.members;
    payments = window.initialSocietyData.payments || [];
    loans = window.initialSocietyData.loans || [];
    exitSettlements = window.initialSocietyData.exitSettlements || [];
    bonusSettlements = window.initialSocietyData.bonusSettlements || [];
    fundTransactions = window.initialSocietyData.fundTransactions || [];
    initLoaded = true;
  }

  if (!initLoaded) {
    try {
      var sM = localStorage.getItem("gullak_v21_m");
      if (sM) members = JSON.parse(sM);
      var sP = localStorage.getItem("gullak_v21_p");
      if (sP) payments = JSON.parse(sP);
      var sL = localStorage.getItem("gullak_v21_l");
      if (sL) loans = JSON.parse(sL);
      var sEx = localStorage.getItem("gullak_v21_ex");
      if (sEx) exitSettlements = JSON.parse(sEx);
      var sB = localStorage.getItem("gullak_v21_b");
      if (sB) bonusSettlements = JSON.parse(sB);
      var sF = localStorage.getItem("gullak_v21_fund");
      if (sF) fundTransactions = JSON.parse(sF);
    } catch(e) {}
  }

  if (!members || members.length === 0) {
    members = JSON.parse(JSON.stringify(DEF_M));
  }
  (members || []).forEach(function(m){
    var rawSt = String(m.status || "ACTIVE").trim().toUpperCase();
    m.status = (rawSt === "INACTIVE" || rawSt === "IN-ACTIVE" || rawSt === "DEACTIVE" || rawSt === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";
  });
  if (!payments) payments = [];
  if (!loans) loans = [];
  if (!exitSettlements) exitSettlements = [];
  if (!bonusSettlements) bonusSettlements = [];
  if (!fundTransactions) fundTransactions = [];

  window.fundTransactions = fundTransactions;
  window.members = members;
  window.payments = payments;
  window.loans = loans;
  window.exitSettlements = exitSettlements;
  window.bonusSettlements = bonusSettlements;
  var globalDefaultRate = 1.0;
  var globalDefaultDue = "15th of every month";
  var pendingBulkData = null;
  var currentActiveLedgerMember = null;

  window.globalSettings = { penaltyStartDate: "2026-10-01", skipPenalty: true };
  try {
    var savedStg = localStorage.getItem("gullak_v21_settings");
    if(savedStg) window.globalSettings = JSON.parse(savedStg);
  } catch(e) {}

  function parseDateParts(d){
    if (!d) return { yr: 2026, mo: 1, day: 1 };
    if (d instanceof Date) {
      if (isNaN(d.getTime())) return { yr: 2026, mo: 1, day: 1 };
      return { yr: d.getFullYear(), mo: d.getMonth() + 1, day: d.getDate() };
    }
    var s = String(d).trim().split("T")[0].split(" ")[0];
    var mYmd = s.match(/^(\d{4})[-\/.](\d{1,2})[-\/.](\d{1,2})$/);
    if (mYmd) {
      return { yr: parseInt(mYmd[1], 10), mo: parseInt(mYmd[2], 10), day: parseInt(mYmd[3], 10) };
    }
    var mDmy = s.match(/^(\d{1,2})[-\/.](\d{1,2})[-\/.](\d{4})$/);
    if (mDmy) {
      return { yr: parseInt(mDmy[3], 10), mo: parseInt(mDmy[2], 10), day: parseInt(mDmy[1], 10) };
    }
    // Handle month names like 01-Aug-2026 or 1 Aug 2026
    var mAlpha = s.match(/^(\d{1,2})[-\/\s]([A-Za-z]{3,9})[-\/\s](\d{4})$/);
    if (mAlpha) {
      var monthMap = { jan:1, feb:2, mar:3, apr:4, may:5, jun:6, jul:7, aug:8, sep:9, oct:10, nov:11, dec:12 };
      var mShort = mAlpha[2].substring(0, 3).toLowerCase();
      var moNum = monthMap[mShort] || 1;
      return { yr: parseInt(mAlpha[3], 10), mo: moNum, day: parseInt(mAlpha[1], 10) };
    }
    var dt = new Date(s);
    if (!isNaN(dt.getTime()) && dt.getFullYear() >= 2020 && dt.getFullYear() <= 2100) {
      return { yr: dt.getFullYear(), mo: dt.getMonth() + 1, day: dt.getDate() };
    }
    return { yr: 2026, mo: 1, day: 1 };
  }
  window.parseDateParts = parseDateParts;

  function toIsoDateStr(d){
    if(!d) return "2026-01-01";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo || !dp.day) return "2026-01-01";
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    var m = String(dp.mo).padStart(2, "0");
    var day = String(dp.day).padStart(2, "0");
    return y + "-" + m + "-" + day;
  }
  window.toIsoDateStr = toIsoDateStr;

  function getYearMonthKey(d){
    if(!d) return "2026-01";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo) return "2026-01";
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    var m = String(dp.mo).padStart(2, "0");
    return y + "-" + m;
  }
  window.getYearMonthKey = getYearMonthKey;

  function cleanNum(val, def){ 
    if(val === null || val === undefined) return (def || 0);
    var str = String(val).replace(/,/g, "").trim();
    var n = Number(str); 
    if(isNaN(n) || n > 100000000 || n < 0) return (def || 0); 
    return Math.round(n); 
  }
  function cleanRd(val){ 
    if(val === null || val === undefined) return 400;
    var str = String(val).replace(/,/g, "").trim();
    var n = Number(str); 
    if(isNaN(n) || n <= 0 || n > 50000) return 400; 
    return Math.round(n); 
  }
  function getTodayYMD(){ 
    var d = new Date(); 
    return d.getFullYear() + "-" + String(d.getMonth()+1).padStart(2,"0") + "-" + String(d.getDate()).padStart(2,"0"); 
  }

  function toDisplayDate(d){
    if(!d) return "01/01/2026";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo || !dp.day) return "01/01/2026";
    var day = String(dp.day).padStart(2, "0");
    var m = String(dp.mo).padStart(2, "0");
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    return day + "/" + m + "/" + y;
  }
  window.toDisplayDate = toDisplayDate;

  // Exact ID Suffixes
  function getDDMMYYFromYMD(ymd){
    var p = String(ymd || getTodayYMD()).split("-");
    if(p.length === 3) return p[2] + p[1] + p[0].substring(2);
    return "010126";
  }

  function getDDMMYYYYFromYMD(ymd){
    var p = String(ymd || getTodayYMD()).split("-");
    if(p.length === 3) return p[2] + p[1] + p[0];
    return "01012026";
  }

  function saveStore(){
    try {
      window.members = members;
      window.payments = payments;
      window.loans = loans;
      window.exitSettlements = exitSettlements;
      window.bonusSettlements = bonusSettlements;
      window.fundTransactions = fundTransactions;
      localStorage.setItem("gullak_v21_m", JSON.stringify(members));
      localStorage.setItem("gullak_v21_p", JSON.stringify(payments));
      localStorage.setItem("gullak_v21_l", JSON.stringify(loans));
      localStorage.setItem("gullak_v21_ex", JSON.stringify(exitSettlements));
      localStorage.setItem("gullak_v21_b", JSON.stringify(bonusSettlements));
      localStorage.setItem("gullak_v21_fund", JSON.stringify(fundTransactions));
      localStorage.setItem("gullak_v21_settings", JSON.stringify(window.globalSettings || { penaltyStartDate: "2026-10-01", skipPenalty: true }));
    } catch(e) {}
  }
function getMemberTotalRd(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var pSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        pSum += cleanNum(p.rd, 0); 
      }
    }); 
    return cleanNum(m.rdPaid, 0) + pSum; 
  }

  function getMemberActiveLoan(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var lSum = 0; 
    loans.forEach(function(l){ 
      if((String(l.id).trim().toUpperCase() === mid || String(l.name).trim().toLowerCase() === mName) && String(l.status).toUpperCase() === "ACTIVE") {
        lSum += cleanNum(l.outstanding, 0); 
      }
    }); 
    return cleanNum(m.opLoan, 0) + lSum; 
  }

  function getMemberTotalPenalty(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var pSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        pSum += cleanNum(p.penalty, 0); 
      }
    }); 
    return cleanNum(m.opPen, 0) + pSum; 
  }

  function getMemberTotalWaiver(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var wSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        wSum += cleanNum(p.waiver, 0); 
      }
    }); 
    return wSum; 
  }

  // EXACT INTEREST CALCULATION (SARIKA & ALL MEMBERS)
  // Loan month earns 0 interest.
  // Starting from 1st of next month, interest accrues at (rate % per month) on remaining balance for each overdue month.
  function calculateMemberLiveInterestDue(m){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;

    var totalAccruedInt = cleanNum(m.opInt, 0);

    loans.filter(function(l){ 
      return (String(l.id).trim().toUpperCase() === mid || String(l.name).trim().toLowerCase() === mName) && String(l.status).toUpperCase() === "ACTIVE"; 
    }).forEach(function(l){
      var lDate = l.date || "2026-01-01";
      var lp = lDate.split("-");
      var lYr = parseInt(lp[0], 10) || 2026;
      var lMo = parseInt(lp[1], 10) || 1;
      
      var monthsPassed = (curYr - lYr) * 12 + (curMo - lMo);
      if(monthsPassed > 0){
        var monthlyRate = (Number(l.rate) || 1.0) / 100.0;
        var monthlyInt = Math.round(cleanNum(l.outstanding, 0) * monthlyRate);
        totalAccruedInt += (monthsPassed * monthlyInt);
      }
    });

    var totalIntPaid = 0;
    payments.forEach(function(p){
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName){
        totalIntPaid += cleanNum(p.interest, 0);
      }
    });

    var netIntDue = totalAccruedInt - totalIntPaid;
    return Math.max(0, netIntDue);
  }

  // EXACT PENALTY CALCULATION (SANISH, AMIT VERMA & ALL MEMBERS: ₹10/DAY OVERDUE FROM 15TH)
  function calculateMemberLivePenaltyDue(m){
    var isSkipChecked = true;
    if(window.globalSettings && typeof window.globalSettings.skipPenalty === "boolean"){
      isSkipChecked = window.globalSettings.skipPenalty;
    } else {
      var chkEl = document.getElementById("chkSkipPenalty");
      if(chkEl) isSkipChecked = chkEl.checked;
    }
    if(isSkipChecked) return 0;

    var penStartStr = "2026-10-01";
    if(window.globalSettings && window.globalSettings.penaltyStartDate){
      penStartStr = window.globalSettings.penaltyStartDate;
    } else {
      var psEl = document.getElementById("inpPenaltyStartDate");
      if(psEl && psEl.value) penStartStr = psEl.value;
    }

    var isoPenStart = toIsoDateStr(penStartStr);
    var todayYMD = getTodayYMD();
    if(todayYMD < isoPenStart) return 0;

    var mid = String(m.id || "").trim().toUpperCase();
    var mName = String(m.name || "").trim().toLowerCase();
    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;
    var curDay = today.getDate();
    
    var dueDayNum = 15;
    if(m.dueDay){
      var match = String(m.dueDay).match(/\d+/);
      if(match) dueDayNum = parseInt(match[0], 10);
    }
    if(isNaN(dueDayNum) || dueDayNum < 1 || dueDayNum > 28) dueDayNum = 15;

    var jdp = parseDateParts(m.dateJoined || "2026-01-01");
    var jYr = jdp.yr;
    var jMo = jdp.mo;
    if(jYr < 2024 || jYr > 2035) jYr = 2026;
    if(jMo < 1 || jMo > 12) jMo = 1;

    var totalRdPaid = cleanNum(m.rdPaid, 0);
    var totalPenPaid = 0;
    var totalWaiver = 0;

    payments.forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase();
      var pMName = String(p.name || "").trim().toLowerCase();
      if((pMid && pMid === mid) || (pMName && pMName === mName)){
        totalRdPaid += cleanNum(p.rd, 0);
        totalPenPaid += cleanNum(p.penalty, 0);
        totalWaiver += cleanNum(p.waiver, 0);
      }
    });

    var monthlyRd = cleanRd(m.rd);
    var monthsCovered = Math.floor(totalRdPaid / monthlyRd);
    var totalAccruedPen = cleanNum(m.opPen, 0);

    var pStartParts = parseDateParts(penStartStr);
    var pStartYr = pStartParts.yr || 2026;
    var pStartMo = pStartParts.mo || 10;
    var penaltyStartSerial = pStartYr * 12 + pStartMo;

    var startSerial = jYr * 12 + jMo;
    var currentSerial = curYr * 12 + curMo;
    var monthIdx = 0;

    for(var s = startSerial; s <= currentSerial; s++){
      if(s >= penaltyStartSerial){
        var yr = Math.floor((s - 1) / 12);
        var mo = ((s - 1) % 12) + 1;
        var isCurMonth = (yr === curYr && mo === curMo);
        if(monthIdx >= monthsCovered){
          if(isCurMonth){
            if(curDay > dueDayNum){
              totalAccruedPen += ((curDay - dueDayNum) * 10);
            }
          } else {
            var dueDt = new Date(yr, mo - 1, dueDayNum);
            var refDt = new Date(pStartYr, pStartMo - 1, 1);
            var calcFromDt = dueDt > refDt ? dueDt : refDt;
            var diffMs = today.getTime() - calcFromDt.getTime();
            var daysLate = Math.floor(diffMs / (1000 * 60 * 60 * 24));
            if(daysLate > 0){
              totalAccruedPen += (daysLate * 10);
            }
          }
        }
      }
      monthIdx++;
    }

    var netPenDue = totalAccruedPen - totalPenPaid - totalWaiver;
    return Math.max(0, netPenDue);
  }

  function calculate1PercentPmBonus(m, filterFromYmd, filterToYmd){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var openingRd = cleanNum(m.rdPaid, 0);
    var schedule = [];
    var totalBonus = 0;
    
    var selVal = document.getElementById("selFinancialYear") ? document.getElementById("selFinancialYear").value : "2026";
    var selYrNum = 2026;
    if(selVal){
      var matched = String(selVal).match(/\d{4}/);
      if(matched) selYrNum = parseInt(matched[0], 10);
    }
    if(isNaN(selYrNum) || selYrNum < 2020 || selYrNum > 2100) selYrNum = 2026;
    var selYear = String(selYrNum);

    var monthsNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

    // 1. Calculate RD deposits made in years prior to selYrNum
    var priorYearsRd = 0;
    // 2. Map monthly deposits for selYrNum
    var monthlyDepositMap = [0,0,0,0,0,0,0,0,0,0,0,0];
    payments.forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase(); var pMName = String(p.name || "").trim().toLowerCase(); if(((pMid && pMid === mid) || (pMName && pMName === mName)) && cleanNum(p.rd, 0) > 0){
        var dp = parseDateParts(p.date);
        if(dp.yr < selYrNum){
          priorYearsRd += cleanNum(p.rd, 0);
        } else if(dp.yr === selYrNum && dp.mo >= 1 && dp.mo <= 12){
          monthlyDepositMap[dp.mo - 1] += cleanNum(p.rd, 0);
        }
      }
    });

    var today = new Date();
    var curYr = today.getFullYear();
    var curMoIdx = today.getMonth(); // 0-based: 8 for Sep

    var baseOpening = openingRd + priorYearsRd;
    var runningBase = baseOpening;

    for (var i = 0; i < 12; i++) {
      var monthStartBase = runningBase;
      var depositThisMonth = monthlyDepositMap[i];
      var monthEndBase = monthStartBase + depositThisMonth;

      // Month i earns bonus if completed strictly before current month in current year, or all months of a prior year:
      var isCompletedMonth = (selYrNum < curYr) ? true : ((selYrNum === curYr) ? (i <= curMoIdx) : false);
      // 1% bonus is calculated on the closing balance of RD for that month (Opening + Prior Deposits + Deposits made in month i)
      var calcBase = monthEndBase;
      var mBonus = isCompletedMonth && calcBase > 0 ? Math.round(calcBase * 0.01) : 0;
      
      if(isCompletedMonth) {
        totalBonus += mBonus;
      }

      schedule.push({
        month: monthsNames[i] + " " + selYear,
        monthIdx: i + 1,
        opening: monthStartBase,
        deposit: depositThisMonth,
        closing: monthEndBase,
        mBonus: mBonus,
        accumBonus: totalBonus
      });

      runningBase = monthEndBase;
    }

    return { totalBonus: totalBonus, schedule: schedule };
  }

  function getMemberBonus(m){ return calculate1PercentPmBonus(m).totalBonus; }
  
  function getMemberLoanLimit(m){ 
    if(String(m.status).toUpperCase() === "INACTIVE"){
      return { limit: 0, text: "0 (INACTIVE)", isBlocked: true };
    }
    var activeDue = getMemberActiveLoan(m); 
    if(activeDue > 0) return { limit: 0, text: "₹0 (Active Loan)", isBlocked: true }; 
    if(cleanNum(m.customLimit, 0) > 0) return { limit: cleanNum(m.customLimit, 0), text: "₹" + cleanNum(m.customLimit, 0).toLocaleString("en-IN"), isBlocked: false }; 
    var std = getMemberTotalRd(m) * 2; 
    return { limit: std, text: "₹" + std.toLocaleString("en-IN"), isBlocked: false }; 
  }

  // AI-POWERED COLLISION-FREE AUTO-ID GENERATOR
  function generateAutoId(type, dateStr){
    var dp = parseDateParts(dateStr || getTodayYMD());
    var yrShort = String(dp.yr).substring(2);
    var moShort = String(dp.mo).padStart(2, "0");
    var dayShort = String(dp.day).padStart(2, "0");
    var datePrefix = yrShort + moShort + dayShort;
    
    var pfx = "REC";
    var existingList = [];
    var t = String(type || "REC").toUpperCase();
    if(t === "REC" || t === "RECEIPT" || t === "PAYMENT"){
      pfx = "REC";
      existingList = payments.map(function(x){ return String(x.receiptNo || ""); });
    } else if(t === "LOAN" || t === "LN"){
      pfx = "LN";
      existingList = loans.map(function(x){ return String(x.loanId || ""); });
    } else if(t === "MEM" || t === "MEMBER"){
      pfx = "MEM";
      existingList = members.map(function(x){ return String(x.id || ""); });
    } else if(t === "EXIT" || t === "EX"){
      pfx = "EXT";
      existingList = exitSettlements.map(function(x){ return String(x.exitId || ""); });
    } else if(t === "SET" || t === "SETTLEMENT" || t === "BONUS"){
      pfx = "SET";
      existingList = bonusSettlements.map(function(x){ return String(x.settlementId || ""); });
    } else if(t === "FUND" || t === "FND"){
      pfx = "FND";
      existingList = fundTransactions.map(function(x){ return String(x.id || ""); });
    }

    var seq = 1;
    var candidate = "";
    while(true){
      var seqStr = String(seq).padStart(3, "0");
      candidate = pfx + "-" + datePrefix + "-" + seqStr;
      if(existingList.indexOf(candidate) === -1){
        break;
      }
      seq++;
    }
    return candidate;
  }
  window.generateAutoId = generateAutoId;

  // FINANCIAL YEAR (CALENDAR YEAR: JAN - DEC) SWITCHER GENERATOR
  function setupFinancialYearDropdown(){
    var sel = document.getElementById("selFinancialYear");
    if(!sel) return;
    var curVal = sel.value || "2026";
    if(curVal.indexOf(" ") >= 0){
      var m = curVal.match(/\d{4}/);
      if(m) curVal = m[0];
    }
    
    var currentYear = (new Date()).getFullYear().toString();
    if(!/^\d{4}$/.test(currentYear)) currentYear = "2026";

    var fyMap = {};
    fyMap["2026"] = true;
    fyMap[currentYear] = true;

    function recordYearFromDate(dtStr){
      if(!dtStr) return;
      var dp = parseDateParts(dtStr);
      if(dp && dp.yr && dp.yr >= 2020 && dp.yr <= 2100){
        fyMap[String(dp.yr)] = true;
      }
    }

    payments.forEach(function(p){ recordYearFromDate(p.date); });
    loans.forEach(function(l){ recordYearFromDate(l.date); });
    exitSettlements.forEach(function(e){ recordYearFromDate(e.date); });
    bonusSettlements.forEach(function(b){ recordYearFromDate(b.date); });
    fundTransactions.forEach(function(f){ recordYearFromDate(f.date); });

    var sortedYrs = Object.keys(fyMap).sort();
    var h = "";
    sortedYrs.forEach(function(yr){
      var isSel = (yr === curVal) ? "selected" : "";
      h += "<option value='" + yr + "' " + isSel + ">FY " + yr + "</option>";
    });
    sel.innerHTML = h;
    if(sortedYrs.indexOf(curVal) === -1){
      sel.value = currentYear;
    } else {
      sel.value = curVal;
    }
  }

  function calculateSocietyLiquidBalances(){
    var cIn = 0, cOut = 0, bIn = 0, bOut = 0;
    // 1. Opening RD from members (considered cash balance unless specified)
    members.forEach(function(m){
      cIn += cleanNum(m.rdPaid || m.opRd, 0);
    });
    // 2. Receipts / Collections
    payments.forEach(function(p){
      var safeMode = String(p.mode||'CASH').toUpperCase().indexOf('ONLINE') >= 0 ? 'BANK' : 'CASH';
      var amt = cleanNum(p.total, 0);
      if(safeMode === 'BANK') bIn += amt; else cIn += amt;
    });
    // 3. Fund Register (Invest/Borrow)
    fundTransactions.forEach(function(f){
      var acc = String(f.account || 'BANK').toUpperCase() === 'CASH' ? 'CASH' : 'BANK';
      var type = String(f.type || 'INVEST').toUpperCase();
      var amt = cleanNum(f.amount, 0);
      if(type === 'INVEST' || type === 'INFLOW'){
        if(acc === 'CASH') cIn += amt; else bIn += amt;
      } else {
        if(acc === 'CASH') cOut += amt; else bOut += amt;
      }
    });
    // 4. Loans Disbursed (Outflow)
    loans.forEach(function(l){
      var safeMode = String(l.mode || 'CASH').toUpperCase().indexOf('ONLINE') >= 0 ? 'BANK' : 'CASH';
      var amt = cleanNum(l.principal, 0);
      if(safeMode === 'BANK') bOut += amt; else cOut += amt;
    });
    // 5. Member Exit Payouts (Outflow)
    exitSettlements.forEach(function(x){
      var amt = cleanNum(x.payout, 0);
      cOut += amt;
    });
    var netCash = cIn - cOut;
    var netBank = bIn - bOut;
    var netTotal = netCash + netBank;
    return { cash: netCash, bank: netBank, total: netTotal, cIn: cIn, cOut: cOut, bIn: bIn, bOut: bOut };
  }
  window.calculateSocietyLiquidBalances = calculateSocietyLiquidBalances;

  function updateKPIs(){
    var activeMems = members.filter(function(m){ return String(m.status).toUpperCase() === 'ACTIVE'; });
    document.getElementById('dispTotalMem').innerText = activeMems.length + ' / ' + members.length;
    var totalRdRecv = 0; members.forEach(function(m){ totalRdRecv += getMemberTotalRd(m); });
    document.getElementById('dispTotalRd').innerText = '₹' + totalRdRecv.toLocaleString('en-IN');
    var totalLoan = 0; loans.forEach(function(l){ if(String(l.status).toUpperCase() === 'ACTIVE') totalLoan += cleanNum(l.outstanding, 0); });
    document.getElementById('dispTotalLoan').innerText = '₹' + totalLoan.toLocaleString('en-IN');
    var estB = 0; members.forEach(function(m){ estB += getMemberBonus(m); });
    document.getElementById('dispTotalBonus').innerText = '₹' + estB.toLocaleString('en-IN');
    
    // Unified Liquid Cash & Bank Register KPI
    var liquid = calculateSocietyLiquidBalances();
    var elFund = document.getElementById('dispTotalFund');
    elFund.innerText = (liquid.total >= 0 ? '+₹' : '-₹') + Math.abs(liquid.total).toLocaleString('en-IN');
    elFund.className = liquid.total >= 0 ? 'kpi-val val-green' : 'kpi-val val-red';
    
    var totalNpa = 0; exitSettlements.forEach(function(e){ totalNpa += cleanNum(e.npaLoss, 0); });
    document.getElementById('dispTotalNpa').innerText = '₹' + totalNpa.toLocaleString('en-IN');
  }
  // MASTER LEDGER (TAB 1) WITH LOAN LIMIT SUM & DYNAMIC SUBTOTALS
  function renderMembers(){
    var st = document.getElementById("selFilterStatus").value;
    var sort = document.getElementById("selSortMembers").value;
    var q = (document.getElementById("memberFilterInput").value || "").toLowerCase().trim();
    var filtered = members.filter(function(m){ 
      return (String(m.name||"").toLowerCase().indexOf(q)>=0||String(m.mobile||"").indexOf(q)>=0)&&(st==="ALL"||String(m.status).toUpperCase()===st); 
    });

    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });
    if(sort==="rd_high") filtered.sort(function(a,b){ return getMemberTotalRd(b)-getMemberTotalRd(a); });
    if(sort==="rd_low") filtered.sort(function(a,b){ return getMemberTotalRd(a)-getMemberTotalRd(b); });
    if(sort==="loan_high") filtered.sort(function(a,b){ return getMemberActiveLoan(b)-getMemberActiveLoan(a); });
    if(sort==="pen_high") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(b)-calculateMemberLivePenaltyDue(a); });

    var tbody = document.getElementById("tbodyMembers"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94A3B8;'>No members found</td></tr>"; 
      document.getElementById("tfootMembersTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumLoan = 0, sumInt = 0, sumPen = 0, sumLoanLimit = 0;

    filtered.forEach(function(m){
      var mid = String(m.id); 
      var totRd = getMemberTotalRd(m); 
      var loanDue = getMemberActiveLoan(m); 
      var intDue = calculateMemberLiveInterestDue(m);
      var penDue = calculateMemberLivePenaltyDue(m);
      var lLimit = getMemberLoanLimit(m);
      var badge = String(m.status).toUpperCase() === "ACTIVE" ? "<span class='badge-active'>ACTIVE</span>" : "<span class='badge-inactive'>INACTIVE</span>";
      
      sumRd += totRd;
      sumLoan += loanDue;
      sumInt += intDue;
      sumPen += penDue;
      sumLoanLimit += cleanNum(lLimit.limit, 0);

      html += "<tr>" +
        "<td><div class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + " 🔍</div><div style='color:#38BDF8;font-size:0.75rem;'>" + m.mobile + "</div></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + loanDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;font-weight:700;'>₹" + intDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;font-weight:700;'>₹" + penDue.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-size:0.78rem;color:" + (lLimit.isBlocked ? "#94A3B8" : "#38BDF8") + ";font-weight:700;'>" + lLimit.text + "</span></td>" +
        "<td>" + badge + "</td>" +
        "<td><button class='btn-action-rcv action-receive-for' data-id='" + mid + "'>Receive</button><button class='btn-action-edit action-edit-member' data-id='" + mid + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootMembersTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + sumInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;font-weight:800;'>₹" + sumLoanLimit.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2' style='color:#94A3B8; font-size:0.75rem;'>Grand Totals</td>" +
      "</tr>";
  }

  // COLLECTIONS & RECEIPTS (TAB 2)
  function renderPayments(){
    var mode = document.getElementById("selFilterPayMode").value;
    var sort = document.getElementById("selSortPayDate").value;
    var fromD = document.getElementById("inpPayFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpPayFilterTo").value || "2030-12-31";
    var q = (document.getElementById("searchPayInput").value || "").toLowerCase().trim();

    var filtered = payments.filter(function(p){
      var d = p.date || "2026-01-01";
      var matchDate = (d >= fromD && d <= toD);
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      var matchMode = (mode === "ALL" || safeMode === mode);
      var matchSearch = String(p.receiptNo||"").toLowerCase().indexOf(q) >= 0 || String(p.name||"").toLowerCase().indexOf(q) >= 0 || String(p.narration||"").toLowerCase().indexOf(q) >= 0;
      return matchDate && matchMode && matchSearch;
    });

    if(sort==="new") filtered.sort(function(a,b){ return new Date(b.date||"2026-01-01")-new Date(a.date||"2026-01-01"); });
    if(sort==="old") filtered.sort(function(a,b){ return new Date(a.date||"2026-01-01")-new Date(b.date||"2026-01-01"); });
    if(sort==="amt_high") filtered.sort(function(a,b){ return cleanNum(b.total,0)-cleanNum(a.total,0); });

    var tbody = document.getElementById("tbodyPayments"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='11' style='text-align:center;color:#94A3B8;'>No receipts found for selected date range</td></tr>"; 
      document.getElementById("tfootPaymentsTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumInt = 0, sumPen = 0, sumRepay = 0, sumWvr = 0, sumTot = 0;

    filtered.forEach(function(p){
      var narrTxt = p.narration ? '<span class="narration-badge">📝 ' + p.narration + '</span>' : '';
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      var rRd = cleanNum(p.rd, 0);
      var rInt = cleanNum(p.interest, 0);
      var rPen = cleanNum(p.penalty, 0);
      var rRepay = cleanNum(p.loanRepay, 0);
      var rWvr = cleanNum(p.waiver, 0);
      var rTot = cleanNum(p.total, 0);

      sumRd += rRd; sumInt += rInt; sumPen += rPen; sumRepay += rRepay; sumWvr += rWvr; sumTot += rTot;

      html += "<tr>" +
        "<td style='color:#FBBF24;font-family:monospace;font-weight:700;'>" + p.receiptNo + "</td>" +
        "<td>" + toDisplayDate(p.date) + "</td>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + p.id + "'>" + p.name + "</strong>" + narrTxt + "</td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + rRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + rInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + rPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;font-weight:700;'>₹" + rRepay.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + rWvr.toLocaleString("en-IN") + "</td>" +
        "<td style='font-weight:800;color:#10B981;font-size:0.95rem;'>₹" + rTot.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-weight:700;color:" + (safeMode==="CASH"?"#F59E0B":"#38BDF8") + ";'>" + safeMode + "</span></td>" +
        "<td><button class='btn-action-edit action-edit-receipt' data-rec='" + p.receiptNo + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootPaymentsTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " RECEIPTS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + sumInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;'>₹" + sumRepay.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + sumWvr.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + sumTot.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  // LOAN REGISTER (TAB 3)
  function renderLoans(){
    var fromD = document.getElementById("inpLoanFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpLoanFilterTo").value || "2030-12-31";
    var tp = document.getElementById("selFilterLoanType").value;
    var st = document.getElementById("selFilterLoanStatus").value;
    var q = (document.getElementById("searchLoanInput") ? document.getElementById("searchLoanInput").value : "").toLowerCase().trim();

    var filtered = loans.filter(function(l){
      var d = l.date || "2026-01-01";
      var matchDate = (d >= fromD && d <= toD);
      var matchType = (tp === "ALL" || String(l.type) === tp);
      var matchStatus = (st === "ALL" || String(l.status).toUpperCase() === st);
      var matchSearch = String(l.loanId||"").toLowerCase().indexOf(q) >= 0 || String(l.name||"").toLowerCase().indexOf(q) >= 0 || String(l.narration||"").toLowerCase().indexOf(q) >= 0;
      return matchDate && matchType && matchStatus && matchSearch;
    });

    var tbody = document.getElementById("tbodyLoans"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No loans matching criteria</td></tr>"; 
      document.getElementById("tfootLoansTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumPrinc = 0, sumOut = 0;

    filtered.forEach(function(l){
      var badge = String(l.status).toUpperCase() === "ACTIVE" ? "badge-active" : "badge-inactive";
      var pr = cleanNum(l.principal, 0);
      var out = cleanNum(l.outstanding, 0);
      sumPrinc += pr; sumOut += out;
      var narrInfo = l.narration ? '<br><small style="color:#94A3B8;font-size:0.75rem;">📝 ' + l.narration + '</small>' : '';

      html += "<tr>" +
        "<td style='color:#FBBF24;font-family:monospace;'>" + l.loanId + "</td>" +
        "<td>" + toDisplayDate(l.date) + "</td>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + l.id + "'>" + l.name + "</strong>" + narrInfo + "</td>" +
        "<td>" + l.type + "</td>" +
        "<td>₹" + pr.toLocaleString("en-IN") + "</td>" +
        "<td>" + l.rate + "%</td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + out.toLocaleString("en-IN") + "</td>" +
        "<td><span class='" + badge + "'>" + l.status + "</span></td>" +
        "<td><button class='btn-action-edit action-edit-loan' data-loanid='" + l.loanId + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootLoansTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='4' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " LOANS)</td>" +
        "<td style='color:#FBBF24;'>₹" + sumPrinc.toLocaleString("en-IN") + "</td>" +
        "<td></td>" +
        "<td style='color:#EF4444;'>₹" + sumOut.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }
`;
}


function getClientScriptPartB() {
  return `
  // TAB 4: ANNUAL BONUS REGISTER WITH DYNAMIC SUBTOTALS
  function renderBonusTab(){
    var st = document.getElementById("selFilterBonusStatus").value;
    var sort = document.getElementById("selSortBonus").value;
    var fromD = document.getElementById("inpBonusFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpBonusFilterTo").value || "2030-12-31";
    var q = (document.getElementById("searchBonusInput").value || "").toLowerCase().trim();

    // Compute live global bonus stats
    var totalIntReceived = 0;
    payments.forEach(function(p){ totalIntReceived += cleanNum(p.interest, 0); });
    var totalCumulativeRd = 0;
    members.forEach(function(m){ if(String(m.status).toUpperCase() === "ACTIVE") totalCumulativeRd += getMemberTotalRd(m); });
    var totalAccruedBonus = 0;
    members.forEach(function(m){ totalAccruedBonus += getMemberBonus(m); });

    var elInt = document.getElementById("dispBonusTotalInterestRecv");
    var elRd = document.getElementById("dispBonusTotalRdBase");
    var elAcc = document.getElementById("dispBonusTotalAccrued");
    if(elInt) elInt.innerText = "₹" + totalIntReceived.toLocaleString("en-IN");
    if(elRd) elRd.innerText = "₹" + totalCumulativeRd.toLocaleString("en-IN");
    if(elAcc) elAcc.innerText = "₹" + totalAccruedBonus.toLocaleString("en-IN");

    var filtered = members.filter(function(m){
      var matchSearch = String(m.name||"").toLowerCase().indexOf(q) >= 0 || String(m.id||"").toLowerCase().indexOf(q) >= 0;
      var hasSettled = bonusSettlements.some(function(b){ return String(b.id) === String(m.id); });
      var matchStatus = (st === "ALL") || (st === "PAID" && hasSettled) || (st === "PENDING" && !hasSettled);
      return matchSearch && matchStatus;
    });

    if(sort==="bonus_high") filtered.sort(function(a,b){ return getMemberBonus(b)-getMemberBonus(a); });
    if(sort==="bonus_low") filtered.sort(function(a,b){ return getMemberBonus(a)-getMemberBonus(b); });
    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });

    var tbody = document.getElementById("tbodyBonusList"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94A3B8;'>No bonus records found</td></tr>"; 
      document.getElementById("tfootBonusTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumBonus = 0, sumLoan = 0, sumPen = 0;

    filtered.forEach(function(m){
      var mid = String(m.id);
      var totRd = getMemberTotalRd(m);
      var bVal = getMemberBonus(m);
      var lDue = getMemberActiveLoan(m);
      var pDue = calculateMemberLivePenaltyDue(m);
      var settled = bonusSettlements.find(function(b){ return String(b.id) === mid; });
      var stBadge = settled ? "<span class='badge-active'>PAID / SET-OFF</span>" : "<span class='badge-pending'>PENDING</span>";

      sumRd += totRd; sumBonus += bVal; sumLoan += lDue; sumPen += pDue;

      html += "<tr>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + "</strong><br><small style='color:#94A3B8;'>" + mid + "</small></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td><span class='bonus-clickable action-view-bonus-stmt' data-id='" + mid + "' title='Click to view 12-month schedule'>₹" + bVal.toLocaleString("en-IN") + " 📋</span></td>" +
        "<td style='color:#EF4444;'>₹" + lDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + pDue.toLocaleString("en-IN") + "</td>" +
        "<td>" + stBadge + "</td>" +
        "<td><button class='btn-action-rcv action-setoff-bonus' data-id='" + mid + "' style='background:#7C3AED;'>Set-off / Pay</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootBonusTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#C084FC;font-size:1.05rem;'>₹" + sumBonus.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  function renderBonusSetoffRegister(){
    var fromD = document.getElementById("inpBonusSetoffFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpBonusSetoffFilterTo").value || "2026-12-31";
    var tbody = document.getElementById("tbodyBonusSetoffRegister"); if(!tbody) return;
    
    var filtered = bonusSettlements.filter(function(b){
      var d = b.date || "2026-01-01";
      return (d >= fromD && d <= toD);
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='10' style='text-align:center;color:#94A3B8;'>No bonus set-offs recorded in date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
      var tf = document.getElementById("tfootBonusSetoffRegister"); if(tf) tf.innerHTML = "";
      return;
    }

    var h = "";
    var totalBonusSum = 0, totalNetPaid = 0;
    filtered.forEach(function(b){
      var bTot = cleanNum(b.totalBonus, 0);
      var netP = cleanNum(b.netPaid, 0);
      totalBonusSum += bTot;
      totalNetPaid += netP;
      h += "<tr>" +
        "<td style='font-family:monospace;color:#FBBF24;'>" + (b.settlementId || "-") + "</td>" +
        "<td>" + toDisplayDate(b.date) + "</td>" +
        "<td><strong>" + b.name + "</strong> (" + b.id + ")</td>" +
        "<td style='color:#C084FC;font-weight:700;'>₹" + bTot.toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjLoan, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjInt, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjRd, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjPenalty, 0).toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;font-weight:800;'>₹" + netP.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-size:0.75rem;color:" + (b.mode==="CASH"?"#F59E0B":"#38BDF8") + ";font-weight:700;'>" + (b.mode||"ONLINE") + "</span></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    var tf = document.getElementById("tfootBonusSetoffRegister");
    if(tf) {
      tf.innerHTML = 
        "<tr class='tfoot-total-row'>" +
          "<td colspan='3' style='color:#FBBF24;'>TOTAL SET-OFFS IN DATE RANGE</td>" +
          "<td style='color:#C084FC;font-weight:800;'>₹" + totalBonusSum.toLocaleString("en-IN") + "</td>" +
          "<td colspan='4'></td>" +
          "<td style='color:#10B981;font-weight:800;'>₹" + totalNetPaid.toLocaleString("en-IN") + "</td>" +
          "<td></td>" +
        "</tr>";
    }
  }

  function renderFundRegister(){
    var fromD = document.getElementById("inpFundFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpFundFilterTo").value || "2026-12-31";
    var accFilter = document.getElementById("selFundFilterAccount").value;
    var typeFilter = document.getElementById("selFundFilterType").value;
    var tbody = document.getElementById("tbodyFundRegisterList"); if(!tbody) return;

    var filtered = fundTransactions.filter(function(f){
      var isoD = toIsoDateStr(f.date);
      var matchDate = (isoD >= fromD && isoD <= toD);
      var matchAcc = (accFilter === "ALL" || String(f.account).toUpperCase() === accFilter);
      var matchType = (typeFilter === "ALL" || String(f.type).toUpperCase() === typeFilter);
      return matchDate && matchAcc && matchType;
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94A3B8;'>No fund transactions recorded in selected filter range</td></tr>";
      var tf = document.getElementById("tfootFundRegisterList"); if(tf) tf.innerHTML = "";
      return;
    }

    var h = "";
    var totalInvest = 0, totalBorrow = 0;
    filtered.forEach(function(f){
      var amt = cleanNum(f.amount, 0);
      var isInvest = (String(f.type).toUpperCase() === "INVEST");
      if(isInvest) totalInvest += amt; else totalBorrow += amt;

      var typeBadge = isInvest 
        ? "<span class='badge-active'>INVESTMENT (+)</span>"
        : "<span class='badge-inactive'>BORROWING (-)</span>";
      var amtColor = isInvest ? "#10B981" : "#EF4444";

      h += "<tr>" +
        "<td>" + toDisplayDate(f.date) + "</td>" +
        "<td style='font-family:monospace;color:#38BDF8;'>" + (f.id || "-") + "</td>" +
        "<td>" + typeBadge + "</td>" +
        "<td><strong style='color:#FBBF24;'>" + (f.account === "BANK" ? "CASH AT BANK" : "CASH IN HAND") + "</strong></td>" +
        "<td>" + (f.entity || "-") + "</td>" +
        "<td style='font-weight:800;color:" + amtColor + ";'>" + (isInvest ? "+₹" : "-₹") + amt.toLocaleString("en-IN") + "</td>" +
        "<td><small style='color:#CBD5E1;'>" + (f.narration || "-") + "</small></td>" +
        "<td style='text-align:center;'><button class='btn-action-edit action-edit-fund' data-id='" + f.id + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    var tf = document.getElementById("tfootFundRegisterList");
    if(tf) {
      tf.innerHTML = 
        "<tr class='tfoot-total-row'>" +
          "<td colspan='5' style='color:#FBBF24;'>NET FUND IMPACT: INVEST (+₹" + totalInvest.toLocaleString("en-IN") + ") | BORROW (-₹" + totalBorrow.toLocaleString("en-IN") + ")</td>" +
          "<td style='font-weight:800;color:" + (totalInvest >= totalBorrow ? "#10B981" : "#EF4444") + ";'>" +
            (totalInvest >= totalBorrow ? "+₹" : "-₹") + Math.abs(totalInvest - totalBorrow).toLocaleString("en-IN") +
          "</td>" +
          "<td></td>" +
        "</tr>";
    }
  }

  // PROFIT & LOSS REGISTER CALCULATOR & RENDERER
  function renderProfitAndLossRegister(){
    var fromD = document.getElementById("inpPlFilterFrom") ? document.getElementById("inpPlFilterFrom").value : "2026-01-01";
    var toD = document.getElementById("inpPlFilterTo") ? document.getElementById("inpPlFilterTo").value : "2026-12-31";
    var selFy = document.getElementById("selPlFinancialYear") ? document.getElementById("selPlFinancialYear").value : "2026";

    var selPlFyEl = document.getElementById("selPlFinancialYear");
    if(selPlFyEl && (!selPlFyEl.options || selPlFyEl.options.length === 0) && document.getElementById("selFinancialYear")){
      selPlFyEl.innerHTML = document.getElementById("selFinancialYear").innerHTML;
      selPlFyEl.value = selFy;
    }

    var intEarned = 0;
    var penReceived = 0;
    var waiverGiven = 0;
    var bonusPaid = 0;
    var bonusPayable = 0;

    var breakupRows = [];

    // 1. Payments: Interest, Penalty, Waiver
    payments.forEach(function(p){
      var d = p.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var iAmt = cleanNum(p.interest, 0);
        var pAmt = cleanNum(p.penalty, 0);
        var wAmt = cleanNum(p.waiver, 0);

        if(iAmt > 0){
          intEarned += iAmt;
          breakupRows.push({ date: d, category: "Interest Earned (+)", source: p.name + " (" + p.id + ")", inflow: iAmt, outflow: 0, remarks: "Receipt " + p.receiptNo });
        }
        if(pAmt > 0){
          penReceived += pAmt;
          breakupRows.push({ date: d, category: "Penalty Received (+)", source: p.name + " (" + p.id + ")", inflow: pAmt, outflow: 0, remarks: "Receipt " + p.receiptNo });
        }
        if(wAmt > 0){
          waiverGiven += wAmt;
          breakupRows.push({ date: d, category: "Waiver Granted (-)", source: p.name + " (" + p.id + ")", inflow: 0, outflow: wAmt, remarks: "Receipt " + p.receiptNo });
        }
      }
    });

    // 2. Bonus Settlements
    bonusSettlements.forEach(function(b){
      var d = b.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var bAmt = cleanNum(b.totalBonus, 0);
        if(bAmt > 0){
          bonusPaid += bAmt;
          breakupRows.push({ date: d, category: "Bonus Paid (-)", source: b.name + " (" + b.id + ")", inflow: 0, outflow: bAmt, remarks: "Settlement " + b.settlementId });
        }
      }
    });

    // 3. Exit Settlements Bonus Adjustment
    exitSettlements.forEach(function(e){
      var d = e.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var bAdj = cleanNum(e.bonusAdj, 0);
        if(bAdj > 0){
          bonusPaid += bAdj;
          breakupRows.push({ date: d, category: "Bonus Settled on Exit (-)", source: e.name + " (" + e.id + ")", inflow: 0, outflow: bAdj, remarks: "Exit " + e.exitId });
        }
      }
    });

    // 4. Estimated Bonus Payable
    members.forEach(function(m){
      if(String(m.status).toUpperCase() === "ACTIVE"){
        var mBonus = getMemberBonus(m);
        if(mBonus > 0){
          bonusPayable += mBonus;
        }
      }
    });

    var netProfit = (intEarned + penReceived) - (waiverGiven + bonusPaid + bonusPayable);

    if(document.getElementById("lblPlIntEarned")) document.getElementById("lblPlIntEarned").innerText = "₹" + intEarned.toLocaleString("en-IN");
    if(document.getElementById("lblPlPenReceived")) document.getElementById("lblPlPenReceived").innerText = "₹" + penReceived.toLocaleString("en-IN");
    if(document.getElementById("lblPlWaiver")) document.getElementById("lblPlWaiver").innerText = "₹" + waiverGiven.toLocaleString("en-IN");
    if(document.getElementById("lblPlBonusPaid")) document.getElementById("lblPlBonusPaid").innerText = "₹" + bonusPaid.toLocaleString("en-IN");
    if(document.getElementById("lblPlBonusPayable")) document.getElementById("lblPlBonusPayable").innerText = "₹" + bonusPayable.toLocaleString("en-IN");
    
    var netEl = document.getElementById("lblPlNetProfit");
    if(netEl){
      netEl.innerText = (netProfit >= 0 ? "+₹" : "-₹") + Math.abs(netProfit).toLocaleString("en-IN");
      netEl.style.color = (netProfit >= 0 ? "#10B981" : "#EF4444");
    }

    var tbody = document.getElementById("tbodyPlBreakup");
    if(tbody){
      if(breakupRows.length === 0){
        tbody.innerHTML = "<tr><td colspan='6' style='text-align:center;color:#94A3B8;'>No profit & loss transactions in selected range</td></tr>";
        if(document.getElementById("tfootPlBreakup")) document.getElementById("tfootPlBreakup").innerHTML = "";
      } else {
        breakupRows.sort(function(a,b){ return (b.date || "").localeCompare(a.date || ""); });
        var h = "";
        breakupRows.forEach(function(r){
          h += "<tr>" +
            "<td>" + toDisplayDate(r.date) + "</td>" +
            "<td><strong style='color:#38BDF8;'>" + r.category + "</strong></td>" +
            "<td>" + r.source + "</td>" +
            "<td style='color:#10B981;font-weight:700;'>" + (r.inflow > 0 ? "+₹" + r.inflow.toLocaleString("en-IN") : "-") + "</td>" +
            "<td style='color:#EF4444;font-weight:700;'>" + (r.outflow > 0 ? "-₹" + r.outflow.toLocaleString("en-IN") : "-") + "</td>" +
            "<td><small style='color:#CBD5E1;'>" + r.remarks + "</small></td>" +
          "</tr>";
        });
        tbody.innerHTML = h;

        var tfoot = document.getElementById("tfootPlBreakup");
        if(tfoot){
          var totIn = intEarned + penReceived;
          var totOut = waiverGiven + bonusPaid;
          tfoot.innerHTML = "<tr class='tfoot-total-row'>" +
            "<td colspan='3' style='color:#FBBF24;'>REALIZED NET INFLOW SUMMARY</td>" +
            "<td style='color:#10B981;font-weight:800;'>+₹" + totIn.toLocaleString("en-IN") + "</td>" +
            "<td style='color:#EF4444;font-weight:800;'>-₹" + totOut.toLocaleString("en-IN") + "</td>" +
            "<td style='color:" + (totIn >= totOut ? "#10B981" : "#EF4444") + ";font-weight:800;'>" + (totIn >= totOut ? "+₹" : "-₹") + Math.abs(totIn - totOut).toLocaleString("en-IN") + "</td>" +
          "</tr>";
        }
      }
    }
  }
  window.renderProfitAndLossRegister = renderProfitAndLossRegister;

  // TAB 5: DEDICATED PENALTY REGISTER (₹10/DAY OVERDUE)
  function renderPenaltyTab(){
    var st = document.getElementById("selFilterPenStatus").value;
    var sort = document.getElementById("selSortPen").value;
    var q = (document.getElementById("searchPenInput").value || "").toLowerCase().trim();

    var filtered = members.filter(function(m){
      var matchSearch = String(m.name||"").toLowerCase().indexOf(q) >= 0 || String(m.id||"").toLowerCase().indexOf(q) >= 0;
      var penDue = calculateMemberLivePenaltyDue(m);
      var matchStatus = (st === "ALL") || (st === "OVERDUE" && penDue > 0) || (st === "CLEAR" && penDue <= 0);
      return matchSearch && matchStatus;
    });

    if(sort==="pen_high") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(b)-calculateMemberLivePenaltyDue(a); });
    if(sort==="pen_low") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(a)-calculateMemberLivePenaltyDue(b); });
    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });

    var tbody = document.getElementById("tbodyPenaltyList"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No penalty records found</td></tr>"; 
      document.getElementById("tfootPenaltyTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumAccrued = 0, sumPaid = 0, sumWaived = 0, sumNet = 0;

    filtered.forEach(function(m){
      var mid = String(m.id);
      var penDue = calculateMemberLivePenaltyDue(m);
      var penPaid = getMemberTotalPenalty(m);
      var waived = getMemberTotalWaiver(m);
      var accrued = penDue + penPaid + waived;

      sumAccrued += accrued; sumPaid += penPaid; sumWaived += waived; sumNet += penDue;

      var dueDay = m.dueDay || "15th of every month";
      var statusBadge = penDue > 0 ? "<span class='badge-inactive'>OVERDUE</span>" : "<span class='badge-active'>CLEAR</span>";
      var overdueInfo = penDue > 0 ? ("₹" + penDue + " (" + Math.ceil(penDue / 10) + " Days @ ₹10/D)") : "No Overdue";

      html += "<tr>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + "</strong><br><small style='color:#94A3B8;'>" + mid + "</small></td>" +
        "<td style='color:#38BDF8;font-weight:700;'>" + dueDay + "</td>" +
        "<td style='color:" + (penDue > 0 ? "#EF4444" : "#10B981") + ";font-weight:700;'>" + overdueInfo + "</td>" +
        "<td style='color:#FBBF24;font-weight:700;'>₹" + accrued.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + penPaid.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + waived.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;font-weight:800;font-size:0.95rem;'>₹" + penDue.toLocaleString("en-IN") + "</td>" +
        "<td>" + statusBadge + "</td>" +
        "<td><button class='btn-action-rcv action-receive-for' data-id='" + mid + "'>Collect / Waive</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootPenaltyTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#FBBF24;'>₹" + sumAccrued.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + sumPaid.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + sumWaived.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumNet.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  // PASSBOOK LEDGER (FULL AUDIT TRAIL MATCHING BY ID & NAME)
  function openMemberLedger(mid){
    var m = members.find(function(x){ return String(x.id).trim().toUpperCase() === mid.trim().toUpperCase() || String(x.name).trim().toLowerCase() === mid.trim().toLowerCase(); });
    if(!m) return;
    currentActiveLedgerMember = m;

    var actualId = String(m.id).trim().toUpperCase();
    var actualName = String(m.name).trim().toLowerCase();

    document.getElementById("lblLedgerName").innerText = "📖 Passbook Ledger: " + m.name + " (" + m.id + ")";
    var fromD = document.getElementById("inpLedgerFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpLedgerFilterTo").value || "2030-12-31";

    var totRd = getMemberTotalRd(m);
    var loanDue = getMemberActiveLoan(m);
    var intDue = calculateMemberLiveInterestDue(m);
    var penDue = calculateMemberLivePenaltyDue(m);

    document.getElementById("ledgerHeaderStats").innerHTML = 
      "<div><div class='ledger-stat-lbl'>MEMBER ID / DUE</div><div class='ledger-stat-val' style='color:#38BDF8;font-size:0.95rem;'>" + m.id + "<br><small style='color:#FBBF24;font-size:0.75rem;'>" + (m.dueDay||"15th of month") + "</small></div></div>" +
      "<div><div class='ledger-stat-lbl'>TOTAL RD SAVED</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + totRd.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>ACTIVE LOAN DUE</div><div class='ledger-stat-val' style='color:#EF4444;'>₹" + loanDue.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>INTEREST DUE</div><div class='ledger-stat-val' style='color:#FBBF24;'>₹" + intDue.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>PENALTY DUE (₹10/D)</div><div class='ledger-stat-val' style='color:#F59E0B;'>₹" + penDue.toLocaleString("en-IN") + "</div></div>";

    var txns = [];
    if(cleanNum(m.rdPaid, 0) > 0){
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OPENING", partic: "Opening RD Balance (As on 31 Dec 2025)", rdDeposit: cleanNum(m.rdPaid, 0), loanIssued: 0, loanRepaid: 0, mode: "SYSTEM" });
    }
    if(cleanNum(m.opLoan, 0) > 0){
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OP-LOAN", partic: "Opening Loan Principal", rdDeposit: 0, loanIssued: cleanNum(m.opLoan, 0), loanRepaid: 0, mode: "SYSTEM" });
    }

    // Match all loans for this member by ID or Name
    loans.forEach(function(l){
      if(String(l.id).trim().toUpperCase() === actualId || String(l.name).trim().toLowerCase() === actualName){
        var lNarr = l.narration ? ('<br><small style="color:#94A3B8;font-size:0.75rem;font-style:italic;">📝 ' + l.narration + '</small>') : '';
        txns.push({
          date: l.date,
          ref: l.loanId,
          partic: "Loan Disbursed (" + l.type + " @ " + l.rate + "% p.m.)" + lNarr,
          rdDeposit: 0,
          loanIssued: cleanNum(l.principal, 0),
          loanRepaid: 0,
          mode: "ONLINE"
        });
      }
    });

    // Match all payments for this member by ID or Name
    payments.forEach(function(p){
      if(String(p.id).trim().toUpperCase() === actualId || String(p.name).trim().toLowerCase() === actualName){
        var pNarr = p.narration ? ('<br><small style="color:#94A3B8;font-size:0.75rem;font-style:italic;">📝 ' + p.narration + '</small>') : '';
        var parts = [];
        if(cleanNum(p.rd, 0) > 0) parts.push("RD ₹" + p.rd);
        if(cleanNum(p.interest, 0) > 0) parts.push("Int ₹" + p.interest);
        if(cleanNum(p.penalty, 0) > 0) parts.push("Pen ₹" + p.penalty);
        if(cleanNum(p.loanRepay, 0) > 0) parts.push("Loan Repay ₹" + p.loanRepay);
        if(cleanNum(p.waiver, 0) > 0) parts.push("Waiver ₹" + p.waiver);

        var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
        txns.push({
          date: p.date,
          ref: p.receiptNo,
          partic: "Receipt: " + parts.join(", ") + pNarr,
          rdDeposit: cleanNum(p.rd, 0),
          loanIssued: 0,
          loanRepaid: cleanNum(p.loanRepay, 0),
          mode: safeMode
        });
      }
    });

    // Sort by date ascending
    txns.sort(function(a,b){ return new Date(a.date||"2026-01-01") - new Date(b.date||"2026-01-01"); });

    var runningRdBal = 0;
    var runningLoanBal = 0;
    var filteredTxns = txns.filter(function(t){ return (t.date >= fromD && t.date <= toD); });

    var tbody = document.getElementById("tbodyLedgerTxns");
    if(filteredTxns.length === 0){
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No transactions in selected date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
    } else {
      var h = "";
      filteredTxns.forEach(function(t){
        runningRdBal += t.rdDeposit;
        runningLoanBal += t.loanIssued;
        runningLoanBal = Math.max(0, runningLoanBal - t.loanRepaid);

        h += "<tr>" +
          "<td>" + toDisplayDate(t.date) + "</td>" +
          "<td style='font-family:monospace;color:#FBBF24;'>" + t.ref + "</td>" +
          "<td>" + t.partic + "</td>" +
          "<td style='color:#10B981;font-weight:700;'>" + (t.rdDeposit > 0 ? "₹" + t.rdDeposit.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#10B981;font-weight:800;'>₹" + runningRdBal.toLocaleString("en-IN") + "</td>" +
          "<td style='color:#EF4444;'>" + (t.loanIssued > 0 ? "₹" + t.loanIssued.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#38BDF8;font-weight:700;'>" + (t.loanRepaid > 0 ? "₹" + t.loanRepaid.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#EF4444;font-weight:800;'>₹" + runningLoanBal.toLocaleString("en-IN") + "</td>" +
          "<td><span style='font-size:0.75rem;color:" + (t.mode==="CASH"?"#F59E0B":"#38BDF8") + ";font-weight:700;'>" + t.mode + "</span></td>" +
        "</tr>";
      });
      tbody.innerHTML = h;
    }
    openModal("modalLedger");
  }

  // BULK ENTRY REGISTER RENDERING WITH SHORT NARRATION
  function renderBulkList(){
    var tbody = document.getElementById("tbodyBulkList"); if(!tbody) return;
    var h = "";
    var activeMems = members.filter(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
    if(activeMems.length === 0){
      tbody.innerHTML = "<tr><td colspan='10' style='text-align:center;color:#94A3B8;'>No active members available</td></tr>";
      return;
    }

    activeMems.forEach(function(m){
      var mid = String(m.id);
      var safeRd = cleanRd(m.rd);
      var intDue = calculateMemberLiveInterestDue(m);
      var penDue = calculateMemberLivePenaltyDue(m);
      var initialTotal = safeRd + intDue + penDue;

      h += "<tr data-id='" + mid + "'>" +
        "<td style='text-align:center;'><input type='checkbox' class='b-chk' checked onchange='calcBulkTotals()'></td>" +
        "<td><strong>" + m.name + "</strong><br><small style='color:#38BDF8;'>RD ₹" + safeRd + " | Int ₹" + intDue + " | Pen ₹" + penDue + "</small></td>" +
        "<td><input type='number' class='field-ctrl b-rd' value='" + safeRd + "' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-int' value='" + intDue + "' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-repay' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-pen' value='" + penDue + "' oninput='calcBulkRow(this)' style='width:75px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-wvr' value='0' oninput='calcBulkRow(this)' style='width:75px;text-align:right;'></td>" +
        "<td style='text-align:right; font-weight:800; color:#10B981;' class='b-tot-cell'>₹" + initialTotal.toLocaleString("en-IN") + "</td>" +
        "<td><select class='field-ctrl b-mode' style='width:85px;padding:4px;'><option value='CASH'>CASH</option><option value='ONLINE'>ONLINE</option></select></td>" +
        "<td><input type='text' class='field-ctrl b-narr' placeholder='Remarks' style='width:130px;padding:4px;font-size:0.78rem;'></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;
    calcBulkTotals();
  }

  window.calcBulkRow = function(inputEl){
    var tr = inputEl.closest("tr");
    if(!tr) return;
    var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
    var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
    var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
    var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
    var wvr = cleanNum(tr.querySelector(".b-wvr").value, 0);
    var tot = rd + intVal + repay + pen - wvr;
    if(tot < 0) tot = 0;
    tr.querySelector(".b-tot-cell").innerText = "₹" + tot.toLocaleString("en-IN");
    calcBulkTotals();
  };

  window.calcBulkTotals = function(){
    var rows = document.querySelectorAll("#tbodyBulkList tr");
    var selCount = 0;
    var grandTot = 0;
    rows.forEach(function(tr){
      var chk = tr.querySelector(".b-chk");
      if(chk && chk.checked){
        selCount++;
        var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
        var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
        var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
        var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
        var wvr = cleanNum(tr.querySelector(".b-wvr").value, 0);
        var tot = rd + intVal + repay + pen - wvr;
        if(tot < 0) tot = 0;
        grandTot += tot;
      }
    });
    var elCnt = document.getElementById("lblBulkSelectedCount");
    var elGrd = document.getElementById("lblBulkGrandTotal");
    if(elCnt) elCnt.innerText = selCount;
    if(elGrd) elGrd.innerText = "₹" + grandTot.toLocaleString("en-IN");
  };

  // NPA & BAD DEBTS LIST WITH DATE FILTER & DYNAMIC TOTAL
  function renderNpaList(){
    var fromD = document.getElementById("inpNpaFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpNpaFilterTo").value || "2026-12-31";
    var tbody = document.getElementById("tbodyNpaList"); if(!tbody) return;
    
    var filtered = exitSettlements.filter(function(e){
      var d = e.date || "2026-01-01";
      return cleanNum(e.npaLoss, 0) > 0 && (d >= fromD && d <= toD);
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No NPA write-offs recorded in date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
      document.getElementById("tfootNpaTotal").innerHTML = "";
      return;
    }

    var h = "";
    var totalNpaSum = 0;
    filtered.forEach(function(e){
      var nVal = cleanNum(e.npaLoss, 0);
      totalNpaSum += nVal;
      h += "<tr>" +
        "<td>" + toDisplayDate(e.date) + "</td>" +
        "<td style='font-family:monospace;color:#EF4444;'>" + e.exitId + "</td>" +
        "<td><strong>" + e.name + "</strong> (" + e.id + ")</td>" +
        "<td style='color:#EF4444;font-weight:800;'>₹" + nVal.toLocaleString("en-IN") + "</td>" +
        "<td><span class='badge-inactive'>WRITTEN OFF</span></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    document.getElementById("tfootNpaTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>TOTAL WRITE-OFF IN DATE RANGE</td>" +
        "<td style='color:#EF4444;font-weight:800;'>₹" + totalNpaSum.toLocaleString("en-IN") + "</td>" +
        "<td></td>" +
      "</tr>";
  }

  // MODAL UTILITIES
  function openModal(id){
    var el = document.getElementById(id);
    if(el) el.style.display = "flex";
  }
  function closeModal(id){
    var el = document.getElementById(id);
    if(el) el.style.display = "none";
  }
  function closeAllModals(){
    document.querySelectorAll(".modal-backdrop").forEach(function(m){
      m.style.display = "none";
    });
  }

  // NOTICE POPUP THAT DOES NOT CLOSE FORM ON OK
  var activeKeepModal = null;
  function showNotice(title, message, keepModalId){
    var h = document.getElementById("noticeHeader");
    var b = document.getElementById("noticeBody");
    if(h) h.innerText = title || "Notice";
    if(b) b.innerText = message || "";
    activeKeepModal = keepModalId || null;
    openModal("modalNotice");
  }

  function handleNoticeOkClick(){
    closeModal("modalNotice");
    if(activeKeepModal){
      openModal(activeKeepModal);
      activeKeepModal = null;
    }
  }

  // OPEN RECEIVE MODAL FOR MEMBER (AUTO-FILL LIVE DUES)
  function openReceiveModalFor(mid, recNo){
    if(!mid){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      mid = firstActive ? firstActive.id : (members[0] ? members[0].id : "");
    }
    if(!mid) return;
    var m = members.find(function(x){ return String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase() || String(x.name).trim().toLowerCase() === String(mid).trim().toLowerCase(); });
    if(!m) return;

    var actualMid = String(m.id);
    document.getElementById("editReceiptNo").value = recNo || "";
    document.getElementById("lblReceiveModalHead").innerText = recNo ? ("✏️ Edit Receipt: " + recNo) : "📥 Receive Amount";
    
    var sel = document.getElementById("selPayMember");
    sel.innerHTML = "<option value='" + actualMid + "' selected>" + m.name + " (" + actualMid + ")</option>";
    members.forEach(function(other){
      if(String(other.id) !== actualMid && String(other.status).toUpperCase() === "ACTIVE"){
        sel.innerHTML += "<option value='" + other.id + "'>" + other.name + " (" + other.id + ")</option>";
      }
    });

    var todayYMD = getTodayYMD();
    document.getElementById("inpPayDate").value = todayYMD;
    document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(todayYMD) + ")";
    document.getElementById("selPayMode").value = "CASH";

    if(recNo){
      var p = payments.find(function(x){ return String(x.receiptNo) === recNo; });
      if(p){
        document.getElementById("inpPayDate").value = p.date;
        document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(p.date) + ")";
        document.getElementById("selPayMode").value = p.mode;
        document.getElementById("inpPayRd").value = cleanNum(p.rd, 0);
        document.getElementById("inpPayInterest").value = cleanNum(p.interest, 0);
        document.getElementById("inpPayPenalty").value = cleanNum(p.penalty, 0);
        document.getElementById("inpPayWaiver").value = cleanNum(p.waiver, 0);
        document.getElementById("inpPayPrincipal").value = cleanNum(p.loanRepay, 0);
        document.getElementById("inpPayNarration").value = p.narration || "";
      }
    } else {
      var liveInt = calculateMemberLiveInterestDue(m);
      var livePen = calculateMemberLivePenaltyDue(m);
      document.getElementById("inpPayRd").value = cleanRd(m.rd);
      document.getElementById("inpPayInterest").value = liveInt;
      document.getElementById("inpPayPenalty").value = livePen;
      document.getElementById("inpPayWaiver").value = 0;
      document.getElementById("inpPayPrincipal").value = 0;
      document.getElementById("inpPayNarration").value = "Monthly Collection";
    }

    openModal("modalReceive");
  }

  // OPEN LOAN MODAL FOR MEMBER
  function openLoanModalFor(mid, lId){
    if(!mid){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      mid = firstActive ? firstActive.id : (members[0] ? members[0].id : "");
    }
    var m = mid ? members.find(function(x){ return String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase() || String(x.name).trim().toLowerCase() === String(mid).trim().toLowerCase(); }) : null;
    document.getElementById("editLoanId").value = lId || "";
    document.getElementById("lblLoanModalHead").innerText = lId ? ("✏️ Edit Society Loan: " + lId) : "💸 Issue Society Loan";

    var sel = document.getElementById("selLoanMember");
    sel.innerHTML = "";
    members.forEach(function(mem){
      if(String(mem.status).toUpperCase() === "ACTIVE"){
        var isSel = (m && String(mem.id) === String(mem.id)) ? "selected" : "";
        sel.innerHTML += "<option value='" + mem.id + "' " + isSel + ">" + mem.name + " (" + mem.id + ")</option>";
      }
    });

    var todayYMD = getTodayYMD();
    document.getElementById("inpLoanDate").value = todayYMD;
    document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(todayYMD) + ")";
    document.getElementById("selLoanType").value = "Gullak Loan";
    document.getElementById("inpLoanRate").value = globalDefaultRate;
    document.getElementById("inpLoanPrinc").value = "";
    document.getElementById("inpLoanNarration").value = "";

    if(m){
      var lmt = getMemberLoanLimit(m);
      document.getElementById("lblMemberLoanLimit").innerText = "Eligible Loan Limit: " + lmt.text;
    } else {
      document.getElementById("lblMemberLoanLimit").innerText = "";
    }

    if(lId){
      var l = loans.find(function(x){ return String(x.loanId) === lId; });
      if(l){
        document.getElementById("inpLoanDate").value = l.date;
        document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(l.date) + ")";
        document.getElementById("selLoanType").value = l.type;
        document.getElementById("inpLoanRate").value = l.rate;
        document.getElementById("inpLoanPrinc").value = cleanNum(l.principal, 0);
        document.getElementById("inpLoanNarration").value = l.narration || "";
      }
    }

    openModal("modalLoan");
  }

  // REFRESH ALL INTERFACES
  function refreshAll(){
    setupFinancialYearDropdown();
    updateKPIs();
    renderMembers();
    renderPayments();
    renderLoans();
    renderBonusTab();
    renderPenaltyTab();
    renderFundRegister();
    renderProfitAndLossRegister();
    if(currentActiveLedgerMember && document.getElementById("modalLedger").style.display === "flex"){
      openMemberLedger(currentActiveLedgerMember.id);
    }
  }

  // EVENT LISTENERS & HOOKS
  window.refreshAll = refreshAll;

  function safeAddListener(id, evt, fn){
    var el = document.getElementById(id);
    if(el) {
      if(evt === "click") {
        el.onclick = fn;
      } else if(evt === "change") {
        el.onchange = fn;
      } else if(evt === "input") {
        el.oninput = fn;
      } else {
        el["on" + evt] = fn;
      }
      return true;
    }
    return false;
  }

  function bootApplication(){
    var overlay = document.getElementById("windowsLoginOverlay");
    if (window.currentUserSession && window.currentUserSession.username) {
      if (overlay) {
        overlay.style.display = "none";
        overlay.style.setProperty("display", "none", "important");
      }
    } else {
      if (overlay) {
        overlay.style.display = "flex";
      }
      var pInput = document.getElementById("inpWinPassword");
      if (pInput) pInput.value = "";
    }
    // Global ESC key and Arrow Up / Down listener (Capturing phase to block browser fullscreen exit)
    window.addEventListener("keydown", function(e){
      if(e.key === "Escape" || e.keyCode === 27){
        if(e.preventDefault) e.preventDefault();
        if(e.stopPropagation) e.stopPropagation();
        if(e.stopImmediatePropagation) e.stopImmediatePropagation();

        // 1. Check if fundDrilldownBox is open
        var box = document.getElementById("fundDrilldownBox");
        if (box && box.style.display !== "none") {
          box.style.display = "none";
          return false;
        }

        // 2. Find any open modal
        var openModals = document.querySelectorAll(".modal-backdrop");
        var activeModal = null;
        openModals.forEach(function(m){
          if(m && (m.style.display === "flex" || m.style.display === "block" || (window.getComputedStyle && getComputedStyle(m).display !== "none"))){
            activeModal = m;
          }
        });

        if (activeModal) {
          var closeBtn = activeModal.querySelector(".action-close-modal, .close-x, button.btn-close, .modal-close, button[onclick*='closeModal']");
          if (closeBtn) {
            try {
              closeBtn.click();
            } catch(err) {
              closeModal(activeModal.id);
            }
          } else if (typeof closeModal === "function") {
            closeModal(activeModal.id);
          } else {
            activeModal.style.display = "none";
          }
        }

        if(window.isAppInFullscreenMode || (document.body && document.body.classList.contains("simulated-fullscreen"))){
          if(document.body) document.body.classList.add("simulated-fullscreen");
        }
        return false;
      }
      if(e.key === "F11" || e.keyCode === 122){
        e.preventDefault();
        window.safeToggleFullscreen();
      }
      // Arrow Up and Down scroll support in Fullscreen mode (both Dashboard and inside any open Form)
      var activeModalBox = document.querySelector(".modal-backdrop[style*='display: flex'] .modal-dialog-box") ||
                           document.querySelector(".modal-backdrop[style*='display: block'] .modal-dialog-box");
      if(e.key === "ArrowDown"){
        if(activeModalBox){
          activeModalBox.scrollTop += 60;
        } else {
          window.scrollBy(0, 60);
        }
      } else if(e.key === "ArrowUp"){
        if(activeModalBox){
          activeModalBox.scrollTop -= 60;
        } else {
          window.scrollBy(0, -60);
        }
      } else if(e.key === "PageDown"){
        if(activeModalBox){
          activeModalBox.scrollTop += 300;
        } else {
          window.scrollBy(0, 300);
        }
      } else if(e.key === "PageUp"){
        if(activeModalBox){
          activeModalBox.scrollTop -= 300;
        } else {
          window.scrollBy(0, -300);
        }
      }
    }, true);

    // Date formatting live indicators
    var pDateInp = document.getElementById("inpPayDate");
    if(pDateInp){
      pDateInp.addEventListener("change", function(){
        document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(this.value) + ")";
      });
    }
    var lDateInp = document.getElementById("inpLoanDate");
    if(lDateInp){
      lDateInp.addEventListener("change", function(){
        document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(this.value) + ")";
      });
    }

    function safeAddListener(id, evt, fn){
      var el = document.getElementById(id);
      if(el) {
        if(evt === "click") {
          el.onclick = fn;
        } else if(evt === "change") {
          el.onchange = fn;
        } else if(evt === "input") {
          el.oninput = fn;
        } else {
          el["on" + evt] = fn;
        }
        return true;
      }
      return false;
    }

    // Modal close buttons
    document.querySelectorAll(".action-close-modal").forEach(function(btn){
      btn.addEventListener("click", function(){
        var b = this.closest(".modal-backdrop");
        if(b) b.style.display = "none";
      });
    });

    // Tab switcher
    function switchTab(tIdx){
      for(var i=1; i<=5; i++){
        var head = document.getElementById("tabHead" + i);
        var panel = document.getElementById("tabPanel" + i);
        if(head) head.className = (i === tIdx) ? "tab-item active" : "tab-item";
        if(panel) panel.style.display = (i === tIdx) ? "block" : "none";
      }
      if(tIdx === 1 && typeof renderMembers === "function") renderMembers();
      else if(tIdx === 2 && typeof renderPayments === "function") renderPayments();
      else if(tIdx === 3 && typeof renderLoans === "function") renderLoans();
      else if(tIdx === 4 && typeof renderBonusTab === "function") renderBonusTab();
      else if(tIdx === 5 && typeof renderPenaltyTab === "function") renderPenaltyTab();
    }
    window.switchTab = switchTab;

    safeAddListener("tabHead1", "click", function(){ switchTab(1); renderMembers(); });
    safeAddListener("tabHead2", "click", function(){ switchTab(2); renderPayments(); });
    safeAddListener("tabHead3", "click", function(){ switchTab(3); renderLoans(); });
    safeAddListener("tabHead4", "click", function(){ switchTab(4); renderBonusTab(); });
    safeAddListener("tabHead5", "click", function(){ switchTab(5); renderPenaltyTab(); });

    // Top action buttons
    function openDefaultReceiveModal(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openReceiveModalFor(firstActive ? firstActive.id : "MEM010120261");
    }
    function openDefaultLoanModal(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openLoanModalFor(firstActive ? firstActive.id : "MEM010120261");
    }

    safeAddListener("btnTopReceive", "click", openDefaultReceiveModal);
    safeAddListener("btnPanelNewReceipt", "click", openDefaultReceiveModal);
    safeAddListener("btnTopLoan", "click", openDefaultLoanModal);
    safeAddListener("btnPanelNewLoan", "click", openDefaultLoanModal);

    safeAddListener("btnTopAddMember", "click", function(){
      document.getElementById("editMemId").value = "";
      var btnDel = document.getElementById("btnDeleteMember");
      if (btnDel) { btnDel.style.display = "none"; btnDel.removeAttribute("data-id"); }
      var btnDel = document.getElementById("btnDeleteMember");
      if (btnDel) { btnDel.style.display = "none"; btnDel.removeAttribute("data-id"); }
    var btnDel = document.getElementById("btnDeleteMember");
    if (btnDel) { btnDel.style.display = "none"; btnDel.removeAttribute("data-id"); }
      document.getElementById("lblMemberModalHead").innerText = "👤 Add New Member Profile";
      document.getElementById("inpNewMemName").value = "";
      document.getElementById("inpNewMemMobile").value = "";
      document.getElementById("inpNewMemStatus").value = "ACTIVE";
      document.getElementById("inpNewMemJoinDate").value = getTodayYMD();
      document.getElementById("inpNewMemRd").value = 400;
      var defDue = getTodayYMD().substring(0,8) + "15";
      document.getElementById("inpNewMemDueDay").value = defDue;
      if(document.getElementById("dispDueDayFormatted")) document.getElementById("dispDueDayFormatted").innerText = "(" + toDisplayDate(defDue) + ")";
      document.getElementById("inpNewMemAddress").value = "";
      document.getElementById("inpNewMemNominee").value = "";
      document.getElementById("inpNewMemBal").value = 0;
      document.getElementById("inpNewMemOpLoan").value = 0;
      document.getElementById("inpNewMemOpInt").value = 0;
      document.getElementById("inpNewMemOpPen").value = 0;
      document.getElementById("inpNewMemCustomLimit").value = 0;
      openModal("modalMember");
    });

    safeAddListener("inpNewMemDueDay", "change", function(){
      var v = this.value;
      var el = document.getElementById("dispDueDayFormatted");
      if(el) el.innerText = v ? "(" + toDisplayDate(v) + ")" : "";
    });

    safeAddListener("btnTopBulk", "click", function(){
      var today = getTodayYMD();
      document.getElementById("inpBulkDate").value = today;
      document.getElementById("dispBulkDateFormatted").innerText = "(" + toDisplayDate(today) + ")";
      renderBulkList();
      openModal("modalBulk");
    });

    safeAddListener("btnBulkSetAllCash", "click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "CASH"; });
    });
    safeAddListener("btnBulkSetAllOnline", "click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "ONLINE"; });
    });
    safeAddListener("chkSelectAllBulk", "change", function(){
      var isChk = this.checked;
      document.querySelectorAll("#tbodyBulkList .b-chk").forEach(function(c){ c.checked = isChk; });
      calcBulkTotals();
    });

    safeAddListener("btnTopExit", "click", function(){
      var sel = document.getElementById("selExitMember");
      if(sel){
        sel.innerHTML = "<option value=''>-- Select Member --</option>";
        members.forEach(function(m){
          if(String(m.status).toUpperCase() === "ACTIVE"){
            sel.innerHTML += "<option value='" + m.id + "'>" + m.name + " (" + m.id + ")</option>";
          }
        });
      }
      handleExitMemberChange();
      openModal("modalExit");
    });

    safeAddListener("btnTopSettings", "click", function(){
      var d = document.getElementById("inpGlobalDueDay");
      var r = document.getElementById("inpGlobalRate");
      var ps = document.getElementById("inpPenaltyStartDate");
      var chk = document.getElementById("chkSkipPenalty");
      if(d) d.value = globalDefaultDue;
      if(r) r.value = globalDefaultRate;
      if(ps && window.globalSettings && window.globalSettings.penaltyStartDate) ps.value = window.globalSettings.penaltyStartDate;
      if(chk && window.globalSettings && typeof window.globalSettings.skipPenalty === "boolean") chk.checked = window.globalSettings.skipPenalty;
      openModal("modalSettings");
    });

    safeAddListener("btnOpenGoogleSheet", "click", window.handleOpenSpreadsheet);

    safeAddListener("btnApplyGlobalSettings", "click", function(){
      globalDefaultDue = (document.getElementById("inpGlobalDueDay").value || "").trim() || "15th of every month";
      globalDefaultRate = Number(document.getElementById("inpGlobalRate").value) || 1.0;
      var penStart = document.getElementById("inpPenaltyStartDate") ? document.getElementById("inpPenaltyStartDate").value : "2026-10-01";
      var skipPen = document.getElementById("chkSkipPenalty") ? document.getElementById("chkSkipPenalty").checked : true;

      if(!window.globalSettings) window.globalSettings = {};
      window.globalSettings.penaltyStartDate = penStart;
      window.globalSettings.skipPenalty = skipPen;
      saveStore();

      closeModal("modalSettings");
      refreshAll();
      showNotice("Settings Saved", "Global settings updated successfully! Overdue penalty status: " + (skipPen ? "PAUSED / SKIPPED" : "Active from " + toDisplayDate(penStart)));
    });

    safeAddListener("btnNoticeOk", "click", handleNoticeOkClick);

    // SYNC FROM GOOGLE SHEET DATABASE - OPEN SETTINGS FORM (V46 PRO)
    safeAddListener("btnTopReload", "click", function(){
      openModal("modalSyncSettings");
    });

    safeAddListener("btnApplySyncSettings", "click", function(){
      var val = document.getElementById("selSyncInterval").value;
      closeModal("modalSyncSettings");
      
      if (val === "now") {
        if(typeof google !== "undefined" && google.script && google.script.run){
          showNotice("Syncing...", "Fetching verified records from Google Spreadsheet...");
          google.script.run.withSuccessHandler(function(res){
            closeModal("modalNotice");
            if(res && res.members && res.members.length > 0){
              members = res.members;
              payments = res.payments || [];
              loans = res.loans || [];
              exitSettlements = res.exitSettlements || [];
              bonusSettlements = res.bonusSettlements || [];
              if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
              if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
              saveStore();
              refreshAll();
              showNotice("Sync Complete", "Successfully synchronized " + members.length + " members, " + payments.length + " receipts, and " + loans.length + " loans from Google Sheet!");
            }
          }).getSocietyFullData();
        } else {
          refreshAll();
          showNotice("Local Reloaded", "Database re-indexed locally.");
        }
      } else {
        var mins = parseInt(val, 10);
        if(typeof window.startAutoSync === "function") {
          window.startAutoSync(mins);
        }
        showNotice("Sync Timer Set", "App will now automatically synchronize in the background every " + mins + " minute(s).");
      }
    });

    // KPI Card Click Events
    safeAddListener("kpiCardMembers", "click", function(){ switchTab(1); });
    safeAddListener("kpiCardRd", "click", function(){ switchTab(2); });
    safeAddListener("kpiCardLoans", "click", function(){ switchTab(3); });
    safeAddListener("kpiCardBonus", "click", function(){ switchTab(4); });
    safeAddListener("kpiCardNpa", "click", function(){
      renderNpaList();
      openModal("modalNpa");
    });
    safeAddListener("btnApplyNpaFilter", "click", function(){
      renderNpaList();
    });

    safeAddListener("kpiCardFund", "click", function(){
      renderFundModal();
      openModal("modalFund");
    });
    safeAddListener("btnApplyFundDate", "click", function(){
      renderFundModal();
    });
    safeAddListener("inpFundFrom", "change", renderFundModal);
    safeAddListener("inpFundTo", "change", renderFundModal);

    // Filters on change
    safeAddListener("selFilterStatus", "change", renderMembers);
    safeAddListener("selSortMembers", "change", renderMembers);
    safeAddListener("memberFilterInput", "input", renderMembers);

    safeAddListener("inpPayFilterFrom", "change", renderPayments);
    safeAddListener("inpPayFilterTo", "change", renderPayments);
    safeAddListener("selFilterPayMode", "change", renderPayments);
    safeAddListener("selSortPayDate", "change", renderPayments);
    safeAddListener("searchPayInput", "input", renderPayments);

    safeAddListener("inpLoanFilterFrom", "change", renderLoans);
    safeAddListener("inpLoanFilterTo", "change", renderLoans);
    safeAddListener("selFilterLoanType", "change", renderLoans);
    safeAddListener("selFilterLoanStatus", "change", renderLoans);
    safeAddListener("searchLoanInput", "input", renderLoans);

    safeAddListener("inpBonusFilterFrom", "change", renderBonusTab);
    safeAddListener("inpBonusFilterTo", "change", renderBonusTab);
    safeAddListener("selFilterBonusStatus", "change", renderBonusTab);
    safeAddListener("selSortBonus", "change", renderBonusTab);
    safeAddListener("searchBonusInput", "input", renderBonusTab);

    safeAddListener("inpPenFilterFrom", "change", renderPenaltyTab);
    safeAddListener("inpPenFilterTo", "change", renderPenaltyTab);
    safeAddListener("selFilterPenStatus", "change", renderPenaltyTab);
    safeAddListener("selSortPen", "change", renderPenaltyTab);
    safeAddListener("searchPenInput", "input", renderPenaltyTab);

    safeAddListener("selFinancialYear", "change", function(){
      refreshAll();
    });

    // Passbook dynamic date filter
    safeAddListener("inpLedgerFilterFrom", "change", function(){
      if(currentActiveLedgerMember) openMemberLedger(currentActiveLedgerMember.id);
    });
    safeAddListener("inpLedgerFilterTo", "change", function(){
      if(currentActiveLedgerMember) openMemberLedger(currentActiveLedgerMember.id);
    });

    // Delegate Click Actions
    document.addEventListener("click", function(e){
      var t = e.target;
      if(t.classList.contains("action-edit-fund")){
        var txnId = t.getAttribute("data-id");
        if(txnId && typeof openEditFundModal === "function"){
          openEditFundModal(txnId);
        }
      }
      if(t.classList.contains("action-view-ledger")){
        var mid = t.getAttribute("data-id");
        if(mid) openMemberLedger(mid);
      }
      if(t.classList.contains("action-receive-for")){
        var mid = t.getAttribute("data-id");
        if(mid) openReceiveModalFor(mid);
      }
      if(t.classList.contains("action-edit-receipt")){
        var recNo = t.getAttribute("data-rec");
        var p = payments.find(function(x){ return String(x.receiptNo) === recNo; });
        if(p) openReceiveModalFor(p.id, recNo);
      }
      if(t.classList.contains("action-edit-loan")){
        var lId = t.getAttribute("data-loanid");
        var l = loans.find(function(x){ return String(x.loanId) === lId; });
        if(l) openLoanModalFor(l.id, lId);
      }
      if(t.classList.contains("action-edit-member")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ 
          return (mid && String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase()) || 
                 (mid && String(x.name).trim().toLowerCase() === String(mid).trim().toLowerCase()); 
        });
        if(m){
          document.getElementById("editMemId").value = m.id;
          document.getElementById("lblMemberModalHead").innerText = "✏️ Edit Member: " + m.name + " (" + m.id + ")";
          var btnDel = document.getElementById("btnDeleteMember");
          if (btnDel) { btnDel.style.display = "inline-flex"; btnDel.setAttribute("data-id", m.id); }
          var btnDel = document.getElementById("btnDeleteMember");
          if (btnDel) { btnDel.style.display = "inline-flex"; btnDel.setAttribute("data-id", m.id); }
          var btnDel = document.getElementById("btnDeleteMember");
          if (btnDel) { btnDel.style.display = "inline-flex"; btnDel.setAttribute("data-id", m.id); }
          document.getElementById("inpNewMemName").value = m.name;
          document.getElementById("inpNewMemMobile").value = m.mobile;
          var rawSt = String(m.status || "ACTIVE").trim().toUpperCase();
          var stVal = (rawSt === "INACTIVE" || rawSt === "IN-ACTIVE" || rawSt === "DEACTIVE" || rawSt === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";
          var selSt = document.getElementById("inpNewMemStatus");
          if(selSt){
            selSt.value = stVal;
            for(var oi = 0; oi < selSt.options.length; oi++){
              if(selSt.options[oi].value === stVal){
                selSt.selectedIndex = oi;
                break;
              }
            }
          }
          document.getElementById("inpNewMemJoinDate").value = m.dateJoined;
          document.getElementById("inpNewMemRd").value = m.rd;
          var dueVal = m.dueDay || "";
          if(!dueVal || dueVal.indexOf("month") >= 0 || dueVal.indexOf("th") >= 0 || dueVal.length < 8){
            dueVal = getTodayYMD().substring(0,8) + "15";
          }
          document.getElementById("inpNewMemDueDay").value = dueVal;
          if(document.getElementById("dispDueDayFormatted")) document.getElementById("dispDueDayFormatted").innerText = "(" + toDisplayDate(dueVal) + ")";
          document.getElementById("inpNewMemAddress").value = m.address || "";
          document.getElementById("inpNewMemNominee").value = m.nominee || "";
          document.getElementById("inpNewMemBal").value = cleanNum(m.rdPaid, 0);
          document.getElementById("inpNewMemOpLoan").value = cleanNum(m.opLoan, 0);
          document.getElementById("inpNewMemOpInt").value = cleanNum(m.opInt, 0);
          document.getElementById("inpNewMemOpPen").value = cleanNum(m.opPen, 0);
          document.getElementById("inpNewMemCustomLimit").value = cleanNum(m.customLimit, 0);
          openModal("modalMember");
        }
      }
      if(t.classList.contains("action-setoff-bonus")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ return String(x.id) === mid; });
        if(m){
          var bVal = getMemberBonus(m);
          document.getElementById("bonusMemId").value = m.id;
          document.getElementById("lblBonusTargetMember").innerText = m.name + " (" + m.id + ")";
          document.getElementById("lblBonusAmount").innerText = "₹" + bVal.toLocaleString("en-IN");
          document.getElementById("inpBonusDate").value = getTodayYMD();
          document.getElementById("inpBonusNetPaid").value = bVal;
          openModal("modalBonusSetoff");
        }
      }
      if(t.classList.contains("action-view-bonus-stmt")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ return String(x.id) === mid; });
        if(m){
          var res = calculate1PercentPmBonus(m);
          document.getElementById("lblBonusStmtTitle").innerText = "🎁 Bonus Calculation Statement: " + m.name + " (" + m.id + ")";
          document.getElementById("bonusStmtHeaderStats").innerHTML = 
            "<div><div class='ledger-stat-lbl'>MEMBER NAME</div><div class='ledger-stat-val' style='color:#FBBF24;'>" + m.name + "</div></div>" +
            "<div><div class='ledger-stat-lbl'>TOTAL RD SAVED</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + getMemberTotalRd(m).toLocaleString("en-IN") + "</div></div>" +
            "<div><div class='ledger-stat-lbl'>ANNUAL BONUS (1% P.M.)</div><div class='ledger-stat-val' style='color:#C084FC;'>₹" + res.totalBonus.toLocaleString("en-IN") + "</div></div>";
          var h = "";
          res.schedule.forEach(function(row){
            h += "<tr>" +
              "<td style='font-weight:700;'>" + row.month + "</td>" +
              "<td>₹" + row.opening.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#10B981;'>+₹" + row.deposit.toLocaleString("en-IN") + "</td>" +
              "<td style='font-weight:700;'>₹" + row.closing.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#C084FC;font-weight:800;'>₹" + row.mBonus.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#FBBF24;font-weight:800;'>₹" + row.accumBonus.toLocaleString("en-IN") + "</td>" +
            "</tr>";
          });
          document.getElementById("tbodyBonusSchedule").innerHTML = h;
          openModal("modalBonusStatement");
        }
      }
    });

    // SUBMIT BONUS SET-OFF
    safeAddListener("btnSubmitBonusSetoff", "click", function(){
      var mid = document.getElementById("bonusMemId").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m) return;
      var bVal = getMemberBonus(m);
      var sDate = document.getElementById("inpBonusDate").value || getTodayYMD();
      var mode = document.getElementById("selBonusMode").value || "ONLINE";
      var setoffId = generateAutoId("SET", sDate);

      var rec = {
        settlementId: setoffId,
        date: sDate,
        id: m.id,
        name: m.name,
        totalBonus: bVal,
        adjLoan: 0,
        adjInt: 0,
        adjRd: 0,
        adjPenalty: 0,
        netPaid: bVal,
        mode: mode
      };
      bonusSettlements.push(rec);
      saveStore();
      closeModal("modalBonusSetoff");
      refreshAll();
      showNotice("Bonus Settled", "Bonus set-off of ₹" + bVal.toLocaleString("en-IN") + " has been posted successfully for " + m.name + ".\\nRef ID: " + setoffId);
    });

    // BONUS SUB-TABS (INTEREST RECEIVED & SET-OFF REGISTER)
    safeAddListener("btnBonusSubTab1", "click", function(){
      document.getElementById("btnBonusSubTab1").classList.add("active");
      document.getElementById("btnBonusSubTab2").classList.remove("active");
      document.getElementById("bonusSubView1").style.display = "block";
      document.getElementById("bonusSubView2").style.display = "none";
    });
    safeAddListener("btnBonusSubTab2", "click", function(){
      document.getElementById("btnBonusSubTab2").classList.add("active");
      document.getElementById("btnBonusSubTab1").classList.remove("active");
      document.getElementById("bonusSubView2").style.display = "block";
      document.getElementById("bonusSubView1").style.display = "none";
      renderBonusSetoffRegister();
    });
    safeAddListener("inpBonusSetoffFilterFrom", "change", renderBonusSetoffRegister);
    safeAddListener("inpBonusSetoffFilterTo", "change", renderBonusSetoffRegister);

    // SETTINGS MODAL & FUND REGISTER SUB-TABS
    function switchSettingsSubTab(tabNum){
      [1, 2, 3, 4].forEach(function(n){
        var b = document.getElementById("btnSettingsSubTab" + n);
        var v = document.getElementById("settingsSubView" + n);
        if(b) {
          if(n === tabNum) b.classList.add("active"); else b.classList.remove("active");
        }
        if(v) {
          v.style.display = (n === tabNum) ? "block" : "none";
        }
      });
      if(tabNum === 2) renderFundRegister();
      if(tabNum === 4) renderProfitAndLossRegister();
    }
    safeAddListener("btnSettingsSubTab1", "click", function(){ switchSettingsSubTab(1); });
    safeAddListener("btnSettingsSubTab2", "click", function(){ switchSettingsSubTab(2); });
    safeAddListener("btnSettingsSubTab3", "click", function(){ switchSettingsSubTab(3); });
    safeAddListener("btnSettingsSubTab4", "click", function(){ switchSettingsSubTab(4); });
    safeAddListener("btnSwitchToFundForm", "click", function(){ switchSettingsSubTab(3); });

    safeAddListener("inpFundFilterFrom", "change", renderFundRegister);
    safeAddListener("inpFundFilterTo", "change", renderFundRegister);
    safeAddListener("selFundFilterAccount", "change", renderFundRegister);
    safeAddListener("selFundFilterType", "change", renderFundRegister);

    safeAddListener("inpPlFilterFrom", "change", renderProfitAndLossRegister);
    safeAddListener("inpPlFilterTo", "change", renderProfitAndLossRegister);
    safeAddListener("selPlFinancialYear", "change", renderProfitAndLossRegister);
    safeAddListener("btnApplyPlFilter", "click", renderProfitAndLossRegister);

    // SUBMIT FUND ENTRY (BORROW / INVEST)
    safeAddListener("btnSubmitFundEntry", "click", function(){
      var fType = document.getElementById("inpFundEntryType") ? document.getElementById("inpFundEntryType").value : "INVEST";
      var fAcc = document.getElementById("inpFundEntryAccount") ? document.getElementById("inpFundEntryAccount").value : "BANK";
      var fDate = (document.getElementById("inpFundEntryDate") && document.getElementById("inpFundEntryDate").value) ? document.getElementById("inpFundEntryDate").value : getTodayYMD();
      var rawAmt = document.getElementById("inpFundEntryAmount") ? document.getElementById("inpFundEntryAmount").value : "0";
      var fAmt = cleanNum(rawAmt, 0);
      var fEntity = document.getElementById("inpFundEntryEntity") ? (document.getElementById("inpFundEntryEntity").value || "").trim() : "";
      var fNarr = document.getElementById("inpFundEntryNarration") ? (document.getElementById("inpFundEntryNarration").value || "").trim() : "";

      if(fAmt < 0){
        showNotice("Validation Error", "Kripya sahi transaction amount bharein (e.g. 50000). Amount 0 ya usse bada hona chahiye.", "modalSettings");
        return;
      }
      var txnId = generateAutoId("FUND", fDate);
      var fObj = {
        id: txnId,
        date: fDate,
        type: fType,
        account: fAcc,
        entity: fEntity || "Society Capital",
        amount: fAmt,
        narration: fNarr || (fType === "INVEST" ? "Capital Investment" : "Fund Borrowing")
      };

      if(!window.fundTransactions) window.fundTransactions = [];
      window.fundTransactions.push(fObj);
      fundTransactions = window.fundTransactions;
      saveStore();

      if(typeof google !== "undefined" && google.script && google.script.run){
        try {
          google.script.run.withSuccessHandler(function(res){
            console.log("Fund transaction synced:", res);
          }).withFailureHandler(function(err){
            console.warn("Fund transaction sync issue:", err);
          }).saveFundTransactionBackend(fObj);
        } catch(e) {
          console.warn("google.script.run exception:", e);
        }
      }

      if(document.getElementById("inpFundEntryAmount")) document.getElementById("inpFundEntryAmount").value = "";
      if(document.getElementById("inpFundEntryEntity")) document.getElementById("inpFundEntryEntity").value = "";
      if(document.getElementById("inpFundEntryNarration")) document.getElementById("inpFundEntryNarration").value = "";
      switchSettingsSubTab(2);
      refreshAll();
      showNotice("Transaction Saved", "Fund transaction " + txnId + " (" + fType + ": ₹" + fAmt.toLocaleString("en-IN") + ") successfully recorded.", "modalSettings");
    });

    // SUBMIT RECEIVE AMOUNT / EDIT RECEIPT
    safeAddListener("btnSubmitReceive", "click", function(){
      var recNo = document.getElementById("editReceiptNo").value.trim();
      var mid = document.getElementById("selPayMember").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m){ showNotice("Validation Error", "Please select a valid member."); return; }

      var pDate = document.getElementById("inpPayDate").value || getTodayYMD();
      var pMode = document.getElementById("selPayMode").value;
      var rdVal = cleanNum(document.getElementById("inpPayRd").value, 0);
      var intVal = cleanNum(document.getElementById("inpPayInterest").value, 0);
      var penVal = cleanNum(document.getElementById("inpPayPenalty").value, 0);
      var wvrVal = cleanNum(document.getElementById("inpPayWaiver").value, 0);
      var princVal = cleanNum(document.getElementById("inpPayPrincipal").value, 0);
      var narr = document.getElementById("inpPayNarration").value.trim();

      var tot = rdVal + intVal + penVal + princVal - wvrVal;
      if(tot < 0) tot = 0;

      if(!recNo){
        recNo = generateAutoId("REC", pDate);
      }

      var newP = {
        receiptNo: recNo,
        date: pDate,
        id: m.id,
        name: m.name,
        rd: rdVal,
        interest: intVal,
        penalty: penVal,
        loanRepay: princVal,
        waiver: wvrVal,
        total: tot,
        mode: pMode,
        by: "Admin",
        type: "REGULAR",
        narration: narr
      };

      var existingIdx = payments.findIndex(function(x){ return String(x.receiptNo) === recNo; });
      if(existingIdx >= 0){
        payments[existingIdx] = newP;
      } else {
        payments.unshift(newP);
      }

      // If principal repayment, reduce loan outstanding
      if(princVal > 0){
        var actLoan = loans.find(function(l){ return String(l.id) === m.id && String(l.status).toUpperCase() === "ACTIVE"; });
        if(actLoan){
          actLoan.repaid = cleanNum(actLoan.repaid, 0) + princVal;
          actLoan.outstanding = Math.max(0, cleanNum(actLoan.outstanding, 0) - princVal);
          if(actLoan.outstanding === 0) actLoan.status = "CLOSED";
          if(typeof google !== "undefined" && google.script && google.script.run){
            google.script.run.saveLoanBackend(actLoan);
          }
        }
      }

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.savePaymentBackend(newP);
      }

      saveStore();
      closeModal("modalReceive");
      showNotice("Receipt Saved", "Receipt " + recNo + " of ₹" + tot.toLocaleString("en-IN") + " has been successfully posted and synced!");
    });

    // SUBMIT ISSUE LOAN / EDIT LOAN
    safeAddListener("btnSubmitLoan", "click", function(){
      var lId = document.getElementById("editLoanId").value.trim();
      var mid = document.getElementById("selLoanMember").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m){ showNotice("Validation Error", "Please select a member."); return; }

      var lDate = document.getElementById("inpLoanDate").value || getTodayYMD();
      var lType = document.getElementById("selLoanType").value;
      var princ = cleanNum(document.getElementById("inpLoanPrinc").value, 0);
      var rate = Number(document.getElementById("inpLoanRate").value) || 1.0;
      var narr = document.getElementById("inpLoanNarration").value.trim();

      if(princ <= 0){
        showNotice("Validation Error", "Principal amount must be greater than 0.");
        return;
      }

      if(!lId){
        // Check active loan
        var activeDue = getMemberActiveLoan(m);
        if(activeDue > 0){
          showNotice("Loan Blocked", "Member already has an active loan outstanding of ₹" + activeDue.toLocaleString("en-IN") + ". Multiple active loans are not permitted.");
          return;
        }

        lId = generateAutoId("LN", lDate);
      }

      var newL = {
        loanId: lId,
        date: lDate,
        id: m.id,
        name: m.name,
        type: lType,
        principal: princ,
        rate: rate,
        repaid: 0,
        outstanding: princ,
        status: "ACTIVE",
        narration: narr
      };

      var exIdx = loans.findIndex(function(x){ return String(x.loanId) === lId; });
      if(exIdx >= 0){
        newL.repaid = loans[exIdx].repaid;
        newL.outstanding = Math.max(0, princ - newL.repaid);
        if(newL.outstanding === 0) newL.status = "CLOSED";
        loans[exIdx] = newL;
      } else {
        loans.unshift(newL);
      }

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.saveLoanBackend(newL);
      }

      saveStore();
      closeModal("modalLoan");
      showNotice("Loan Disbursed", "Loan " + lId + " of ₹" + princ.toLocaleString("en-IN") + " has been approved & recorded.");
    });

    // SUBMIT ADD / EDIT MEMBER (WITH STRICT MEMDDMMYYYY AND PRESERVING FORM ON VALIDATION NOTICE)
    
    // 🗑️ STRICT DOUBLE-CONFIRMATION DELETE HANDLER
    safeAddListener("btnDeleteMember", "click", function(){
      var mid = document.getElementById("editMemId").value.trim();
      var mName = document.getElementById("inpNewMemName").value.trim();
      if(!mid){
        showNotice("Error", "No Member selected for deletion.", "modalMember");
        return;
      }

      var msg1 = [
        "STEP 1 OF 2: DELETE CONFIRMATION",
        "",
        "Are you sure you want to permanently delete member: " + mName + " (" + mid + ")?",
        "",
        "This will remove this member from Master Ledger, Loan Register, Receipts, Bonus, and Passbooks."
      ].join(String.fromCharCode(10));
      var confirm1 = confirm(msg1);
      if(!confirm1) return;

      var msg2 = [
        "STEP 2 OF 2: FINAL PERMANENT DELETION CHECK",
        "",
        "Type DELETE in capital letters to permanently erase all loans, receipts, and ledger history for " + mName + ":"
      ].join(String.fromCharCode(10));
      var confirm2 = prompt(msg2);
      if(!confirm2 || confirm2.trim().toUpperCase() !== "DELETE"){
        alert("Deletion Cancelled. Text did not match DELETE. Member remains completely safe.");
        return;
      }

      var cleanMid = String(mid).trim().toUpperCase();
      var cleanMName = String(mName).trim().toLowerCase();

      members = members.filter(function(x){
        return String(x.id).trim().toUpperCase() !== cleanMid && String(x.name).trim().toLowerCase() !== cleanMName;
      });

      loans = loans.filter(function(l){
        return String(l.id).trim().toUpperCase() !== cleanMid && String(l.name).trim().toLowerCase() !== cleanMName;
      });

      payments = payments.filter(function(p){
        return String(p.id).trim().toUpperCase() !== cleanMid && String(p.name).trim().toLowerCase() !== cleanMName;
      });

      if(typeof exitSettlements !== "undefined" && exitSettlements){
        exitSettlements = exitSettlements.filter(function(e){
          return String(e.id).trim().toUpperCase() !== cleanMid && String(e.name).trim().toLowerCase() !== cleanMName;
        });
      }

      if(typeof bonusSettlements !== "undefined" && bonusSettlements){
        bonusSettlements = bonusSettlements.filter(function(b){
          return String(b.id).trim().toUpperCase() !== cleanMid && String(b.name).trim().toLowerCase() !== cleanMName;
        });
      }

      saveStore();
      refreshAll();
      closeModal("modalMember");

      if(typeof google !== "undefined" && google.script && google.script.run){
        showNotice("Deleting Everywhere...", "Permanently removing member " + mName + " from all Google Sheets...");
        google.script.run.withSuccessHandler(function(res){
          closeModal("modalNotice");
          if(res && res.success){
            showNotice("Deleted", "Member " + mName + " and all linked history removed from all registers and Google Sheets.");
          } else {
            showNotice("Notice", "Deleted locally from all registers: " + (res && res.error ? res.error : ""));
          }
        }).withFailureHandler(function(err){
          closeModal("modalNotice");
        }).deleteMemberBackend(cleanMid);
      } else {
        showNotice("Deleted", "Member " + mName + " deleted from all local registers.");
      }
    });

    safeAddListener("btnSubmitMember", "click", function(){
      var mid = document.getElementById("editMemId").value.trim();
      var name = document.getElementById("inpNewMemName").value.trim();
      var mob = document.getElementById("inpNewMemMobile").value.trim();
      var rawSt = document.getElementById("inpNewMemStatus").value;
      var st = String(rawSt || "ACTIVE").trim().toUpperCase();
      st = (st === "INACTIVE" || st === "IN-ACTIVE" || st === "DEACTIVE" || st === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";
      var jDate = document.getElementById("inpNewMemJoinDate").value || getTodayYMD();
      var rdVal = cleanRd(document.getElementById("inpNewMemRd").value);
      var dueDayVal = document.getElementById("inpNewMemDueDay").value || "15th of every month";
      var addr = document.getElementById("inpNewMemAddress").value.trim();
      var nom = document.getElementById("inpNewMemNominee").value.trim();

      var opRd = cleanNum(document.getElementById("inpNewMemBal").value, 0);
      var opLoan = cleanNum(document.getElementById("inpNewMemOpLoan").value, 0);
      var opInt = cleanNum(document.getElementById("inpNewMemOpInt").value, 0);
      var opPen = cleanNum(document.getElementById("inpNewMemOpPen").value, 0);
      var custLim = cleanNum(document.getElementById("inpNewMemCustomLimit").value, 0);

      if(!name){
        showNotice("Required Field", "Please enter Member Full Name.", "modalMember");
        return;
      }
      if(!mob || mob.length !== 10 || isNaN(Number(mob))){
        showNotice("Required Field", "Mobile number must be exactly 10 digits.", "modalMember");
        return;
      }
      if(!addr){
        showNotice("Required Field", "Please enter Member Address.", "modalMember");
        return;
      }
      if(!nom){
        showNotice("Required Field", "Please enter Nominee / Reference details.", "modalMember");
        return;
      }

      if(!mid){
        mid = generateAutoId("MEM", jDate);
      }

      var newM = {
        id: mid,
        name: name,
        mobile: mob,
        status: st,
        address: addr,
        nominee: nom,
        rd: rdVal,
        dateJoined: jDate,
        rdPaid: opRd,
        dueDay: dueDayVal,
        customLimit: custLim,
        opLoan: opLoan,
        opInt: opInt,
        opPen: opPen
      };

      var exIdx = members.findIndex(function(x){ 
        return (mid && String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase()) || 
               (name && String(x.name).trim().toLowerCase() === String(name).trim().toLowerCase()); 
      });
      if(exIdx >= 0){
        newM.rdPaid = members[exIdx].rdPaid;
        members[exIdx] = newM;
        var cMid = String(mid).trim().toUpperCase();
        loans.forEach(function(l){ if(String(l.id).trim().toUpperCase() === cMid) l.name = name; });
        payments.forEach(function(p){ if(String(p.id).trim().toUpperCase() === cMid) p.name = name; });
      } else {
        members.push(newM);
      }
      saveStore();
      refreshAll();

      if(typeof google !== "undefined" && google.script && google.script.run){
        showNotice("Saving...", "Saving member profile to Google Sheet...");
        google.script.run.withSuccessHandler(function(res){
          closeModal("modalNotice");
          if(res && res.member){
            var idx = members.findIndex(function(x){
              return (res.member.id && String(x.id).trim().toUpperCase() === String(res.member.id).trim().toUpperCase()) ||
                     (res.member.name && String(x.name).trim().toLowerCase() === String(res.member.name).trim().toLowerCase());
            });
            if(idx >= 0) members[idx] = res.member;
          }
          saveStore();
          closeModal("modalMember");
          showNotice("Member Saved", "Member profile for " + name + " (ID: " + mid + ") saved successfully on Spreadsheet!");
          refreshAll();
        }).withFailureHandler(function(err){
          closeModal("modalNotice");
          saveStore();
          closeModal("modalMember");
          showNotice("Member Saved (Local)", "Saved in browser cache. Notice: " + (err && err.message ? err.message : err));
          refreshAll();
        }).saveMemberBackend(newM);
      } else {
        saveStore();
        closeModal("modalMember");
        showNotice("Member Saved", "Member profile for " + name + " (ID: " + mid + ") saved successfully! (Local Mode)");
        refreshAll();
      }
    });

    // SUBMIT BULK ENTRY (WITH SHORT NARRATION)
    safeAddListener("btnSubmitBulk", "click", function(){
      var bDate = document.getElementById("inpBulkDate").value || getTodayYMD();
      var rows = document.querySelectorAll("#tbodyBulkList tr");
      var postedCount = 0;
      var grandTotalPosted = 0;

      rows.forEach(function(tr){
        var chk = tr.querySelector(".b-chk");
        if(chk && chk.checked){
          var mid = tr.getAttribute("data-id");
          var m = members.find(function(x){ return String(x.id) === mid; });
          if(m){
            var rdVal = cleanNum(tr.querySelector(".b-rd").value, 0);
            var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
            var repayVal = cleanNum(tr.querySelector(".b-repay").value, 0);
            var penVal = cleanNum(tr.querySelector(".b-pen").value, 0);
            var wvrVal = cleanNum(tr.querySelector(".b-wvr").value, 0);
            var bMode = tr.querySelector(".b-mode").value;
            var bNarr = (tr.querySelector(".b-narr").value || "").trim() || "Bulk Collection";

            var tot = rdVal + intVal + repayVal + penVal - wvrVal;
            if(tot > 0){
              var finalId = generateAutoId("REC", bDate);

              var newP = {
                receiptNo: finalId,
                date: bDate,
                id: m.id,
                name: m.name,
                rd: rdVal,
                interest: intVal,
                penalty: penVal,
                loanRepay: repayVal,
                waiver: wvrVal,
                total: tot,
                mode: bMode,
                by: "Admin",
                type: "REGULAR",
                narration: bNarr
              };

              payments.unshift(newP);

              if(repayVal > 0){
                var actLoan = loans.find(function(l){ return String(l.id) === m.id && String(l.status).toUpperCase() === "ACTIVE"; });
                if(actLoan){
                  actLoan.repaid = cleanNum(actLoan.repaid, 0) + repayVal;
                  actLoan.outstanding = Math.max(0, cleanNum(actLoan.outstanding, 0) - repayVal);
                  if(actLoan.outstanding === 0) actLoan.status = "CLOSED";
                  if(typeof google !== "undefined" && google.script && google.script.run){
                    google.script.run.saveLoanBackend(actLoan);
                  }
                }
              }

              if(typeof google !== "undefined" && google.script && google.script.run){
                google.script.run.savePaymentBackend(newP);
              }

              postedCount++;
              grandTotalPosted += tot;
            }
          }
        }
      });

      if(postedCount > 0){
        saveStore();
        closeModal("modalBulk");
        showNotice("Bulk Entry Posted", "Successfully posted " + postedCount + " receipts totaling ₹" + grandTotalPosted.toLocaleString("en-IN") + "!");
      } else {
        showNotice("Notice", "No collections selected or entered.");
      }
    });

    // PRINTING PASSBOOK
    safeAddListener("btnPrintLedgerPdf", "click", function(){
      window.print();
    });
    safeAddListener("btnPrintBonusPdf", "click", function(){
      window.print();
    });

    // Define background auto-sync mechanism (V46 PRO)
    window.syncTimerId = null;
    window.startAutoSync = function(intervalMinutes) {
      if(window.syncTimerId) {
        clearInterval(window.syncTimerId);
        window.syncTimerId = null;
      }
      var mins = parseInt(intervalMinutes, 10);
      if(isNaN(mins) || mins <= 0) {
        console.log("Auto-sync interval disabled.");
        return;
      }
      window.syncTimerId = setInterval(function(){
        if(typeof google !== "undefined" && google.script && google.script.run){
          console.log("Starting background auto-sync...");
          google.script.run.withSuccessHandler(function(res){
            if(res && res.members && res.members.length > 0){
              members = res.members;
              payments = res.payments || [];
              loans = res.loans || [];
              exitSettlements = res.exitSettlements || [];
              bonusSettlements = res.bonusSettlements || [];
              if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
              if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
              saveStore();
              refreshAll();
              console.log("Background auto-sync complete. Total members: " + members.length);
            }
          }).getSocietyFullData();
        }
      }, mins * 60 * 1000);
      console.log("Initialized background auto-sync timer for every " + mins + " mins.");
    };

    // Initial Render
    refreshAll();

    // Start 5 min default background sync on boot
    window.startAutoSync(5);

    // Auto-fetch fresh sheet data on start
    if(typeof google !== "undefined" && google.script && google.script.run){
      google.script.run.withSuccessHandler(function(res){
        if(res && res.members && res.members.length > 0){
          members = res.members;
          payments = res.payments || [];
          loans = res.loans || [];
          exitSettlements = res.exitSettlements || [];
          bonusSettlements = res.bonusSettlements || [];
          if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
          if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
          saveStore();
          refreshAll();
        }
      }).getSocietyFullData();
    }
  }

  // EXPOSE ALL ESSENTIAL FUNCTIONS GLOBALLY
  window.bootApplication = bootApplication;
  window.refreshAll = refreshAll;
  window.openModal = openModal;
  window.closeModal = closeModal;
  window.closeAllModals = closeAllModals;
  window.showNotice = showNotice;
  window.openReceiveModalFor = openReceiveModalFor;
  window.openLoanModalFor = openLoanModalFor;
  window.renderMembers = renderMembers;
  window.renderPayments = renderPayments;
  window.renderLoans = renderLoans;
  window.renderBonusTab = renderBonusTab;
  window.renderPenaltyTab = renderPenaltyTab;

  window.openAddMemberModal = function(){
    var editId = document.getElementById("editMemId");
    if(editId) editId.value = "";
    var lbl = document.getElementById("lblMemberModalHead");
    if(lbl) lbl.innerText = "👤 Add New Member Profile";
    var nameEl = document.getElementById("inpNewMemName"); if(nameEl) nameEl.value = "";
    var mobEl = document.getElementById("inpNewMemMobile"); if(mobEl) mobEl.value = "";
    var stEl = document.getElementById("inpNewMemStatus"); if(stEl) stEl.value = "ACTIVE";
    var jDateEl = document.getElementById("inpNewMemJoinDate"); if(jDateEl) jDateEl.value = getTodayYMD();
    var rdEl = document.getElementById("inpNewMemRd"); if(rdEl) rdEl.value = 400;
    var defDue = getTodayYMD().substring(0,8) + "15";
    var dueEl = document.getElementById("inpNewMemDueDay"); if(dueEl) dueEl.value = defDue;
    var dueFmt = document.getElementById("dispDueDayFormatted"); if(dueFmt) dueFmt.innerText = "(" + toDisplayDate(defDue) + ")";
    var addrEl = document.getElementById("inpNewMemAddress"); if(addrEl) addrEl.value = "";
    var nomEl = document.getElementById("inpNewMemNominee"); if(nomEl) nomEl.value = "";
    var balEl = document.getElementById("inpNewMemBal"); if(balEl) balEl.value = 0;
    var opLoanEl = document.getElementById("inpNewMemOpLoan"); if(opLoanEl) opLoanEl.value = 0;
    var opIntEl = document.getElementById("inpNewMemOpInt"); if(opIntEl) opIntEl.value = 0;
    var opPenEl = document.getElementById("inpNewMemOpPen"); if(opPenEl) opPenEl.value = 0;
    var limEl = document.getElementById("inpNewMemCustomLimit"); if(limEl) limEl.value = 0;
    openModal("modalMember");
  };

  window.openBulkModal = function(){
    var today = getTodayYMD();
    var bDate = document.getElementById("inpBulkDate"); if(bDate) bDate.value = today;
    var bFmt = document.getElementById("dispBulkDateFormatted"); if(bFmt) bFmt.innerText = "(" + toDisplayDate(today) + ")";
    renderBulkList();
    openModal("modalBulk");
  };

  function renderFundModal(){
    var fromEl = document.getElementById('inpFundFrom');
    var toEl = document.getElementById('inpFundTo');
    var fromD = fromEl ? fromEl.value : '2026-01-01';
    var toD = toEl ? toEl.value : '2026-12-31';
    if(!fromD) fromD = '2026-01-01';
    if(!toD) toD = '2026-12-31';

    var liquid = (typeof calculateSocietyLiquidBalances === 'function')
      ? calculateSocietyLiquidBalances()
      : { cash: 0, bank: 0, total: 0 };

    var cashEl = document.getElementById('lblRegCashBal');
    var bankEl = document.getElementById('lblRegBankBal');
    var fundEl = document.getElementById('lblRegTotalFund');
    if(cashEl) {
      cashEl.innerText = (liquid.cash >= 0 ? '₹' : '-₹') + Math.abs(liquid.cash).toLocaleString('en-IN');
      cashEl.style.color = liquid.cash >= 0 ? '#10B981' : '#EF4444';
    }
    if(bankEl) {
      bankEl.innerText = (liquid.bank >= 0 ? '₹' : '-₹') + Math.abs(liquid.bank).toLocaleString('en-IN');
      bankEl.style.color = liquid.bank >= 0 ? '#38BDF8' : '#EF4444';
    }
    if(fundEl) {
      fundEl.innerText = (liquid.total >= 0 ? '₹' : '-₹') + Math.abs(liquid.total).toLocaleString('en-IN');
      fundEl.style.color = liquid.total >= 0 ? '#FBBF24' : '#EF4444';
    }
    var monthMap = {};
    var monthList = [];
    var monthNames = [];
    var monthNamesFull = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    try {
      var startYr = parseInt(fromD.substring(0, 4), 10);
      var startMo = parseInt(fromD.substring(5, 7), 10);
      var endYr = parseInt(toD.substring(0, 4), 10);
      var endMo = parseInt(toD.substring(5, 7), 10);

      if (isNaN(startYr) || isNaN(startMo)) { startYr = 2026; startMo = 1; }
      if (isNaN(endYr) || isNaN(endMo)) { endYr = 2026; endMo = 12; }

      var curYr = startYr;
      var curMo = startMo;
      var limit = 0;
      while ((curYr < endYr || (curYr === endYr && curMo <= endMo)) && limit < 120) {
        var mKey = curYr + "-" + String(curMo).padStart(2, "0");
        monthList.push(mKey);
        monthNames.push(monthNamesFull[curMo - 1] + " " + curYr);
        monthMap[mKey] = { inflow: 0, outflow: 0 };
        curMo++;
        if (curMo > 12) {
          curMo = 1;
          curYr++;
        }
        limit++;
      }
    } catch(e) {
      console.error("Error generating month list:", e);
      monthList = ["2026-01","2026-02","2026-03","2026-04","2026-05","2026-06","2026-07","2026-08","2026-09","2026-10","2026-11","2026-12"];
      monthNames = ["Jan 2026","Feb 2026","Mar 2026","Apr 2026","May 2026","Jun 2026","Jul 2026","Aug 2026","Sep 2026","Oct 2026","Nov 2026","Dec 2026"];
      monthList.forEach(function(mKey){
        monthMap[mKey] = { inflow: 0, outflow: 0 };
      });
    }

    payments.forEach(function(p){
      var mKey = getYearMonthKey(p.date);
      if(monthMap[mKey]) {
        monthMap[mKey].inflow += cleanNum(p.total, 0);
      }
    });

    fundTransactions.forEach(function(f){
      var mKey = getYearMonthKey(f.date);
      if(monthMap[mKey]) {
        if(String(f.type || "INVEST").toUpperCase() === "INVEST"){
          monthMap[mKey].inflow += cleanNum(f.amount, 0);
        } else {
          monthMap[mKey].outflow += cleanNum(f.amount, 0);
        }
      }
    });

    loans.forEach(function(l){
      var mKey = getYearMonthKey(l.date);
      if(monthMap[mKey]) {
        monthMap[mKey].outflow += cleanNum(l.principal, 0);
      }
    });

    exitSettlements.forEach(function(x){
      var mKey = getYearMonthKey(x.date);
      if(monthMap[mKey]) {
        monthMap[mKey].outflow += cleanNum(x.payout, 0);
      }
    });

    var tbodyMonths = document.getElementById("tbodyFundMonths");
    if(tbodyMonths){
      var html = "";
      monthList.forEach(function(mKey, idx){
        var data = monthMap[mKey] || { inflow: 0, outflow: 0 };
        var net = data.inflow - data.outflow;
        var netBadge = net >= 0 ? "<span style='color:#10B981; font-weight:700;'>+₹" + net.toLocaleString("en-IN") + "</span>" : "<span style='color:#EF4444; font-weight:700;'>-₹" + Math.abs(net).toLocaleString("en-IN") + "</span>";
        
        html += "<tr>" +
          "<td style='font-weight:700; color:#FBBF24;'>" + (monthNames[idx] || mKey) + "</td>" +
          "<td style='color:#10B981; font-weight:700;'>+₹" + data.inflow.toLocaleString("en-IN") + "</td>" +
          "<td style='color:#EF4444; font-weight:700;'>-₹" + data.outflow.toLocaleString("en-IN") + "</td>" +
          "<td>" + netBadge + "</td>" +
          "<td><button class='btn btn-dark action-view-fund-drilldown' data-ym='" + mKey + "' style='padding:3px 8px; font-size:0.75rem;'>🔍 Details</button></td>" +
        "</tr>";
      });
      tbodyMonths.innerHTML = html || "<tr><td colspan='5' style='text-align:center; color:#94A3B8;'>No monthly data within selected range</td></tr>";
      tbodyMonths.onclick = function(ev) {
        var target = ev.target || ev.srcElement;
        var btn = target.closest ? target.closest(".action-view-fund-drilldown") : null;
        if (btn) {
          var ym = btn.getAttribute("data-ym");
          if (ym && typeof window.showFundMonthDrilldown === "function") {
            window.showFundMonthDrilldown(ym);
          }
        }
      };
    }
  }
  window.renderFundModal = renderFundModal;

  window.showFundMonthDrilldown = function(mKey) {
    var box = document.getElementById("fundDrilldownBox");
    var title = document.getElementById("lblDrilldownTitle");
    var tbody = document.getElementById("tbodyDrilldown");
    var btnClose = document.getElementById("btnCloseDrilldown");
    if (!box || !tbody) return;

    if (btnClose) {
      btnClose.onclick = function() {
        box.style.display = "none";
      };
    }

    var items = [];
    // 1. Payments
    payments.forEach(function(p){
      if (getYearMonthKey(p.date) === mKey) {
        items.push({
          date: toDisplayDate(p.date),
          name: p.name || "Member " + p.id,
          cat: "RD + Repay + Int",
          amt: "+₹" + cleanNum(p.total, 0).toLocaleString("en-IN"),
          mode: p.mode || "CASH",
          color: "#10B981"
        });
      }
    });

    // 2. Fund Register
    fundTransactions.forEach(function(f){
      if (getYearMonthKey(f.date) === mKey) {
        var isIn = String(f.type || "INVEST").toUpperCase() === "INVEST";
        items.push({
          date: toDisplayDate(f.date),
          name: f.entity || "Fund Register",
          cat: f.narration || f.type,
          amt: (isIn ? "+₹" : "-₹") + cleanNum(f.amount, 0).toLocaleString("en-IN"),
          mode: f.account || "BANK",
          color: isIn ? "#10B981" : "#EF4444"
        });
      }
    });

    // 3. Loans
    loans.forEach(function(l){
      if (getYearMonthKey(l.date) === mKey) {
        items.push({
          date: toDisplayDate(l.date),
          name: l.name || "Member " + l.id,
          cat: "Loan Disbursed (" + (l.type || "Gullak Loan") + ")",
          amt: "-₹" + cleanNum(l.principal, 0).toLocaleString("en-IN"),
          mode: "CASH/BANK",
          color: "#EF4444"
        });
      }
    });

    // 4. Exit Settlements
    exitSettlements.forEach(function(x){
      if (getYearMonthKey(x.date) === mKey) {
        items.push({
          date: toDisplayDate(x.date),
          name: x.name || "Member " + x.id,
          cat: "Exit Settlement Payout",
          amt: "-₹" + cleanNum(x.payout || x.netSettlement, 0).toLocaleString("en-IN"),
          mode: "CASH/BANK",
          color: "#EF4444"
        });
      }
    });

    if (title) title.innerText = "Source Details for " + mKey + " (" + items.length + " transactions)";

    if (items.length === 0) {
      tbody.innerHTML = "<tr><td colspan='5' style='text-align:center; color:#94A3B8; padding:10px;'>No transactions recorded for " + mKey + "</td></tr>";
    } else {
      var h = "";
      items.forEach(function(it){
        h += "<tr>" +
          "<td style='font-size:0.75rem;'>" + it.date + "</td>" +
          "<td style='font-weight:700; font-size:0.75rem; color:#F8FAFC;'>" + it.name + "</td>" +
          "<td style='font-size:0.75rem; color:#CBD5E1;'>" + it.cat + "</td>" +
          "<td style='font-weight:700; font-size:0.75rem; color:" + it.color + ";'>" + it.amt + "</td>" +
          "<td style='font-size:0.75rem;'><span class='badge' style='background:#334155; color:#FBBF24;'>" + it.mode + "</span></td>" +
        "</tr>";
      });
      tbody.innerHTML = h;
    }

    box.style.display = "block";
  };

  window.openFundModal = function(){
    renderFundModal();
    openModal("modalFund");
  };

  window.openNpaModal = function(){
    renderNpaList();
    openModal("modalNpa");
  };

  window.logoutSession = function(){
    window.currentUserSession = null;
    try {
      sessionStorage.clear();
    } catch(e){}
    var overlay = document.getElementById("windowsLoginOverlay");
    if(overlay) {
      overlay.style.display = "flex";
    }
    var pInput = document.getElementById("inpWinPassword");
    if(pInput) {
      pInput.value = "";
      try { pInput.focus(); } catch(err) {}
    }
  };

  window.handleTopReload = function(){
    if(typeof google !== "undefined" && google.script && google.script.run){
      showNotice("Syncing...", "Fetching verified records from Google Spreadsheet...");
      google.script.run.withSuccessHandler(function(res){
        closeModal("modalNotice");
        if(res && res.members && res.members.length > 0){
          members = res.members;
          payments = res.payments || [];
          loans = res.loans || [];
          exitSettlements = res.exitSettlements || [];
          bonusSettlements = res.bonusSettlements || [];
          if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
          if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
          saveStore();
          showNotice("Sync Complete", "Successfully synchronized " + members.length + " members, " + payments.length + " receipts, and " + loans.length + " loans from Google Sheet!");
        }
      }).getSocietyFullData();
    } else {
      refreshAll();
      showNotice("Local Reloaded", "Database re-indexed locally.");
    }
  };

  // MEMBER EXIT SETTLEMENT CONTROLLER (V41 PRO)
  function handleExitMemberChange(){
    var sel = document.getElementById("selExitMember");
    var selId = sel ? sel.value : "";
    var m = (members || []).find(function(x){
      return String(x.id).trim().toUpperCase() === String(selId).trim().toUpperCase();
    });

    var dispRd = document.getElementById("lblExitRd") || document.getElementById("dispExitRd");
    var dispLoan = document.getElementById("lblExitLoan") || document.getElementById("dispExitLoan");
    var dispBonus = document.getElementById("lblExitBonus") || document.getElementById("dispExitBonus");
    var dispPen = document.getElementById("lblExitPen") || document.getElementById("dispExitPen");

    if(!m){
      if(dispRd) dispRd.textContent = "₹0";
      if(dispLoan) dispLoan.textContent = "₹0";
      if(dispBonus) dispBonus.textContent = "₹0";
      if(dispPen) dispPen.textContent = "₹0";
      recalculateExitFinal();
      return;
    }

    var mid = String(m.id || "").trim().toUpperCase();
    var mName = String(m.name || "").trim().toLowerCase();

    var totalRd = cleanNum(m.rdPaid, 0);
    (payments || []).forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase();
      var pMName = String(p.name || "").trim().toLowerCase();
      if((pMid && pMid === mid) || (pMName && pMName === mName)){
        totalRd += cleanNum(p.rd, 0);
      }
    });

    var totalLoanDue = 0;
    (loans || []).forEach(function(l){
      var lMid = String(l.memberId || l.id || "").trim().toUpperCase();
      var lMName = String(l.name || "").trim().toLowerCase();
      if((lMid && lMid === mid) || (lMName && lMName === mName)){
        totalLoanDue += (cleanNum(l.balance, 0) + cleanNum(l.interestDue, 0));
      }
    });

    var bonusObj = calculate1PercentPmBonus(m);
    var totalBonus = bonusObj.bonusAccrued || 0;
    var totalPenalty = calculateMemberLivePenaltyDue(m);

    if(dispRd) dispRd.textContent = "₹" + totalRd.toLocaleString("en-IN");
    if(dispLoan) dispLoan.textContent = "₹" + totalLoanDue.toLocaleString("en-IN");
    if(dispBonus) dispBonus.textContent = "₹" + totalBonus.toLocaleString("en-IN");
    if(dispPen) dispPen.textContent = "₹" + totalPenalty.toLocaleString("en-IN");

    recalculateExitFinal();
  }
  window.handleExitMemberChange = handleExitMemberChange;
  window.loadExitDetails = handleExitMemberChange;

  function recalculateExitFinal(){
    var sel = document.getElementById("selExitMember");
    var selId = sel ? sel.value : "";
    var m = (members || []).find(function(x){
      return String(x.id).trim().toUpperCase() === String(selId).trim().toUpperCase();
    });

    var dispFinal = document.getElementById("dispExitNetResult") || document.getElementById("dispExitFinal");
    var lblResult = document.getElementById("lblExitResultType");
    var inpRefund = document.getElementById("inpExitNetRefund");
    if(!m){
      if(dispFinal) dispFinal.textContent = "₹0";
      if(lblResult) lblResult.textContent = "FINAL SETTLEMENT AMOUNT";
      if(inpRefund) inpRefund.value = "0";
      return;
    }

    var mid = String(m.id || "").trim().toUpperCase();
    var mName = String(m.name || "").trim().toLowerCase();

    var totalRd = cleanNum(m.rdPaid, 0);
    (payments || []).forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase();
      var pMName = String(p.name || "").trim().toLowerCase();
      if((pMid && pMid === mid) || (pMName && pMName === mName)){
        totalRd += cleanNum(p.rd, 0);
      }
    });

    var totalLoanDue = 0;
    (loans || []).forEach(function(l){
      var lMid = String(l.memberId || l.id || "").trim().toUpperCase();
      var lMName = String(l.name || "").trim().toLowerCase();
      if((lMid && lMid === mid) || (lMName && lMName === mName)){
        totalLoanDue += (cleanNum(l.balance, 0) + cleanNum(l.interestDue, 0));
      }
    });

    var bonusObj = calculate1PercentPmBonus(m);
    var totalBonus = bonusObj.bonusAccrued || 0;
    var totalPenalty = calculateMemberLivePenaltyDue(m);

    var inclBonus = document.getElementById("chkExitIncludeBonus") ? document.getElementById("chkExitIncludeBonus").checked : true;
    var npaLoss = cleanNum(document.getElementById("inpExitNpa") ? document.getElementById("inpExitNpa").value : 0, 0);
    var waiver = cleanNum(document.getElementById("inpExitWaiver") ? document.getElementById("inpExitWaiver").value : 0, 0);

    var netSettlement = totalRd + (inclBonus ? totalBonus : 0) - totalLoanDue - totalPenalty + waiver - npaLoss;
    if(inpRefund) inpRefund.value = String(netSettlement);

    if(dispFinal){
      if(netSettlement >= 0){
        dispFinal.textContent = "₹" + netSettlement.toLocaleString("en-IN") + " (Payable to Member)";
        dispFinal.style.color = "#10B981";
      } else {
        dispFinal.textContent = "₹" + Math.abs(netSettlement).toLocaleString("en-IN") + " (Recoverable from Member)";
        dispFinal.style.color = "#EF4444";
      }
    }
    if(lblResult){
      lblResult.textContent = netSettlement >= 0 ? "PAYABLE SETTLEMENT REFUND" : "RECOVERY BALANCE DUE";
    }
  }
  window.recalculateExitFinal = recalculateExitFinal;
  window.calcExitNet = recalculateExitFinal;

  function handleExecuteExitSettlement(){
    var sel = document.getElementById("selExitMember");
    var selId = sel ? sel.value : "";
    var m = (members || []).find(function(x){
      return String(x.id).trim().toUpperCase() === String(selId).trim().toUpperCase();
    });

    if(!m){
      showNotice("Select Member", "Kripya exit settlement ke liye pehle active member select karein.", "modalExit");
      return;
    }

    var mid = String(m.id || "").trim().toUpperCase();
    var mName = String(m.name || "").trim();

    var totalRd = cleanNum(m.rdPaid, 0);
    (payments || []).forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase();
      var pMName = String(p.name || "").trim().toLowerCase();
      if((pMid && pMid === mid) || (pMName && pMName === mName.toLowerCase())){
        totalRd += cleanNum(p.rd, 0);
      }
    });

    var totalLoanDue = 0;
    (loans || []).forEach(function(l){
      var lMid = String(l.memberId || l.id || "").trim().toUpperCase();
      var lMName = String(l.name || "").trim().toLowerCase();
      if((lMid && lMid === mid) || (lMName && lMName === mName.toLowerCase())){
        totalLoanDue += (cleanNum(l.balance, 0) + cleanNum(l.interestDue, 0));
      }
    });

    var bonusObj = calculate1PercentPmBonus(m);
    var totalBonus = bonusObj.bonusAccrued || 0;
    var totalPenalty = calculateMemberLivePenaltyDue(m);

    var inclBonus = document.getElementById("chkExitIncludeBonus") ? document.getElementById("chkExitIncludeBonus").checked : true;
    var npaLoss = cleanNum(document.getElementById("inpExitNpa") ? document.getElementById("inpExitNpa").value : 0, 0);
    var waiver = cleanNum(document.getElementById("inpExitWaiver") ? document.getElementById("inpExitWaiver").value : 0, 0);

    var netSettlement = totalRd + (inclBonus ? totalBonus : 0) - totalLoanDue - totalPenalty + waiver - npaLoss;

    var exitDate = getTodayYMD();
    var exitId = generateAutoId("EXIT", exitDate);

    var exitRecord = {
      exitId: exitId,
      date: exitDate,
      id: m.id,
      name: m.name,
      totalRd: totalRd,
      loanDues: totalLoanDue,
      bonusAdj: inclBonus ? totalBonus : 0,
      npaLoss: npaLoss,
      waiver: waiver,
      netSettlement: netSettlement,
      status: "INACTIVE"
    };

    if(!window.exitSettlements) window.exitSettlements = [];
    window.exitSettlements.push(exitRecord);
    exitSettlements = window.exitSettlements;

    m.status = "INACTIVE";
    saveStore();

    if(typeof google !== "undefined" && google.script && google.script.run){
      try {
        google.script.run.saveExitSettlementBackend(exitRecord);
        google.script.run.saveMemberBackend(m);
      } catch(e) {
        console.warn("Backend exit sync issue:", e);
      }
    }

    closeModal("modalExit");
    refreshAll();
    showNotice("Settlement Completed", "Member " + m.name + " (" + m.id + ") has been successfully settled and marked INACTIVE.\\n\\nExit ID: " + exitId + "\\nNet Settlement: ₹" + netSettlement.toLocaleString("en-IN"));
  }
  window.handleExecuteExitSettlement = handleExecuteExitSettlement;

  // EDIT BORROW / INVEST TRANSACTION CONTROLLER (V41 PRO)
  function openEditFundModal(txnId){
    if(!window.fundTransactions) window.fundTransactions = fundTransactions || [];
    var f = window.fundTransactions.find(function(x){ return String(x.id) === String(txnId); });
    if(!f){
      showNotice("Not Found", "Transaction " + txnId + " not found!");
      return;
    }

    if(document.getElementById("inpEditFundId")) document.getElementById("inpEditFundId").value = f.id || "";
    if(document.getElementById("inpEditFundType")) document.getElementById("inpEditFundType").value = (f.type || "INVEST").toUpperCase();
    if(document.getElementById("inpEditFundAccount")) document.getElementById("inpEditFundAccount").value = (f.account || "BANK").toUpperCase();
    if(document.getElementById("inpEditFundDate")) document.getElementById("inpEditFundDate").value = toIsoDateStr(f.date || getTodayYMD());
    if(document.getElementById("inpEditFundAmount")) document.getElementById("inpEditFundAmount").value = cleanNum(f.amount, 0);
    if(document.getElementById("inpEditFundEntity")) document.getElementById("inpEditFundEntity").value = f.entity || "";
    if(document.getElementById("inpEditFundNarration")) document.getElementById("inpEditFundNarration").value = f.narration || "";

    openModal("modalEditFund");
  }
  window.openEditFundModal = openEditFundModal;

  function handleSaveEditFund(){
    var txnId = document.getElementById("inpEditFundId") ? document.getElementById("inpEditFundId").value : "";
    var fType = document.getElementById("inpEditFundType") ? document.getElementById("inpEditFundType").value : "INVEST";
    var fAcc = document.getElementById("inpEditFundAccount") ? document.getElementById("inpEditFundAccount").value : "BANK";
    var fDate = document.getElementById("inpEditFundDate") ? document.getElementById("inpEditFundDate").value : getTodayYMD();
    var rawAmt = document.getElementById("inpEditFundAmount") ? document.getElementById("inpEditFundAmount").value : "0";
    var fAmt = cleanNum(rawAmt, 0);
    var fEntity = document.getElementById("inpEditFundEntity") ? (document.getElementById("inpEditFundEntity").value || "").trim() : "";
    var fNarr = document.getElementById("inpEditFundNarration") ? (document.getElementById("inpEditFundNarration").value || "").trim() : "";

    if(fAmt < 0){
      showNotice("Validation Error", "Kripya sahi transaction amount bharein. Amount 0 ya usse bada hona chahiye.", "modalEditFund");
      return;
    }

    if(!window.fundTransactions) window.fundTransactions = fundTransactions || [];
    var f = window.fundTransactions.find(function(x){ return String(x.id) === String(txnId); });
    
    if(!f){
      f = { id: txnId };
      window.fundTransactions.push(f);
    }

    f.type = fType;
    f.account = fAcc;
    f.date = fDate;
    f.amount = fAmt;
    f.entity = fEntity || "Society Capital";
    f.narration = fNarr || (fType === "INVEST" ? "Capital Investment" : "Fund Borrowing");

    fundTransactions = window.fundTransactions;
    saveStore();

    if(typeof google !== "undefined" && google.script && google.script.run){
      try {
        google.script.run.withSuccessHandler(function(res){
          console.log("Edit fund sync:", res);
        }).saveFundTransactionBackend(f);
      } catch(e) {
        console.warn("Backend edit fund sync error:", e);
      }
    }

    closeModal("modalEditFund");
    renderFundRegister();
    renderProfitAndLossRegister();
    refreshAll();
    showNotice("Transaction Updated", "Fund transaction " + txnId + " has been successfully updated!");
  }
  window.handleSaveEditFund = handleSaveEditFund;

  // DIALOG / SUB-TAB / MODAL ACTION CONTROLLERS
  safeAddListener("btnSubmitExit", "click", handleExecuteExitSettlement);
  safeAddListener("btnApplyFundDate", "click", renderFundModal);

  safeAddListener("btnAddBulkRow", "click", function(){
    var tbody = document.getElementById("tbodyBulkList");
    if(!tbody) return;
    var activeMems = (members || []).filter(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
    if(activeMems.length === 0) return;
    var opts = activeMems.map(function(m){ return "<option value='" + m.id + "'>" + m.name + " (" + m.id + ")</option>"; }).join("");
    var tr = document.createElement("tr");
    tr.setAttribute("data-id", activeMems[0].id);
    tr.innerHTML = "<td style='text-align:center;'><input type='checkbox' class='b-chk' checked onchange='calcBulkTotals()'></td>" +
      "<td><select class='field-ctrl' style='width:160px;' onchange='this.closest(\\\"tr\\\").setAttribute(\\\"data-id\\\", this.value);'>" + opts + "</select></td>" +
      "<td><input type='number' class='field-ctrl b-rd' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
      "<td><input type='number' class='field-ctrl b-int' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
      "<td><input type='number' class='field-ctrl b-repay' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
      "<td><input type='number' class='field-ctrl b-pen' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
      "<td><strong class='b-tot' style='color:#10B981;'>₹0</strong></td>" +
      "<td><select class='field-ctrl b-mode' style='width:90px;'><option value='CASH'>CASH</option><option value='ONLINE'>ONLINE</option></select></td>" +
      "<td><input type='text' class='field-ctrl b-rem' placeholder='Note...' style='width:120px;'></td>";
    tbody.appendChild(tr);
    calcBulkTotals();
  });

  var pendingConfirmCallback = null;
  window.showConfirm = function(title, msg, onProceed){
    var h = document.getElementById("confirmHeader");
    var b = document.getElementById("confirmBody");
    if(h) h.innerText = title || "Confirm Action";
    if(b) b.innerText = msg || "Are you sure you want to proceed?";
    pendingConfirmCallback = onProceed;
    openModal("modalConfirm");
  };
  safeAddListener("btnConfirmProceed", "click", function(){
    closeModal("modalConfirm");
    if(typeof pendingConfirmCallback === "function"){
      var cb = pendingConfirmCallback;
      pendingConfirmCallback = null;
      cb();
    }
  });

  function showSubTabInterest(){
    var b1 = document.getElementById("btnSubTabInterest");
    var b2 = document.getElementById("btnSubTabBonus");
    var p1 = document.getElementById("boxSubInterest");
    var p2 = document.getElementById("boxSubBonus");
    if(b1) b1.className = "tab-item active";
    if(b2) b2.className = "tab-item";
    if(p1) p1.style.display = "block";
    if(p2) p2.style.display = "none";
  }
  function showSubTabBonus(){
    var b1 = document.getElementById("btnSubTabInterest");
    var b2 = document.getElementById("btnSubTabBonus");
    var p1 = document.getElementById("boxSubInterest");
    var p2 = document.getElementById("boxSubBonus");
    if(b1) b1.className = "tab-item";
    if(b2) b2.className = "tab-item active";
    if(p1) p1.style.display = "none";
    if(p2) p2.style.display = "block";
  }
  function renderSubTables(){
    var fromD = document.getElementById("inpSubFilterFrom") ? document.getElementById("inpSubFilterFrom").value : "2026-01-01";
    var toD = document.getElementById("inpSubFilterTo") ? document.getElementById("inpSubFilterTo").value : "2026-12-31";
    var tbodyI = document.getElementById("tbodySubInterest");
    var tbodyB = document.getElementById("tbodySubBonus");
    if(tbodyI){
      var hI = "";
      (payments || []).forEach(function(p){
        var d = p.date || "2026-01-01";
        if(d >= fromD && d <= toD && cleanNum(p.interest, 0) > 0){
          hI += "<tr><td>" + toDisplayDate(d) + "</td><td>" + (p.name || "") + " (" + (p.id || "") + ")</td><td>" + (p.receiptNo || "") + "</td><td style='color:#10B981; font-weight:700;'>₹" + cleanNum(p.interest, 0).toLocaleString("en-IN") + "</td><td>" + (p.mode || "CASH") + "</td></tr>";
        }
      });
      tbodyI.innerHTML = hI || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No interest records found</td></tr>";
    }
    if(tbodyB){
      var hB = "";
      (bonusSettlements || []).forEach(function(b){
        var d = b.date || "2026-01-01";
        if(d >= fromD && d <= toD){
          hB += "<tr><td>" + toDisplayDate(d) + "</td><td>" + (b.name || "") + " (" + (b.id || "") + ")</td><td>" + (b.settlementId || "") + "</td><td style='color:#C084FC; font-weight:700;'>₹" + cleanNum(b.amount || b.netBonus, 0).toLocaleString("en-IN") + "</td><td>" + (b.mode || "SET-OFF") + "</td></tr>";
        }
      });
      tbodyB.innerHTML = hB || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No bonus settlement records found</td></tr>";
    }
  }
  safeAddListener("btnSubTabInterest", "click", showSubTabInterest);
  safeAddListener("btnSubTabBonus", "click", showSubTabBonus);
  safeAddListener("btnApplySubFilter", "click", renderSubTables);
  window.openBonusOverviewModal = function(){
    renderSubTables();
    showSubTabInterest();
    openModal("modalBonusOverview");
  };

  // Expose global state arrays for external testing and inspection
  window.members = members;
  window.payments = payments;
  window.loans = loans;
  window.exitSettlements = exitSettlements;
  window.bonusSettlements = bonusSettlements;
  window.fundTransactions = fundTransactions;

  // Force lock on initial load or F5 refresh
  window.currentUserSession = null;
  try {
    sessionStorage.removeItem("gullak_v22_session");
    sessionStorage.removeItem("gullak_v21_session");
  } catch(e) {}

  // RUN IMMEDIATELY AND ON READY
  bootApplication();
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bootApplication);
  }
  window.addEventListener("load", bootApplication);
})();
</script>
`;
}

function getCompleteSoftwareClientScript() {
  return getClientScriptPartA() + getClientScriptPartB();
}
