package org.luke.questMC.GUI.Confirm;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.luke.questMC.GUI.GUITypes;
import org.luke.questMC.QuestMC;
import org.luke.yakisobaGUILib.Abstract.GUIAbstract;

import static org.luke.takoyakiLibrary.TakoUtility.getInitInventory;

public class GUI_Confirm extends GUIAbstract<GUITypes.GUIEnum> {
    @Override
    public GUITypes.GUIEnum getType() {
        return GUITypes.GUIEnum.Confirm;
    }

    @Override
    public Inventory getInventory() {
        ConfirmManager.confirmInfo info = ConfirmManager.getPlayerConfirmInfo(player);
        Inventory inv = getInitInventory(3*9, "確認画面");

        try {
            if(info == null) return inv;
            inv = getInitInventory(3*9, info.title());

            inv.setItem(9+3, info.no());
            inv.setItem(9+5, info.yes());

            return inv;

        } catch (NullPointerException e) {
            QuestMC.getInstance().getLogger().warning(e.toString());
        }

        return inv;
    }
    @Override
    public void InventoryClickListener(InventoryClickEvent inventoryClickEvent) {
        try {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            ConfirmManager.confirmInfo info = ConfirmManager.getPlayerConfirmInfo(player);
            int slot = inventoryClickEvent.getSlot();
            if(info == null) return;
            if(slot == 9+3) {
                info.noCallback().run();
            } else if(slot == 9+5) {
                info.yesCallback().run();
            }
        } catch (NullPointerException e) {
            QuestMC.getInstance().getLogger().warning(e.toString());
        }
    }
}
