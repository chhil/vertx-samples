package chhil.vertx.rxexample;

import io.vertx.book.http.FrameToken;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.example.util.Runner;

/**
 * @author Murtuza
 *         This is a non Rx example.
 */
public class TCPServer extends AbstractVerticle {

    private FrameToken expectedToken = FrameToken.SIZE;
    RecordParser       parser, frameParser = null;


    private Handler<NetSocket> getTwoByteLengthHandler() {
        return socket -> {
            parser = RecordParser.newFixed(2);

            Handler<Buffer> handler = new Handler<Buffer>() {

                int size = -1;

                public void handle(Buffer buff) {
                    if (size == -1) {
                        byte[] b = buff.getBytes(0, 2);
                        size = (((b[0]) & 0xFF) << 8) | ((b[1]) & 0xFF);
                        System.out.println("size: " + size);

                        parser.fixedSizeMode(size);
                    }
                    else {

                        byte[] dest = new byte[size];
                        buff.getBytes(dest);
                        System.out.println(new String(dest));
                        parser.fixedSizeMode(2);

                        size = -1;

                    }
                }
            };
            parser.setOutput(handler);
            socket.handler(parser);
        };
    }

    @Override
    public void start() throws Exception {

        NetServerOptions opt = new NetServerOptions();
        opt.setIdleTimeout(500); // large to assist in debugging for now
        NetServer server = vertx.createNetServer(opt);

        // server.connectHandler(getServerHandler());
        server.connectHandler(getTwoByteLengthHandler());

        server.listen(8888, "localhost", new Handler<AsyncResult<NetServer>>() {

            @Override
            public void handle(AsyncResult<NetServer> res) {

                if (res.succeeded()) {
                    System.out.println("Server is now listening!");
                }
                else {
                    System.out.println("Failed to bind!");
                }

            }
        });

    }

    public static void main(String[] args) {

        Runner.runExample(TCPServer.class);

    }

}
