package process_test

import (
	"fmt"
	"github.com/eclipse/che/exec-agent/process"
	"io/ioutil"
	"os"
	"testing"
)

func TestLogsDistributorCreatesSubdirectories(t *testing.T) {
	baseDir := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer os.RemoveAll(baseDir)

	distributor := process.DefaultLogsDistributor{
		MaxDirsCount: 4,
	}

	dir, err := distributor.DirForPid(baseDir, 1)
	if err != nil {
		t.Fatal(err)
	}

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		t.Fatal("Expected that logs file subdirectory was created")
	} else if err != nil {
		t.Fatal(err)
	}
}

func TestLogsDistribution(t *testing.T) {
	baseDir := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer os.RemoveAll(baseDir)

	distributor := process.DefaultLogsDistributor{
		MaxDirsCount: 4,
	}

	// Those files should be evenly distributed in 4 directories
	for pid := 1; pid <= 16; pid++ {
		dir, err := distributor.DirForPid(baseDir, uint64(pid))
		if err != nil {
			t.Fatal(err)
		}
		filename := fmt.Sprintf("%s%cpid-%d", dir, os.PathSeparator, pid)
		if _, err := os.Create(filename); err != nil {
			t.Fatal(err)
		}
	}

	for i := 0; i < 4; i++ {
		dir := fmt.Sprintf("%s%c%d", baseDir, os.PathSeparator, i)
		fi, err := ioutil.ReadDir(dir)
		if err != nil {
			t.Fatal(err)
		}
		if len(fi) != 4 {
			t.Fatalf("Expected directory '%s' to contain 4 files", dir)
		}
	}
}
