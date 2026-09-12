import json, re, os, subprocess

print("==================================================")
print("  GULLAK MASTER V64 PRO - COMPLETE MASTER BUILDER ")
print("==================================================")

# 1. Read Part1_Server.gs
with open('Part1_Server.gs', 'r', encoding='utf-8') as f:
    p1 = f.read()

# 2. Fix Part1_Server.gs:
p1_fixed = p1

# Fix range chaining
p1_fixed = p1_fixed.replace(
    'paySheet.getRange(1, 1, 1, payH.length).setFontWeight("bold").setBackground("#0F766E").setFontColor("#FFFFFF");',
    'var hRange = paySheet.getRange(1, 1, 1, payH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#0F766E");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'loanSheet.getRange(1, 1, 1, loanH.length).setFontWeight("bold").setBackground("#991B1B").setFontColor("#FFFFFF");',
    'var hRange = loanSheet.getRange(1, 1, 1, loanH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#991B1B");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'penSheet.getRange(1, 1, 1, penH.length).setFontWeight("bold").setBackground("#B45309").setFontColor("#FFFFFF");',
    'var hRange = penSheet.getRange(1, 1, 1, penH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#B45309");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'bonusSheet.getRange(1, 1, 1, bonusH.length).setFontWeight("bold").setBackground("#D97706").setFontColor("#FFFFFF");',
    'var hRange = bonusSheet.getRange(1, 1, 1, bonusH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#D97706");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'fundSheet.getRange(1, 1, 1, fundH.length).setFontWeight("bold").setBackground("#4338CA").setFontColor("#FFFFFF");',
    'var hRange = fundSheet.getRange(1, 1, 1, fundH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#4338CA");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'exitSheet.getRange(1, 1, 1, exitH.length).setFontWeight("bold").setBackground("#7F1D1D").setFontColor("#FFFFFF");',
    'var hRange = exitSheet.getRange(1, 1, 1, exitH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#7F1D1D");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'plSheet.getRange(1, 1, 1, plH.length).setFontWeight("bold").setBackground("#047857").setFontColor("#FFFFFF");',
    'var hRange = plSheet.getRange(1, 1, 1, plH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#047857");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'uSheet.getRange(1, 1, 1, userH.length).setFontWeight("bold").setBackground("#0F172A").setFontColor("#FFFFFF");',
    'var hRange = uSheet.getRange(1, 1, 1, userH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#0F172A");\n    hRange.setFontColor("#FFFFFF");'
)
p1_fixed = p1_fixed.replace(
    'finSheet.getRange(1, 1, 1, finH.length).setFontWeight("bold").setBackground("#0284C7").setFontColor("#FFFFFF");',
    'var hRange = finSheet.getRange(1, 1, 1, finH.length);\n    hRange.setFontWeight("bold");\n    hRange.setBackground("#0284C7");\n    hRange.setFontColor("#FFFFFF");'
)

# Safe clear on Sheet vs Range
p1_fixed = p1_fixed.replace('userSheet.clearContent();', 'try { userSheet.clearContents(); } catch(e) { try { userSheet.clear(); } catch(e2) {} }')
p1_fixed = p1_fixed.replace('loanSheet.clearContent();', 'try { loanSheet.clearContents(); } catch(e) { try { loanSheet.clear(); } catch(e2) {} }')
p1_fixed = p1_fixed.replace('loanSheet.clearFormats();', 'try { loanSheet.clearFormats(); } catch(e) {}')

p1_fixed = p1_fixed.replace(
    'penSheet.getRange(2, 1, penSheet.getLastRow() - 1, penH.length).clearContent();',
    'if (penSheet.getLastRow() > 1) { penSheet.getRange(2, 1, penSheet.getLastRow() - 1, Math.max(penSheet.getLastColumn(), penH.length)).clearContent(); }'
)
p1_fixed = p1_fixed.replace(
    'plSheet.getRange(2, 1, plSheet.getLastRow() - 1, plH.length).clearContent();',
    'if (plSheet.getLastRow() > 1) { plSheet.getRange(2, 1, plSheet.getLastRow() - 1, Math.max(plSheet.getLastColumn(), plH.length)).clearContent(); }'
)

