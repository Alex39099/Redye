package com.github.alexqp.redye.main;

public class RedyeMaterial {

    private String configName;
    private String colorMatName;
    private String undyeMatName = "";
    private int input;
    private String vanillaGroupName;
    private boolean isUndyeable = false; // Can you dye the not dyed material normally?


    public RedyeMaterial(String configName, String colorMatName, String undyeMatName, int input, String vanillaGroupName, boolean isUndyeable) {
        this(configName, colorMatName, undyeMatName, input, vanillaGroupName);
        this.isUndyeable = isUndyeable;
    }

    public RedyeMaterial(String configName, String colorMatName, String undyeMatName, int input, String vanillaGroupName) {
        this(configName, colorMatName, input, vanillaGroupName);
        this.undyeMatName = undyeMatName;
    }

    public RedyeMaterial(String configName, String colorMatName, int input, String vanillaGroupName) {
        this.configName = configName;
        this.colorMatName = colorMatName;
        this.input = input;
        this.vanillaGroupName = vanillaGroupName;
    }

    public String getConfigName() {
        return this.configName;
    }

    public String getColorMatName() {
        return this.colorMatName;
    }

    public String getUndyeMatName() {
        return this.undyeMatName;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public String getVanillaGroupName() {
        return vanillaGroupName;
    }

    public void setVanillaGroupName(String vanillaGroupName) {
        this.vanillaGroupName = vanillaGroupName;
    }

    public boolean isUndyeable() {
        return isUndyeable;
    }

    public boolean hasUndyeMatName() {
        return !this.getUndyeMatName().equals("");
    }
}
