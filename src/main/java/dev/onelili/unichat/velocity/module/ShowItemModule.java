package dev.onelili.unichat.velocity.module;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.Gson;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.handler.PlayerData;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import dev.onelili.unichat.velocity.util.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShowItemModule extends PatternModule {
    @Override
    public @Nonnull Component handle(@Nonnull Player sender, boolean doProcess) {
        PlayerData data = PlayerData.getPlayerData(sender);
        if(data == null || data.getHandItem() < 0)
            return new Message("&7[None]").toComponent();
        ItemStack item = data.getInventory().get(data.getHandItem() + 36);
        if(item == null)
            return new Message("&7[None]").toComponent();

        return Component.empty()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(Component.text("]").color(NamedTextColor.GRAY));
    }

    private static Component createItemDisplay(@Nonnull ItemStack item) {
//        boolean trimmed = false;
        String itemJson = new Gson().toJson(Utils.getNbtCompoundJson(item.getOrCreateTag()));
//        ItemStack trimmedItem = null;
//        if (InteractiveChat.sendOriginalIfTooLong && itemJson.length() > InteractiveChat.itemTagMaxLength) {
//            trimmedItem = new ItemStack(item.getType());
//            trimmedItem.addUnsafeEnchantments(item.getEnchantments());
//            if (itemMeta != null && itemMeta.hasDisplayName()) {
//                ItemStack nameItem = trimmedItem.clone();
//                Component name = NMS.getInstance().getItemStackDisplayName(item);
//                NMS.getInstance().setItemStackDisplayName(nameItem, name);
//                String newjson = ItemNBTUtils.getNMSItemStackJson(nameItem);
//                if (newjson.length() <= InteractiveChat.itemTagMaxLength) {
//                    trimmedItem = nameItem;
//                }
//            }
//            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
//                ItemStack loreItem = trimmedItem.clone();
//                ItemMeta meta = loreItem.getItemMeta();
//                meta.setLore(item.getItemMeta().getLore());
//                loreItem.setItemMeta(meta);
//                String newjson = ItemNBTUtils.getNMSItemStackJson(loreItem);
//                if (newjson.length() <= InteractiveChat.itemTagMaxLength) {
//                    trimmedItem = loreItem;
//                }
//            }
//            trimmed = true;
//        }
//
//        String amountString = "";
//        Component itemDisplayNameComponent = ItemStackUtils.getDisplayName(item);
//
//        amountString = String.valueOf(itemAmount);
//        Key key = ItemNBTUtils.getNMSItemStackNamespacedKey(item);
//
//        ShowItem showItem;
//        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
//            if (item.getType().equals(Material.AIR)) {
//                showHover = false;
//            }
//            Map<Key, DataComponentValue> dataComponents = ItemNBTUtils.getNMSItemStackDataComponents(trimmedItem == null ? item : trimmedItem);
//            showItem = dataComponents.isEmpty() ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, dataComponents);
//        } else {
//            String tag = ItemNBTUtils.getNMSItemStackTag(trimmedItem == null ? item : trimmedItem);
//            showItem = tag == null ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, BinaryTagHolder.binaryTagHolder(tag));
//        }
//
//        HoverEvent<ShowItem> hoverEvent = HoverEvent.showItem(showItem);
//        String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, rawTitle));
//        String sha1 = HashUtils.createSha1(title, item);
//
//        String command = null;
//        boolean isMapView = false;
//
//        if (!preview) {
//            if (InteractiveChat.itemMapPreview && FilledMapUtils.isFilledMap(item)) {
//                isMapView = true;
//                if (!InteractiveChat.mapDisplay.containsKey(sha1)) {
//                    InteractiveChatAPI.addMapToMapSharedList(sha1, item);
//                }
//            } else if (!InteractiveChat.itemDisplay.containsKey(sha1)) {
//                if (useInventoryView(item)) {
//                    Inventory container = ((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState()).getInventory();
//                    Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, container.getSize() + 9, title);
//                    ItemStack empty = InteractiveChat.itemFrame1.clone();
//                    if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
//                        empty = InteractiveChat.itemFrame2.clone();
//                    }
//                    if (empty.getItemMeta() != null) {
//                        ItemMeta emptyMeta = empty.getItemMeta();
//                        emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
//                        empty.setItemMeta(emptyMeta);
//                    }
//                    for (int j = 0; j < 9; j++) {
//                        inv.setItem(j, empty);
//                    }
//                    inv.setItem(4, isAir ? null : originalItem);
//                    for (int j = 0; j < container.getSize(); j++) {
//                        ItemStack shulkerItem = container.getItem(j);
//                        if (shulkerItem != null && !shulkerItem.getType().equals(Material.AIR)) {
//                            inv.setItem(j + 9, shulkerItem == null ? null : shulkerItem.clone());
//                        }
//                    }
//                    InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
//                } else {
//                    if (InteractiveChat.version.isOld()) {
//                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 27, title);
//                        ItemStack empty = InteractiveChat.itemFrame1.clone();
//                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
//                            empty = InteractiveChat.itemFrame2.clone();
//                        }
//                        if (empty.getItemMeta() != null) {
//                            ItemMeta emptyMeta = empty.getItemMeta();
//                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
//                            empty.setItemMeta(emptyMeta);
//                        }
//                        for (int j = 0; j < inv.getSize(); j++) {
//                            inv.setItem(j, empty);
//                        }
//                        inv.setItem(13, isAir ? null : originalItem);
//                        InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
//                    } else {
//                        Inventory inv = InventoryUtils.CAN_USE_DROPPER_TYPE ? Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryType.DROPPER, title) : Bukkit.createInventory(ICInventoryHolder.INSTANCE, 27, title);
//                        ItemStack empty = InteractiveChat.itemFrame1.clone();
//                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
//                            empty = InteractiveChat.itemFrame2.clone();
//                        }
//                        if (empty.getItemMeta() != null) {
//                            ItemMeta emptyMeta = empty.getItemMeta();
//                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
//                            empty.setItemMeta(emptyMeta);
//                        }
//                        for (int j = 0; j < inv.getSize(); j++) {
//                            inv.setItem(j, empty);
//                        }
//                        inv.setItem(inv.getSize() / 2, isAir ? null : originalItem);
//                        InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
//                    }
//                }
//            }
//            command = isMapView ? "/interactivechat viewmap " + sha1 : "/interactivechat viewitem " + sha1;
//        }
//
//        if (trimmed && InteractiveChat.cancelledMessage) {
//            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Trimmed an item display's meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
//        }
//
//        Component itemDisplayComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, itemAmount == 1 ? InteractiveChat.itemSingularReplaceText : InteractiveChat.itemReplaceText.replace("{Amount}", amountString))));
//        itemDisplayComponent = itemDisplayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(itemDisplayNameComponent).build());
//        if (showHover) {
//            itemDisplayComponent = itemDisplayComponent.hoverEvent(hoverEvent);
//        } else if (alternativeHover != null) {
//            itemDisplayComponent = itemDisplayComponent.hoverEvent(HoverEvent.showText(alternativeHover));
//        }
//        if (command != null && !isAir && (isMapView || (!isMapView && InteractiveChat.itemGUI))) {
//            itemDisplayComponent = itemDisplayComponent.clickEvent(ClickEvent.runCommand(command));
//        }
//        return ComponentCompacting.optimize(itemDisplayComponent);
        return null;
    }
}
