# Suplement: pomiar całego startu na UART

`systemd-analyze` liczy czas od startu kernela. Firmware RPi i U-Boot nie przekazują kernelowi swoich czasów
(na PC z UEFI `systemd-analyze` pokazuje też `firmware` i `loader`). Cały start - od podłączenia zasilania do
`login:` - zmierzymy na hoście, z czasem każdej linii, która przychodzi z UART.

## Pomiar

Do znaczników czasu użyjemy `ts` z pakietu `moreutils`:

    sudo apt install moreutils

Zamknij picocom (port może mieć otwarty tylko jeden program). Ustaw port i zapisuj wszystko, co przychodzi z UART,
z czasem od uruchomienia komendy:

    stty -F /dev/ttyUSB0 115200 raw -echo
    ts -s '%.s' < /dev/ttyUSB0 | tee start_1.log

Zaraz po uruchomieniu `ts` podłącz zasilanie. Gdy pojawi się `login:`, przerwij (`Ctrl+C`). Nie loguj się.

Ostatnia linia z czasem to linia nad `login:`:

    tail -3 start_1.log

Czas liczy się od uruchomienia `ts`, więc zawiera Twój czas reakcji przy zasilaniu - mierz 3 razy i bierz medianę.

## Etapy

Firmware RPi 4 nic nie wypisuje na UART - pierwsza linia w logu to U-Boot albo (bez U-Boota) kernel. Granice etapów:

    grep -E 'U-Boot 20|Starting kernel' start_1.log

- od zera do `U-Boot 20...` - firmware RPi,
- od `U-Boot 20...` do `Starting kernel` - U-Boot,
- dalej - kernel i userspace (to samo, co `systemd-analyze time`).

Bez U-Boota i z `quiet` w `cmdline.txt` kernel nic nie wypisuje - wtedy widać tylko czas do `login:`.

| Etap | Skąd | Z U-Bootem | Bez U-Boota |
|---|---|---|---|
| firmware RPi | do `U-Boot 20...` / do pierwszej linii kernela | | |
| U-Boot | do `Starting kernel` | | - |
| razem do `login:` | ostatnia linia | | |

Na UART widać też zmiany, których `systemd-analyze` nie pokazuje: `boot_delay`, `disable_splash` (firmware) i samego
U-Boota.

Źródła:
- `moreutils` - `man ts`
- [systemd-analyze(1)](https://www.freedesktop.org/software/systemd/man/latest/systemd-analyze.html) (`firmware`, `loader`)
- U-Boot v2026.01 `arch/arm/lib/bootm.c` (`Starting kernel ...`)
