export interface TripPosition {
  coordinates: Coordinate;
  description: string;
}

export interface Transport {
  id: string;
  start: string;
  end: string;
  plannedDuration: number;
  actualDuration: number;
  delay: number;
  delayReason?: string;
  lastPosition?: TripPosition;
  map: MappedTransport;
}

export interface Coordinate {
  lat: number;
  lon: number;
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
