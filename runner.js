
const ids = ['inpSearchLoanMember', 'noticeBody', 'modalReceive', 'btnSubmitLoan', 'tfootPenaltyTotal', 'btnPanelNewReceipt', 'btnTopBulk', 'dispTotalNpa', 'searchPayInput', 'inpBonusAdjInt', 'lblDrilldownTitle', 'tbodyBonusList', 'inpNewMemName', 'modalBonusSetoff', 'lblBonusTargetMember', 'tabHead5', 'tbodyLoans', 'searchPenInput', 'lblMemberModalHead', 'selSortPayDate', 'inpBonusNetPaid', 'memberFilterInput', 'selSortPen', 'dispPayDateFormatted', 'btnApplyNpaFilter', 'inpGlobalDueDay', 'modalLoan', 'inpExitNpa', 'selFilterPayMode', 'ledgerHeaderStats', 'lblRegCashBal', 'winForgotCard', 'btnSubmitBonusSetoff', 'btnSubmitExit', 'btnConfirmProceed', 'inpNewMemOpLoan', 'winLoginError', 'chkSelectAllBulk', 'lblBonusStmtTitle', 'modalBulk', 'inpNewMemBal', 'modalFund', 'btnBulkSetAllCash', 'btnAddBulkRow', 'inpPenFilterFrom', 'inpBonusAdjRd', 'inpLedgerFilterFrom', 'printableBonusArea', 'inpLedgerFilterTo', 'lblExitLoan', 'btnSubmitReceive', 'lblReceiveModalHead', 'dispExitNetResult', 'tfootMembersTotal', 'modalMember', 'inpPayPrincipal', 'boxSubInterest', 'tbodyBonusSchedule', 'modalNpa', 'btnTopReload', 'formWinLogin', 'kpiCardMembers', 'inpPayFilterFrom', 'dispTotalBonus', 'tabPanel5', 'inpLoanPrinc', 'printableLedgerArea', 'inpFundFrom', 'btnToggleEye', 'tbodyDrilldown', 'tbodyPayments', 'lblMemberLoanLimit', 'tfootBonusTotal', 'inpBulkDate', 'tbodyNpaList', 'inpSearchReceiveMember', 'inpNewMemStatus', 'inpLoanRate', 'btnApplySubFilter', 'inpNewMemCustomLimit', 'editLoanId', 'modalBonusOverview', 'inpExitNetRefund', 'tbodyMembers', 'btnTopAddMember', 'inpNpaFilterTo', 'tabHead3', 'btnCloseDrilldown', 'btnApplyFundDate', 'inpNewMemNominee', 'btnPanelNewLoan', 'btnPrintBonusPdf', 'dispTotalLoan', 'tfootLoansTotal', 'lblLedgerName', 'inpGlobalRate', 'lblLoanModalHead', 'inpWinPassword', 'selSortBonus', 'inpNewMemJoinDate', 'modalSettings', 'selPayMode', 'tabPanel4', 'btnLoginFullscreen', 'inpPayPenalty', 'dispBulkDateFormatted', 'selSortMembers', 'inpSubFilterTo', 'boxSubBonus', 'dispTotalMem', 'inpNewMemRd', 'selPayMember', 'tabHead1', 'dispTotalFund', 'selFilterLoanStatus', 'inpBonusAdjPen', 'inpBonusFilterFrom', 'chkExitIncludeBonus', 'tabPanel3', 'inpNewMemDueDay', 'selBonusMode', 'btnApplyGlobalSettings', 'inpBonusFilterTo', 'lblRegBankBal', 'inpLoanNarration', 'inpLoanDate', 'tbodyLedgerTxns', 'inpWinUsername', 'tabHead2', 'btnWinLogin', 'searchLoanInput', 'inpNewMemAddress', 'inpPayDate', 'inpLoanFilterTo', 'btnSubTabInterest', 'confirmHeader', 'lblBulkGrandTotal', 'tabPanel2', 'btnTopSettings', 'lblExitBonus', 'tabPanel1', 'selExitMember', 'btnSubmitMember', 'bonusStmtHeaderStats', 'tbodySubBonus', 'lblBonusAmount', 'inpPayWaiver', 'kpiCardRd', 'inpFundTo', 'selFilterLoanType', 'selFilterPenStatus', 'tbodyBulkList', 'btnPrintLedgerPdf', 'kpiCardBonus', 'lblExitResultType', 'inpSubFilterFrom', 'inpBonusDate', 'editReceiptNo', 'tbodyFundMonths', 'inpPenFilterTo', 'dispTotalRd', 'lblExitPen', 'inpPayNarration', 'selFilterStatus', 'inpNpaFilterFrom', 'btnSubmitBulk', 'tfootNpaTotal', 'confirmBody', 'btnBulkSetAllOnline', 'tabHead4', 'btnTopReceive', 'inpNewMemOpPen', 'tfootPaymentsTotal', 'modalConfirm', 'inpBonusAdjLoan', 'inpExitWaiver', 'dispLoanDateFormatted', 'btnNoticeOk', 'selLoanType', 'kpiCardNpa', 'noticeHeader', 'lblExitRd', 'tbodySubInterest', 'inpLoanFilterFrom', 'modalNotice', 'modalBonusStatement', 'btnTopLoan', 'lblRegTotalFund', 'inpPayInterest', 'inpPayFilterTo', 'selFilterBonusStatus', 'bonusMemId', 'editMemId', 'modalExit', 'btnSubTabBonus', 'fundDrilldownBox', 'windowsLoginOverlay', 'kpiCardLoans', 'selFinancialYear', 'inpPayRd', 'inpNewMemMobile', 'kpiCardFund', 'btnTopExit', 'selLoanMember', 'tbodyPenaltyList', 'modalLedger', 'inpNewMemOpInt', 'searchBonusInput', 'lblBulkSelectedCount'];
const elements = {};

function makeEl(id) {
  return {
    id: id,
    style: { display: "" },
    classList: {
      contains: () => false,
      add: () => {},
      remove: () => {}
    },
    value: "",
    innerText: "",
    innerHTML: "",
    addEventListener: (ev, cb) => {},
    querySelectorAll: () => [],
    querySelector: () => null,
    setAttribute: () => {},
    getAttribute: () => "",
    focus: () => {},
    appendChild: () => {},
    closest: () => null
  };
}

for (const id of ids) {
  elements[id] = makeEl(id);
}

global.window = global;
global.window.addEventListener = (ev, cb) => {};
global.document = {
  readyState: "complete",
  getElementById: (id) => elements[id] || null,
  querySelectorAll: (sel) => [],
  querySelector: (sel) => null,
  addEventListener: (ev, cb) => {},
  body: makeEl("body"),
  documentElement: makeEl("documentElement")
};
global.sessionStorage = {
  getItem: () => null,
  setItem: () => {},
  removeItem: () => {}
};
global.localStorage = {
  getItem: () => null,
  setItem: () => {},
  removeItem: () => {},
  clear: () => {}
};
global.google = {
  script: {
    run: {
      withSuccessHandler: function() { return this; },
      withFailureHandler: function() { return this; },
      getSocietyFullData: function() {}
    }
  }
};

try {
  require("./script_test_2.js");
  console.log("SUCCESS! script_test_2 executed without throwing error!");
  console.log("typeof window.refreshAll:", typeof window.refreshAll);
  if (typeof window.refreshAll === "function") {
    window.refreshAll();
    console.log("refreshAll executed successfully!");
  }
} catch(e) {
  console.error("RUNTIME ERROR in script_test_2:", e);
}
