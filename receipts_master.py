import json

# Let's create the master receipt list extracted precisely row by row from Receipt Sheet (Image 2)
# Columns: sno, date, name, mode, mobile, total, narration, rd, loanEmi, interest
# Notice Sanish Kumar (S.No 44/45/99/100/155/199/234/257/276/384/448) is skipped or filtered as per Instruction 4.

# Let's structure the raw table entries from image 2:
# We'll map member name -> member ID from ledger (MEM010120261 ... MEM0101202668)
