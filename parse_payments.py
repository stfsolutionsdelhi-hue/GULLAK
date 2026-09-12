import json

# Payment Sheet (Image 3) - Loan Disbursements
# Headers: S.No | Date | Ledger No | Particular | Cash/Card | Mobile | Paid Amount | Narration | BONUS | RD PAYMENT | LOAN DISBURSHMENT
# As instructed: skip rows 6-59 (blank/inactive), and skip row 6 & 13 (Sanish Kumar 072022 Admin)

payment_disbursements = [
    {"sno": 1, "date": "2026-01-31", "ledgerNo": 25, "name": "Kuwar Pal -1 X2 082022", "mode": "CASH", "mobile": "9871130935", "amount": 30000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 2, "date": "2026-01-31", "ledgerNo": 26, "name": "Kuwar Pal-2 X2 082022", "mode": "CASH", "mobile": "9871130935", "amount": 30000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 3, "date": "2026-03-18", "ledgerNo": 29, "name": "Narayan Yadav X2 032022", "mode": "CASH", "mobile": "9599959948", "amount": 10000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 4, "date": "2026-03-18", "ledgerNo": 16, "name": "Jagdish Mehto X2  022022", "mode": "CASH", "mobile": "7042511481", "amount": 40000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 5, "date": "2026-03-19", "ledgerNo": 8, "name": "Chanda Devi Ref Shila Devi 022024", "mode": "CASH", "mobile": "8447218816", "amount": 30000, "rate": 1.0, "narration": "Loan Disbursed"},
    # 6 31-Dec-2025 Sanish Kumar 072022 is SKIPPED
    {"sno": 7, "date": "2026-07-18", "ledgerNo": 1, "name": "Afsana Sister Pappu Ji 012025", "mode": "CASH", "mobile": "9773841314", "amount": 10000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 8, "date": "2026-06-18", "ledgerNo": 32, "name": "Neeraj Renew So Raghuveer Ji 012025", "mode": "CASH", "mobile": "9891811697", "amount": 40000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 9, "date": "2026-07-18", "ledgerNo": 43, "name": "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023", "mode": "CASH", "mobile": "9650862110", "amount": 40000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 10, "date": "2026-07-18", "ledgerNo": 40, "name": "Ram Bharose Ji Goyla Dairy 022022", "mode": "CASH", "mobile": "9717961768", "amount": 40000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 11, "date": "2026-04-18", "ledgerNo": 13, "name": "Hari Ram Ji Vikas Vihar 032022", "mode": "CASH", "mobile": "9650013268", "amount": 5000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 12, "date": "2026-07-15", "ledgerNo": 22, "name": "Kazim So Mumina Khatoon Ref Pappu 012025", "mode": "CASH", "mobile": "8287493771", "amount": 10000, "rate": 1.0, "narration": "Loan Disbursed"},
    # 13 18-Apr-2026 Sanish Kumar 072022 is SKIPPED
    {"sno": 14, "date": "2026-05-18", "ledgerNo": 46, "name": "Sanjay Yadav -2- Shubhankar 072023", "mode": "CASH", "mobile": "7827004101", "amount": 40000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 15, "date": "2026-04-18", "ledgerNo": 53, "name": "Sushil Ji So Sheela Devi 012025", "mode": "CASH", "mobile": "7042480937", "amount": 30000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 16, "date": "2026-04-18", "ledgerNo": 24, "name": "Kiran Devi Wo Sushil Kumar 202501", "mode": "CASH", "mobile": "7042480937", "amount": 20000, "rate": 1.0, "narration": "Loan Disbursed"},
    {"sno": 17, "date": "2026-05-31", "ledgerNo": 53, "name": "Sushil Ji So Sheela Devi 012025", "mode": "CASH", "mobile": "7042480937", "amount": 300, "rate": 0, "narration": "Panulty"},
    {"sno": 18, "date": "2026-05-31", "ledgerNo": 24, "name": "Kiran Devi Wo Sushil Kumar 202501", "mode": "CASH", "mobile": "7042480937", "amount": 300, "rate": 0, "narration": "Panulty"}
]

print(f"Loaded {len(payment_disbursements)} payment entries (excluding Sanish Admin and blank rows).")
with open('payments_parsed.json', 'w') as f:
    json.dump(payment_disbursements, f, indent=2)
