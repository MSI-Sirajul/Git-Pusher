import http.server
import ssl

# Set the port for the HTTPS server (e.g., 4443)
PORT = 4443

# Create the HTTP server
httpd = http.server.HTTPServer(('localhost', PORT), http.server.SimpleHTTPRequestHandler)

# Create an SSL context
context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
context.load_cert_chain(certfile="cert.pem", keyfile="key.pem")

# Wrap the HTTP server's socket with SSL
httpd.socket = context.wrap_socket(httpd.socket, server_side=True)

# Start the HTTPS server
print(f"Serving HTTPS on port {PORT}...")
httpd.serve_forever()

