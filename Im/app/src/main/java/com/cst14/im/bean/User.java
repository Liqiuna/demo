package com.cst14.im.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name = "";
    private long lastGroupListUpdateTime;
    private Map<Integer, UserGroup> groups = new HashMap<>();

    public User() {
        lastGroupListUpdateTime = 0;
    }

    public void reset() {
        name = "";
        groups = new LinkedHashMap<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGroups(List<UserGroup> groupList) {
        this.groups.clear();
        for (UserGroup userGroup : groupList) {
            this.groups.put(userGroup.getID(), userGroup);
        }
    }

    public List<UserGroup> getGroups() {
        List<UserGroup> groupList = new ArrayList<>();
        for (Map.Entry<Integer, UserGroup> entry : groups.entrySet()) {
            groupList.add(entry.getValue());
        }
        return groupList;
    }

    public UserGroup getGroup(int groupID) {
        if (groupID < 0 || !this.groups.containsKey(groupID)) {
            return null;
        }
        return groups.get(groupID);
    }

    public boolean hasGroup(int groupID) {
        if (groupID < 0 || !this.groups.containsKey(groupID)) {
            return false;
        }
        return true;
    }

    public void setLastGroupListUpdateTime(long lastGroupListUpdateTime) {
        this.lastGroupListUpdateTime = lastGroupListUpdateTime;
    }

    public long getLastGroupListUpdateTime() {
        return lastGroupListUpdateTime;
    }
}
