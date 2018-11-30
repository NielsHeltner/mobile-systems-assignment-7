package sems.raarchitecturalprototype.interfaces;

import android.location.Location;

public interface ILocationCalculator {

    Location calculate(double lat, double lon, float speed, float bearing);

}
