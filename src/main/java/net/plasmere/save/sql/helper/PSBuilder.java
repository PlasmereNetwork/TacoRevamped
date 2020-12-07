package net.plasmere.save.sql.helper;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class PSBuilder<T> {
    protected Connection conn;
    protected String baseQuery;
    protected String querySuffix;
    protected PreparedStatement ps;

    public PSBuilder(Connection conn, String baseQuery, String querySuffix) {
        this.conn = conn;
        this.baseQuery = baseQuery;
        this.querySuffix = querySuffix;
    }

    public PSBuilder(Connection conn, String baseQuery) {
        this(conn, baseQuery, "");
    }

    public final void prepare() throws SQLException {
        this.ps = createStatement();
    }

    protected abstract ArrayList<String> params();

    abstract PreparedStatement createStatement() throws SQLException;

    public abstract Runner<T> createRunner();

    public final PreparedStatement getStatement() {
        return ps;
    }

    public abstract static class Runner<T> {
        protected HashMap<String, Object[]> paramValues = new HashMap<>();

        public void fillParameter (String name, Object... value) {
            this.paramValues.put(name, value);
        }

        protected static void setDefaultVal(PreparedStatement ps, int i, SQLType type) throws SQLException {
            // ps.setNull(i, type.getVendorTypeNumber());
            switch (type.getVendorTypeNumber()) {
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.VARCHAR:
                    ps.setString(i, "");
                    break;
                case Types.ARRAY:
                    ps.setObject(i, new Object[]{});
                    break;
                default:
                    ps.setNull(i, type.getVendorTypeNumber());
                    break;
            }
        }

        public abstract T execute() throws SQLException;
    }
}