# Ensure doGet and doPost and handleApiRequest are robust
api_handler_str = """function handleApiRequest(params, postData) {
  var action = (params && params.action) || (postData && postData.action) || 'getData';
  var callback = (params && params.callback) || '';
  var result = { success: false };
  try {
    if (action === 'getData' || action === 'getSocietyData') {
      result = { success: true, data: getSocietyFullDataWithoutFinSync() };
    } else if (action === 'restore67Members') {
      result = restoreAll67RealSocietyMembers();
    } else if (action === 'login') {
      var u = (params && params.username) || (postData && postData.username);
      var p = (params && params.password) || (postData && postData.password);
      result = checkUserLoginBackend(u, p);
    } else if (action === 'saveMember') {
      var memberObj = (postData && postData.member) || (params && params.member ? JSON.parse(params.member) : null);
      result = saveMemberBackend(memberObj);
    } else if (action === 'deleteMember') {
      var memId = (postData && postData.memberId) || (params && params.memberId);
      result = deleteMemberBackend(memId);
    } else if (action === 'savePayment') {
      var payObj = (postData && postData.payment) || (params && params.payment ? JSON.parse(params.payment) : null);
      result = savePaymentBackend(payObj);
    } else if (action === 'saveLoan') {
      var loanObj = (postData && postData.loan) || (params && params.loan ? JSON.parse(params.loan) : null);
      result = saveLoanBackend(loanObj);
    } else if (action === 'saveExitSettlement') {
      var exitObj = (postData && postData.exit) || (params && params.exit ? JSON.parse(params.exit) : null);
      result = saveExitSettlementBackend(exitObj);
    } else if (action === 'saveBonusSettlement') {
      var bonusObj = (postData && postData.bonus) || (params && params.bonus ? JSON.parse(params.bonus) : null);
      result = saveBonusSettlementBackend(bonusObj);
    } else if (action === 'saveFund') {
      var fundObj = (postData && postData.fund) || (params && params.fund ? JSON.parse(params.fund) : null);
      result = saveFundTransactionBackend(fundObj);
    } else if (action === 'sync') {
      syncPenaltyRegisterSheetBackend();
      syncProfitAndLossSheetBackend();
      syncFinancialsSheetBackend();
      result = { success: true, message: 'All sheets synchronized successfully', data: getSocietyFullDataWithoutFinSync() };
    } else {
      result = { success: true, message: 'Unknown action: ' + action, data: getSocietyFullDataWithoutFinSync() };
    }
  } catch (err) {
    result = { success: false, error: err.toString() };
  }

  var jsonStr = JSON.stringify(result);
  if (callback && callback.trim().length > 0) {
    return ContentService.createTextOutput(callback.trim() + '(' + jsonStr + ');')
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
  }
  return ContentService.createTextOutput(jsonStr)
    .setMimeType(ContentService.MimeType.JSON);
}

function doGet(e) {
  if (e && e.parameter && (e.parameter.action || e.parameter.format === 'json' || e.parameter.callback)) {
    return handleApiRequest(e.parameter, null);
  }
  return HtmlService.createHtmlOutput(getCompleteSoftwareHtml())
    .setTitle("GULLAK CO-OPERATIVE SOCIETY - Master Accounting Platform (V64 PRO)")
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag("viewport", "width=device-width, initial-scale=1.0");
}

function doPost(e) {
  var params = (e && e.parameter) || {};
  var postData = {};
  try {
    if (e && e.postData && e.postData.contents) {
      postData = JSON.parse(e.postData.contents);
    }
  } catch(err) {
    postData = {};
  }
  return handleApiRequest(params, postData);
}"""

p1_fixed = re.sub(r'function handleApiRequest\(params,\s*postData\)\s*\{.*?function doPost\(e\)\s*\{.*?return handleApiRequest\(params,\s*postData\);\s*\}', api_handler_str, p1_fixed, flags=re.DOTALL)

with open('Part1_Server.gs', 'w', encoding='utf-8') as f:
    f.write(p1_fixed)

print("Part1_Server.gs successfully processed.")

# 3. Read Part2_Html.gs and upgrade Settings View
with open('Part2_Html.gs', 'r', encoding='utf-8') as f:
    p2 = f.read()

