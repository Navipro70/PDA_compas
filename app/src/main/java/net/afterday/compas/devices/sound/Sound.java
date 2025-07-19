package net.afterday.compas.devices.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;

import net.afterday.compas.R;

public class Sound {
    private static final String TAG = "SOUND";
    private final int mRadTick;
    private final int mEmissionStarts;
    private final int mEmissionWarning;
    private final int mEmissionHit;
    private final int mEmissionPeriodical;
    private final int mEmissionEnds;
    private final int mAnomalyTick;
    private final int mMental;
    private final int mAnomalyDeath;
    private final int mDie;
    private final int mZombify;
    private final int mControl;
    private final int mBurer;
    private final int mController;
    private final int mHealing;
    private final int mGlassBreak;
    private final int mBulbBreak;
    private final int mLevelUp;
    private final int mInventoryOpen;
    private final int mInventoryDrop;
    private final int mInventoryUse;
    private final int mInventoryClose;
    private final int mPdaOn;
    private final int mPdaOff;
    private final int mTransmutating;
    private final int mWTimer;
    private final int mMentalHit;
    private final int mMonolithHit;
    private final int mAbducted;
    private final int mItemScanned;
    private final int mArtefact;
    private final int mFruitPunch;
    private final int mWeakExplosion;
    private final int mStrongExplosion;
    private final int mFruitPunchDeath;    
    private MediaPlayer burrerPlayer = new MediaPlayer();
    private MediaPlayer controllerPlayer = new MediaPlayer();
    private SoundPool mSoundPool;
    private String pckg;
    private Context ctx;
    private boolean burrerPlaying;
    private boolean controllerPlaying;

    public Sound(Context ctx) {
        this.ctx = ctx;
        pckg = ctx.getPackageName();

        mSoundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 32);

        preparePlayer(burrerPlayer, R.raw.burer_presence, (s) -> {
            burrerPlaying = false;
        });
        preparePlayer(controllerPlayer, R.raw.controller_presence, (s) -> {
            controllerPlaying = false;
        });

