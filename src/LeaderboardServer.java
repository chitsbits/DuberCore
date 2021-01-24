import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class LeaderboardServer implements Runnable {

    ServerSocket serverSock;
    boolean running;

    @Override
    public void run() {
        running = true;
        Socket clientSock = null;
        try {
            serverSock = new ServerSocket(5000); // open a socket on port 5000
            serverSock.setSoTimeout(5000);       // Set timeout to 5 seconds
            while (running) {
                try {
                    System.out.println("Waiting for client connection...");
                    clientSock = serverSock.accept();                  // Blocks for 5 seconds
                    new Thread(new ClientHandler(clientSock)).start(); // Hand over the client to a handler thread
                }
                catch (SocketTimeoutException e1){
                    System.out.println("Timeout");
                }
            }
            System.out.println("Server shutting down.");
        }
        catch (IOException e) {
            System.out.println("Error accepting client");
            e.printStackTrace();
            try {
                clientSock.close();
            }
            catch (IOException e1) {
                System.out.println("Error closing socket.");
                e1.printStackTrace();
            }
            System.exit(-1);
        }
    }

    class ClientHandler implements Runnable {

        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        Socket client;

        public ClientHandler(Socket sock) {
            this.client = sock;
        }

        @Override
        public void run() {

            // Open streams
            try {
                outputStream = new ObjectOutputStream(client.getOutputStream());
                inputStream = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                System.out.println("Error opening streams.");
                e.printStackTrace();
            }

            // Recieve packet
            try {
                Object packet = inputStream.readObject();
                if (packet instanceof GetLeaderboard){
                    System.out.println("Get packet recieved");
                    outputStream.writeObject(LeaderboardServer.fetchTopLeaderboard());
                    outputStream.flush();
                }
                else if (packet instanceof WriteLeaderboard){
                    System.out.println("Write packet recieved");
                    WriteLeaderboard record = (WriteLeaderboard) packet;
                    LeaderboardServer.addRecord(record.name, record.score);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }

            // Close socket (and its streams)
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Error closing sockets");
                e.printStackTrace();
            }
        }
    }

    public static void createDatabase() {
        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
            System.out.println("Database opened");

            statement = connection.createStatement();
            String sql = "CREATE TABLE LEADERBOARD " + "(" + "NAME           TEXT    NOT NULL, "
                    + "SCORE          INT     NOT NULL" + ")";

            statement.executeUpdate(sql);
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Database created successfully");
    }

    public static void addRecord(String name, int score) {

        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            String sql = "INSERT INTO LEADERBOARD (NAME,SCORE) " + "VALUES ('" + name + "', " + Integer.toString(score)
                    + ");";
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Record added successfully");

    }

    public static String fetchTopLeaderboard() {
        Connection connection;
        Statement statement;
        String returnString = "";
        
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM LEADERBOARD ORDER BY SCORE DESC;");

            int i = 0;
            while (set.next() && i < 10) {
                String name = set.getString("NAME");
                int score = set.getInt("SCORE");
                returnString += "Name: " + name + "\tScore: " + Integer.toString(score) + "\n";
                i++;
            }
            set.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("top 10 fetched");
        return returnString;
    }

    public static void main(String[] args) {

        //createDatabase();
        LeaderboardServer server = new LeaderboardServer();
        new Thread(server).start();

        System.out.println("Server has been started.");
        Scanner input = new Scanner(System.in);
        while (server.running) {
            System.out.println("Type \"stop\" to close server");
            if (input.next().equals("stop")) {
                server.running = false;
                System.out.println("stopping...");
            }
        }
        input.close();
    
        // fetchTopLeaderboard();

    }

}
