import json

# Parsed from Payments image (Disbursements / Penalties, skipping blank rows and Sanish Admin)
payments = [
    {"sno": 1, "date": "2026-01-31", "ledgerNo": 25, "particular": "Kuwar Pal -1 X2 082022", "mode": "CASH", "mobile": "9871130935", "paidAmount": 30000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 30000, "penalty": 0},
    {"sno": 2, "date": "2026-01-31", "ledgerNo": 26, "particular": "Kuwar Pal-2 X2 082022", "mode": "CASH", "mobile": "9871130935", "paidAmount": 30000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 30000, "penalty": 0},
    {"sno": 3, "date": "2026-03-18", "ledgerNo": 29, "particular": "Narayan Yadav X2 032022", "mode": "CASH", "mobile": "9599959948", "paidAmount": 10000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 10000, "penalty": 0},
    {"sno": 4, "date": "2026-03-18", "ledgerNo": 16, "particular": "Jagdish Mehto X2 022022", "mode": "CASH", "mobile": "7042511481", "paidAmount": 40000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 40000, "penalty": 0},
    {"sno": 5, "date": "2026-03-19", "ledgerNo": 8, "particular": "Chanda Devi Ref Shila Devi 022024", "mode": "CASH", "mobile": "8447218816", "paidAmount": 30000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 30000, "penalty": 0},
    {"sno": 7, "date": "2026-07-18", "ledgerNo": 1, "particular": "Afsana Sister Pappu Ji 012025", "mode": "CASH", "mobile": "9773841314", "paidAmount": 10000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 10000, "penalty": 0},
    {"sno": 8, "date": "2026-06-18", "ledgerNo": 32, "particular": "Neeraj Renew So Raghuveer Ji 012025", "mode": "CASH", "mobile": "9891811697", "paidAmount": 40000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 40000, "penalty": 0},
    {"sno": 9, "date": "2026-07-18", "ledgerNo": 43, "particular": "Sanjay Kumar -2- Sandeep Kr Ref DeeplaI 022023", "mode": "CASH", "mobile": "9650862110", "paidAmount": 40000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 40000, "penalty": 0},
    {"sno": 10, "date": "2026-07-18", "ledgerNo": 40, "particular": "Ram Bharose Ji Goyla Dairy 022022", "mode": "CASH", "mobile": "9717961768", "paidAmount": 40000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 40000, "penalty": 0},
    {"sno": 11, "date": "2026-04-18", "ledgerNo": 13, "particular": "Hari Ram Ji Vikas Vihar 032022", "mode": "CASH", "mobile": "9650013268", "paidAmount": 5000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 5000, "penalty": 0},
    {"sno": 12, "date": "2026-07-15", "ledgerNo": 22, "particular": "Kazim So Mumina Khatoon Ref Pappu 012025", "mode": "CASH", "mobile": "8287493771", "paidAmount": 10000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 10000, "penalty": 0},
    {"sno": 14, "date": "2026-05-18", "ledgerNo": 46, "particular": "Sanjay Yadav -2- Shubhankar 072023", "mode": "CASH", "mobile": "7827004101", "paidAmount": 40000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 40000, "penalty": 0},
    {"sno": 15, "date": "2026-04-18", "ledgerNo": 53, "particular": "Sushil Ji So Sheela Devi 012025", "mode": "CASH", "mobile": "7042480937", "paidAmount": 30000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 30000, "penalty": 0},
    {"sno": 16, "date": "2026-04-18", "ledgerNo": 24, "particular": "Kiran Devi Wo Sushil Kumar 202501", "mode": "CASH", "mobile": "7042480937", "paidAmount": 20000, "narration": "", "bonus": 0, "rdPayment": 0, "loanAmount": 20000, "penalty": 0},
    {"sno": 17, "date": "2026-05-31", "ledgerNo": 53, "particular": "Sushil Ji So Sheela Devi 012025", "mode": "CASH", "mobile": "7042480937", "paidAmount": 300, "narration": "Panulty", "bonus": 0, "rdPayment": 0, "loanAmount": 0, "penalty": 300},
    {"sno": 18, "date": "2026-05-31", "ledgerNo": 24, "particular": "Kiran Devi Wo Sushil Kumar 202501", "mode": "CASH", "mobile": "7042480937", "paidAmount": 300, "narration": "Panulty", "bonus": 0, "rdPayment": 0, "loanAmount": 0, "penalty": 300}
]

with open('payments_parsed.json', 'w') as f:
    json.dump(payments, f, indent=2)

print(f"Total payments saved: {len(payments)}")
