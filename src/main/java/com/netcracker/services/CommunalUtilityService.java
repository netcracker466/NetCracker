package com.netcracker.services;

import com.netcracker.dao.CalculationMethodDao;
import com.netcracker.dao.CommunalUtilityDao;
import com.netcracker.exception.DaoAccessException;
import com.netcracker.exception.ErrorCodes;
import com.netcracker.models.CalculationMethod;
import com.netcracker.models.CommunalUtility;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Service
@Transactional
@Log4j
public class CommunalUtilityService {
    @Autowired
    private CommunalUtilityDao communalUtilityDao;
    @Autowired
    private  CalculationMethodDao calculationMethodDao;
    @Autowired
    private  NotificationService notificationService;

//    @Autowired
//    public CommunalUtilityService(CommunalUtilityDao communalUtilityDao, CalculationMethodDao calculationMethodDao, NotificationService notificationService) {
//        this.communalUtilityDao = communalUtilityDao;
//        this.calculationMethodDao = calculationMethodDao;
//        this.notificationService = notificationService;
//    }

    public List<CommunalUtility> getAllCommunalUtilities(CommunalUtility.Status status) throws DaoAccessException, NullPointerException {
        try {
            List<CommunalUtility> utilities;
            if (status != null) {
                utilities = communalUtilityDao.getAllCommunalUtilitiesFilterByStatus(status);
            } else {
                utilities = communalUtilityDao.getAllCommunalUtilities();
            }
            for (CommunalUtility utility : utilities
            ) {
                try {
                    utility.setCalculationMethod(calculationMethodDao.
                            getCalculationMethodByCommunalUtilityId(utility.getCommunalUtilityId()));
                }catch (DaoAccessException ignored){

                }
            }
            return utilities;
        } catch (DaoAccessException e) {
            log.error("CommunalUtilityService method getAllCommunalUtilities(): " + e.getMessage(), e);
            throw e;
        }
    }

    public List<CommunalUtility> getAllCommunalUtilities() throws DaoAccessException, NullPointerException {
        return getAllCommunalUtilities(null);
    }

    public CommunalUtility getCommunalUtilityById(BigInteger id) {
        try {
            try {
                return communalUtilityDao.
                        getCommunalUtilityByIdWithCalculationMethod(id);
            }catch (DaoAccessException ignored){}
            return communalUtilityDao.getCommunalUtilityById(id);
        } catch (DaoAccessException e) {
            log.error("CommunalUtilityService method getCommunalUtilityById(): " + e.getMessage(), e);
            throw e;
        }
    }

    public void createCalculationMethod(CalculationMethod calculationMethod){
        calculationMethodDao.createCalculationMethod(calculationMethod);
    }

    public List<CalculationMethod> getAllCalculationMethods(){
            return calculationMethodDao.getAllCalculationMethods();
    }

    public void updateCalculationMethod(CalculationMethod calculationMethod){
        calculationMethodDao.updateCalculationMethod(calculationMethod);
    }
    public void deleteCalculationMethod(BigInteger id){
        try {
            calculationMethodDao.getCalculationMethodById(id);
        }catch (DaoAccessException e){
            log.error(e.getMessage(),e);
            throw e;
        }
        try {
            List<CommunalUtility> communalUtilities = communalUtilityDao.getAllCommunalUtilitiesByCalculationMethodId(id);
            for (CommunalUtility utility: communalUtilities
                 ) {
                utility.setStatus(CommunalUtility.Status.Disabled);
                updateCommunalUtility(utility);
            }
        }catch (DaoAccessException ignored){} //no referenced Calculation methods
        calculationMethodDao.deleteCalculationMethod(id);
    }

    public void createCommunalUtility(CommunalUtility communalUtility)
            throws DaoAccessException, NullPointerException, IOException, MessagingException {
        try {
            try {
                communalUtilityDao.getUniqueCommunalUtility(communalUtility);
                IllegalArgumentException exception = new IllegalArgumentException("Communal utility with such name already exists");
                log.error(exception.getMessage(),exception);
                throw exception;
            }
            catch (DaoAccessException ignored){
            }
            CalculationMethod calculationMethod=null;
            if (communalUtility.getCalculationMethod() == null){
                if(communalUtility.getStatus().equals(CommunalUtility.Status.Enabled)) {
                    IllegalArgumentException exception = new IllegalArgumentException("Communal utility can not be Enabled when calculation method is not declared");
                    log.error(exception.getMessage(),exception);
                    throw exception;
                }
                communalUtilityDao.createCommunalUtility(communalUtility);
            }else {
                try {
                    calculationMethod = calculationMethodDao.getCalculationMethodById(
                            communalUtility.getCalculationMethod().getCalculationMethodId());
                } catch (DaoAccessException ex) {
                    IllegalArgumentException exception = new IllegalArgumentException("Calculation method doesn't exist");
                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
                communalUtilityDao.createCommunalUtilityWithRef(communalUtility);
            }
            CommunalUtility comUtil = communalUtilityDao.getUniqueCommunalUtility(communalUtility);
            comUtil.setCalculationMethod(calculationMethod);
            if (comUtil.getDurationType().equals(CommunalUtility.Duration.Temporary)) {
                notificationService.sendTempCommunalUtilityNotificationToAllApartments(communalUtility);
            }
        } catch (NullPointerException | IOException | MessagingException e) {
            log.error("CommunalUtilityService method createCommunalUtility(): " + e.getMessage(), e);
            throw e;
        }
    }

    public void updateCommunalUtility(CommunalUtility communalUtility) throws DaoAccessException, NullPointerException {
        try {
            if (communalUtility.getCalculationMethod()==null){
                if (communalUtilityDao.getCommunalUtilityById(
                        communalUtility.getCommunalUtilityId()).equals(communalUtility))
                    throw new DaoAccessException("update is the same as existed communal utility");
                try{
                   calculationMethodDao.getCalculationMethodByCommunalUtilityId
                           (communalUtility.getCommunalUtilityId());
                }catch (DaoAccessException exception){
                    if (communalUtility.getStatus()== CommunalUtility.Status.Enabled){
                        IllegalArgumentException ex = new IllegalArgumentException("Status can not be Enabled");
                        log.error(ex.getMessage(),ex);
                        throw ex;
                    }
                }
            }else {
                if (communalUtilityDao.getCommunalUtilityByIdWithCalculationMethod(
                        communalUtility.getCommunalUtilityId()).equals(communalUtility))
                    throw new DaoAccessException("update is the same as existed communal utility");
                try{
                    calculationMethodDao.getCalculationMethodById(communalUtility
                            .getCalculationMethod().getCalculationMethodId());
                }catch (DaoAccessException exception){
                    log.error(exception.getMessage(),exception);
                    throw exception;
                }
            }
            communalUtilityDao.updateCommunalUtility(communalUtility);
        } catch (DaoAccessException e) {
            log.error("CommunalUtilityService method updateCommunalUtility(): " + e.getMessage(), e);
            throw e;
        }
    }

}
