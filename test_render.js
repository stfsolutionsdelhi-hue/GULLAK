
const fs = require("fs");

const elements = {};
const idList = ['modalNpa', 'btnWinLogin', 'inpNewMemOpLoan', 'tabPanel3', 'inpBonusFilterFrom', 'tfootLoansTotal', 'lblRegCashBal', 'tbodyPayments', 'modalBonusSetoff', 'inpBonusAdjLoan', 'btnSubmitMember', 'btnSubmitReceive', 'winForgotCard', 'btnApplyGlobalSettings', 'inpFundTo', 'dispTotalLoan', 'lblBulkSelectedCount', 'btnConfirmProceed', 'inpNewMemOpInt', 'tabHead2', 'btnBulkSetAllOnline', 'kpiCardNpa', 'inpNewMemCustomLimit', 'btnAddBulkRow', 'formWinLogin', 'inpPayDate', 'lblRegBankBal', 'selFilterBonusStatus', 'windowsLoginOverlay', 'dispLoanDateFormatted', 'editReceiptNo', 'inpNewMemAddress', 'btnTopLoan', 'selLoanType', 'btnPrintBonusPdf', 'kpiCardRd', 'modalSettings', 'editMemId', 'inpSubFilterTo', 'ledgerHeaderStats', 'inpBonusDate', 'btnTopExit', 'tbodyFundMonths', 'inpExitWaiver', 'kpiCardFund', 'tabPanel5', 'lblExitBonus', 'modalFund', 'btnTopReload', 'btnSubTabInterest', 'inpNewMemJoinDate', 'inpNewMemStatus', 'lblMemberLoanLimit', 'tbodyBulkList', 'btnApplyNpaFilter', 'inpNpaFilterTo', 'searchPayInput', 'dispTotalBonus', 'confirmHeader', 'inpNewMemNominee', 'btnSubmitBulk', 'noticeHeader', 'inpSearchLoanMember', 'dispBulkDateFormatted', 'inpPayNarration', 'btnBulkSetAllCash', 'inpLoanNarration', 'inpPayFilterTo', 'lblLedgerName', 'selLoanMember', 'inpNewMemBal', 'inpPayPrincipal', 'btnTopReceive', 'inpBonusNetPaid', 'tbodySubBonus', 'tfootPaymentsTotal', 'inpLoanFilterTo', 'inpFundFrom', 'inpExitNpa', 'inpWinPassword', 'inpNpaFilterFrom', 'dispTotalRd', 'searchPenInput', 'lblLoanModalHead', 'modalNotice', 'inpLoanFilterFrom', 'inpPayInterest', 'tbodyLedgerTxns', 'modalConfirm', 'tbodySubInterest', 'printableBonusArea', 'selFilterStatus', 'bonusStmtHeaderStats', 'inpBonusAdjRd', 'btnApplyFundDate', 'fundDrilldownBox', 'selFilterPenStatus', 'chkExitIncludeBonus', 'inpGlobalDueDay', 'memberFilterInput', 'tfootMembersTotal', 'inpGlobalRate', 'inpNewMemDueDay', 'tfootBonusTotal', 'selFilterPayMode', 'btnApplySubFilter', 'inpBulkDate', 'inpPenFilterTo', 'inpPayFilterFrom', 'chkSelectAllBulk', 'tbodyDrilldown', 'selSortBonus', 'boxSubBonus', 'printableLedgerArea', 'inpExitNetRefund', 'dispTotalNpa', 'searchLoanInput', 'winLoginError', 'btnPanelNewReceipt', 'inpBonusAdjInt', 'selExitMember', 'inpPayRd', 'lblMemberModalHead', 'inpPenFilterFrom', 'modalMember', 'modalBulk', 'tabPanel1', 'tbodyMembers', 'btnPanelNewLoan', 'inpBonusAdjPen', 'inpWinUsername', 'modalReceive', 'selPayMode', 'kpiCardLoans', 'dispTotalFund', 'searchBonusInput', 'inpSubFilterFrom', 'lblExitRd', 'selSortMembers', 'kpiCardBonus', 'confirmBody', 'btnTopBulk', 'lblExitResultType', 'lblExitPen', 'kpiCardMembers', 'bonusMemId', 'btnPrintLedgerPdf', 'btnSubmitBonusSetoff', 'modalLoan', 'tbodyBonusList', 'inpPayPenalty', 'selFinancialYear', 'modalBonusStatement', 'btnSubmitExit', 'inpNewMemMobile', 'inpNewMemRd', 'tabHead3', 'inpNewMemOpPen', 'tbodyLoans', 'tabPanel2', 'btnLoginFullscreen', 'lblBonusAmount', 'tbodyNpaList', 'inpLoanRate', 'selSortPayDate', 'boxSubInterest', 'btnNoticeOk', 'btnTopAddMember', 'lblReceiveModalHead', 'modalBonusOverview', 'dispPayDateFormatted', 'inpPayWaiver', 'inpLedgerFilterTo', 'inpBonusFilterTo', 'tabPanel4', 'modalExit', 'tabHead5', 'modalLedger', 'dispExitNetResult', 'selPayMember', 'lblExitLoan', 'inpSearchReceiveMember', 'inpLoanDate', 'btnSubmitLoan', 'lblRegTotalFund', 'inpLoanPrinc', 'lblBonusStmtTitle', 'btnToggleEye', 'editLoanId', 'inpNewMemName', 'tfootPenaltyTotal', 'lblBonusTargetMember', 'selBonusMode', 'selSortPen', 'selFilterLoanStatus', 'btnSubTabBonus', 'tabHead1', 'tbodyBonusSchedule', 'dispTotalMem', 'tbodyPenaltyList', 'lblDrilldownTitle', 'btnTopSettings', 'selFilterLoanType', 'inpLedgerFilterFrom', 'tabHead4', 'btnCloseDrilldown', 'noticeBody', 'lblBulkGrandTotal', 'tfootNpaTotal'];

function createElement(id) {
  return {
    id: id,
    value: (id.startsWith("sel") || id.startsWith("inp")) ? (id === "selFilterStatus" ? "ACTIVE" : (id === "selSortMembers" ? "name_az" : (id === "selFinancialYear" ? "2026" : "0"))) : "",
    innerText: "",
    innerHTML: "",
    style: { display: "block", borderColor: "" },
    classList: { add: ()=>{}, remove: ()=>{}, contains: ()=>false },
    addEventListener: function(event, cb) {
      // console.log("Added listener to", id, event);
    },
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

idList.forEach(id => {
  elements[id] = createElement(id);
});

global.window = global;
global.window.addEventListener = function(evt, cb){};

global.document = {
  readyState: "complete",
  getElementById: function(id) {
    if (!elements[id]) {
      // console.warn("Element NOT FOUND:", id);
      elements[id] = createElement(id); // return dummy so it might fail or warn
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
  console.log("SUCCESS! Script executed without throwing error!");
} catch (err) {
  console.error("CRASH ERROR:", err);
}


try {
  renderMembers();
  console.log("tbodyMembers.innerHTML length:", elements["tbodyMembers"].innerHTML.length);
  console.log("First 300 chars of tbodyMembers.innerHTML:", elements["tbodyMembers"].innerHTML.substring(0, 300));
} catch (e) {
  console.error("renderMembers CRASHED:", e);
}
