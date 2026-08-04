package io.izzel.arclight.common.mod.permission;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;

public class ArclightDummyPermissible extends PermissibleBase {

    public static final ServerOperator DUMMY_OPERATOR = new ServerOperator() {
        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean b) {}
    };

    public ArclightDummyPermissible() {
        super(DUMMY_OPERATOR);
    }
}
