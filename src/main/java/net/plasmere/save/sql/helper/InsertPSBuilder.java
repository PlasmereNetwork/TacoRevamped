package net.plasmere.save.sql.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class InsertPSBuilder extends PSBuilder<Boolean> {
    private HashMap<String, String[]> fillables = new HashMap<>();

    public InsertPSBuilder(Connection conn, String baseQuery, String querySuffix) {
        super(conn, baseQuery, querySuffix);
    }

    public InsertPSBuilder(Connection conn, String baseQuery) {
        super(conn, baseQuery);
    }

    @Override
    protected ArrayList<String> params() {
        ArrayList<String> params = new ArrayList<>(this.fillables.keySet());
        params.sort(String::compareTo);
        return params;
    }

    @Override
    PreparedStatement createStatement() throws SQLException {
        StringBuilder query = new StringBuilder(baseQuery);
        query.append(" (");
        boolean first = true;

        for (String fillable : params()) {
            String[] columns = this.fillables.get(fillable);
            for (String column : columns) {
                if (first) {
                    first = false;
                } else {
                    query.append(", ");
                }
                query.append(column);
            }
        }
        query.append(") VALUES (");

        first = true;
        for (String fillable : params()) {
            int numColumns = this.fillables.get(fillable).length;
            for (int i = 0; i < numColumns; i++) {
                if (first) {
                    first = false;
                } else {
                    query.append(", ");
                }
                query.append('?');
            }
        }

        query.append(") ").append(querySuffix);

        return conn.prepareStatement(query.toString());
    }

    @Override
    public InsertRunner createRunner() {
        return new InsertRunner();
    }

    public void addFillable(String name, String... params) {
        this.fillables.put(name, params);
    }

    public void addFillable(String name) {
        this.addFillable(name, name);
    }

    public class InsertRunner extends Runner<Boolean> {
        @Override
        public Boolean execute() throws SQLException { // TODO: thread safety
            int i = 1; // 1-indexed, ugh
            ps.clearParameters();
            for (String fillable : params()) {
                String[] columns = fillables.get(fillable);
                Object[] args = paramValues.get(fillable);
                for (int j = 0; j < columns.length; j++) {
                    if (args == null) {
                        throw new SQLException("SQL INSERT: Not all parameters filled");
                    }
                    ps.setObject(i, args[j % args.length]);
                    i++;
                }
            }
            return ps.execute();
        }
    }
}
