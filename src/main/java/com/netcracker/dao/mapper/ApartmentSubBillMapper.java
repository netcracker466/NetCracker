package com.netcracker.dao.mapper;

import com.netcracker.models.ApartmentSubBill;
import com.netcracker.models.CalculationMethod;
import com.netcracker.models.CommunalUtility;
import com.netcracker.models.PojoBuilder.ApartmentBuilder;
import com.netcracker.models.PojoBuilder.ApartmentSubBillBuilder;

import com.netcracker.models.PojoBuilder.CommunalUtilityBuilder;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApartmentSubBillMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        ApartmentSubBill apartmentSubBill = new ApartmentSubBillBuilder()
                .withSubBillId(new BigInteger(resultSet.getString("sub_bill_id")))
                .withBalance(Double.valueOf(resultSet.getString("balance")))
                .withDept(Double.valueOf(resultSet.getString("debt")))
                .withApartment(new ApartmentBuilder()
                        .withAccountId(new BigInteger(resultSet.getString("account_id")))
                        .build())
                .withCommunalUtility(new CommunalUtilityBuilder()
                        .withCommunalUtilityId(new BigInteger(resultSet.getString("communal_util_id")))
                        .withName(resultSet.getString("communal_name"))
                        .withDurationType(CommunalUtility.Duration.valueOf(resultSet.getString("duration_type")))
                        .withStatus(CommunalUtility.Status.valueOf(resultSet.getString("status")))
                        .withDeadline(Date.valueOf(resultSet.getString("dead_line")))
                        .withCalculationMethod(new CalculationMethod(
                                new BigInteger(resultSet.getString("calc_method_id")),
                                resultSet.getString("calc_name")
                        ))
                        .build())
                .build();
        return apartmentSubBill;
    }
}
