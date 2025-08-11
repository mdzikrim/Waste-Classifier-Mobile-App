from flask import Flask, render_template, request, jsonify
from tensorflow.keras.models import load_model
import numpy as np
from PIL import Image
import io

app = Flask(__name__)
model = load_model('best_model_mobilenetv2.h5')

label_level1_map = {0: 'Organik', 1: 'Anorganik'}
label_level2_map = {
    0: 'Cangkang Telur', 1: 'Elektronik', 2: 'Kaca', 3: 'Kain', 4: 'Kardus',
    5: 'Karet', 6: 'Kayu', 7: 'Kertas', 8: 'Kotoran Hewan', 9: 'Logam',
    10: 'Plastik', 11: 'Sepatu', 12: 'Sisa Buah', 13: 'Sisa Teh Kopi',
    14: 'Sisa makanan', 15: 'Styrofoam', 16: 'Tumbuhan'
}

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400
    file = request.files['file']
    try:
        img = Image.open(io.BytesIO(file.read())).convert('RGB').resize((224,224))
    except Exception as e:
        return jsonify({'error': f'Cannot process image: {e}'}), 400
    img_array = np.expand_dims(np.array(img)/255.0, axis=0)
    pred1, pred2 = model.predict(img_array)
    kategori = label_level1_map[int(pred1[0][0] > 0.5)]
    subkategori = label_level2_map[np.argmax(pred2)]
    return jsonify({'kategori': kategori, 'subkategori': subkategori})

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0',port=5000)
