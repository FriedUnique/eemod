package net.friedunique.eemod.core;

public interface IVoltageSource {

    double getBaseVoltage();
    double getInternalResistance();
    double getShortCircuitCurrent();

}
