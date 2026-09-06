
const fs = require("fs");

const actualIds = new Set(['bonusMemId', 'tabHead3', 'searchPayInput', 'inpExitNetRefund', 'tbodySubBonus', 'selPayMember', 'inpPayRd', 'dispTotalNpa', 'tbodyBulkList', 'btnTopReload', 'dispTotalRd', 'inpBonusDate', 'inpLoanDate', 'selFilterStatus', 'searchLoanInput', 'btnSubmitReceive', 'selSortPayDate', 'inpSubFilterFrom', 'btnApplyNpaFilter', 'tfootPenaltyTotal', 'inpNewMemName', 'noticeBody', 'inpSearchLoanMember', 'inpPayNarration', 'btnWinLogin', 'tbodyFundMonths', 'dispPayDateFormatted', 'inpBonusNetPaid', 'selBonusMode', 'lblExitPen', 'kpiCardLoans', 'inpGlobalDueDay', 'kpiCardMembers', 'inpSearchReceiveMember', 'modalReceive', 'tabHead1', 'inpPenFilterTo', 'inpNewMemRd', 'fundDrilldownBox', 'lblReceiveModalHead', 'btnPrintLedgerPdf', 'confirmBody', 'windowsLoginOverlay', 'winForgotCard', 'selLoanMember', 'tabPanel5', 'btnTopExit', 'modalBonusSetoff', 'inpNewMemBal', 'tbodyLedgerTxns', 'noticeHeader', 'lblBonusStmtTitle', 'inpNpaFilterTo', 'tabPanel4', 'btnBulkSetAllCash', 'btnAddBulkRow', 'tfootMembersTotal', 'inpLoanFilterTo', 'modalConfirm', 'inpLoanNarration', 'inpPayFilterTo', 'lblLoanModalHead', 'kpiCardBonus', 'tbodyBonusList', 'tbodyPenaltyList', 'inpLoanPrinc', 'tabPanel2', 'lblExitRd', 'inpWinUsername', 'btnSubTabInterest', 'inpBulkDate', 'tbodyDrilldown', 'selLoanType', 'inpNewMemOpPen', 'inpNewMemStatus', 'btnSubmitMember', 'selSortMembers', 'btnSubTabBonus', 'bonusStmtHeaderStats', 'inpPayDate', 'inpBonusAdjLoan', 'tbodyBonusSchedule', 'inpNewMemOpLoan', 'selFilterLoanStatus', 'formWinLogin', 'lblDrilldownTitle', 'btnTopAddMember', 'inpBonusAdjPen', 'inpPayFilterFrom', 'dispExitNetResult', 'modalLedger', 'kpiCardRd', 'inpNewMemNominee', 'lblMemberLoanLimit', 'btnApplyGlobalSettings', 'tbodyNpaList', 'lblExitLoan', 'dispBulkDateFormatted', 'inpPayInterest', 'dispTotalMem', 'boxSubBonus', 'editReceiptNo', 'tbodySubInterest', 'modalFund', 'inpBonusAdjInt', 'inpFundTo', 'tfootLoansTotal', 'tabPanel3', 'btnNoticeOk', 'inpLedgerFilterFrom', 'lblRegBankBal', 'modalSettings', 'btnPanelNewLoan', 'inpBonusAdjRd', 'btnToggleEye', 'modalBulk', 'lblExitBonus', 'tfootBonusTotal', 'selFilterPayMode', 'lblBulkSelectedCount', 'inpNewMemDueDay', 'lblBonusTargetMember', 'selSortPen', 'inpNewMemCustomLimit', 'dispLoanDateFormatted', 'searchBonusInput', 'inpNpaFilterFrom', 'kpiCardNpa', 'btnCloseDrilldown', 'inpPayPenalty', 'inpLedgerFilterTo', 'ledgerHeaderStats', 'btnSubmitBonusSetoff', 'inpExitWaiver', 'btnBulkSetAllOnline', 'modalLoan', 'selExitMember', 'chkExitIncludeBonus', 'inpNewMemMobile', 'inpBonusFilterFrom', 'tabHead4', 'searchPenInput', 'inpFundFrom', 'inpLoanRate', 'btnTopLoan', 'inpWinPassword', 'modalNpa', 'btnSubmitLoan', 'selPayMode', 'btnApplyFundDate', 'chkSelectAllBulk', 'confirmHeader', 'btnTopReceive', 'selFilterBonusStatus', 'boxSubInterest', 'tfootPaymentsTotal', 'inpNewMemAddress', 'modalMember', 'selFinancialYear', 'inpPayWaiver', 'tbodyLoans', 'editMemId', 'btnTopBulk', 'modalNotice', 'selFilterLoanType', 'winLoginError', 'lblLedgerName', 'kpiCardFund', 'btnConfirmProceed', 'lblExitResultType', 'memberFilterInput', 'btnSubmitExit', 'lblMemberModalHead', 'inpNewMemOpInt', 'btnPrintBonusPdf', 'inpExitNpa', 'editLoanId', 'inpLoanFilterFrom', 'inpNewMemJoinDate', 'inpGlobalRate', 'tbodyMembers', 'printableBonusArea', 'tfootNpaTotal', 'tbodyPayments', 'tabPanel1', 'btnApplySubFilter', 'lblRegCashBal', 'inpBonusFilterTo', 'btnTopSettings', 'dispTotalLoan', 'btnPanelNewReceipt', 'lblRegTotalFund', 'inpPenFilterFrom', 'dispTotalFund', 'printableLedgerArea', 'tabHead2', 'modalExit', 'btnLoginFullscreen', 'modalBonusStatement', 'lblBulkGrandTotal', 'tabHead5', 'lblBonusAmount', 'selFilterPenStatus', 'modalBonusOverview', 'dispTotalBonus', 'selSortBonus', 'btnSubmitBulk', 'inpPayPrincipal', 'inpSubFilterTo']);

