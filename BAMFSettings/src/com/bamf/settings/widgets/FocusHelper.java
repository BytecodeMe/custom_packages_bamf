/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bamf.settings.widgets;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TabHost;

/**
 * A keyboard listener we set on the last tab button in AppsCustomize to jump to then
 * market icon and vice versa.
 */
class SettingsTabKeyEventListener implements View.OnKeyListener {
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return FocusHelper.handleSettingsTabKeyEvent(v, keyCode, event);
    }
}

public class FocusHelper {
    /**
     * Private helper to get the parent TabHost in the view hiearchy.
     */
    private static TabHost findTabHostParent(View v) {
        ViewParent p = v.getParent();
        while (p != null && !(p instanceof TabHost)) {
            p = p.getParent();
        }
        return (TabHost) p;
    }

    /**
     * Handles key events in a AppsCustomize tab between the last tab view and the shop button.
     */
    static boolean handleSettingsTabKeyEvent(View v, int keyCode, KeyEvent e) {

        final int action = e.getAction();
        final boolean handleKeyEvent = (action != KeyEvent.ACTION_UP);
        boolean wasHandled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (handleKeyEvent) {
                    
                }
                wasHandled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (handleKeyEvent) {
                    // Select the content view (down is handled by the tab key handler otherwise)
                    
                }
                break;
            default: break;
        }
        return wasHandled;
    }

    

    /**
     * Handles key events in a PageViewCellLayout containing PagedViewIcons.
     */
    static boolean handleSettingsKeyEvent(View v, int keyCode, KeyEvent e) {
        
        // Note we have an extra parent because of the
        // PagedViewCellLayout/PagedViewCellLayoutChildren relationship
//        final PagedView container = (PagedView) parentLayout.getParent();
//        final TabHost tabHost = findTabHostParent(container);
//        final TabWidget tabs = (TabWidget) tabHost.findViewById(android.R.id.tabs);
//        final int iconIndex = itemContainer.indexOfChild(v);
//        final int itemCount = itemContainer.getChildCount();
//        final int pageIndex = ((PagedView) container).indexToPage(container.indexOfChild(parentLayout));
//        final int pageCount = container.getChildCount();
//
//        final int x = iconIndex % countX;
//        final int y = iconIndex / countX;
//
//        final int action = e.getAction();
//        final boolean handleKeyEvent = (action != KeyEvent.ACTION_UP);
//        ViewGroup newParent = null;
//        // Side pages do not always load synchronously, so check before focusing child siblings
//        // willy-nilly
//        View child = null;
//        boolean wasHandled = false;
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DPAD_LEFT:
//                if (handleKeyEvent) {
//                    // Select the previous icon or the last icon on the previous page
//                    if (iconIndex > 0) {
//                        itemContainer.getChildAt(iconIndex - 1).requestFocus();
//                    } else {
//                        if (pageIndex > 0) {
//                            newParent = getSettingsPage(container, pageIndex - 1);
//                            if (newParent != null) {
//                                container.snapToPage(pageIndex - 1);
//                                child = newParent.getChildAt(newParent.getChildCount() - 1);
//                                if (child != null) child.requestFocus();
//                            }
//                        }
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_DPAD_RIGHT:
//                if (handleKeyEvent) {
//                    // Select the next icon or the first icon on the next page
//                    if (iconIndex < (itemCount - 1)) {
//                        itemContainer.getChildAt(iconIndex + 1).requestFocus();
//                    } else {
//                        if (pageIndex < (pageCount - 1)) {
//                            newParent = getSettingsPage(container, pageIndex + 1);
//                            if (newParent != null) {
//                                container.snapToPage(pageIndex + 1);
//                                child = newParent.getChildAt(0);
//                                if (child != null) child.requestFocus();
//                            }
//                        }
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_DPAD_UP:
//                if (handleKeyEvent) {
//                    // Select the closest icon in the previous row, otherwise select the tab bar
//                    if (y > 0) {
//                        int newiconIndex = ((y - 1) * countX) + x;
//                        itemContainer.getChildAt(newiconIndex).requestFocus();
//                    } else {
//                        tabs.requestFocus();
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_DPAD_DOWN:
//                if (handleKeyEvent) {
//                    // Select the closest icon in the previous row, otherwise do nothing
//                    if (y < (countY - 1)) {
//                        int newiconIndex = Math.min(itemCount - 1, ((y + 1) * countX) + x);
//                        itemContainer.getChildAt(newiconIndex).requestFocus();
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_ENTER:
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                if (handleKeyEvent) {
//                    // Simulate a click on the icon
//                    View.OnClickListener clickListener = (View.OnClickListener) container;
//                    clickListener.onClick(v);
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_PAGE_UP:
//                if (handleKeyEvent) {
//                    // Select the first icon on the previous page, or the first icon on this page
//                    // if there is no previous page
//                    if (pageIndex > 0) {
//                        newParent = getSettingsPage(container, pageIndex - 1);
//                        if (newParent != null) {
//                            container.snapToPage(pageIndex - 1);
//                            child = newParent.getChildAt(0);
//                            if (child != null) child.requestFocus();
//                        }
//                    } else {
//                        itemContainer.getChildAt(0).requestFocus();
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_PAGE_DOWN:
//                if (handleKeyEvent) {
//                    // Select the first icon on the next page, or the last icon on this page
//                    // if there is no next page
//                    if (pageIndex < (pageCount - 1)) {
//                        newParent = getSettingsPage(container, pageIndex + 1);
//                        if (newParent != null) {
//                            container.snapToPage(pageIndex + 1);
//                            child = newParent.getChildAt(0);
//                            if (child != null) child.requestFocus();
//                        }
//                    } else {
//                        itemContainer.getChildAt(itemCount - 1).requestFocus();
//                    }
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_MOVE_HOME:
//                if (handleKeyEvent) {
//                    // Select the first icon on this page
//                    itemContainer.getChildAt(0).requestFocus();
//                }
//                wasHandled = true;
//                break;
//            case KeyEvent.KEYCODE_MOVE_END:
//                if (handleKeyEvent) {
//                    // Select the last icon on this page
//                    itemContainer.getChildAt(itemCount - 1).requestFocus();
//                }
//                wasHandled = true;
//                break;
//            default: break;
//        }
        return true;
    }

    /**
     * Handles key events in the tab widget.
     */
    static boolean handleTabKeyEvent(AccessibleTabView v, int keyCode, KeyEvent e) {
        
        final FocusOnlyTabWidget parent = (FocusOnlyTabWidget) v.getParent();
        final TabHost tabHost = findTabHostParent(parent);
        final ViewGroup contents = (ViewGroup)
                tabHost.findViewById(android.R.id.tabcontent);
        final int tabCount = parent.getTabCount();
        final int tabIndex = parent.getChildTabIndex(v);

        final int action = e.getAction();
        final boolean handleKeyEvent = (action != KeyEvent.ACTION_UP);
        boolean wasHandled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (handleKeyEvent) {
                    // Select the previous tab
                    if (tabIndex > 0) {
                        parent.getChildTabViewAt(tabIndex - 1).requestFocus();
                    }
                }
                wasHandled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (handleKeyEvent) {
                    // Select the next tab, or if the last tab has a focus right id, select that
                    if (tabIndex < (tabCount - 1)) {
                        parent.getChildTabViewAt(tabIndex + 1).requestFocus();
                    } else {
                        if (v.getNextFocusRightId() != View.NO_ID) {
                            tabHost.findViewById(v.getNextFocusRightId()).requestFocus();
                        }
                    }
                }
                wasHandled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // Do nothing
                wasHandled = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (handleKeyEvent) {
                    // Select the content view
                    contents.requestFocus();
                }
                wasHandled = true;
                break;
            default: break;
        }
        return wasHandled;
    }    
}