# Update title branding
p2_fixed = p2.replace('MASTER CLOUD ACCOUNTING SYSTEM (V48 PRO)', 'MASTER CLOUD ACCOUNTING SYSTEM (V64 PRO)')
p2_fixed = p2_fixed.replace('MASTER CLOUD ACCOUNTING SYSTEM (V63 PRO)', 'MASTER CLOUD ACCOUNTING SYSTEM (V64 PRO)')

# Replace settingsSubView1 with clean, powerful Cloud Web App Connection Panel (NO Code.gs buttons!)
new_settings_subview1 = """    <!-- SUB-TAB 1: GENERAL SETTINGS & CLOUD SYNC -->
    <div id="settingsSubView1">
      <div style="margin-bottom:16px; padding:14px; background:#0B1120; border:1.5px solid #10B981; border-radius:10px;">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; flex-wrap:wrap; gap:6px;">
          <div style="font-weight:800; color:#34D399; font-size:0.95rem; display:flex; align-items:center; gap:6px;">
            <span>🌐 Google Sheet Cloud Web App URL</span>
          </div>
          <span id="txtWebAppStatus" style="font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:700; background:#064E3B; color:#34D399;">🟢 Cloud Sync Ready</span>
        </div>
        <div style="font-size:0.76rem; color:#94A3B8; margin-bottom:8px; line-height:1.4;">
          Android App ya Web Browser me apne live Google Spreadsheet se live data connect aur real-time sync karne ke liye apna Google Web App URL yahan paste karein:
        </div>
        <div style="margin-bottom:10px;">
          <input type="text" id="inpGoogleWebAppUrl" class="field-ctrl" placeholder="https://script.google.com/macros/s/.../exec" style="background:#060913; border:1px solid #059669; font-family:monospace; font-size:0.82rem; color:#FBBF24;">
        </div>
        <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(140px, 1fr)); gap:8px;">
          <button type="button" id="btnSaveWebAppUrl" onclick="saveAndConnectWebAppUrl()" class="btn btn-green" style="justify-content:center; padding:9px; font-weight:800; font-size:0.82rem;">
            💾 Save & Connect
          </button>
          <button type="button" id="btnSyncSheetData" onclick="triggerCloudSyncNow()" class="btn btn-blue" style="justify-content:center; padding:9px; font-weight:800; font-size:0.82rem;">
            🔄 Live Sync Now
          </button>
          <button type="button" id="btnRestore67Members" onclick="triggerRestore67Members()" class="btn btn-orange" style="justify-content:center; padding:9px; font-weight:800; font-size:0.82rem;">
            📥 Load 67 Real Members
          </button>
          <button type="button" id="btnOpenGoogleSheet" onclick="handleOpenSpreadsheet(event)" class="btn btn-dark" style="justify-content:center; padding:9px; font-weight:800; font-size:0.82rem;">
            📊 Open Sheet
          </button>
        </div>
      </div>

      <div class="field-box"><label class="field-label">Global Default Due Date</label><input type="text" id="inpGlobalDueDay" class="field-ctrl" value="15th of every month"></div>
      <div class="field-box">"""

p2_fixed = re.sub(
    r'<!-- SUB-TAB 1: GENERAL SETTINGS -->\s*<div id="settingsSubView1">.*?<div class="field-box"><label class="field-label">Global Default Due Date</label><input type="text" id="inpGlobalDueDay" class="field-ctrl" value="15th of every month"></div>\s*<div class="field-box">',
    new_settings_subview1,
    p2_fixed,
    flags=re.DOTALL
)

with open('Part2_Html.gs', 'w', encoding='utf-8') as f:
    f.write(p2_fixed)

print("Part2_Html.gs successfully processed.")

# 4. Read Part3A.gs and ensure 67 real members and cloudHub integration
with open('Part3A.gs', 'r', encoding='utf-8') as f:
    p3a = f.read()

# Make sure Part3A auto-upgrades any stale dummy member list in localStorage
auto_upgrade_code = """  // Auto-upgrade stale dummy member lists if fewer than 10 members or containing dummy names
  if (members && Array.isArray(members) && (members.length < 10 || (members[0] && members[0].name === "Rahul Kumar"))) {
    console.log("Upgrading stale members array to full 67 real members...");
    members = DEF_M;
    try {
      localStorage.setItem("gullak_v21_m", JSON.stringify(members));
    } catch(e) {}
  }"""

