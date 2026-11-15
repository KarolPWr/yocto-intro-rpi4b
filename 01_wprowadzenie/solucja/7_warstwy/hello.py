from flask import Flask

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, from Flask on Yocto!'

if __name__ == '__main__':
    # 0.0.0.0 - serwer dostępny z sieci, nie tylko z płytki
    app.run(host='0.0.0.0', port=5000)
