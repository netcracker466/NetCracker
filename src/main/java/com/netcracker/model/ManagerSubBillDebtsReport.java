package com.netcracker.model;

import lombok.Data;

import java.util.List;

@Data
public class ManagerSubBillDebtsReport {
    private ManagerSubBill managerSubBull;
    private List<ManagerOperationSpending> managerOperationSpendings;
}
