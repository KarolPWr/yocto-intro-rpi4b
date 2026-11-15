from flask import Flask

app = Flask(__name__)


def read_file(path):
    with open(path) as f:
        return f.read().strip()


@app.route('/')
def hello_world():
    return 'Hello, from Flask on Yocto!'


@app.route('/stats')
def stats():
    # temperatura w milistopniach C
    temp_c = int(read_file('/sys/class/thermal/thermal_zone0/temp')) / 1000
    # pierwsza liczba w /proc/uptime to sekundy od startu systemu
    uptime_s = int(float(read_file('/proc/uptime').split()[0]))
    # słownik flask zamienia na odpowiedź JSON
    return {'temp_c': temp_c, 'uptime_s': uptime_s}


if __name__ == '__main__':
    # 0.0.0.0 - serwer dostępny z sieci, nie tylko z płytki
    app.run(host='0.0.0.0', port=5000)
