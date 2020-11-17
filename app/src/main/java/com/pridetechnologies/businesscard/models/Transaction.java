package com.pridetechnologies.businesscard.models;

public class Transaction {
    private int id, status, type, currencyIsoCode, amount, merchantAccountId, subMerchantAccountId;
    private String masterMerchantAccountId, orderId,createAt,updateAt;

    public Transaction()
    {

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(int currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMerchantAccountId() {
        return merchantAccountId;
    }

    public void setMerchantAccountId(int merchantAccountId) {
        this.merchantAccountId = merchantAccountId;
    }

    public int getSubMerchantAccountId() {
        return subMerchantAccountId;
    }

    public void setSubMerchantAccountId(int subMerchantAccountId) {
        this.subMerchantAccountId = subMerchantAccountId;
    }

    public String getMasterMerchantAccountId() {
        return masterMerchantAccountId;
    }

    public void setMasterMerchantAccountId(String masterMerchantAccountId) {
        this.masterMerchantAccountId = masterMerchantAccountId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }
}
