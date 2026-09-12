import json

with open('members_final.json') as f:
    members = json.load(f)

with open('payments_parsed.json') as f:
    payments = json.load(f)

with open('receipts_parsed.json') as f:
    receipts = json.load(f)

print(f"Summary:")
print(f"Members: {len(members)}")
print(f"Payments (Disbursed Loans): {len(payments)}")
print(f"Receipts: {len(receipts)}")

# Total RD collected
total_rd = sum(r[8] for r in receipts)
# Total Loan returned
total_loan_ret = sum(r[9] for r in receipts)
# Total Interest collected
total_interest = sum(r[10] for r in receipts)
# Total Disbursed Loan
total_disbursed = sum(p['loanAmount'] for p in payments)

print(f"Total RD Collected: ₹{total_rd:,.2f}")
print(f"Total Loan Returned: ₹{total_loan_ret:,.2f}")
print(f"Total Interest Collected: ₹{total_interest:,.2f}")
print(f"Total Loan Disbursed: ₹{total_disbursed:,.2f}")
