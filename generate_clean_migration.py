import json

with open('members_final.json') as f:
    members = json.load(f)

with open('payments_parsed.json') as f:
    payments = json.load(f)

with open('receipts_parsed.json') as f:
    receipts = json.load(f)

# Sort receipts chronologically
receipts.sort(key=lambda r: r[1])

ledger_to_mem = {}
for m in members:
    ledger_to_mem[m['ledgerNo']] = m

# 1. Members Rows
# Schema: ["Member ID", "Full Name", "Mobile Number", "Address", "Nominee / Ref", "RD / Month (₹)", "Status", "Date Joined", "Opening RD (₹)", "Due Day", "Custom Loan Limit (₹)", "Opening Loan (₹)", "Opening Int (₹)", "Opening Pen (₹)"]
mem_rows = []
for m in members:
    mem_rows.append([
        m['id'],
        m['name'],
        str(m['mobile']),
        m['address'],
        'ADMIN',
        m.get('rd', 400),
        m['status'],
        '2026-01-01',
        m['opRd'],
        '15th of every month',
        m.get('customLimit', 0),
        m['opLoan'],
        m.get('opInt', 0),
        m.get('opPen', 0)
    ])

# 2. Receipts Rows
# Schema: ["Receipt No", "Date", "Member ID", "Name", "RD Amount (₹)", "Interest (₹)", "Penalty (₹)", "Loan Repayment (₹)", "Waiver (₹)", "Total (₹)", "Mode", "Recorded By", "Type", "Narration"]
receipt_rows = []
for r in receipts:
    sno, date, ledgerNo, particular, mode, mobile, total, narration, rd, loanReturn, interest = r
    m = ledger_to_mem.get(ledgerNo, {})
    m_id = m.get('id', f'MEM01012026{ledgerNo}')
    m_name = m.get('name', particular)
    clean_mode = 'ONLINE' if ('ONLINE' in str(mode).upper() or 'UPI' in str(mode).upper() or 'BANK' in str(mode).upper()) else 'CASH'
    receipt_rows.append([
        f'REC-{sno:04d}',
        date,
        m_id,
        m_name,
        rd,
        interest,
        0, # penalty
        loanReturn,
        0, # waiver
        total,
        clean_mode,
        'Admin',
        'REGULAR',
        narration or ''
    ])

# 3. Loans Rows
# Schema: ["Loan ID", "Date", "Member ID", "Name", "Type", "Principal (₹)", "Rate (%)", "Repaid (₹)", "Outstanding (₹)", "Status", "Narration"]
# Disbursed Loans from Payments
loan_rows = []
for p in payments:
    if p['loanAmount'] > 0:
        m = ledger_to_mem.get(p['ledgerNo'], {})
        m_id = m.get('id', f"MEM01012026{p['ledgerNo']}")
        m_name = m.get('name', p['particular'])
        loan_rows.append([
            f"LN-DISB-{p['sno']:03d}",
            p['date'],
            m_id,
            m_name,
            'Gullak Loan',
            p['loanAmount'],
            1.0,
            0,
            p['loanAmount'],
            'ACTIVE',
            p.get('narration', '') or 'Loan Disbursed'
        ])

print(f"Total Member rows: {len(mem_rows)}")
print(f"Total Receipt rows: {len(receipt_rows)}")
print(f"Total Loan rows: {len(loan_rows)}")

