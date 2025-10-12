package com.daox.online.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class JsonMapTypeHandler extends BaseTypeHandler<Map<String, BigDecimal>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, BigDecimal> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to convert Map to JSON string", e);
        }
    }

    @Override
    public Map<String, BigDecimal> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, BigDecimal>>() {});
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSON string to Map<String, BigDecimal>", e);
        }
    }

    @Override
    public Map<String, BigDecimal> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, BigDecimal>>() {});
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSON string to Map<String, BigDecimal>", e);
        }
    }

    @Override
    public Map<String, BigDecimal> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, BigDecimal>>() {});
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSON string to Map<String, BigDecimal>", e);
        }
    }
}
