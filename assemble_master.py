# Assemble all parts into Code.gs and Gullak_V34_Master_Code.zip
import os
import zipfile

with open("Part1_Server.gs", "r", encoding="utf-8") as f:
    p1 = f.read()

with open("Part2_Html.gs", "r", encoding="utf-8") as f:
    p2 = f.read()

with open("Part3A.gs", "r", encoding="utf-8") as f:
    p3a = f.read()

with open("Part3B.gs", "r", encoding="utf-8") as f:
    p3b = f.read()

# Build monolithic Code.gs
code_gs = p1 + "\n\n" + p2 + "\n\n" + p3a + "\n\n" + p3b + "\n"

with open("Code.gs", "w", encoding="utf-8") as f:
    f.write(code_gs)

print(f"Code.gs created successfully. Total bytes: {len(code_gs)}")

# Build V36 master zip file
zip_v36 = "Gullak_V36_Master_Code.zip"
with zipfile.ZipFile(zip_v36, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v36} created successfully. File size: {os.path.getsize(zip_v36)} bytes.")

# Build V35 master zip file
zip_v35 = "Gullak_V35_Master_Code.zip"
with zipfile.ZipFile(zip_v35, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v35} created successfully. File size: {os.path.getsize(zip_v35)} bytes.")

# Build V34 master zip file
zip_v34 = "Gullak_V34_Master_Code.zip"
with zipfile.ZipFile(zip_v34, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v34} created successfully. File size: {os.path.getsize(zip_v34)} bytes.")

# Build V33 master zip file
zip_v33 = "Gullak_V33_Master_Code.zip"
with zipfile.ZipFile(zip_v33, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v33} created successfully. File size: {os.path.getsize(zip_v33)} bytes.")

# Build V32 master zip file
zip_v32 = "Gullak_V32_Master_Code.zip"
with zipfile.ZipFile(zip_v32, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v32} created successfully. File size: {os.path.getsize(zip_v32)} bytes.")

# Build V22 zip file
zip_v22 = "Gullak_V22_Master_Code.zip"
with zipfile.ZipFile(zip_v22, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v22} created successfully. File size: {os.path.getsize(zip_v22)} bytes.")

# Also build V21 zip file for backward compatibility
zip_v21 = "Gullak_V21_Master_Code.zip"
with zipfile.ZipFile(zip_v21, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

print(f"{zip_v21} created successfully. File size: {os.path.getsize(zip_v21)} bytes.")

