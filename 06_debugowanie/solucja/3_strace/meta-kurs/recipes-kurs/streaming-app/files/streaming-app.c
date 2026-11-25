#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

// "zamknięty" program od dostawcy - ścieżka do licencji jest zaszyfrowana
static const unsigned char lic[] = {
    0x75, 0x2e, 0x37, 0x2a, 0x75, 0x74, 0x2c, 0x3f, 0x34, 0x3e,
    0x35, 0x28, 0x05, 0x36, 0x33, 0x39, 0x3f, 0x34, 0x29, 0x3f,
};

int main(void)
{
    char path[sizeof(lic) + 1];

    for (size_t i = 0; i < sizeof(lic); i++)
        path[i] = lic[i] ^ 0x5a;
    path[sizeof(lic)] = '\0';

    printf("streaming-app: start\n");

    int fd = open(path, O_RDONLY);
    if (fd < 0) {
        printf("brak licencji - tryb Freemium (tylko zwiastuny)\n");
        return 0;
    }
    close(fd);

    printf("licencja OK - tryb Premium (cały katalog)\n");
    return 0;
}
