package chhil.vertx.rxexample;

import io.reactivex.Single;
import io.reactivex.observers.DisposableSingleObserver;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.net.NetSocket;

/**
 * @author Murtuza
 *
 *         Make sure to use the io.vertx.reactivex.core.AbstractVerticle;
 */
public class TCPRxClient extends AbstractVerticle {

    Single<NetSocket> observableNetSocket;

    private boolean   connected;
    private NetSocket socket;
    DisposableSingleObserver<NetSocket> dispSocketObserver;
    @Override
    public void start() throws Exception {

        observableNetSocket = vertx.createNetClient().rxConnect(8888, "127.0.0.1");
        // A connect attempt is made only once a subscribe is done.

        dispSocketObserver = new DisposableSingleObserver<NetSocket>() {

            @Override
            public void onSuccess(NetSocket t) {
                setSocket(t);
                setConnected(true);

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        observableNetSocket.subscribe(dispSocketObserver);

    }

    public NetSocket getSocket() {
        return socket;
    }

    public void setSocket(NetSocket sock) {
        this.socket = sock;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public static void main(String[] args) throws InterruptedException {

        TCPRxClient client = new TCPRxClient();
        Single<String> deployment = io.vertx.reactivex.core.RxHelper.deployVerticle(Vertx.vertx(), client);

        deployment.subscribe(id -> {
            // Deployed
            System.out.println(id);
        }, err -> {
            // Could not deploy
            err.printStackTrace();
        });

        Thread.sleep(10000);

        if (client.isConnected()) {
            byte[] b = { 0, 4, 0x31, 0x32, 0x33, 0x34 };
            client.getSocket().write(new String(b));
        }
        client.dispSocketObserver.dispose();

        Thread.sleep(10000);

    }

}
