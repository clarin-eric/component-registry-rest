package clarin.cmdi.componentregistry.impl.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ComponentRegistryTestDatabase {

    private ComponentRegistryTestDatabase() {
    }

    public static void resetDatabase(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
    }

    public static void createTablePersistentComponents(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE persistentcomponents ("
                + "id IDENTITY NOT NULL,"
                + "  user_id integer,"
                + "  content_id integer NOT NULL,"
                + "  is_public boolean NOT NULL,"
                + "  is_deleted boolean DEFAULT false NOT NULL,"
                + "  component_id VARCHAR(255) NOT NULL,"
                + "  name VARCHAR(255) NOT NULL,"
                + "  description VARCHAR(255) NOT NULL,"
                + "  registration_date timestamp,"// with timezone,"
                + "  href VARCHAR(255),"
                + "  creator_name VARCHAR(255),"
                + "  domain_name VARCHAR(255),"
                + "  group_name VARCHAR(255), "
                + "  show_in_editor boolean DEFAULT true NOT NULL, "
                + "  CONSTRAINT UNIQUE_PROFILE_ID UNIQUE (component_id));");
    }

    public static void createTableXmlContent(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE xml_content ("
                + "id IDENTITY NOT NULL, content VARCHAR(10240) NOT NULL);");
    }

    public static void createTableRegistryUser(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE registry_user ("
                + " id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) not null,"
                + " name VARCHAR(255),"
                + " principal_name VARCHAR(255));");
    }

    public static void createTableComments(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE comments ("
                + "id IDENTITY NOT NULL,"
                + "user_id integer,"
                + "  component_id VARCHAR(255),"               
                + "  comments VARCHAR(255) NOT NULL,"
                + "  comment_date timestamp,"
		+ "  user_name VARCHAR(255)"
                + "  );");
    }
    
    public static void createTableGroup(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE usergroup ("
                + "id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) not null,"
                + "ownerId integer NOT NULL,"
                + "  name VARCHAR(255) NOT NULL,"
                + "constraint usergroup_ID primary key (id)"
                + ");");
    }
    
    public static void createTableOwnership(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE ownership ("
                + "id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) not null,"
                + "componentId VARCHAR(255),"
                + "groupId int,"
                + "userId int,"
                + "constraint ownership_id primary key (id)"
                + ");");
    }
    
    public static void createTableGroupMembership(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE groupmembership ("
                + "id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) not null,"
                + "groupId integer,"
                + "userId integer,"
                + "constraint groupmembership_id primary key (id)"
                + ");");
    }

    public static void resetAndCreateAllTables(JdbcTemplate jdbcTemplate) {
        resetDatabase(jdbcTemplate);
        createTablePersistentComponents(jdbcTemplate);
        createTableXmlContent(jdbcTemplate);
        createTableRegistryUser(jdbcTemplate);
        createTableComments(jdbcTemplate);
        createTableGroup(jdbcTemplate);
        createTableOwnership(jdbcTemplate);
        createTableGroupMembership(jdbcTemplate);
    }
}