        // Load samples
        mPdaOn = mSoundPool.load(ctx, R.raw.pda_app_start, 1);
        mPdaOff = mSoundPool.load(ctx, R.raw.pda_app_stop, 1);
        mRadTick = mSoundPool.load(ctx, R.raw.rad_click, 1);
        mAnomalyTick = mSoundPool.load(ctx, R.raw.anomaly, 1);
        mMental = mSoundPool.load(ctx, R.raw.mental, 1);
        mEmissionStarts = mSoundPool.load(ctx, R.raw.pda_emission_begins, 1);
        mEmissionWarning = mSoundPool.load(ctx, R.raw.pda_emission_warning, 1);
        mEmissionHit = mSoundPool.load(ctx, R.raw.emission_hit, 1);
        mEmissionPeriodical = mSoundPool.load(ctx, R.raw.emission_periodical, 1);
        mEmissionEnds = mSoundPool.load(ctx, R.raw.pda_emission_ends, 1);
        mAnomalyDeath = mSoundPool.load(ctx, R.raw.ano_kill, 1);
        mDie = mSoundPool.load(ctx, R.raw.die, 1);
        mZombify = mSoundPool.load(ctx, R.raw.zombified, 1);
        mControl = mSoundPool.load(ctx, R.raw.controlled, 1);
        mBurer = mSoundPool.load(ctx, R.raw.burer, 1);
        mController = mSoundPool.load(ctx, R.raw.controller, 1);
        mHealing = mSoundPool.load(ctx, R.raw.healing, 1);
        mGlassBreak = mSoundPool.load(ctx, R.raw.glass_break, 1);
        mBulbBreak = mSoundPool.load(ctx, R.raw.bulb_break, 1);
        mLevelUp = mSoundPool.load(ctx, R.raw.pda_level_up, 1);
        mInventoryOpen = mSoundPool.load(ctx, R.raw.inv_open, 1);
        mInventoryDrop = mSoundPool.load(ctx, R.raw.inv_drop, 1);
        mInventoryUse = mSoundPool.load(ctx, R.raw.inv_use, 1);
        mInventoryClose = mSoundPool.load(ctx, R.raw.inv_close, 1);
        mTransmutating = mSoundPool.load(ctx, R.raw.transmutating, 1);
        mWTimer = mSoundPool.load(ctx, R.raw.w_timer_begins, 1);
        mMentalHit = mSoundPool.load(ctx, R.raw.mental_hit, 1);
        mMonolithHit = mSoundPool.load(ctx, R.raw.monolith_call_1, 1);
        mAbducted = mSoundPool.load(ctx, R.raw.abducted, 1);
        mItemScanned = mSoundPool.load(ctx, R.raw.pda_qr_scanned, 1);
        mArtefact = mSoundPool.load(ctx, R.raw.pda_artefact, 1);
        mFruitPunch = mSoundPool.load(ctx, R.raw.fruitpunch, 1);
        mFruitPunchDeath = mSoundPool.load(ctx, R.raw.fruitpunch_kill, 1);
        mWeakExplosion = mSoundPool.load(ctx, R.raw.weak_explosion, 1);        
        mStrongExplosion = mSoundPool.load(ctx, R.raw.strong_explosion, 1);
    }

    public void playRadClick() {
        float vol = (float) Math.random() * 0.1f + 0.5f; // From 0.5 to 0.6
        float freq = (float) Math.random() * 0.2f + 0.9f; // From 0.9 to 1.1
        int soundStream = mSoundPool.play(
                mRadTick, // Sound
                vol, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                vol, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                freq // Frequency
        );
    }

    public void playGlassBreak() {
        mSoundPool.play(mGlassBreak, 1f, 1f, 2, 0, 1f);
    }

    public void playBulbBreak() {
        mSoundPool.play(mBulbBreak, 1f, 1f, 2, 0, 1f);
    }

    public void playLevelUp() {
        int result = mSoundPool.play(mLevelUp, 1f, 1f, 2, 0, 1f);
        if (result > 0)
            return;
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
            {
                if (sampleId == mLevelUp && status == 0) {
                    mSoundPool.play(mLevelUp, 1f, 1f, 2, 0, 1f);
                }
            }
        });
    }

    public void playHealing() {
        mSoundPool.play(mHealing,
                1f,
                1f,
                2,
                0,
                1f);

    }

    public void playPdaOn() {
        mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (sampleId == mPdaOn && status == 0) {
                mSoundPool.play(mPdaOn, 1f, 1f, 1, 0, 1f);
            }
        });
    }

    public void stopHealing() {
        mSoundPool.stop(mHealing);
    }

    public void playAnomalyClick() {
        mSoundPool.play(
                mAnomalyTick, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playWeakExplosion() {
        mSoundPool.play(mWeakExplosion, 1f, 1f, 2, 0, 1f);
    }

    public void playStrongExplosion() {
        mSoundPool.play(mStrongExplosion, 1f, 1f, 2, 0, 1f);
    }
    public void playMental() {
        mSoundPool.play(
                mMental, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playAnomalyDeath() {
        mSoundPool.play(
                mAnomalyDeath, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playFruitPunch() {
        mSoundPool.play(
                mFruitPunch, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playFruitPunchDeath() {
        mSoundPool.play(
                mFruitPunchDeath, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playEmissionWarning() {

        mSoundPool.play(
                mEmissionWarning, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playEmissionStarts() {
        mSoundPool.play(
                mEmissionStarts, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playEmissionHit() {
        mSoundPool.play(
                mEmissionHit, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playEmissionPeriodical() {
        mSoundPool.play(
                mEmissionPeriodical, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playEmissionEnds() {
        mSoundPool.play(
                mEmissionEnds, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playInventoryOpen() {
        mSoundPool.play(
                mInventoryOpen, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playInventoryDrop() {
        mSoundPool.play(
                mInventoryDrop, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playInventoryUse() {
        mSoundPool.play(
                mInventoryUse, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playInventoryClose() {
        mSoundPool.play(
                mInventoryClose, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playPdaOff() {
        mSoundPool.play(
                mPdaOff, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playWTimer() {
        mSoundPool.play(
                mWTimer, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playDeath() {
        mSoundPool.play(
                mDie, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playTransmutating() {
        mSoundPool.play(
                mTransmutating, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playAbducted() {
        mSoundPool.play(
                mAbducted, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playZombify() {
        mSoundPool.play(
                mZombify, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playControlled() {
        mSoundPool.play(
                mControl, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playController() {
        mSoundPool.play(
                mController, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playBurer() {
        mSoundPool.play(
                mBurer, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playMentalHit() {
        mSoundPool.play(
                mMentalHit, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                1, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playMonolithHit() {
        mSoundPool.play(
                mMonolithHit, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playItemScanned() {
        mSoundPool.play(
                mItemScanned, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playArtefact() {
        mSoundPool.play(
                mArtefact, // Sound
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Left volume
                1f, //mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC),// / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), // Right volume
                2, // Priority
                0, // Loop
                1f // Frequency
        );
    }

    public void playControllerPresence() {
        if (!controllerPlaying) {
            this.controllerPlaying = true;
            controllerPlayer.start();
        }
    }

    public void playBurerPresence() {
        if (!burrerPlaying) {
            burrerPlaying = true;
            burrerPlayer.start();
        }
    }

    private boolean preparePlayer(MediaPlayer mediaPlayer, int resId, MediaPlayer.OnCompletionListener mp) {
        boolean prepared;
        try {
            mediaPlayer.setDataSource(ctx, getSounUri(resId));
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp);
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    mp.onCompletion(mediaPlayer);
                    return true;
                }
            });
            prepared = true;
        } catch (Exception e) {
            prepared = false;
        }
        return prepared;
    }

    private Uri getSounUri(int res) {
        return Uri.parse("android.resource://" + pckg + "/" + res);
    }
}
