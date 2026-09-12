# Assemble all parts into Code.gs and Version 43 Zip archives
import os
import zipfile
import json
import base64

with open("Part1_Server.gs", "r", encoding="utf-8") as f:
    p1 = f.read()

with open("Part2_Html.gs", "r", encoding="utf-8") as f:
    p2 = f.read()

with open("Part3A.gs", "r", encoding="utf-8") as f:
    p3a = f.read()

with open("Part3B.gs", "r", encoding="utf-8") as f:
    p3b = f.read()

# Build monolithic Code.gs and Gullak_Master_V48_PRO_Clean.txt / V48 Clean
code_gs = p1 + "\n\n" + p2 + "\n\n" + p3a + "\n\n" + p3b + "\n"

with open("Code.gs", "w", encoding="utf-8") as f:
    f.write(code_gs)

with open("Gullak_Master_V48_PRO.gs", "w", encoding="utf-8") as f:
    f.write(code_gs)

with open("Gullak_Master_V48_PRO_Clean.txt", "w", encoding="utf-8") as f:
    f.write(code_gs)

with open("Gullak_Master_V48_Clean.txt", "w", encoding="utf-8") as f:
    f.write(code_gs)

with open("Gullak_Master_V38_Clean.txt", "w", encoding="utf-8") as f:
    f.write(code_gs)

print(f"Code.gs and Gullak_Master_V48_PRO_Clean.txt created successfully. Total bytes: {len(code_gs)}")

# Build Gullak_Master_V48_PRO.zip and aliases
with zipfile.ZipFile("Gullak_Master_V48_PRO.zip", "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Gullak_Master_V48_PRO_Clean.txt", arcname="Gullak_Master_V48_PRO_Clean.txt")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")
print("Gullak_Master_V48_PRO.zip created successfully.")

