/**
 * This class is generated by jOOQ
 */
package net.respawn.slicegames.database.generated;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Respawn extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -393465465;

	/**
	 * The singleton instance of <code>respawn</code>
	 */
	public static final Respawn RESPAWN = new Respawn();

	/**
	 * No further instances allowed
	 */
	private Respawn() {
		super("respawn");
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			net.respawn.slicegames.database.generated.tables.HgGames.HG_GAMES,
			net.respawn.slicegames.database.generated.tables.HgUsers.HG_USERS);
	}
}
