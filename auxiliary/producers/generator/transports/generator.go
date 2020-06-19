package main

import (
	"context"
	"fmt"
	//"strconv"
	"math"
	"math/rand"
	"os"
	"os/signal"
	//"strings"
	"sync"
	"syscall"
	"time"
	"sort"

	"github.com/Shopify/sarama"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"

	"github.com/paulmach/orb"
	"github.com/paulmach/orb/planar"
	"gonum.org/v1/plot"
	"gonum.org/v1/plot/plotter"
	"gonum.org/v1/plot/plotutil"
  //"gonum.org/v1/plot/vg"

	"github.com/bptlab/cepta/ci/versioning"
	//"github.com/bptlab/cepta/models/internal/delay"
	//notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	livepb "github.com/bptlab/cepta/models/events/livetraindataevent"
	"github.com/bptlab/cepta/models/internal/station"
	"github.com/bptlab/cepta/models/internal/types/coordinate"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/transport"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	//durationpb "github.com/golang/protobuf/ptypes/duration"
	"github.com/google/uuid"
	"github.com/k0kubun/pp"
	"github.com/yourbasic/graph"

	topics "github.com/bptlab/cepta/models/constants/topic"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

// German InfrastructureManager (DB Netz) --> 80
const germIm int64 = 80

var mapLong = make(map[string][]int)
var mapLat = make(map[string][]int)

// Config ....
type Config struct {
	Speedup      int64
	Jitter       int
	TransportIDs int64
	Transports   map[int64][]int
	Nodes        int64
	Edges        int64
}

// Generator ....
type Generator struct {
	Config
	ProducerConfig kafkaproducer.Config

	graph       *graph.Mutable
	stations    []*station.Station
	producer    *kafkaproducer.Producer
	done        chan bool
	cancelSetup context.Context
}

func roundTo(x, decimalPoints float64 ) float64 {
	decimals := math.Pow(10, decimalPoints)
    return math.Round(x * decimals) / decimals
}

func (gen *Generator) generateUniqueSourceID() string {
	for {
		if id, err := uuid.NewRandom(); err != nil {
			return id.String()
		}
	}
}
func (gen *Generator) coordinatesInPolygon(long, lat float64, spanningCoordinates [][]float64) bool{
	var ring orb.Ring

	for _, val := range spanningCoordinates {
	  point := orb.Point{val[0], val[1]}
	  ring = append(ring, point)
	}
	ring = append(ring, orb.Point{spanningCoordinates[0][0], spanningCoordinates[0][1]})
  polygon := orb.Polygon{ring}

	pt1 := orb.Point{long, lat}

	return planar.PolygonContains(polygon, pt1)
}

func (gen *Generator) generateCoordinates(minLong, maxLong, minLat, maxLat float64, spanningCoordinates [][]float64, si int) *coordinate.Coordinate {
	long := roundTo(minLong + rand.Float64()*(maxLong-minLong), 3)
	lat := roundTo(minLat + rand.Float64()*(maxLat-minLat), 3)

	if gen.coordinatesInPolygon(long, lat, spanningCoordinates){
    mapLong[fmt.Sprintf("%f", roundTo(long, 1))] = append(mapLong[fmt.Sprintf("%f", roundTo(long, 1))], si)
    mapLat[fmt.Sprintf("%f", roundTo(lat, 1))] = append(mapLat[fmt.Sprintf("%f", roundTo(lat, 1))], si)
		return &coordinate.Coordinate{Longitude: long, Latitude: lat}
	}
	return gen.generateCoordinates(minLong, maxLong, minLat, maxLat, spanningCoordinates, si)
}

func (gen *Generator) extractExtremeValues(spanningCoordinates [][]float64) (float64, float64, float64, float64) {
	minLong, maxLong, minLat, maxLat := spanningCoordinates[0][0], spanningCoordinates[0][0], spanningCoordinates[0][1], spanningCoordinates[0][1]
	for _, val := range spanningCoordinates {
		if val[0] < minLong {
			minLong = val[0]
		}
		if val[0] > maxLong {
			maxLong = val[0]
		}
		if val[1] < minLat {
			minLat = val[1]
		}
		if val[1] > maxLat {
			maxLat = val[1]
		}
	}
	return minLong, maxLong, minLat, maxLat
}

func (gen *Generator) FindDuplicates(neighbours []int, si int) []int {
  last := -1
  var duplicates []int
  sort.Ints(neighbours)

  for _, i := range neighbours {
    if i == si {
      continue
    }
    if i == last {
      duplicates = append(duplicates, i)
    }
    last = i
  }
  return duplicates
}

func (gen *Generator) FindConnections(si int) []int{
	var duplicates []int
	latClass :=  roundTo(gen.stations[si].GetPosition().GetLatitude(), 1)
	longClass := roundTo(gen.stations[si].GetPosition().GetLongitude(), 1)

  distance := 0.0
  var neighbours []int
  for len(duplicates) < 4 {

    if distance > 0 {
      neighbours = append(neighbours, mapLat[fmt.Sprintf("%f", roundTo(latClass + distance, 1))]...)
      neighbours = append(neighbours, mapLat[fmt.Sprintf("%f", roundTo(latClass - distance, 1))]...)
      neighbours = append(neighbours, mapLong[fmt.Sprintf("%f", roundTo(longClass + distance, 1))]...)
      neighbours = append(neighbours, mapLong[fmt.Sprintf("%f", roundTo(longClass - distance, 1))]...)
    } else {
      neighbours = append(neighbours, mapLat[fmt.Sprintf("%f", roundTo(latClass + distance, 1))]...)
      neighbours = append(neighbours, mapLong[fmt.Sprintf("%f", roundTo(longClass + distance, 1))]...)
    }


    distance += 0.1
    if distance > 1 {
     break
    }
    duplicates = gen.FindDuplicates(neighbours, si)
  }
  //log.Infof("duplicates: %d", duplicates)
  return duplicates
}

func (gen *Generator) GenerateTransportNetwork() *graph.Mutable{
	/*
	// Abstract europe layout as a parallelogram
  // NW: Rennes, France
	coordinatesEurope = append(coordinatesEurope, []float64{48.111, -1.626})
	// SW: Andorra
	coordinatesEurope = append(coordinatesEurope, []float64{42.5482, 1.575})
	// SE: Budapest, Hungary
	coordinatesEurope = append(coordinatesEurope, []float64{47.495, 19.079})
	// NE: Danzig, Poland
	coordinatesEurope = append(coordinatesEurope, []float64{54.359, 18.638})

  // Coordinates of europe in more detail
	coordinatesEurope = append(coordinatesEurope, []float64{48.382073, -4.501661})
	coordinatesEurope = append(coordinatesEurope, []float64{43.393383, -1.425489})
	coordinatesEurope = append(coordinatesEurope, []float64{42.871394, -8.552323})
	coordinatesEurope = append(coordinatesEurope, []float64{37.087569, -8.250044})
	coordinatesEurope = append(coordinatesEurope, []float64{36.858936, -2.097021})
	coordinatesEurope = append(coordinatesEurope, []float64{43.316295, 3.025443})
	coordinatesEurope = append(coordinatesEurope, []float64{44.379382, 9.758721})
	coordinatesEurope = append(coordinatesEurope, []float64{39.184855, 16.412316})
	coordinatesEurope = append(coordinatesEurope, []float64{37.875888, 12.667179})
	coordinatesEurope = append(coordinatesEurope, []float64{36.830787, 14.938166})
	coordinatesEurope = append(coordinatesEurope, []float64{40.651505, 17.089628})
	coordinatesEurope = append(coordinatesEurope, []float64{45.479411, 11.950025})
	coordinatesEurope = append(coordinatesEurope, []float64{47.495, 19.079})
	coordinatesEurope = append(coordinatesEurope, []float64{54.359, 18.638})
	coordinatesEurope = append(coordinatesEurope, []float64{53.114058, 5.559876})
	*/

	// CoordinatesGermany will save the spanning coordinates for germany
	var coordinatesGermany [][]float64

	coordinatesGermany = append(coordinatesGermany, []float64{54.104461, 13.643508})
	coordinatesGermany = append(coordinatesGermany, []float64{53.893748, 10.543708})
	coordinatesGermany = append(coordinatesGermany, []float64{54.730220, 9.847835})
	coordinatesGermany = append(coordinatesGermany, []float64{54.815364, 8.793481})
	coordinatesGermany = append(coordinatesGermany, []float64{53.825347, 9.120331})
	coordinatesGermany = append(coordinatesGermany, []float64{53.462838, 7.138146})
	coordinatesGermany = append(coordinatesGermany, []float64{50.784750, 6.145043})
	coordinatesGermany = append(coordinatesGermany, []float64{49.495443, 6.513806})
	coordinatesGermany = append(coordinatesGermany, []float64{48.969793, 8.267557})
	coordinatesGermany = append(coordinatesGermany, []float64{47.631918, 7.609116})
	coordinatesGermany = append(coordinatesGermany, []float64{47.716537, 12.800814})
	coordinatesGermany = append(coordinatesGermany, []float64{48.143119, 12.679619})
	coordinatesGermany = append(coordinatesGermany, []float64{48.725042, 13.772803})
	coordinatesGermany = append(coordinatesGermany, []float64{50.301217, 12.018625})
	coordinatesGermany = append(coordinatesGermany, []float64{51.002372, 14.815140})
	coordinatesGermany = append(coordinatesGermany, []float64{53.702927, 14.123163})

	minLong, maxLong, minLat, maxLat := gen.extractExtremeValues(coordinatesGermany)

	// Generate stations
	gen.stations = make([]*station.Station, gen.Nodes)
	for si := range gen.stations {
		gen.stations[si] = &station.Station{
			CeptaId:  &ids.CeptaStationID{Id: fmt.Sprintf("Gen-Station%d", si)},
			Name:     fmt.Sprintf("Station %d", si),
			Position: gen.generateCoordinates(minLong, maxLong, minLat, maxLat, coordinatesGermany, si),
			Type:     transport.TransportType_RAIL,
		}
	}

	g := graph.New(int(gen.Nodes))
	for si := range gen.stations {
		neighbours := gen.FindConnections(si)

		for _, i := range neighbours {
		  g.AddBoth(si, i)
		}
	}
	gen.plot(g)

	return g

}

func (gen *Generator) worker() {
	// time.AfterFunc(4*time.Hour, func() { destroyObject("idofobject") })
	//
}

func (gen *Generator) receiveCoordinates() plotter.XYs {
  pts := make(plotter.XYs, gen.Nodes)
	for i := range pts {
		pts[i].X = gen.stations[i].GetPosition().GetLatitude()
		pts[i].Y = gen.stations[i].GetPosition().GetLongitude()
	}
	return pts
}

func (gen *Generator) plot(g *graph.Mutable) {
  p, err := plot.New()
  if err != nil {
    panic(err)
  }

  p.Title.Text = "Mock Stations"
  p.X.Label.Text = "X"
  p.Y.Label.Text = "Y"

  err = plotutil.AddScatters(p,
    "Stations", gen.receiveCoordinates())
  if err != nil {
    panic(err)
  }

  /*
  Adding Tracklines
  */
  n := g.Order()
  for v := 0; v<n; v++ {
    g.Visit(v, func(w int, c int64) (skip bool) {
        pts := make(plotter.XYs, 2)
        pts[0].X = gen.stations[v].GetPosition().GetLatitude()
        pts[0].Y = gen.stations[v].GetPosition().GetLongitude()
        pts[1].X = gen.stations[w].GetPosition().GetLatitude()
        pts[1].Y = gen.stations[w].GetPosition().GetLongitude()
        err = plotutil.AddLines(p, pts)
        if err != nil {
          panic(err)
        }
        return
    })
  }
  /*
  // Save the plot to a PNG file.
  if err := p.Save(10*vg.Inch, 10*vg.Inch, "/Users/leonardpetter/Development/bp1920/cepta/auxiliary/producers/generator/transports/Graph.png"); err != nil {
    panic(err)
  }
  */
}

func (gen *Generator) CalculateDistance (lat1, lng1, lat2, lng2 float64) float64 {
  // Calculating Distance algorithm from GeoDataSource (https://www.geodatasource.com/developers/go)
  radlat1 := float64(math.Pi * lat1 / 180)
	radlat2 := float64(math.Pi * lat2 / 180)

	theta := float64(lng1 - lng2)
	radtheta := float64(math.Pi * theta / 180)

	dist := math.Sin(radlat1) * math.Sin(radlat2) + math.Cos(radlat1) * math.Cos(radlat2) * math.Cos(radtheta)

	if dist > 1 {
		dist = 1
	}

	dist = math.Acos(dist)
	dist = dist * 180 / math.Pi * 60 * 1.1515 * 1.609344

	return dist
}

func (gen *Generator) StartProducing (wg *sync.WaitGroup, tid int64){
  path := gen.Transports[tid]
  defer wg.Done()
  for {
    var lastStation int
    var arrival float64
    var dist []float64
    for i, station := range path {
      if i > 0 {
        lastStation = path[i-1]
      } else {
        lastStation = station
      }
      d := gen.CalculateDistance(gen.stations[lastStation].GetPosition().GetLatitude(), gen.stations[lastStation].GetPosition().GetLongitude(), gen.stations[station].GetPosition().GetLatitude(), gen.stations[station].GetPosition().GetLongitude())
      dist = append(dist, d)
      arrival = arrival + d
    }

    startTime := time.Now()
    // train drives approximately 90km/h
    endTime := startTime.Add(time.Duration(arrival/90*60) * time.Minute)

    for i, d := range dist {
      select {
      case <-gen.done:
        return
      default:
        // waiting time until the distance is driven and the event can be emitted
        time.Sleep(time.Minute * time.Duration(d/90*60) / time.Duration(gen.Speedup))
        ingestionTime := time.Now()
        ingestionTimeProto, err := ptypes.TimestampProto(ingestionTime)
        if err != nil {
            log.Error("Failed to parse ingestionTime to proto.Timestamp")
        }
        eventTime := ingestionTime.Add(time.Duration(rand.Intn(gen.Jitter)) * time.Minute * -1)
        eventTimeProto, err := ptypes.TimestampProto(eventTime)
        if err != nil {
            log.Error("Failed to parse eventTime to proto.Timestamp")
        }
        // Same for all stations
        plannedArrival := endTime
        plannedArrivalProto, err := ptypes.TimestampProto(plannedArrival)
        if err != nil {
            log.Error("Failed to parse plannedArrival to proto.Timestamp")
        }

        var status int64
        var delay int64
        if i == 0 {
          status = 2
        } else if i==len(path)-1 {
          status = 1
          delay = int64(eventTime.Sub(plannedArrival).Minutes())
        } else {
          status = 5
        }

        liveEvent := &livepb.LiveTrainData{
            Id:           int64(rand.Intn(int(gen.Nodes*gen.Edges))),
            TrainSectionId: tid*3,
            StationId:    int64(path[i]),
            EventTime:    eventTimeProto,
            Status:       status, // drive-through
            FirstTrainId: tid,
            TrainId:      tid,
            PlannedArrivalTimeEndStation:   plannedArrivalProto,
            Delay:        delay,
            EndStationId: int64(path[len(path)-1]),
            ImId:           germIm,
            FollowingImId:  germIm,
            MessageStatus:  1, //created
            IngestionTime:  ingestionTimeProto,
        }

        eventBytes, err := proto.Marshal(liveEvent)
        if err != nil {
          log.Error("Failed to marshal notification: %v", err)
          break
        }
        gen.producer.Send(topics.Topic_LIVE_TRAIN_DATA.String(), topics.Topic_LIVE_TRAIN_DATA.String(), sarama.ByteEncoder(eventBytes))
        log.Debugf("Sent LiveTrainData event with trainId=%d at station=%d with status=%d to kafka topic=%s", liveEvent.GetTrainId(), liveEvent.GetStationId(), liveEvent.GetStatus(), topics.Topic_LIVE_TRAIN_DATA.String())
      }
    }
    // Waiting time until the same trainId leaves the first station again
    realtime := time.Duration(60 * 60 * rand.Intn(10) + 5)
    time.Sleep(realtime / time.Duration(gen.Speedup))
  }
}

func (gen *Generator) GenerateTransports(g *graph.Mutable) map[int64][]int{
  transports := make(map[int64][]int, gen.TransportIDs)
  for int64(len(transports)) < gen.TransportIDs{
    tid := rand.Int63n(gen.TransportIDs*gen.Nodes)
    start := rand.Int63n(gen.Nodes)
    end := rand.Int63n(gen.Nodes)
    path, _ := graph.ShortestPath(g, int(start), int(end))
    transports[tid] = path
  }
  return transports
}



func (gen *Generator) Serve(ctx context.Context) (err error) {
	log.Info("Connecting to Kafka...")
	gen.producer, err = kafkaproducer.Create(ctx, gen.ProducerConfig)
	if err != nil {
		return fmt.Errorf("failed to create kafka producer: %v", err)
	}
	log.Info("Connected")

	g := gen.GenerateTransportNetwork()
	gen.Transports = gen.GenerateTransports(g)

  keys := make([]int64, 0, len(gen.Transports))
  for k := range gen.Transports {
      keys = append(keys, k)
  }

	var wg sync.WaitGroup
	for _, tid := range keys {
		wg.Add(1)
		go gen.StartProducing(&wg, tid)
	}

	wg.Wait()
	return nil
}

// Shutdown ...
func (gen *Generator) Shutdown() {
	log.Info("Graceful shutdown")
	for _ = range gen.Transports {
      gen.done <- true
  }
	if gen.producer != nil {
		_ = gen.producer.Close()
	}
}

func main() {
	var cliFlags []cli.Flag
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaproducer.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.Int64Flag{
			Name:    "speed",
			Value:   1,
			Aliases: []string{"speedup", "factor"},
			EnvVars: []string{"SPEEDUP", "FACTOR"},
			Usage:   "speedup factor (1x=realtime, 2x=twice as fast as realtime, 0x=benchmark)",
		},
		&cli.IntFlag{
			Name:    "jitter",
			Value:   0, // No jitter
			Aliases: []string{"variance", "jitter-minutes"},
			EnvVars: []string{"JITTER", "VARIANCE"},
			Usage:   "random lag in seconds to be added to scheduled events to simulate transmission jitter",
		},
		&cli.Int64Flag{
			Name:    "transport-ids",
			Value:    100,
			Aliases: []string{"ids", "transports"},
			EnvVars: []string{"TRANSPORT_IDS"},
			Usage:   "number of train ids created to drive through the generated network",
		},
		&cli.Int64Flag{
			Name:    "nodes",
			Value:   10000,
			Aliases: []string{"node-count", "num-nodes", "network-size"},
			EnvVars: []string{"NODES", "NODE_COUNT", "NETWORK_SIZE"},
			Usage:   "number of nodes in the generated transport network",
		},
		&cli.Int64Flag{
			Name:    "edges",
			Aliases: []string{"edge-count", "num-edges", "routes"},
			EnvVars: []string{"NODES", "NODE_COUNT", "NETWORK_SIZE"},
			Usage:   "number of edges (routes) in the generated transport network (default: nodes*nodes/2)",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA mock transport data generator",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Generates fake source transport events on a randomly generated transportation network for benchmarking purposes",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			if logLevel, err := log.ParseLevel(ctx.String("log")); err == nil {
				log.SetLevel(logLevel)
			}
			gen := Generator{
				ProducerConfig: kafkaproducer.Config{}.ParseCli(ctx),
				Config: Config{
					Speedup:      ctx.Int64("speed"),
					Jitter:   ctx.Int("jitter"),
          //TransportIDs: []string{},
          TransportIDs: ctx.Int64("transport-ids"),
					Nodes:        ctx.Int64("nodes"),
					Edges:        ctx.Int64("edges"),
				},
				done: make(chan bool),
			}

			if gen.Config.Edges < 1 {
				gen.Config.Edges = gen.Config.Nodes * 2
			}

      if gen.Nodes < gen.TransportIDs {
        log.Fatal("It is not possible to generate representable data by having less nodes than generated transports")
      }

      /*
      Old way to handle ids
			for _, tid := range strings.Split(ctx.String("transport-ids"), ",") {
				if trimmed := strings.TrimSpace(tid); trimmed != "" {
					gen.Config.TransportIDs = append(gen.Config.TransportIDs, trimmed)
				}
			}
      */

			log.Info(pp.Sprintln(gen.Config))

			// Register shutdown routine
			setupCtx, cancelSetup := context.WithCancel(context.Background())
			shutdown := make(chan os.Signal)
			signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
			go func() {
				<-shutdown
				cancelSetup()
				gen.Shutdown()
			}()
			return gen.Serve(setupCtx)
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
