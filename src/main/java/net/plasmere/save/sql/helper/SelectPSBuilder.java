package net.plasmere.save.sql.helper;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectPSBuilder extends PSBuilder<ResultSet> {
    private HashMap<String, PSPredicate> predicates = new HashMap<>();

    public SelectPSBuilder(Connection conn, String baseQuery, String querySuffix) {
        super(conn, baseQuery, querySuffix);
    }

    public SelectPSBuilder(Connection conn, String baseQuery) {
        super(conn, baseQuery);
    }

    public void addPredicate(SQLType type, String name, String predicate, int numArgs) {
        predicates.put(name, new PSPredicate(predicate, numArgs, type));
    }

    public void addPredicate(SQLType type, String name, String predicate) {
        this.addPredicate(type, name, predicate, (int) predicate.chars().filter(c -> c == '?').count());
    }

    @Override
    PreparedStatement createStatement() throws SQLException {
        StringBuilder query = new StringBuilder(baseQuery);
        query.append(" WHERE ");
        boolean first = true;

        for (String param : params()) {
            PSPredicate predicate = predicates.get(param);
            if (first) {
                first = false;
            } else {
                query.append(" AND ");
            }
            query.append("((").append(predicate.predicate).append(") OR ?)");
        }
        query.append(' ').append(querySuffix);
        return conn.prepareStatement(query.toString());
    }

    @Override
    public SelectRunner createRunner() {
        return new SelectRunner();
    }

    @Override
    protected ArrayList<String> params() {
        ArrayList<String> params = new ArrayList<>(predicates.keySet());
        params.sort(String::compareTo);
        return params;
    }

    public class SelectRunner extends Runner<ResultSet> {
        @Override
        public ResultSet execute() throws SQLException { // TODO: thread safety
            int i = 1; // 1-indexed, ugh
            ps.clearParameters();
            for (String param : params()) {
                PSPredicate predicate = predicates.get(param);
                Object[] args = paramValues.get(param);
                for (int j = 0; j < predicate.numArgs; j++) {
                    if (args != null) {
                        ps.setObject(i, args[j % args.length], predicate.type.getVendorTypeNumber());
                    } else {
                        setDefaultVal(ps, i, predicate.type);
                    }
                    i++;
                }
                ps.setBoolean(i, args == null);
                i++;
            }
            return ps.executeQuery();
        }
    }

    private static class PSPredicate {
        protected String predicate;
        protected int numArgs;
        protected SQLType type;

        public PSPredicate(String predicate, int numArgs, SQLType type) {
            this.predicate = predicate;
            this.numArgs = numArgs;
            this.type = type;
        }
    }
}
