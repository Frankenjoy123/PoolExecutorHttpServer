import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by frank on 2017/8/20.
 */
public class ServerStarter {

    public static void main(String[] args){
        ServerSocket serverSocket;
        try{
            serverSocket = new ServerSocket(8081);
            ExecutorService pool = Executors.newFixedThreadPool(6);
            while (true){
                //���ܿͻ�������
                Socket socket = serverSocket.accept();
                //ServerRunnable ���н���http ����
                ServerRunnable hs = new ServerRunnable(socket);
                Thread thread = new Thread(hs);
                // thread.start();
                pool.execute(thread);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
