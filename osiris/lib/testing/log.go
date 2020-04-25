package testing

import "github.com/testcontainers/testcontainers-go"

// LogCollector ...
type LogCollector struct {
	Messages []string
}

// Accept ...
func (c *LogCollector) Accept(l testcontainers.Log) {
	c.Messages = append(c.Messages, string(l.Content))
}