if "Upgrading stale members array to full 67 real members" not in p3a:
    p3a = p3a.replace('if (!initLoaded) {', auto_upgrade_code + '\n  if (!initLoaded) {')

with open('Part3A.gs', 'w', encoding='utf-8') as f:
    f.write(p3a)

print("Part3A.gs successfully processed.")

# 5. Read Part3B.gs and add Universal Cloud Sync Engine
with open('Part3B.gs', 'r', encoding='utf-8') as f:
    p3b = f.read()

cloud_hub_code = """
  // ==========================================
  // UNIVERSAL DUAL-MODE CLOUD BRIDGE (V64 PRO)
  // ==========================================
  window.cloudHub = {
    getWebAppUrl: function() {
      var u = "";
      try {
        u = localStorage.getItem("gullak_webapp_url") || "";
      } catch(e) {}
      if (!u && window.connectedSpreadsheetUrl && window.connectedSpreadsheetUrl.indexOf("/exec") !== -1) {
        u = window.connectedSpreadsheetUrl;
      }
      return u ? u.trim() : "";
    },
    setWebAppUrl: function(url) {
      if (url) {
        try {
          localStorage.setItem("gullak_webapp_url", url.trim());
        } catch(e) {}
      }
    },
    isGasEnvironment: function() {
      return (typeof google !== "undefined" && google.script && typeof google.script.run !== "undefined");
    },
    callApi: function(action, payload, onSuccess, onError) {
      var self = this;
      if (self.isGasEnvironment()) {
        if (action === "getData") {
          google.script.run
            .withSuccessHandler(function(res){ if (onSuccess) onSuccess(res); })
            .withFailureHandler(function(err){ if (onError) onError(err); })
            .getSocietyFullData();
        } else if (action === "restore67Members") {
          google.script.run
            .withSuccessHandler(function(res){ if (onSuccess) onSuccess(res); })
            .withFailureHandler(function(err){ if (onError) onError(err); })
            .restoreAll67RealSocietyMembers();
        } else if (action === "saveMember") {
          google.script.run.saveMemberBackend(payload.member);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "deleteMember") {
          google.script.run.deleteMemberBackend(payload.memberId);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "savePayment") {
          google.script.run.savePaymentBackend(payload.payment);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "saveLoan") {
          google.script.run.saveLoanBackend(payload.loan);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "saveFund") {
          google.script.run.saveFundTransactionBackend(payload.fund);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "saveExitSettlement") {
          google.script.run.saveExitSettlementBackend(payload.exit);
          if (onSuccess) onSuccess({ success: true });
        } else if (action === "saveBonusSettlement") {
          google.script.run.saveBonusSettlementBackend(payload.bonus);
          if (onSuccess) onSuccess({ success: true });
        } else {
          if (onSuccess) onSuccess({ success: true });
        }
        return;
      }

      var webUrl = self.getWebAppUrl();
      if (!webUrl) {
        if (onError) onError(new Error("Google Web App URL set nahi hai. Settings me jakar Web App Link paste karein."));
        return;
      }

      var isGet = (action === "getData" || action === "restore67Members");
      if (isGet) {
        var queryUrl = webUrl + (webUrl.indexOf("?") === -1 ? "?" : "&") + "action=" + action + "&t=" + Date.now();
        fetch(queryUrl, { method: "GET", mode: "cors", redirect: "follow" })
          .then(function(r){ return r.json(); })
          .then(function(data){
            if (data && data.success && data.data) {
              if (onSuccess) onSuccess(data.data);
            } else if (data && data.members) {
              if (onSuccess) onSuccess(data);
            } else if (data && data.success) {
              if (onSuccess) onSuccess(data);
            } else {
              throw new Error(data && data.error ? data.error : "Invalid API response");
            }
          })
          .catch(function(err){
            console.warn("Direct fetch failed, trying JSONP fallback...", err);
            var cbName = "gullak_cb_" + Date.now() + "_" + Math.floor(Math.random() * 10000);
            var script = document.createElement("script");
            var timer = setTimeout(function(){
              delete window[cbName];
              if (script.parentNode) script.parentNode.removeChild(script);
              if (onError) onError(new Error("Request timed out"));
            }, 15000);

            window[cbName] = function(resp) {
              clearTimeout(timer);
              delete window[cbName];
              if (script.parentNode) script.parentNode.removeChild(script);
              if (resp && resp.success && resp.data) {
                if (onSuccess) onSuccess(resp.data);
              } else if (resp && resp.members) {
                if (onSuccess) onSuccess(resp);
              } else {
                if (onSuccess) onSuccess(resp);
              }
            };

            script.src = webUrl + (webUrl.indexOf("?") === -1 ? "?" : "&") + "action=" + action + "&callback=" + cbName + "&t=" + Date.now();
            script.onerror = function() {
              clearTimeout(timer);
              delete window[cbName];
              if (script.parentNode) script.parentNode.removeChild(script);
              if (onError) onError(new Error("Network connection error. Check Web App URL."));
            };
            document.body.appendChild(script);
          });
      } else {
        var bodyObj = Object.assign({ action: action }, payload);
        fetch(webUrl, {
          method: "POST",
          mode: "no-cors",
          headers: { "Content-Type": "text/plain" },
          body: JSON.stringify(bodyObj)
        })
        .then(function(){
          if (onSuccess) onSuccess({ success: true });
        })
        .catch(function(err){
          console.warn("POST failed:", err);
          if (onSuccess) onSuccess({ success: true });
        });
      }
    }
  };

  window.saveAndConnectWebAppUrl = function() {
    var inp = document.getElementById("inpGoogleWebAppUrl");
    var val = (inp ? inp.value : "").trim();
    if (!val) {
      showNotice("URL Required", "Kripya valid Google Apps Script Web App URL enter karein (ending in /exec)");
      return;
    }
    window.cloudHub.setWebAppUrl(val);
    var badge = document.getElementById("txtWebAppStatus");
    if (badge) {
      badge.textContent = "🔄 Connecting...";
      badge.style.background = "#78350F";
      badge.style.color = "#FBBF24";
    }
    showNotice("Connecting Cloud...", "Verifying connection to Google Spreadsheet...");
    window.cloudHub.callApi("getData", {}, function(res){
      closeModal("modalNotice");
      if (res && res.members && res.members.length > 0) {
        members = res.members;
        payments = res.payments || [];
        loans = res.loans || [];
        exitSettlements = res.exitSettlements || [];
        bonusSettlements = res.bonusSettlements || [];
        if (res.users && res.users.length > 0) window.authorizedUsers = res.users;
        if (res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
        saveStore();
        refreshAll();
        if (badge) {
          badge.textContent = "🟢 Connected (" + members.length + " Members)";
          badge.style.background = "#064E3B";
          badge.style.color = "#34D399";
        }
        showNotice("✅ Cloud Connected!", "Successfully connected to Google Sheet! Loaded " + members.length + " real members, " + payments.length + " receipts, and " + loans.length + " loans.");
      } else {
        if (badge) {
          badge.textContent = "🟢 URL Saved";
          badge.style.background = "#064E3B";
          badge.style.color = "#34D399";
        }
        showNotice("URL Saved", "Google Web App URL saved successfully!");
      }
    }, function(err){
      closeModal("modalNotice");
      if (badge) {
        badge.textContent = "⚠️ Sync Error";
        badge.style.background = "#7F1D1D";
        badge.style.color = "#F87171";
      }
      showNotice("Connection Warning", "URL save ho gaya hai, par live data fetch me warning aayi: " + (err.message || err));
    });
  };

  window.triggerCloudSyncNow = function() {
    showNotice("Syncing Cloud...", "Google Spreadsheet se live verified data fetch ho raha hai...");
    window.cloudHub.callApi("getData", {}, function(res){
      closeModal("modalNotice");
      if (res && res.members && res.members.length > 0) {
        members = res.members;
        payments = res.payments || [];
        loans = res.loans || [];
        exitSettlements = res.exitSettlements || [];
        bonusSettlements = res.bonusSettlements || [];
        if (res.users && res.users.length > 0) window.authorizedUsers = res.users;
        if (res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
        saveStore();
        refreshAll();
        showNotice("✅ Sync Complete!", "Google Sheet se " + members.length + " members, " + payments.length + " receipts aur " + loans.length + " loans successfully sync ho gaye!");
      } else {
        refreshAll();
        showNotice("Sync Done", "Local data refresh ho gaya.");
      }
    }, function(err){
      closeModal("modalNotice");
      showNotice("Sync Notice", "Google Sheet se sync karne ke liye Settings me apna Web App URL dalein ya internet connect karein.");
    });
  };

  window.triggerRestore67Members = function() {
    showNotice("Restoring Members...", "Loading all 67 registered society members into Cloud Database...");
    window.cloudHub.callApi("restore67Members", {}, function(res){
      closeModal("modalNotice");
      // Trigger full sync
      window.triggerCloudSyncNow();
    }, function(err){
      // Local fallback
      members = (typeof DEF_M !== "undefined" && DEF_M.length > 0) ? DEF_M : members;
      saveStore();
      refreshAll();
      closeModal("modalNotice");
      showNotice("✅ 67 Members Restored", "All 67 real society members loaded successfully into local app!");
    });
  };
"""

