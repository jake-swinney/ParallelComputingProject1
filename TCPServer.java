import java.io.*;
import java.net.*;

public class TCPServer
{
    public static void main(String[] args) throws IOException
    {
        // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading form ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Server machine's IP
        String routerName = "192.168.0.100"; // ServerRouter host name - Raspberry Pi
        int SockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try
        {
            Socket = new Socket(routerName, SockNum);
            out = new PrintWriter(Socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        // Variables for message passing
        String fromServer; // messages sent to ServerRouter
        String fromClient; // messages received from ServerRouter
        String address = "192.168.0.103"; // destination IP (Client) - desktop

        // Communication process (initial sends/receives)
        out.println(address); // initial send (IP of the destination Client)
        fromClient = in.readLine();// initial receive from router (verification of connection)
        System.out.println("ServerRouter: " + fromClient);

        // Communication while loop
        while ((fromClient = in.readLine()) != null)
        {
            System.out.println("Client said: " + fromClient);
            if (fromClient.equals("Bye.")) // exit statement
                break;
            else if (fromClient.startsWith("!BYTES:"))
            {
                String numBytesStr = fromClient.substring(7);
                int numBytes = Integer.parseInt(numBytesStr);
                byte[] data = new byte[numBytes];
                
                int bytesRead;
                int current;
                
                System.out.println("Receiving " + numBytes + " bytes.");
                
                InputStream inStream = Socket.getInputStream();
                
                bytesRead = inStream.read(data, 0, numBytes);
                current = bytesRead;
                
                do {
                    bytesRead = inStream.read(data, current, (numBytes - current));
                    if (bytesRead >= 0) current += bytesRead;
                } while (bytesRead > -1);
                
                System.out.println("Bytes transferred.");
                
                FileOutputStream fos = new FileOutputStream("file"); // TODO: name
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(data, 0, numBytes);
                
                System.out.println("Wrote " + numBytes + " bytes to file.");
                
                if (fos != null) fos.close();
                if (bos != null) bos.close();
                break;
            }
            else
            {
                fromServer = fromClient.toUpperCase(); // converting received message to upper case
                System.out.println("Server said: " + fromServer);
                out.println(fromServer); // sending the converted message back to the Client via ServerRouter
            }
        }

        System.out.println("Closing connection.");
        // closing connections
        out.close();
        in.close();
        Socket.close();
    }
}
