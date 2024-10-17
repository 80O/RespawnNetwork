package im.xpd.parkour;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	private Main main;
	private Connection SQLconnection = null;
	private Connection liteConnection = null;

	public Database(Main main) {
		this.main = main;
	}

	public PPlayer getPPlayer(String name) {
		try {
			long start = System.currentTimeMillis();

			// Get basic player data
            //Ensure connection is present
            if(SQLconnection == null){
                main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] SQL Connection was null! Attempting to manually initialise!");
                String host = main.getConfig().getString("database.host");
                String port = main.getConfig().getString("database.port");
                String dbName = main.getConfig().getString("database.name");
                String pass = main.getConfig().getString("database.pass");
                String user = main.getConfig().getString("database.user");
                SQLconnection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, pass);
                if(SQLconnection == null){
                    main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Could not manually intialise the SQL connection.");
                    return null;
                }
            }
			PreparedStatement playerStmt = SQLconnection
					.prepareStatement("SELECT * FROM par_users WHERE uid = ? LIMIT 1");
			playerStmt.setString(1, Bukkit.getPlayer(name).getUniqueId().toString());
			playerStmt.execute();
			ResultSet playerRS = playerStmt.getResultSet();
			if (playerRS.next()) {
				String playerId = playerRS.getString(1);
				int playerPoints = playerRS.getInt(2);
				// Get the players checkpoints
				Map<String, Checkpoint> playerCheckpoints = new HashMap<String, Checkpoint>();
				PreparedStatement cpStmt = liteConnection
						.prepareStatement("SELECT * FROM checkpoints WHERE player = ?");
				cpStmt.setString(1, playerId);
				cpStmt.execute();
				ResultSet cpRS = cpStmt.getResultSet();
				while (cpRS.next()) {
					World checkpointWorld = main.getServer().getWorld(
							cpRS.getString(3));
					if (checkpointWorld != null) {
						Location playerLoc = new Location(checkpointWorld,
								cpRS.getDouble(7), cpRS.getDouble(8),
								cpRS.getDouble(9));
						playerLoc.setPitch(cpRS.getFloat(10));
						playerLoc.setYaw(cpRS.getFloat(11));
						Location blockLoc = new Location(checkpointWorld,
								cpRS.getInt(4), cpRS.getInt(5), cpRS.getInt(6));
						Checkpoint cp = new Checkpoint(blockLoc.getBlock(),
								playerLoc);
						playerCheckpoints.put(cpRS.getString(3).toLowerCase(),
								cp);
					}
				}

				// Get the players block log and calculate course specific
				// points
				List<LogBlock> playerBlockLog = new ArrayList<LogBlock>();
				Map<String, Integer> coursePoints = new HashMap<String, Integer>();
				PreparedStatement blStmt = liteConnection
						.prepareStatement("SELECT * FROM block_log WHERE player = ?");
				blStmt.setString(1, playerId);
				blStmt.execute();
				ResultSet blRS = blStmt.getResultSet();
				while (blRS.next()) {
					// Changed string id to 2 from 3
					World world = main.getServer().getWorld(blRS.getString(2));
					if (world.getName() != null) {
						// Changed all ResultSet indexes to match database
						ParkourBlockType type = ParkourBlockType.getById(blRS
								.getInt(3));
						playerBlockLog.add(new LogBlock(
								world.getBlockAt(blRS.getInt(4),
										blRS.getInt(5), blRS.getInt(6)), type));
						if (coursePoints.containsKey(blRS.getString(2)
								.toLowerCase())) {
							int currentpoints = coursePoints.get(blRS
									.getString(2).toLowerCase());
							int newpoints = currentpoints + type.getPoints();
							coursePoints
									.remove(blRS.getString(2).toLowerCase());
							coursePoints.put(blRS.getString(2).toLowerCase(),
									newpoints);
						} else {
							coursePoints.put(blRS.getString(2).toLowerCase(),
									type.getPoints());
						}
					}
				}
				main.getServer()
						.getConsoleSender()
						.sendMessage(
								ChatColor.GREEN
										+ "[Parkour] Finished loading player "
										+ ChatColor.YELLOW + name
										+ ChatColor.GREEN + " in "
										+ ChatColor.YELLOW
										+ (System.currentTimeMillis() - start)
										+ "ms" + ChatColor.GREEN + ".");
				main.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Parkour] "
										+ playerBlockLog.size()
										+ " points were loaded!");
				return new PPlayer(playerId, name, playerPoints,
						coursePoints, playerBlockLog, playerCheckpoints);
			} else {
				main.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Parkour] Registering new player!");
				PreparedStatement newPlayerStmt = SQLconnection
						.prepareStatement("INSERT INTO par_users (uid, points) VALUES (?, ?)");
				newPlayerStmt.setString(1,Bukkit.getPlayer(name).getUniqueId().toString());
				newPlayerStmt.setInt(2, 0);
				newPlayerStmt.executeUpdate();
				PPlayer pp = getPPlayer(name);
				pp.setFirstJoin(true);
				return pp;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void savePlayer(PPlayer pPlayer) {
		try {
			long start = System.currentTimeMillis();

			// Save points
			PreparedStatement pStmt = SQLconnection
					.prepareStatement("UPDATE par_users SET points = ? WHERE uid = ?");
			pStmt.setInt(1, pPlayer.getPoints());
			pStmt.setString(2, pPlayer.getId());
			// Changed executeUpdate to execute
			pStmt.execute();

			// Clear checkpoints
			PreparedStatement cpStmtD = liteConnection
					.prepareStatement("DELETE FROM checkpoints WHERE player = ?");
			cpStmtD.setString(1, pPlayer.getId());
			cpStmtD.executeUpdate();

			// Insert new values of the checkpoints
			for (Checkpoint checkpoint : pPlayer.getCheckPoints()) {
				PreparedStatement cpStmtI = liteConnection
						.prepareStatement("INSERT INTO checkpoints (player, world, block_x, block_y, block_z, player_x, player_y, player_z, player_pitch, player_yaw) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				cpStmtI.setString(1, pPlayer.getId());
				cpStmtI.setString(2, checkpoint.getBlock().getWorld().getName()
						.toLowerCase());
				cpStmtI.setInt(3, checkpoint.getBlock().getX());
				cpStmtI.setInt(4, checkpoint.getBlock().getY());
				cpStmtI.setInt(5, checkpoint.getBlock().getZ());
				cpStmtI.setDouble(6, checkpoint.getLocation().getX());
				cpStmtI.setDouble(7, checkpoint.getLocation().getY());
				cpStmtI.setDouble(8, checkpoint.getLocation().getZ());
				cpStmtI.setFloat(9, checkpoint.getLocation().getPitch());
				cpStmtI.setFloat(10, checkpoint.getLocation().getYaw());
				cpStmtI.executeUpdate();
			}

			// Insert block log cache
			for (LogBlock lBlock : pPlayer.getBlockLogCache()) {
				PreparedStatement lbStmt = liteConnection
						.prepareStatement("INSERT INTO block_log (player, world, type, x, y, z) VALUES (?, ?, ?, ?, ?, ?)");
				lbStmt.setString(1, pPlayer.getId());
				lbStmt.setString(2, lBlock.getBlock().getWorld().getName()
						.toLowerCase());
				lbStmt.setInt(3, lBlock.getType().getId());
				lbStmt.setInt(4, lBlock.getBlock().getX());
				lbStmt.setInt(5, lBlock.getBlock().getY());
				lbStmt.setInt(6, lBlock.getBlock().getZ());
				lbStmt.executeUpdate();
			}
			pPlayer.getBlockLogCache().clear();

			main.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatColor.GREEN
									+ "[Parkour] Finished saving player "
									+ ChatColor.YELLOW + pPlayer.getName()
									+ ChatColor.GREEN + " in "
									+ ChatColor.YELLOW
									+ (System.currentTimeMillis() - start)
									+ "ms" + ChatColor.GREEN + ".");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getLiteConnection() {
		return liteConnection;
	}

	public Connection getSqlConnection() {
		return SQLconnection;
	}

	public boolean connectLite() {
		try {
			Class.forName("org.sqlite.JDBC");
			liteConnection = DriverManager.getConnection("jdbc:sqlite:"
					+ "LiteDB.db");
			// Check if database has already been setup
			DatabaseMetaData meta = 
					liteConnection.getMetaData();
			ResultSet res = meta.getTables(null, null, "block_log", null);
			if (!res.next()) {
				// table does not exist.
				main.getServer()
						.getConsoleSender()
						.sendMessage(
								ChatColor.GREEN
										+ "[Parkour] First Load! Attempting to create database...");
				if (createLiteTables() == false) {
					return false;
				}
			} else {
				// table exists.
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean connectSQL(String host, String port, String user, String pass,
			String dbName) {
		try {
			SQLconnection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, pass);
            if(SQLconnection == null){
                main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to connect to database!");
            }
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean createLiteTables() {
		// Create sql tables, assuming blank database.
		try {
			PreparedStatement ps = liteConnection
					.prepareStatement("CREATE TABLE IF NOT EXISTS block_log(player int, world varchar(255),type int,x int,y int,z int)");
			ps.executeUpdate();
			PreparedStatement psi = liteConnection
					.prepareStatement("CREATE TABLE IF NOT EXISTS checkpoints(player int, world string, block_x int, block_y int, block_z int, player_x int, player_y int, player_z int, player_pitch int, player_yaw int)");
			psi.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
}
