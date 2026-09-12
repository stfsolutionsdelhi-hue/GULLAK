import re

def extract_template(gs_text, func_name):
    prefix = "function " + func_name + "()"
    idx = gs_text.find(prefix)
    if idx == -1:
        raise ValueError("Could not find " + func_name)
    ret_idx = gs_text.find("return `", idx)
    if ret_idx == -1:
        raise ValueError("Could not find return ` in " + func_name)
    start_pos = ret_idx + len("return `")
    end_pos = gs_text.rfind("`;")
    if end_pos == -1:
        end_pos = gs_text.rfind("`")
    return gs_text[start_pos:end_pos]

with open('Part2_Html.gs', 'r', encoding='utf-8') as f:
    p2 = f.read()
with open('Part3A.gs', 'r', encoding='utf-8') as f:
    p3a = f.read()
with open('Part3B.gs', 'r', encoding='utf-8') as f:
    p3b = f.read()

html_content = extract_template(p2, 'getCompleteSoftwareHtmlContent')
script_a = extract_template(p3a, 'getClientScriptPartA')
script_b = extract_template(p3b, 'getClientScriptPartB')

full_page = html_content + script_a + script_b

print("Full page length:", len(full_page))
print("Has windowsLoginOverlay:", "windowsLoginOverlay" in full_page)
print("Has tabHead1:", "tabHead1" in full_page)
print("Has inpGoogleWebAppUrl:", "inpGoogleWebAppUrl" in full_page)
print("Has cloudHub:", "cloudHub" in full_page)

with open('index.html', 'w', encoding='utf-8') as f:
    f.write(full_page)

print("index.html written successfully! Total bytes:", len(full_page))
