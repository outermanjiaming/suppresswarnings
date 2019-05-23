package com.suppresswarnings.android.model;

import com.suppresswarnings.android.utils.ActionType;

public class Actions {
    ActionType actionType;
    String input;

    public Actions(ActionType actionType) {
        this.actionType = actionType;
    }

    public Actions(ActionType actionType, String input) {
        this.actionType = actionType;
        this.input = input;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getInput() {
        return input;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public String toString() {
        return "Actions{" +
                "actionType=" + actionType +
                ", input='" + input + '\'' +
                '}';
    }
}
