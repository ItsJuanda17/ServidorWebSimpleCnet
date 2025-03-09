import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;

final class SolicitudesHttp implements Runnable {
    final static String CRLF = "\r\n";
    private Socket socket;

    public SolicitudesHttp(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.err.println("Error procesando la solicitud: " + e.getMessage());
        }
    }

    private void proceseSolicitud() throws Exception {
        try (
                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String lineaDeSolicitud = in.readLine();
            System.out.println("Solicitud: " + lineaDeSolicitud);

            if (lineaDeSolicitud == null) {
                System.out.println("Solicitud vacía o conexión cerrada por el cliente");
                return;
            }

            String linea;
            while ((linea = in.readLine()) != null && !linea.isEmpty()) {
                System.out.println(linea);
            }

            StringTokenizer tokens = new StringTokenizer(lineaDeSolicitud);
            String method = tokens.nextToken();
            if (!method.equals("GET")) {
                System.out.println("Método no soportado: " + method);
                return;
            }

            String fileName = "./ServidorWebSimple" + tokens.nextToken();
            FileInputStream fis = null;
            boolean fileExists = true;

            try {
                fis = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                fileExists = false;
            }

            String statusLine;
            String contentTypeLine;
            String entityBody = null;

            if (fileExists) {
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
            } else {
                statusLine = "HTTP/1.0 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: text/html" + CRLF;
                entityBody = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD><BODY>404 Not Found</BODY></HTML>";
            }

            enviarString(statusLine, out);
            enviarString(contentTypeLine, out);
            enviarString(CRLF, out);

            if (fileExists) {
                enviarArchivo(fis, out);
                fis.close();
            } else {
                enviarString(entityBody, out);
            }
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    private String contentType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }

    private void enviarString(String data, OutputStream out) throws IOException {
        out.write(data.getBytes(StandardCharsets.UTF_8));
    }

    private void enviarArchivo(FileInputStream fis, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
        }
    }
}