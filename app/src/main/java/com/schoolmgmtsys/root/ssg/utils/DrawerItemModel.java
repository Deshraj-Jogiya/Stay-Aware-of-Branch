/*******************************************************************************
 * Copyright 2013 Gabriele Mariotti
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.schoolmgmtsys.root.ssg.utils;

import android.content.Context;

/**
 *
 * Model per media_item menu
 *
 * @author gabriele
 *
 */
public class DrawerItemModel {

    public String Title;
    public String Icon;
    public int type;
    public int identifier;
    public int resIcon;
    public Class referClass;
    public String targetFragment;
    public boolean appendUserId = false;
    public String findWord;
    public String replaceWord;
    private Context mContext;

    /**
     * @param type : 1-> header, 2-> media_item, 3-> SettingItem
     */
    public DrawerItemModel(Context context, String findName, int Title, Class referClass, String Icon, int resIcon, int type, int identifier) {
        this.mContext = context;
        this.Title = Concurrent.getLangSubWords(findName, context.getResources().getString(Title));
        this.referClass = referClass;
        this.type = type;
        this.Icon = Icon;
        this.resIcon = resIcon;
        this.identifier = identifier;
    }

    public void setActivityTarget(String targetFragment) {
        this.targetFragment = targetFragment;
    }

    public void setHeadText(String findWord, String replaceWord) {
        this.findWord = findWord;
        this.replaceWord = replaceWord;
    }

}
