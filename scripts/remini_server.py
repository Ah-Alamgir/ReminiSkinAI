"""
ReminiSkinAI — Local Neural Inference API Server
Exposes high-speed native Python ONNX inference for CodeFormer & GPEN to the web UI.

Endpoints:
  GET  /ping    -> Health check & active models
  POST /enhance -> Executes CodeFormer or GPEN with deep face detection & feathering
"""

import sys, os, io, json, base64, time
from http.server import HTTPServer, BaseHTTPRequestHandler
from PIL import Image

# Ensure scripts dir is on sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import remini_model_runner

HOST = '127.0.0.1'
PORT = 8000

class ReminiAPIHandler(BaseHTTPRequestHandler):
    def _send_cors_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')

    def do_OPTIONS(self):
        self.send_response(204)
        self._send_cors_headers()
        self.end_headers()

    def do_GET(self):
        if self.path == '/ping' or self.path == '/':
            res = {
                'status': 'ok',
                'server': 'ReminiSkinAI Local Neural Bridge',
                'version': '2.8.0',
                'models': ['codeformer', 'gpen'],
                'device': 'Native ONNX Engine'
            }
            body = json.dumps(res).encode('utf-8')
            self.send_response(200)
            self._send_cors_headers()
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == '/enhance':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            
            try:
                payload = json.loads(post_data.decode('utf-8'))
                img_data_b64 = payload.get('image', '')
                model_name = payload.get('model', 'codeformer').lower()
                fidelity = float(payload.get('fidelity', 0.70))

                # Strip base64 header if present
                if ',' in img_data_b64:
                    img_data_b64 = img_data_b64.split(',', 1)[1]

                img_bytes = base64.b64decode(img_data_b64)
                input_img = Image.open(io.BytesIO(img_bytes)).convert('RGB')
                
                t0 = time.time()
                restored_img = remini_model_runner.enhance_full_image(
                    input_img,
                    model=model_name,
                    fidelity=fidelity
                )
                duration = time.time() - t0

                # Encode output
                buf = io.BytesIO()
                restored_img.save(buf, format='JPEG', quality=95)
                out_b64 = base64.b64encode(buf.getvalue()).decode('utf-8')
                out_url = f"data:image/jpeg;base64,{out_b64}"

                response_data = {
                    'status': 'success',
                    'model': model_name,
                    'durationSec': round(duration, 3),
                    'width': restored_img.width,
                    'height': restored_img.height,
                    'image': out_url
                }
                body = json.dumps(response_data).encode('utf-8')
                self.send_response(200)
                self._send_cors_headers()
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', str(len(body)))
                self.end_headers()
                self.wfile.write(body)
            except Exception as e:
                err_res = {'status': 'error', 'message': str(e)}
                body = json.dumps(err_res).encode('utf-8')
                self.send_response(500)
                self._send_cors_headers()
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', str(len(body)))
                self.end_headers()
                self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        # Concise logging
        print(f"[ReminiServer] {self.address_string()} - {format % args}")

def run_server():
    server = HTTPServer((HOST, PORT), ReminiAPIHandler)
    print(f"===========================================================")
    print(f"  ReminiSkinAI Neural Inference Server v2.8.0")
    print(f"  Listening on http://{HOST}:{PORT}")
    print(f"  Available models: CodeFormer (sczhou) & GPEN (yangxy)")
    print(f"===========================================================")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        server.server_close()

if __name__ == '__main__':
    run_server()
