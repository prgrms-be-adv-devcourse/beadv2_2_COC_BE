package com.coc.modi.seller.settlement.batch;

public final class SettlementBatchContextKeys {

    private SettlementBatchContextKeys() {
    }

    public static final String SELLER_INDEX = "settlement.reader.sellerIndex";
    public static final String PAGE = "settlement.reader.page";
    public static final String LAST_CURSOR = "settlement.lastCursor";

    public static final String TOTAL_AMOUNT = "settlement.totalAmount";
    public static final String FEE_AMOUNT = "settlement.feeAmount";
    public static final String TOTAL_COUNT = "settlement.totalCount";
    public static final String SUCCESS_COUNT = "settlement.successCount";
    public static final String FAIL_COUNT = "settlement.failCount";
    public static final String SKIP_COUNT = "settlement.skipCount";
}
