package hci.skywatch.model;

import com.google.gson.annotations.SerializedName;

public abstract class FlightInfo {

    private final Airport airport;

    @SerializedName("scheduled_time")
    private final String scheduledTime;
    @SerializedName("actual_time")
    private final String actualTime;

    @SerializedName("scheduled_gate_time")
    private final String scheduledGateTime;
    @SerializedName("actual_gate_time")
    private final String actualGateTime;
    @SerializedName("gate_delay")
    private final Integer gateDelay;

    @SerializedName("estimate_runway_time")
    private final String estimatedRunwayTime;
    @SerializedName("actual_runway_time")
    private final String actualRunwayTime;
    @SerializedName("runway_delay")
    private final Integer runwayDelay;

    public FlightInfo(Airport airport, String scheduledTime, String actualTime, String scheduledGateTime,
                      String actualGateTime, Integer gateDelay, String estimatedRunwayTime, String actualRunwayTime, Integer runwayDelay) {
        this.airport = airport;
        this.scheduledTime = scheduledTime;
        this.actualTime = actualTime;
        this.scheduledGateTime = scheduledGateTime;
        this.actualGateTime = actualGateTime;
        this.gateDelay = gateDelay;

        this.estimatedRunwayTime = estimatedRunwayTime;
        this.actualRunwayTime = actualRunwayTime;
        this.runwayDelay = runwayDelay;
    }

    public Airport getAirport() {
        return airport;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getActualTime() {
        return actualTime;
    }

    public String getScheduledGateTime() {
        return scheduledGateTime;
    }

    public String getActualGateTime() {
        return actualGateTime;
    }

    public Integer getGateDelay() {
        return gateDelay;
    }

    public String getEstimatedRunwayTime() {
        return estimatedRunwayTime;
    }

    public String getActualRunwayTime() {
        return actualRunwayTime;
    }

    public Integer getRunwayDelay() {
        return runwayDelay;
    }

    public static class Arrival extends FlightInfo {
        public Arrival(Airport airport, String scheduledTime, String actualTime, String scheduledGateTime, String actualGateTime, Integer gateDelay, String estimatedRunwayTime, String actualRunwayTime, Integer runwayDelay) {
            super(airport, scheduledTime, actualTime, scheduledGateTime, actualGateTime, gateDelay, estimatedRunwayTime, actualRunwayTime, runwayDelay);
        }
    }


    public static class Departure extends FlightInfo {
        public Departure(Airport airport, String scheduledTime, String actualTime, String scheduledGateTime, String actualGateTime, Integer gateDelay, String estimatedRunwayTime, String actualRunwayTime, Integer runwayDelay) {
            super(airport, scheduledTime, actualTime, scheduledGateTime, actualGateTime, gateDelay, estimatedRunwayTime, actualRunwayTime, runwayDelay);
        }
    }

    @SuppressWarnings("all")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.getClass().equals(Departure.class)) {
            sb.append("Departure: ");
        } else {
            sb.append("Arrival: ");
        }
        sb.append("\nAirport: " + airport);
        sb.append("\nScheduledTime: " + scheduledTime);
        sb.append("\nActualTime: " + actualTime);
        sb.append("\nScheduledGateTime: " + scheduledGateTime);
        sb.append("\nActualGateTime: " + actualGateTime);
        sb.append("\nGateDelay: " + gateDelay);
        sb.append("\nEstimatedRunwayTime: " + estimatedRunwayTime);
        sb.append("\nActualRunwayTime: " + actualGateTime);
        sb.append("\nRunwayDelay: " + runwayDelay);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!o.getClass().equals(this.getClass())) {
            return false;
        }

        FlightInfo fi = (FlightInfo) o;

        if (scheduledTime == null) {
            if (fi.scheduledTime != null) {
                return false;
            }
        } else if (!scheduledTime.equals(fi.scheduledTime)) {
            return false;
        }

        if (actualTime == null) {
            if (fi.actualTime != null) {
                return false;
            }
        } else if (!actualTime.equals(fi.actualTime)) {
            return false;
        }

        if (scheduledGateTime == null) {
            if (fi.scheduledGateTime != null) {
                return false;
            }
        } else if (!scheduledGateTime.equals(fi.scheduledGateTime)) {
            return false;
        }

        if (actualGateTime == null) {
            if (fi.actualGateTime != null) {
                return false;
            }
        } else if (!actualGateTime.equals(fi.actualGateTime)) {
            return false;
        }

        if (gateDelay == null) {
            if (fi.gateDelay != null) {
                return false;
            }
        } else if (!gateDelay.equals(fi.gateDelay)) {
            return false;
        }

        if (estimatedRunwayTime == null) {
            if (fi.estimatedRunwayTime != null) {
                return false;
            }
        } else if (!estimatedRunwayTime.equals(fi.estimatedRunwayTime)) {
            return false;
        }

        if (actualRunwayTime == null) {
            if (fi.actualRunwayTime != null) {
                return false;
            }
        } else if (!actualRunwayTime.equals(fi.actualRunwayTime)) {
            return false;
        }

        if (runwayDelay == null) {
            if (fi.runwayDelay != null) {
                return false;
            }
        } else if (!runwayDelay.equals(fi.runwayDelay)) {
            return false;
        }

        return airport.equals(fi.getAirport());
    }
}
