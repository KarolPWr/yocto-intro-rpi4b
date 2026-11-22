#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define IIO_DIR "/sys/bus/iio/devices"

// szuka urządzenia IIO o podanej nazwie, zwraca jego numer albo -1
static int find_device(const char *name)
{
    char path[128];
    char buf[32];

    for (int i = 0; i < 10; i++) {
        snprintf(path, sizeof(path), IIO_DIR "/iio:device%d/name", i);
        FILE *f = fopen(path, "r");
        if (!f)
            continue;
        if (fgets(buf, sizeof(buf), f)) {
            buf[strcspn(buf, "\n")] = '\0';
            if (strcmp(buf, name) == 0) {
                fclose(f);
                return i;
            }
        }
        fclose(f);
    }
    return -1;
}

// temperatura w tysięcznych częściach stopnia Celsjusza, 0 = OK, -1 = błąd odczytu
static int read_temp(int dev, long *temp)
{
    char path[128];
    char buf[32];

    snprintf(path, sizeof(path), IIO_DIR "/iio:device%d/in_temp_input", dev);
    FILE *f = fopen(path, "r");
    if (!f)
        return -1;
    if (!fgets(buf, sizeof(buf), f)) {
        fclose(f);
        return -1;
    }
    fclose(f);
    *temp = strtol(buf, NULL, 10);
    return 0;
}

int main(int argc, char *argv[])
{
    const char *name = argc > 1 ? argv[1] : "bme280";
    long temp;

    int dev = find_device(name);
    if (dev < 0) {
        fprintf(stderr, "kurs-temp: nie ma urządzenia IIO o nazwie %s\n", name);
        return 1;
    }
    if (read_temp(dev, &temp) < 0) {
        fprintf(stderr, "kurs-temp: nie mogę odczytać temperatury z iio:device%d\n", dev);
        return 1;
    }

    printf("%s: %.2f C\n", name, temp / 1000.0);
    return 0;
}
