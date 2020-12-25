package com.netcracker.services;

import com.netcracker.dao.ManagerSubBillDao;
import com.netcracker.exception.InsufficientBalanceException;
import com.netcracker.models.*;
import com.netcracker.models.PojoBuilder.ManagerSubBillBuilder;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Collection;

@Log4j
@Service
@Transactional
public class ManagerSubBillService {

    private final ManagerSubBillDao managerSubBillDao;
    private final ManagerInfoService managerInfoService;
    private final ManagerOperationSpendingService managerOperationSpendingService;
    private final DebtPaymentOperationService debtPaymentOperationService;

    @Autowired
    public ManagerSubBillService(ManagerSubBillDao managerSubBillDao, ManagerInfoService managerInfoService,
                                 ManagerOperationSpendingService managerOperationSpendingService,
                                 DebtPaymentOperationService debtPaymentOperationService) {
        this.managerSubBillDao = managerSubBillDao;
        this.managerInfoService = managerInfoService;
        this.managerOperationSpendingService = managerOperationSpendingService;
        this.debtPaymentOperationService = debtPaymentOperationService;
    }


    public Collection<ManagerSubBill> getAllManagerSubBills() {
        try {
            Collection<ManagerSubBill> managerSubBills = managerSubBillDao.getAllManagerSubBills();
            for (ManagerSubBill managerSubBill : managerSubBills) {
                BigInteger managerSubBillId = managerSubBill.getSubBillId();
                managerSubBill.setManagerSpendingOperations(managerOperationSpendingService.getAllManagerOperationBySubBillId(managerSubBillId));
                managerSubBill.setDebtPaymentOperations(debtPaymentOperationService.getDebtPaymentOperationsByManagerSubBillId(managerSubBillId));
 }
            return managerSubBills;
        } catch (NullPointerException e) {
            log.error("IN Service method getManagerSubBillByCommunalUtilityId: " + e.getMessage());
            throw e;
        }
    }


    public ManagerSubBill getManagerSubBill(BigInteger managerSubBillId) {
        try {
            ManagerSubBill managerSubBill = managerSubBillDao.getManagerSubBillById(managerSubBillId);
            managerSubBill.setManagerSpendingOperations(managerOperationSpendingService.getAllManagerOperationBySubBillId(managerSubBillId));
            managerSubBill.setDebtPaymentOperations(debtPaymentOperationService.getDebtPaymentOperationsByManagerSubBillId(managerSubBillId));

            return managerSubBill;
        } catch (NullPointerException e) {
            log.error("IN Service method getManagerSubBillByCommunalUtilityId: " + e.getMessage());
            throw e;
        }
    }

    public ManagerSubBill getManagerSubBillByCommunalUtilityId(BigInteger communalUtilityId) {
        try {
            ManagerSubBill managerSubBill = managerSubBillDao.getManagerSubBillByCommunalUtilityId(communalUtilityId);
            BigInteger managerSubBillId = managerSubBill.getSubBillId();
            managerSubBill.setManagerSpendingOperations(managerOperationSpendingService.getAllManagerOperationBySubBillId(managerSubBillId));
            managerSubBill.setDebtPaymentOperations(debtPaymentOperationService.getDebtPaymentOperationsByManagerSubBillId(managerSubBillId));

            return managerSubBill;
        } catch (NullPointerException e) {
            log.error("IN Service method getManagerSubBillByCommunalUtilityId: " + e.getMessage());
            throw e;
        }
    }

    public void createManagerSubBill(CommunalUtility communalUtility) {
        try {
            Manager manager = managerInfoService.getManager();

            managerSubBillDao.createManagerSubBill(new ManagerSubBillBuilder()
                    .withManager(manager)
                    .withCommunalUtility(communalUtility)
                    .build());
        } catch (NullPointerException e) {
            log.error("IN Service method createManagerSubBill: " + e.getMessage());
            throw e;
        }
    }

    public void updateManagerSubBill(ManagerSubBill managerSubBill) {
        try {
            managerSubBillDao.updateManagerSubBill(managerSubBill);
        } catch (NullPointerException e) {
            log.error("IN Service method updateManagerSubBill: " + e.getMessage());
            throw e;
        }
    }

    public void updateManagerSubBillByManagerOperation(ManagerSpendingOperation managerSpendingOperation) {
        try {
            ManagerSubBill managerSubBill = managerSubBillDao.getManagerSubBillById(managerSpendingOperation.getManagerSubBill().getSubBillId());

            if (managerSubBill.getBalance() >= managerSpendingOperation.getSum()) {
                managerSubBill.setBalance(managerSubBill.getBalance() - managerSpendingOperation.getSum());
                managerSubBillDao.updateManagerSubBill(managerSubBill);
            } else {
                InsufficientBalanceException balanceException = new InsufficientBalanceException("Insufficient funds on the balance sheet");
                log.error("IN Service method updateManagerSubBillByManagerOperation: " + balanceException.getMessage());
                throw balanceException;
            }

        } catch (NullPointerException e) {
            log.error("IN Service method updateManagerSubBillByManagerOperation: " + e.getMessage());
            throw e;
        }
    }

    public void updateManagerSubBillByDeptPaymentOperationService(DebtPaymentOperation debtPaymentOperation) {
        try {
            ManagerSubBill managerSubBill = managerSubBillDao.getManagerSubBillById(debtPaymentOperation.getManagerSubBill().getSubBillId());

            managerSubBill.setBalance(managerSubBill.getBalance() + debtPaymentOperation.getSum());
            managerSubBillDao.updateManagerSubBill(managerSubBill);
        } catch (NullPointerException e) {
            log.error("IN Service method updateManagerSubBillByDeptPaymentOperationService: " + e.getMessage());
            throw e;
        }
    }

}


