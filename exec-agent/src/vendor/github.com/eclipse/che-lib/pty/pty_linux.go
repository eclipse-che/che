package pty

import (
	"os"
	"strconv"
	"syscall"
	"unsafe"
)

func open() (pty, tty *os.File, err error) {
	p, err := os.OpenFile("/dev/ptmx", os.O_RDWR, 0)
	if err != nil {
		return nil, nil, err
	}

	sname, err := ptsname(p)
	if err != nil {
		return nil, nil, err
	}

	err = unlockpt(p)
	if err != nil {
		return nil, nil, err
	}

	t, err := os.OpenFile(sname, os.O_RDWR|syscall.O_NOCTTY, 0)
	if err != nil {
		return nil, nil, err
	}
	return p, t, nil
}

func ptsname(f *os.File) (string, error) {
	var n _C_uint
	err := ioctl(f.Fd(), syscall.TIOCGPTN, uintptr(unsafe.Pointer(&n)))
	if err != nil {
		return "", err
	}
	return "/dev/pts/" + strconv.Itoa(int(n)), nil
}

func unlockpt(f *os.File) error {
	var u _C_int
	// use TIOCSPTLCK with a zero valued arg to clear the slave pty lock
	return ioctl(f.Fd(), syscall.TIOCSPTLCK, uintptr(unsafe.Pointer(&u)))
}

func setsize(f *os.File, rows uint16, cols uint16) error {
	var ws winsize
	ws.ws_row = rows
	ws.ws_col = cols
	return ioctl(f.Fd(), syscall.TIOCSWINSZ, uintptr(unsafe.Pointer(&ws)))
}
