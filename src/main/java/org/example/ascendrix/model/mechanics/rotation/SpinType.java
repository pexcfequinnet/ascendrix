package org.example.ascendrix.model.mechanics.rotation;

public enum SpinType {
    MINI_T_SPIN,
    T_SPIN,
    MINI_L_SPIN,
    MINI_S_SPIN,
    MINI_Z_SPIN,
    MINI_I_SPIN,
    MINI_J_SPIN,
    NONE;

    public boolean isSpin() {
        return this != NONE;
    }

    public boolean isMini() {
        return this == MINI_T_SPIN
                || this == MINI_L_SPIN
                || this == MINI_S_SPIN
                || this == MINI_Z_SPIN
                || this == MINI_I_SPIN
                || this == MINI_J_SPIN;
    }
}