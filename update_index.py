# Python script to write index.html safely
html_content = """<!DOCTYPE html>
<html lang="hi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Gullak Society V38 PRO — Master Download Hub & Code Viewer</title>
  <style>
    :root {
      --bg: #0B0F19;
      --card-bg: #151D2F;
      --card-border: #243049;
      --accent: #3B82F6;
      --accent-hover: #2563EB;
      --success: #10B981;
      --gold: #F59E0B;
      --text: #F3F4F6;
      --text-muted: #9CA3AF;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body {
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      padding: 30px 20px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .container {
      max-width: 960px;
      width: 100%;
    }
    .header {
      text-align: center;
      margin-bottom: 24px;
      padding-bottom: 20px;
      border-bottom: 1px solid var(--card-border);
    }
    .badge {
      display: inline-block;
      background: rgba(16, 185, 129, 0.15);
      color: #34D399;
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 0.85rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      margin-bottom: 12px;
      border: 1px solid rgba(16, 185, 129, 0.3);
    }
    h1 {
      font-size: 2.1rem;
      font-weight: 800;
      background: linear-gradient(135deg, #FFFFFF, #93C5FD);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 10px;
    }
    p.sub {
      color: var(--text-muted);
      font-size: 1.02rem;
      line-height: 1.5;
    }
    .alert-card {
      background: rgba(245, 158, 11, 0.1);
      border: 1.5px solid #F59E0B;
      border-radius: 12px;
      padding: 16px 20px;
      margin-bottom: 25px;
      display: flex;
      align-items: center;
      gap: 15px;
    }
    .main-download-card {
      background: linear-gradient(135deg, #1E293B, #0F172A);
      border: 2px solid #3B82F6;
      border-radius: 16px;
      padding: 28px;
      text-align: center;
      margin-bottom: 24px;
      box-shadow: 0 10px 30px -10px rgba(59, 130, 246, 0.4);
    }
    .btn-action-primary {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background: #2563EB;
      color: #FFFFFF;
      text-decoration: none;
      font-size: 1.15rem;
      font-weight: 700;
      padding: 14px 28px;
      border-radius: 10px;
      transition: all 0.2s ease;
      box-shadow: 0 4px 15px rgba(37, 99, 235, 0.5);
      margin: 8px;
      cursor: pointer;
      border: none;
    }
    .btn-action-primary:hover {
      background: #1D4ED8;
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(37, 99, 235, 0.7);
    }
    .btn-action-secondary {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: #334155;
      color: #38BDF8;
      text-decoration: none;
      font-size: 1.05rem;
      font-weight: 700;
      padding: 14px 24px;
      border-radius: 10px;
      transition: all 0.2s ease;
      margin: 8px;
      cursor: pointer;
      border: 1px solid #475569;
    }
    .btn-action-secondary:hover {
      background: #475569;
      color: #FFFFFF;
      transform: translateY(-2px);
    }
    .btn-action-green {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: #059669;
      color: #FFFFFF;
      text-decoration: none;
      font-size: 1.05rem;
      font-weight: 700;
      padding: 14px 24px;
      border-radius: 10px;
      transition: all 0.2s ease;
      margin: 8px;
      cursor: pointer;
      border: 1px solid #10B981;
    }
    .btn-action-green:hover {
      background: #047857;
      transform: translateY(-2px);
    }
    .file-meta {
      font-size: 0.88rem;
      color: #94A3B8;
      margin-top: 10px;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 16px;
      margin-bottom: 25px;
    }
    .file-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 12px;
      padding: 18px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .file-card h3 {
      font-size: 1.1rem;
      color: #F8FAFC;
      margin-bottom: 6px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .file-card p {
      font-size: 0.85rem;
      color: var(--text-muted);
      margin-bottom: 14px;
      flex-grow: 1;
    }
    .file-card-buttons {
      display: flex;
      gap: 8px;
    }
    .file-card-buttons button, .file-card-buttons a {
      flex: 1;
      text-align: center;
      background: #1E293B;
      color: #38BDF8;
      border: 1px solid #334155;
      padding: 9px 12px;
      border-radius: 8px;
      text-decoration: none;
      font-weight: 600;
      font-size: 0.85rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .file-card-buttons button:hover, .file-card-buttons a:hover {
      background: #334155;
      color: #FFFFFF;
    }
    .instruction-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 24px;
      margin-bottom: 25px;
    }
    .instruction-card h2 {
      font-size: 1.25rem;
      color: var(--gold);
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .instruction-card ol {
      padding-left: 20px;
      color: #E2E8F0;
      line-height: 1.8;
      font-size: 0.95rem;
    }
    .instruction-card li strong {
      color: #60A5FA;
    }
    
    /* Code Viewer Modal */
    .viewer-modal-backdrop {
      display: none;
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.85);
      z-index: 99999;
      justify-content: center;
      align-items: center;
      padding: 20px;
      backdrop-filter: blur(5px);
    }
    .viewer-modal-content {
      background: #0F172A;
      border: 2px solid #3B82F6;
      border-radius: 16px;
      width: 100%;
      max-width: 1050px;
      height: 90vh;
      display: flex;
      flex-direction: column;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.9);
      overflow: hidden;
    }
    .viewer-header {
      background: #1E293B;
      padding: 14px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #334155;
    }
    .viewer-title {
      font-weight: 800;
      font-size: 1.1rem;
      color: #F8FAFC;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .viewer-actions {
      display: flex;
      gap: 10px;
      align-items: center;
    }
    .btn-copy {
      background: #10B981;
      color: #FFFFFF;
      border: none;
      padding: 8px 18px;
      border-radius: 8px;
      font-weight: 700;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .btn-copy:hover {
      background: #059669;
    }
    .btn-close-viewer {
      background: #EF4444;
      color: #FFFFFF;
      border: none;
      padding: 8px 14px;
      border-radius: 8px;
      font-weight: 700;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-close-viewer:hover {
      background: #DC2626;
    }
    .viewer-tabs {
      background: #151D2F;
      display: flex;
      gap: 4px;
      padding: 8px 16px 0 16px;
      border-bottom: 1px solid #334155;
      overflow-x: auto;
    }
    .viewer-tab {
      background: #1E293B;
      color: #94A3B8;
      padding: 8px 16px;
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
      cursor: pointer;
      font-size: 0.88rem;
      font-weight: 600;
      border: 1px solid #334155;
      border-bottom: none;
      white-space: nowrap;
    }
    .viewer-tab.active {
      background: #0F172A;
      color: #38BDF8;
      border-top: 2px solid #38BDF8;
      font-weight: 700;
    }
    .code-textarea {
      flex: 1;
      width: 100%;
      background: #090D16;
      color: #E2E8F0;
      font-family: Consolas, Monaco, "Courier New", monospace;
      font-size: 0.88rem;
      line-height: 1.55;
      padding: 18px;
      border: none;
      resize: none;
      outline: none;
      white-space: pre;
      overflow: auto;
    }
    .viewer-footer {
      background: #1E293B;
      padding: 10px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid #334155;
      font-size: 0.82rem;
      color: #94A3B8;
    }
    
    /* Toast Notification */
    #copyToast {
      visibility: hidden;
      min-width: 250px;
      background-color: #10B981;
      color: #fff;
      text-align: center;
      border-radius: 8px;
      padding: 12px 20px;
      position: fixed;
      z-index: 100000;
      top: 30px;
      right: 30px;
      font-weight: 700;
      font-size: 0.95rem;
      box-shadow: 0 10px 25px rgba(0,0,0,0.5);
      transition: all 0.3s;
      transform: translateY(-20px);
      opacity: 0;
    }
    #copyToast.show {
      visibility: visible;
      opacity: 1;
      transform: translateY(0);
    }
  </style>
</head>
<body>

  <div id="copyToast">✅ Code Copied to Clipboard!</div>

  <!-- CODE VIEWER MODAL -->
  <div id="codeViewerModal" class="viewer-modal-backdrop">
    <div class="viewer-modal-content">
      <div class="viewer-header">
        <div class="viewer-title" id="lblViewerFileName">
          <span>📄</span> Code.gs — Full Monolithic Script
        </div>
        <div class="viewer-actions">
          <button class="btn-copy" id="btnCopyCode" onclick="copyCurrentViewerCode()">
            <span>📋</span> Copy All Code
          </button>
          <button class="btn-action-primary" style="padding: 8px 16px; font-size: 0.88rem; margin:0;" onclick="downloadCurrentViewerFile()">
            <span>⬇️</span> Download File
          </button>
          <button class="btn-close-viewer" onclick="closeCodeViewer()">✖ Close</button>
        </div>
      </div>

      <div class="viewer-tabs">
        <div class="viewer-tab active" onclick="switchViewerTab('Code.gs')">Code.gs (Complete Monolithic)</div>
        <div class="viewer-tab" onclick="switchViewerTab('Part1_Server.gs')">Part1_Server.gs</div>
        <div class="viewer-tab" onclick="switchViewerTab('Part2_Html.gs')">Part2_Html.gs</div>
        <div class="viewer-tab" onclick="switchViewerTab('Part3A.gs')">Part3A.gs</div>
        <div class="viewer-tab" onclick="switchViewerTab('Part3B.gs')">Part3B.gs</div>
      </div>

      <textarea id="txtCodeContent" class="code-textarea" readonly spellcheck="false"></textarea>

      <div class="viewer-footer">
        <span id="lblLineStats">Lines: 0 | Size: 0 KB</span>
        <span>💡 Click 'Copy All Code' and paste directly into Google Apps Script editor.</span>
      </div>
    </div>
  </div>

  <div class="container">
    <div class="header">
      <div class="badge">UPDATED &amp; VERIFIED &bull; V38 PRO MASTER RELEASE</div>
      <h1>Gullak Co-operative Society — Direct Download Hub</h1>
      <p class="sub">V38 Update: Direct Download, 1-Click Code Copy, Locked Web Portal on Load/F5 Refresh, Flexible Column Mapping for Members &amp; Payments Sheets, Auto-Financials Sync &amp; Working Month-wise Details Drilldown</p>
    </div>

    <!-- CREDENTIALS BANNER -->
    <div class="alert-card">
      <div style="font-size: 2rem;">🔑</div>
      <div>
        <div style="font-weight: 800; color: #FBBF24; font-size: 1rem; margin-bottom: 3px;">
          Default Login Credentials (V38 PRO):
        </div>
        <div style="font-size: 0.95rem; color: #F1F5F9;">
          <strong>Username:</strong> <span style="color: #60A5FA;">SANISH</span> (ya ADMIN) &nbsp;|&nbsp; 
          <strong>Default Password:</strong> <span style="color: #34D399; font-weight: 800;">12345</span>
        </div>
        <div style="font-size: 0.82rem; color: #94A3B8; margin-top: 5px;">
          💡 <em>Aapka Web Portal ab refresh (F5) karne par automatically lock ho jata hai aur system ko unlock karne ke liye password input required hai.</em>
        </div>
      </div>
    </div>

    <!-- PRIMARY SINGLE CODE.GS ZIP CARD (V38) -->
    <div class="main-download-card">
      <div style="font-size: 2.5rem; margin-bottom: 10px;">📦</div>
      <div class="badge" style="background:rgba(16,185,129,0.2); color:#10B981; border-color:rgba(16,185,129,0.4); margin-bottom:8px;">RECOMMENDED &bull; SINGLE ALL-IN-ONE CODE.GS ZIP</div>
      <h2 style="font-size: 1.6rem; margin-bottom: 8px; color:#FFFFFF;">Code_gs_Only_V38.zip</h2>
      <p style="color: #CBD5E1; margin-bottom: 16px; font-size: 0.95rem;">
        Contains <strong>ONLY Code.gs</strong> (Monolithic All-In-One single file) inside a ZIP archive. Direct Google Apps Script single file deployment without extra sub-files!
      </p>
      
      <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px;">
        <button onclick="downloadZipV38()" class="btn-action-primary">
          <span>⬇️</span> Direct Download Code_gs_Only_V38.zip (49 KB)
        </button>
        <button onclick="openCodeViewer('Code.gs')" class="btn-action-green">
          <span>👁️</span> View &amp; Copy Code.gs Script
        </button>
      </div>

      <div class="file-meta">Version 38 &bull; Monolithic Single File Code.gs ONLY &bull; Direct Browser Download &amp; Deploy</div>
    </div>

    <!-- SECONDARY MASTER PACKAGE CARD -->
    <div class="main-download-card" style="border-color:#F59E0B; background: linear-gradient(135deg, #1E1B4B, #0F172A);">
      <div style="font-size: 2rem; margin-bottom: 8px;">📂</div>
      <h3 style="font-size: 1.3rem; margin-bottom: 6px; color:#FBBF24;">Gullak_V38_Master_Code.zip (Full Developer Package)</h3>
      <p style="color: #94A3B8; margin-bottom: 12px; font-size: 0.88rem;">
        Full developer package containing Code.gs + Part1_Server.gs, Part2_Html.gs, Part3A.gs, and Part3B.gs.
      </p>
      <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px;">
        <button onclick="downloadZipMasterV38()" class="btn-action-secondary" style="background:#3730A3; color:#A5B4FC; border-color:#4F46E5;">
          <span>⬇️</span> Download Gullak_V38_Master_Code.zip (100 KB)
        </button>
      </div>
    </div>

    <!-- INDIVIDUAL FILES DOWNLOAD & VIEW SECTION -->
    <h2 style="font-size: 1.3rem; margin-bottom: 16px; color: #94A3B8;">Ya Single Script Files Ko Direct View, Copy Ya Download Karein (V38):</h2>
    <div class="grid">
      <div class="file-card">
        <h3>📄 Code.gs</h3>
        <p>Complete Monolithic All-in-One File (Sabhi parts combined, 240 KB).</p>
        <div class="file-card-buttons">
          <button onclick="openCodeViewer('Code.gs')">👁️ View/Copy</button>
          <button onclick="downloadTextFile('Code.gs')">⬇️ Download</button>
        </div>
      </div>

      <div class="file-card">
        <h3>⚙️ Part1_Server.gs</h3>
        <p>Dynamic Header Column Mapper, Financials Sync &amp; Database Engine (32 KB).</p>
        <div class="file-card-buttons">
          <button onclick="openCodeViewer('Part1_Server.gs')">👁️ View/Copy</button>
          <button onclick="downloadTextFile('Part1_Server.gs')">⬇️ Download</button>
        </div>
      </div>

      <div class="file-card">
        <h3>🖥️ Part2_Html.gs</h3>
        <p>Locked Login Overlay, Month-wise Fund Breakdown UI &amp; Forms (75 KB).</p>
        <div class="file-card-buttons">
          <button onclick="openCodeViewer('Part2_Html.gs')">👁️ View/Copy</button>
          <button onclick="downloadTextFile('Part2_Html.gs')">⬇️ Download</button>
        </div>
      </div>

      <div class="file-card">
        <h3>📊 Part3A.gs</h3>
        <p>Login Auth Handler &amp; Date Helpers Engine (37 KB).</p>
        <div class="file-card-buttons">
          <button onclick="openCodeViewer('Part3A.gs')">👁️ View/Copy</button>
          <button onclick="downloadTextFile('Part3A.gs')">⬇️ Download</button>
        </div>
      </div>

      <div class="file-card">
        <h3>📝 Part3B.gs</h3>
        <p>Month-wise Fund Drilldown Handler, ESC Key Modal Listener &amp; Lock Boot (85 KB).</p>
        <div class="file-card-buttons">
          <button onclick="openCodeViewer('Part3B.gs')">👁️ View/Copy</button>
          <button onclick="downloadTextFile('Part3B.gs')">⬇️ Download</button>
        </div>
      </div>

      <div class="file-card">
        <h3>📦 V37 Master Zip</h3>
        <p>Previous V37 build package kept for reference (96 KB).</p>
        <div class="file-card-buttons">
          <a href="Gullak_V37_Master_Code.zip" download="Gullak_V37_Master_Code.zip">⬇️ Download V37</a>
        </div>
      </div>
    </div>

    <!-- RESOLVED ISSUES OVERVIEW (VERSION 38) -->
    <div class="instruction-card">
      <h2>✅ What Has Been Fixed &amp; Updated in Version 38 PRO Release:</h2>
      <ol>
        <li>
          <strong>1. Forced Lock on Web App Open &amp; F5 Refresh:</strong> Web app opening par aur F5 refresh par automatically unlock nahi hota. Session clearing engine dwara overlay locked screen show hota hai aur valid password entry required hai.
        </li>
        <li>
          <strong>2. Google Sheet Column Header Auto-Mapping &amp; Data Validation:</strong> Google Sheet ke Members aur Payments tab me headers key-value map dwara dynamically read/write kiye jate hain. Extra Month/Year columns ya shifted Address/RD columns ke dauran data scrambled ("ulat-pulat") nahi hoga.
        </li>
        <li>
          <strong>3. Auto-Populated Financials Sheet &amp; Sheet1 Cleanup:</strong> <code>Financials</code> sheet automatically active society financial metrics (Active Members, RD Collected, Disbursed Loans, Outstanding Dues, Interest, Penalties, Liquid Reserves) se fill rehti hai. Unused <code>Sheet1</code> auto-clean up hota hai.
        </li>
        <li>
          <strong>4. ESC Key Modal Close Protection:</strong> ESC button dabaane se full screen exit nahi hota — capture phase event listener direct modal ke close button ko trigger karke sirf open form/report modal ko close karta hai.
        </li>
        <li>
          <strong>5. Month-wise Fund Breakdown Details Button Functional:</strong> Cash &amp; Bank Register modal me "🔍 Details" button click karne par us specific month ki sabhi source receipts, discursals aur transactions <code>#fundDrilldownBox</code> me exact line items ke saath display hoti hain.
        </li>
      </ol>
    </div>

    <!-- HOW TO DEPLOY IN GOOGLE SHEETS -->
    <div class="instruction-card">
      <h2>🚀 Google Sheets me Deploy Kaise Karein (Easy 3 Steps):</h2>
      <ol>
        <li><strong>Extensions &gt; Apps Script</strong> me jayein.</li>
        <li>Wahan <strong>Code.gs</strong> ka sara purana code delete karein. Fir upar <strong>'View &amp; Copy Code.gs Script'</strong> button daba kar <strong>'Copy All Code'</strong> karein aur Apps Script me paste kar dein.</li>
        <li>Upar blue button <strong>Deploy &gt; Manage deployments &gt; Edit (pencil icon) &gt; Version: New version &gt; Deploy</strong> par click karein. Aapka live portal ready ho jayega!</li>
      </ol>
    </div>

  </div>

  <!-- Data Bundle containing Code.gs and all parts -->
  <script src="files_data.js"></script>

  <script>
    var currentActiveFile = "Code.gs";

    function showToast(msg) {
      var toast = document.getElementById("copyToast");
      toast.innerText = msg || "✅ Code Copied to Clipboard!";
      toast.className = "show";
      setTimeout(function(){ toast.className = toast.className.replace("show", ""); }, 3000);
    }

    function b64toBlob(b64Data, contentType, sliceSize) {
      contentType = contentType || "";
      sliceSize = sliceSize || 512;
      var byteCharacters = atob(b64Data);
      var byteArrays = [];
      for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
        var slice = byteCharacters.slice(offset, offset + sliceSize);
        var byteNumbers = new Array(slice.length);
        for (var i = 0; i < slice.length; i++) {
          byteNumbers[i] = slice.charCodeAt(i);
        }
        var byteArray = new Uint8Array(byteNumbers);
        byteArrays.push(byteArray);
      }
      return new Blob(byteArrays, {type: contentType});
    }

    function triggerBlobDownload(blob, filename) {
      var url = URL.createObjectURL(blob);
      var a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      setTimeout(function() {
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      }, 300);
    }

    function downloadZipV38() {
      if (window.GULLAK_ZIP38_B64) {
        var blob = b64toBlob(window.GULLAK_ZIP38_B64, "application/zip");
        triggerBlobDownload(blob, "Code_gs_Only_V38.zip");
        showToast("📦 Downloaded Code_gs_Only_V38.zip");
      } else {
        window.location.href = "Code_gs_Only_V38.zip";
      }
    }

    function downloadZipMasterV38() {
      if (window.GULLAK_ZIP_MASTER_B64) {
        var blob = b64toBlob(window.GULLAK_ZIP_MASTER_B64, "application/zip");
        triggerBlobDownload(blob, "Gullak_V38_Master_Code.zip");
        showToast("📂 Downloaded Gullak_V38_Master_Code.zip");
      } else {
        window.location.href = "Gullak_V38_Master_Code.zip";
      }
    }

    function downloadTextFile(fileName) {
      var content = (window.GULLAK_FILES && window.GULLAK_FILES[fileName]) ? window.GULLAK_FILES[fileName] : "";
      if (content) {
        var blob = new Blob([content], { type: "text/plain;charset=utf-8" });
        triggerBlobDownload(blob, fileName);
        showToast("📄 Downloaded " + fileName);
      } else {
        window.location.href = fileName;
      }
    }

    function openCodeViewer(fileName) {
      currentActiveFile = fileName || "Code.gs";
      var modal = document.getElementById("codeViewerModal");
      modal.style.display = "flex";
      switchViewerTab(currentActiveFile);
    }

    function closeCodeViewer() {
      var modal = document.getElementById("codeViewerModal");
      modal.style.display = "none";
    }

    function switchViewerTab(fileName) {
      currentActiveFile = fileName;
      var title = document.getElementById("lblViewerFileName");
      var txt = document.getElementById("txtCodeContent");
      var stats = document.getElementById("lblLineStats");

      // Update tabs styling
      var tabs = document.querySelectorAll(".viewer-tab");
      tabs.forEach(function(t){
        if (t.innerText.indexOf(fileName) !== -1) {
          t.classList.add("active");
        } else {
          t.classList.remove("active");
        }
      });

      title.innerHTML = "<span>📄</span> " + fileName;

      var content = (window.GULLAK_FILES && window.GULLAK_FILES[fileName]) ? window.GULLAK_FILES[fileName] : "";
      txt.value = content;

      var lineCount = content ? content.split("\\n").length : 0;
      var kbSize = Math.round((content.length / 1024) * 10) / 10;
      stats.innerText = "File: " + fileName + " | Lines: " + lineCount + " | Size: " + kbSize + " KB";
      txt.scrollTop = 0;
    }

    function copyCurrentViewerCode() {
      var txt = document.getElementById("txtCodeContent");
      if (!txt || !txt.value) return;
      txt.select();
      txt.setSelectionRange(0, 999999);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(txt.value).then(function(){
          showToast("📋 " + currentActiveFile + " copied to clipboard!");
        }).catch(function(){
          document.execCommand("copy");
          showToast("📋 " + currentActiveFile + " copied to clipboard!");
        });
      } else {
        document.execCommand("copy");
        showToast("📋 " + currentActiveFile + " copied to clipboard!");
      }
    }

    function downloadCurrentViewerFile() {
      downloadTextFile(currentActiveFile);
    }

    // ESC key closes viewer modal
    window.addEventListener("keydown", function(e){
      if(e.key === "Escape"){
        var modal = document.getElementById("codeViewerModal");
        if(modal && modal.style.display === "flex"){
          modal.style.display = "none";
        }
      }
    });
  </script>
</body>
</html>
"""

with open("index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print("Updated index.html written successfully!")
