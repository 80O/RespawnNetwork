package im.xpd.parkour;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.network.accounts.MojangAccount;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.network.tokens.TokenReward;
import com.respawnnetwork.respawnlib.network.tokens.Tokens;
import im.xpd.parkour.SpecialCondition.SpecialCondition;
import im.xpd.parkour.SpecialCondition.SpecialConditionType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class EventListener implements Listener {
	private Main main;

    /** The token instance */
    private final Tokens tokens;


	public EventListener(Main main) {
		this.main = main;

        com.respawnnetwork.respawnlib.network.database.Database db = main.getDatabaseManager();

        if (db != null) {
            tokens = new Tokens(db);
        } else {
            main.getPluginLog().info("Could not get database, will not be able to use tokens in events!");
            tokens = null;
        }
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        String uuid = new MojangAccount(player).getUuid();

        PPlayer pp = main.parkour.loadPPlayer(player.getName());
        pp.updateExpBar(main.parkour, null);

        player.teleport(main.parkour.getLobby().getSpawn());
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();

        //Join items

        MojangAccount account = new MojangAccount(player);

        // Get tokens
        int softTokens = tokens.getSoft(account.getUuid());

        // Give the player the lobby items
        ItemStack gameSelection = Item.getFor(Material.ENDER_PEARL, "§6§lGame Selection");
        gameSelection.addUnsafeEnchantment(Enchantment.LUCK, 1);
        player.getInventory().setItem(0, gameSelection);

        ItemStack diamond = Item.getFor(Material.DIAMOND, String.format("§6§lYou have %d diamonds.", softTokens));
        diamond.addUnsafeEnchantment(Enchantment.LUCK, 1);
        player.getInventory().setItem(8, diamond);

        player.getInventory().setItem(1, Item.getFor(Material.BOOK, "§6§lCourse Selection"));

        player.updateInventory();

        // Say welcome message
        Message.INFO.send(player, ChatColor.GOLD + "Welcome to the parkour lobby!");

        // Remove potion effects
        for(PotionEffect pe : player.getActivePotionEffects()){
			player.removePotionEffect(pe.getType());
		}

        // Add free token for new players
        if(pp.isFirstJoin() && main.parkour.tokensEnabled()){
            tokens.give(new TokenReward(uuid, 1));

            Message.INFO.send(player, ChatColor.GOLD + "Welcome to RespawnNet Parkour! Have a free introductory token!");
		}
	}

	@EventHandler
	public void weather(WeatherChangeEvent event){
		event.setCancelled(true);
	}

    @EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		main.parkour.unloadPPlayer(event.getPlayer().getName());
        event.setQuitMessage(null);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(main.parkour.getLobby().getSpawn());
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getPlayer().getItemInHand().getType() == Material.BOOK) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.getPlayer().openInventory(main.in);
            }
			}
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getPlayer().isOp()) {
				if (event.getPlayer().getItemInHand() != null) {
					if (event.getPlayer().getItemInHand().getType() == Material.STICK) {
						if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
							event.getPlayer().setGameMode(GameMode.ADVENTURE);
						} else {
							event.getPlayer().setGameMode(GameMode.CREATIVE);
						}
					}
				}
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getItem() != null) {
				if (event.getItem().getType() == Material.BLAZE_ROD) {
					if (event.getPlayer().isOp()) {
						event.getPlayer().sendMessage("X: " + event.getClickedBlock().getX() + ", Y: " + event.getClickedBlock().getY() + ", Z: " + event.getClickedBlock().getZ() + " - " + event.getClickedBlock().getType().name() + " ("+event.getClickedBlock().getType().getId()+":" + event.getClickedBlock().getData() + ")");
						return;
					}
				}
			}
			if (event.getClickedBlock().getState() instanceof Sign) {
				if (event.getPlayer().getWorld() == Bukkit.getWorlds().get(0)) {
					Course course = main.parkour.getCourseByLobbySign(event.getClickedBlock().getLocation());
					PPlayer pPlayer = main.parkour.getPPlayer(event.getPlayer().getName());
					if (course != null && pPlayer != null) {
						if (event.getPlayer().hasPermission(course.getPermission())){
							pPlayer.updateExpBar(main.parkour, course);
							//event.getPlayer().teleport(pPlayer.getCheckPoint(course).getLocation());
							event.getPlayer().teleport(course.getStart());
							pPlayer.setCheckpoint(course.getStart().clone().add(0, -1, 0).getBlock(), course.getStart());
							pPlayer.updatePotions(course);
							event.getPlayer().sendMessage(ChatColor.GOLD + "You're playing " + ChatColor.YELLOW + course.getName() + ChatColor.GOLD + " by " + ChatColor.YELLOW + course.getAuthor() + ChatColor.GOLD + ".");
							event.getPlayer().sendMessage(ChatColor.GOLD + "Use " + ChatColor.YELLOW + "/return" + ChatColor.GOLD + " to get back to the lobby at any time.");
							event.getPlayer().sendMessage(ChatColor.GOLD + "Use " + ChatColor.YELLOW + "/checkpoint" + ChatColor.GOLD + " to get back to your last reached checkpoint.");
						} else {
							event.getPlayer().sendMessage(ChatColor.GOLD + "You don't have access to " + ChatColor.YELLOW + course.getName() + ChatColor.GOLD + ".");
						}
					}
				}
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (sign.getLine(1).equalsIgnoreCase("[Play again]")) {
					Course course = main.parkour.getCourseByWorldName(event.getPlayer().getWorld().getName());
					PPlayer pPlayer = main.parkour.getPPlayer(event.getPlayer().getName());
					if (course != null && pPlayer != null) {
						pPlayer.clearBlockLog(event.getPlayer().getName(), ParkourBlockType.CHECKPOINT);
						pPlayer.setCheckpoint(course.getStart().clone().add(0, -1, 0).getBlock(), course.getStart());
						event.getPlayer().teleport(course.getStart());
						event.getPlayer().sendMessage(ChatColor.GOLD + "Checkpoints cleared!");
					}
				} else if (sign.getLine(1).equalsIgnoreCase("[Back to lobby]")) {
					event.getPlayer().teleport(main.parkour.getLobby().getSpawn());
					PPlayer pPlayer = main.parkour.getPPlayer(event.getPlayer().getName());
					if (pPlayer != null) {
						pPlayer.updateExpBar(main.parkour, null);
					}
					pPlayer.updatePotions(null);
				}
			}
		}
	}
    
    public void blockCheckLoop() {
        //Placed in this class to stay near original location, for reference.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE) continue;
                if (p.getWorld() == Bukkit.getWorlds().get(0)) {
                    if (p.getLocation().getBlockY() <= main.parkour.getLobby().getBackToSpawnY()) {
                        p.teleport(main.parkour.getLobby().getSpawn());
                    }
                }

                Course course = main.parkour.getCourseByWorldName(p.getWorld().getName());

                if (course != null) {
                    Block block = p.getLocation().add(0, -1, 0).getBlock();
                    PPlayer pPlayer = main.parkour.getPPlayer(p.getName());
                    ParkourBlock pBlock = course.getBlock(block.getType().getId(), block.getData());
                    if (pBlock != null) {
                        if (p.isOnGround()) {
                            if (!pPlayer.hasTakenBlock(block)) {

                                //Points Blocks

                                if (pBlock.getType() == ParkourBlockType.ONE_POINT) {
                                    pPlayer.awardPoints(1, course);
                                    pPlayer.updateExpBar(main.parkour, course);
                                    pPlayer.takeBlock(block, pBlock.getType());
                                    p.playSound(p.getLocation(), Sound.BLAZE_HIT,10,1);
                                }

                                else if (pBlock.getType() == ParkourBlockType.TWO_POINTS) {
                                    pPlayer.awardPoints(2, course);
                                    pPlayer.takeBlock(block, pBlock.getType());
                                    pPlayer.updateExpBar(main.parkour, course);
                                    p.playSound(p.getLocation(), Sound.COW_HURT,10,1);
                                }

                                else if (pBlock.getType() == ParkourBlockType.THREE_POINTS) {
                                    pPlayer.awardPoints(3, course);
                                    pPlayer.takeBlock(block, pBlock.getType());
                                    pPlayer.updateExpBar(main.parkour, course);
                                    p.playSound(p.getLocation(), Sound.PIG_DEATH,10,1);
                                }

                                //Special blocks

                                else if (pBlock.getType() == ParkourBlockType.TOKEN) {
                                    if (!main.parkour.tokensEnabled()) return;

                                        //Get uuid
                                        String uuid = new MojangAccount(p).getUuid();

                                        //Award token
                                        tokens.give(new TokenReward(uuid,1));

                                        pPlayer.takeBlock(block, pBlock.getType());
                                        Message.INFO.send(p,ChatColor.GOLD + "You earned a token!");
                                        p.playSound(p.getLocation(), Sound.VILLAGER_HAGGLE,10,1);
                                }
                            }
                            if (pBlock.getType() == ParkourBlockType.CHECKPOINT) {
                                Checkpoint lastCheckpoint = pPlayer.getCheckPoint(course);
                                if (block.getX() != lastCheckpoint.getBlock().getX() || block.getY() != lastCheckpoint.getBlock().getY() || block.getZ() != lastCheckpoint.getBlock().getZ()) {
                                    //Checkpoints saved one point higher, for above block spawning
                                    pPlayer.setCheckpoint(block, p.getLocation());
                                    p.sendMessage(ChatColor.GOLD + "Checkpoint reached!");
                                    p.playSound(p.getLocation(), Sound.BURP,10,1);
                                }
                            }
                            if (pBlock.getType() == ParkourBlockType.LOBBY) {
                                p.teleport(main.parkour.getLobby().getSpawn());
                                pPlayer.updatePotions(null);
                                pPlayer.updateExpBar(main.parkour, null);
                                for (PotionEffect pe : p.getActivePotionEffects()) {
                                    p.removePotionEffect(pe.getType());
                                }
                            }
                        }
                    } else {
                        try {
                            if (p.isOnGround()) {
                                if (course.getMode().equals("whitelist")) {
                                    //Change to check if block is on whitelist
                                    String dmgvalue = Byte.toString(block.getData());
                                    if (!course.getModeBlocks().contains(block.getTypeId() + "/" + dmgvalue) && !course.getModeBlocks().contains(block.getTypeId() + "/?")) {
                                        Checkpoint lastCheckpoint = pPlayer.getCheckPoint(course);
                                        p.teleport(lastCheckpoint.getLocation());
                                    }

                                }
                                if (course.getMode().equals("blacklist")) {
                                    //Change to check if block is on whitelist
                                    String dmgvalue = Byte.toString(block.getData());
                                    if (course.getModeBlocks().contains(block.getTypeId() + "/" + dmgvalue)) {
                                        Checkpoint lastCheckpoint = pPlayer.getCheckPoint(course);
                                        p.teleport(lastCheckpoint.getLocation());
                                    }
                                    if (course.getModeBlocks().contains(block.getTypeId() + "/?")) {
                                        Checkpoint lastCheckpoint = pPlayer.getCheckPoint(course);
                                        p.teleport(lastCheckpoint.getLocation());
                                    }
                                }
                            }
                            if(p.getLocation().add(0,1,0).getBlock().isLiquid()){
                                p.teleport(pPlayer.getCheckPoint(course).getLocation());
                            }
                        } catch (Exception ex) {
                        }
                    }
                    if (p.getFireTicks() > 0) {
                        p.setFireTicks(0);
                        Checkpoint lastCheckpoint = pPlayer.getCheckPoint(course);
                        p.teleport(lastCheckpoint.getLocation());
                        p.setFireTicks(0);
                    }
                    if (block.getY() <= course.getCheckpointYTrigger()) {
                        p.teleport(pPlayer.getCheckPoint(course).getLocation());
                    }
                }
            }
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			
			Course course = main.parkour.getCourseByWorldName(event.getEntity().getWorld().getName());
			if (course != null) {
				if (event.getCause() == DamageCause.FALL) {
					for (SpecialCondition sc : course.getSpecialConditions()) {
						if (sc.getType() == SpecialConditionType.FALL_DAMAGE) {
							if (sc.test(event.getEntity().getFallDistance())) {
								sc.takeAction(main.parkour, (Player) event.getEntity());
							}
						}
					}
				}
			}
			event.setCancelled(true);
		}
	}
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.getPlayer().teleport(event.getPlayer().getLocation());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onHangingRemove(HangingBreakByEntityEvent event) {
		if (event.getRemover().getType() == EntityType.PLAYER) {
			if (((Player)event.getRemover()).getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.getDrops().clear();
		event.setDroppedExp(0);
		event.setKeepLevel(true);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)){
		try{
		if(event.getInventory().getName().equals(main.in.getName())){
			event.setCancelled(true);
			ItemStack ClickedItem = event.getCurrentItem();
			String iconname = ChatColor.stripColor(ClickedItem.getItemMeta().getDisplayName());
			for(String worldname : main.inicons.keySet()){
				if(main.inicons.get(worldname).equals(iconname)){
					Player p = (Player) event.getWhoClicked();
					p.closeInventory();
                    Course course = main.parkour.getCourseByWorldName(worldname);
					PPlayer pPlayer = main.parkour.getPPlayer(p.getName());
					if (course != null && pPlayer != null) {
                        if (event.getWhoClicked().hasPermission(course.getPermission())){
							pPlayer.updateExpBar(main.parkour, course);
							//p.teleport(pPlayer.getCheckPoint(course).getLocation());
							p.teleport(course.getStart());
							pPlayer.setCheckpoint(course.getStart().clone().add(0, -1, 0).getBlock(), course.getStart());
							pPlayer.updatePotions(course);
							p.sendMessage(ChatColor.GOLD + "You're playing " + ChatColor.YELLOW + course.getName() + ChatColor.GOLD + " by " + ChatColor.YELLOW + course.getAuthor() + ChatColor.GOLD + ".");
							p.sendMessage(ChatColor.GOLD + "Use " + ChatColor.YELLOW + "/return" + ChatColor.GOLD + " to get back to the lobby at any time.");
							p.sendMessage(ChatColor.GOLD + "Use " + ChatColor.YELLOW + "/checkpoint" + ChatColor.GOLD + " to get back to your last reached checkpoint.");
						} else {
							p.sendMessage(ChatColor.GOLD + "You don't have access to " + ChatColor.YELLOW + course.getName() + ChatColor.GOLD + ".");
						}
				}
				}
			}
		}
		else{
			event.setCancelled(true);
		}
		}catch(Exception ex){
			//Usually called if no item was clicked
		}
		}
	}
	
	
	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		event.setCancelled(true);
	}
}
