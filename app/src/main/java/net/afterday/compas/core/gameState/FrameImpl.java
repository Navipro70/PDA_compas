package net.afterday.compas.core.gameState;

import net.afterday.compas.core.player.PlayerProps;

/**
 * Created by spaka on 4/18/2018.
 */

public class FrameImpl implements Frame {
    private PlayerProps mPlayerProps;

    public FrameImpl(PlayerProps playerProps) {
        this.mPlayerProps = playerProps;
    }

    @Override
    public PlayerProps getPlayerProps() {
        return mPlayerProps;
    }


}
