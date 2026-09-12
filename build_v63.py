import json, re, zipfile, os

with open('Part1_Server.gs', 'r') as f:
    part1 = f.read()

# Get 67 members data
p = part1.find('function getDefaultDataFallback()')
p2 = part1.find('function checkUserLoginBackend(', p)
fallback_code = part1[p:p2]

m_match = re.search(r'members:\s*(\[.*?\]),\s*payments:', fallback_code, re.DOTALL)
members = json.loads(m_match.group(1))

member_rows = []
for m in members:
    row = [
        m['id'],
        m['name'],
        str(m.get('mobile', '')),
        str(m.get('address', '')),
        str(m.get('nominee', '')),
        int(m.get('rd', 400)),
        str(m.get('status', 'ACTIVE')),
        str(m.get('dateJoined', '2026-01-01')),
        round(float(m.get('rdPaid', 0))),
        str(m.get('dueDay', '15th of every month')),
        int(m.get('customLimit', 0)),
        int(m.get('opLoan', 0)),
        int(m.get('opInt', 0)),
        int(m.get('opPen', 0))
    ]
    member_rows.append(row)

members_rows_json = json.dumps(member_rows, indent=2)

# Update onOpen menu to include option 2: Force load all 67 members
old_menu_target = """function onOpen() {
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
    .addToUi();"""

new_menu_target = """function onOpen() {
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
    .addToUi();"""

# Replace in installAndRunDatabase the 4 dummy rows with all 67 members
old_sample_m = """  if (memSheet.getLastRow() <= 1) {
    var sampleM = [
      ["MEM010120261", "Rahul Kumar", "9810011111", "H-12, Sector 3, Rohini", "Sunita Kumar (Wife)", 400, "ACTIVE", "2026-01-01", 4800, "15th of every month", 0, 0, 0, 0],
      ["MEM010120262", "Suresh Sharma", "9810022222", "Shop 4, Main Market", "Vikas Sharma (Son)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120263", "Amit Verma", "9810033333", "B-45, Shastri Nagar", "Pooja Verma (Wife)", 400, "ACTIVE", "2026-01-01", 4400, "15th of every month", 0, 0, 0, 0],
      ["MEM010120264", "SANISH", "9718174244", "ASD", "DFFF", 400, "ACTIVE", "2026-01-01", 1000, "15th of every month", 0, 0, 0, 0]
    ];
    memSheet.getRange(2, 1, sampleM.length, 14).setValues(sampleM);
  }"""

new_sample_m = """  if (memSheet.getLastRow() <= 1) {
    var all67Members = get67RealMembersArray();
    memSheet.getRange(2, 1, all67Members.length, 14).setValues(all67Members);
  }"""

# Update doGet and add doPost and handleApiRequest
old_doget = """function doGet(e) {
  return HtmlService.createHtmlOutput(getCompleteSoftwareHtml())
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Accounting Platform (V41 PRO)")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}"""

new_doget = """function handleApiRequest(params, postData) {
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
  return """ + members_rows_json + """;
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
      memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).clearContent();
    }
    
    var allRows = get67RealMembersArray();
    memSheet.getRange(2, 1, allRows.length, 14).setValues(allRows);
    
    // Re-format
    alignAndFormatSheet(memSheet, [6, 9, 11, 12, 13, 14], [8]);
    
    try {
      SpreadsheetApp.getUi().alert("✅ Success: All 67 Real Society Members Restored!\\n\\nTotal " + allRows.length + " official society members with original opening balances and nominees loaded successfully into the Members sheet.");
    } catch(uiErr) {}
    
    return { success: true, count: allRows.length };
  } catch(e) {
    try {
      SpreadsheetApp.getUi().alert("Error restoring members: " + e.toString());
    } catch(uiErr) {}
    return { success: false, error: e.toString() };
  }
}
"""

if old_menu_target in part1:
    part1 = part1.replace(old_menu_target, new_menu_target)
    print("Replaced onOpen menu")
else:
    print("WARNING: old_menu_target not found")

if old_sample_m in part1:
    part1 = part1.replace(old_sample_m, new_sample_m)
    print("Replaced sampleM with 67 members")
else:
    print("WARNING: old_sample_m not found")

if old_doget in part1:
    part1 = part1.replace(old_doget, new_doget)
    print("Replaced doGet with API support and restore function")
else:
    print("WARNING: old_doget not found")

with open('Part1_Server.gs', 'w') as f:
    f.write(part1)

with open('Part2_Html.gs', 'r') as f:
    part2 = f.read()

with open('Part3A.gs', 'r') as f:
    part3a = f.read()

with open('Part3B.gs', 'r') as f:
    part3b = f.read()

full_v63_gs = part1 + '\n\n' + part2 + '\n\n' + part3a + '\n\n' + part3b

with open('Gullak_Master_V63_PRO.gs', 'w') as f:
    f.write(full_v63_gs)

# Also write to code.gs / Code.gs
with open('code.gs', 'w') as f:
    f.write(full_v63_gs)

# Create zip files
zip_names = ['Gullak_Master_V63_PRO.zip', 'v63_code.zip', 'V63_CODE.zip', 'Code_gs_Only_V63_PRO.zip']
for zn in zip_names:
    with zipfile.ZipFile(zn, 'w', zipfile.ZIP_DEFLATED) as zf:
        zf.writestr('Code.gs', full_v63_gs)
        zf.writestr('Part1_Server.gs', part1)
        zf.writestr('Part2_Html.gs', part2)
        zf.writestr('Part3A.gs', part3a)
        zf.writestr('Part3B.gs', part3b)

print("V63 PRO successfully assembled! Files written and zipped.")
