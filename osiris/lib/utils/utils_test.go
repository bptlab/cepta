package utils

import (
	"reflect"
	"testing"

	replayerpb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/romnnn/deepequal"
)

const parallel = true


func TestIsValidEmail(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	for _, sample := range []struct {
		Input    string
		Expected bool
	}{
		{"cepta@cepta.org", true},
		{"a@b.de", true},
		{"a@b", true},
		{"a", false},
		{"@b", false},
		{"@", false},
		{"a@b@", false},
	} {
		if valid := IsValidEmail(sample.Input); valid != sample.Expected {
			t.Errorf("Expected IsValidEmail(%s) = %v but got %v", sample.Input, sample.Expected, valid)
		}
	}
}

func TestMaxInt64(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	type InputType = struct {
		A int64
		B int64
	}
	for _, sample := range []struct {
		Input    InputType
		Expected int64
	}{
		{InputType{10, 20}, 20},
		{InputType{-2, 0}, 0},
	} {
		if m := MaxInt64(sample.Input.A, sample.Input.B); m != sample.Expected {
			t.Errorf("Expected MaxInt64(%d, %d) = %d but got %d", sample.Input.A, sample.Input.B, sample.Expected, m)
		}
	}
}

func TestAbsInt64(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	for _, sample := range []struct {
		Input    int64
		Expected int64
	}{
		{10, 10},
		{-2, 2},
		{0, 0},
	} {
		if a := AbsInt64(sample.Input); a != sample.Expected {
			t.Errorf("Expected AbsInt64(%d) = %d but got %d", sample.Input, sample.Expected, a)
		}
	}
}

func TestIsNilInterface(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	var test *string
	for _, sample := range []struct {
		Input    interface{}
		Expected bool
	}{
		{nil, true},
		{test, true},
		{12, false},
	} {
		if isNil := IsNilInterface(sample.Input); isNil != sample.Expected {
			t.Errorf("Expected IsNilInterface(%v) = %v but got %v", sample.Input, sample.Expected, isNil)
		}
	}
}

func TestInPlusMinusRange(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	type InputType = struct {
		C int64
		A int64
		V int64
	}
	for _, sample := range []struct {
		Input    InputType
		Expected bool
	}{
		{InputType{0, 10, 5}, true},
		{InputType{10, 0, 5}, false},
		{InputType{10, 0, 10}, true},
		{InputType{-1, 1, 0}, true},
		{InputType{-1, 10, -7}, true},
	} {
		if inRange := InPlusMinusRange(sample.Input.C, sample.Input.A, sample.Input.V); inRange != sample.Expected {
			t.Errorf("Expected InPlusMinusRange(%d, %d, %d) = %d but got %v", sample.Input.C, sample.Input.A, sample.Input.V, sample.Expected, inRange)
		}
	}
}

func TestContains(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	type InputType = struct {
		A []string
		X string
	}
	for _, sample := range []struct {
		Input    InputType
		Expected bool
	}{
		{InputType{[]string{"a", "b", "c"}, "a"}, true},
		{InputType{[]string{"a", "b", "c"}, "b"}, true},
		{InputType{[]string{"a", "b", "c"}, "B"}, false},
		{InputType{[]string{"a", "b", "  "}, ""}, false},
		{InputType{[]string{}, "a"}, false},
	} {
		if c := Contains(sample.Input.A, sample.Input.X); c != sample.Expected {
			t.Errorf("Expected Contains(%s, %s) = %v but got %v", sample.Input.A, sample.Input.X, sample.Expected, c)
		}
	}
}

func TestUnique(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	for _, sample := range []struct {
		Input    []int
		Expected []int
	}{
		{[]int{1, 2, 1}, []int{1, 2}},
		{[]int{1, 2, 1, 2, 1, 1, 1}, []int{1, 2}},
	} {
		if equal, err := deepequal.DeepEqual(Unique(sample.Input), sample.Expected); !equal {
			t.Errorf("Expected Unique(%s) to be %v but got: %v: %v", sample.Input, sample.Expected, Unique(sample.Input), err)
		}
	}
}

func TestGetUserToken(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	// TODO
}

// TestProtoReflection ...
func TestProtoReflection(t *testing.T) {
	if parallel {
		t.Parallel()
	}
	fieldName := "Speed"
	s := &replayerpb.Speed{Speed: 20}
	st := reflect.TypeOf(s).Elem()
	f, ok := st.FieldByName(fieldName)
	if !ok {
		t.Fatalf("Failed to get field \"%s\" from proto", fieldName)
	}
	if f.Type != reflect.TypeOf(int32(0)) {
		t.Errorf("Expected type of field \"%s\" to be %v but got %v", fieldName, reflect.TypeOf(int32(0)), f.Type)
	}
}
