export interface Transport {
  id: string;
  start: string;
  end: string;
  trend: {
    value: number;
    sample: string;
  };
  connection: {
    online: boolean;
    lastConnection: string;
  };
  plannedDuration: number;
  actualDuration: number;
  delay: number;
  delayReason?: string;
  routeProgress?: number;
  lastPosition?: TripPosition;
  map: MappedTransport;
}

export interface Size {
  width: number;
  height: number;
}

export interface MapTripPosition {
  position: TripPosition;
  icon?: {
    url?: string;
    size?: [number, number];
  };
}

export interface MappedTransport {
  positions: MapTripPosition[];
  color?: string;
}

export interface TripPosition {
  coordinates: [number, number];
  description?: string;
  stationName?: string;
  stationID?: string;
}
