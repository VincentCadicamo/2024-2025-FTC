package org.firstinspires.ftc.teamcode.main.autonomous.sensors.distance;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.main.autonomous.sensors.SensorWrapper;

public class DistanceSensorWrapper implements SensorWrapper {

    DistanceUnit units = DistanceUnit.CM;
    Rev2mDistanceSensor sensor;
    HardwareMap hardwareMap;
    String name;


    public DistanceSensorWrapper(HardwareMap hardwareMap, String name) {
        this.hardwareMap = hardwareMap;
        this.name = name;

        sensor = (Rev2mDistanceSensor)hardwareMap.get(DistanceSensor.class, name);
    }

    @Override
    public void setUnits(DistanceUnit unit) {
        units = unit;
    }

    @Override
    public double getData() {
        return sensor.getDistance(units);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean didTimeoutOccur() {
        return sensor.didTimeoutOccur();
    }
}
