package org.igye.sqlexercises.newclasses;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class QueryExecutor {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public synchronized Pair<ResultSetDto, ResultSetDto> executeQueriesOnExampleData(
            String schemaId, List<String> data, String expectedQuery, String actualQuery) throws IOException, SQLException {
        ScriptUtils.executeSqlScript(
                jdbcTemplate.getDataSource().getConnection(),
                new ClassPathResource(getDdlPath(schemaId))
        );
        for (String insert : data) {
            jdbcTemplate.execute(insert);
        }

        return Pair.of(executeQuery(expectedQuery), actualQuery!=null?executeQuery(actualQuery):null);
    }

    private ResultSetDto executeQuery(String query) {
        ResultSetDto res = new ResultSetDto();
        res.setColNames(new ArrayList<>());
        jdbcTemplate.query(query, (RowMapper<Void>) (resultSet, i) -> {
            if (res.getColNames().isEmpty()) {
                int colCnt = resultSet.getMetaData().getColumnCount();
                for (int c = 1; c <= colCnt; c++) {
                    res.getColNames().add(resultSet.getMetaData().getColumnName(c));
                }
            }
            return null;
        });
        res.setData(jdbcTemplate.queryForList(query));
        return res;
    }

    public String getDdlPath(String schemaId) {
        return "schemas/" + schemaId + ".sql";
    }
}