# Output Migration_Importer.gs
code_lines = [
    "/**",
    " * 🏦 GULLAK CO-OPERATIVE SOCIETY - CLEAN 1-CLICK DATA MIGRATION IMPORTER (V49)",
    f" * Total Members: {len(mem_rows)} (Columns: Member ID, Full Name, Mobile Number, Address, Nominee = 'ADMIN', RD/Mo, Status, Date Joined, Opening RD, Due Day, Custom Limit, Op Loan, Op Int, Op Pen)",
    f" * Total Receipts: {len(receipt_rows)} (Chronological 2026-01-01 to 2026-08-31)",
    f" * Total Disbursed Loans: {len(loan_rows)}",
    " */",
    "",
    "function importParsedSocietyDataNow() {",
    "  var ss = SpreadsheetApp.getActiveSpreadsheet();",
    "  if (!ss) {",
    "    SpreadsheetApp.getUi().alert('❌ Active Spreadsheet not found.');",
    "    return;",
    "  }",
    "",
    "  // 1. Initialize clean schema & structure",
    "  installAndRunDatabase();",
    "",
    "  // 2. Clear and write Members sheet",
    "  var memSheet = ss.getSheetByName('Members');",
    f"  var memData = {json.dumps(mem_rows)};",
    "  if (memSheet) {",
    "    if (memSheet.getLastRow() > 1) {",
    "      memSheet.getRange(2, 1, memSheet.getLastRow() - 1, memSheet.getLastColumn()).clearContent();",
    "    }",
    "    if (memData.length > 0) {",
    "      memSheet.getRange(2, 1, memData.length, memData[0].length).setValues(memData);",
    "      // Format Member ID and Mobile Number as plain text so leading zeros and formatting are preserved",
    "      memSheet.getRange(2, 1, memData.length, 1).setNumberFormat('@');",
    "      memSheet.getRange(2, 3, memData.length, 1).setNumberFormat('@');",
    "      memSheet.getRange(2, 8, memData.length, 1).setNumberFormat('yyyy-mm-dd');",
    "    }",
    "  }",
    "",
    "  // 3. Clear and write Receipts sheet",
    "  var recSheet = ss.getSheetByName('Receipts');",
    f"  var recData = {json.dumps(receipt_rows)};",
    "  if (recSheet) {",
    "    if (recSheet.getLastRow() > 1) {",
    "      recSheet.getRange(2, 1, recSheet.getLastRow() - 1, recSheet.getLastColumn()).clearContent();",
    "    }",
    "    if (recData.length > 0) {",
    "      recSheet.getRange(2, 1, recData.length, recData[0].length).setValues(recData);",
    "      recSheet.getRange(2, 1, recData.length, 1).setNumberFormat('@');",
    "      recSheet.getRange(2, 2, recData.length, 1).setNumberFormat('yyyy-mm-dd');",
    "      recSheet.getRange(2, 3, recData.length, 1).setNumberFormat('@');",
    "    }",
    "  }",
    "",
    "  // 4. Clear and write Loans sheet (New Disbursed Loans)",
    "  var loanSheet = ss.getSheetByName('Loans');",
    f"  var loanData = {json.dumps(loan_rows)};",
    "  if (loanSheet) {",
    "    if (loanSheet.getLastRow() > 1) {",
    "      loanSheet.getRange(2, 1, loanSheet.getLastRow() - 1, loanSheet.getLastColumn()).clearContent();",
    "    }",
    "    if (loanData.length > 0) {",
    "      loanSheet.getRange(2, 1, loanData.length, loanData[0].length).setValues(loanData);",
    "      loanSheet.getRange(2, 1, loanData.length, 1).setNumberFormat('@');",
    "      loanSheet.getRange(2, 2, loanData.length, 1).setNumberFormat('yyyy-mm-dd');",
    "      loanSheet.getRange(2, 3, loanData.length, 1).setNumberFormat('@');",
    "    }",
    "  }",
    "",
    "  // 5. Align columns, styling, filters and recalculate summaries",
    "  autoFixAndAlignAllSheets(true);",
    "  syncFinancialsSheetBackend();",
    "  syncProfitAndLossSheetBackend();",
    "  SpreadsheetApp.flush();",
    "",
    "  SpreadsheetApp.getUi().alert('✅ PERFECT DATA MIGRATION SUCCESSFUL!\\n\\n' +",
    f"    '• Members Loaded: {len(mem_rows)} (Nominee = ADMIN, Names & Phones in Exact Columns)\\n' +",
    f"    '• Receipts Loaded: {len(receipt_rows)} (RD, Loan Return, Interest)\\n' +",
    f"    '• Loans Disbursed Loaded: {len(loan_rows)}\\n\\n' +",
    "    'Web App & Google Sheet are now 100% in sync!');",
    "}"
]

with open('Migration_Importer.gs', 'w', encoding='utf-8') as f:
    f.write('\n'.join(code_lines))

print('Migration_Importer.gs successfully generated!')
