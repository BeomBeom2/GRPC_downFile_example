import com.google.protobuf.ByteString
import com.google.protobuf.compiler.PluginProtos
import io.grpc.stub.StreamObserver
import com.main.file_proto.file_proto
import com.main.file_proto.FileServiceGrpc
import io.grpc.ServerBuilder
import java.io.*
import java.lang.Thread.sleep

fun main() {
    println("in main()")
    val service = FileService()
    val server = ServerBuilder
        .forPort(10004) // ServerBuilder.forPort()에서  ProviderNotFoundException 발생, grpc-netty or grpc-netty-shaded를 종속성에 추가
        .addService(service)
        .build()

    println("[GRPC] server starts()")
    server.start()
    server.awaitTermination()

}

class FileService : FileServiceGrpc.FileServiceImplBase() {
    override fun sayHello(request: file_proto.HelloRequest?, responseObserver: StreamObserver<file_proto.HelloResponse>?) {
        println("sayHello(${request?.greeting})")

        val response = file_proto.HelloResponse.newBuilder().setReply("hello, Client").build()
        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun fileDown(request: file_proto.HelloRequest?, responseObserver: StreamObserver<file_proto.file_Response>?) {
        val BUF_SIZE = 4096;
        var buffer = ByteArray(BUF_SIZE)
        val input : InputStream = BufferedInputStream(FileInputStream("C:\\Server\\zlib-1.2.3.exe"));
        println("[GRPC, Server] file down ...")
        var nRead : Int  = input.read(buffer);
        while (nRead > 0)
        {
            val resp = file_proto.file_Response.newBuilder()
                .setFileBin(ByteString.copyFrom(buffer))
                .setFileSize(nRead)
                .build()
            responseObserver?.onNext(resp)
            sleep(50)
            println("size is - ${nRead}")
            nRead = input.read(buffer);
        }
        input.close();
        responseObserver?.onCompleted()
    }

    override fun reqRes(responseObserver: StreamObserver<file_proto.HelloResponse>?): StreamObserver<file_proto.HelloRequest> {
        return object : StreamObserver<file_proto.HelloRequest> {
            override fun onNext(value: file_proto.HelloRequest?) {
                    val resp = file_proto.HelloResponse.newBuilder()
                        .setReply("Response to Client - (${value?.greeting})")
                        .build()
                    responseObserver?.onNext(resp)
                    Thread.sleep(100)
            }
            override fun onError(t: Throwable?) {
                println("[GRPC, Server] bidiHello() - onError()")
            }
            override fun onCompleted() {
                println("[GRPC, Server] bidiHello() - onCompleted()")
                responseObserver?.onCompleted()
            }
        }
    }

    override fun serverVersionInfo(request: file_proto.ver_Request?, responseObserver: StreamObserver<file_proto.ver_Response>?) {
        for(i in 0 until 5) {
            val resp = file_proto.ver_Response.newBuilder()
                .setVersion("vi.0.0.$i")
                .setFileInfo("version explain - $i")
                .setFileInfo("hash - ${i*3}")
                .build()
            responseObserver?.onNext(resp)

            Thread.sleep(100)
        }
        responseObserver?.onCompleted()
    }

    override fun clientVersionInfo(responseObserver: StreamObserver<file_proto.ver_Response>?): StreamObserver<file_proto.ver_Request> {
        return object : StreamObserver<file_proto.ver_Request> {

            override fun onNext(value: file_proto.ver_Request?) {
                println("[GRPC, Server] Client version is ${value?.version}\n")
                println("[GRPC, Server] Client fileInfo is ${value?.fileInfo}\n")
            }

            override fun onError(t: Throwable?) {
                println("[GRPC, Server] lotsOfGreetings() - onError()")
            }

            override fun onCompleted() {
                println("[GRPC, Server] lotsOfGreetings() - onCompleted()")

                val response = file_proto.ver_Response.newBuilder().setVersion("v1.0.0.3").build()
                responseObserver?.onNext(response)
                responseObserver?.onCompleted()
            }
        }
    }
}
