package uk.co.jacekk.bukkit.infiniteplots;

import org.bukkit.permissions.PermissionDefault;

import uk.co.jacekk.bukkit.baseplugin.permissions.PluginPermission;

/**
 * The {@link PluginPermission} used to control access to various features.
 */
public class Permission {

	public static final PluginPermission PLOT_CLAIM					= new PluginPermission("infiniteplots.plot.claim",				PermissionDefault.TRUE,		"Allows the player to claim plots");
	public static final PluginPermission PLOT_UNCLAIM				= new PluginPermission("infiniteplots.plot.unclaim",			PermissionDefault.TRUE,		"Allows the player to unclaim plots");
	public static final PluginPermission PLOT_UNCLAIM_OTHERS		= new PluginPermission("infiniteplots.plot.unclaim.others",		PermissionDefault.OP,		"Allows the player to unclaim other players plots");
	public static final PluginPermission PLOT_ADD_BUILDER			= new PluginPermission("infiniteplots.plot.add-builder",		PermissionDefault.TRUE,		"Allows the player to add builders to their plots");
	public static final PluginPermission PLOT_REMOVE_BUILDER		= new PluginPermission("infiniteplots.plot.remove-builder",		PermissionDefault.TRUE,		"Allows the player to remove builders from their plots");
	public static final PluginPermission PLOT_FLAG					= new PluginPermission("infiniteplots.plot.flag",				PermissionDefault.TRUE,		"Allows the player to modify their plots flags");
	public static final PluginPermission PLOT_FLAG_OTHER			= new PluginPermission("infiniteplots.plot.flag.others",		PermissionDefault.OP,		"Allows the player to modify other players plot flags");
	public static final PluginPermission PLOT_SET_BIOME				= new PluginPermission("infiniteplots.plot.set-biome",			PermissionDefault.TRUE,		"Allows the player to set their plots biome");
	public static final PluginPermission PLOT_SET_BIOME_OTHERS		= new PluginPermission("infiniteplots.plot.set-biome.others",	PermissionDefault.OP,		"Allows the player to set biome of other player plots");
	public static final PluginPermission PLOT_INFO					= new PluginPermission("infiniteplots.plot.info",				PermissionDefault.TRUE,		"Allows the player to view plot info");
	public static final PluginPermission PLOT_RESET					= new PluginPermission("infiniteplots.plot.reset",				PermissionDefault.TRUE,		"Allows the player to regenerate their plots");
	public static final PluginPermission PLOT_RESET_OTHER			= new PluginPermission("infiniteplots.plot.reset.others",		PermissionDefault.OP,		"Allows the player to regenerate other players plots");
	public static final PluginPermission PLOT_LIST					= new PluginPermission("infiniteplots.plot.list",				PermissionDefault.TRUE,		"Allows the player to list their plots");
	public static final PluginPermission PLOT_LIST_OTHER			= new PluginPermission("infiniteplots.plot.list.others",		PermissionDefault.OP,		"Allows the player to list another players plots");
	public static final PluginPermission PLOT_TELEPORT				= new PluginPermission("infiniteplots.plot.teleport",			PermissionDefault.TRUE,		"Allows the player to teleport to their plots");
	public static final PluginPermission PLOT_TELEPORT_OTHER		= new PluginPermission("infiniteplots.plot.teleport.others",	PermissionDefault.OP,		"Allows the player to teleport to another players plots");
	public static final PluginPermission PLOT_DECORATE				= new PluginPermission("infiniteplots.plot.decorate",			PermissionDefault.TRUE,		"Allows the player to decorate plots");
	public static final PluginPermission PLOT_DECORATE_OTHER		= new PluginPermission("infiniteplots.plot.decorate.others",	PermissionDefault.OP,		"Allows the player to decorate another players plots");
	public static final PluginPermission PLOT_PROTECTION			= new PluginPermission("infiniteplots.plot.protection",			PermissionDefault.TRUE,		"Allows the player to modify a plots protection");
	public static final PluginPermission PLOT_PROTECTION_OTHER		= new PluginPermission("infiniteplots.plot.protection.others",	PermissionDefault.OP,		"Allows the player to modify another players plot protection");

	public static final PluginPermission PLOT_BUILD_ALL				= new PluginPermission("infiniteplots.plot.build-all",			PermissionDefault.OP,		"Allows the player to build in plots they do not own");
	public static final PluginPermission PLOT_BYPASS_CLAIM_LIMIT	= new PluginPermission("infiniteplots.plot.bypass-claim-limit",	PermissionDefault.OP,		"Allows the player to claim more plots than the limit");
	public static final PluginPermission PLOT_PURGE					= new PluginPermission("infiniteplots.plot.purge",				PermissionDefault.OP,		"Allows the player to remove dead plots");

	public static final PluginPermission PLOT_LIMIT_NORMAL			= new PluginPermission("infiniteplots.plot.limit.normal",		PermissionDefault.TRUE,		"Allows the player to have x plots claimed.");
	public static final PluginPermission PLOT_LIMIT_PREMIUM			= new PluginPermission("infiniteplots.plot.limit.premium",		PermissionDefault.FALSE,	"Allows the player to have x plots claimed.");
	public static final PluginPermission PLOT_LIMIT_INVESTOR		= new PluginPermission("infiniteplots.plot.limit.investor",		PermissionDefault.FALSE,	"Allows the player to have x plots claimed.");
	public static final PluginPermission PLOT_LIMIT_BUILDER			= new PluginPermission("infiniteplots.plot.limit.builder",		PermissionDefault.FALSE,	"Allows the player to have x plots claimed.");
	public static final PluginPermission PLOT_LIMIT_VIP				= new PluginPermission("infiniteplots.plot.limit.vip"	,		PermissionDefault.FALSE,	"Allows the player to have x plots claimed.");

}
