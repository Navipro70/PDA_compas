package net.afterday.compas.core;

import net.afterday.compas.core.gameState.Frame;
import net.afterday.compas.core.gameState.FrameImpl;
import net.afterday.compas.core.influences.InfluencesPack;
import net.afterday.compas.core.inventory.Inventory;
import net.afterday.compas.core.inventory.InventoryImpl;
import net.afterday.compas.core.inventory.items.Item;
import net.afterday.compas.core.player.Player;
import net.afterday.compas.core.player.PlayerImpl;
import net.afterday.compas.core.serialization.Serializer;
import net.afterday.compas.persistency.PersistencyProvider;
import net.afterday.compas.persistency.items.ItemDescriptor;

public class GameImpl implements Game {
    private PlayerImpl mPlayer;
    private PersistencyProvider persistencyProvider;

    public GameImpl(PersistencyProvider persistencyProvider, Serializer serializer) {
        this.persistencyProvider = persistencyProvider;
        mPlayer = new PlayerImpl(new InventoryImpl(persistencyProvider.getItemsPersistency(), serializer), serializer);
    }

    @Override
    public Frame start() {
        return new FrameImpl(mPlayer.getPlayerProps());
    }

    public Frame acceptInfluences(InfluencesPack influencesPack) {
        return mPlayer.acceptInfluences(influencesPack);
    }

    @Override
    public Player getPlayer() {
        return mPlayer;
    }

    @Override
    public Inventory getInventory() {
        return mPlayer.getInventory();
    }

    @Override
    public boolean acceptCode(String code) {
        ItemDescriptor itemDesc = persistencyProvider.getItemsPersistency().getItemForCode(code);
        if (itemDesc != null) {
            return mPlayer.addItem(itemDesc, code);
        }
        Player.FRACTION fraction = persistencyProvider.getPlayerPersistency().getFractionByCode(code);
        if (fraction != null) {
            mPlayer.setFraction(fraction);
            return true;
        }
        Player.COMMAND command = persistencyProvider.getPlayerPersistency().getCommandByCode(code);
        if (command != null) {
            switch (command) {
                case KILL:
                    mPlayer.setState(Player.STATE.DEAD_BURER);
                    return true;
                case REVIVE:
                    mPlayer.reborn();
                    return true;
                case ACTIVATE_DETECTOR:
                    mPlayer.getPlayerProps().setDetectorAccess(true);
                    ((PlayerImpl)mPlayer).saveDetectorAccessFlag(true);
                    net.afterday.compas.logging.Logger.d("Получен детектор артефактов!");
                    return true;
            }
        }
        return false;
    }

    @Override
    public Frame useItem(Item item) {
        return mPlayer.useItem(item);
    }

}