# Build Code_gs_Only_V48_PRO.zip
zip_code_only_v48_pro = "Code_gs_Only_V48_PRO.zip"
with zipfile.ZipFile(zip_code_only_v48_pro, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Gullak_Master_V48_PRO_Clean.txt", arcname="Gullak_Master_V48_PRO_Clean.txt")
print(f"{zip_code_only_v48_pro} created successfully.")

# Build Gullak_Master_V38_Clean.zip and V48 Clean.zip
with zipfile.ZipFile("Gullak_Master_V38_Clean.zip", "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Gullak_Master_V38_Clean.txt", arcname="Gullak_Master_V38_Clean.txt")

with zipfile.ZipFile("Gullak_Master_V48_Clean.zip", "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Gullak_Master_V48_Clean.txt", arcname="Gullak_Master_V48_Clean.txt")

# Build V48 Single Code.gs ONLY zip file
zip_code_only_v48 = "Code_gs_Only_V48.zip"
with zipfile.ZipFile(zip_code_only_v48, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
print(f"{zip_code_only_v48} created successfully. File size: {os.path.getsize(zip_code_only_v48)} bytes.")

# Build V48 Master Package zip file
zip_v48 = "Gullak_V48_Master_Code.zip"
with zipfile.ZipFile(zip_v48, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")
print(f"{zip_v48} created successfully. File size: {os.path.getsize(zip_v48)} bytes.")

# Build V47 Single Code.gs ONLY zip file
zip_code_only_v47 = "Code_gs_Only_V47.zip"
with zipfile.ZipFile(zip_code_only_v47, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
print(f"{zip_code_only_v47} created successfully. File size: {os.path.getsize(zip_code_only_v47)} bytes.")

# Build V47 Master Package zip file
zip_v47 = "Gullak_V47_Master_Code.zip"
with zipfile.ZipFile(zip_v47, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")
print(f"{zip_v47} created successfully. File size: {os.path.getsize(zip_v47)} bytes.")

# Build V46 Single Code.gs ONLY zip file
zip_code_only_v46 = "Code_gs_Only_V46.zip"
with zipfile.ZipFile(zip_code_only_v46, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
print(f"{zip_code_only_v46} created successfully. File size: {os.path.getsize(zip_code_only_v46)} bytes.")

# Build V46 Master Package zip file
zip_v46 = "Gullak_V46_Master_Code.zip"
with zipfile.ZipFile(zip_v46, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")
print(f"{zip_v46} created successfully. File size: {os.path.getsize(zip_v46)} bytes.")

# Maintain V45, V44, V43, and V42 zip archives
zip_code_only_v45 = "Code_gs_Only_V45.zip"
with zipfile.ZipFile(zip_code_only_v45, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")

zip_v45 = "Gullak_V45_Master_Code.zip"
with zipfile.ZipFile(zip_v45, "w", zipfile.ZIP_DEFLATED) as zipf:
    zipf.write("Code.gs", arcname="Code.gs")
    zipf.write("Part1_Server.gs", arcname="Part1_Server.gs")
    zipf.write("Part2_Html.gs", arcname="Part2_Html.gs")
    zipf.write("Part3A.gs", arcname="Part3A.gs")
    zipf.write("Part3B.gs", arcname="Part3B.gs")

# Generate files_data.js automatically on assemble
with open("Code_gs_Only_V48_PRO.zip", "rb") as fz:
    b64_zip48_pro = base64.b64encode(fz.read()).decode("utf-8")
with open("Gullak_Master_V48_PRO.zip", "rb") as fz:
    b64_zip_master48_pro = base64.b64encode(fz.read()).decode("utf-8")

with open("Code_gs_Only_V48.zip", "rb") as fz:
    b64_zip48 = base64.b64encode(fz.read()).decode("utf-8")
with open("Gullak_V48_Master_Code.zip", "rb") as fz:
    b64_zip_master48 = base64.b64encode(fz.read()).decode("utf-8")

with open("Code_gs_Only_V47.zip", "rb") as fz:
    b64_zip47 = base64.b64encode(fz.read()).decode("utf-8")
with open("Gullak_V47_Master_Code.zip", "rb") as fz:
    b64_zip_master47 = base64.b64encode(fz.read()).decode("utf-8")

with open("Code_gs_Only_V46.zip", "rb") as fz:
    b64_zip46 = base64.b64encode(fz.read()).decode("utf-8")
with open("Gullak_V46_Master_Code.zip", "rb") as fz:
    b64_zip_master46 = base64.b64encode(fz.read()).decode("utf-8")

with open("files_data.js", "w", encoding="utf-8") as fj:
    bundle = {
        "Code.gs": code_gs,
        "Gullak_Master_V48_PRO.gs": code_gs,
        "Gullak_Master_V48_PRO_Clean.txt": code_gs,
        "Gullak_Master_V48_Clean.txt": code_gs,
        "Gullak_Master_V38_Clean.txt": code_gs,
        "Part1_Server.gs": p1,
        "Part2_Html.gs": p2,
        "Part3A.gs": p3a,
        "Part3B.gs": p3b
    }
    fj.write("window.GULLAK_FILES = " + json.dumps(bundle) + ";\n")
    fj.write("window.GULLAK_ZIP48_PRO_B64 = \"" + b64_zip48_pro + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_48_PRO_B64 = \"" + b64_zip_master48_pro + "\";\n")
    fj.write("window.GULLAK_ZIP48_B64 = \"" + b64_zip48 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_48_B64 = \"" + b64_zip_master48 + "\";\n")
    fj.write("window.GULLAK_ZIP47_B64 = \"" + b64_zip47 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_47_B64 = \"" + b64_zip_master47 + "\";\n")
    fj.write("window.GULLAK_ZIP46_B64 = \"" + b64_zip46 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_46_B64 = \"" + b64_zip_master46 + "\";\n")
    fj.write("window.GULLAK_ZIP45_B64 = \"" + b64_zip46 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_45_B64 = \"" + b64_zip_master46 + "\";\n")
    fj.write("window.GULLAK_ZIP44_B64 = \"" + b64_zip46 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_44_B64 = \"" + b64_zip_master46 + "\";\n")
    fj.write("window.GULLAK_ZIP43_B64 = \"" + b64_zip46 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_43_B64 = \"" + b64_zip_master46 + "\";\n")
    fj.write("window.GULLAK_ZIP42_B64 = \"" + b64_zip46 + "\";\n")
    fj.write("window.GULLAK_ZIP_MASTER_42_B64 = \"" + b64_zip_master46 + "\";\n")

print("files_data.js built successfully with V48 PRO!")
