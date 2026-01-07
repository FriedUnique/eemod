package net.friedunique.eemod.core;

import net.minecraft.core.BlockPos;

public interface IElectricalConductor {
    // Spatial Data
    BlockPos getPos();

    void tick(); // called for temperature operations or someting

    // Electrical Properties
    double getBaseResistance(); // Resistance at 20°C
    double getTemperature();    // Current temp in Celsius
    double getTempCoefficient(); // How much R increases as it gets hot

    default double getEffectiveResistance() {
        double deltaT = getTemperature() - 20;
        return getBaseResistance() * (1 + getTempCoefficient() * deltaT);
    }

    // Capacity (Maximum Amps before the wire catches fire)
    double getMaxCurrent();
}