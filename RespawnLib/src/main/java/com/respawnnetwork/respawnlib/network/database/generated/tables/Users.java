/**
 * This class is generated by jOOQ
 */
package com.respawnnetwork.respawnlib.network.database.generated.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Users extends org.jooq.impl.TableImpl<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord> {

	private static final long serialVersionUID = 284474546;

	/**
	 * The singleton instance of <code>respawn.users</code>
	 */
	public static final Users USERS = new Users();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord> getRecordType() {
		return com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord.class;
	}

	/**
	 * The column <code>respawn.users.uuid</code>.
	 */
	public final org.jooq.TableField<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord, String> UUID = createField("uuid", org.jooq.impl.SQLDataType.VARCHAR.length(32).nullable(false), this, "");

	/**
	 * The column <code>respawn.users.last_seen_username</code>.
	 */
	public final org.jooq.TableField<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord, String> LAST_SEEN_USERNAME = createField("last_seen_username", org.jooq.impl.SQLDataType.VARCHAR.length(16), this, "");

	/**
	 * The column <code>respawn.users.last_seen_ip</code>.
	 */
	public final org.jooq.TableField<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord, String> LAST_SEEN_IP = createField("last_seen_ip", org.jooq.impl.SQLDataType.VARCHAR.length(15), this, "");

	/**
	 * The column <code>respawn.users.tokens_soft</code>.
	 */
	public final org.jooq.TableField<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord, Integer> TOKENS_SOFT = createField("tokens_soft", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>respawn.users.tokens_hard</code>.
	 */
	public final org.jooq.TableField<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord, Integer> TOKENS_HARD = createField("tokens_hard", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * Create a <code>respawn.users</code> table reference
	 */
	public Users() {
		this("users", null);
	}

	/**
	 * Create an aliased <code>respawn.users</code> table reference
	 */
	public Users(String alias) {
		this(alias, Users.USERS);
	}

	private Users(String alias, org.jooq.Table<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord> aliased) {
		this(alias, aliased, null);
	}

	private Users(String alias, org.jooq.Table<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.respawnnetwork.respawnlib.network.database.generated.Respawn.RESPAWN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord> getPrimaryKey() {
		return com.respawnnetwork.respawnlib.network.database.generated.Keys.KEY_USERS_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.respawnnetwork.respawnlib.network.database.generated.tables.records.UsersRecord>>asList(com.respawnnetwork.respawnlib.network.database.generated.Keys.KEY_USERS_PRIMARY, com.respawnnetwork.respawnlib.network.database.generated.Keys.KEY_USERS_UID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Users as(String alias) {
		return new Users(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Users rename(String name) {
		return new Users(name, null);
	}
}
