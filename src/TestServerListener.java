import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class TestServerListener extends Listener {

    @Override
    public void connected(Connection c){
        System.out.println("[SERVER] >> a client has connected");
    }

    @Override
    public void disconnected(Connection c){
        System.out.println("[SERVER] >> a client has disconnected");
    }

    @Override
    public void received (Connection connection, Object object) {
        // Sent by the client upon connecting
        if (object instanceof JoinGameRequest){
            JoinGameRequest joinRequest = (JoinGameRequest) object;
            System.out.println("[CLIENT] >> " + joinRequest.name);
        }
        
        else if (object instanceof PlayerMovementRequest) {
            PlayerMovementRequest request = (PlayerMovementRequest) object;
        }   

    }
    
}
