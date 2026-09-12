import re
import json

# We can parse the receipts from text / structured data
# Let's inspect the receipts image:
# Dates: 31-Jan-2026, 28-Feb-2026, 31-Mar-2026, 15-Apr-2026, 30-Apr-2026, 15-May-2026, 31-May-2026, 15-Jun-2026, 24-Jun-2026, 15-Jul-2026, 21-Jul-2026, 31-Jul-2026, 15-Aug-2026.
# Mode: CASH (all)
# Columns: S.No | Date | Particular | Cash/Card | Mobile | Total Paid | Narration | RD | Loan EMI (Repayment) | Loan Interest

print("Building receipt extractor...")
