package net.afterday.compas.persistency.items;

import static net.afterday.compas.core.inventory.items.Item.CATEGORY;
import static net.afterday.compas.core.inventory.items.Item.MODIFIERS_COUNT;

import androidx.annotation.NonNull;

import net.afterday.compas.core.inventory.items.Item;

import java.util.Arrays;

/**
 * Created by Justas Spakauskas on 3/25/2018.
 */

public class ItemDescriptorImpl implements ItemDescriptor {
    private static final String TAG = "ItemDescriptorImpl";
    private int imageId;
    private String title;
    private int titleId = -1;
    private double[] modifiers = new double[MODIFIERS_COUNT];
    private boolean isArtifact = false;
    private boolean isBooster = false;
    private boolean isDevice = false;
    private boolean isArmor = false;
    private long duration;
    private int xpPoints;
    private String description;
    private boolean isConsumable = true;
    private boolean isUsable = false;
    private boolean isDropable = true;
    private int descriptionId = -1;
    private CATEGORY category;

    public ItemDescriptorImpl(CATEGORY category) {
        this.category = category;
        initModifiers();
    }

    public ItemDescriptorImpl(CATEGORY category, String title) {
        this.title = title;
        this.category = category;
        initModifiers();
    }

    public ItemDescriptorImpl(CATEGORY category, int titleId) {
        this.titleId = titleId;
        this.category = category;
        initModifiers();
    }

    private void initModifiers() {
        Arrays.fill(modifiers, NULL_MODIFIER);
    }

    public ItemDescriptorImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public ItemDescriptorImpl setTitleId(int titleId) {
        this.titleId = titleId;
        return this;
    }

    public ItemDescriptorImpl addModifier(int modifier, double value) {
        modifiers[modifier] = value;
        return this;
    }

    @Override
    public int getImage() {
        return imageId;
    }

    public ItemDescriptorImpl setImage(int image) {
        this.imageId = image;
        return this;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public int getNameId() {
        return titleId;
    }

    @Override
    public boolean isBooster() {
        return isBooster;
    }

    public ItemDescriptorImpl setBooster(boolean isBooster) {
        this.isBooster = isBooster;
        return this;
    }

    @Override
    public boolean isDevice() {
        return isDevice;
    }

    public ItemDescriptorImpl setDevice(boolean isDevice) {
        this.isDevice = isDevice;
        return this;
    }

    @Override
    public boolean isArmor() {
        return isArmor;
    }

    public ItemDescriptorImpl setArmor(boolean isArmor) {
        this.isArmor = isArmor;
        return this;
    }

    @Override
    public boolean isArtefact() {
        return isArtifact;
    }

    public ItemDescriptorImpl setArtefact(boolean isArtifact) {
        this.isArtifact = isArtifact;
        return this;
    }

    @Override
    public boolean isSingleUse() {
        return !(isArtifact || isBooster || isDevice || isArmor);
    }

    @Override
    public boolean isConsumable() {
        return isConsumable;
    }

    public ItemDescriptorImpl setConsumable(boolean isConsumable) {
        this.isConsumable = isConsumable;
        return this;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public ItemDescriptorImpl setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public double[] getModifiers() {
        return modifiers;
    }

    @Override
    public int getXpPoints() {
        return xpPoints;
    }

    public ItemDescriptorImpl setXpPoints(int xpPoints) {
        this.xpPoints = xpPoints;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public ItemDescriptorImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemDescriptorImpl setDescription(int descriptionId) {
        this.descriptionId = descriptionId;
        return this;
    }

    @Override
    public int getDescriptionId() {
        return descriptionId;
    }

    @Override
    public boolean isUsable() {
        return isUsable;
    }

    public ItemDescriptorImpl setUsable(boolean isUsable) {
        this.isUsable = isUsable;
        return this;
    }

    @Override
    public boolean isDropable() {
        return isDropable;
    }

    public ItemDescriptorImpl setDropable(boolean isDropable) {
        this.isDropable = isDropable;
        return this;
    }

    @Override
    public CATEGORY getCategory() {
        return category;
    }

    public ItemDescriptorImpl setCategory(Item.CATEGORY category) {
        this.category = category;
        return this;
    }

    @NonNull
    public String toString() {
        return "";
    }

}
