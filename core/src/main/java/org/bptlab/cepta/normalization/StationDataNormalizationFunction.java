package org.bptlab.cepta.normalization;

import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.events.station.StationDataOuterClass;
import org.bptlab.cepta.models.internal.modalities.rail.Rail;
import org.bptlab.cepta.models.internal.station.StationOuterClass.Station;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.models.internal.types.country.CountryOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.types.transport.Transport;
import org.bptlab.cepta.utils.IDGenerator;
import org.bptlab.cepta.models.internal.event.Event.NormalizedEvent;
import org.bptlab.cepta.models.events.event.EventOuterClass.Event;

public class StationDataNormalizationFunction extends NormalizationFunction {

    @Override
    public void flatMap(Event event, Collector<NormalizedEvent> collector) throws Exception {
        if (!event.hasStation()) return;
        StationDataOuterClass.StationData stationEvent = event.getStation();

        Station.Builder station = Station.newBuilder();

        String countryCode = String.valueOf(stationEvent.getCountryId());

        station.setCeptaId(
                Ids.CeptaStationID.newBuilder()
                        .setId(IDGenerator.hashed(stationEvent.getStationCode()))
                        .build());
        station.setName(stationEvent.getStationName());
        station.setAbbreviation(stationEvent.getStationAbbreviation());
        station.setCountry(
                CountryOuterClass.Country.newBuilder()
                        .setCode(countryCode)
                        .setCeptaId(Ids.CeptaCountryID.newBuilder().setId(countryCode).build())
                        .build());
        station.setPosition(
                CoordinateOuterClass.Coordinate.newBuilder()
                        .setLatitude(stationEvent.getLatitude())
                        .setLatitude(stationEvent.getLongitude())
                        .build());
        station.setType(Transport.TransportType.RAIL);
        station.setRail(
                Rail.RailStation.newBuilder()
                        .build());

        collector.collect(NormalizedEvent.newBuilder().setStation(station.build()).build());
    }
}
