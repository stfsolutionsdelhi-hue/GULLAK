import json
import re

# Load parsed ledger
with open('ledger_parsed.json', 'r') as f:
    ledger_members = json.load(f)

# Load parsed payments (loans)
with open('payments_parsed.json', 'r') as f:
    payment_loans = json.load(f)

print(f"Ledger members: {len(ledger_members)}")
print(f"Payment loans: {len(payment_loans)}")
