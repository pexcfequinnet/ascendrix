package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.LockDelay;
import org.example.ascendrix.model.mechanics.MovementProcessor;
import org.example.ascendrix.model.mechanics.rotation.RotationSystem;
import org.example.ascendrix.model.state.HUDState;

public abstract class BaseModeRuleset implements ModeRuleset {

    protected final GameMode mode;
    protected final MovementProcessor handling;
    protected final GravityModel gravity;
    protected final LockDelay lockDelay;
    protected final ARETimer are;
    protected final RotationSystem rotationSystem;
    protected final HUDState hudState = new HUDState();

    protected BaseModeRuleset(GameMode mode,
                              MovementProcessor handling,
                              GravityModel gravity,
                              LockDelay lockDelay,
                              ARETimer are,
                              RotationSystem rotationSystem) {
        this.mode = mode;
        this.handling = handling;
        this.gravity = gravity;
        this.lockDelay = lockDelay;
        this.are = are;
        this.rotationSystem = rotationSystem;
    }

    @Override
    public GameMode getMode()                  { return mode; }

    @Override
    public MovementProcessor getHandling()     { return handling; }

    @Override
    public GravityModel getGravity()           { return gravity; }

    @Override
    public LockDelay getLockDelay()            { return lockDelay; }

    @Override
    public ARETimer getARE()                   { return are; }

    @Override
    public RotationSystem getRotationSystem()  { return rotationSystem; }

    @Override
    public HUDState getHUDState()              { return hudState; }
}