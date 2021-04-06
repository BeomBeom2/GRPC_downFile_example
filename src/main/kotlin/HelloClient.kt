import com.google.protobuf.ByteString.copyFrom
import com.main.file_proto.file_proto
import com.main.file_proto.FileServiceGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.io.*
import java.lang.Thread.sleep

fun main() {
    println("[GRPC, Client] main()")
    val channel = ManagedChannelBuilder
        .forAddress("localhost", 10004)
        .usePlaintext()
        .build()

    val stub = FileServiceGrpc.newBlockingStub(channel)
    val response = stub.sayHello(getHelloRequest("hello, server"))
    sleep(3000);
    println("[GRPC, Client] response(${response.reply})")

    println("\n###########################################################")
    println("###########################################################\n")
    println("[GRPC, Client] file down")

    val BUF_SIZE = 4096;
    var buffer = ByteArray(BUF_SIZE)
    var size : Int = 0
    val output : OutputStream = BufferedOutputStream(FileOutputStream("C:\\Client\\zlib-1.2.3.exe"))

    println("[GRPC, Server] file down ...")

    val response1 = stub.fileDown(getHelloRequest("Client file request"))

    response1.forEach {

        buffer = it!!.fileBin.toByteArray()
        size = it!!.fileSize
        println("get file size is - ${size}")

        output.write(buffer,  0, size )
    }
    output.close();

    println("\n###########################################################")
    println("###########################################################\n")

    println("[GRPC, Client] get Server Version Info")

    val asyncStub = FileServiceGrpc.newStub(channel)

    val response_1 = stub.serverVersionInfo(file_proto.ver_Request.newBuilder()
        .setVersion("v1.0.0.1")
        .build())

    response_1.forEach {
        println("[GRPC, Client] response: ${it.version}")
        println("[GRPC, Client] response: ${it.fileInfo}")
        println("[GRPC, Client] response: ${it.hash}")
    }

    println("\n###########################################################")
    println("###########################################################\n")

    println("[GRPC, Client] Client Version Info")

    val requestObserver_1 = asyncStub.clientVersionInfo(VersionResponseStreamObserver())

    for(i in 0 until 2) {
        requestObserver_1.onNext(file_proto.ver_Request.newBuilder()
            .setVersion("vi.0.0.$i")
            .setFileInfo("version explain - $i")
            .setFileInfo("hash - ${i*3}")
            .build())
        sleep(100)
    }
    requestObserver_1.onCompleted()

    println("\n###########################################################")
    println("###########################################################\n")

    println("[GRPC, Client] Client Stream to Stream ")

    val requestObserver_2 = asyncStub.reqRes(HelloResponseStreamObserver())

    for(i in 0 until 3) {
        requestObserver_2.onNext(file_proto.HelloRequest.newBuilder()
            .setGreeting("\n ${i}th Client send")
            .build())
        sleep(100)
    }
    requestObserver_2.onCompleted()

    channel.shutdown()
    println("[GRPC, Client] channel is shutdown")
}

fun getHelloRequest(greeting: String): file_proto.HelloRequest {
    return file_proto.HelloRequest.newBuilder()
        .setGreeting(greeting)
        .build()
}

class HelloResponseStreamObserver : StreamObserver<file_proto.HelloResponse> {
    override fun onNext(value: file_proto.HelloResponse?) {
            println("[GRPC, Client] reqRes = ${value?.reply}")
    }

    override fun onError(t: Throwable?) {
        println("[GRPC, Client] reqRes onError()")
    }
    override fun onCompleted() {
        println("[GRPC, Client] reqRes onCompleted()")
    }
}

class VersionResponseStreamObserver : StreamObserver<file_proto.ver_Response> {
    override fun onNext(value: file_proto.ver_Response?) {
        println("[GRPC, Client] send version = ${value?.version}")
    }

    override fun onError(t: Throwable?) {
        println("[GRPC, Client] send version onError()")
    }
    override fun onCompleted() {
        println("[GRPC, Client] send version onCompleted()")
    }
}
