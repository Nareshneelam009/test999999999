/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.db.paros;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.paros.ParosAbstractTable;
import org.zaproxy.zap.db.RecordAlertTag;
import org.zaproxy.zap.db.TableAlertTag;

public class ParosTableAlertTag extends ParosAbstractTable implements TableAlertTag {

    private PreparedStatement psReadByTagId;
    private PreparedStatement psReadByAlertIdTagKey;
    private PreparedStatement psInsertOrUpdate;
    private PreparedStatement psGetAllTags;
    private PreparedStatement psGetTagsByAlertId;
    private PreparedStatement psDeleteByTagId;
    private PreparedStatement psDeleteByAlertIdTagKey;
    private PreparedStatement psDeleteAllTagsForAlert;
    private PreparedStatement psDeleteAllTags;

    public ParosTableAlertTag() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            if (!DbUtils.hasTable(conn, "ALERT_TAG")) {
                // Need to create the table
                DbUtils.execute(
                        conn,
                        "CREATE CACHED TABLE alert_tag ("
                                + "tag_id bigint generated by default as identity (start with 1) primary key,"
                                + "alert_id bigint not null,"
                                + "key varchar(1024) default '' not null,"
                                + "value nvarchar(4000) default '' not null,"
                                + ")");
            }

            if (!DbUtils.hasIndex(conn, "ALERT_TAG", "ALERT_ID_INDEX")) {
                DbUtils.execute(conn, "CREATE INDEX alert_id_index ON alert_tag (alert_id)");
            }

            psReadByTagId = conn.prepareStatement("SELECT * FROM alert_tag WHERE tag_id = ?");
            psReadByAlertIdTagKey =
                    conn.prepareStatement("SELECT * FROM alert_tag WHERE alert_id = ? AND key = ?");
            psInsertOrUpdate =
                    conn.prepareStatement(
                            "MERGE INTO alert_tag AS tag "
                                    + "USING (VALUES(?, ?, ?)) AS v(aid, key, val) "
                                    + "ON tag.alert_id = v.aid AND tag.key = v.key "
                                    + "WHEN MATCHED AND tag.value <> v.val THEN UPDATE SET tag.value = v.val "
                                    + "WHEN NOT MATCHED THEN INSERT (alert_id, key, value) VALUES (v.aid, v.key, v.val)");
            psGetTagsByAlertId =
                    conn.prepareStatement("SELECT * FROM alert_tag WHERE alert_id = ?");
            psGetAllTags =
                    conn.prepareStatement("SELECT DISTINCT key, value FROM alert_tag ORDER BY key");
            psDeleteByTagId = conn.prepareStatement("DELETE FROM alert_tag WHERE tag_id = ?");
            psDeleteByAlertIdTagKey =
                    conn.prepareStatement("DELETE FROM alert_tag WHERE alert_id = ? AND key = ?");
            psDeleteAllTagsForAlert =
                    conn.prepareStatement("DELETE FROM alert_tag WHERE alert_id = ?");
            psDeleteAllTags = conn.prepareStatement("DELETE FROM alert_tag");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlertTag read(long tagId) throws DatabaseException {
        try {
            psReadByTagId.setLong(1, tagId);
            try (ResultSet rs = psReadByTagId.executeQuery()) {
                return build(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlertTag read(long alertId, String key) throws DatabaseException {
        try {
            psReadByAlertIdTagKey.setLong(1, alertId);
            psReadByAlertIdTagKey.setString(2, key);
            try (ResultSet rs = psReadByAlertIdTagKey.executeQuery()) {
                return build(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlertTag insertOrUpdate(long alertId, String key, String value)
            throws DatabaseException {
        try {
            psInsertOrUpdate.setLong(1, alertId);
            psInsertOrUpdate.setString(2, key);
            psInsertOrUpdate.setString(3, value);
            psInsertOrUpdate.executeUpdate();
            return read(alertId, key);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void delete(long tagId) throws DatabaseException {
        try {
            psDeleteByTagId.setLong(1, tagId);
            psDeleteByTagId.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void delete(long alertId, String key) throws DatabaseException {
        try {
            psDeleteByAlertIdTagKey.setLong(1, alertId);
            psDeleteByAlertIdTagKey.setString(2, key);
            psDeleteByAlertIdTagKey.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void deleteAllTagsForAlert(long alertId) throws DatabaseException {
        try {
            psDeleteAllTagsForAlert.setLong(1, alertId);
            psDeleteAllTagsForAlert.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized int deleteAllTags() throws DatabaseException {
        try {
            return psDeleteAllTags.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized Map<String, String> getTagsByAlertId(long alertId)
            throws DatabaseException {
        try {
            Map<String, String> result = new HashMap<>();
            psGetTagsByAlertId.setLong(1, alertId);
            try (ResultSet rs = psGetTagsByAlertId.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("key"), rs.getString("value"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized Map<String, String> getAllTags() throws DatabaseException {
        try {
            Map<String, String> result = new HashMap<>();
            try (ResultSet rs = psGetAllTags.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("key"), rs.getString("value"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized List<RecordAlertTag> getAllRecords() throws DatabaseException {
        try {
            List<RecordAlertTag> result = new ArrayList<>();
            try (ResultSet rs = psGetAllTags.executeQuery()) {
                RecordAlertTag rat;
                while ((rat = build(rs)) != null) {
                    result.add(rat);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private RecordAlertTag build(ResultSet rs) throws SQLException {
        RecordAlertTag rat = null;
        if (rs.next()) {
            rat =
                    new RecordAlertTag(
                            rs.getLong("tag_id"),
                            rs.getLong("alert_id"),
                            rs.getString("key"),
                            rs.getString("value"));
        }
        return rat;
    }
}
