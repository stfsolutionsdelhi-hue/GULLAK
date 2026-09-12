import json

with open('members_final.json') as f:
    members = json.load(f)

with open('payments_parsed.json') as f:
    payments = json.load(f)

with open('receipts_parsed.json') as f:
    receipts = json.load(f)

# Clean member ledger structure for modern system
all_members = []
for m in members:
    all_members.append({
        "id": m['id'],
        "ledgerNo": m['ledgerNo'],
        "name": m['name'],
        "mobile": m['mobile'],
        "address": m['address'],
        "status": m['status'],
        "openingBalance": m['opRd'],
        "currentBalance": m['opRd'],
        "openingLoan": m['opLoan'],
        "activeLoan": m['opLoan'],
        "monthlyContribution": m.get('rd', 400),
        "nominee": m.get('nominee', '')
    })

# Member lookup by ledgerNo
member_by_ledger = {m['ledgerNo']: m for m in all_members}

# Format all transactions into unified system ledger
transactions = []

# Process initial loan disbursements from payments
for p in payments:
    m_info = member_by_ledger.get(p['ledgerNo'], {})
    m_id = m_info.get('id', f"MEM{int(p['ledgerNo']):03d}")
    if p['loanAmount'] > 0:
        transactions.append({
            "id": f"TX-DISB-{p['sno']}",
            "date": p['date'],
            "type": "LOAN_DISBURSEMENT",
            "ledgerNo": p['ledgerNo'],
            "memberId": m_id,
            "memberName": p['particular'],
            "mobile": p['mobile'],
            "amount": p['loanAmount'],
            "rdAmount": 0,
            "loanPrincipal": p['loanAmount'],
            "interest": 0,
            "penalty": 0,
            "paymentMode": p['mode'],
            "narration": p.get('narration', '') or 'Loan Disbursed'
        })
    elif p['penalty'] > 0:
        transactions.append({
            "id": f"TX-PEN-{p['sno']}",
            "date": p['date'],
            "type": "PENALTY",
            "ledgerNo": p['ledgerNo'],
            "memberId": m_id,
            "memberName": p['particular'],
            "mobile": p['mobile'],
            "amount": p['paidAmount'],
            "rdAmount": 0,
            "loanPrincipal": 0,
            "interest": 0,
            "penalty": p['penalty'],
            "paymentMode": p['mode'],
            "narration": p.get('narration', '') or 'Penalty'
        })

# Process receipts
for r in receipts:
    sno, date, ledgerNo, particular, mode, mobile, total, narration, rd, loanReturn, interest = r
    m_info = member_by_ledger.get(ledgerNo, {})
    m_id = m_info.get('id', f"MEM{int(ledgerNo):03d}")
    transactions.append({
        "id": f"TX-REC-{sno}",
        "date": date,
        "type": "COLLECTION",
        "ledgerNo": ledgerNo,
        "memberId": m_id,
        "memberName": particular,
        "mobile": mobile,
        "amount": total,
        "rdAmount": rd,
        "loanPrincipal": loanReturn,
        "interest": interest,
        "penalty": 0,
        "paymentMode": mode,
        "narration": narration
    })

# Sort all transactions chronologically
transactions.sort(key=lambda x: x['date'])

# Recalculate member balances and active loans
mem_dict = {m['id']: m for m in all_members}
for t in transactions:
    m_id = t['memberId']
    if m_id in mem_dict:
        m = mem_dict[m_id]
        if t['type'] == 'COLLECTION':
            m['currentBalance'] += t['rdAmount']
            m['activeLoan'] = max(0, m['activeLoan'] - t['loanPrincipal'])
        elif t['type'] == 'LOAN_DISBURSEMENT':
            m['activeLoan'] += t['loanPrincipal']

print(f"Generated complete migration dataset:")
print(f"Total Members: {len(all_members)}")
print(f"Total Transactions: {len(transactions)}")
print(f"Total RD in Circulation: ₹{sum(m['currentBalance'] for m in all_members):,.2f}")
print(f"Total Active Loan Balance: ₹{sum(m['activeLoan'] for m in all_members):,.2f}")

with open('full_society_database.json', 'w') as f:
    json.dump({
        "members": all_members,
        "transactions": transactions,
        "summary": {
            "totalMembers": len(all_members),
            "totalTransactions": len(transactions),
            "totalRDBalance": sum(m['currentBalance'] for m in all_members),
            "totalOutstandingLoan": sum(m['activeLoan'] for m in all_members)
        }
    }, f, indent=2)

print("Saved full_society_database.json successfully.")
