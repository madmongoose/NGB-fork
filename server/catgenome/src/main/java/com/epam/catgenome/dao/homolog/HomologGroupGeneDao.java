/*
 * MIT License
 *
 * Copyright (c) 2021 EPAM Systems
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.epam.catgenome.dao.homolog;

import com.epam.catgenome.dao.DaoHelper;
import com.epam.catgenome.entity.externaldb.homolog.HomologGroupGene;
import com.epam.catgenome.util.db.QueryParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.epam.catgenome.util.Utils.addParametersToQuery;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomologGroupGeneDao extends NamedParameterJdbcDaoSupport {

    @Autowired
    private DaoHelper daoHelper;
    private String sequenceName;
    private String insertQuery;
    private String deleteQuery;
    private String loadQuery;
    private String totalCountQuery;

    /**
     * Persists a new Homolog group gene record.
     * @param gene {@code HomologGroupGene} a Homolog group gene to persist.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void save(final HomologGroupGene gene) {
        long newId = daoHelper.createId(sequenceName);
        getNamedParameterJdbcTemplate().update(insertQuery, GroupGeneParameters.getParameters(newId, gene));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void save(final List<HomologGroupGene> genes) {
        if (!CollectionUtils.isEmpty(genes)) {
            List<Long> newIds = daoHelper.createIds(sequenceName, genes.size());
            List<MapSqlParameterSource> params = new ArrayList<>(genes.size());
            for (int i = 0; i < genes.size(); i++) {
                MapSqlParameterSource param = GroupGeneParameters.getParameters(newIds.get(i), genes.get(i));
                params.add(param);
            }
            getNamedParameterJdbcTemplate().batchUpdate(insertQuery,
                    params.toArray(new MapSqlParameterSource[genes.size()]));
        }
    }

    /**
     * Deletes Homolog groups genes from the database
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final Long id) {
        getJdbcTemplate().update(deleteQuery, id);
    }

    /**
     * Loads {@code Homolog groups gene} from a database by parameters.
     * @param queryParameters {@code QueryParameters} query parameters
     * @return a {@code List<HomologGroupGene>} from the database
     */
    public List<HomologGroupGene> load(final QueryParameters queryParameters) {
        String query = addParametersToQuery(loadQuery, queryParameters);
        return getJdbcTemplate().query(query, GroupGeneParameters.getRowMapper());
    }

    enum GroupGeneParameters {
        ID,
        GROUP_ID,
        GENE_ID,
        TAX_ID,
        DATABASE_ID;

        static MapSqlParameterSource getParameters(final long id, final HomologGroupGene gene) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue(ID.name(), id);
            params.addValue(GROUP_ID.name(), gene.getGroupId());
            params.addValue(GENE_ID.name(), gene.getGeneId());
            params.addValue(TAX_ID.name(), gene.getTaxId());
            params.addValue(DATABASE_ID.name(), gene.getDatabaseId());
            return params;
        }

        static RowMapper<HomologGroupGene> getRowMapper() {
            return (rs, rowNum) -> parseGroupGene(rs);
        }

        static HomologGroupGene parseGroupGene(final ResultSet rs) throws SQLException {
            return HomologGroupGene.builder()
                    .id(rs.getLong(ID.name()))
                    .groupId(rs.getLong(GROUP_ID.name()))
                    .geneId(rs.getLong(GENE_ID.name()))
                    .taxId(rs.getLong(TAX_ID.name()))
                    .databaseId(rs.getLong(DATABASE_ID.name()))
                    .build();
        }
    }
}