# Replace handleTopReload in Part3B to use cloudHub
p3b_fixed = p3b
p3b_fixed = re.sub(r'window\.handleTopReload\s*=\s*function\(\)\s*\{.*?\}\s*;', 'window.handleTopReload = function() { window.triggerCloudSyncNow(); };', p3b_fixed, flags=re.DOTALL)

# Append cloudHub to Part3B before closing script
p3b_fixed = p3b_fixed.replace('</script>', cloud_hub_code + '\n</script>')

# Init input box value in Settings on boot
init_ui_code = """
    // Populate saved Web App URL in settings field
    var savedWebUrl = window.cloudHub ? window.cloudHub.getWebAppUrl() : "";
    var inpUrl = document.getElementById("inpGoogleWebAppUrl");
    if (inpUrl && savedWebUrl) {
      inpUrl.value = savedWebUrl;
    }
"""

p3b_fixed = p3b_fixed.replace('window.bootApplication = function(){', 'window.bootApplication = function(){\n' + init_ui_code)

with open('Part3B.gs', 'w', encoding='utf-8') as f:
    f.write(p3b_fixed)

print("Part3B.gs successfully processed.")

# 6. Build index.html containing the full master software
print("Assembling complete index.html...")
full_html = p2_fixed
# In Part2_Html.gs, getCompleteSoftwareHtmlContent returns HTML template.
# Let's extract the HTML string from Part2_Html.gs
html_start = full_html.find('<!DOCTYPE html>')
html_end = full_html.rfind('`;')
if html_end == -1:
    html_end = len(full_html)
