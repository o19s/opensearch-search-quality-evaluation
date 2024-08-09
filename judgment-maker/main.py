from flask import Flask

import notebook

app = Flask(__name__)


@app.route('/make')
def make():
	return notebook.make()


if __name__ == '__main__':
	app.run(host='0.0.0.0', port=8000)