const elements = {};

function createElement(id) {
  return {
    id: id,
    value: (id.startsWith("sel") || id.startsWith("inp")) ? (id === "selFilterStatus" ? "ACTIVE" : (id === "selSortMembers" ? "name_az" : (id === "selFinancialYear" ? "2026" : "0"))) : "",
    innerText: "",
    innerHTML: "",
    style: { display: "block", borderColor: "" },
    classList: { add: ()=>{}, remove: ()=>{}, contains: ()=>false },
    addEventListener: function(event, cb) {},
    removeEventListener: ()=>{},
    focus: ()=>{},
    blur: ()=>{},
    getAttribute: (attr)=>"",
    setAttribute: (attr, val)=>{},
    closest: (selector)=>null,
    querySelector: (sel)=>createElement("dummy_qs"),
    querySelectorAll: (sel)=>[createElement("dummy_qsa")]
  };
}

actualIds.forEach(id => {
  elements[id] = createElement(id);
});

global.window = global;
global.window.addEventListener = function(evt, cb){};

global.document = {
  readyState: "complete",
  getElementById: function(id) {
    if (!actualIds.has(id)) {
      console.error("STRICT ERROR: document.getElementById requested NON-EXISTENT ID:", id);
      return null;
    }
    return elements[id];
  },
  querySelector: function(sel) {
    return createElement("dummy_" + sel);
  },
  querySelectorAll: function(sel) {
    return [createElement("dummy_all_" + sel)];
  },
  addEventListener: function(evt, cb) {
    if (evt === "DOMContentLoaded") cb();
  }
};

global.sessionStorage = {
  getItem: (k)=>null,
  setItem: (k,v)=>{},
  removeItem: (k)=>{}
};

global.localStorage = {
  getItem: (k)=>null,
  setItem: (k,v)=>{},
  removeItem: (k)=>{}
};

global.google = {
  script: {
    run: {
      withSuccessHandler: function(cb) {
        return {
          getSocietyFullData: function() {
            console.log("Mock getSocietyFullData called");
          }
        };
      }
    }
  }
};

try {
  const code = fs.readFileSync("test_client.js", "utf-8");
  eval(code);
  console.log("SUCCESS! Strict test passed with zero errors!");
} catch (err) {
  console.error("STRICT CRASH DETECTED:", err);
}