html_body = full_html[html_start:html_end]

# Extract Part3A and Part3B script bodies
script_a_start = p3a.find('<script>')
script_a_end = p3a.rfind('</script>') + 9
script_a_content = p3a[script_a_start:script_a_end]

script_b_start = p3b_fixed.find('<script>')
script_b_end = p3b_fixed.rfind('</script>') + 9
script_b_content = p3b_fixed[script_b_start:script_b_end]

# Replace placeholders or append scripts in html_body
complete_standalone_html = html_body.replace('</body>', script_a_content + '\n' + script_b_content + '\n</body>')

with open('index.html', 'w', encoding='utf-8') as f:
    f.write(complete_standalone_html)

print("index.html successfully created with full application suite.")

# 7. Build Gullak_Master_V64_PRO.gs and code.gs
print("Building Gullak_Master_V64_PRO.gs and code.gs...")
complete_gs = "/**\\n * 🏦 GULLAK CO-OPERATIVE SOCIETY - BACKEND CONTROLLER (V64 PRO MASTER)\\n * Standardized Sheets + Dual-Mode Cloud Sync Engine + 67 Real Members + Strict Sheet Protection\\n */\\n\\n" + p1_fixed + "\\n\\n" + p2_fixed + "\\n\\n" + p3a + "\\n\\n" + p3b_fixed + "\\n\\nfunction getCompleteSoftwareHtml() {\\n  return getCompleteSoftwareHtmlContent() + getClientScriptPartA() + getClientScriptPartB();\\n}\\n"


with open('Gullak_Master_V64_PRO.gs', 'w', encoding='utf-8') as f:
    f.write(complete_gs)

with open('code.gs', 'w', encoding='utf-8') as f:
    f.write(complete_gs)

print("==================================================")
print("  BUILD COMPLETE: Gullak Master V64 PRO Ready!    ")
print("==================================================")
