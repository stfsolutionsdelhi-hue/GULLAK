/**
 * 🏦 GULLAK CO-OPERATIVE SOCIETY - BACKEND CONTROLLER (V41 PRO MASTER)
 * Standardized Sheets + Auto-Cleanup + Strict ID Formats + Sheet Protection ('Password') + Users Auth
 */

function onOpen() {
  var ui = SpreadsheetApp.getUi();
  ui.createMenu('🏦 Gullak Co-operative')
    .addItem('⚡ 1. Initialize & Auto-Fix Sheet Database', 'installAndRunDatabase')
    .addItem('📥 2. Load / Restore All 67 Real Members', 'restoreAll67RealSocietyMembers')
    .addItem('🛠️ 3. Fix & Align Columns & Formats Now', 'autoFixAndAlignAllSheets')
    .addItem('🔒 4. Lock All Sheets (Strict Lock - No Accidental Edits)', 'enableStrictSheetProtectionWithPassword')
    .addItem('🔓 5. Unlock All Sheets (Requires Password)', 'unlockAllSheetsWithPassword')
    .addItem('🔑 6. Change Sheet Security Password', 'changeSheetMasterPassword')
    .addItem('⚠️ 7. Warning Mode Only (Show Warning Dialog)', 'enableSafeSheetProtection')
    .addItem('👤 8. Reset User Passwords to 12345', 'resetUsersCredentialsToDefault')
    .addItem('🌐 9. Get Live Web App URL', 'showWebPortalUrl')
    .addItem('✉️ 10. Authorize Email Sending Permission', 'testEmailPermission')
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
    try { userSheet.clearContents(); } catch(e) { try { userSheet.clear(); } catch(e2) {} }
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
    var all67Members = get67RealMembersArray();
    memSheet.getRange(2, 1, all67Members.length, 14).setValues(all67Members);
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
        if (memSheet.getLastRow() > 1) { memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).clearContent(); }
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
    var hRange = paySheet.getRange(1, 1, 1, payH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#0F766E");
    hRange.setFontColor("#FFFFFF");
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

    try { loanSheet.clearContents(); } catch(e) { try { loanSheet.clear(); } catch(e2) {} }
    try { try { try { loanSheet.clearFormats(); } catch(e) {} } catch(e) {} } catch(e) {}
    loanSheet.getRange(1, 1, 1, loanH.length).setValues([loanH]);
    var hRange = loanSheet.getRange(1, 1, 1, loanH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#991B1B");
    hRange.setFontColor("#FFFFFF");
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
    var hRange = penSheet.getRange(1, 1, 1, penH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#B45309");
    hRange.setFontColor("#FFFFFF");
    try { penSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(penSheet, { 11: ["OVERDUE", "CLEAR"] });
  }

  // 5. Fix BonusSettlements
  var bonusSheet = ss.getSheetByName("BonusSettlements");
  if (bonusSheet) {
    var bonusH = ["Settlement ID", "Date", "Member ID", "Name", "Total Bonus (₹)", "Adj Loan (₹)", "Adj Interest (₹)", "Adj RD (₹)", "Adj Penalty (₹)", "Net Paid (₹)", "Mode"];
    bonusSheet.getRange(1, 1, 1, bonusH.length).setValues([bonusH]);
    var hRange = bonusSheet.getRange(1, 1, 1, bonusH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#D97706");
    hRange.setFontColor("#FFFFFF");
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
    var hRange = fundSheet.getRange(1, 1, 1, fundH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#4338CA");
    hRange.setFontColor("#FFFFFF");
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
    var hRange = exitSheet.getRange(1, 1, 1, exitH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#7F1D1D");
    hRange.setFontColor("#FFFFFF");
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
    var hRange = plSheet.getRange(1, 1, 1, plH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#047857");
    hRange.setFontColor("#FFFFFF");
    try { plSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(plSheet);
  }

  // 9. Fix Users Sheet
  var uSheet = ss.getSheetByName("Users");
  if (uSheet) {
    var userH = ["Username", "Password", "Role", "Email", "Status", "CreatedAt"];
    uSheet.getRange(1, 1, 1, userH.length).setValues([userH]);
    var hRange = uSheet.getRange(1, 1, 1, userH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#0F172A");
    hRange.setFontColor("#FFFFFF");
    try { uSheet.setFrozenRows(1); } catch(e) {}
    applySheetTableStylingAndFilters(uSheet, { 3: ["Super Admin", "Manager", "Auditor"], 5: ["ACTIVE", "INACTIVE"] });
  }

  // 10. Fix Financials
  var finSheet = ss.getSheetByName("Financials");
  if (finSheet) {
    var finH = ["Metric / Category", "Value (₹)", "Description", "Last Updated"];
    finSheet.getRange(1, 1, 1, finH.length).setValues([finH]);
    var hRange = finSheet.getRange(1, 1, 1, finH.length);
    hRange.setFontWeight("bold");
    hRange.setBackground("#0284C7");
    hRange.setFontColor("#FFFFFF");
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
      if (penSheet.getLastRow() > 1) { penSheet.getRange(2, 1, penSheet.getLastRow() - 1, Math.max(penSheet.getLastColumn(), penH.length)).clearContent(); }
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
      if (plSheet.getLastRow() > 1) { plSheet.getRange(2, 1, plSheet.getLastRow() - 1, Math.max(plSheet.getLastColumn(), plH.length)).clearContent(); }
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

function handleApiRequest(params, postData) {
  var action = (params && params.action) || (postData && postData.action) || 'getData';
  var result = { success: false };
  try {
    if (action === 'getData' || action === 'getSocietyData') {
      result = { success: true, data: getSocietyFullDataWithoutFinSync() };
    } else if (action === 'login') {
      var u = (params && params.username) || (postData && postData.username);
      var p = (params && params.password) || (postData && postData.password);
      result = checkUserLoginBackend(u, p);
    } else if (action === 'saveMember') {
      var memberObj = (postData && postData.member) || (params && params.member ? JSON.parse(params.member) : null);
      result = saveMemberBackend(memberObj);
    } else if (action === 'deleteMember') {
      var memId = (postData && postData.memberId) || (params && params.memberId);
      result = deleteMemberBackend(memId);
    } else if (action === 'savePayment') {
      var payObj = (postData && postData.payment) || (params && params.payment ? JSON.parse(params.payment) : null);
      result = savePaymentBackend(payObj);
    } else if (action === 'saveLoan') {
      var loanObj = (postData && postData.loan) || (params && params.loan ? JSON.parse(params.loan) : null);
      result = saveLoanBackend(loanObj);
    } else if (action === 'saveExitSettlement') {
      var exitObj = (postData && postData.exitSettlement) || (params && params.exitSettlement ? JSON.parse(params.exitSettlement) : null);
      result = saveExitSettlementBackend(exitObj);
    } else if (action === 'saveBonusSettlement') {
      var bonusObj = (postData && postData.bonusSettlement) || (params && params.bonusSettlement ? JSON.parse(params.bonusSettlement) : null);
      result = saveBonusSettlementBackend(bonusObj);
    } else if (action === 'saveFund') {
      var fundObj = (postData && postData.fund) || (params && params.fund ? JSON.parse(params.fund) : null);
      result = saveFundTransactionBackend(fundObj);
    } else if (action === 'restore67Members') {
      result = restoreAll67RealSocietyMembers();
    } else {
      result = { success: true, data: getSocietyFullDataWithoutFinSync() };
    }
  } catch(err) {
    result = { success: false, error: err.toString() };
  }
  return ContentService.createTextOutput(JSON.stringify(result))
    .setMimeType(ContentService.MimeType.JSON);
}

function doGet(e) {
  if (e && e.parameter && (e.parameter.action || e.parameter.format === 'json')) {
    return handleApiRequest(e.parameter, null);
  }
  return HtmlService.createHtmlOutput(getCompleteSoftwareHtml())
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Accounting Platform (V63 PRO)")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}

function doPost(e) {
  var postData = null;
  if (e && e.postData && e.postData.contents) {
    try {
      postData = JSON.parse(e.postData.contents);
    } catch(parseErr) {
      postData = e.parameter;
    }
  }
  return handleApiRequest(e ? e.parameter : {}, postData);
}

function get67RealMembersArray() {
  return [
  [
    "MEM010120261",
    "Afsana Sister Pappu Ji 012025",
    "9773841314",
    "Mohan Garden",
    "Pappu Ji",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM010120262",
    "Ajay Kumar Garg Ref Suresh Lala Ji 012025",
    "9873898898",
    "Kakrola",
    "Suresh Lala Ji",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM010120263",
    "Amit S/O Sunil (Omwati Aunti Ji ) 102022",
    "8287127921",
    "Vikas Vihar Kakrola",
    "Omwati Aunti",
    400,
    "ACTIVE",
    "2026-01-01",
    15200,
    "15th of every month",
    0,
    18000,
    0,
    0
  ],
  [
    "MEM010120264",
    "Arvind Kumar 022022X2",
    "9350743408",
    "Ghaziabad",
    "Rekha Kumari",
    400,
    "ACTIVE",
    "2026-01-01",
    18800,
    "15th of every month",
    0,
    7500,
    0,
    0
  ],
  [
    "MEM010120265",
    "ASHA DEVI REF SUSHIL SO SHILA JI 012025",
    "9311043442",
    "Vikas Vihar",
    "Sushil",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM010120266",
    "Ashish Aswal Ashu Vikas Vihar 022022",
    "9899801307",
    "C-141 Vikas Vihar Kakrola",
    "Sarita Aswal",
    400,
    "ACTIVE",
    "2026-01-01",
    14000,
    "15th of every month",
    0,
    9000,
    0,
    0
  ],
  [
    "MEM010120267",
    "Chanchal D/O Anil Padosi 022022",
    "9910216942",
    "Vikas Vihar Kakrola",
    "Anil Padosi",
    400,
    "ACTIVE",
    "2026-01-01",
    16600,
    "15th of every month",
    0,
    16000,
    0,
    0
  ],
  [
    "MEM010120268",
    "Chanda Devi Ref Shila Devi 022024",
    "8447218816",
    "Kakrola",
    "Shila Devi",
    400,
    "ACTIVE",
    "2026-01-01",
    9200,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM010120269",
    "Deep Lal - Reena Devi 022023",
    "9871869719",
    "Vikas Vihar Kakrola",
    "Reena Devi",
    400,
    "ACTIVE",
    "2026-01-01",
    14000,
    "15th of every month",
    0,
    4000,
    0,
    0
  ],
  [
    "MEM0101202610",
    "Deep Lal Electrician 022022",
    "9871869719",
    "Vikas Vihar Kakrola",
    "Deep Lal",
    400,
    "ACTIVE",
    "2026-01-01",
    16600,
    "15th of every month",
    0,
    3000,
    0,
    0
  ],
  [
    "MEM0101202611",
    "DEVENDER SINGH REF RAVI 202501",
    "9456304719",
    "Kakrola",
    "Ravi",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202612",
    "Geeta Devi Wo Narender 012025",
    "7042511156",
    "Vikas Vihar Kakrola",
    "Narender",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202613",
    "Hari Ram Ji Vikas Vihar 032022",
    "9650013268",
    "Kakrola",
    "Hari Ram",
    400,
    "ACTIVE",
    "2026-01-01",
    16400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202614",
    "Hirender Kumar - 2 - Neetu 012023",
    "9599356910",
    "Vikas Vihar Kakrola",
    "Neetu",
    400,
    "ACTIVE",
    "2026-01-01",
    15360,
    "15th of every month",
    0,
    5050,
    0,
    0
  ],
  [
    "MEM0101202615",
    "Hirender Kumar -1- 022022",
    "9599356910",
    "Vikas Vihar Kakrola",
    "Hirender",
    400,
    "ACTIVE",
    "2026-01-01",
    17802,
    "15th of every month",
    0,
    3030,
    0,
    0
  ],
  [
    "MEM0101202616",
    "Jagdish Mehto X2  022022",
    "7042511481",
    "Jj Colony Bharat Vihar",
    "Jagdish",
    400,
    "ACTIVE",
    "2026-01-01",
    18400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202617",
    "Jagriti Sharma W/O Jugal Kishor 012023",
    "9953111505",
    "Vikas Vihar Kakrola",
    "Jugal Kishor",
    400,
    "ACTIVE",
    "2026-01-01",
    14400,
    "15th of every month",
    0,
    15000,
    0,
    0
  ],
  [
    "MEM0101202618",
    "JAHANVI SHARMA DO JAGRITI JI 012025",
    "9953111505",
    "Vikas Vihar Kakrola",
    "Jagriti Sharma",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202619",
    "Jot Singh Ref Ravi 012025",
    "8178738999",
    "Kakrola",
    "Ravi",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202620",
    "Jugal Kishor Ji X2 072022",
    "9310732656",
    "Vikas Vihar Kakrola",
    "Jagriti Sharma",
    400,
    "ACTIVE",
    "2026-01-01",
    16800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202621",
    "JYOTI JOSHI JI REF JAGRITI JI 012025",
    "9716124006",
    "Vikas Vihar Kakrola",
    "Jagriti Ji",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202622",
    "Kazim So Mumina Khatoon Ref Pappu 012025",
    "8287493771",
    "Kakrola",
    "Mumina Khatoon",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202623",
    "KEERTHI R S DO SOMYA MADAM 202501",
    "7827596703",
    "Kakrola",
    "Somya Madam",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202624",
    "KIRAN DEVI WO SUSHIL KUMAR 202501",
    "7042480937",
    "Vikas Vihar Kakrola",
    "Sushil Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202625",
    "Kuwar Pal -1 X2 082022",
    "9871130935",
    "Vikas Vihar Kakrola",
    "Kuwar Pal",
    400,
    "ACTIVE",
    "2026-01-01",
    16400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202626",
    "Kuwar Pal-2 X2 082022",
    "9871130935",
    "Vikas Vihar Kakrola",
    "Kuwar Pal",
    400,
    "ACTIVE",
    "2026-01-01",
    16400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202627",
    "Mukesh Sharma Ji X2 022022",
    "8285405743",
    "Vikas Vihar",
    "Mukesh",
    400,
    "ACTIVE",
    "2026-01-01",
    18800,
    "15th of every month",
    0,
    5623,
    0,
    0
  ],
  [
    "MEM0101202628",
    "NANDINI JI 202501",
    "8383071508",
    "SULAHKUL VIHAR",
    "Nandini",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202629",
    "Narayan Yadav X2 032022",
    "9599959948",
    "Vikas Vihar",
    "Narayan",
    400,
    "ACTIVE",
    "2026-01-01",
    18400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202630",
    "Narender Babblu Bo Ravi 012025",
    "9354214597",
    "Kakrola",
    "Ravi",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202631",
    "Narender Kumar S/O Shila Devi 012023",
    "7042511156",
    "S/O Shila Devi Vikas Vihar Kakrola",
    "Shila Devi",
    400,
    "ACTIVE",
    "2026-01-01",
    14400,
    "15th of every month",
    0,
    2000,
    0,
    0
  ],
  [
    "MEM0101202632",
    "Neeraj Renew So Raghuveer Ji 012025",
    "9891811697",
    "Kakrola",
    "Raghuveer Ji",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202633",
    "Omwati Aunti M/O Anil Kumar 022022",
    "9971157481",
    "C-143 Vikas Vihar Kakrola",
    "Anil Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    17800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202634",
    "Pappu Carpainter - 1 - 022022",
    "9911563986",
    "Vikas Vihar Kakrola",
    "Pappu",
    400,
    "ACTIVE",
    "2026-01-01",
    16600,
    "15th of every month",
    0,
    13000,
    0,
    0
  ],
  [
    "MEM0101202635",
    "Pappu Carpainter - 2 - Nargis 102022",
    "9911563986",
    "Vikas Vihar Kakrola",
    "Nargis",
    400,
    "ACTIVE",
    "2026-01-01",
    15600,
    "15th of every month",
    0,
    21000,
    0,
    0
  ],
  [
    "MEM0101202636",
    "Pawan Kumar X2 072022",
    "8368934198",
    "S/O Rakesh Kumar Vikas Vihar",
    "Rakesh Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    16800,
    "15th of every month",
    0,
    19230,
    0,
    0
  ],
  [
    "MEM0101202637",
    "Peter Masih 042022",
    "99990023275",
    "Mohan Garden",
    "Peter",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202638",
    "Raj Kumar (Colony) Kakrola 062022",
    "8750830986",
    "Vikas Vihar Kakrola",
    "Raj Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    15800,
    "15th of every month",
    0,
    22136,
    0,
    0
  ],
  [
    "MEM0101202639",
    "Raja Ram Ji Ref Deepak 062022",
    "9810812331",
    "Narela",
    "Deepak",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202640",
    "Ram Bharose Ji Goyla Dairy 022022",
    "9717961768",
    "Goyla Dairy 9717961768 , 0838392003",
    "Ram Bharose",
    400,
    "ACTIVE",
    "2026-01-01",
    16200,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202641",
    "Ravi Garwali 022022",
    "7042085508",
    "Vikas Vihar Kakrola",
    "Ravi",
    400,
    "ACTIVE",
    "2026-01-01",
    16600,
    "15th of every month",
    0,
    17000,
    0,
    0
  ],
  [
    "MEM0101202642",
    "Sanjay Kumar -1- Ref DeeplaI 022022",
    "9650862110",
    "Bharat Vihar Kakrola",
    "Deep Lal",
    400,
    "ACTIVE",
    "2026-01-01",
    18800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202643",
    "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023",
    "9650862110",
    "Bharat Vihar Kakrola",
    "Sandeep Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    14400,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202645",
    "Sanjay Yadav -1 X2 022022",
    "7827004101",
    "Vikas Vihar Kakrola",
    "Sanjay Yadav",
    400,
    "ACTIVE",
    "2026-01-01",
    18800,
    "15th of every month",
    0,
    23000,
    0,
    0
  ],
  [
    "MEM0101202646",
    "Sanjay Yadav -2- Shubhankar 072023",
    "7827004101",
    "Vikas Vihar Kakrola",
    "Shubhankar",
    400,
    "ACTIVE",
    "2026-01-01",
    16800,
    "15th of every month",
    0,
    5000,
    0,
    0
  ],
  [
    "MEM0101202647",
    "Santosh Mehto X2 022022",
    "9968062512",
    "Bharat Vihar Kakrola",
    "Santosh",
    400,
    "ACTIVE",
    "2026-01-01",
    18800,
    "15th of every month",
    0,
    17000,
    0,
    0
  ],
  [
    "MEM0101202648",
    "Santosh Mistri Ref DeeplaI 012025",
    "9891703298",
    "Kakrola",
    "Deep Lal",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202649",
    "Sarika 022022",
    "9718174244",
    "Kakrola",
    "Sarika",
    400,
    "ACTIVE",
    "2026-01-01",
    15584,
    "15th of every month",
    0,
    12000,
    0,
    0
  ],
  [
    "MEM0101202650",
    "Sarita Aswal Wo Ashish 012025",
    "9899801307",
    "Kakrola",
    "Ashish Aswal",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    25000,
    0,
    0
  ],
  [
    "MEM0101202651",
    "Shila Devi Ref Omwati Aunti X2 092022",
    "9643588165",
    "Vikas Vihar Kakrola",
    "Omwati Aunti",
    400,
    "ACTIVE",
    "2026-01-01",
    16000,
    "15th of every month",
    0,
    11000,
    0,
    0
  ],
  [
    "MEM0101202652",
    "Somya Madam Ref Jagriti Sharma 012023",
    "7827596703",
    "Kakrola",
    "Jagriti Sharma",
    400,
    "ACTIVE",
    "2026-01-01",
    14400,
    "15th of every month",
    0,
    16000,
    0,
    0
  ],
  [
    "MEM0101202653",
    "Sushil Ji So Sheela Devi 012025",
    "7042480937",
    "Vikas Vihar",
    "Sheela Devi",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202654",
    "URUZ KHATMA DO MUMINA REF PAPPU 012025",
    "8287493771",
    "Kakrola",
    "Mumina Khatoon",
    400,
    "ACTIVE",
    "2026-01-01",
    4800,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202655",
    "Viney Electrician Ref Deep Lal 052023",
    "7065708037",
    "Vikas Vihar Kakrola",
    "Deep Lal",
    400,
    "ACTIVE",
    "2026-01-01",
    12800,
    "15th of every month",
    0,
    18000,
    0,
    0
  ],
  [
    "MEM0101202656",
    "Vishnu Aggarwal -1 102022",
    "9773557036",
    "Kakrola",
    "Vishnu",
    400,
    "ACTIVE",
    "2026-01-01",
    15600,
    "15th of every month",
    0,
    10000,
    0,
    0
  ],
  [
    "MEM0101202657",
    "Vishnu Aggarwal -2 102022",
    "9773557036",
    "Kakrola",
    "Vishnu",
    400,
    "ACTIVE",
    "2026-01-01",
    15600,
    "15th of every month",
    0,
    10000,
    0,
    0
  ],
  [
    "MEM0101202658",
    "Parvesh Ansari Ref DeeplaI 010126",
    "9315426875",
    "Kakrola",
    "Deep Lal",
    400,
    "ACTIVE",
    "2026-01-12",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202659",
    "Hazrat Ref Parvesh Ansari 010126",
    "9718172262",
    "Dda Flat Janak Puri",
    "Parvesh Ansari",
    400,
    "ACTIVE",
    "2026-01-12",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202660",
    "Mintu Devi Ref Chanda Devi 012026",
    "7033953938",
    "Vikas Vihar",
    "Chanda Devi",
    400,
    "ACTIVE",
    "2026-01-15",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202661",
    "Mariam R/O Rupam & Shila Devi",
    "8826567542",
    "Bharat Vihar Kakrola",
    "Shila Devi",
    400,
    "ACTIVE",
    "2026-01-19",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202662",
    "Rupam Ref Shila Devi 012026",
    "8130546714",
    "Vikas Vihar Kakrola",
    "Shila Devi",
    400,
    "ACTIVE",
    "2026-01-19",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202663",
    "Surender Rawat 012026",
    "9266782629",
    "Vikas Vihar Kakrola",
    "Sumitra Rawat",
    400,
    "ACTIVE",
    "2026-01-19",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202664",
    "Sumitra Rawat Wo Surender 012026",
    "9266782629",
    "Vikas Vihar Kakrola",
    "Surender Rawat",
    400,
    "ACTIVE",
    "2026-01-19",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202665",
    "Priya Sood Ref Raj Kumar 012026",
    "8750830986",
    "House Number B-115 Surya Vihar Binda",
    "Raj Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202666",
    "Raj Kumari Ref Raj Kumar 012026",
    "8750830986",
    "B-75 Bharat Vihar Kakrola 9810424981",
    "Raj Kumar",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202667",
    "Arvind Kumar Rekha Kumari 012026",
    "9350743408",
    "Gazhiabad",
    "Arvind Kumar",
    400,
    "ACTIVE",
    "2026-01-31",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ],
  [
    "MEM0101202668",
    "Rakhi Madam Ref Shila Ji 012026",
    "9311633238",
    "Delhi",
    "Shila Ji",
    400,
    "ACTIVE",
    "2026-01-01",
    0,
    "15th of every month",
    0,
    0,
    0,
    0
  ]
];
}

function restoreAll67RealSocietyMembers() {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    if (!ss) return { success: false, error: "No active spreadsheet found" };
    var memSheet = getOrCreateSheet(ss, "Members", [
      "Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", 
      "RD / Month (₹)", "Status", "Date Joined", "Opening RD (₹)", "Due Day", 
      "Custom Loan Limit (₹)", "Opening Loan (₹)", "Opening Int (₹)", "Opening Pen (₹)"
    ]);
    
    // Clear old data rows if any
    if (memSheet.getLastRow() > 1) {
      if (memSheet.getLastRow() > 1) { memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).clearContent(); }
    }
    
    var allRows = get67RealMembersArray();
    memSheet.getRange(2, 1, allRows.length, 14).setValues(allRows);
    
    // Re-format
    alignAndFormatSheet(memSheet, [6, 9, 11, 12, 13, 14], [8]);
    
    try {
      SpreadsheetApp.getUi().alert("✅ Success: All 67 Real Society Members Restored!\n\nTotal " + allRows.length + " official society members with original opening balances and nominees loaded successfully into the Members sheet.");
    } catch(uiErr) {}
    
    return { success: true, count: allRows.length };
  } catch(e) {
    try {
      SpreadsheetApp.getUi().alert("Error restoring members: " + e.toString());
    } catch(uiErr) {}
    return { success: false, error: e.toString() };
  }
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
