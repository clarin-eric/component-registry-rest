package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.UserMapping.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class UserDao extends ComponentRegistryDao<User> {

    private final static String SELECT_BASE = "SELECT " + COLUMN_ID + ", name, principal_name FROM " + TABLE_REGISTRY_USER;

    public List<User> getAllUsers() throws DataAccessException {
	return getList(SELECT_BASE);
    }

    public User getByPrincipalName(String principalName) throws DataAccessException {
	return getFirstOrNull(SELECT_BASE + " WHERE principal_name = ?", principalName);
    }

    public User getById(Number id) throws DataAccessException {
	return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", id);
    }

    public Number insertUser(User user) throws DataAccessException {
	SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).
		withTableName(TABLE_REGISTRY_USER).usingGeneratedKeyColumns(
		COLUMN_ID);

	Map<String, Object> params = new HashMap<String, Object>();
	params.put("name", user.getName());
	params.put("principal_name", user.getPrincipalName());
	return insert.executeAndReturnKey(params);
    }

    @Override
    protected ParameterizedRowMapper<User> getRowMapper() {
	return rowMapper;
    }
    private final ParameterizedRowMapper<User> rowMapper = new ParameterizedRowMapper<User>() {

	@Override
	public User mapRow(ResultSet rs, int rowNumber) throws SQLException {
	    User user = new User();
	    user.setId(rs.getInt(COLUMN_ID));
	    user.setName(rs.getString("name"));
	    user.setPrincipalName(rs.getString("principal_name"));
	    return user;
	}
    };
}