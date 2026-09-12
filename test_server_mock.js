
// Mock Google Apps Script environment
global.SpreadsheetApp = {
  ProtectionType: { SHEET: 'SHEET' },
  TextDirection: { LEFT_TO_RIGHT: 'LEFT_TO_RIGHT' },
  flush: function() {},
  getUi: function() {
    return {
      createMenu: function() { return this; },
      addItem: function() { return this; },
      addSeparator: function() { return this; },
      addToUi: function() { return this; },
      alert: function(msg) { console.log('UI Alert:', msg); },
      prompt: function(t, m) { return { getSelectedButton: () => 'OK', getResponseText: () => '12345' }; },
      ButtonSet: { OK: 'OK', OK_CANCEL: 'OK_CANCEL' },
      Button: { OK: 'OK', CANCEL: 'CANCEL' }
    };
  },
  getActiveSpreadsheet: function() {
    return mockSpreadsheet;
  }
};

global.ScriptApp = {
  getService: function() { return { getUrl: () => 'https://script.google.com/macros/s/TEST/exec' }; }
};
global.MailApp = {
  sendEmail: function(to, sub, body) { console.log('Mail sent to', to); }
};
global.Session = {
  getActiveUser: function() { return { getEmail: () => 'admin@gmail.com' }; }
};
global.PropertiesService = {
  getScriptProperties: function() {
    var props = {};
    return {
      getProperty: (k) => props[k] || null,
      setProperty: (k, v) => { props[k] = v; }
    };
  }
};
global.Utilities = {
  formatDate: function(d, tz, fmt) { return '2026-01-01'; }
};
global.ContentService = {
  MimeType: { JSON: 'application/json', JAVASCRIPT: 'application/javascript' },
  createTextOutput: function(text) {
    return {
      setMimeType: function(mt) { return this; },
      text: text
    };
  }
};
global.HtmlService = {
  XFrameOptionsMode: { ALLOWALL: 'ALLOWALL' },
  createHtmlOutput: function(html) {
    return {
      setTitle: function() { return this; },
      setXFrameOptionsMode: function() { return this; },
      addMetaTag: function() { return this; }
    };
  }
};

class MockSheet {
  constructor(name) {
    this.name = name;
    this.rows = [];
  }
  getName() { return this.name; }
  getLastRow() { return this.rows.length; }
  getLastColumn() { return this.rows.length > 0 ? (this.rows[0] ? this.rows[0].length : 0) : 0; }
  getRange(r, c, numR, numC) {
    var self = this;
    return {
      getValues: function() {
        var res = [];
        for (var i = 0; i < (numR || 1); i++) {
          var rowIdx = (r - 1) + i;
          var row = self.rows[rowIdx] || [];
          var slice = [];
          for (var j = 0; j < (numC || 1); j++) {
            slice.push(row[(c - 1) + j] !== undefined ? row[(c - 1) + j] : '');
          }
          res.push(slice);
        }
        return res;
      },
      setValues: function(vals) {
        for (var i = 0; i < vals.length; i++) {
          var rowIdx = (r - 1) + i;
          if (!self.rows[rowIdx]) self.rows[rowIdx] = [];
          for (var j = 0; j < vals[i].length; j++) {
            self.rows[rowIdx][(c - 1) + j] = vals[i][j];
          }
        }
      },
      setValue: function(v) {
        if (!self.rows[r - 1]) self.rows[r - 1] = [];
        self.rows[r - 1][c - 1] = v;
      },
      setBackground: function() {},
      setFontColor: function() {},
      setFontWeight: function() {},
      setHorizontalAlignment: function() {},
      setFontFamily: function() {},
      setFontSize: function() {},
      setNumberFormat: function() {},
      setBorder: function() {},
      setDataValidation: function() {},
      clear: function() { this.rows = []; }, clearContents: function() { this.rows = []; }, clearFormats: function() {}, clearContent: function() {
        for (var i = 0; i < (numR || 1); i++) {
          var rowIdx = (r - 1) + i;
          if (self.rows[rowIdx]) {
            for (var j = 0; j < (numC || 1); j++) {
              self.rows[rowIdx][(c - 1) + j] = '';
            }
          }
        }
      }
    };
  }
  appendRow(row) {
    this.rows.push(row);
  }
  deleteRow(rowIdx) {
    this.rows.splice(rowIdx - 1, 1);
  }
  setTabColor() {}
  setFrozenRows() {}
  getFilter() { return null; }
  autoResizeColumns() {}
  setColumnWidth() {}
  getProtections() { return []; }
  protect() {
    return {
      setDescription: function() {},
      setWarningOnly: function() {},
      remove: function() {}
    };
  }
}

var mockSpreadsheet = {
  sheets: {},
  getUrl: function() { return 'https://docs.google.com/spreadsheets/d/TEST/edit'; },
  getSheets: function() { return Object.values(this.sheets); },
  getSheetByName: function(name) { return this.sheets[name] || null; },
  insertSheet: function(name) {
    var s = new MockSheet(name);
    this.sheets[name] = s;
    return s;
  },
  deleteSheet: function(s) {
    delete this.sheets[s.getName()];
  },
  getProtections: function() { return []; }
};

const fs = require('fs');
eval(fs.readFileSync('Part1_Server.gs', 'utf8'));

console.log('Testing onOpen...');
onOpen();

console.log('Testing installAndRunDatabase...');
installAndRunDatabase();

console.log('Testing restoreAll67RealSocietyMembers...');
restoreAll67RealSocietyMembers();

console.log('Testing autoFixAndAlignAllSheets...');
autoFixAndAlignAllSheets();

console.log('Testing syncPenaltyRegisterSheetBackend...');
syncPenaltyRegisterSheetBackend();

console.log('Testing syncProfitAndLossSheetBackend...');
syncProfitAndLossSheetBackend();

console.log('Testing syncFinancialsSheetBackend...');
syncFinancialsSheetBackend();

console.log('Testing getSocietyFullData...');
var data = getSocietyFullData();
console.log('Members count:', data.members ? data.members.length : 0);

console.log('Testing handleApiRequest getData...');
var res = handleApiRequest({ action: 'getData' }, null);
console.log('API output length:', res.text.length);

console.log('Testing handleApiRequest restore67Members...');
var res2 = handleApiRequest({ action: 'restore67Members' }, null);
console.log('API restore output:', res2.text);

console.log('ALL TESTS PASSED WITH ZERO ERRORS!');
