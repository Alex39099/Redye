/*
 * Copyright (C) 2018-2022 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.redye.main;

public class RedyeMaterial {

    private final String configName;
    private final String colorMatName;
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
