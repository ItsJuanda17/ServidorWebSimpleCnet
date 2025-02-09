import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServidorWebSimple{
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server waiting for connection...");
        while(true){
        Socket socket = serverSocket.accept();
        System.out.println("Connection accepted");  
        SolicitudesHttp solicitudesHttp = new SolicitudesHttp(socket);
        Thread hilo = new Thread(solicitudesHttp);
        hilo.start();
        }
    }

}